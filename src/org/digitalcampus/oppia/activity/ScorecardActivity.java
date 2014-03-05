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
import org.digitalcampus.oppia.fragments.ScorecardFragment;
import org.digitalcampus.oppia.widgets.FeedbackWidget;
import org.digitalcampus.oppia.widgets.PageWidget;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.digitalcampus.oppia.widgets.ResourceWidget;

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

public class ScorecardActivity extends SherlockFragmentActivity implements ActionBar.TabListener {

	public static final String TAG = ScorecardActivity.class.getSimpleName();
	private SharedPreferences prefs;
	private ActionBar actionBar;
	private ViewPager viewPager;
	private ActivityPagerAdapter apAdapter;
	private int currentTab = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_scorecard);
		actionBar = getSupportActionBar();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		viewPager = (ViewPager) findViewById(R.id.activity_scorecard_pager);
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
	}
	
	@Override
	public void onStart() {
		super.onStart();

		actionBar.removeAllTabs();
		List<Fragment> fragments = new ArrayList<Fragment>();
		
		Fragment fScorecard = ScorecardFragment.newInstance();
		fragments.add(fScorecard);
		actionBar.addTab(actionBar.newTab().setText("Scorecard").setTabListener(this), true);
	
		
		apAdapter = new ActivityPagerAdapter(getSupportFragmentManager(), fragments);
		viewPager.setAdapter(apAdapter);

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
