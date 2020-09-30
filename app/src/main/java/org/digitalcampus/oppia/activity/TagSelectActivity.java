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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.TagsAdapter;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.model.TagRepository;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

public class TagSelectActivity extends AppActivity implements APIRequestListener {

	private ProgressDialog pDialog;
	private JSONObject json;
    private ArrayList<Tag> tags;

	@Inject TagRepository tagRepository;
	private TagsAdapter adapterTags;

	@Override
	public void onStart(){
		super.onStart();
		initialize();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		View subtitleBar = findViewById(R.id.action_bar_subtitle);
		subtitleBar.setVisibility(View.GONE);
		getAppComponent().inject(this);

        tags = new ArrayList<>();
        adapterTags = new TagsAdapter(this, tags);

		RecyclerView recyclerTags = findViewById(R.id.recycler_tags);
		recyclerTags.setAdapter(adapterTags);
		adapterTags.setOnItemClickListener(new TagsAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				Tag selectedTag = tags.get(position);
				Intent i = new Intent(TagSelectActivity.this, DownloadActivity.class);
				Bundle tb = new Bundle();
				tb.putSerializable(Tag.TAG_CLASS, selectedTag);
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
        } else if ((tags != null) && !tags.isEmpty()) {
            //We already have loaded JSON and tags (coming from orientationchange)
            adapterTags.notifyDataSetChanged();
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
            Serializable savedTags = savedInstanceState.getSerializable("tags");
            if (savedTags != null){
                ArrayList<Tag> savedTagsList = (ArrayList<Tag>) savedTags;
                this.tags.addAll(savedTagsList);
            }

            this.json = new JSONObject(savedInstanceState.getString("json"));
        } catch (Exception e) {
            Mint.logException(e);
            Log.d(TAG, "Error restoring saved state: ", e);
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
		pDialog = new ProgressDialog(this, R.style.Oppia_AlertDialogStyle);
		pDialog.setTitle(R.string.loading);
		pDialog.setMessage(getString(R.string.loading));
		pDialog.setCancelable(true);
		pDialog.show();

		tagRepository.getTagList(this);
	}

	public void refreshTagList() {
		tags.clear();
		try {
			tagRepository.refreshTagList(tags, json);

            adapterTags.notifyDataSetChanged();
            findViewById(R.id.empty_state).setVisibility((tags.isEmpty()) ? View.VISIBLE : View.GONE);

		} catch (JSONException e) {
            Mint.logException(e);
            Log.d(TAG, "Error refreshing tag list: ", e);
		}
		
	}
	
	public void apiRequestComplete(Payload response) {
		// close dialog and process results
		pDialog.dismiss();

        Callable<Boolean> finishActivity = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                TagSelectActivity.this.finish();
                return true;
            }
        };
	
		if(response.isResult()){
			try {
				json = new JSONObject(response.getResultResponse());
				refreshTagList();
			} catch (JSONException e) {
				Mint.logException(e);
                Log.d(TAG, "Error conencting to server: ", e);
				UIUtils.showAlert(this, R.string.loading, R.string.error_connection, finishActivity);
			}
		} else {
            String errorMsg = response.getResultResponse();
			UIUtils.showAlert(this, R.string.error, errorMsg, finishActivity);
		}

	}

}
