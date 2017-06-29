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

import org.kano.training.oppia.R;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;
import org.digitalcampus.oppia.fragments.BadgesFragment;
import org.digitalcampus.oppia.fragments.GlobalScorecardFragment;
import org.digitalcampus.oppia.fragments.PointsFragment;
import org.digitalcampus.oppia.fragments.CourseScorecardFragment;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.ImageUtils;

import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


public class ScorecardActivity extends AppActivity {

	public static final String TAG = ScorecardActivity.class.getSimpleName();
    public static final String TAB_TARGET = "target";
    public static final String TAB_TARGET_POINTS = "tab_points";
    public static final String TAB_TARGET_BADGES = "tab_badges";

    private ActionBar actionBar;
    private TabLayout tabs;
	private ViewPager viewPager;
	private ActivityPagerAdapter apAdapter;
	private SharedPreferences prefs;
	private Course course = null;

    private String targetTabOnLoad;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_scorecard);

		viewPager = (ViewPager) findViewById(R.id.activity_scorecard_pager);

        tabs = (TabLayout) findViewById(R.id.tabs_toolbar);

        setSupportActionBar( (Toolbar)findViewById(R.id.toolbar) );
        actionBar = getSupportActionBar();
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

		List<Fragment> fragments = new ArrayList<Fragment>();
        List<String> tabTitles = new ArrayList<>();

		Fragment fScorecard;
		if(this.course != null){
			fScorecard = CourseScorecardFragment.newInstance(course);
            if (course.getImageFile() != null) {
                BitmapDrawable bm = ImageUtils.LoadBMPsdcard(course.getImageFileFromRoot(), this.getResources(), R.drawable.dc_logo);
                //actionBar.setIcon(bm);
                actionBar.setHomeAsUpIndicator(bm);
            }
		} else {
			fScorecard = GlobalScorecardFragment.newInstance();
		}
		fragments.add(fScorecard);
        tabTitles.add(this.getString(R.string.tab_title_scorecard));

		boolean scoringEnabled = prefs.getBoolean(PrefsActivity.PREF_SCORING_ENABLED, true);
		if (scoringEnabled) {
			Fragment fPoints = PointsFragment.newInstance();
			fragments.add(fPoints);
            tabTitles.add(this.getString(R.string.tab_title_points));
        }

		boolean badgingEnabled = prefs.getBoolean(PrefsActivity.PREF_BADGING_ENABLED, true);
		if (badgingEnabled) {
			Fragment fBadges= BadgesFragment.newInstance();
			fragments.add(fBadges);
            tabTitles.add(this.getString(R.string.tab_title_badges));
        }

		apAdapter = new ActivityPagerAdapter(this, getSupportFragmentManager(), fragments, tabTitles);
		viewPager.setAdapter(apAdapter);
        tabs.setupWithViewPager(viewPager);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);

        int currentTab = 0;
        if ( targetTabOnLoad != null){
            if (targetTabOnLoad.equals(TAB_TARGET_POINTS) && scoringEnabled) {
                currentTab = 1;
            }
            if (targetTabOnLoad.equals(TAB_TARGET_BADGES) && badgingEnabled) {
                currentTab = scoringEnabled ? 2 : 1;
            }
        }
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
