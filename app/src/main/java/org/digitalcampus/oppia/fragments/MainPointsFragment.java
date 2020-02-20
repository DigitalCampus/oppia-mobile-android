package org.digitalcampus.oppia.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;

import java.util.ArrayList;
import java.util.List;

public class MainPointsFragment extends TabsFragment {

    public static MainPointsFragment newInstance() {
        return new MainPointsFragment();
    }

    public MainPointsFragment() {
        // do nothing
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<Fragment> fragments = new ArrayList<>();
        List<String> tabTitles = new ArrayList<>();

        boolean scoringEnabled = getPrefs().getBoolean(PrefsActivity.PREF_SCORING_ENABLED, true);
        if (scoringEnabled) {
            Fragment fPoints = PointsFragment.newInstance(null);
            fragments.add(fPoints);
            tabTitles.add(getString(R.string.tab_title_points));

            Fragment fLeaderboard = LeaderboardFragment.newInstance();
            fragments.add(fLeaderboard);
            tabTitles.add(getString(R.string.tab_title_leaderboard));

        }

        boolean badgingEnabled = getPrefs().getBoolean(PrefsActivity.PREF_BADGING_ENABLED, true);
        if (badgingEnabled) {
            Fragment fBadges = BadgesFragment.newInstance();
            fragments.add(fBadges);
            tabTitles.add(this.getString(R.string.tab_title_badges));
        }

        configureFragments(fragments, tabTitles);
    }
}
