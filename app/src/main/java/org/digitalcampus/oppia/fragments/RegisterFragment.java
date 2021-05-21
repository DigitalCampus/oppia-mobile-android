/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.badoualy.stepperindicator.StepperIndicator;
import com.hbb20.CountryCodePicker;

import androidx.appcompat.app.AlertDialog;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentAboutBinding;
import org.digitalcampus.mobile.learning.databinding.FragmentRegisterBinding;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.ViewDigestActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.gamification.GamificationEngine;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomFieldsRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.RegisterTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.ui.fields.CustomFieldsUIManager;
import org.digitalcampus.oppia.utils.ui.fields.SteppedFormUIManager;
import org.digitalcampus.oppia.utils.ui.fields.ValidableField;
import org.digitalcampus.oppia.utils.ui.fields.ValidableTextInputLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

public class RegisterFragment extends AppFragment implements RegisterTask.RegisterListener {


	private HashMap<String, ValidableField> fields = new HashMap<>();

	private List<CustomField> profileCustomFields;

	private CustomFieldsUIManager fieldsManager;
	private SteppedFormUIManager stepsManager;

	private FragmentRegisterBinding binding;


	@Inject
	CustomFieldsRepository customFieldsRepo;

	@Inject
	ApiEndpoint apiEndpoint;


	public static RegisterFragment newInstance() {
	    return new RegisterFragment();
	}

	public RegisterFragment(){
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentRegisterBinding.inflate(inflater, container, false);

		binding.registerFormEmailField.setCustomValidator(field -> {
			String email = field.getCleanedValue();
			if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
				binding.registerFormEmailField.setErrorEnabled(true);
				binding.registerFormEmailField.setError(getString(R.string.error_register_email));
				return false;
			}
			return true;
		});

		binding.ccp.registerCarrierNumberEditText(binding.registerFormPhonenoEdittext);
		View phoneInput = binding.registerFormPhonenoField.getChildAt(0);
		binding.registerFormPhonenoField.removeView(phoneInput);
		phoneInput.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		binding.fieldPhonenoContainer.addView(phoneInput);
		binding.registerFormPhonenoField.setCustomValidator(field -> {
			String phoneNo = field.getCleanedValue();
			if ((phoneNo.length() > 0) && !binding.ccp.isValidFullNumber()){
				binding.registerFormPhonenoField.setErrorEnabled(true);
				binding.registerFormPhonenoField.setError(getString(R.string.error_register_no_phoneno));
				binding.registerFormPhonenoField.requestFocus();
				return false;
			}
			return true;
		});

		binding.registerFormPasswordField.setCustomValidator(field -> {
			String password = binding.registerFormPasswordField.getCleanedValue();
			String passwordAgain = binding.registerFormPasswordAgainField.getCleanedValue();
			return checkPasswordCriteria(password, passwordAgain);
		});

		binding.registerFormUsernameField.setCustomValidator(field -> {
			boolean validValue = !TextUtils.isEmpty(field.getCleanedValue()) && field.getCleanedValue().length() >= App.USERNAME_MIN_CHARACTERS;
			if (!validValue) {
				binding.registerFormUsernameField.setError(getString(R.string.error_register_username_length, App.USERNAME_MIN_CHARACTERS));
			}
			return validValue;
		});

		fields = new HashMap<>();
		fields.put("username", binding.registerFormUsernameField);
		fields.put("email", binding.registerFormEmailField);
		fields.put("password", binding.registerFormPasswordField);
		fields.put("passwordagain", binding.registerFormPasswordAgainField);
		fields.put("first_name", binding.registerFormFirstnameField);
		fields.put("last_name", binding.registerFormLastnameField);
		fields.put("jobtitle", binding.registerFormJobtitleField);
		fields.put("organisation", binding.registerFormOrganisationField);
		fields.put("phoneno", binding.registerFormPhonenoField);

		return binding.getRoot();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getAppComponent().inject(this);

		List<String> requiredFields = customFieldsRepo.getRequiredFields(getContext());
		for (String f : requiredFields){
			ValidableField field = fields.get(f);
			if (field != null){
				field.setRequired(true);
			}
		}

		profileCustomFields = customFieldsRepo.getAll(getContext());
		List<CustomField.RegisterFormStep> registerSteps = customFieldsRepo.getRegisterSteps(getContext());
		fieldsManager = new CustomFieldsUIManager(this.getActivity(), fields, profileCustomFields);
		fieldsManager.populateAndInitializeFields(binding.customFieldsContainer);
		if (registerSteps == null || registerSteps.isEmpty()){
			binding.frameStepperIndicator.setVisibility(View.GONE);
			binding.loginContainer.setVisibility(View.VISIBLE);
		}
		else{
			stepsManager = new SteppedFormUIManager(binding.stepperIndicator, registerSteps, fieldsManager);
			binding.frameStepperIndicator.setVisibility(View.VISIBLE);
			binding.loginContainer.setVisibility(View.GONE);
			binding.registerBtn.setVisibility(View.GONE);
			binding.nextBtn.setVisibility(View.VISIBLE);
			binding.prevBtn.setVisibility(View.INVISIBLE);
			stepsManager.initialize(binding.customFieldsContainer, binding.steppedFieldsContainer, binding.stepDescription);
		}

