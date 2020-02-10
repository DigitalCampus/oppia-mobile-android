package org.digitalcampus.oppia.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.digitalcampus.mobile.learning.R;

import java.util.ArrayList;
import java.util.List;

public class MainScorecardFragment extends TabsFragment {

    public static MainScorecardFragment newInstance() {
        return new MainScorecardFragment();
    }

    public MainScorecardFragment() {
        // do nothing
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<Fragment> fragments = new ArrayList<>();
        List<String> tabTitles = new ArrayList<>();

        fragments.add(GlobalScorecardFragment.newInstance());
        tabTitles.add(this.getString(R.string.tab_title_scorecard));

        fragments.add(ActivitiesFragment.newInstance(null));
        tabTitles.add(this.getString(R.string.tab_title_activity));

        fragments.add(GlobalQuizAttemptsFragment.newInstance());
        tabTitles.add(this.getString(R.string.scorecard_quizzes_title));

        configureFragments(fragments, tabTitles);
    }
}
