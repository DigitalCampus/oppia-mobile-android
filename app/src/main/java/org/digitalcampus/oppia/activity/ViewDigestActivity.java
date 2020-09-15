package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityViewDigestBinding;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerService;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerServiceDelegate;
import org.digitalcampus.oppia.service.courseinstall.InstallerBroadcastReceiver;

import java.nio.file.Path;

import javax.inject.Inject;

public class ViewDigestActivity extends AppActivity implements CourseInstallerListener, View.OnClickListener {

    public static final String ACTIVITY_DIGEST_PARAM = "digest";
    public static final String COURSE_SHORTNAME_PARAM = "course";

    private TextView errorText;

    @Inject
    CoursesRepository coursesRepository;
    @Inject
    User user;
    @Inject
    CourseInstallerServiceDelegate courseInstallerServiceDelegate;
    private ActivityViewDigestBinding binding;
    private InstallerBroadcastReceiver receiver;
    private String courseShortname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewDigestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getAppComponent().inject(this);

        if (TextUtils.isEmpty(SessionManager.getUsername(this))) {
            toast(R.string.login_to_continue);
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        processLinkPath(getIntent().getData());

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
            // catches data == null, path segment empty or != "view"
            toast(R.string.weblink_not_supported);
            finish();
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
        Course course = coursesRepository.getCourseByShortname(this, shortname, user.getUserId());
        if (course != null) {
            Intent i = new Intent(this, CourseIndexActivity.class);
            Bundle tb = new Bundle();
            tb.putSerializable(Course.TAG, course);
            i.putExtras(tb);
            startActivity(i);
            finish();
        } else {
            binding.courseTitle.setText(shortname);
            this.courseShortname = shortname;
            binding.downloadCourseBtn.setOnClickListener(this);
        }

    }

    private void downloadCourse() {

        updateProgress(0);

        Intent serviceIntent = new Intent(this, CourseInstallerService.class);
        Course courseDownload = new Course();
        String oppiaServer = prefs.getString("prefServer", getString(R.string.prefServerDefault));
        courseDownload.setDownloadUrl(oppiaServer + String.format(Paths.COURSE_DOWNLOAD_SHORTNAME_PATH, courseShortname));
        courseDownload.setShortname(courseShortname);
        courseDownload.setVersionId((double) 1);
        courseInstallerServiceDelegate.installCourse(this, serviceIntent, courseDownload);

    }

    private void processActivityDigestLink(Uri data) {

        String digest = data.getQueryParameter(ACTIVITY_DIGEST_PARAM);

        errorText = findViewById(R.id.error_text);

        boolean validDigest = validate(digest);
        if (validDigest) {
            Log.d(TAG, "Digest valid, checking activity");
            Activity activity = coursesRepository.getActivityByDigest(this, digest);

            if (activity == null) {
                errorText.setText(this.getText(R.string.open_digest_errors_activity_not_found));
                errorText.setVisibility(View.VISIBLE);
            } else {
                Course course = coursesRepository.getCourse(this, activity.getCourseId(), user.getUserId());
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

    private boolean validate(String digest) {
        if (digest == null) {
            //The query parameter is missing or misconfigured
            Log.d(TAG, "Invalid digest");
            errorText.setText(this.getText(R.string.open_digest_errors_invalid_param));
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        if (user == null || TextUtils.isEmpty(user.getUsername())) {
            Log.d(TAG, "Not logged in");
            errorText.setText(this.getText(R.string.open_digest_errors_not_logged_in));
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.download_course_btn:

                downloadCourse();

                break;
        }
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

    }

}