		binding.registerBtn.setOnClickListener(v -> onRegisterClick());
		binding.nextBtn.setOnClickListener(v -> nextStep());
		binding.prevBtn.setOnClickListener(v -> prevStep());
		binding.actionLoginBtn.setOnClickListener(v -> {
			WelcomeActivity activity = (WelcomeActivity) RegisterFragment.super.getActivity();
			if (activity != null){
				activity.switchTab(WelcomeActivity.TAB_LOGIN);
			}

		});
		for (ValidableField field : fields.values()){
			field.initialize();
		}
	}

	private void nextStep(){
		if (stepsManager.nextStep()){
			binding.prevBtn.setVisibility(View.VISIBLE);
		}
		if (stepsManager.isLastStep()){
			binding.nextBtn.setVisibility(View.GONE);
			binding.registerBtn.setVisibility(View.VISIBLE);
		}

	}

	private void prevStep(){
		if (stepsManager.prevStep()){
			binding.prevBtn.setVisibility(View.INVISIBLE);
		}
		binding.registerBtn.setVisibility(View.GONE);
		binding.nextBtn.setVisibility(View.VISIBLE);
	}


	public void onRegisterClick() {

		boolean valid = true;
		if (stepsManager == null){
			// Only validate fields if we are not in a stepped form
			// (if the form was stepped, we already validated each conditional step)
			for (ValidableField field : fields.values()){
				valid = field.validate() && valid;
			}
			valid = fieldsManager.validateFields() && valid;
		}
		else{
			valid = stepsManager.validate();
		}

		if (valid){
            User u = new User();
            u.setUsername(binding.registerFormUsernameField.getCleanedValue());
            u.setPassword(binding.registerFormPasswordField.getCleanedValue());
            u.setPasswordAgain(binding.registerFormPasswordAgainField.getCleanedValue());
            u.setFirstname(binding.registerFormFirstnameField.getCleanedValue());
            u.setLastname(binding.registerFormLastnameField.getCleanedValue());
            u.setEmail(binding.registerFormEmailField.getCleanedValue());
            u.setJobTitle(binding.registerFormJobtitleField.getCleanedValue());
            u.setOrganisation(binding.registerFormOrganisationField.getCleanedValue());
            u.setPhoneNo(binding.ccp.getFormattedFullNumber());
			u.setUserCustomFields(fieldsManager.getCustomFieldValues());
            executeRegisterTask(u);
        }

	}

	private boolean checkPasswordCriteria(String password, String passwordAgain){
		if (password.length() < App.PASSWORD_MIN_LENGTH) {
			binding.registerFormPasswordField.setErrorEnabled(true);
			binding.registerFormPasswordField.setError(getString(R.string.error_register_password,  App.PASSWORD_MIN_LENGTH ));
			return false;
		}
		else if (!password.equals(passwordAgain)) {
			binding.registerFormPasswordField.setErrorEnabled(true);
			binding.registerFormPasswordField.setError(getString(R.string.error_register_password_no_match ));
			return false;
		}
		return true;
	}

	@Override
	public void onSubmitComplete(User registeredUser) {
		hideProgressDialog();

		// registration gamification
		GamificationEngine gamificationEngine = new GamificationEngine(super.getActivity());
		gamificationEngine.processEventRegister();

		//Save the search tracker
		new Tracker(super.getActivity()).saveRegisterTracker();

		((WelcomeActivity) getActivity()).onSuccessUserAccess(registeredUser);

	}

	@Override
	public void onSubmitError(String error) {
		hideProgressDialog();
		Context ctx = super.getActivity();
		if (ctx != null){
			UIUtils.showAlert(getActivity(), R.string.error, error);
		}
	}

	@Override
	public void onConnectionError(String error, final User u) {
		hideProgressDialog();
		Context ctx = super.getActivity();
		if (ctx == null){
			return;
		}
		if (BuildConfig.OFFLINE_REGISTER_ENABLED){
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setCancelable(false);
			builder.setTitle(error);
			builder.setMessage(R.string.offline_register_confirm);
			builder.setPositiveButton(R.string.register_offline, (dialog, which) -> {
				u.setOfflineRegister(true);
				executeRegisterTask(u);
			});
			builder.setNegativeButton(R.string.cancel, null);
			builder.show();
		}
		else{
			UIUtils.showAlert(ctx,R.string.error,error);
		}
	}

	private void executeRegisterTask(User user){

		showProgressDialog(getString(R.string.register_process));

		RegisterTask rt = new RegisterTask(super.getActivity(), apiEndpoint);
		rt.setRegisterListener(this);
		rt.execute(user);
	}
}
