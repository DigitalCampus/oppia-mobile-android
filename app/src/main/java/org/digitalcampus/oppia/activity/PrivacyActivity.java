package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityPrivacyBinding;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.fragments.DeleteAccountDialogFragment;
import org.digitalcampus.oppia.fragments.DownloadUserDataDialogFragment;
import org.digitalcampus.oppia.utils.ConnectionUtils;

public class PrivacyActivity extends AppActivity implements DeleteAccountDialogFragment.DeleteAccountListener {

    private ActivityPrivacyBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        getAppComponent().inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        binding.aboutPrivacyPolicy.setOnClickListener(view -> launchAboutPage(AboutActivity.TAB_PRIVACY_POLICY));
        binding.aboutPrivacyWhat.setOnClickListener(view -> launchAboutPage(AboutActivity.TAB_PRIVACY_WHAT));
        binding.aboutPrivacyWhy.setOnClickListener(view -> launchAboutPage(AboutActivity.TAB_PRIVACY_HOW));
        binding.aboutPrivacyTerms.setOnClickListener(view -> launchAboutPage(AboutActivity.TAB_PRIVACY_TERMS));

        binding.analyticsCheckbox.setChecked(Analytics.isTrackingEnabled(this));
        binding.analyticsCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()){
                Analytics.enableTracking(this);
            }
            else{
                Analytics.disableTracking(this);
            }
        });

        binding.bugreportCheckbox.setChecked(Analytics.isBugReportEnabled(this));
        binding.bugreportCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()){
                Analytics.enableBugReport(this);
            }
            else{
                Analytics.disableBugReport(this);
            }
        });


        if (!SessionManager.isLoggedIn(this)){
            binding.privacyUserSection.setVisibility(View.GONE);
            return;
        }

        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountWarning());
        binding.btnDownloadData.setOnClickListener(v -> showDownloadDataDialog());

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

        if (!ConnectionUtils.isNetworkConnected(this)) {
            alert(R.string.error_connection_needed);
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        DeleteAccountDialogFragment deleteDialog = DeleteAccountDialogFragment.newInstance();
        deleteDialog.show(fm, "fragment_delete_account");

    }

    private void showDownloadDataDialog(){

        if (!ConnectionUtils.isNetworkConnected(this)) {
            alert(R.string.error_connection_needed);
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        DownloadUserDataDialogFragment downloadDialog = DownloadUserDataDialogFragment.newInstance();
        downloadDialog.show(fm, "fragment_download_data");
    }


    @Override
    public void onDeleteAccountSuccess() {
        logoutAndRestartApp();
    }
}
