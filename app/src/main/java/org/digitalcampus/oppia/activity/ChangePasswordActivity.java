package org.digitalcampus.oppia.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityChangePasswordBinding;
import org.digitalcampus.mobile.learning.databinding.ActivityEditProfileBinding;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomFieldsRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.UpdateProfileTask;
import org.digitalcampus.oppia.utils.ui.fields.CustomFieldsUIManager;
import org.digitalcampus.oppia.utils.ui.fields.ValidableField;

import java.util.HashMap;
import java.util.List;

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

        });

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
