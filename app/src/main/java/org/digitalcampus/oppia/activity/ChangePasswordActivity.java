package org.digitalcampus.oppia.activity;

import android.app.AlertDialog;
import android.os.Bundle;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityChangePasswordBinding;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.UpdateProfileTask;

import javax.inject.Inject;

public class ChangePasswordActivity extends AppActivity implements UpdateProfileTask.ResponseListener {

    private ActivityChangePasswordBinding binding;

    @Inject
    ApiEndpoint apiEndpoint;

    @Inject
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        getAppComponent().inject(this);

        binding.btnSaveNewPassword.setOnClickListener(v -> {

            showChangePasswordSuccessDialog();
        });

    }

    private void showChangePasswordSuccessDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.change_password_success)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    finish();
                }).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize(false);
    }


    private void executeUpdateProfileTask(final User user) {
        UpdateProfileTask task = new UpdateProfileTask(this, apiEndpoint);
        task.setResponseListener(this);
        task.execute(user);
    }


    // Request responses
    @Override
    public void onSubmitComplete(User u) {
        toast(R.string.profile_updated_successfuly);
        finish();
    }

    @Override
    public void onSubmitError(String error) {
        toast(error);
    }

    @Override
    public void onConnectionError(String error, User u) {
        toast(error);
    }
}
