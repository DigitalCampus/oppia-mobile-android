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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityDownloadBinding;
import org.digitalcampus.oppia.adapter.TagsAdapter;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.model.TagRepository;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

public class TagSelectActivity extends AppActivity implements APIRequestListener {

	private static final String KEY_JSON = "json";
	private static final String KEY_TAGS = "tags";

	private JSONObject json;
    private ArrayList<Tag> tags;

	@Inject TagRepository tagRepository;
	@Inject	ApiEndpoint apiEndpoint;
	private TagsAdapter adapterTags;
	private ActivityDownloadBinding binding;

	@Override
	public void onStart(){
		super.onStart();
		initialize();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityDownloadBinding.inflate(LayoutInflater.from(this));
		setContentView(binding.getRoot());

		binding.actionBarSubtitle.setVisibility(View.GONE);
		getAppComponent().inject(this);

        tags = new ArrayList<>();
        adapterTags = new TagsAdapter(this, tags);

		binding.recyclerTags.setAdapter(adapterTags);
		adapterTags.setOnItemClickListener((view, position) -> {
			Tag selectedTag = tags.get(position);
			Intent i = new Intent(TagSelectActivity.this, DownloadActivity.class);
			Bundle tb = new Bundle();
			tb.putInt(DownloadActivity.EXTRA_MODE, DownloadActivity.MODE_TAG_COURSES);
			tb.putSerializable(DownloadActivity.EXTRA_TAG, selectedTag);
			i.putExtras(tb);
			startActivity(i);
		});

		updateCourseCache();
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
		hideProgressDialog();
		super.onPause();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
        try {
			if (savedInstanceState.containsKey(KEY_TAGS)) {
				Serializable savedTags = savedInstanceState.getSerializable(KEY_TAGS);
				if (savedTags != null){
					ArrayList<Tag> savedTagsList = (ArrayList<Tag>) savedTags;
					this.tags.addAll(savedTagsList);
				}
			}

			if (savedInstanceState.containsKey(KEY_JSON)) {
				this.json = new JSONObject(savedInstanceState.getString(KEY_JSON));
			}
        } catch (Exception e) {
            Analytics.logException(e);
            Log.d(TAG, "Error restoring saved state: ", e);
        }
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
	    super.onSaveInstanceState(savedInstanceState);
        if (json != null){
            //Only save the instance if the request has been proccessed already
            savedInstanceState.putString(KEY_JSON, json.toString());
            savedInstanceState.putSerializable(KEY_TAGS, tags);
        }
	}
	
	private void getTagList() {

		tagRepository.getTagList(this, apiEndpoint);
	}

	public void refreshTagList() {
		tags.clear();
		try {

			tagRepository.refreshTagList(tags, json, getInstalledCoursesNames());

            adapterTags.notifyDataSetChanged();
            binding.emptyState.setVisibility((tags.isEmpty()) ? View.VISIBLE : View.GONE);

		} catch (JSONException e) {
            Analytics.logException(e);
            Log.d(TAG, "Error refreshing tag list: ", e);
		}
		
	}

	private List<String> getInstalledCoursesNames() {
		List<Course> installedCourses = DbHelper.getInstance(this).getAllCourses();
		List<String> installedCoursesNames = new ArrayList<>();
		for (Course course : installedCourses) {
			installedCoursesNames.add(course.getShortname());
		}
		return installedCoursesNames;
	}

	public void apiRequestComplete(BasicResult result) {
		hideProgressDialog();
		
        Callable<Boolean> finishActivity = () -> {
			TagSelectActivity.this.finish();
			return true;
		};
	
		if(result.isSuccess()){
			try {
				json = new JSONObject(result.getResultMessage());
				refreshTagList();
			} catch (JSONException e) {
				Analytics.logException(e);
                Log.d(TAG, "Error conencting to server: ", e);
				UIUtils.showAlert(this, R.string.loading, R.string.error_connection, finishActivity);
			}
		} else {
			String errorMsg = result.getResultMessage();
			UIUtils.showAlert(this, R.string.error, errorMsg, finishActivity);
		}

	}

	private void updateCourseCache() {

		APIUserRequestTask task = new APIUserRequestTask(this, apiEndpoint);
		String url = Paths.SERVER_COURSES_PATH;
		task.setAPIRequestListener(new APIRequestListener() {
			@Override
			public void apiRequestComplete(BasicResult result) {

				if (result.isSuccess()) {
					prefs.edit()
							.putLong(PrefsActivity.PREF_LAST_COURSES_CHECKS_SUCCESSFUL_TIME, System.currentTimeMillis())
							.putString(PrefsActivity.PREF_SERVER_COURSES_CACHE, result.getResultMessage())
							.commit();
				}
			}

			@Override
			public void apiKeyInvalidated() {

			}
		});
		task.execute(url);

	}
}
