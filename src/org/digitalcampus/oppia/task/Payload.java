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

import java.util.ArrayList;

public class Payload {
	
	private ArrayList<? extends Object> data;
	private boolean result = false;
	private String resultResponse;
	private ArrayList<Object> responseData = new ArrayList<Object>();
	private String url;

	public Payload(){
		
	}
	
	public Payload(String url){
		this.setUrl(url);		
	}
	
	public Payload(ArrayList<? extends Object> data) {
		this.data = data;
	}

	public ArrayList<? extends Object> getData() {
		return data;
	}

	public void setData(ArrayList<? extends Object> data) {
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

	public ArrayList<Object> getResponseData() {
		return responseData;
	}

	public void setResponseData(ArrayList<Object> responseData) {
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
