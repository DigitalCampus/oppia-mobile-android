package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Activity;
import org.w3c.dom.Text;

public class ViewDigestActivity extends AppCompatActivity {

    public static final String TAG = ViewDigestActivity.class.getSimpleName();
    public static final String ACTIVITY_DIGEST_PARAM = "digest";

    private TextView errorText;
    private View activityDetail;

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
            Activity act = DbHelper.getInstance(this).getActivityByDigest(digest);
            if (act == null){
                errorText.setText(this.getText(R.string.open_digest_errors_activity_not_found));
                validDigest = false;
            }
            else{
                showActivityDetails(act);
            }
        }

        if (validDigest){
            errorText.setVisibility(View.GONE);
        }
        else{
            activityDetail.setVisibility(View.GONE);
        }


    }

    private boolean validate(String digest){
        if (digest == null){
            //The query parameter is missing or misconfigured
            errorText.setText(this.getText(R.string.open_digest_errors_invalid_param));
            return false;
        }

        if (!SessionManager.isLoggedIn(this)){
            Log.d(TAG, "Not logged in");
            errorText.setText(this.getText(R.string.open_digest_errors_not_logged_in));
            return false;
        }
        return true;
    }

    private void showActivityDetails(Activity act){

    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }
}
