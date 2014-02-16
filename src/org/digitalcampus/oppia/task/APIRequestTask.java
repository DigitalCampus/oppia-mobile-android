/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;

import android.content.Context;
import android.os.AsyncTask;

public class APIRequestTask extends AsyncTask<Payload, Object, Payload>{
	
	public static final String TAG = APIRequestTask.class.getSimpleName();
	protected Context ctx;
	private APIRequestListener requestListener;
	
	public APIRequestTask(Context ctx) {
		this.ctx = ctx;
	}
	
	@Override
	protected Payload doInBackground(Payload... params){
		
		Payload payload = params[0];
		String responseStr = "";
		
		HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
		String url = client.getFullURL(payload.getUrl());
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader(client.getAuthHeader());
		
		try {
			
			// make request
			HttpResponse response = client.execute(httpGet);
		
			// read response
			InputStream content = response.getEntity().getContent();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 1024);
			String s = "";
			while ((s = buffer.readLine()) != null) {
				responseStr += s;
			}
			
			
			switch (response.getStatusLine().getStatusCode()){
				// TODO check the unauthorised response code...
				case 400: // unauthorised
					payload.setResult(false);
					payload.setResultResponse(ctx.getString(R.string.error_login));
					break;
				case 200: 
					payload.setResult(true);
					payload.setResultResponse(responseStr);
					break;
				default:
					payload.setResult(false);
					payload.setResultResponse(ctx.getString(R.string.error_connection));
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException e) {
			e.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		}
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