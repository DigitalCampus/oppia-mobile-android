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

import java.util.ArrayList;
import java.util.List;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;
import org.digitalcampus.oppia.fragments.BadgesFragment;
import org.digitalcampus.oppia.fragments.PointsFragment;
import org.digitalcampus.oppia.fragments.ScorecardFragment;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.ImageUtils;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;


public class ScorecardActivity extends FragmentActivity implements ActionBar.TabListener {

	public static final String TAG = ScorecardActivity.class.getSimpleName();
    public static final String TAB_TARGET = "target";
    public static final String TAB_TARGET_POINTS = "tab_points";
    public static final String TAB_TARGET_BADGES = "tab_badges";

	private ActionBar actionBar;
	private ViewPager viewPager;
	private ActivityPagerAdapter apAdapter;
	private int currentTab = 0;
	private SharedPreferences prefs;
	private Course course = null;

    private String targetTabOnLoad;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_scorecard);
		actionBar = getActionBar();
		viewPager = (ViewPager) findViewById(R.id.activity_scorecard_pager);
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			this.course = (Course) bundle.getSerializable(Course.TAG);
            this.targetTabOnLoad = bundle.getString(TAB_TARGET);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();

		actionBar.removeAllTabs();
		List<Fragment> fragments = new ArrayList<Fragment>();
		
		Fragment fScorecard;
		if(this.course != null){
			fScorecard = ScorecardFragment.newInstance(course);
            if (course.getImageFile() != null) {
                BitmapDrawable bm = ImageUtils.LoadBMPsdcard(course.getImageFileFromRoot(), this.getResources(),
                        R.drawable.dc_logo);
                actionBar.setIcon(bm);
            }
		} else {
			fScorecard = ScorecardFragment.newInstance();
		}
		
		fragments.add(fScorecard);
		actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_scorecard)).setTabListener(this), true);
	
		boolean scoringEnabled = prefs.getBoolean(PrefsActivity.PREF_SCORING_ENABLED, true);
		if (scoringEnabled) {
			Fragment fPoints = PointsFragment.newInstance();
			fragments.add(fPoints);
			actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_points)).setTabListener(this), false);
		}
		
		boolean badgingEnabled = prefs.getBoolean(PrefsActivity.PREF_BADGING_ENABLED, true);
		if (badgingEnabled) {
			Fragment fBadges= BadgesFragment.newInstance();
			fragments.add(fBadges);
			actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_badges)).setTabListener(this), false);
		}
		
		apAdapter = new ActivityPagerAdapter(getSupportFragmentManager(), fragments);
		viewPager.setAdapter(apAdapter);

        if ( targetTabOnLoad != null){
            if (targetTabOnLoad.equals(TAB_TARGET_POINTS) && scoringEnabled) {
                currentTab = 1;
            }
            if (targetTabOnLoad.equals(TAB_TARGET_BADGES) && badgingEnabled) {
                currentTab = scoringEnabled ? 2 : 1;
            }
        }
		viewPager.setCurrentItem(currentTab);
        actionBar.setSelectedNavigationItem(currentTab);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int arg0) {
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
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

	public void onTabUnselected(Tab tab, FragmentTransaction ft) { }

	public void onTabReselected(Tab tab, FragmentTransaction ft) { }
	
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
