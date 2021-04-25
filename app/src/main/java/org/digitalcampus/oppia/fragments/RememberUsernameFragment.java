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
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentRememberUsernameBinding;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.listener.SubmitEntityListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.RememberUsernameTask;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

public class RememberUsernameFragment extends AppFragment implements SubmitEntityListener<User> {

    @Inject
    ApiEndpoint apiEndpoint;
    private FragmentRememberUsernameBinding binding;

    public static RememberUsernameFragment newInstance() {
        return new RememberUsernameFragment();
    }

    public RememberUsernameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRememberUsernameBinding.inflate(LayoutInflater.from(getActivity()), container, false);
        getAppComponent().inject(this);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.btnRememberUsername.setOnClickListener(v -> onRememberClick());
    }

    public void submitComplete(EntityResult<User> result) {

        if (getActivity() == null) {
            return;
        }

        hideProgressDialog();

        if (result.isSuccess()) {
            UIUtils.showAlert(super.getActivity(), R.string.tab_title_remember_username, result.getResultMessage());
        } else {

            try {
                JSONObject jo = new JSONObject(result.getResultMessage());
                UIUtils.showAlert(super.getActivity(), R.string.error, jo.getString("error"));
            } catch (JSONException je) {
                UIUtils.showAlert(super.getActivity(), R.string.error, result.getResultMessage());
            }

        }
    }

    public void onRememberClick() {
        // get form fields
        String email = binding.editEmail.getText().toString();

        // do validation
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            UIUtils.showAlert(super.getActivity(), R.string.error, R.string.error_register_email);
            return;
        }

        showProgressDialog(getString(R.string.processing));

        User user = new User();
        user.setEmail(email);
        RememberUsernameTask rt = new RememberUsernameTask(super.getActivity(), apiEndpoint);
        rt.setListener(this);
        rt.execute(user);
    }
}
