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
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.utils.HTTPClientUtils;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class APIUserRequestTask extends APIRequestTask<Payload, Object, Payload>{

	private APIRequestListener requestListener;

    public APIUserRequestTask(Context ctx) { super(ctx); }
    public APIUserRequestTask(Context ctx, ApiEndpoint api) { super(ctx, api); }

    @Override
	protected Payload doInBackground(Payload... params){

        long now = System.currentTimeMillis();
		Payload payload = params[0];
		try {

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = createRequestBuilderWithUserAuth(apiEndpoint.getFullURL(ctx, payload.getUrl())).build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                payload.setResult(true);
                payload.setResultResponse(response.body().string());
            }
            else{
                switch (response.code()) {
                    case 401:
                    case 403: // unauthorised
                        payload.setResult(false);
                        payload.setResultResponse(ctx.getString(R.string.error_login));
                        break;

                    default:
                        payload.setResult(false);
                        payload.setResultResponse(ctx.getString(R.string.error_connection));
                }
            }

		}  catch (IOException e) {
            Mint.logException(e);
            Log.d(TAG, "IO exception", e);
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		}

        long spent = System.currentTimeMillis() - now;
        Log.d(TAG, "Spent " + spent + " ms");
        return payload;
	}
	
	@Override
	protected void onPostExecute(Payload response) {
		synchronized (this) {
            if (requestListener != null) {
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