package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityViewDigestBinding;
import org.digitalcampus.oppia.adapter.CourseInstallViewAdapter;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerService;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerServiceDelegate;
import org.digitalcampus.oppia.service.courseinstall.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.task.CourseInfoTask;
import org.digitalcampus.oppia.task.Payload;

import java.util.Arrays;

import javax.inject.Inject;

public class ViewDigestActivity extends AppActivity implements CourseInstallerListener, View.OnClickListener, CourseInfoTask.CourseInfoListener {

    public static final String ACTIVITY_DIGEST_PARAM = "digest";
    public static final String COURSE_SHORTNAME_PARAM = "course";

    @Inject
    CoursesRepository coursesRepository;
    @Inject
    User user;
    @Inject
    CourseInstallerServiceDelegate courseInstallerServiceDelegate;

    @Inject
    ApiEndpoint apiEndpoint;

    private ActivityViewDigestBinding binding;
    private InstallerBroadcastReceiver receiver;
    private CourseInstallViewAdapter course;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewDigestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getAppComponent().inject(this);

        if (isUserLoggedIn()) {
            processLinkPath(getIntent().getData());
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
    }

    @Override
    public void onResume() {
        super.onResume();

        receiver = new InstallerBroadcastReceiver();
        receiver.setCourseInstallerListener(this);
        IntentFilter broadcastFilter = new IntentFilter(CourseInstallerService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(receiver, broadcastFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void processLinkPath(Uri data) {

        try {
            if (!TextUtils.equals(data.getPathSegments().get(0), "view")) {
                throw new IllegalArgumentException("Incorrect path segment");
            }
        } catch (Exception e) {
            // catches: data == null, path segment empty or != "view"
            showError(getString(R.string.open_digest_weblink_not_supported), true);
            return;
        }

        if (data.getQueryParameterNames().contains(ACTIVITY_DIGEST_PARAM)) {
            processActivityDigestLink(data);
        } else if (data.getQueryParameterNames().contains(COURSE_SHORTNAME_PARAM)) {
            processCourseLink(data);
        }

    }

    private void processCourseLink(Uri data) {

        String shortname = data.getQueryParameter(COURSE_SHORTNAME_PARAM);
        Course localCourse = coursesRepository.getCourseByShortname(this, shortname, user.getUserId());
        if (localCourse != null) {
            Intent i = new Intent(this, CourseIndexActivity.class);
            Bundle tb = new Bundle();
            tb.putSerializable(Course.TAG, localCourse);
            i.putExtras(tb);
            startActivity(i);
            finish();
        } else {
            showError(getString(R.string.open_digest_course_not_installed), false);
            fetchCourseInfo(shortname);
        }

    }

    private void fetchCourseInfo(String shortname) {

        binding.courseTitle.setText(R.string.open_digest_fetching_course_info);
        binding.downloadProgress.setVisibility(View.VISIBLE);

        Payload p = new Payload(Arrays.asList(shortname));
        CourseInfoTask task = new CourseInfoTask(this, apiEndpoint);
        task.setListener(this);
        task.execute(p);
    }

    private void downloadCourse() {

        updateProgress(0);

        Intent serviceIntent = new Intent(this, CourseInstallerService.class);
        courseInstallerServiceDelegate.installCourse(this, serviceIntent, course);

    }

    private void processActivityDigestLink(Uri data) {

        String digest = data.getQueryParameter(ACTIVITY_DIGEST_PARAM);

        boolean validDigest = validateDigest(digest);
        if (validDigest) {
            Log.d(TAG, "Digest valid, checking activity");
            Activity activity = coursesRepository.getActivityByDigest(this, digest);

            if (activity == null) {
                showError(getString(R.string.open_digest_errors_activity_not_found), true);
            } else {
                Intent i = new Intent(this, CourseIndexActivity.class);
                Bundle tb = new Bundle();
                tb.putSerializable(Course.TAG, course);
                tb.putSerializable(CourseIndexActivity.JUMPTO_TAG, activity.getDigest());
                i.putExtras(tb);
                startActivity(i);
                finish();
            }
        }
    }

    private boolean validateDigest(String digest) {
        if (digest == null) {
            //The query parameter is missing or misconfigured
            Log.d(TAG, "Invalid digest");
            showError(getString(R.string.open_digest_errors_invalid_param), true);
            return false;
        }
        return true;
    }

    private boolean isUserLoggedIn() {

        if (user == null || TextUtils.isEmpty(user.getUsername())) {
            Log.d(TAG, "Not logged in");
            showError(getString(R.string.open_digest_errors_not_logged_in), true);
            return false;
        }
        return true;
    }

    private void showError(String errorMessage, boolean hideCourseCard) {
        binding.courseCard.setVisibility(hideCourseCard ? View.GONE : View.VISIBLE);
        binding.errorText.setVisibility(View.VISIBLE);
        binding.errorText.setText(errorMessage);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.download_course_btn:
                downloadCourse();
                break;

            case R.id.btn_go_to_course:

                Course courseInstalled = coursesRepository.getCourseByShortname(this, course.getShortname(), user.getUserId());
                if (course != null) {
                    Intent i = new Intent(this, CourseIndexActivity.class);
                    Bundle tb = new Bundle();
                    tb.putSerializable(Course.TAG, courseInstalled);
                    i.putExtras(tb);
                    startActivity(i);
                    finish();
                }
                break;
            default:
                //do nothing
        }
    }

    // COURSE INFO LISTENERS
    @Override
    public void onSuccess(CourseInstallViewAdapter course) {
        this.course = course;
        binding.downloadProgress.setVisibility(View.GONE);
        binding.downloadCourseBtn.setOnClickListener(this);

        showCourseInfo();
    }

    private void showCourseInfo() {
        binding.courseTitle.setText(course.getTitle(prefs));
        String description = course.getDescription(prefs);
        binding.courseDescription.setText(description != null ? description : "");
        if (description == null){
            binding.courseDescription.setVisibility(View.GONE);
        }
    }

    @Override
    public void onError(String error) {
        showError(error, true);
        binding.downloadProgress.setVisibility(View.GONE);
    }

    // DOWNLOAD COURSE LISTENERS

    @Override
    public void onDownloadProgress(String fileUrl, int progress) {
        Log.i(TAG, "DOWNLOAD COURSE LISTENERS onDownloadProgress: " + progress);
        updateProgress(progress);
    }

    @Override
    public void onInstallProgress(String fileUrl, int progress) {
        Log.i(TAG, "DOWNLOAD COURSE LISTENERS onInstallProgress: " + progress);
        updateProgress(progress);
    }

    private void updateProgress(int progress) {

        binding.downloadProgress.setVisibility(View.VISIBLE);
        if (progress > 0) {
            binding.downloadProgress.setIndeterminate(false);
            binding.downloadProgress.setProgress(progress);
        } else {
            binding.downloadProgress.setIndeterminate(true);
        }
    }

    @Override
    public void onInstallFailed(String fileUrl, String message) {
        Log.i(TAG, "DOWNLOAD COURSE LISTENERS onInstallFailed: ");
        binding.downloadProgress.setVisibility(View.GONE);

    }

    @Override
    public void onInstallComplete(String fileUrl) {
        Log.i(TAG, "DOWNLOAD COURSE LISTENERS onInstallComplete: ");
        binding.downloadProgress.setVisibility(View.GONE);
        binding.btnGoToCourse.setVisibility(View.VISIBLE);
        binding.btnGoToCourse.setOnClickListener(this);

    }

}
