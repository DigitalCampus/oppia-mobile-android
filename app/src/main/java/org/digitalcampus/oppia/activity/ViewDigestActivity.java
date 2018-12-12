package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;

public class ViewDigestActivity extends AppActivity {

    public static final String TAG = ViewDigestActivity.class.getSimpleName();
    public static final String ACTIVITY_DIGEST_PARAM = "digest";

    private TextView errorText;
    private View activityDetail;
    private Activity activity;
    private Course activityCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_digest);

        final Intent intent = getIntent();
        final String digest = intent.getData().getQueryParameter(ACTIVITY_DIGEST_PARAM);

        activityDetail = findViewById(R.id.activity_detail);
        errorText = (TextView) findViewById(R.id.error_text);

        boolean validDigest = validate(digest);
        if (validDigest){
            Log.d(TAG, "Digest valid, checking activity");
            DbHelper db = DbHelper.getInstance(this);
            activity = db.getActivityByDigest(digest);

            if (activity == null){
                errorText.setText(this.getText(R.string.open_digest_errors_activity_not_found));
                errorText.setVisibility(View.VISIBLE);
                activityDetail.setVisibility(View.GONE);
            }
            else{
                long userID = db.getUserId(SessionManager.getUsername(this));
                activityCourse = db.getCourse(activity.getCourseId(), userID);
                activity.setCompleted(db.activityCompleted(activityCourse.getCourseId(), digest, userID));
                Intent i = new Intent(ViewDigestActivity.this, CourseIndexActivity.class);
                Bundle tb = new Bundle();
                tb.putSerializable(Course.TAG, activityCourse);
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
        if (!SessionManager.isLoggedIn(this)){
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
