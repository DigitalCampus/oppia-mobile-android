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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.TrackerServiceListener;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public class SubmitTrackerMultipleTask extends AsyncTask<Payload, Integer, Payload> {

	public final static String TAG = SubmitTrackerMultipleTask.class.getSimpleName();

	private Context ctx;
	private SharedPreferences prefs;
	private TrackerServiceListener trackerServiceListener;

	public SubmitTrackerMultipleTask(Context ctx) {
		this.ctx = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = new Payload();
		
		try {
				
				DbHelper db = new DbHelper(ctx);
				long userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
				Log.d(TAG,"userId: " + userId);
				payload = db.getUnsentTrackers(userId);
				DatabaseManager.getInstance().closeDatabase();
				@SuppressWarnings("unchecked")
				Collection<Collection<TrackerLog>> result = (Collection<Collection<TrackerLog>>) split((Collection<Object>) payload.getData(), MobileLearning.MAX_TRACKER_SUBMIT);
				
				
				
				for (Collection<TrackerLog> trackerBatch : result) {
					String dataToSend = createDataString(trackerBatch);
					
					try {
		
						HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
						String url = client.getFullURL(MobileLearning.TRACKER_PATH);
						HttpPatch httpPatch = new HttpPatch(url);
						
						StringEntity se = new StringEntity(dataToSend,"utf8");
		                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		                httpPatch.setEntity(se);
		                
		                httpPatch.addHeader(client.getAuthHeader());
						
		                // make request
						HttpResponse response = client.execute(httpPatch);	
						
						InputStream content = response.getEntity().getContent();
						BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 4096);
						String responseStr = "";
						String s = "";
		
						while ((s = buffer.readLine()) != null) {
							responseStr += s;
						}
						Log.d(TAG,responseStr);
						switch (response.getStatusLine().getStatusCode()){
							case 200: // submitted
								for(TrackerLog tl: trackerBatch){
									DbHelper db2 = new DbHelper(ctx);
									db2.markLogSubmitted(tl.getId());
									DatabaseManager.getInstance().closeDatabase();
								}
								payload.setResult(true);
								// update points
								JSONObject jsonResp = new JSONObject(responseStr);
								Editor editor = prefs.edit();
								
								editor.putInt(PrefsActivity.PREF_POINTS, jsonResp.getInt("points"));
								editor.putInt(PrefsActivity.PREF_BADGES, jsonResp.getInt("badges"));
								try {
									editor.putBoolean(PrefsActivity.PREF_SCORING_ENABLED, jsonResp.getBoolean("scoring"));
									editor.putBoolean(PrefsActivity.PREF_BADGING_ENABLED, jsonResp.getBoolean("badging"));
								} catch (JSONException e) {
									e.printStackTrace();
								}
								editor.commit();
								
								try {
									JSONObject metadata = jsonResp.getJSONObject("metadata");
							        MetaDataUtils mu = new MetaDataUtils(ctx);
							        mu.saveMetaData(metadata, prefs);
								} catch (JSONException e) {
									e.printStackTrace();
								}
						    	
								break;
							case 400: // submitted but invalid digest - returned 400 Bad Request - so record as submitted so doesn't keep trying
								for(TrackerLog tl: trackerBatch){
									DbHelper db3 = new DbHelper(ctx);
									db3.markLogSubmitted(tl.getId());
									DatabaseManager.getInstance().closeDatabase();
								};
								payload.setResult(true);
								break;
							default:
								payload.setResult(false);
						}
		
					} catch (UnsupportedEncodingException e) {
						payload.setResult(false);
					} catch (ClientProtocolException e) {
						payload.setResult(false);
					} catch (IOException e) {
						payload.setResult(false);
					} catch (JSONException e) {
						if(!MobileLearning.DEVELOPER_MODE){
							BugSenseHandler.sendException(e);
						} else {
							e.printStackTrace();
						}
						payload.setResult(false);
					} 
					publishProgress(0);
				}
			
	
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			payload.setResult(false);
		} 
		return payload;
	}

	@Override
	protected void onProgressUpdate(Integer... obj) {
		synchronized (this) {
            if (trackerServiceListener != null) {
            	trackerServiceListener.trackerProgressUpdate();
            }
        }
	}
	
	@Override
    protected void onPostExecute(Payload p) {
		synchronized (this) {
            if (trackerServiceListener != null) {
            	trackerServiceListener.trackerComplete();
            }
        }
		// reset submittask back to null after completion - so next call can run properly
		MobileLearning app = (MobileLearning) ctx.getApplicationContext();
		app.omSubmitTrackerMultipleTask = null;
		
    }
	
	public void setTrackerServiceListener(TrackerServiceListener tsl) {
        trackerServiceListener = tsl;
    }
	
	private static Collection<Collection<TrackerLog>> split(Collection<Object> bigCollection, int maxBatchSize) {
		Collection<Collection<TrackerLog>> result = new ArrayList<Collection<TrackerLog>>();

		ArrayList<TrackerLog> currentBatch = null;
		for (Object obj : bigCollection) {
			TrackerLog tl = (TrackerLog) obj;
			if (currentBatch == null) {
				currentBatch = new ArrayList<TrackerLog>();
			} else if (currentBatch.size() >= maxBatchSize) {
				result.add(currentBatch);
				currentBatch = new ArrayList<TrackerLog>();
			}

			currentBatch.add(tl);
		}

		if (currentBatch != null) {
			result.add(currentBatch);
		}

		return result;
	}
	
	private String createDataString(Collection<TrackerLog> collection){
		String s = "{\"objects\":[";
		int counter = 0;
		for(TrackerLog tl: collection){
			counter++;
			s += tl.getContent();
			if(counter != collection.size()){
				s += ",";
			}
		}
		s += "]}";
		return s;
	}

}
