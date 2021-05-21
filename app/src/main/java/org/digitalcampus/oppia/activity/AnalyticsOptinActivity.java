package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;

import org.digitalcampus.mobile.learning.databinding.ActivityAnalyticsOptinBinding;
import org.digitalcampus.oppia.analytics.Analytics;

public class AnalyticsOptinActivity extends AppActivity {

    private ActivityAnalyticsOptinBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyticsOptinBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());


        binding.continueButton.setOnClickListener(view -> {
            Analytics.optOutRationaleShown(this);

            if (binding.analyticsCheckbox.isChecked()){
                Analytics.enableTracking(this);
            }
            if (binding.bugreportCheckbox.isChecked()){
                Analytics.enableBugReport(this);
            }

            finish();
        });

    }

    @Override
    public void onBackPressed() {
        // Block back behaviour to forze press continue button
    }
}
