package org.digitalcampus.oppia.activity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityEditProfileBinding;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.UpdateProfileTask;
import org.digitalcampus.oppia.utils.ui.CustomFieldsUIManager;
import org.digitalcampus.oppia.utils.ui.ValidableTextInputLayout;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class EditProfileActivity extends AppActivity implements View.OnClickListener, UpdateProfileTask.ResponseListener {

    private ActivityEditProfileBinding binding;
    private CustomFieldsUIManager fieldsManager;

    @Inject
    ApiEndpoint apiEndpoint;

    @Inject
    User user;

    @Inject
    List<CustomField> profileCustomFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSaveProfile.setOnClickListener(this);

        getAppComponent().inject(this);

        fieldsManager = new CustomFieldsUIManager(this, profileCustomFields);
        fieldsManager.createFieldsInContainer(binding.customFieldsContainer);

        fillUserProfileData();
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize(false);
    }


    private void fillUserProfileData() {

        binding.fieldEmail.setText(user.getEmail());
        binding.fieldFirstname.setText(user.getFirstname());
        binding.fieldLastname.setText(user.getLastname());
        binding.fieldOrganisation.setText(user.getOrganisation());
        binding.fieldJobtitle.setText(user.getJobTitle());
        fieldsManager.fillWithUserData(user);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_save_profile:
                getDataAndSend();
                break;
        }
    }

    private void getDataAndSend() {

        String email = binding.fieldEmail.getCleanedValue();
        String firstname = binding.fieldFirstname.getCleanedValue();
        String lastname = binding.fieldLastname.getCleanedValue();
        String jobTitle = binding.fieldJobtitle.getCleanedValue();
        String organisation = binding.fieldOrganisation.getCleanedValue();

        ValidableTextInputLayout[] fields = new ValidableTextInputLayout[]{binding.fieldEmail, binding.fieldFirstname,
                binding.fieldLastname, binding.fieldJobtitle, binding.fieldOrganisation};


        boolean valid = true;
        for (ValidableTextInputLayout field : fields){
            valid = field.validate() && valid;
        }

        //If the rest of email validations passed, check that the email is valid
        if (binding.fieldEmail.validate() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.fieldEmail.setErrorEnabled(true);
            binding.fieldEmail.setError(getString(R.string.error_register_email));
            valid = false;
        }


        if (valid){
            user.setFirstname(firstname);
            user.setLastname(lastname);
            user.setEmail(email);
            user.setJobTitle(jobTitle);
            user.setOrganisation(organisation);
            user.setUserCustomFields(fieldsManager.getCustomFieldValues());
            executeUpdateProfileTask(user);
        }
    }

    private void executeUpdateProfileTask(final User user) {
        Payload p = new Payload(Arrays.asList(user));
        UpdateProfileTask task = new UpdateProfileTask(this, apiEndpoint);
        task.setResponseListener(this);
        task.execute(p);
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
