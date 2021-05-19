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

package org.digitalcampus.oppia.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentAboutBinding;
import org.digitalcampus.mobile.learning.databinding.FragmentGlobalScorecardBinding;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.adapter.ScorecardsGridAdapter;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.model.Badge;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.recyclerview.widget.RecyclerView;

public class GlobalScorecardFragment extends AppFragment implements ScorecardsGridAdapter.OnItemClickListener {

    private ScorecardsGridAdapter adapterScorecards;

    @Inject
    CoursesRepository coursesRepository;

    @Inject
    ApiEndpoint apiEndpoint;

    private final List<Course> courses = new ArrayList<>();
    private FragmentGlobalScorecardBinding binding;

    public static GlobalScorecardFragment newInstance() {
        return new GlobalScorecardFragment();
    }
    public GlobalScorecardFragment(){
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGlobalScorecardBinding.inflate(inflater, container, false);

        getAppComponent().inject(this);
        showBadgeAwardingInfo();

        adapterScorecards = new ScorecardsGridAdapter(getActivity(), courses);
        adapterScorecards.setOnItemClickListener(this);
        binding.recyclerScorecards.setAdapter(adapterScorecards);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        courses.clear();
        courses.addAll(coursesRepository.getCourses(getActivity()));
        showEmptyStateView(courses.isEmpty());
        adapterScorecards.notifyDataSetChanged();
    }

    private void showBadgeAwardingInfo(){
        String criteria = prefs.getString(PrefsActivity.PREF_BADGE_AWARD_CRITERIA, null);
        String criteriaDescription = null;
        if (TextUtils.equals(criteria, Badge.BADGE_CRITERIA_ALL_QUIZZES_PERCENT)){
            int percent = prefs.getInt(PrefsActivity.PREF_BADGE_AWARD_CRITERIA_PERCENT, 100);
            criteriaDescription = getString(R.string.badges_award_method_all_quizzes_plus_percent, percent);
        }
        else if (criteria!=null && !TextUtils.isEmpty(criteria) && !TextUtils.equals(criteria, "undefined")) {
            int resId = getResources().getIdentifier("badges.award_method." + criteria, "string", getContext().getPackageName());
            criteriaDescription = getString(resId);
        }

        if (criteriaDescription != null){
            binding.badgeAward.setVisibility(View.VISIBLE);
            binding.dismissBadge.setOnClickListener(view -> binding.badgeAward.setVisibility(View.GONE));
            String badgeCriteriaTitle = getString(R.string.badges_award_method_criteria);
            SpannableString badgeText = new SpannableString(badgeCriteriaTitle + " " + criteriaDescription);
            badgeText.setSpan(new StyleSpan(Typeface.BOLD), 0, badgeCriteriaTitle.length(),  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.badgeAwardCriteria.setText(badgeText, TextView.BufferType.SPANNABLE);
        }
    }

    private void showEmptyStateView(boolean show) {

        if (show) {
            binding.recyclerScorecards.setVisibility(View.GONE);
            binding.emptyState.setVisibility(View.VISIBLE);

            binding.btnDownloadCourses.setOnClickListener(v ->
                    AdminSecurityManager.with(getActivity()).checkAdminPermission(R.id.menu_download, () ->
                            startActivity(new Intent(getActivity(), TagSelectActivity.class))));
        } else {
            binding.recyclerScorecards.setVisibility(View.VISIBLE);
            binding.emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(View view, int position) {

        Course selectedCourse = adapterScorecards.getItemAtPosition(position);
        Intent i = new Intent(super.getActivity(), CourseIndexActivity.class);
        Bundle tb = new Bundle();
        tb.putSerializable(Course.TAG, selectedCourse);
        i.putExtras(tb);
        startActivity(i);
    }
}
