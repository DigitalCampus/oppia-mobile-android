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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentAboutBinding;
import org.digitalcampus.mobile.learning.databinding.FragmentWelcomeBinding;
import org.digitalcampus.oppia.activity.PrivacyActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;

public class WelcomeFragment extends AppFragment {

	private FragmentWelcomeBinding binding;

	public static WelcomeFragment newInstance() {
        return new WelcomeFragment();
	}

	public WelcomeFragment() {
        // do nothing
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentWelcomeBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		binding.welcomeLogin.setOnClickListener(v -> {
            WelcomeActivity wa = (WelcomeActivity) getActivity();
            wa.switchTab(WelcomeActivity.TAB_LOGIN);
        });
		
		binding.welcomeRegister.setOnClickListener(v -> {
            WelcomeActivity wa = (WelcomeActivity) getActivity();
            wa.switchTab(WelcomeActivity.TAB_REGISTER);
        });

		binding.welcomePrivacyInfo.setOnClickListener(v -> {
			Intent iA = new Intent(getActivity(), PrivacyActivity.class);
			startActivity(iA);
		});
	}
}
