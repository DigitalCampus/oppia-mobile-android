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

package org.digitalcampus.oppia.fragments.register;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatButton;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.fragments.AppFragment;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.ui.fields.ValidableTextInputLayout;

import java.util.Arrays;
import java.util.List;

public class RegisterOtherFragment extends AppFragment implements View.OnClickListener {


    private ValidableTextInputLayout usernameField;
    private ValidableTextInputLayout emailField;
    private ValidableTextInputLayout passwordField;
    private ValidableTextInputLayout passwordAgainField;
    private ValidableTextInputLayout firstnameField;
    private ValidableTextInputLayout lastnameField;
    private ValidableTextInputLayout phoneNoField;
    private ValidableTextInputLayout jobTitleField;
    private ValidableTextInputLayout organisationField;
    private List<ValidableTextInputLayout> fields;

    private AppCompatButton btnPrevious;
    private AppCompatButton btnRegisterPerform;

    public static RegisterOtherFragment newInstance() {
        return new RegisterOtherFragment();
    }

    public RegisterOtherFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_register_other, container, false);
        findViews(layout);

        return layout;
    }

    private void findViews(View layout) {

        usernameField = layout.findViewById(R.id.register_form_username_field);
        emailField = layout.findViewById(R.id.register_form_email_field);
        passwordField = layout.findViewById(R.id.register_form_password_field);
        passwordAgainField = layout.findViewById(R.id.register_form_password_again_field);
        firstnameField = layout.findViewById(R.id.register_form_firstname_field);
        lastnameField = layout.findViewById(R.id.register_form_lastname_field);
        phoneNoField = layout.findViewById(R.id.register_form_phoneno_field);
        jobTitleField = layout.findViewById(R.id.register_form_jobtitle_field);
        organisationField = layout.findViewById(R.id.register_form_organisation_field);

        fields = Arrays.asList(usernameField, emailField, passwordField, passwordAgainField,
                firstnameField, lastnameField, jobTitleField, organisationField);

        btnPrevious = layout.findViewById(R.id.btn_register_previous);
        btnRegisterPerform = layout.findViewById(R.id.btn_register_perform);

        btnPrevious.setOnClickListener(this);
        btnRegisterPerform.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        for (ValidableTextInputLayout field : fields) {
            field.initialize();
        }

    }


    public User getUserData() {

        String username = usernameField.getCleanedValue();
        String email = emailField.getCleanedValue();
        String password = passwordField.getCleanedValue();
        String passwordAgain = passwordAgainField.getCleanedValue();
        String firstname = firstnameField.getCleanedValue();
        String lastname = lastnameField.getCleanedValue();
        String phoneNo = phoneNoField.getCleanedValue();
        String jobTitle = jobTitleField.getCleanedValue();
        String organisation = organisationField.getCleanedValue();

        for (ValidableTextInputLayout field : fields) {
            if (!field.validate()) {
                return null;
            }
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setErrorEnabled(true);
            emailField.setError(getString(R.string.error_register_email));
            emailField.requestFocus();
            return null;
        }

        if (!TextUtils.isEmpty(phoneNo) && !Patterns.PHONE.matcher(phoneNo).matches()) {
            phoneNoField.setErrorEnabled(true);
            phoneNoField.setError(getString(R.string.error_register_no_phoneno));
            phoneNoField.requestFocus();
            return null;
        }

        // check password length
        if (password.length() < App.PASSWORD_MIN_LENGTH) {
            passwordField.setErrorEnabled(true);
            passwordField.setError(getString(R.string.error_register_password, App.PASSWORD_MIN_LENGTH));
            passwordField.requestFocus();
            return null;
        }

        // check password match
        if (!password.equals(passwordAgain)) {
            passwordField.setErrorEnabled(true);
            passwordField.setError(getString(R.string.error_register_password_no_match));
            passwordField.requestFocus();
            return null;
        }


        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setPasswordAgain(passwordAgain);
        u.setFirstname(firstname);
        u.setLastname(lastname);
        u.setEmail(email);
        u.setJobTitle(jobTitle);
        u.setOrganisation(organisation);
        u.setPhoneNo(phoneNo);
        return u;

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register_previous:
                ((RegisterMainFragment) getParentFragment()).goBack();
                break;

            case R.id.btn_register_perform:
                User user = getUserData();
                if (user != null) {
                    ((RegisterMainFragment) getParentFragment()).registerUser(user);
                }
                break;
        }
    }
}
