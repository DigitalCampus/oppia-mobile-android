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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityScorecardBinding;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;
import org.digitalcampus.oppia.fragments.ActivitiesFragment;
import org.digitalcampus.oppia.fragments.BadgesFragment;
import org.digitalcampus.oppia.fragments.CourseScorecardFragment;
import org.digitalcampus.oppia.fragments.GlobalScorecardFragment;
import org.digitalcampus.oppia.fragments.LeaderboardFragment;
import org.digitalcampus.oppia.fragments.PointsFragment;
import org.digitalcampus.oppia.model.Course;

import java.util.ArrayList;
import java.util.List;


public class ScorecardActivity extends AppActivity {

    public static final String TAB_TARGET = "target";
    public static final String TAB_TARGET_POINTS = "tab_points";
    public static final String TAB_TARGET_BADGES = "tab_badges";

	private Course course = null;

    private String targetTabOnLoad;

	private List<Fragment> fragments = new ArrayList<>();
	private List<String> tabTitles = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityScorecardBinding binding = ActivityScorecardBinding.inflate(LayoutInflater.from(this));
		setContentView(binding.getRoot());

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			this.course = (Course) bundle.getSerializable(Course.TAG);
            this.targetTabOnLoad = bundle.getString(TAB_TARGET);
		}

		Fragment fScorecard;
		if(this.course != null){
			fScorecard = CourseScorecardFragment.newInstance(course);
		} else {
			fScorecard = GlobalScorecardFragment.newInstance();
		}
		fragments.add(fScorecard);
		tabTitles.add(this.getString(R.string.tab_title_scorecard));

		fragments.add(ActivitiesFragment.newInstance(course));
		tabTitles.add(this.getString(R.string.tab_title_activity));

		boolean scoringEnabled = prefs.getBoolean(PrefsActivity.PREF_SCORING_ENABLED, true);
		this.displayPoints(scoringEnabled);

		boolean badgingEnabled = prefs.getBoolean(PrefsActivity.PREF_BADGING_ENABLED, true);
		if ((badgingEnabled) && (course == null)){
			Fragment fBadges= BadgesFragment.newInstance();
			fragments.add(fBadges);
			tabTitles.add(this.getString(R.string.tab_title_badges));
		}

		ActivityPagerAdapter apAdapter = new ActivityPagerAdapter(this, getSupportFragmentManager(), fragments, tabTitles);
		binding.activityScorecardPager.setAdapter(apAdapter);
		binding.tabsToolbar.setupWithViewPager(binding.activityScorecardPager);
		apAdapter.updateTabViews(binding.tabsToolbar);

		int currentTab = 0;
		if ( targetTabOnLoad != null){
			if (targetTabOnLoad.equals(TAB_TARGET_POINTS) && scoringEnabled) {
				currentTab = 2;
			}
			if (targetTabOnLoad.equals(TAB_TARGET_BADGES) && badgingEnabled) {
				currentTab = scoringEnabled ? 3 : 2;
			}
		}
		binding.activityScorecardPager.setCurrentItem(currentTab);
		binding.activityScorecardPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabsToolbar));
		binding.tabsToolbar.setTabMode(TabLayout.MODE_FIXED);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// If we come back from another child activity that wants to jump to an activity,
		// pass back the digest to the course index activity
		if (resultCode == CourseIndexActivity.RESULT_JUMPTO) {

			String digest = data.getStringExtra(CourseIndexActivity.JUMPTO_TAG);
			Intent returnIntent = new Intent();
			returnIntent.putExtra(CourseIndexActivity.JUMPTO_TAG,digest);
			setResult(CourseIndexActivity.RESULT_JUMPTO, returnIntent);
			finish();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		initialize();
	}

	public Course getCourse() {
		return course;
	}

	private void displayPoints(boolean scoringEnabled){
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
	}
}
