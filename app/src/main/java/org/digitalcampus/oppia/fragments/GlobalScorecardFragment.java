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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.adapter.ScorecardsGridAdapter;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Badge;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.task.APIRequestTask;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.FetchServerInfoTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

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

    private RecyclerView recyclerScorecards;
    private View emptyState;
    private View badgesAwardingInfo;
    private TextView badgeCriteria;
    private ImageButton dismissBadgeInfoBtn;

    private final List<Course> courses = new ArrayList<>();

    public static GlobalScorecardFragment newInstance() {
        return new GlobalScorecardFragment();
    }
    public GlobalScorecardFragment(){
        // Required empty public constructor
    }


    private void findViews(View layout) {
        recyclerScorecards = layout.findViewById(R.id.recycler_scorecards);
        emptyState = layout.findViewById(R.id.empty_state);
        badgesAwardingInfo = layout.findViewById(R.id.badge_award);
        badgeCriteria = layout.findViewById(R.id.badge_award_criteria);
        dismissBadgeInfoBtn = layout.findViewById(R.id.dismiss_badge);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_global_scorecard, container, false);
        findViews(layout);

        getAppComponent().inject(this);
        showBadgeAwardingInfo();

        adapterScorecards = new ScorecardsGridAdapter(getActivity(), courses);
        adapterScorecards.setOnItemClickListener(this);
        recyclerScorecards.setAdapter(adapterScorecards);

        return layout;
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
            badgesAwardingInfo.setVisibility(View.VISIBLE);
            dismissBadgeInfoBtn.setOnClickListener(view -> badgesAwardingInfo.setVisibility(View.GONE));
            String badgeCriteriaTitle = getString(R.string.badges_award_method_criteria);
            SpannableString badgeText = new SpannableString(badgeCriteriaTitle + " " + criteriaDescription);
            badgeText.setSpan(new StyleSpan(Typeface.BOLD), 0, badgeCriteriaTitle.length(),  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            badgeCriteria.setText(badgeText, TextView.BufferType.SPANNABLE);
        }
    }

    private void showEmptyStateView(boolean show) {

        if (show) {
            recyclerScorecards.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);

            Button download = emptyState.findViewById(R.id.btn_download_courses);
            download.setOnClickListener(v ->
                    AdminSecurityManager.with(getActivity()).checkAdminPermission(R.id.menu_download, () ->
                            startActivity(new Intent(getActivity(), TagSelectActivity.class))));
        } else {
            recyclerScorecards.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
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
