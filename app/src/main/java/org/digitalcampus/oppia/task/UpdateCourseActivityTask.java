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
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.xmlreaders.CourseTrackerXMLReader;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateCourseActivityTask extends APIRequestTask<Course, DownloadProgress, EntityResult<Course>> {

	private UpdateActivityListener mStateListener;
    private boolean apiKeyInvalidated = false;
	private long userId;

	public UpdateCourseActivityTask(Context ctx, long userId, ApiEndpoint apiEndpoint) {
        super(ctx, apiEndpoint);
		this.userId = userId;
	}

    @Override
	protected EntityResult<Course> doInBackground(Course... params) {
		
		Course course = params[0];
		DownloadProgress dp = new DownloadProgress();

        EntityResult<Course> result = new EntityResult<>();
        result.setEntity(course);

		try {
			DbHelper db = DbHelper.getInstance(this.ctx);
			User u = db.getUser(userId);

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(apiEndpoint.getFullURL(ctx, course.getTrackerLogUrl()))
                    .addHeader(HTTPClientUtils.HEADER_AUTH,
                            HTTPClientUtils.getAuthHeaderValue(u.getUsername(), u.getApiKey()))
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()){
                result.setResultMessage(ctx.getString(R.string.error_connection));
                result.setSuccess(false);

                if (response.code() == 401){
                    invalidateApiKey(result);
                    apiKeyInvalidated = true;
                }
            }
            else{
                CourseTrackerXMLReader ctxr;
                try {
                    ctxr = new CourseTrackerXMLReader(response.body().string());
                    db.resetCourse(course.getCourseId(), userId);
                    db.insertTrackers(ctxr.getTrackers(course.getCourseId(), userId));
                    db.insertQuizAttempts(ctxr.getQuizAttempts(course.getCourseId(), userId));
                    dp.setProgress(100);
                    dp.setMessage(ctx.getString(R.string.download_complete));
                    publishProgress(dp);
                    result.setSuccess(true);

                } catch (InvalidXMLException e) {
                    Analytics.logException(e);
                    Log.d(TAG, "InvalidXMLException:", e);
                }
            }

        } catch(javax.net.ssl.SSLHandshakeException e) {
            Analytics.logException(e);
            Log.d(TAG, "InvalidXMLException:", e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection_ssl));
		} catch (SocketTimeoutException cpe) {
			Analytics.logException(cpe);
            Log.d(TAG, "SocketTimeoutException:", cpe);
			result.setSuccess(false);
			result.setResultMessage(ctx.getString(R.string.error_connection));
		} catch (IOException ioe) {
			Analytics.logException(ioe);
            Log.d(TAG, "IOException:", ioe);
			result.setSuccess(false);
			result.setResultMessage(ctx.getString(R.string.error_connection));
		} catch (UserNotFoundException unfe) {
            Analytics.logException(unfe);
            Log.d(TAG, "UserNotFoundException:", unfe);
			result.setSuccess(false);
			result.setResultMessage(ctx.getString(R.string.error_connection));
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
	protected void onPostExecute(EntityResult<Course> result) {
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
