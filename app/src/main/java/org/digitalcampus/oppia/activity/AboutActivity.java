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
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.digitalcampus.mobile.learning.R;
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

	public static final String TAB_ACTIVE = "TAB_ACTIVE";
	public static final int TAB_HELP = 1;
	public static final int TAB_PRIVACY_POLICY = 0;
	public static final int TAB_PRIVACY_WHAT = 1;
	public static final int TAB_PRIVACY_WHY = 2;

	private String contents = ABOUT_MAIN;
	private ViewPager viewPager;
    private TabLayout tabs;
	private int currentTab = 0;
	private SharedPreferences sharedPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_about);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		viewPager = findViewById(R.id.activity_about_pager);

        tabs = findViewById(R.id.tabs_toolbar);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			contents = bundle.getString(ABOUT_CONTENTS, ABOUT_MAIN);
			currentTab = bundle.getInt(AboutActivity.TAB_ACTIVE, 0);
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

			String urlWhat = Storage.getLocalizedFilePath(this, lang, "privacy_data_what.html");
			Fragment fWhat = OppiaWebViewFragment.newInstance(TAB_PRIVACY_WHY, urlWhat);
			fragments.add(fWhat);
			titles.add(this.getString(R.string.privacy_data_what));

			String urlWhy = Storage.getLocalizedFilePath(this, lang, "privacy_data_how.html");
			Fragment fWhy = OppiaWebViewFragment.newInstance(TAB_HELP, urlWhy);
			fragments.add(fWhy);
			titles.add(this.getString(R.string.privacy_data_why));
		}


		ActivityPagerAdapter adapter = new ActivityPagerAdapter(this, getSupportFragmentManager(), fragments, titles);
		viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
		adapter.updateTabViews(tabs);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

	}

	@Override
	public void onResume(){
		super.onResume();
		viewPager.setCurrentItem(currentTab);
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
