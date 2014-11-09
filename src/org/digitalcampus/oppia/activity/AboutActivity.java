/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;
import org.digitalcampus.oppia.fragments.AboutFragment;
import org.digitalcampus.oppia.fragments.OppiaWebViewFragment;
import org.digitalcampus.oppia.fragments.StatsFragment;
import org.digitalcampus.oppia.utils.FileUtils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class AboutActivity extends SherlockFragmentActivity implements ActionBar.TabListener {

	public static final String TAG = AboutActivity.class.getSimpleName();
	
	public static final String TAB_ACTIVE = "TAB_ACTIVE";
	public static final int TAB_ABOUT = 0;
	public static final int TAB_HELP = 1;
	public static final int TAB_PRIVACY = 2;
	public static final int TAB_STATS = 3;

	private ActionBar actionBar;
	private ViewPager viewPager;
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
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			currentTab = (Integer) bundle.getSerializable(AboutActivity.TAB_ACTIVE);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();

		String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
		
		actionBar.removeAllTabs();
		List<Fragment> fragments = new ArrayList<Fragment>();
		
		Fragment fAbout = AboutFragment.newInstance();
		fragments.add(fAbout);
		
		String urlHelp = FileUtils.getLocalizedFilePath(this,lang, "help.html");
		Fragment fHelp = OppiaWebViewFragment.newInstance(TAB_HELP, urlHelp);
		fragments.add(fHelp);
		
		String url = FileUtils.getLocalizedFilePath(this,lang, "privacy.html");
		Fragment fPrivacy = OppiaWebViewFragment.newInstance(TAB_PRIVACY, url);
		fragments.add(fPrivacy);
		
		Fragment fStats = StatsFragment.newInstance();
		fragments.add(fStats);
		
		
		apAdapter = new ActivityPagerAdapter(getSupportFragmentManager(), fragments);
		viewPager.setAdapter(apAdapter);

		boolean setSelected = false;
		if (currentTab == AboutActivity.TAB_ABOUT){
			setSelected = true;
		} else {
			setSelected = false;
		}
		actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_about)).setTabListener(this), TAB_ABOUT, setSelected);
		if (currentTab == AboutActivity.TAB_HELP){
			setSelected = true;
		}else {
			setSelected = false;
		}
		actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_help)).setTabListener(this), TAB_HELP, setSelected);
		if (currentTab == AboutActivity.TAB_PRIVACY){
			setSelected = true;
		}else {
			setSelected = false;
		}
		actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_privacy)).setTabListener(this), TAB_PRIVACY, setSelected);

		if (currentTab == AboutActivity.TAB_STATS){
			setSelected = true;
		}else {
			setSelected = false;
		}
		actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_activity)).setTabListener(this), TAB_STATS, setSelected);
		viewPager.setCurrentItem(currentTab);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			public void onPageScrollStateChanged(int arg0) {
				// do nothing
			}

			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// do nothing
			}

			public void onPageSelected(int arg0) {
				actionBar.setSelectedNavigationItem(arg0);
			}

		});
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
		this.currentTab = tab.getPosition();
		
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
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
