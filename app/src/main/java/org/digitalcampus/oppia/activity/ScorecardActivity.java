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
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;
import org.digitalcampus.oppia.fragments.ActivitiesFragment;
import org.digitalcampus.oppia.fragments.BadgesFragment;
import org.digitalcampus.oppia.fragments.CourseScorecardFragment;
import org.digitalcampus.oppia.fragments.GlobalScorecardFragment;
import org.digitalcampus.oppia.fragments.LeaderboardFragment;
import org.digitalcampus.oppia.fragments.PointsFragment;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;


public class ScorecardActivity extends AppActivity {

	public static final String TAG = ScorecardActivity.class.getSimpleName();
    public static final String TAB_TARGET = "target";
    public static final String TAB_TARGET_POINTS = "tab_points";
    public static final String TAB_TARGET_BADGES = "tab_badges";

	private TabLayout tabs;
	private ViewPager viewPager;
	private SharedPreferences prefs;
	private Course course = null;

    private String targetTabOnLoad;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_scorecard);

		viewPager = findViewById(R.id.activity_scorecard_pager);
        tabs = findViewById(R.id.tabs_toolbar);
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

		List<Fragment> fragments = new ArrayList<>();
        List<String> tabTitles = new ArrayList<>();

		Fragment fScorecard;
		if(this.course != null){
			fScorecard = CourseScorecardFragment.newInstance(course);
			/*ActionBar actionBar = getSupportActionBar();
            if ((actionBar != null) && (course.getImageFile() != null)) {
                BitmapDrawable bm = ImageUtils.LoadBMPsdcard(course.getImageFileFromRoot(), this.getResources(), R.drawable.dc_logo);
                actionBar.setHomeAsUpIndicator(bm);
            }*/
		} else {
			fScorecard = GlobalScorecardFragment.newInstance();
		}
		fragments.add(fScorecard);
        tabTitles.add(this.getString(R.string.tab_title_scorecard));

		fragments.add(ActivitiesFragment.newInstance());
		tabTitles.add(this.getString(R.string.tab_title_activity));

		boolean scoringEnabled = prefs.getBoolean(PrefsActivity.PREF_SCORING_ENABLED, true);
		if (scoringEnabled) {
			Fragment fPoints = PointsFragment.newInstance(course);
			fragments.add(fPoints);
            tabTitles.add(this.getString(R.string.tab_title_points));

            if (course == null){
				Fragment fLeaderboard = LeaderboardFragment.newInstance();
				fragments.add(fLeaderboard);
				tabTitles.add(getString(R.string.tab_title_leaderboard));
			}

        }

		boolean badgingEnabled = prefs.getBoolean(PrefsActivity.PREF_BADGING_ENABLED, true);
		if ((badgingEnabled) && (course == null)){
			Fragment fBadges= BadgesFragment.newInstance();
			fragments.add(fBadges);
            tabTitles.add(this.getString(R.string.tab_title_badges));
        }


		ActivityPagerAdapter apAdapter = new ActivityPagerAdapter(this, getSupportFragmentManager(), fragments, tabTitles);
		viewPager.setAdapter(apAdapter);
        tabs.setupWithViewPager(viewPager);
		apAdapter.updateTabViews(tabs);

        int currentTab = 0;
        if ( targetTabOnLoad != null){
            if (targetTabOnLoad.equals(TAB_TARGET_POINTS) && scoringEnabled) {
                currentTab = 2;
            }
            if (targetTabOnLoad.equals(TAB_TARGET_BADGES) && badgingEnabled) {
                currentTab = scoringEnabled ? 3 : 2;
            }
        }
		viewPager.setCurrentItem(currentTab);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
		tabs.setTabMode(TabLayout.MODE_FIXED);
	}

	public Course getCourse() {
		return course;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				this.finish();
				return true;
			default:
				return false;
		}
	}
}
