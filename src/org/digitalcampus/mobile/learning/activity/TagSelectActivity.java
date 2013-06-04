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
import org.digitalcampus.mobile.learning.adapter.TagListAdapter;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.APIRequestListener;
import org.digitalcampus.mobile.learning.model.Tag;
import org.digitalcampus.mobile.learning.task.APIRequestTask;
import org.digitalcampus.mobile.learning.task.Payload;
import org.digitalcampus.mobile.learning.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class TagSelectActivity extends AppActivity implements APIRequestListener {

	public static final String TAG = TagSelectActivity.class.getSimpleName();

	private ProgressDialog pDialog;
	private JSONObject json;
	private TagListAdapter tla;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		this.drawHeader();
		
		TextView tv = (TextView) getHeader().findViewById(R.id.page_title);
		tv.setText(R.string.title_download_activity);
	
	}
	
	@Override
	public void onResume(){
		super.onResume();
		// Get Module list
		if(this.json == null){
			this.getTagList();
		}
	}

	@Override
	public void onPause(){
		// kill any open dialogs
		if (pDialog != null){
			pDialog.dismiss();
		}
		if (tla != null){
			tla.closeDialogs();
		}
		super.onPause();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    try {
			this.json = new JSONObject(savedInstanceState.getString("json"));
		} catch (JSONException e) {
			// error in the json so just get the list again
			//this.getModuleList();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
	    super.onSaveInstanceState(savedInstanceState);
	    savedInstanceState.putString("json", json.toString());
	}
	
	private void getTagList() {
		// show progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setTitle(R.string.loading);
		pDialog.setMessage(getString(R.string.loading));
		pDialog.setCancelable(true);
		pDialog.show();

		APIRequestTask task = new APIRequestTask(this);
		Payload p = new Payload(MobileLearning.SERVER_TAG_PATH);
		task.setAPIRequestListener(this);
		task.execute(p);
	}

	public void refreshTagList() {
		ArrayList<Tag> tags = new ArrayList<Tag>();
		try {
			for (int i = 0; i < (json.getJSONArray("tags").length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray("tags").get(i);
				Tag t = new Tag();
				t.setName(json_obj.getString("name"));
				t.setId(json_obj.getInt("id"));
				t.setCount(json_obj.getInt("count"));
				tags.add(t);
			}
			tla = new TagListAdapter(this, tags);
			ListView listView = (ListView) findViewById(R.id.tag_list);
			listView.setAdapter(tla);
			
			listView.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Tag t = (Tag) view.getTag();
					Intent i = new Intent(TagSelectActivity.this, DownloadActivity.class);
					Bundle tb = new Bundle();
					tb.putSerializable(Tag.TAG, t);
					i.putExtras(tb);
					startActivity(i);
				}
			});
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void apiRequestComplete(Payload response) {
		// close dialog and process results
		pDialog.dismiss();
	
		if(response.isResult()){
			try {
				json = new JSONObject(response.getResultResponse());
				refreshTagList();
			} catch (JSONException e) {
				BugSenseHandler.sendException(e);
				UIUtils.showAlert(this, R.string.loading, R.string.error_connection);
				e.printStackTrace();
			}
		} else {
			UIUtils.showAlert(this, R.string.error, R.string.error_connection_required, new Callable<Boolean>() {
				public Boolean call() throws Exception {
					TagSelectActivity.this.finish();
					return true;
				}
			});
		}

	}

}
