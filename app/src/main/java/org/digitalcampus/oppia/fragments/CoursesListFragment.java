package org.digitalcampus.oppia.fragments;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.DownloadMediaActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.adapter.CoursesListAdapter;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.listener.DeleteCourseListener;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.listener.UpdateActivityListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.service.courseinstall.CourseIntallerService;
import org.digitalcampus.oppia.service.courseinstall.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.task.DeleteCourseTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.ScanMediaTask;
import org.digitalcampus.oppia.task.UpdateCourseActivityTask;
import org.digitalcampus.oppia.utils.UIUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.inject.Inject;


public class CoursesListFragment extends AppFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        ScanMediaListener,
        DeleteCourseListener,
        CourseInstallerListener,
        UpdateActivityListener, CoursesListAdapter.OnItemClickListener {

    private ArrayList<Course> courses;
    private Course tempCourse;
    private int initialCourseListPadding = 0;

    private TextView messageText;
    private Button messageButton;
    private View messageContainer;
    private View noCoursesView;

    private ProgressDialog progressDialog;
    private InstallerBroadcastReceiver receiver;

    @Inject CoursesRepository coursesRepository;
    @Inject SharedPreferences prefs;
    private LinearLayout llLoading;
    private TextView tvManageCourses;
    private Button manageBtn;
    private RecyclerView recyclerCourses;
    private CoursesListAdapter adapterListCourses;

    private void findViews(View layout) {

        messageContainer = layout.findViewById(R.id.home_messages);
        messageText = layout.findViewById(R.id.home_message);
        messageButton = layout.findViewById(R.id.message_action_button);
        recyclerCourses = layout.findViewById(R.id.recycler_courses);
        noCoursesView = layout.findViewById(R.id.no_courses);
        llLoading = layout.findViewById(R.id.loading_courses);

        tvManageCourses = layout.findViewById(R.id.manage_courses_text);
        manageBtn = layout.findViewById(R.id.manage_courses_btn);
        
        
    }
    
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_courses_list, container, false);
        findViews(layout);

        initializeDagger();

        prefs.registerOnSharedPreferenceChangeListener(this);

        // set preferred lang to the default lang
        if ("".equals(prefs.getString(PrefsActivity.PREF_LANGUAGE, ""))) {
            prefs.edit().putString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()).apply();
        }

        if (getResources().getBoolean(R.bool.is_tablet)) {
            recyclerCourses.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        } else {
            recyclerCourses.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        courses = new ArrayList<>();
        adapterListCourses = new CoursesListAdapter(getActivity(), courses);
        adapterListCourses.setOnItemClickListener(this);
        recyclerCourses.setAdapter(adapterListCourses);

        initialCourseListPadding = recyclerCourses.getPaddingTop();

        return layout;
    }

    private void initializeDagger() {
        MobileLearning app = (MobileLearning) getActivity().getApplication();
        app.getComponent().inject(this);
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
        IntentFilter broadcastFilter = new IntentFilter(CourseIntallerService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        getActivity().registerReceiver(receiver, broadcastFilter);

        if (adapterListCourses != null) {
            adapterListCourses.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    private void displayCourses() {
        courses.clear();
        courses.addAll(coursesRepository.getCourses(getActivity()));

        llLoading.setVisibility(View.GONE);

        if (courses.size() < MobileLearning.DOWNLOAD_COURSES_DISPLAY){
            displayDownloadSection();
        } else {
            tvManageCourses.setText(R.string.no_courses);
            noCoursesView.setVisibility(View.GONE);
        }

        adapterListCourses.notifyDataSetChanged();
        this.scanMedia();
    }

    private void displayDownloadSection(){
        noCoursesView.setVisibility(View.VISIBLE);

        tvManageCourses.setText((!courses.isEmpty())? R.string.more_courses : R.string.no_courses);

        manageBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AdminSecurityManager.with(getActivity()).checkAdminPermission(R.id.menu_download, new AdminSecurityManager.AuthListener() {
                    public void onPermissionGranted() {
                        startActivity(new Intent(getActivity(), TagSelectActivity.class));
                    }
                });
            }
        });
    }

    private void createLanguageDialog() {
        ArrayList<Lang> langs = new ArrayList<>();
        for(Course m: courses){ langs.addAll(m.getLangs()); }

        UIUtils.createLanguageDialog(getActivity(), langs, prefs, new Callable<Boolean>() {
            public Boolean call(){
                onStart();
                return true;
            }
        });
    }


    // Recycler callbacks
    @Override
    public void onItemClick(int position) {
        Course selectedCourse = courses.get(position);
        Intent i = new Intent(getActivity(), CourseIndexActivity.class);
        Bundle tb = new Bundle();
        tb.putSerializable(Course.TAG, selectedCourse);
        i.putExtras(tb);
        startActivity(i);
    }

    @Override
    public void onContextMenuItemSelected(final int position, final int itemId) {
        AdminSecurityManager.with(getActivity()).checkAdminPermission(itemId, new AdminSecurityManager.AuthListener() {
            public void onPermissionGranted() {
                tempCourse = courses.get(position);
                if (itemId == R.id.course_context_delete) {
                    if (prefs.getBoolean(PrefsActivity.PREF_DELETE_COURSE_ENABLED, true)){
                        confirmCourseDelete();
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.warning_delete_disabled), Toast.LENGTH_LONG).show();
                    }
                } else if (itemId == R.id.course_context_reset) {
                    confirmCourseReset();
                } else if (itemId == R.id.course_context_update_activity){
                    confirmCourseUpdateActivity();
                }
            }
        });
    }

    private void confirmCourseDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Oppia_AlertDialogStyle);
