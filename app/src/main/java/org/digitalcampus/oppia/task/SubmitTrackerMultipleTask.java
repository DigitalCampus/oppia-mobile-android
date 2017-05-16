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
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.TrackerServiceListener;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SubmitTrackerMultipleTask extends APIRequestTask<Payload, Integer, Payload> {

	public final static String TAG = SubmitTrackerMultipleTask.class.getSimpleName();

	private TrackerServiceListener trackerServiceListener;

    public SubmitTrackerMultipleTask(Context ctx) { super(ctx); }
    public SubmitTrackerMultipleTask(Context ctx, ApiEndpoint api) { super(ctx, api); }

	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = new Payload();

		try {	
			DbHelper db = DbHelper.getInstance(ctx);
			ArrayList<User> users = db.getAllUsers();
			
			for (User u: users) {
                payload = db.getUnsentTrackers(u.getUserId());

                @SuppressWarnings("unchecked")
                Collection<Collection<TrackerLog>> userTrackers = split((Collection<Object>) payload.getData(), MobileLearning.MAX_TRACKER_SUBMIT);
                sendTrackerBatch(userTrackers, u, db, payload);
            }

	
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			payload.setResult(false);
		} 
		
		Editor editor = prefs.edit();
		long now = System.currentTimeMillis()/1000;
		editor.putLong(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH, now).apply();
		return payload;
	}

    private void sendTrackerBatch(Collection<Collection<TrackerLog>> trackers, User user, DbHelper db, Payload p) {
            for (Collection<TrackerLog> trackerBatch : trackers) {
                String dataToSend = createDataString(trackerBatch);
                Log.d(TAG, dataToSend);
                try {

                    OkHttpClient client = HTTPClientUtils.getClient(ctx);
                    Request request = new Request.Builder()
                            .url(apiEndpoint.getFullURL(ctx, MobileLearning.TRACKER_PATH))
                            .addHeader(HTTPClientUtils.HEADER_AUTH,
                                    HTTPClientUtils.getAuthHeaderValue(user.getUsername(), user.getApiKey()))
                            .patch(RequestBody.create(HTTPClientUtils.MEDIA_TYPE_JSON, dataToSend))
                            .build();

                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        for (TrackerLog tl : trackerBatch) {
                            db.markLogSubmitted(tl.getId());
                        }
                        p.setResult(true);
                        // update points
                        JSONObject jsonResp = new JSONObject(response.body().string());
                        db.updateUserPoints(user.getUserId(), jsonResp.getInt("points"));
                        db.updateUserBadges(user.getUserId(), jsonResp.getInt("badges"));

                        Editor editor = prefs.edit();
                        try {
                            editor.putBoolean(PrefsActivity.PREF_SCORING_ENABLED, jsonResp.getBoolean("scoring"));
                            editor.putBoolean(PrefsActivity.PREF_BADGING_ENABLED, jsonResp.getBoolean("badging"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        editor.apply();

                        try {
                            JSONObject metadata = jsonResp.getJSONObject("metadata");
                            MetaDataUtils mu = new MetaDataUtils(ctx);
                            mu.saveMetaData(metadata, prefs);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (response.code() == 400) {
                            // submitted but invalid digest - returned 400 Bad Request -
                            // so record as submitted so doesn't keep trying
                            for (TrackerLog tl : trackerBatch) {
                                db.markLogSubmitted(tl.getId());
                            }
                            p.setResult(true);
                        } else{
                            p.setResult(false);
                            if (response.code() == 401) {
                                //The apiKey of this user is invalid
                                SessionManager.setUserApiKeyValid(ctx, user, false);
                                //We don't process more of this user trackers
                                return;
                            }
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    p.setResult(false);
                } catch (IOException e) {
                    p.setResult(false);
                } catch (JSONException e) {
                    Mint.logException(e);
                    e.printStackTrace();
                    p.setResult(false);
                }
                publishProgress(0);
            }
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
		Collection<Collection<TrackerLog>> result = new ArrayList<>();

		ArrayList<TrackerLog> currentBatch = null;
		for (Object obj : bigCollection) {
			TrackerLog tl = (TrackerLog) obj;
			if (currentBatch == null) {
				currentBatch = new ArrayList<>();
			} else if (currentBatch.size() >= maxBatchSize) {
				result.add(currentBatch);
				currentBatch = new ArrayList<>();
			}

			currentBatch.add(tl);
		}

		if (currentBatch != null) {
			result.add(currentBatch);
		}

		return result;
	}
	
	private String createDataString(Collection<TrackerLog> collection){
		String jsonString = "{\"objects\":[";
		int counter = 0;
        int collectionSize = collection.size();
		for(TrackerLog tl: collection){
			counter++;
            jsonString += tl.getContent();
			if(counter != collectionSize){
                jsonString += ",";
			}
		}
        jsonString += "]}";
		return jsonString;
	}

}
