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

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.UpdateActivityListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.xmlreaders.CourseTrackerXMLReader;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateCourseActivityTask extends APIRequestTask<Payload, DownloadProgress, Payload> {

	private UpdateActivityListener mStateListener;
    private boolean apiKeyInvalidated = false;
	private long userId;

	public UpdateCourseActivityTask(Context ctx, long userId, ApiEndpoint apiEndpoint) {
        super(ctx, apiEndpoint);
		this.userId = userId;
	}

    @Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		
		Course course = (Course) payload.getData().get(0);
		DownloadProgress dp = new DownloadProgress();

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
                payload.setResult(false);
                if (response.code() == 401){
                    SessionManager.setUserApiKeyValid(u, false);
                    apiKeyInvalidated = true;
                }
                payload.setResultResponse(ctx.getString(
                        (response.code()==401) ? R.string.error_apikey_expired : R.string.error_connection));
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
                    payload.setResult(true);

                } catch (InvalidXMLException e) {
                    Mint.logException(e);
                    Log.d(TAG, "InvalidXMLException:", e);
                }
            }

        } catch(javax.net.ssl.SSLHandshakeException e) {
            Mint.logException(e);
            Log.d(TAG, "InvalidXMLException:", e);
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_connection_ssl));
		} catch (SocketTimeoutException cpe) {
			Mint.logException(cpe);
            Log.d(TAG, "SocketTimeoutException:", cpe);
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException ioe) {
			Mint.logException(ioe);
            Log.d(TAG, "IOException:", ioe);
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (UserNotFoundException unfe) {
            Mint.logException(unfe);
            Log.d(TAG, "UserNotFoundException:", unfe);
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		}
		
		return payload;
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
	protected void onPostExecute(Payload results) {
		synchronized (this) {
            if (mStateListener != null) {
                if (apiKeyInvalidated)
                    mStateListener.apiKeyInvalidated();
                else
                    mStateListener.updateActivityComplete(results);
            }
        }
	}

	public void setUpdateActivityListener(UpdateActivityListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }
}
