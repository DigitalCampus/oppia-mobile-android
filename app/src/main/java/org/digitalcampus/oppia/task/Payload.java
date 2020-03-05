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

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Payload {

	protected static final String TAG = Payload.class.getSimpleName();
	private List<?> data;
	private boolean result = false;
	private String resultResponse;
	private List<Object> responseData = new ArrayList<>();
	private String url;

	public Payload(){
		
	}
	
	public Payload(String url){
		this.setUrl(url);		
	}
	
	public Payload(List<?> data) {
		this.data = data;
	}

	public List<?> getData() {
		return data;
	}

	public void setData(List<Object> data) {
		this.data = data;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getResultResponse() {
		return resultResponse;
	}

	public void setResultResponse(String resultResponse) {
		this.resultResponse = resultResponse;
	}

	public void setResultResponseDataError(String data, String errorMessage) {
		try {
			this.resultResponse = new JSONObject().put("error", errorMessage).toString();
			this.responseData.add(data);
		} catch (JSONException e) {
			Log.d(TAG,"Invalid json for result response", e);
		}
	}

	public List<Object> getResponseData() {
		return responseData;
	}

	public void setResponseData(List<Object> responseData) {
		this.responseData = responseData;
	}
	
	public void addResponseData(Object obj){
		this.responseData.add(obj);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
