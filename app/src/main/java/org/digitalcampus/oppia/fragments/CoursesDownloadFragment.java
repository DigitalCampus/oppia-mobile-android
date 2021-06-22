package org.digitalcampus.oppia.fragments;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityDownloadBinding;
import org.digitalcampus.mobile.learning.databinding.FragmentCoursesDownloadBinding;
import org.digitalcampus.oppia.activity.DownloadCoursesActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.adapter.DownloadCoursesAdapter;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseInstallRepository;
import org.digitalcampus.oppia.model.CourseInstallViewAdapter;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerService;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerServiceDelegate;
import org.digitalcampus.oppia.service.courseinstall.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.MultiChoiceHelper;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

public class CoursesDownloadFragment extends AppFragment implements APIRequestListener, CourseInstallerListener {

    public static final String ARG_TAG = "extra_tag";
    public static final String ARG_COURSE = "extra_course";
    public static final String ARG_MODE = "extra_mode";

    public static final int MODE_TAG_COURSES = 0;
    public static final int MODE_COURSE_TO_UPDATE = 1;
    public static final int MODE_NEW_COURSES = 2;

    private JSONObject json;
    private String url;
    private ArrayList<CourseInstallViewAdapter> courses;
    private ArrayList<CourseInstallViewAdapter> selected;

    private InstallerBroadcastReceiver receiver;

    @Inject
    CourseInstallRepository courseInstallRepository;
    @Inject
    CourseInstallerServiceDelegate courseInstallerServiceDelegate;
    @Inject
    CoursesRepository coursesRepository;
    private DownloadCoursesAdapter coursesAdapter;
    private MultiChoiceHelper multiChoiceHelper;
    private Course courseToUpdate;
    private int mode;
    private FragmentCoursesDownloadBinding binding;
    private ActionMode actionMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {

        binding = FragmentCoursesDownloadBinding.inflate(LayoutInflater.from(getActivity()), container, false);

        getAppComponent().inject(this);

        Bundle bundle = getArguments();
        if (bundle == null || !bundle.containsKey(ARG_MODE)) {
            throw new IllegalArgumentException("Mode parameter not found" + ARG_MODE);
        }

        mode = bundle.getInt(ARG_MODE);

        setUpRecyclerView();

        setUpScreen(mode, bundle);

        return binding.getRoot();
    }


    private void setUpScreen(int mode, Bundle bundle) {

        switch (mode) {
            case MODE_TAG_COURSES:
                if (bundle.containsKey(ARG_TAG)) {
                    Tag tag = (Tag) bundle.getSerializable(ARG_TAG);
                    this.url = Paths.SERVER_TAG_PATH + tag.getId() + File.separator;
                } else {
                    binding.emptyState.setVisibility(View.VISIBLE);
                    binding.emptyState.setText(R.string.select_category);
                }

                break;

            case MODE_COURSE_TO_UPDATE:

                if (!bundle.containsKey(ARG_COURSE)) {
                    throw new IllegalArgumentException("Course parameter not found");
                }

                courseToUpdate = (Course) bundle.getSerializable(ARG_COURSE);
                this.url = Paths.SERVER_COURSES_PATH;
                break;

            case MODE_NEW_COURSES:
                this.url = Paths.SERVER_COURSES_PATH;
                break;
        }
    }

