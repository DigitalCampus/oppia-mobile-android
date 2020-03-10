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
	
	public static final String TAB_ACTIVE = "TAB_ACTIVE";
	public static final int TAB_HELP = 1;
	public static final int TAB_PRIVACY = 2;

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
			currentTab = bundle.getInt(AboutActivity.TAB_ACTIVE);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		initialize();

		String lang = sharedPreferences.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
		List<Fragment> fragments = new ArrayList<>();
        List<String> titles = new ArrayList<>();
		
		Fragment fAbout = AboutFragment.newInstance();
		fragments.add(fAbout);
        titles.add(this.getString(R.string.tab_title_about));
		
		String urlHelp = Storage.getLocalizedFilePath(this, lang, "help.html");
		Fragment fHelp = OppiaWebViewFragment.newInstance(TAB_HELP, urlHelp);
		fragments.add(fHelp);
        titles.add(this.getString(R.string.tab_title_help));
		
		String url = Storage.getLocalizedFilePath(this,lang, "privacy.html");
		Fragment fPrivacy = OppiaWebViewFragment.newInstance(TAB_PRIVACY, url);
		fragments.add(fPrivacy);
        titles.add(this.getString(R.string.tab_title_privacy));

		ActivityPagerAdapter adapter = new ActivityPagerAdapter(this, getSupportFragmentManager(), fragments, titles);
		viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
		adapter.updateTabViews(tabs);
		viewPager.setCurrentItem(currentTab);
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
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
