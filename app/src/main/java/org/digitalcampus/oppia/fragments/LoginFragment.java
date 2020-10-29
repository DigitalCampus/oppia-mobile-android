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
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.ViewDigestActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.LoginTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;

import java.util.ArrayList;

import javax.inject.Inject;

public class LoginFragment extends AppFragment implements SubmitListener {

    private EditText usernameField;
    private EditText passwordField;
    private ProgressDialog pDialog;
    private Context appContext;

    @Inject
    ApiEndpoint apiEndpoint;

    private Button registerBtn;
    private Button loginBtn;
    private Button resetPasswordBtn;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        usernameField = v.findViewById(R.id.login_username_field);
        passwordField = v.findViewById(R.id.login_password_field);
        loginBtn = v.findViewById(R.id.login_btn);
        registerBtn = v.findViewById(R.id.action_register_btn);
        resetPasswordBtn = v.findViewById(R.id.forgot_btn);
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        appContext = super.getActivity().getApplicationContext();
        getAppComponent().inject(this);

        loginBtn.setOnClickListener(v -> onLoginClick());

        resetPasswordBtn.setOnClickListener(v -> {
            WelcomeActivity wa = (WelcomeActivity) LoginFragment.super.getActivity();
            wa.switchTab(WelcomeActivity.TAB_PASSWORD);
        });
        registerBtn.setOnClickListener(v -> {
            WelcomeActivity wa = (WelcomeActivity) LoginFragment.super.getActivity();
            wa.switchTab(WelcomeActivity.TAB_REGISTER);
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    protected void onLoginClick() {
        String username = usernameField.getText().toString();
        //check valid email address format
        if (username.length() == 0) {
            UIUtils.showAlert(super.getActivity(), R.string.error, R.string.error_no_username);
            return;
        }

        String password = passwordField.getText().toString();

        // show progress dialog
        pDialog = new ProgressDialog(super.getActivity());
        pDialog.setTitle(R.string.title_login);
        pDialog.setMessage(this.getString(R.string.login_process));
        pDialog.setCancelable(true);
        pDialog.show();

        ArrayList<Object> users = new ArrayList<>();
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        users.add(u);

        Payload p = new Payload(users);
        LoginTask lt = new LoginTask(super.getActivity(), apiEndpoint);
        lt.setLoginListener(this);
        lt.execute(p);
    }


    public void submitComplete(Payload response) {
        try {
            pDialog.dismiss();
        } catch (IllegalArgumentException iae) {
            //
        }

        boolean fromViewDigest = getActivity().getIntent().getBooleanExtra(ViewDigestActivity.EXTRA_FROM_VIEW_DIGEST, false);

        if (response.isResult()) {
            User user = (User) response.getData().get(0);
            SessionManager.loginUser(appContext, user);

            if (fromViewDigest) {
                getActivity().setResult(Activity.RESULT_OK);
            } else {
                startActivity(new Intent(super.getActivity(), MainActivity.class));
            }

            super.getActivity().finish();

        } else {
            Context ctx = super.getActivity();
            if (ctx != null) {
                UIUtils.showAlert(ctx, R.string.title_login, response.getResultResponse());
            }
        }
    }
}
