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

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class APIRequestTask extends AsyncTask<Payload, Object, Payload>{

	public static final String TAG = APIRequestTask.class.getSimpleName();
	protected Context ctx;
	private APIRequestListener requestListener;
    private boolean APIKeyInvalidated = false;

	public APIRequestTask(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected Payload doInBackground(Payload... params){

        long now = System.currentTimeMillis();
		Payload payload = params[0];
		try {

			DbHelper db = DbHelper.getInstance(ctx);
        	User u = db.getUser(SessionManager.getUsername(ctx));

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(HTTPClientUtils.getFullURL(ctx, payload.getUrl()))
                    .addHeader(HTTPClientUtils.HEADER_AUTH,
                            HTTPClientUtils.getAuthHeaderValue(u.getUsername(), u.getApiKey()))
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                payload.setResult(true);
                payload.setResultResponse(response.body().string());
            }
            else{
                switch (response.code()) {
                    case 401:
                        payload.setResult(false);
                        payload.setResultResponse(ctx.getString(R.string.error_apikey_expired));
                        SessionManager.setUserApiKeyValid(ctx, u, false);
                        APIKeyInvalidated = true;
                        break;
                    case 403: // unauthorised
                        payload.setResult(false);
                        payload.setResultResponse(ctx.getString(R.string.error_login));
                        break;
                    default:
                        payload.setResult(false);
                        payload.setResultResponse(ctx.getString(R.string.error_connection));
                }
            }
		} catch(javax.net.ssl.SSLHandshakeException e) {
            e.printStackTrace();
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_connection_ssl));
        }catch (ClientProtocolException | UserNotFoundException e) {
            e.printStackTrace();
            payload.setResult(false);
            if (e.getCause() != null && e.getCause() instanceof javax.security.cert.CertificateException){
                payload.setResultResponse(ctx.getString(R.string.error_connection_ssl));
            }
            else
			    payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException e) {
			e.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection_required));
		}

        long spent = System.currentTimeMillis() - now;
        Log.d(TAG, "Spent " + spent + " ms");
        return payload;
	}
	
	@Override
	protected void onPostExecute(Payload response) {
		synchronized (this) {
            if (requestListener != null) {
                if (APIKeyInvalidated)
                    requestListener.apiKeyInvalidated();
                else
                    requestListener.apiRequestComplete(response);
            }
        }
	}
	
	public void setAPIRequestListener(APIRequestListener srl) {
        synchronized (this) {
        	requestListener = srl;
        }
    }
}