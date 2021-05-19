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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentAboutBinding;
import org.digitalcampus.mobile.learning.databinding.FragmentResetPasswordBinding;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.listener.SubmitEntityListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.ResetPasswordTask;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import javax.inject.Inject;

public class ResetPasswordFragment extends AppFragment implements SubmitEntityListener<User> {

    @Inject
    ApiEndpoint apiEndpoint;
    private FragmentResetPasswordBinding binding;

    public static ResetPasswordFragment newInstance() {
        return new ResetPasswordFragment();
    }

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false);
        getAppComponent().inject(this);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.resetBtn.setOnClickListener(v -> onResetClick());
    }

    public void submitComplete(EntityResult<User> result) {

        if (getActivity() == null) {
            return;
        }

        hideProgressDialog();

        if (result.isSuccess()) {
            UIUtils.showAlert(getActivity(), getString(R.string.reset_password), result.getResultMessage(),
                    getString(R.string.ok), () -> {
                        UIUtils.hideSoftKeyboard(getActivity());
                        binding.resetUsernameField.setText("");
                        WelcomeActivity wa = (WelcomeActivity) getActivity();
                        wa.switchTab(WelcomeActivity.TAB_LOGIN);
                        return null;
                    });
        } else {
            try {
                JSONObject jo = new JSONObject(result.getResultMessage());
                UIUtils.showAlert(super.getActivity(), R.string.error, jo.getString("error"));
            } catch (JSONException je) {
                UIUtils.showAlert(super.getActivity(), R.string.error, result.getResultMessage());
            }

        }
    }

    public void onResetClick() {
        // get form fields
        String username = binding.resetUsernameField.getText().toString();

        // do validation
        // check firstname
        if (username.length() == 0) {
            UIUtils.showAlert(super.getActivity(), R.string.error, R.string.error_register_no_username);
            return;
        }

        showProgressDialog(getString(R.string.reset_process));

        User user = new User();
        user.setUsername(username);
        ResetPasswordTask rt = new ResetPasswordTask(super.getActivity(), apiEndpoint);
        rt.setResetListener(this);
        rt.execute(user);
    }
}
