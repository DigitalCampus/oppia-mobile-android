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
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.CourseInstallViewAdapter;
import org.digitalcampus.oppia.adapter.DownloadCoursesAdapter;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.model.CourseInstallRepository;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerServiceDelegate;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerService;
import org.digitalcampus.oppia.service.courseinstall.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.MultiChoiceHelper;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

public class DownloadActivity extends AppActivity implements APIRequestListener, CourseInstallerListener {
	
	private SharedPreferences sharedPreferences;
	private ProgressDialog progressDialog;
	private JSONObject json;
	private String url;
	private ArrayList<CourseInstallViewAdapter> courses;
	private ArrayList<CourseInstallViewAdapter> selected;
	private boolean showUpdatesOnly = false;

	private Button downloadButton;

    private InstallerBroadcastReceiver receiver;

    @Inject CourseInstallRepository courseInstallRepository;
    @Inject CourseInstallerServiceDelegate courseInstallerServiceDelegate;
    private DownloadCoursesAdapter coursesAdapter;
    private MultiChoiceHelper multiChoiceHelper;

    @Override
    public void onStart(){
        super.onStart();
        initialize();
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        getAppComponent().inject(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            Tag t = (Tag) bundle.getSerializable(Tag.TAG_CLASS);
            if (t != null){
                this.url = Paths.SERVER_TAG_PATH + String.valueOf(t.getId()) + File.separator;
                TextView tagTitle = findViewById(R.id.category_title);
                tagTitle.setVisibility(View.VISIBLE);
                tagTitle.setText(t.getName());
            }

        } else {
            this.url = Paths.SERVER_COURSES_PATH;
            this.showUpdatesOnly = true;
        }

        downloadButton = findViewById(R.id.btn_download_courses);
        courses = new ArrayList<>();
        selected = new ArrayList<>();
        coursesAdapter = new DownloadCoursesAdapter(this, courses);
        multiChoiceHelper = new MultiChoiceHelper(this, coursesAdapter);
        multiChoiceHelper.setMultiChoiceModeListener(new MultiChoiceHelper.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(androidx.appcompat.view.ActionMode mode, int position, long id, boolean checked) {
                Log.v(TAG, "Count: " + multiChoiceHelper.getCheckedItemCount());
                CourseInstallViewAdapter course = courses.get(position);
                if (checked) {
                    if (!course.isToInstall()){
                        multiChoiceHelper.setItemChecked(position, false, true);
                        return;
                    }
                    selected.add(course);
                } else {
                    selected.remove(course);
                }

                int count = selected.size();
                mode.setSubtitle(count == 1 ? count + " item selected" : count + " items selected");
            }

            @Override
            public boolean onCreateActionMode(final androidx.appcompat.view.ActionMode mode, Menu menu) {

                onPrepareOptionsMenu(menu);
                downloadButton.setOnClickListener(view -> {
                    for (CourseInstallViewAdapter course : selected){
                        downloadCourse(course);
                    }
                    mode.finish();
                });
                mode.setTitle(R.string.title_download_activity);
                coursesAdapter.setEnterOnMultiChoiceMode(true);
                coursesAdapter.notifyDataSetChanged();
                showDownloadButton(true);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_select_all:
                        selectAllInstallableCourses();
                        return true;
                    case R.id.menu_unselect_all:
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
                selected.clear();
                showDownloadButton(false);
                multiChoiceHelper.clearChoices();
                coursesAdapter.setEnterOnMultiChoiceMode(false);
                coursesAdapter.notifyDataSetChanged();

            }
        });

        coursesAdapter.setOnItemClickListener((view, position) -> {
            Log.d("course-download", "Clicked " + position);
            CourseInstallViewAdapter course = courses.get(position);
            // When installing, don't do anything on click
            if (course.isInstalling()) return;
            if (course.isDownloading()){
                cancelCourseTask(course);
            }
            else if (course.isToInstall()){
                downloadCourse(course);
            }

        });

