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
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.kano.training.oppia.R;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;
import org.digitalcampus.oppia.fragments.AboutFragment;
import org.digitalcampus.oppia.fragments.OppiaWebViewFragment;
import org.digitalcampus.oppia.fragments.StatsFragment;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AboutActivity extends AppActivity {

	public static final String TAG = AboutActivity.class.getSimpleName();
	
	public static final String TAB_ACTIVE = "TAB_ACTIVE";
	public static final int TAB_ABOUT = 0;
	public static final int TAB_HELP = 1;
	public static final int TAB_PRIVACY = 2;
	public static final int TAB_STATS = 3;

	private ActionBar actionBar;
	private ViewPager viewPager;
    private TabLayout tabs;
	private ActivityPagerAdapter apAdapter;
	private int currentTab = 0;
	private SharedPreferences prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_about);
		actionBar = getSupportActionBar();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		viewPager = (ViewPager) findViewById(R.id.activity_about_pager);

        tabs = (TabLayout) findViewById(R.id.tabs_toolbar);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			currentTab = (Integer) bundle.getSerializable(AboutActivity.TAB_ACTIVE);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();

		String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
		List<Fragment> fragments = new ArrayList<Fragment>();
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
		
		Fragment fStats = StatsFragment.newInstance();
		fragments.add(fStats);
        titles.add(this.getString(R.string.tab_title_activity));
		
		apAdapter = new ActivityPagerAdapter(this, getSupportFragmentManager(), fragments, titles);
		viewPager.setAdapter(apAdapter);
        tabs.setupWithViewPager(viewPager);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);

		viewPager.setCurrentItem(currentTab);
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				this.finish();
				return true;
		}
		return true;
	}
}
