package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.User;

import javax.inject.Inject;

public class ViewDigestActivity extends AppActivity {

    public static final String ACTIVITY_DIGEST_PARAM = "digest";
    public static final String COURSE_SHORTNAME = "course";

    private TextView errorText;
    private View activityDetail;

    @Inject
    CoursesRepository coursesRepository;
    @Inject
    User user;

    @Override
    public void onStart() {
        super.onStart();
        initialize();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_digest);
        getAppComponent().inject(this);

        processLinkPath(getIntent().getData());

    }

    private void processLinkPath(Uri data) {

        if (data == null || data.getPathSegments().isEmpty()) {
            // weblink not supported
            finish();
            return;
        }

        String firstPathSegment = data.getPathSegments().get(0);

        switch (firstPathSegment) {
            case "view":
                processActivityDigestLink(data);
                break;

            case "open":
                processCourseLink(data);
                break;
        }
    }

    private void processCourseLink(Uri data) {

        String shortname = data.getQueryParameter(COURSE_SHORTNAME);
        Course course = coursesRepository.getCourseByShortname(this, shortname, user.getUserId());
        if (course != null) {
            Intent i = new Intent(this, CourseIndexActivity.class);
            Bundle tb = new Bundle();
            tb.putSerializable(Course.TAG, course);
            i.putExtras(tb);
            startActivity(i);
        } else {
            // todo download it
        }

    }

    private void processActivityDigestLink(Uri data) {

        String digest = data.getQueryParameter(ACTIVITY_DIGEST_PARAM);

        activityDetail = findViewById(R.id.activity_detail);
        errorText = findViewById(R.id.error_text);

        boolean validDigest = validate(digest);
        if (validDigest) {
            Log.d(TAG, "Digest valid, checking activity");
            Activity activity = coursesRepository.getActivityByDigest(this, digest);

            if (activity == null) {
                errorText.setText(this.getText(R.string.open_digest_errors_activity_not_found));
                errorText.setVisibility(View.VISIBLE);
                activityDetail.setVisibility(View.GONE);
            } else {
                Course course = coursesRepository.getCourse(this, activity.getCourseId(), user.getUserId());
                Intent i = new Intent(this, CourseIndexActivity.class);
                Bundle tb = new Bundle();
                tb.putSerializable(Course.TAG, course);
                tb.putSerializable(CourseIndexActivity.JUMPTO_TAG, activity.getDigest());
                i.putExtras(tb);
                startActivity(i);
            }
        }
    }

    private boolean validate(String digest) {
        if (digest == null) {
            //The query parameter is missing or misconfigured
            Log.d(TAG, "Invalid digest");
            errorText.setText(this.getText(R.string.open_digest_errors_invalid_param));
            errorText.setVisibility(View.VISIBLE);
            activityDetail.setVisibility(View.GONE);
            return false;
        }
        if (user == null || TextUtils.isEmpty(user.getUsername())) {
            Log.d(TAG, "Not logged in");
            errorText.setText(this.getText(R.string.open_digest_errors_not_logged_in));
            errorText.setVisibility(View.VISIBLE);
            activityDetail.setVisibility(View.GONE);
            return false;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.finish();
    }
}
