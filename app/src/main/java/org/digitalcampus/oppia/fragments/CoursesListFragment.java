package org.digitalcampus.oppia.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentAboutBinding;
import org.digitalcampus.mobile.learning.databinding.FragmentCoursesListBinding;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.adapter.CoursesListAdapter;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.listener.DeleteCourseListener;
import org.digitalcampus.oppia.listener.UpdateActivityListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerService;
import org.digitalcampus.oppia.service.courseinstall.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.task.DeleteCourseTask;
import org.digitalcampus.oppia.task.UpdateCourseActivityTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.CourseUtils;
import org.digitalcampus.oppia.utils.ui.MediaScanView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;


public class CoursesListFragment extends AppFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        DeleteCourseListener,
        CourseInstallerListener,
        UpdateActivityListener, CoursesListAdapter.OnItemClickListener {

    public  static final String ACTION_COURSES_UPDATES = "actionCoursesUpdates";

    private List<Course> courses;

    private FragmentCoursesListBinding binding;
    private CoursesListAdapter adapterListCourses;

    private InstallerBroadcastReceiver receiver;

    @Inject CoursesRepository coursesRepository;
    @Inject SharedPreferences sharedPrefs;
    @Inject ApiEndpoint apiEndpoint;

    BroadcastReceiver coursesUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), ACTION_COURSES_UPDATES)) {
                displayCourses();
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCoursesListBinding.inflate(inflater, container, false);
        getAppComponent().inject(this);

        sharedPrefs.registerOnSharedPreferenceChangeListener(this);

        // set preferred lang to the default lang
        if ("".equals(sharedPrefs.getString(PrefsActivity.PREF_LANGUAGE, ""))) {
            sharedPrefs.edit().putString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()).apply();
        }

        if (getResources().getBoolean(R.bool.is_tablet)) {
            binding.recyclerCourses.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        } else {
            binding.recyclerCourses.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        courses = new ArrayList<>();
        adapterListCourses = new CoursesListAdapter(getActivity(), courses);
        adapterListCourses.setOnItemClickListener(this);
        binding.recyclerCourses.setAdapter(adapterListCourses);

        binding.viewMediaScan.setViewBelow(binding.recyclerCourses);
        binding.viewMediaScan.setUpdateMediaScan(true);

        return binding.getRoot();
    }


    @Override
    public void onStart() {
        super.onStart();
        displayCourses();
    }

    @Override
    public void onResume(){
        super.onResume();

        receiver = new InstallerBroadcastReceiver();
        receiver.setCourseInstallerListener(this);
        IntentFilter broadcastFilter = new IntentFilter(CourseInstallerService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        getActivity().registerReceiver(receiver, broadcastFilter);

        IntentFilter coursesUpdatesBroadcastFilter = new IntentFilter(ACTION_COURSES_UPDATES);
        getActivity().registerReceiver(coursesUpdatesReceiver, coursesUpdatesBroadcastFilter);

        if (adapterListCourses != null) {
            adapterListCourses.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        getActivity().unregisterReceiver(receiver);
        getActivity().unregisterReceiver(coursesUpdatesReceiver);
    }

    private void displayCourses() {
        courses.clear();
        courses.addAll(coursesRepository.getCourses(getActivity()));
        CourseUtils.setSyncStatus(prefs, courses, null);

        if (courses.size() < App.DOWNLOAD_COURSES_DISPLAY){
            displayDownloadSection();
        } else {
            binding.manageCoursesText.setText(R.string.no_courses);
            binding.noCourses.setVisibility(View.GONE);
        }

        adapterListCourses.notifyDataSetChanged();

        binding.viewMediaScan.scanMedia(courses);
    }


    private void displayDownloadSection(){
        binding.noCourses.setVisibility(View.VISIBLE);
        binding.manageCoursesText.setText((!courses.isEmpty())? R.string.more_courses : R.string.no_courses);
        binding.manageCoursesBtn.setOnClickListener(v -> onManageCoursesClick());
        binding.emptyStateImg.setOnClickListener(v -> onManageCoursesClick());

    }

    private void onManageCoursesClick() {
        AdminSecurityManager.with(getActivity()).checkAdminPermission(R.id.menu_download, () ->
                startActivity(new Intent(getActivity(), TagSelectActivity.class)));
    }

    // Recycler callbacks
    @Override
    public void onItemClick(int position) {
        Course selectedCourse = courses.get(position);

        boolean toUpdateOrDelete = checkToUpdateOrDeleteStatusWarning(selectedCourse);
        if (!toUpdateOrDelete) {
            openCourse(selectedCourse);
        }
    }

    private boolean checkToUpdateOrDeleteStatusWarning(Course selectedCourse) {
        if (selectedCourse.isToDelete()) {
            showCourseToDeleteDialog(selectedCourse);
            return true;
        } else if (selectedCourse.isToUpdate()) {
            showCourseToUpdateDialog(selectedCourse);
            return true;
        }

        return false;
    }

    private void showCourseToUpdateDialog(Course selectedCourse) {

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.course_update)
                .setMessage(R.string.course_update_dialog_message)
                .setPositiveButton(R.string.update, (dialog, which) -> {
                    Intent i = new Intent(getActivity(), DownloadActivity.class);
                    Bundle tb = new Bundle();
                    tb.putInt(DownloadActivity.EXTRA_MODE, DownloadActivity.MODE_COURSE_TO_UPDATE);
                    tb.putSerializable(DownloadActivity.EXTRA_COURSE, selectedCourse);
                    i.putExtras(tb);
                    startActivity(i);
                })
                .setNegativeButton(R.string.continue_to_course, (dialog, which) -> openCourse(selectedCourse))
                .setNeutralButton(R.string.back, null)
                .show();
    }

    private void showCourseToDeleteDialog(Course selectedCourse) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.course_deleted)
                .setMessage(R.string.course_deleted_dialog_message)
                .setPositiveButton(R.string.course_context_delete, (dialog, which) -> deleteCourse(selectedCourse))
                .setNeutralButton(R.string.back, null)
                .show();
    }

    private void openCourse(Course selectedCourse) {
        Intent i = new Intent(getActivity(), CourseIndexActivity.class);
        Bundle tb = new Bundle();
        tb.putSerializable(Course.TAG, selectedCourse);
        i.putExtras(tb);
        startActivity(i);
    }

    @Override
    public void onContextMenuItemSelected(final int position, final int itemId) {
        AdminSecurityManager.with(getActivity()).checkAdminPermission(itemId, () -> {
            Course course = courses.get(position);
            if (itemId == R.id.course_context_delete) {
                if (sharedPrefs.getBoolean(PrefsActivity.PREF_DELETE_COURSE_ENABLED, true)){
                    confirmCourseDelete(course);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.warning_delete_disabled), Toast.LENGTH_LONG).show();
                }
            } else if (itemId == R.id.course_context_reset) {
                confirmCourseReset(course);
            } else if (itemId == R.id.course_context_update_activity){
                confirmCourseUpdateActivity(course);
            }
        });
    }

    private void confirmCourseDelete(Course course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Oppia_AlertDialogStyle);
        builder.setTitle(R.string.course_context_delete);
        builder.setMessage(R.string.course_context_delete_confirm);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> deleteCourse(course));
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    private void deleteCourse(Course course) {

        showProgressDialog(getString(R.string.course_deleting), false);

        DeleteCourseTask task = new DeleteCourseTask(getActivity());
        task.setOnDeleteCourseListener(CoursesListFragment.this);
        task.execute(course);
    }

    private void confirmCourseReset(Course course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Oppia_AlertDialogStyle);
        builder.setTitle(R.string.course_context_reset);
        builder.setMessage(R.string.course_context_reset_confirm);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            DbHelper db = DbHelper.getInstance(getActivity());
            db.resetCourse(course.getCourseId(), SessionManager.getUserId(getActivity()));
            displayCourses();
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    private void confirmCourseUpdateActivity(Course course){

        showProgressDialog(getString(R.string.course_updating), false);

        UpdateCourseActivityTask task = new UpdateCourseActivityTask(getActivity(),
                SessionManager.getUserId(getActivity()), apiEndpoint);
        task.setUpdateActivityListener(CoursesListFragment.this);
        task.execute(course);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equalsIgnoreCase(PrefsActivity.PREF_SHOW_SCHEDULE_REMINDERS) || key.equalsIgnoreCase(PrefsActivity.PREF_NO_SCHEDULE_REMINDERS)){
            displayCourses();
        }

    }

    @Override
    public void onCourseDeletionComplete(BasicResult result) {
        if (result.isSuccess()){
            Media.resetMediaScan(prefs);
        }

        hideProgressDialog();

        toast(result.isSuccess() ? R.string.course_deleting_success : R.string.course_deleting_error);
        displayCourses();
        getActivity().invalidateOptionsMenu();
    }


    /* CourseInstallerListener implementation */
    public void onInstallComplete(String fileUrl) {
        toast(R.string.install_complete);
        Log.d(TAG, fileUrl + ": Installation complete.");
        displayCourses();
    }

    public void onDownloadProgress(String fileUrl, int progress) {
        // no need to show download progress in this activity
    }

    public void onInstallProgress(String fileUrl, int progress) {
        // no need to show install progress in this activity
    }

    public void onInstallFailed(String fileUrl, String message) {
        // no need to show install failed in this activity
    }

    /* UpdateActivityListener implementation */
    public void updateActivityProgressUpdate(DownloadProgress dp) {
        // no need to show download progress in this activity
    }

    public void updateActivityComplete(EntityResult<Course> result) {
        Course course = result.getEntity();

        hideProgressDialog();

        toast(getString(result.isSuccess() ? R.string.course_updating_success :
                        R.string.course_updating_error, (course!=null) ? course.getShortname() : ""));

        displayCourses();
    }

}
