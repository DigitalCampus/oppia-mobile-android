/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.task;

import android.content.Context;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.UpdateActivityListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.xmlreaders.CourseTrackerXMLReader;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateCourseActivityTask extends APIRequestTask<List<Course>, DownloadProgress, EntityResult<List<Course>>> {

    private final boolean singleCourseUpdate;
    private UpdateActivityListener mStateListener;
    private boolean apiKeyInvalidated = false;
    private long userId;

    public UpdateCourseActivityTask(Context ctx, long userId, ApiEndpoint apiEndpoint, boolean singleCourseUpdate) {
        super(ctx, apiEndpoint);
        this.userId = userId;
        this.singleCourseUpdate = singleCourseUpdate;
    }

    @Override
    protected EntityResult<List<Course>> doInBackground(List<Course>... params) {

        List<Course> courses = params[0];
        DownloadProgress dp = new DownloadProgress();

        EntityResult<List<Course>> result = new EntityResult<>();
        result.setEntity(courses);

        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);

            dp.setMessage(course.getShortname());
            dp.setProgress((i+1) * 100 / courses.size());
            publishProgress(dp);

            try {
                DbHelper db = DbHelper.getInstance(this.ctx);
                User user = db.getUser(userId);

                OkHttpClient client = HTTPClientUtils.getClient(ctx);
                String url = apiEndpoint.getFullURL(ctx, course.getTrackerLogUrl());
                Request request = new Request.Builder()
                        .url(HTTPClientUtils.getUrlWithCredentials(url, user.getUsername(), user.getApiKey()))
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    result.setResultMessage(ctx.getString(R.string.error_connection));
                    result.setSuccess(false);

                    if (response.code() == 401) {
                        invalidateApiKey(result);
                        apiKeyInvalidated = true;
                    }

                    return result;

                } else {
                    CourseTrackerXMLReader ctxr;
                    try {
                        String responseString = response.body().string();
                        ctxr = new CourseTrackerXMLReader(responseString);
                        List<TrackerLog> trackers = ctxr.getTrackers(ctx, course.getCourseId(), userId);
                        List<QuizAttempt> quizAttempts = ctxr.getQuizAttempts(course.getCourseId(), userId);

                        if (singleCourseUpdate) {
                            db.resetCourse(course.getCourseId(), userId);
                            db.insertTrackers(trackers);
                            db.insertQuizAttempts(quizAttempts);
                        } else {
                            db.insertOrUpdateTrackers(trackers);
                            db.insertOrUpdateQuizAttempts(quizAttempts);
                        }

                        result.setSuccess(true);

                    } catch (InvalidXMLException e) {
                        Analytics.logException(e);
                        Log.d(TAG, "InvalidXMLException:", e);
                    }
                }

            } catch (javax.net.ssl.SSLHandshakeException e) {
                Log.d(TAG, "InvalidXMLException:", e);
                result.setSuccess(false);
                result.setResultMessage(ctx.getString(R.string.error_connection_ssl));
                return result;
            } catch (SocketTimeoutException cpe) {
                Log.d(TAG, "SocketTimeoutException:", cpe);
                result.setSuccess(false);
                result.setResultMessage(ctx.getString(R.string.error_connection));
                return result;
            } catch (IOException ioe) {
                Log.d(TAG, "IOException:", ioe);
                result.setSuccess(false);
                result.setResultMessage(ctx.getString(R.string.error_connection));
                return result;
            } catch (UserNotFoundException unfe) {
                Log.d(TAG, "UserNotFoundException:", unfe);
                result.setSuccess(false);
                result.setResultMessage(ctx.getString(R.string.error_connection));
                return result;
            }

        }

        return result;
    }

    @Override
    protected void onProgressUpdate(DownloadProgress... obj) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.updateActivityProgressUpdate(obj[0]);
            }
        }
    }

    @Override
    protected void onPostExecute(EntityResult<List<Course>> result) {
        synchronized (this) {
            if (mStateListener != null) {
                if (apiKeyInvalidated)
                    mStateListener.apiKeyInvalidated();
                else
                    mStateListener.updateActivityComplete(result);
            }
        }
    }

    public void setUpdateActivityListener(UpdateActivityListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }
}