        coursesAdapter.setMultiChoiceHelper(multiChoiceHelper);
        RecyclerView recyclerCourses = findViewById(R.id.recycler_tags);
        if (recyclerCourses != null) {
            recyclerCourses.setAdapter(coursesAdapter);
        }

    }

	
	@Override
	public void onResume(){
		super.onResume();
		if(json == null){
            // The JSON download task has not started or been completed yet
			getCourseList();
		} else if ((courses != null) && !courses.isEmpty()) {
            // We already have loaded JSON and courses (coming from orientationchange)
            coursesAdapter.notifyDataSetChanged();
        }
        else{
            // The JSON is downloaded but course list is not
	        refreshCourseList();
		}
        receiver = new InstallerBroadcastReceiver();
        receiver.setCourseInstallerListener(this);
        IntentFilter broadcastFilter = new IntentFilter(CourseInstallerService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(receiver, broadcastFilter);

	}

	@Override
	public void onPause(){
		// Kill any open dialogs
		if (progressDialog != null){
            progressDialog.dismiss();
        }
		super.onPause();
        unregisterReceiver(receiver);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

	    try {
			this.json = new JSONObject(savedInstanceState.getString("json"));
            ArrayList<CourseInstallViewAdapter> savedCourses = (ArrayList<CourseInstallViewAdapter>) savedInstanceState.getSerializable("courses");
            if (savedCourses!=null) this.courses.addAll(savedCourses);
		} catch (Exception e) {
            // error in the json so just get the list again
        }
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
            if (json != null){
                // Only save the instance if the request has been proccessed already
                savedInstanceState.putString("json", json.toString());
                savedInstanceState.putSerializable("courses", courses);
            }
	}
	
	private void getCourseList() {
		// show progress dialog
		progressDialog = new ProgressDialog(this, R.style.Oppia_AlertDialogStyle);
		progressDialog.setTitle(R.string.loading);
		progressDialog.setMessage(getString(R.string.loading));
		progressDialog.setCancelable(false);
		progressDialog.show();

        courseInstallRepository.getCourseList(this, url);
	}

	private void showDownloadButton(boolean show){
        downloadButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void downloadCourse(CourseInstallViewAdapter course){
        if (course.isToInstall() && !course.isInProgress()){
            Intent serviceIntent = new Intent(DownloadActivity.this, CourseInstallerService.class);
            courseInstallerServiceDelegate.installCourse(DownloadActivity.this, serviceIntent, course);
            resetCourseProgress(course, true, false);
        }
    }

    private void cancelCourseTask(CourseInstallViewAdapter course){
        Intent serviceIntent = new Intent(DownloadActivity.this, CourseInstallerService.class);
        courseInstallerServiceDelegate.cancelCourseInstall(DownloadActivity.this, serviceIntent, course);
        resetCourseProgress(course, false, false);
    }

	public void refreshCourseList() {
		// process the response and display on screen in listview
		// Create an array of courses, that will be put to our ListActivity
		try {
            String storage = sharedPreferences.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
            courses.clear();
            courseInstallRepository.refreshCourseList(this, courses, json, storage, showUpdatesOnly);
            coursesAdapter.notifyDataSetChanged();
            findViewById(R.id.empty_state).setVisibility((courses.isEmpty()) ? View.VISIBLE : View.GONE);

		} catch (Exception e) {
			Mint.logException(e);
            Log.d(TAG, "Error processing response: ", e);
			UIUtils.showAlert(this, R.string.loading, R.string.error_processing_response);
		}
	}

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.missing_media_sortby, menu);
        MenuItem sortBy = menu.findItem(R.id.menu_sort_by);
        if (sortBy != null) {
            sortBy.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_select_all:
                selectAllInstallableCourses();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectAllInstallableCourses(){
        for (int i = 0; i < coursesAdapter.getItemCount(); i++) {
            CourseInstallViewAdapter course = courses.get(i);
            if (course.isToInstall() && !multiChoiceHelper.isItemChecked(i)) {
                multiChoiceHelper.setItemChecked(i, true, true);
            }
        }
    }
	
	public void apiRequestComplete(Payload response) {
		progressDialog.dismiss();

        Callable<Boolean> finishActivity = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DownloadActivity.this.finish();
                return true;
            }
        };

		if(response.isResult()){
			try {
				json = new JSONObject(response.getResultResponse());
				refreshCourseList();

			} catch (JSONException e) {
				Mint.logException(e);
                Log.d(TAG, "Error connecting to server: ", e);
				UIUtils.showAlert(this, R.string.loading, R.string.error_connection, finishActivity);
			}
		} else {
            String errorMsg = response.getResultResponse();
            UIUtils.showAlert(this, R.string.error, errorMsg, finishActivity);
		}
	}

    //@Override
    public void onDownloadProgress(String fileUrl, int progress) {
        CourseInstallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            course.setDownloading(true);
            course.setInstalling(false);
            course.setProgress(progress);
            coursesAdapter.notifyDataSetChanged();
        }
    }

    //@Override
    public void onInstallProgress(String fileUrl, int progress) {
        CourseInstallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            course.setDownloading(false);
            course.setInstalling(true);
            course.setProgress(progress);
            coursesAdapter.notifyDataSetChanged();
        }
    }

    //@Override
    public void onInstallFailed(String fileUrl, String message) {
        CourseInstallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            resetCourseProgress(course, false, false);
        }
    }

    //@Override
    public void onInstallComplete(String fileUrl) {
        CourseInstallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            Toast.makeText(this, this.getString(R.string.install_course_complete, course.getShortname()), Toast.LENGTH_LONG).show();
            course.setInstalled(true);
            course.setToUpdate(false);
            resetCourseProgress(course, false, false);
        }
    }

    private CourseInstallViewAdapter findCourse(String fileUrl){
        if (!courses.isEmpty()){
            for (CourseInstallViewAdapter course : courses){
                if (course.getDownloadUrl().equals(fileUrl)){
                    return course;
                }
            }
        }
        return null;
    }

    protected void resetCourseProgress(CourseInstallViewAdapter courseSelected,
                                       boolean downloading, boolean installing ){

        courseSelected.setDownloading(downloading);
        courseSelected.setInstalling(installing);
        courseSelected.setProgress(0);
        coursesAdapter.notifyDataSetChanged();
    }


}
