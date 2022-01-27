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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentAboutBinding;
import org.digitalcampus.mobile.learning.databinding.FragmentLoginBinding;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.ViewDigestActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.SubmitEntityListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.LoginTask;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.UIUtils;

import javax.inject.Inject;

public class LoginFragment extends AppFragment implements SubmitEntityListener<User> {

    @Inject
    ApiEndpoint apiEndpoint;

    private FragmentLoginBinding binding;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAppComponent().inject(this);

        binding.loginBtn.setOnClickListener(v -> onLoginClick());

        binding.btnResetPassword.setOnClickListener(v -> {
            WelcomeActivity wa = (WelcomeActivity) getActivity();
            wa.switchTab(WelcomeActivity.TAB_RESET_PASSWORD);
        });
        binding.actionRegisterBtn.setOnClickListener(v -> {
            WelcomeActivity wa = (WelcomeActivity) getActivity();
            wa.switchTab(WelcomeActivity.TAB_REGISTER);
        });

        binding.btnRememberUsername.setOnClickListener(v -> {
            WelcomeActivity wa = (WelcomeActivity) getActivity();
            wa.switchTab(WelcomeActivity.TAB_REMEMBER_USERNAME);
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        hideProgressDialog();
    }

    protected void onLoginClick() {
        String username = binding.loginUsernameField.getText().toString();
        //check valid email address format
        if (username.length() == 0) {
            UIUtils.showAlert(super.getActivity(), R.string.error, R.string.error_no_username);
            return;
        }

        String password = binding.loginPasswordField.getText().toString();

        showProgressDialog(getString(R.string.login_process));

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        LoginTask lt = new LoginTask(super.getActivity(), apiEndpoint);
        lt.setLoginListener(this);
        lt.execute(user);
    }


    public void submitComplete(EntityResult<User> response) {
        hideProgressDialog();

        if (response.isSuccess()) {
            User user = response.getEntity();
            ((WelcomeActivity) getActivity()).onSuccessUserAccess(user);

        } else {
            Context ctx = super.getActivity();
            if (ctx != null) {
                UIUtils.showAlert(ctx, R.string.title_login, response.getResultMessage());
            }
        }
    }
}