    private void setUpRecyclerView() {

        courses = new ArrayList<>();
        selected = new ArrayList<>();
        coursesAdapter = new DownloadCoursesAdapter(getActivity(), courses);
        multiChoiceHelper = new MultiChoiceHelper((AppCompatActivity) getActivity(), coursesAdapter);
        multiChoiceHelper.setMultiChoiceModeListener(new MultiChoiceHelper.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(androidx.appcompat.view.ActionMode mode, int position, long id, boolean checked) {
                Log.v(TAG, "Count: " + multiChoiceHelper.getCheckedItemCount());
                CourseInstallViewAdapter course = courses.get(position);
                if (checked) {
                    if (!course.isToInstall()) {
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
                actionMode = mode;
                mode.setTitle(R.string.title_download_activity);
                coursesAdapter.setEnterOnMultiChoiceMode(true);
                coursesAdapter.notifyDataSetChanged();
                getDownloadCoursesActivity().showDownloadButton(true);

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
                getDownloadCoursesActivity().showDownloadButton(false);
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
            if (course.isDownloading()) {
                cancelCourseTask(course);
            } else if (course.isToInstall()) {
                downloadCourse(course);
            }

        });

        coursesAdapter.setMultiChoiceHelper(multiChoiceHelper);
        if (binding.recyclerCoursesDownload != null) {
            binding.recyclerCoursesDownload.setAdapter(coursesAdapter);
        }

    }

    private DownloadCoursesActivity getDownloadCoursesActivity() {
        return (DownloadCoursesActivity) getActivity();
    }

    public void onDownloadCoursesButtonClick() {

        for (CourseInstallViewAdapter course : selected) {
            downloadCourse(course);
        }
        actionMode.finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        receiver = new InstallerBroadcastReceiver();
        receiver.setCourseInstallerListener(this);
        IntentFilter broadcastFilter = new IntentFilter(CourseInstallerService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        getActivity().registerReceiver(receiver, broadcastFilter);

        if (json == null) {
            // The JSON download task has not started or been completed yet
            getCourseList();
        } else if ((courses != null) && !courses.isEmpty()) {
            // We already have loaded JSON and courses (coming from orientationchange)
            coursesAdapter.notifyDataSetChanged();
        } else {
            // The JSON is downloaded but course list is not
            refreshCourseList();
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        hideProgressDialog();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        mode = savedInstanceState.getInt("mode");

        try {
            this.json = new JSONObject(savedInstanceState.getString("json"));
            ArrayList<CourseInstallViewAdapter> savedCourses = (ArrayList<CourseInstallViewAdapter>) savedInstanceState.getSerializable("courses");
            if (savedCourses != null) this.courses.addAll(savedCourses);
        } catch (Exception e) {
            // error in the json so just get the list again
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt("mode", mode);

        if (json != null) {
            // Only save the instance if the request has been proccessed already
            savedInstanceState.putString("json", json.toString());
            savedInstanceState.putSerializable("courses", courses);
        }
    }

    private void getCourseList() {
        if (url != null) {
            showProgressDialog(getString(R.string.loading));
            courseInstallRepository.getCourseList(getActivity(), this, url);
        }
    }

    private void downloadCourse(CourseInstallViewAdapter course) {
        if (course.isToInstall() && !course.isInProgress()) {
            Intent serviceIntent = new Intent(getActivity(), CourseInstallerService.class);
            courseInstallerServiceDelegate.installCourse(getActivity(), serviceIntent, course);
            resetCourseProgress(course, true, false);
        }
    }

    private void cancelCourseTask(CourseInstallViewAdapter course) {
        Intent serviceIntent = new Intent(getActivity(), CourseInstallerService.class);
        courseInstallerServiceDelegate.cancelCourseInstall(getActivity(), serviceIntent, course);
        resetCourseProgress(course, false, false);
    }

    public void refreshCourseList() {
        // process the response and display on screen in listview
        // Create an array of courses, that will be put to our ListActivity
        try {
            String storage = Storage.getStorageLocationRoot(getActivity());
            courses.clear();

            // TODO 'refreshCourseList' method should be refactorized
            courseInstallRepository.refreshCourseList(getActivity(), courses, json, storage, mode == MODE_COURSE_TO_UPDATE);
            if (mode == MODE_COURSE_TO_UPDATE) {
                filterOnlyInstalledCourses();
            } else if (mode == MODE_NEW_COURSES) {
                filterNewCoursesNotSeen();
            }
            coursesAdapter.notifyDataSetChanged();
            binding.emptyState.setVisibility((courses.isEmpty()) ? View.VISIBLE : View.GONE);
            binding.emptyState.setText(R.string.empty_state_courses);

        } catch (Exception e) {
            Analytics.logException(e);
            Log.d(TAG, "Error processing response: ", e);
            UIUtils.showAlert(getActivity(), R.string.loading, R.string.error_processing_response);
        }
    }

    private void filterNewCoursesNotSeen() {

        final long lastNewCourseSeenTimestamp = getPrefs().getLong(PrefsActivity.PREF_LAST_NEW_COURSE_SEEN_TIMESTAMP, 0);
        long newLastNewCourseSeenTimestamp = lastNewCourseSeenTimestamp;

        List<Course> installedCourses = coursesRepository.getCourses(getActivity());

        Iterator<CourseInstallViewAdapter> iter = courses.iterator();
        while (iter.hasNext()) {
            CourseInstallViewAdapter courseAdapter = iter.next();

            newLastNewCourseSeenTimestamp = (long) Math.max(newLastNewCourseSeenTimestamp, courseAdapter.getVersionId());

            boolean newCourseSeen = courseAdapter.getVersionId() <= lastNewCourseSeenTimestamp;
            if (isInstalled(courseAdapter, installedCourses) || newCourseSeen) {
                iter.remove();
            }

        }


        getPrefs().edit().putLong(PrefsActivity.PREF_LAST_NEW_COURSE_SEEN_TIMESTAMP, newLastNewCourseSeenTimestamp).commit();
    }


    private void filterOnlyInstalledCourses() {
        List<Course> installedCourses = coursesRepository.getCourses(getActivity());

        Iterator<CourseInstallViewAdapter> iter = courses.iterator();
        while (iter.hasNext()) {
            CourseInstallViewAdapter courseAdapter = iter.next();
            if (!isInstalled(courseAdapter, installedCourses)) {
                iter.remove();
            }
        }

    }

    private boolean isInstalled(CourseInstallViewAdapter courseAdapter, List<Course> installedCourses) {
        for (Course course : installedCourses) {
            if (TextUtils.equals(course.getShortname(), courseAdapter.getShortname())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {

        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.missing_media_sortby, menu);
        MenuItem sortBy = menu.findItem(R.id.menu_sort_by);
        if (sortBy != null) {
            sortBy.setVisible(false);
        }
    }

    //    public boolean onPrepareOptionsMenu(Menu menu) {
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_select_all:
                selectAllInstallableCourses();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectAllInstallableCourses() {
        for (int i = 0; i < coursesAdapter.getItemCount(); i++) {
            CourseInstallViewAdapter course = courses.get(i);
            if (course.isToInstall() && !multiChoiceHelper.isItemChecked(i)) {
                multiChoiceHelper.setItemChecked(i, true, true);
            }
        }
    }

    public void apiRequestComplete(BasicResult result) {
        hideProgressDialog();

        Callable<Boolean> finishActivity = () -> {
            getActivity().finish();
            return true;
        };

        if (result.isSuccess()) {
            try {

                json = new JSONObject(result.getResultMessage());
                refreshCourseList();

                if (courseToUpdate != null) {
                    findCourseAndDownload(courseToUpdate);
                }

            } catch (JSONException e) {
                Analytics.logException(e);
                Log.d(TAG, "Error connecting to server: ", e);
                UIUtils.showAlert(getActivity(), R.string.loading, R.string.error_connection, finishActivity);
            }
        } else {
            String errorMsg = result.getResultMessage();
            UIUtils.showAlert(getActivity(), R.string.error, errorMsg, finishActivity);
        }
    }

    private void findCourseAndDownload(Course courseToUpdate) {
        for (CourseInstallViewAdapter course : courses) {
            if (TextUtils.equals(course.getShortname(), courseToUpdate.getShortname())) {
                downloadCourse(course);
            }
        }
    }

    //@Override
    public void onDownloadProgress(String fileUrl, int progress) {
        CourseInstallViewAdapter course = findCourse(fileUrl);
        if (course != null) {
            course.setDownloading(true);
            course.setInstalling(false);
            course.setProgress(progress);
            coursesAdapter.notifyDataSetChanged();
        }
    }

    //@Override
    public void onInstallProgress(String fileUrl, int progress) {
        CourseInstallViewAdapter course = findCourse(fileUrl);
        if (course != null) {
            course.setDownloading(false);
            course.setInstalling(true);
            course.setProgress(progress);
            coursesAdapter.notifyDataSetChanged();
        }
    }

    //@Override
    public void onInstallFailed(String fileUrl, String message) {
        CourseInstallViewAdapter course = findCourse(fileUrl);
        if (course != null) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            resetCourseProgress(course, false, false);
        }
    }

    //@Override
    public void onInstallComplete(String fileUrl) {
        CourseInstallViewAdapter course = findCourse(fileUrl);
        if (course != null) {
            Toast.makeText(getActivity(), this.getString(R.string.install_course_complete, course.getShortname()), Toast.LENGTH_LONG).show();
            course.setInstalled(true);
            course.setToUpdate(false);
            resetCourseProgress(course, false, false);
        }
    }

    private CourseInstallViewAdapter findCourse(String fileUrl) {
        if (!courses.isEmpty()) {
            for (CourseInstallViewAdapter course : courses) {
                if (course.getDownloadUrl().equals(fileUrl)) {
                    return course;
                }
            }
        }
        return null;
    }

    protected void resetCourseProgress(CourseInstallViewAdapter courseSelected,
                                       boolean downloading, boolean installing) {

        courseSelected.setDownloading(downloading);
        courseSelected.setInstalling(installing);
        courseSelected.setProgress(0);
        coursesAdapter.notifyDataSetChanged();
    }


}

