package org.digitalcampus.oppia.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityChangePasswordBinding;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.ChangePasswordTask;
import org.digitalcampus.oppia.task.UpdateProfileTask;

import javax.inject.Inject;

public class ChangePasswordActivity extends AppActivity implements ChangePasswordTask.ResponseListener {

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
            checkAndChangePasswords();
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


    private void checkAndChangePasswords() {

        String pass1 = binding.fieldPassword.getEditText().getText().toString();
        String pass2 = binding.fieldPasswordRepeat.getEditText().getText().toString();

        if (!TextUtils.equals(pass1, pass2)) {
            toast(R.string.error_register_password_no_match);
            return;
        }

        showProgressDialog(getString(R.string.loading));

        ChangePasswordTask task = new ChangePasswordTask(this, apiEndpoint);
        task.setResponseListener(this);
        task.execute(pass1, pass2);
    }


    // Request responses
    @Override
    public void onSuccess() {
        hideProgressDialog();
        showChangePasswordSuccessDialog();
    }

    @Override
    public void onError(String error) {
        hideProgressDialog();
        alert(error);
    }
}