//        builder.setCancelable(false);
        builder.setTitle(R.string.course_context_delete);
        builder.setMessage(R.string.course_context_delete_confirm);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DeleteCourseTask task = new DeleteCourseTask(getActivity());
                ArrayList<Object> payloadData = new ArrayList<>();
                payloadData.add(tempCourse);
                Payload p = new Payload(payloadData);
                task.setOnDeleteCourseListener(CoursesListFragment.this);
                task.execute(p);

                progressDialog = new ProgressDialog(getActivity(), R.style.Oppia_AlertDialogStyle);
                progressDialog.setMessage(getString(R.string.course_deleting));
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                tempCourse = null;
            }
        });
        builder.show();
    }

    private void confirmCourseReset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Oppia_AlertDialogStyle);
//        builder.setCancelable(false);
        builder.setTitle(R.string.course_context_reset);
        builder.setMessage(R.string.course_context_reset_confirm);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DbHelper db = DbHelper.getInstance(getActivity());
                db.resetCourse(tempCourse.getCourseId(), SessionManager.getUserId(getActivity()));
                displayCourses();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                tempCourse = null;
            }
        });
        builder.show();
    }

    private void confirmCourseUpdateActivity(){

        progressDialog = new ProgressDialog(getActivity(), R.style.Oppia_AlertDialogStyle);
        progressDialog.setMessage(getString(R.string.course_updating));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        UpdateCourseActivityTask task = new UpdateCourseActivityTask(getActivity(), SessionManager.getUserId(getActivity()));
        ArrayList<Object> payloadData = new ArrayList<>();
        payloadData.add(tempCourse);
        Payload p = new Payload(payloadData);
        task.setUpdateActivityListener(CoursesListFragment.this);
        task.execute(p);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equalsIgnoreCase(PrefsActivity.PREF_SHOW_SCHEDULE_REMINDERS) || key.equalsIgnoreCase(PrefsActivity.PREF_NO_SCHEDULE_REMINDERS)){
            displayCourses();
        }

    }

    //region ScanMedia
    ///Everything related to the ScanMediaTask, including UI management

    private void scanMedia() {

        if (Media.shouldScanMedia(prefs)){
            ScanMediaTask task = new ScanMediaTask(getActivity());
            Payload p = new Payload(this.courses);
            task.setScanMediaListener(this);
            task.execute(p);
        }
        else{
            hideScanMediaMessage();
        }
    }

    private void animateScanMediaMessage(){
        TranslateAnimation anim = new TranslateAnimation(0, 0, -200, 0);
        anim.setDuration(900);
        messageContainer.startAnimation(anim);

        messageContainer.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ValueAnimator animator = ValueAnimator.ofInt(initialCourseListPadding, messageContainer.getMeasuredHeight());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            //@Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                recyclerCourses.setPadding(0, (Integer) valueAnimator.getAnimatedValue(), 0, 0);
//                recyclerCourses.setSelectionAfterHeaderView();
            }
        });
        animator.setStartDelay(200);
        animator.setDuration(700);
        animator.start();
    }

    private void hideScanMediaMessage(){
        messageContainer.setVisibility(View.GONE);
        recyclerCourses.setPadding(0, initialCourseListPadding, 0, 0);
    }

    /* ScanMediaListener implementation */
    public void scanStart() {
        messageText.setText(this.getString(R.string.info_scan_media_start));
    }

    public void scanProgressUpdate(String msg) {
        messageText.setText(this.getString(R.string.info_scan_media_checking, msg));
    }

    public void scanComplete(Payload response) {
        if (!response.getResponseData().isEmpty()) {
            if (messageContainer.getVisibility() != View.VISIBLE){
                messageContainer.setVisibility(View.VISIBLE);
                messageButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View view) {
                        @SuppressWarnings("unchecked")
                        ArrayList<Object> m = (ArrayList<Object>) view.getTag();
                        Intent i = new Intent(getActivity(), DownloadMediaActivity.class);
                        Bundle tb = new Bundle();
                        tb.putSerializable(DownloadMediaActivity.MISSING_MEDIA, m);
                        i.putExtras(tb);
                        startActivity(i);
                    }
                });
                animateScanMediaMessage();
            }

            messageText.setText(this.getString(R.string.info_scan_media_missing));
            messageButton.setText(this.getString(R.string.scan_media_download_button));
            messageButton.setTag(response.getResponseData());
            Media.resetMediaScan(prefs);
        } else {
            hideScanMediaMessage();
            messageButton.setOnClickListener(null);
            messageButton.setTag(null);
            Media.updateMediaScan(prefs);
        }
    }

    @Override
    public void onCourseDeletionComplete(Payload response) {
        if (response.isResult()){
            Media.resetMediaScan(prefs);
        }
        if (progressDialog != null){
            progressDialog.dismiss();
        }

        toast(response.isResult()? R.string.course_deleting_success : R.string.course_deleting_error);
        displayCourses();
        getActivity().invalidateOptionsMenu();
    }

    /* CourseInstallerListener implementation */
    public void onInstallComplete(String fileUrl) {
        toast(R.string.install_complete);
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

    public void updateActivityComplete(Payload response) {
        Course course = (Course) response.getData().get(0);
        if (progressDialog != null){
            progressDialog.dismiss();
        }

        toast(getString(response.isResult() ? R.string.course_updating_success :
                        R.string.course_updating_error, (course!=null) ? course.getShortname() : ""));

        displayCourses();
    }



}