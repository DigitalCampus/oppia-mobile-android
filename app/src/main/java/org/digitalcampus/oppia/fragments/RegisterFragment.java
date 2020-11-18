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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.badoualy.stepperindicator.StepperIndicator;

import androidx.appcompat.app.AlertDialog;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
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


	private ValidableTextInputLayout usernameField;
	private ValidableTextInputLayout emailField;
	private ValidableTextInputLayout passwordField;
	private ValidableTextInputLayout passwordAgainField;
	private ValidableTextInputLayout firstnameField;
	private ValidableTextInputLayout lastnameField;
	private ValidableTextInputLayout jobTitleField;
	private ValidableTextInputLayout organisationField;
	private ValidableTextInputLayout phoneNoField;
	private HashMap<String, ValidableField> fields = new HashMap<>();

	private LinearLayout customFieldsContainer;
	private List<CustomField> profileCustomFields;

	private LinearLayout steppedFieldsContainer;
	private CustomFieldsUIManager fieldsManager;
	private StepperIndicator stepperIndicator;
	private SteppedFormUIManager stepsManager;
	private TextView stepDescription;

	private Button registerButton;
	private Button nextStepButton;
	private Button prevStepButton;
	private Button loginButton;
	private ProgressDialog pDialog;
	private View stepperContainer;
	private View loginContainer;


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
		View layout = inflater.inflate(R.layout.fragment_register, container, false);
		usernameField = layout.findViewById(R.id.register_form_username_field);
		emailField = layout.findViewById(R.id.register_form_email_field);
		passwordField = layout.findViewById(R.id.register_form_password_field);
		passwordAgainField = layout.findViewById(R.id.register_form_password_again_field);
		firstnameField = layout.findViewById(R.id.register_form_firstname_field);
		lastnameField = layout.findViewById(R.id.register_form_lastname_field);
		jobTitleField = layout.findViewById(R.id.register_form_jobtitle_field);
		organisationField = layout.findViewById(R.id.register_form_organisation_field);
		phoneNoField = layout.findViewById(R.id.register_form_phoneno_field);
		registerButton = layout.findViewById(R.id.register_btn);
		loginButton = layout.findViewById(R.id.action_login_btn);
		customFieldsContainer = layout.findViewById(R.id.custom_fields_container);
		stepperIndicator = layout.findViewById(R.id.stepper_indicator);
		stepperContainer = layout.findViewById(R.id.frame_stepper_indicator);
		prevStepButton = layout.findViewById(R.id.prev_btn);
		nextStepButton = layout.findViewById(R.id.next_btn);
		loginContainer = layout.findViewById(R.id.login_container);
		stepDescription = layout.findViewById(R.id.step_description);
		steppedFieldsContainer = layout.findViewById(R.id.stepped_fields_container);

		emailField.setCustomValidator(field -> {
			String email = field.getCleanedValue();
			if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
				emailField.setErrorEnabled(true);
				emailField.setError(getString(R.string.error_register_email));
				return false;
			}
			return true;
		});

		phoneNoField.setCustomValidator(field -> {
			String phoneNo = field.getCleanedValue();
			if ((phoneNo.length() > 0) && (phoneNo.length() < App.PHONENO_MIN_LENGTH)) {
				phoneNoField.setErrorEnabled(true);
				phoneNoField.setError(getString(R.string.error_register_no_phoneno));
				phoneNoField.requestFocus();
				return false;
			}
			return true;
		});

		passwordField.setCustomValidator(field -> {
			String password = passwordField.getCleanedValue();
			String passwordAgain = passwordAgainField.getCleanedValue();
			return checkPasswordCriteria(password, passwordAgain);
		});

		usernameField.setCustomValidator(field -> {
			boolean validValue = !TextUtils.isEmpty(field.getCleanedValue()) && field.getCleanedValue().length() >= App.USERNAME_MIN_CHARACTERS;
			if (!validValue) {
				usernameField.setError(getString(R.string.error_register_username_lenght, App.USERNAME_MIN_CHARACTERS));
			}
			return validValue;
		});

		fields = new HashMap<>();
		fields.put("username", usernameField);
		fields.put("email", emailField);
		fields.put("password", passwordField);
		fields.put("passwordagain", passwordAgainField);
		fields.put("first_name", firstnameField);
		fields.put("last_name", lastnameField);
		fields.put("jobtitle", jobTitleField);
		fields.put("organisation", organisationField);
		fields.put("phoneno", phoneNoField);

		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getAppComponent().inject(this);

		profileCustomFields = customFieldsRepo.getAll(getContext());
		List<CustomField.RegisterFormStep> registerSteps = customFieldsRepo.getRegisterSteps(getContext());
		fieldsManager = new CustomFieldsUIManager(this.getActivity(), fields, profileCustomFields);
		fieldsManager.populateAndInitializeFields(customFieldsContainer);
		if (registerSteps == null || registerSteps.isEmpty()){
			stepperContainer.setVisibility(View.GONE);
			loginContainer.setVisibility(View.VISIBLE);
		}
		else{
			stepsManager = new SteppedFormUIManager(stepperIndicator, registerSteps, fieldsManager);
			stepperContainer.setVisibility(View.VISIBLE);
			loginContainer.setVisibility(View.GONE);
			registerButton.setVisibility(View.GONE);
			nextStepButton.setVisibility(View.VISIBLE);
			prevStepButton.setVisibility(View.INVISIBLE);
			stepsManager.initialize(customFieldsContainer, steppedFieldsContainer, stepDescription);
		}

		registerButton.setOnClickListener(v -> onRegisterClick());
		nextStepButton.setOnClickListener(v -> nextStep());
		prevStepButton.setOnClickListener(v -> prevStep());
		loginButton.setOnClickListener(v -> {
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
			prevStepButton.setVisibility(View.VISIBLE);
		}
		if (stepsManager.isLastStep()){
			nextStepButton.setVisibility(View.GONE);
			registerButton.setVisibility(View.VISIBLE);
		}

	}

	private void prevStep(){
		if (stepsManager.prevStep()){
			prevStepButton.setVisibility(View.INVISIBLE);
		}
		registerButton.setVisibility(View.GONE);
		nextStepButton.setVisibility(View.VISIBLE);
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
            u.setUsername(usernameField.getCleanedValue());
            u.setPassword(passwordField.getCleanedValue());
            u.setPasswordAgain(passwordAgainField.getCleanedValue());
            u.setFirstname(firstnameField.getCleanedValue());
            u.setLastname(lastnameField.getCleanedValue());
            u.setEmail(emailField.getCleanedValue());
            u.setJobTitle(jobTitleField.getCleanedValue());
            u.setOrganisation(organisationField.getCleanedValue());
            u.setPhoneNo(phoneNoField.getCleanedValue());
			u.setUserCustomFields(fieldsManager.getCustomFieldValues());
            executeRegisterTask(u);
        }

	}

	private boolean checkPasswordCriteria(String password, String passwordAgain){
		if (password.length() < App.PASSWORD_MIN_LENGTH) {
			passwordField.setErrorEnabled(true);
			passwordField.setError(getString(R.string.error_register_password,  App.PASSWORD_MIN_LENGTH ));
			return false;
		}
		else if (!password.equals(passwordAgain)) {
			passwordField.setErrorEnabled(true);
			passwordField.setError(getString(R.string.error_register_password_no_match ));
			return false;
		}
		return true;
	}

	@Override
	public void onSubmitComplete(User registeredUser) {
		pDialog.dismiss();
		SessionManager.loginUser(getActivity(), registeredUser);
		// registration gamification
		GamificationEngine gamificationEngine = new GamificationEngine(super.getActivity());
		gamificationEngine.processEventRegister();

		//Save the search tracker
		new Tracker(super.getActivity()).saveRegisterTracker();

		boolean fromViewDigest = getActivity().getIntent().getBooleanExtra(ViewDigestActivity.EXTRA_FROM_VIEW_DIGEST, false);

		if (fromViewDigest) {
			getActivity().setResult(Activity.RESULT_OK);
		} else {
			startActivity(new Intent(getActivity(), MainActivity.class));
		}

		super.getActivity().finish();
	}

	@Override
	public void onSubmitError(String error) {
		pDialog.dismiss();
		Context ctx = super.getActivity();
		if (ctx != null){
			UIUtils.showAlert(getActivity(), R.string.error, error);
		}
	}

	@Override
	public void onConnectionError(String error, final User u) {
		pDialog.dismiss();
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

	private void executeRegisterTask(User u){

		pDialog = new ProgressDialog(super.getActivity());
		pDialog.setTitle(R.string.register_alert_title);
		pDialog.setMessage(getString(R.string.register_process));
		pDialog.setCancelable(true);
		pDialog.show();

		Payload p = new Payload(Arrays.asList(u));
		RegisterTask rt = new RegisterTask(super.getActivity(), apiEndpoint);
		rt.setRegisterListener(this);
		rt.execute(p);
	}
}
