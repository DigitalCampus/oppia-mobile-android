package org.digitalcampus.oppia.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.adapter.ScorecardListAdapter;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Course;

import java.util.ArrayList;

public class GlobalScorecardFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static final String TAG = CourseScorecardFragment.class.getSimpleName();
    private ScorecardListAdapter scorecardListAdapter;

    public static GlobalScorecardFragment newInstance() {
        return new GlobalScorecardFragment();
    }
    public GlobalScorecardFragment(){ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_scorecards, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        vv.setLayoutParams(lp);
        return vv;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DbHelper db = new DbHelper(super.getActivity());
        long userId = db.getUserId(SessionManager.getUsername(getActivity()));
        ArrayList<Course> courses = db.getCourses(userId);
        DatabaseManager.getInstance().closeDatabase();
        scorecardListAdapter = new ScorecardListAdapter(super.getActivity(), courses);
        GridView scorecardList = (GridView) super.getActivity().findViewById(R.id.scorecards_list);
        scorecardList.setAdapter(scorecardListAdapter);
        scorecardList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Course selectedCourse = scorecardListAdapter.getItem(position);
        Intent i = new Intent(super.getActivity(), CourseIndexActivity.class);
        Bundle tb = new Bundle();
        tb.putSerializable(Course.TAG, selectedCourse);
        i.putExtras(tb);
        startActivity(i);
        super.getActivity().finish();
    }
}
