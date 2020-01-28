package org.digitalcampus.oppia.fragments.register;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSpinner;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.ui.ValidableTextInputLayout;


public class RegisterCHFragment extends RegisterBaseFragment implements View.OnClickListener {

    private int currentScreen = 1;

    private TextView tvExplanationRegScreen;
    private LinearLayout viewRegChScreen1;
    private ValidableTextInputLayout editRegChFirstName;
    private ValidableTextInputLayout editRegChLastName;
    private ValidableTextInputLayout editRegChEmployeeId;
    private LinearLayout viewRegChScreen2;
    private ValidableTextInputLayout editRegChPassword;
    private ValidableTextInputLayout editRegChPasswordAgain;
    private LinearLayout viewRegChScreen3;
    private AppCompatSpinner selectorCountry;
    private AppCompatSpinner selectorDistrict;
    private TextView btnRegisterChPrevious;
    private TextView tvRegisterPageIndicator;
    private TextView btnRegisterChNext;

    private void findViews(View layout) {
        tvExplanationRegScreen = layout.findViewById(R.id.tv_explanation_reg_screen);
        viewRegChScreen1 = layout.findViewById(R.id.view_reg_ch_screen_1);
        editRegChFirstName = layout.findViewById(R.id.edit_reg_ch_first_name);
        editRegChLastName = layout.findViewById(R.id.edit_reg_ch_last_name);
        editRegChEmployeeId = layout.findViewById(R.id.edit_reg_ch_employee_id);
        viewRegChScreen2 = layout.findViewById(R.id.view_reg_ch_screen_2);
        editRegChPassword = layout.findViewById(R.id.edit_reg_ch_password);
        editRegChPasswordAgain = layout.findViewById(R.id.edit_reg_ch_password_again);
        viewRegChScreen3 = layout.findViewById(R.id.view_reg_ch_screen_3);
        selectorCountry = layout.findViewById(R.id.selector_country);
        selectorDistrict = layout.findViewById(R.id.selector_district);
        btnRegisterChPrevious = layout.findViewById(R.id.btn_register_ch_previous);
        tvRegisterPageIndicator = layout.findViewById(R.id.tv_register_page_indicator);
        btnRegisterChNext = layout.findViewById(R.id.btn_register_ch_next);

        btnRegisterChPrevious.setOnClickListener(this);
        btnRegisterChNext.setOnClickListener(this);
    }



    public RegisterCHFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_register_ch, container, false);
        findViews(layout);

        updateScreen();

        return layout;
    }

    private void updateScreen() {

        viewRegChScreen1.setVisibility(currentScreen == 1 ? View.VISIBLE : View.GONE);
        viewRegChScreen2.setVisibility(currentScreen == 2 ? View.VISIBLE : View.GONE);
        viewRegChScreen3.setVisibility(currentScreen == 3 ? View.VISIBLE : View.GONE);

        tvExplanationRegScreen.setText("Explanation for REG CH screen: " + currentScreen);

        tvRegisterPageIndicator.setText(currentScreen + " of 3");

        btnRegisterChPrevious.setVisibility(currentScreen > 1 ? View.VISIBLE : View.GONE);
        btnRegisterChNext.setVisibility(currentScreen < 3 ? View.VISIBLE : View.GONE);

        ((RegisterMainFragment) getParentFragment()).showRegisterButton(currentScreen == 3);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_register_ch_previous:
                currentScreen--;
                updateScreen();
                break;

            case R.id.btn_register_ch_next:
                if (validateFieldsOfCurrentScreen()) {
                    currentScreen++;
                    updateScreen();
                }

                break;
        }

    }

    private boolean validateFieldsOfCurrentScreen() {

        switch (currentScreen) {
            case 1:
                return editRegChFirstName.validate() && editRegChLastName.validate() && editRegChEmployeeId.validate();

            case 2:
                if (editRegChPassword.validate() && editRegChPasswordAgain.validate()) {

                    String password = editRegChPassword.getCleanedValue();
                    String passwordAgain = editRegChPasswordAgain.getCleanedValue();

                    // check password length
                    if (password.length() < MobileLearning.PASSWORD_MIN_LENGTH) {
                        editRegChPassword.setErrorEnabled(true);
                        editRegChPassword.setError(getString(R.string.error_register_password,  MobileLearning.PASSWORD_MIN_LENGTH ));
                        editRegChPassword.requestFocus();
                        return false;
                    }

                    // check password match
                    if (!password.equals(passwordAgain)) {
                        editRegChPasswordAgain.setErrorEnabled(true);
                        editRegChPasswordAgain.setError(getString(R.string.error_register_password_no_match ));
                        editRegChPasswordAgain.requestFocus();
                        return false;
                    }

                    return true;
                }
                break;
        }

        return false;
    }

    @Override
    public User getUser() {
        return null;
    }
}
