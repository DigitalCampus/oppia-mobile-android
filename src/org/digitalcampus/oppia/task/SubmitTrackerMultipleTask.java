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
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
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

public class SubmitTrackerMultipleTask extends AsyncTask<Payload, Object, Payload> {

	public final static String TAG = SubmitTrackerMultipleTask.class.getSimpleName();

	private Context ctx;
	private SharedPreferences prefs;

	public SubmitTrackerMultipleTask(Context ctx) {
		this.ctx = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	protected Payload doInBackground(Payload... params) {
		DbHelper db = new DbHelper(ctx);
		Payload payload = db.getUnsentLog();
		db.close();
		
		@SuppressWarnings("unchecked")
		Collection<Collection<TrackerLog>> result = (Collection<Collection<TrackerLog>>) split((Collection<Object>) payload.getData(), MobileLearning.MAX_TRACKER_SUBMIT);
		
		HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
		
		String url =client.getFullURL(MobileLearning.TRACKER_PATH);
		
		HttpPatch httpPatch = new HttpPatch(url);
		
		for (Collection<TrackerLog> trackerBatch : result) {
			String dataToSend = createDataString(trackerBatch);
			
			try {

				StringEntity se = new StringEntity(dataToSend,"utf8");
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPatch.setEntity(se);
                
                httpPatch.addHeader(client.getAuthHeader());

                Log.d(TAG,url);
				Log.d(TAG,dataToSend);
				
                // make request
				HttpResponse response = client.execute(httpPatch);	
				
				Log.d(TAG,String.valueOf(response.getStatusLine().getStatusCode()));
				
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
						DbHelper dbh = new DbHelper(ctx);
						for(TrackerLog tl: trackerBatch){
							dbh.markLogSubmitted(tl.getId());
						}
						dbh.close();
						payload.setResult(true);
						// update points
						JSONObject jsonResp = new JSONObject(responseStr);
						Editor editor = prefs.edit();
						
						editor.putInt(ctx.getString(R.string.prefs_points), jsonResp.getInt("points"));
						editor.putInt(ctx.getString(R.string.prefs_badges), jsonResp.getInt("badges"));
						try {
							editor.putBoolean(ctx.getString(R.string.prefs_scoring_enabled), jsonResp.getBoolean("scoring"));
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
						DbHelper dbh2 = new DbHelper(ctx);
						for(TrackerLog tl: trackerBatch){
							dbh2.markLogSubmitted(tl.getId());
						};
						dbh2.close();
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
		}
		
		return payload;
	}

	protected void onProgressUpdate(String... obj) {
		// do nothing
	}
	
	@Override
    protected void onPostExecute(Payload p) {
		// reset submittask back to null after completion - so next call can run properly
		MobileLearning app = (MobileLearning) ctx.getApplicationContext();
		app.omSubmitTrackerMultipleTask = null;
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
