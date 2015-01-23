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

package org.digitalcampus.oppia.activity;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.TagListAdapter;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.task.APIRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.bugsense.trace.BugSenseHandler;

public class TagSelectActivity extends AppActivity implements APIRequestListener {

	public static final String TAG = TagSelectActivity.class.getSimpleName();

	private ProgressDialog pDialog;
	private JSONObject json;
	private TagListAdapter tla;
    private ArrayList<Tag> tags;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        tags = new ArrayList<Tag>();
        tla = new TagListAdapter(this, tags);

        ListView listView = (ListView) findViewById(R.id.tag_list);
        listView.setAdapter(tla);
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tag selectedTag = tags.get(position);
                Intent i = new Intent(TagSelectActivity.this, DownloadActivity.class);
                Bundle tb = new Bundle();
                tb.putSerializable(Tag.TAG, selectedTag);
                i.putExtras(tb);
                startActivity(i);
            }
        });

	}
	
	@Override
	public void onResume(){
		super.onResume();
		// Get tags list
		if(this.json == null){
			this.getTagList();
        } else if ((tags != null) && tags.size()>0) {
            //We already have loaded JSON and tags (coming from orientationchange)
            tla.notifyDataSetChanged();
        }
        else{
            //The JSON is downloaded but tag list is not
            refreshTagList();
        }
	}

	@Override
	public void onPause(){
		// kill any open dialogs
		if (pDialog != null){
			pDialog.dismiss();
		}
		super.onPause();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    try {
			this.json = new JSONObject(savedInstanceState.getString("json"));
            ArrayList<Tag> savedTags = (ArrayList<Tag>) savedInstanceState.getSerializable("tags");
            this.tags.addAll(savedTags);
		} catch (JSONException e) {
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
	    super.onSaveInstanceState(savedInstanceState);
        if (json != null){
            //Only save the instance if the request has been proccessed already
            savedInstanceState.putString("json", json.toString());
            savedInstanceState.putSerializable("tags", tags);
        }
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
		tags.clear();
		try {
			for (int i = 0; i < (json.getJSONArray("tags").length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray("tags").get(i);
				Tag t = new Tag();
				t.setName(json_obj.getString("name"));
				t.setId(json_obj.getInt("id"));
				t.setCount(json_obj.getInt("count"));
				// Description
				if (json_obj.has("description") && !json_obj.isNull("description")){
					t.setDescription(json_obj.getString("description"));
				}
				// icon
				if (json_obj.has("icon") && !json_obj.isNull("icon")){
					t.setIcon(json_obj.getString("icon"));
				}
				// highlight
				if (json_obj.has("highlight") && !json_obj.isNull("highlight")){
					t.setHighlight(json_obj.getBoolean("highlight"));
				}
				// order priority
				if (json_obj.has("order_priority") && !json_obj.isNull("order_priority")){
					t.setOrderPriority(json_obj.getInt("order_priority"));
				}
				tags.add(t);
			}
            tla.notifyDataSetChanged();

		} catch (JSONException e) {
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
				e.printStackTrace();
				UIUtils.showAlert(this, R.string.loading, R.string.error_connection);
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
