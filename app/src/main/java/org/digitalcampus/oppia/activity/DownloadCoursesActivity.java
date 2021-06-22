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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityDownloadBinding;
import org.digitalcampus.mobile.learning.databinding.ActivityDownloadCoursesBinding;
import org.digitalcampus.oppia.adapter.TagsAdapter;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.fragments.CoursesDownloadFragment;
import org.digitalcampus.oppia.fragments.TagSelectFragment;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseInstallViewAdapter;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.model.TagRepository;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

public class DownloadCoursesActivity extends AppActivity {

	public static final String EXTRA_TAG = "extra_tag";
	public static final String EXTRA_COURSE = "extra_course";
	public static final String EXTRA_MODE = "extra_mode";

	public static final int MODE_TAG_COURSES = 0;
	public static final int MODE_COURSE_TO_UPDATE = 1;
	public static final int MODE_NEW_COURSES = 2;

	private ActivityDownloadCoursesBinding binding;
	private CoursesDownloadFragment coursesDownloadFragment;

	@Override
	public void onStart(){
		super.onStart();
		initialize(false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityDownloadCoursesBinding.inflate(LayoutInflater.from(this));
		setContentView(binding.getRoot());

		Bundle extras = getIntent().getExtras();
		if (extras == null || !extras.containsKey(EXTRA_MODE)) {
			throw new IllegalArgumentException("Missing extra argument: " + EXTRA_MODE);
		}

		setUpScreen(extras.getInt(EXTRA_MODE));

		binding.btnDownloadCourses.setOnClickListener(view -> {

			if (coursesDownloadFragment != null) {
				coursesDownloadFragment.onDownloadCoursesButtonClick();
			}
		});

		// For phones
		getSupportFragmentManager().addOnBackStackChangedListener(() -> {
			if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
				binding.categoryTitle.setText(R.string.select_category);
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int itemId = item.getItemId();
		switch (itemId) {
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		} else {
			super.onBackPressed();
		}
	}

	public void onTagSelected(Tag tag) {

		binding.categoryTitle.setText(tag.getName());

		Bundle args = new Bundle();
		args.putInt(CoursesDownloadFragment.ARG_MODE, CoursesDownloadFragment.MODE_TAG_COURSES);
		args.putSerializable(CoursesDownloadFragment.ARG_TAG, tag);
		coursesDownloadFragment = new CoursesDownloadFragment();
		coursesDownloadFragment.setArguments(args);

		if (isTabletLandscape()) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.frame_courses_download, coursesDownloadFragment)
					.commit();
		} else {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.frame_tags, coursesDownloadFragment)
					.addToBackStack(null)
					.commit();
		}
	}

	private void setUpScreen(int mode) {

		if (isTabletLandscape()) {
			// Tablet landscape
			coursesDownloadFragment = new CoursesDownloadFragment();
			coursesDownloadFragment.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction().replace(R.id.frame_courses_download, coursesDownloadFragment).commit();
		}

		switch (mode) {

			case MODE_TAG_COURSES:
				TagSelectFragment tagSelectFragment = new TagSelectFragment();
				getSupportFragmentManager().beginTransaction().replace(R.id.frame_tags, tagSelectFragment).commit();
				return;

			case MODE_COURSE_TO_UPDATE:
				binding.categoryTitle.setText(R.string.course_updates);

				if (!isTabletLandscape()) {
					coursesDownloadFragment = new CoursesDownloadFragment();
					coursesDownloadFragment.setArguments(getIntent().getExtras());
					getSupportFragmentManager().beginTransaction().replace(R.id.frame_tags, coursesDownloadFragment).commit();
				}
				break;

			case MODE_NEW_COURSES:
				binding.categoryTitle.setText(R.string.new_courses);

				if (!isTabletLandscape()) {
					coursesDownloadFragment = new CoursesDownloadFragment();
					coursesDownloadFragment.setArguments(getIntent().getExtras());
					getSupportFragmentManager().beginTransaction().replace(R.id.frame_tags, coursesDownloadFragment).commit();
				}
				break;
		}

	}

	public void showDownloadButton(boolean show) {
		binding.btnDownloadCourses.setVisibility(show ? View.VISIBLE : View.GONE);
	}
}
