package org.digitalcampus.oppia.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentCoursesListBinding;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.adapter.CoursesListAdapter;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.AdminSecurityManager;
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
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.CourseUtils;
import org.digitalcampus.oppia.utils.TextUtilsJava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;


public class CoursesListFragment extends AppFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        DeleteCourseListener,
        CourseInstallerListener,
        UpdateActivityListener, CoursesListAdapter.OnItemClickListener {

    public static final String ACTION_COURSES_UPDATES = "actionCoursesUpdates";

    private List<Course> courses;

    private FragmentCoursesListBinding binding;
    private CoursesListAdapter adapterListCourses;

    private InstallerBroadcastReceiver receiver;

    @Inject
    CoursesRepository coursesRepository;
    @Inject
    SharedPreferences sharedPrefs;
    @Inject
    ApiEndpoint apiEndpoint;
    @Inject
    ConnectionUtils connectionUtils;

    private boolean singleCourseUpdate;

    BroadcastReceiver coursesUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtilsJava.equals(intent.getAction(), ACTION_COURSES_UPDATES)) {
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
        if ("".equals(sharedPrefs.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, ""))) {
            sharedPrefs.edit().putString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().getLanguage()).apply();
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

        if (getArguments() != null && getArguments().getBoolean(MainActivity.EXTRA_FIRST_LOGIN)) {
            String updateActivityOnLoginOption = sharedPrefs.getString(PrefsActivity.PREF_UPDATE_ACTIVITY_ON_LOGIN, getString(R.string.prefUpdateActivityOnLoginDefault));
            if (TextUtilsJava.equals(updateActivityOnLoginOption, getString(R.string.update_activity_on_login_value_optional))
                    || TextUtilsJava.equals(updateActivityOnLoginOption, getString(R.string.update_activity_on_login_value_force))) {
                runUpdateCoursesActivityProcess();
            }
            getArguments().remove(MainActivity.EXTRA_FIRST_LOGIN);
        }
    }

    private void runUpdateCoursesActivityProcess() {
        if (!courses.isEmpty()) {
            launchUpdateCoursesActivityTask(courses, "", false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        receiver = new InstallerBroadcastReceiver();
        receiver.setCourseInstallerListener(this);
        IntentFilter broadcastFilter = new IntentFilter(CourseInstallerService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        ContextCompat.registerReceiver(getActivity(), receiver, broadcastFilter, ContextCompat.RECEIVER_NOT_EXPORTED);

        IntentFilter coursesUpdatesBroadcastFilter = new IntentFilter(ACTION_COURSES_UPDATES);
        ContextCompat.registerReceiver(getActivity(), coursesUpdatesReceiver, coursesUpdatesBroadcastFilter, ContextCompat.RECEIVER_NOT_EXPORTED);

        if (adapterListCourses != null) {
            adapterListCourses.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
        getActivity().unregisterReceiver(coursesUpdatesReceiver);
    }

    private void displayCourses() {
        courses.clear();
        courses.addAll(coursesRepository.getCourses(getActivity()));
        CourseUtils.refreshStatuses(prefs, courses, null);

        if (courses.size() < BuildConfig.DOWNLOAD_COURSES_DISPLAY) {
            displayDownloadSection();
        } else {
            binding.manageCoursesText.setText(R.string.no_courses);
            binding.noCourses.setVisibility(View.GONE);
        }

        adapterListCourses.notifyDataSetChanged();

        binding.viewMediaScan.scanMedia(courses);
    }


    private void displayDownloadSection() {
        binding.noCourses.setVisibility(View.VISIBLE);
        binding.manageCoursesText.setText((!courses.isEmpty()) ? R.string.more_courses : R.string.no_courses);
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
                if (sharedPrefs.getBoolean(PrefsActivity.PREF_DELETE_COURSE_ENABLED, true)) {
                    confirmCourseDelete(course);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.warning_delete_disabled), Toast.LENGTH_LONG).show();
                }
            } else if (itemId == R.id.course_context_reset) {
                confirmCourseReset(course);
            } else if (itemId == R.id.course_context_update_activity) {
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

    private void confirmCourseUpdateActivity(Course course) {

        singleCourseUpdate = true;
        launchUpdateCoursesActivityTask(Arrays.asList(course), getString(R.string.course_updating), true);
    }

    private void launchUpdateCoursesActivityTask(List<Course> courses, String progressDialogMessage, boolean progressDialogIndeterminate) {

        if (!connectionUtils.isConnected(getContext())) {
            showCoursesActivityUpdateErrorDialog(getString(R.string.connection_unavailable_couse_activity));
            return;
        }

        showProgressDialog(progressDialogMessage, false, progressDialogIndeterminate);

        UpdateCourseActivityTask task = new UpdateCourseActivityTask(getActivity(),
                SessionManager.getUserId(getActivity()), apiEndpoint, singleCourseUpdate);
        task.setUpdateActivityListener(CoursesListFragment.this);
        task.execute(courses);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase(PrefsActivity.PREF_SHOW_SCHEDULE_REMINDERS) || key.equalsIgnoreCase(PrefsActivity.PREF_NO_SCHEDULE_REMINDERS)) {
            displayCourses();
        }
    }

    @Override
    public void onCourseDeletionComplete(BasicResult result) {
        if (result.isSuccess()) {
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
        getProgressDialog().setProgress(dp.getProgress());
        getProgressDialog().setMessage(getString(R.string.updating_course_activity, dp.getMessage()));
    }

    public void updateActivityComplete(EntityResult<List<Course>> result) {
        List<Course> courses = result.getEntity();

        hideProgressDialog();

        if (singleCourseUpdate) {
            singleCourseUpdate = false;
            Course course = courses.get(0);
            toast(getString(result.isSuccess() ? R.string.course_updating_success :
                    R.string.course_updating_error, (course != null) ? course.getShortname() : ""));
        } else {
            if (!result.isSuccess()) {
                showCoursesActivityUpdateErrorDialog(getString(R.string.error_unable_retrieve_course_activity));
            }
        }


        displayCourses();
    }

    private void showCoursesActivityUpdateErrorDialog(String message) {
        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setMessage(message);
        ab.setPositiveButton(R.string.try_again, (dialog, which) -> runUpdateCoursesActivityProcess());

        String updateActivityOnLoginOption = sharedPrefs.getString(PrefsActivity.PREF_UPDATE_ACTIVITY_ON_LOGIN,
                getString(R.string.prefUpdateActivityOnLoginDefault));
        boolean canContinue = TextUtilsJava.equals(updateActivityOnLoginOption, getString(R.string.update_activity_on_login_value_optional));

        if (canContinue) {
            ab.setNegativeButton(R.string.continue_anyway, (dialog, which) -> dialog.dismiss());
        }

        ab.setCancelable(false);
        ab.show();
    }

}
