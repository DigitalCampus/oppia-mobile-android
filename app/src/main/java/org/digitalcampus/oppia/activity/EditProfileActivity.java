package org.digitalcampus.oppia.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityEditProfileBinding;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomFieldsRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.UpdateProfileTask;
import org.digitalcampus.oppia.utils.ui.fields.CustomFieldsUIManager;
import org.digitalcampus.oppia.utils.ui.fields.ValidableField;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

public class EditProfileActivity extends AppActivity implements View.OnClickListener, UpdateProfileTask.ResponseListener {

    private ActivityEditProfileBinding binding;
    private CustomFieldsUIManager fieldsManager;
    private HashMap<String, ValidableField> fields = new HashMap<>();

    @Inject
    ApiEndpoint apiEndpoint;

    @Inject
    User user;

    @Inject
    CustomFieldsRepository customFieldsRepo;

    List<CustomField> profileCustomFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSaveProfile.setOnClickListener(this);

        getAppComponent().inject(this);

        fields = new HashMap<>();
        fields.put("email", binding.fieldEmail);
        fields.put("first_name", binding.fieldFirstname);
        fields.put("last_name", binding.fieldLastname);
        fields.put("jobtitle", binding.fieldJobtitle);
        fields.put("organisation", binding.fieldOrganisation);
        fields.put("phoneno", binding.fieldPhoneno);

        profileCustomFields = customFieldsRepo.getAll(this);
        fieldsManager = new CustomFieldsUIManager(this, fields, profileCustomFields);
        fieldsManager.populateAndInitializeFields(binding.customFieldsContainer);

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
        binding.fieldPhoneno.setText(user.getPhoneNo());
        fieldsManager.fillWithUserData(user);

        binding.fieldPhoneno.setCustomValidator(field -> {
            String phoneNo = field.getCleanedValue();
            if ((phoneNo.length() > 0) && (phoneNo.length() < App.PHONENO_MIN_LENGTH)) {
                binding.fieldPhoneno.setErrorEnabled(true);
                binding.fieldPhoneno.setError(getString(R.string.error_register_no_phoneno));
                binding.fieldPhoneno.requestFocus();
                return false;
            }
            return true;
        });

        binding.fieldEmail.setCustomValidator(field -> {
            String email = field.getCleanedValue();
            if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.fieldEmail.setErrorEnabled(true);
                binding.fieldEmail.setError(getString(R.string.error_register_email));
                return false;
            }
            return true;
        });

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_save_profile) {
            getDataAndSend();
        }
    }

    private void getDataAndSend() {

        String email = binding.fieldEmail.getCleanedValue();
        String firstname = binding.fieldFirstname.getCleanedValue();
        String lastname = binding.fieldLastname.getCleanedValue();
        String jobTitle = binding.fieldJobtitle.getCleanedValue();
        String organisation = binding.fieldOrganisation.getCleanedValue();
        String phoneNo = binding.fieldPhoneno.getCleanedValue();

        boolean valid = true;
        for (ValidableField field : fields.values()){
            valid = field.validate() && valid;
        }
        valid = fieldsManager.validateFields() && valid;

        if (valid){
            user.setFirstname(firstname);
            user.setLastname(lastname);
            user.setEmail(email);
            user.setJobTitle(jobTitle);
            user.setOrganisation(organisation);
            user.setPhoneNo(phoneNo);
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
