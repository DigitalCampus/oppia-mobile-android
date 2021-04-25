package org.digitalcampus.oppia.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ExportedTrackersFileAdapter;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.fragments.DeleteAccountDialogFragment;
import org.digitalcampus.oppia.fragments.DownloadUserDataDialogFragment;
import org.digitalcampus.oppia.listener.ExportActivityListener;
import org.digitalcampus.oppia.listener.TrackerServiceListener;
import org.digitalcampus.oppia.model.ActivityLogRepository;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.task.DeleteAccountTask;
import org.digitalcampus.oppia.task.ExportActivityTask;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PrivacyActivity extends AppActivity implements DeleteAccountDialogFragment.DeleteAccountListener {

    private CheckBox analyticsCheck;
    private CheckBox bugreportCheck;
    private AlertDialog aDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        getAppComponent().inject(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        View aboutPrivacyInfo = findViewById(R.id.about_privacy_policy);
        View aboutWhatDataInfo = findViewById(R.id.about_privacy_what);
        View aboutWhyDataInfo = findViewById(R.id.about_privacy_why);
        //View aboutTermsInfo = findViewById(R.id.about_privacy_terms);

        aboutPrivacyInfo.setOnClickListener(view -> launchAboutPage(AboutActivity.TAB_PRIVACY_POLICY));
        aboutWhatDataInfo.setOnClickListener(view -> launchAboutPage(AboutActivity.TAB_PRIVACY_WHAT));
        aboutWhyDataInfo.setOnClickListener(view -> launchAboutPage(AboutActivity.TAB_PRIVACY_HOW));
        //aboutTermsInfo.setOnClickListener(view -> launchAboutPage(AboutActivity.TAB_PRIVACY_TERMS));

        if (!SessionManager.isLoggedIn(this)){
            findViewById(R.id.privacy_user_section).setVisibility(View.GONE);
            return;
        }

        analyticsCheck = findViewById(R.id.analytics_checkbox);
        analyticsCheck.setChecked(Analytics.isTrackingEnabled(this));
        analyticsCheck.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()){
                Analytics.enableTracking(this);
            }
            else{
                Analytics.disableTracking(this);
            }
        });

        bugreportCheck = findViewById(R.id.bugreport_checkbox);
        bugreportCheck.setChecked(Analytics.isBugReportEnabled(this));
        bugreportCheck.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()){
                Analytics.enableBugReport(this);
            }
            else{
                Analytics.disableBugReport(this);
            }
        });

        Button deleteBtn = findViewById(R.id.btn_delete_account);
        deleteBtn.setOnClickListener(v -> showDeleteAccountWarning());
        Button downloadBtn = findViewById(R.id.btn_download_data);
        downloadBtn.setOnClickListener(v -> showDownloadDataDialog());

    }

    private void launchAboutPage(int tab){
        Intent i = new Intent(this, AboutActivity.class);
        Bundle tb = new Bundle();
        tb.putString(AboutActivity.TITLE, getString(R.string.privacy_section_privacy));
        tb.putString(AboutActivity.ABOUT_CONTENTS, AboutActivity.ABOUT_PRIVACY);
        tb.putInt(AboutActivity.TAB_ACTIVE, tab);
        i.putExtras(tb);
        startActivity(i);
    }

    private void showDeleteAccountWarning(){

        FragmentManager fm = getSupportFragmentManager();
        DeleteAccountDialogFragment deleteDialog = DeleteAccountDialogFragment.newInstance();
        deleteDialog.show(fm, "fragment_delete_account");

    }

    private void showDownloadDataDialog(){
        FragmentManager fm = getSupportFragmentManager();
        DownloadUserDataDialogFragment downloadDialog = DownloadUserDataDialogFragment.newInstance();
        downloadDialog.show(fm, "fragment_download_data");
    }


    @Override
    public void onDeleteAccountSuccess() {
        logoutAndRestartApp();
    }
}
