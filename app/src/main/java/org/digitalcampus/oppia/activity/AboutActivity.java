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

package org.digitalcampus.oppia.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import com.google.android.material.tabs.TabLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityAboutBinding;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;
import org.digitalcampus.oppia.fragments.AboutFragment;
import org.digitalcampus.oppia.fragments.OppiaWebViewFragment;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AboutActivity extends AppActivity {

	public static final String ABOUT_CONTENTS = "CONTENTS";
	public static final String ABOUT_MAIN = "MAIN";
	public static final String ABOUT_PRIVACY = "PRIVACY";
	public static final String ABOUT_TERMS = "TERMS";

	public static final String TITLE = "TITLE";
	public static final String TAB_ACTIVE = "TAB_ACTIVE";
	public static final int TAB_HELP = 1;
	public static final int TAB_PRIVACY_POLICY = 0;
	public static final int TAB_PRIVACY_TERMS = 1;
	public static final int TAB_PRIVACY_WHAT = 2;
	public static final int TAB_PRIVACY_HOW = 3;


	private String contents = ABOUT_MAIN;
	private int currentTab = 0;
	private SharedPreferences sharedPreferences;
	private ActivityAboutBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityAboutBinding.inflate(LayoutInflater.from(this));
		setContentView(binding.getRoot());
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			contents = bundle.getString(ABOUT_CONTENTS, ABOUT_MAIN);
			currentTab = bundle.getInt(AboutActivity.TAB_ACTIVE, 0);

			String actTitle = bundle.getString(TITLE);
			if (!TextUtils.isEmpty(actTitle)){ setTitle(actTitle); }
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		initialize();

		String lang = sharedPreferences.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
		List<Fragment> fragments = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        if (contents.equals(ABOUT_MAIN)){
			Fragment fAbout = AboutFragment.newInstance();
			fragments.add(fAbout);
			titles.add(this.getString(R.string.tab_title_about));

			String urlHelp = Storage.getLocalizedFilePath(this, lang, "help.html");
			Fragment fHelp = OppiaWebViewFragment.newInstance(TAB_HELP, urlHelp);
			fragments.add(fHelp);
			titles.add(this.getString(R.string.tab_title_help));
		}
        else if (contents.equals(ABOUT_PRIVACY)){
			String urlPolicy = Storage.getLocalizedFilePath(this, lang, "privacy.html");
			Fragment fPolicy = OppiaWebViewFragment.newInstance(TAB_PRIVACY_POLICY, urlPolicy);
			fragments.add(fPolicy);
			titles.add(this.getString(R.string.tab_title_privacy));

			String urlTerms = Storage.getLocalizedFilePath(this, lang, "terms.html");
			Fragment fTerms = OppiaWebViewFragment.newInstance(TAB_PRIVACY_TERMS, urlTerms);
			fragments.add(fTerms);
			titles.add(this.getString(R.string.tab_title_terms));

			String urlWhat = Storage.getLocalizedFilePath(this, lang, "privacy_data_what.html");
			Fragment fWhat = OppiaWebViewFragment.newInstance(TAB_PRIVACY_WHAT, urlWhat);
			fragments.add(fWhat);
			titles.add(this.getString(R.string.privacy_data_what));

			String urlWhy = Storage.getLocalizedFilePath(this, lang, "privacy_data_how.html");
			Fragment fWhy = OppiaWebViewFragment.newInstance(TAB_PRIVACY_HOW, urlWhy);
			fragments.add(fWhy);
			titles.add(this.getString(R.string.privacy_data_why));
		}


		ActivityPagerAdapter adapter = new ActivityPagerAdapter(this, getSupportFragmentManager(), fragments, titles);
		binding.activityAboutPager.setAdapter(adapter);
        binding.tabsToolbar.setupWithViewPager(binding.activityAboutPager);
		adapter.updateTabViews(binding.tabsToolbar);
        binding.activityAboutPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabsToolbar));

	}

	@Override
	public void onResume(){
		super.onResume();
		binding.activityAboutPager.setCurrentItem(currentTab);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.finish();
			return true;
		} else {
			return false;
		}
	}
}
