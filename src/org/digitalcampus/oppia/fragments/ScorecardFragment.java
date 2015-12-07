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

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.adapter.CourseQuizzesGridAdapter;
import org.digitalcampus.oppia.adapter.ScorecardListAdapter;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.task.ParseCourseXMLTask;
import org.digitalcampus.oppia.utils.ui.ScorecardPieChart;
import org.digitalcampus.oppia.utils.ui.ProgressBarAnimator;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidplot.pie.PieChart;

public class ScorecardFragment extends Fragment implements ParseCourseXMLTask.OnParseXmlListener, AdapterView.OnItemClickListener {

	public static final String TAG = ScorecardFragment.class.getSimpleName();
	private SharedPreferences prefs;
	private Course course = null;
    private boolean firstTimeOpened = true;
    private GridView quizzesGrid;
    private PieChart scorecardPieChart;
    private ArrayList<QuizStats> quizStats = new ArrayList<QuizStats>();
    private CourseQuizzesGridAdapter quizzesAdapter;
    ParseCourseXMLTask xmlTask;

    private TextView highlightPretest;
    private TextView highlightAttempted;
    private TextView highlightPassed;
    private TextView activitiesTotal;
    private TextView activitiesCompleted;
    private ProgressBar quizzesProgressBar;
    private View quizzesView;
    private View quizzesContainer;

    public static ScorecardFragment newInstance() {
        return new ScorecardFragment();
	}
    
	public static ScorecardFragment newInstance(Course course) {
		ScorecardFragment myFragment = new ScorecardFragment();
		Bundle args = new Bundle();
	    args.putSerializable(Course.TAG, course);
	    myFragment.setArguments(args);
	    return myFragment;
	}

	public ScorecardFragment(){ }

    @Override
    public void onCreate( Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if ( getArguments() != null && getArguments().containsKey(Course.TAG)) {
            this.course = (Course) getArguments().getSerializable(Course.TAG);
        
            xmlTask = new ParseCourseXMLTask(getActivity(), true);
            xmlTask.setListener(this);
            xmlTask.execute(course);
            
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		View vv;
		if (course != null){
			vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_scorecard, null);
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			vv.setLayoutParams(lp);
			// refresh course to get most recent info (otherwise gets the info from when course first opened)
			DbHelper db = new DbHelper(super.getActivity());
			long userId = db.getUserId(SessionManager.getUsername(getActivity()));
			this.course = db.getCourse(this.course.getCourseId(), userId);
            DatabaseManager.getInstance().closeDatabase();

            quizzesGrid = (GridView) vv.findViewById(R.id.scorecard_grid_quizzes);
            scorecardPieChart = (PieChart) vv.findViewById(R.id.scorecard_pie_chart);

            highlightPretest = (TextView) vv.findViewById(R.id.highlight_pretest);
            highlightAttempted = (TextView) vv.findViewById(R.id.highlight_attempted);
            highlightPassed = (TextView) vv.findViewById(R.id.highlight_passed);
            quizzesProgressBar = (ProgressBar) vv.findViewById(R.id.progress_quizzes);
            quizzesView = vv.findViewById(R.id.quizzes_view);
            quizzesContainer = vv.findViewById(R.id.scorecard_quizzes_container);
            activitiesTotal = (TextView)vv.findViewById(R.id.scorecard_activities_total);
            activitiesCompleted = (TextView) vv.findViewById(R.id.scorecard_activities_completed);

		} else {
			vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_scorecards, null);
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			vv.setLayoutParams(lp);
		}
		return vv;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());

		if(this.course != null){

			ScorecardPieChart spc = new ScorecardPieChart(super.getActivity(), scorecardPieChart, this.course);
            spc.drawChart(0, false, firstTimeOpened);
            firstTimeOpened = false;
            activitiesTotal.setText(""+course.getNoActivities());
            activitiesCompleted.setText(""+course.getNoActivitiesCompleted());

            quizzesAdapter = new CourseQuizzesGridAdapter(getActivity(), quizStats);
            quizzesGrid.setAdapter(quizzesAdapter);
            quizzesGrid.setOnItemClickListener(this);

		} else {
			DbHelper db = new DbHelper(super.getActivity());
			long userId = db.getUserId(SessionManager.getUsername(getActivity()));
			ArrayList<Course> courses = db.getCourses(userId);
			DatabaseManager.getInstance().closeDatabase();
            ScorecardListAdapter scorecardListAdapter = new ScorecardListAdapter(super.getActivity(), courses);
			ListView listView = (ListView) super.getActivity().findViewById(R.id.scorecards_list);
			listView.setAdapter(scorecardListAdapter);
		}
	}

    //@Override
    public void onParseComplete(CourseXMLReader parsed) {

        ArrayList<Activity> baseline = parsed.getBaselineActivities();
        
    	DbHelper db = new DbHelper(super.getActivity());
        long userId = db.getUserId(SessionManager.getUsername(getActivity()));
        ArrayList<Activity> quizActs = db.getCourseQuizzes(course.getCourseId());
        ArrayList<QuizStats> quizzes = new ArrayList<QuizStats>();
        for (Activity a: quizActs){
        	// get the max score for the quiz for the user
        	QuizStats qs = db.getQuizAttempt(a.getDigest(), userId);
        	quizzes.add(qs);
        }

        int quizzesAttempted = 0, quizzesPassed = 0;

        for (QuizStats qs: quizzes){
        	if (qs.isAttempted()){
        		quizzesAttempted++;
        	}
        	if (qs.isPassed()){
        		quizzesPassed++;
        	}
        }
        
        int pretestScore = -1;
        for (Activity baselineAct : baseline){
            if (!baselineAct.getActType().equals("quiz")) continue;
            QuizStats pretest = db.getQuizAttempt(baselineAct.getDigest(), userId);
            pretestScore = pretest.getPercent();
        }
        DatabaseManager.getInstance().closeDatabase();
        
        quizStats.clear();
        quizStats.addAll(quizzes);
        if (quizStats.size() == 0){
            quizzesContainer.setVisibility(View.GONE);
            return;
        }
        
        highlightPretest.setText(pretestScore >= 0 ? (pretestScore + "%") : "-");
        highlightAttempted.setText("" + quizzesAttempted);
        highlightPassed.setText("" + quizzesPassed);
        quizzesAdapter.notifyDataSetChanged();

        AlphaAnimation fadeInAnimation = new AlphaAnimation(0f, 1f);
        fadeInAnimation.setDuration(700);
        fadeInAnimation.setFillAfter(true);

        quizzesProgressBar.setProgress(0);
        quizzesProgressBar.setSecondaryProgress(0);

        quizzesView.setVisibility(View.VISIBLE);
        quizzesView.startAnimation(fadeInAnimation);

        quizzesProgressBar.setMax(quizStats.size());
        ProgressBarAnimator animator = new ProgressBarAnimator(quizzesProgressBar);
        animator.setStartDelay(500);
        animator.animateBoth(quizzesPassed, quizzesAttempted);
    }

    @Override
    public void onStop(){
        super.onStop();
        if (xmlTask != null){
            xmlTask.setListener(null);
        }
    }

    //@Override
    public void onParseError() {

    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        QuizStats quiz = quizzesAdapter.getItem(i);

        Intent returnIntent = new Intent();
        returnIntent.putExtra(CourseIndexActivity.JUMPTO_TAG, quiz.getDigest());
        getActivity().setResult(CourseIndexActivity.RESULT_JUMPTO, returnIntent);
        getActivity().finish();
    }
}
