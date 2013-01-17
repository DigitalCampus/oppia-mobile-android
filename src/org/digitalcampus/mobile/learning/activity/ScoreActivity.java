package org.digitalcampus.mobile.learning.activity;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.adapter.PointsListAdapter;
import org.digitalcampus.mobile.learning.listener.GetPointsListener;
import org.digitalcampus.mobile.learning.model.Points;
import org.digitalcampus.mobile.learning.task.GetPointsTask;
import org.digitalcampus.mobile.learning.task.Payload;
import org.digitalcampus.mobile.learning.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.bugsense.trace.BugSenseHandler;

public class ScoreActivity extends AppActivity implements GetPointsListener{

	public static final String TAG = ScoreActivity.class.getSimpleName();
	private ProgressDialog pDialog;
	private JSONObject json;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score);
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
		
		GetPointsTask task = new GetPointsTask(this);
		Payload p = new Payload(0,null);
		task.setGetPointsListener(this);
		task.execute(p);
	}

	public void refreshPointsList() {
		try {
			ArrayList<Points> points = new ArrayList<Points>();
			
			for (int i = 0; i < (json.getJSONArray("objects").length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray("objects").get(i);
				Points p = new Points();
				p.setDescription(json_obj.getString("description"));
				p.setDate(json_obj.getString("date"));
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
	
	public void pointsComplete(Payload response) {
		pDialog.dismiss();
		if(response.result){
			try {
				Log.d(TAG,response.resultResponse);
				json = new JSONObject(response.resultResponse);
				refreshPointsList();
			} catch (JSONException e) {
				BugSenseHandler.sendException(e);
				UIUtils.showAlert(this, R.string.loading, R.string.error_connection, new Callable<Boolean>() {
					
					public Boolean call() throws Exception {
						
						return true;
					}
				});
				e.printStackTrace();
			}
		} else {
			UIUtils.showAlert(this, R.string.loading, response.resultResponse);
		}
		
	}
}
