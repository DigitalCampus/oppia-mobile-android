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

package org.digitalcampus.mobile.learning.activity;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.adapter.PointsListAdapter;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.APIRequestListener;
import org.digitalcampus.mobile.learning.model.Points;
import org.digitalcampus.mobile.learning.task.APIRequestTask;
import org.digitalcampus.mobile.learning.task.Payload;
import org.digitalcampus.mobile.learning.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ListView;

import com.bugsense.trace.BugSenseHandler;

public class ScoreActivity extends AppActivity implements APIRequestListener{

	public static final String TAG = ScoreActivity.class.getSimpleName();
	private ProgressDialog pDialog;
	private JSONObject json;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scorecard);
		this.drawHeader();
		this.getPoints();
	}
	
	private void getPoints(){
		// show progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setTitle(R.string.loading);
		pDialog.setMessage(getString(R.string.loading_points));
		pDialog.setCancelable(true);
		pDialog.show();
		
		APIRequestTask task = new APIRequestTask(this);
		Payload p = new Payload(MobileLearning.SERVER_POINTS_PATH);
		task.setAPIRequestListener(this);
		task.execute(p);
	}

	public void refreshPointsList() {
		try {
			ArrayList<Points> points = new ArrayList<Points>();
			
			for (int i = 0; i < (json.getJSONArray("objects").length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray("objects").get(i);
				Points p = new Points();
				p.setDescription(json_obj.getString("description"));
				p.setDateTime(json_obj.getString("date"));
				p.setPoints(json_obj.getInt("points"));

				points.add(p);
			}
			
			PointsListAdapter pla = new PointsListAdapter(this, points);
			ListView listView = (ListView) findViewById(R.id.points_list);
			listView.setAdapter(pla);

		} catch (Exception e) {
			e.printStackTrace();
			BugSenseHandler.sendException(e);
			UIUtils.showAlert(this, R.string.loading, R.string.error_processing_response);
		}

	}
	
	public void apiRequestComplete(Payload response) {
		pDialog.dismiss();
		if(response.isResult()){
			try {
				json = new JSONObject(response.getResultResponse());
				refreshPointsList();
			} catch (JSONException e) {
				BugSenseHandler.sendException(e);
				UIUtils.showAlert(this, R.string.loading, R.string.error_connection);
				e.printStackTrace();
			}
		} else {
			UIUtils.showAlert(this, R.string.error, R.string.error_connection_required, new Callable<Boolean>() {
				public Boolean call() throws Exception {
					ScoreActivity.this.finish();
					return true;
				}
			});
		}
		
	}
}
