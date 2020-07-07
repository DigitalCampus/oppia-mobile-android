package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.User;

import javax.inject.Inject;

public class ViewDigestActivity extends AppActivity {

    public static final String ACTIVITY_DIGEST_PARAM = "digest";

    private TextView errorText;
    private View activityDetail;

    @Inject CoursesRepository coursesRepository;
    @Inject User user;

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

        Intent intent = getIntent();
        String digest = null;
        if (intent.getData() != null){
            digest = intent.getData().getQueryParameter(ACTIVITY_DIGEST_PARAM);
        }

        activityDetail = findViewById(R.id.activity_detail);
        errorText = findViewById(R.id.error_text);

        boolean validDigest = validate(digest);
        if (validDigest){
            Log.d(TAG, "Digest valid, checking activity");
            Activity activity = coursesRepository.getActivityByDigest(this, digest);
            DbHelper db = DbHelper.getInstance(this);

            if (activity == null){
                errorText.setText(this.getText(R.string.open_digest_errors_activity_not_found));
                errorText.setVisibility(View.VISIBLE);
                activityDetail.setVisibility(View.GONE);
            }
            else{
                Course course = coursesRepository.getCourse(this, activity.getCourseId(), user.getUserId());
                activity.setCompleted(db.activityCompleted(course.getCourseId(), digest, user.getUserId()));
                Intent i = new Intent(ViewDigestActivity.this, CourseIndexActivity.class);
                Bundle tb = new Bundle();
                tb.putSerializable(Course.TAG, course);
                tb.putSerializable(CourseIndexActivity.JUMPTO_TAG, activity.getDigest());
                i.putExtras(tb);
                ViewDigestActivity.this.startActivity(i);
            }
        }

    }

    private boolean validate(String digest){
        if (digest == null){
            //The query parameter is missing or misconfigured
            Log.d(TAG, "Invalid digest");
            errorText.setText(this.getText(R.string.open_digest_errors_invalid_param));
            errorText.setVisibility(View.VISIBLE);
            activityDetail.setVisibility(View.GONE);
            return false;
        }
        if (user == null){
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
