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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.fragments.AppFragment;
import org.digitalcampus.oppia.gamification.GamificationEngine;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.RegisterTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class RegisterMainFragment extends AppFragment implements SubmitListener, RegisterTask.RegisterListener, View.OnClickListener {


    private LinearLayout viewRegisterType;
    private AppCompatButton btnRegCha;
    private AppCompatButton btnRegChss;
    private AppCompatButton btnRegOther;

    private void findViews(View layout) {
        viewRegisterType = layout.findViewById(R.id.view_register_type);
        btnRegCha = layout.findViewById(R.id.btn_reg_cha);
        btnRegChss = layout.findViewById(R.id.btn_reg_chss);
        btnRegOther = layout.findViewById(R.id.btn_reg_other);

        registerButton = layout.findViewById(R.id.register_btn);
        loginButton = layout.findViewById(R.id.action_login_btn);

        btnRegCha.setOnClickListener(this);
        btnRegChss.setOnClickListener(this);
        btnRegOther.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_reg_cha:
                setFragment(new RegisterCHFragment());
                break;

            case R.id.btn_reg_chss:
                setFragment(new RegisterCHFragment());
                break;

            case R.id.btn_reg_other:
                setFragment(new RegisterOtherFragment());
                registerButton.setVisibility(View.VISIBLE);
                break;
        }


        viewRegisterType.setVisibility(View.GONE);
    }

    private void setFragment(RegisterBaseFragment fragment) {
        getChildFragmentManager().beginTransaction().replace(R.id.frame_register_main, fragment).addToBackStack(null).commit();
    }


    private Button registerButton;
    private Button loginButton;
    private ProgressDialog pDialog;

    public static RegisterMainFragment newInstance() {
        return new RegisterMainFragment();
    }

    public RegisterMainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_register_main, container, false);

        findViews(layout);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                onRegisterClick();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                WelcomeActivity wa = (WelcomeActivity) getActivity();
                wa.switchTab(WelcomeActivity.TAB_LOGIN);
            }
        });

    }

    public void submitComplete(Payload response) {
        pDialog.dismiss();
        if (response.isResult()) {
            User user = (User) response.getData().get(0);
            SessionManager.loginUser(getActivity(), user);
            // registration gamification
            GamificationEngine gamificationEngine = new GamificationEngine(super.getActivity());
            gamificationEngine.processEventRegister();
            //Save the search tracker
            new Tracker(super.getActivity()).saveRegisterTracker();
            startActivity(new Intent(getActivity(), MainActivity.class));
            super.getActivity().finish();
        } else {
            Context ctx = super.getActivity();
            if (ctx != null) {
                try {
                    JSONObject jo = new JSONObject(response.getResultResponse());
                    UIUtils.showAlert(ctx, R.string.error, jo.getString("error"));
                } catch (JSONException je) {
                    UIUtils.showAlert(ctx, R.string.error, response.getResultResponse());
                }
            }
        }
    }


    public void onRegisterClick() {

        RegisterBaseFragment registerBaseFragment = (RegisterBaseFragment)
                getChildFragmentManager().findFragmentById(R.id.frame_register_main);
        User user = registerBaseFragment.getUser();

        if (user != null) {
            executeRegisterTask(user);
        }

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
        startActivity(new Intent(getActivity(), MainActivity.class));
        super.getActivity().finish();
    }

    @Override
    public void onSubmitError(String error) {
        pDialog.dismiss();
        Context ctx = super.getActivity();
        if (ctx != null) {
            UIUtils.showAlert(getActivity(), R.string.error, error);
        }
    }

    @Override
    public void onConnectionError(String error, final User u) {
        pDialog.dismiss();
        Context ctx = super.getActivity();
        if (ctx == null) {
            return;
        }
        if (BuildConfig.OFFLINE_REGISTER_ENABLED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setCancelable(false);
            builder.setTitle(error);
            builder.setMessage(R.string.offline_register_confirm);
            builder.setPositiveButton(R.string.register_offline, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    u.setOfflineRegister(true);
                    executeRegisterTask(u);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
        } else {
            UIUtils.showAlert(ctx, R.string.error, error);
        }
    }

    private void executeRegisterTask(User u) {

        pDialog = new ProgressDialog(super.getActivity());
        pDialog.setTitle(R.string.register_alert_title);
        pDialog.setMessage(getString(R.string.register_process));
        pDialog.setCancelable(true);
        pDialog.show();

        Payload p = new Payload(Arrays.asList(u));
        RegisterTask rt = new RegisterTask(super.getActivity());
        rt.setRegisterListener(this);
        rt.execute(p);
    }

    public boolean goBack() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            viewRegisterType.setVisibility(View.VISIBLE);
            return true;
        }

        return false;
    }

    public void showRegisterButton(boolean show) {
        registerButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
