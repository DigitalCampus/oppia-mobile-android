package org.digitalcampus.oppia.fragments.register;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;

import com.badoualy.stepperindicator.StepperIndicator;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.fragments.AppFragment;
import org.digitalcampus.oppia.model.County;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.District;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.ui.fields.ValidableTextInputLayout;

import java.util.ArrayList;
import java.util.List;


public class RegisterCHFragment extends AppFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String ARG_ROLE = "arg_role";
    private int currentScreen = 0;

    private TextView tvExplanationRegScreen;
    private LinearLayout viewRegChScreen0;
    private ValidableTextInputLayout editRegChFirstName;
    private ValidableTextInputLayout editRegChLastName;
    private ValidableTextInputLayout editRegChEmployeeId;
    private ValidableTextInputLayout editRegYearStarted;
    private LinearLayout viewRegChScreen1;
    private ValidableTextInputLayout editRegChPassword;
    private ValidableTextInputLayout editRegChPasswordAgain;
    private LinearLayout viewRegChScreen2;
    private AppCompatSpinner spinnerCounties;
    private AppCompatSpinner spinnerDistricts;
    private AppCompatSpinner spinnerGender;
    private TextView btnRegisterChPrevious;
    private TextView btnRegisterChNext;
    private List<County> counties = new ArrayList<>();
    private List<District> districts = new ArrayList<>();
    private List<CustomField.CollectionItem> genders = new ArrayList<>();
    private ArrayAdapter<District> adapterDistricts;
    private StepperIndicator stepperIndicator;
    private AppCompatButton btnRegisterPerform;
    private String role;

    private void findViews(View layout) {
        tvExplanationRegScreen = layout.findViewById(R.id.tv_explanation_reg_screen);
        viewRegChScreen0 = layout.findViewById(R.id.view_reg_ch_screen_0);
        editRegChFirstName = layout.findViewById(R.id.edit_reg_ch_first_name);
        editRegChLastName = layout.findViewById(R.id.edit_reg_ch_last_name);
        editRegChEmployeeId = layout.findViewById(R.id.edit_reg_ch_employee_id);
        editRegYearStarted = layout.findViewById(R.id.edit_reg_year_started);
        viewRegChScreen1 = layout.findViewById(R.id.view_reg_ch_screen_1);
        editRegChPassword = layout.findViewById(R.id.edit_reg_ch_password);
        editRegChPasswordAgain = layout.findViewById(R.id.edit_reg_ch_password_again);
        viewRegChScreen2 = layout.findViewById(R.id.view_reg_ch_screen_2);
        spinnerCounties = layout.findViewById(R.id.spinner_counties);
        spinnerDistricts = layout.findViewById(R.id.spinner_districts);
        spinnerGender = layout.findViewById(R.id.spinner_gender);
        btnRegisterChPrevious = layout.findViewById(R.id.btn_register_ch_previous);
        stepperIndicator = layout.findViewById(R.id.stepper_indicator);
        btnRegisterChNext = layout.findViewById(R.id.btn_register_ch_next);
        btnRegisterPerform = layout.findViewById(R.id.btn_register_perform);

        btnRegisterChPrevious.setOnClickListener(this);
        btnRegisterChNext.setOnClickListener(this);
        btnRegisterPerform.setOnClickListener(this);
    }

    public static RegisterCHFragment getInstance(String role) {
        RegisterCHFragment fragment = new RegisterCHFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROLE, role);
        fragment.setArguments(args);
        return fragment;
    }

    public RegisterCHFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_register_ch, container, false);
        findViews(layout);

        role = getArguments().getString(ARG_ROLE);
        if (role == null) {
            throw new IllegalArgumentException("Role argument missing");
        }

        configureCounties();
        updateScreen();

        return layout;
    }

    private void configureCounties() {

        counties.clear();
        counties.addAll(County.geCountries(this.getContext()));
        counties.add(0, new County(getString(R.string.select_county)));

        ArrayAdapter<County> adapterCounties = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, counties);
        spinnerCounties.setAdapter(adapterCounties);
        spinnerCounties.setOnItemSelectedListener(this);

        adapterDistricts = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, districts);
        districts.add(0, new District(getString(R.string.select_district)));
        spinnerDistricts.setAdapter(adapterDistricts);
        spinnerDistricts.setEnabled(false);

        genders = DbHelper.getInstance(this.getContext()).getCollection("gender");
        genders.add(0, new CustomField.CollectionItem("", "Select sex..."));
        ArrayAdapter<CustomField.CollectionItem> genderAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, genders);
        spinnerGender.setAdapter(genderAdapter);
    }

    private void updateScreen() {

        viewRegChScreen0.setVisibility(currentScreen == 0 ? View.VISIBLE : View.GONE);
        viewRegChScreen1.setVisibility(currentScreen == 1 ? View.VISIBLE : View.GONE);
        viewRegChScreen2.setVisibility(currentScreen == 2 ? View.VISIBLE : View.GONE);


        View containerView = this.getView();
        if (containerView != null){
            InputMethodManager imm =  (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(containerView.getWindowToken(), 0);
        }


        stepperIndicator.setCurrentStep(currentScreen);

//        btnRegisterChPrevious.setVisibility(currentScreen > 0 ? View.VISIBLE : View.GONE);
        btnRegisterChNext.setVisibility(currentScreen < 2 ? View.VISIBLE : View.GONE);

        showRegisterButton(currentScreen == 2);

        String[] explanations = getResources().getStringArray(R.array.registration_screens_explanations);
        tvExplanationRegScreen.setText(explanations[currentScreen]);


    }

    private void showRegisterButton(boolean show) {
        btnRegisterPerform.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegisterChNext.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_register_ch_previous:
                currentScreen--;
                if (currentScreen == -1) {
                    ((RegisterMainFragment) getParentFragment()).goBack();
                } else {
                    updateScreen();
                }
                break;

            case R.id.btn_register_ch_next:
                if (validateFieldsOfCurrentScreen()) {
                    currentScreen++;
                    updateScreen();
                }
                break;

            case R.id.btn_register_perform:
                if (validateFieldsOfCurrentScreen()) {
                    ((RegisterMainFragment) getParentFragment()).registerUser(getUserData());
                }
                break;
        }

    }

    private User getUserData() {

        User user = new User();
        user.setFirstname(editRegChFirstName.getCleanedValue());
        user.setLastname(editRegChLastName.getCleanedValue());
        user.setEmployeeID(editRegChEmployeeId.getCleanedValue());
        user.autogenerateUsername();
        user.setPassword(editRegChPassword.getCleanedValue());
        user.setPasswordAgain(editRegChPasswordAgain.getCleanedValue());
        user.setRole(role);
        user.setYearStarted(Integer.parseInt(editRegYearStarted.getCleanedValue()));
        if (spinnerGender.getSelectedItemPosition() != 0){
            user.setGender(genders.get(spinnerGender.getSelectedItemPosition()).getKey());
        }
        user.setCounty(counties.get(spinnerCounties.getSelectedItemPosition()).getId());
        user.setDistrict(districts.get(spinnerDistricts.getSelectedItemPosition()).getId());
        return user;

    }


    // Spinner Countries selection
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        spinnerDistricts.setSelection(0);

        if (position == 0) {
            spinnerDistricts.setEnabled(false);
        } else {
            spinnerDistricts.setEnabled(true);
            County county = counties.get(position);

            districts.clear();
            districts.addAll(county.getDistricts());
            districts.add(0, new District(getString(R.string.select_district)));
            adapterDistricts.notifyDataSetChanged();

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private boolean validateFieldsOfCurrentScreen() {

        switch (currentScreen) {
            case 0:
                return editRegChFirstName.validate() && editRegChLastName.validate() && editRegChEmployeeId.validate();

            case 1:
                if (editRegChPassword.validate() && editRegChPasswordAgain.validate()) {

                    String password = editRegChPassword.getCleanedValue();
                    String passwordAgain = editRegChPasswordAgain.getCleanedValue();

                    // check password length
                    if (password.length() < App.PASSWORD_MIN_LENGTH) {
                        editRegChPassword.setErrorEnabled(true);
                        editRegChPassword.setError(getString(R.string.error_register_password, App.PASSWORD_MIN_LENGTH));
                        editRegChPassword.requestFocus();
                        return false;
                    }

                    // check password match
                    if (!password.equals(passwordAgain)) {
                        editRegChPasswordAgain.setErrorEnabled(true);
                        editRegChPasswordAgain.setError(getString(R.string.error_register_password_no_match));
                        editRegChPasswordAgain.requestFocus();
                        return false;
                    }

                    return true;
                }
                break;

            case 2:
                if (spinnerCounties.getSelectedItemPosition() == 0 || spinnerDistricts.getSelectedItemPosition() == 0) {
                    toast(R.string.please_select_county_district);
                } else {
                    return true;
                }
                break;
        }

        return false;
    }


}
