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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.adapter.CourseQuizzesGridAdapter;
import org.digitalcampus.oppia.adapter.ScorecardListAdapter;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.task.ParseCourseXMLTask;
import org.digitalcampus.oppia.utils.ScorecardPieChart;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidplot.pie.PieChart;

public class ScorecardFragment extends Fragment implements ParseCourseXMLTask.OnParseXmlListener {

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
			long userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
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
            spc.drawChart(50, true, firstTimeOpened);
            firstTimeOpened = false;

            quizzesAdapter = new CourseQuizzesGridAdapter(getActivity(), quizStats);
            quizzesGrid.setAdapter(quizzesAdapter);

		} else {
			DbHelper db = new DbHelper(super.getActivity());
			long userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
			ArrayList<Course> courses = db.getCourses(userId);
			DatabaseManager.getInstance().closeDatabase();
            ScorecardListAdapter scorecardListAdapter = new ScorecardListAdapter(super.getActivity(), courses);
			ListView listView = (ListView) super.getActivity().findViewById(R.id.scorecards_list);
			listView.setAdapter(scorecardListAdapter);
		}
	}

    @Override
    public void onParseComplete(CourseXMLReader parsed) {

        ArrayList<Activity> activities = parsed.getActivities(course.getCourseId());
        ArrayList<Activity> baseline = parsed.getBaselineActivities(course.getCourseId());
        ArrayList<QuizStats> stats = new ArrayList<QuizStats>();
        int pretestScore = -1, quizzesAttempted = 0, quizzesPassed = 0;

        String prefLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        Pattern quizDataPattern = Pattern.compile(QuizStats.fromCourseXMLRegex);
        Matcher matcher = quizDataPattern.matcher("");

        for (Activity act : activities){
            if (!act.getActType().equals("quiz")) continue;
            String contents = act.getContents(prefLang);
            matcher.reset(contents);

            if (matcher.find()){
                QuizStats quiz = new QuizStats();
                quiz.setQuizId(Integer.parseInt(matcher.group(1)));
                quiz.setPassThreshold(Integer.parseInt(matcher.group(2)));
                stats.add(quiz);
            }
        }

        quizStats.clear();
        quizStats.addAll(stats);

        if (quizStats.size() == 0){
            quizzesContainer.setVisibility(View.GONE);
            return;
        }

        DbHelper db = new DbHelper(super.getActivity());
        long userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
        db.getCourseQuizResults(quizStats, course.getCourseId(), userId);

        for (Activity baselineAct : baseline){
            if (!baselineAct.getActType().equals("quiz")) continue;
            String contents = baselineAct.getContents(prefLang);
            matcher.reset(contents);
            if (!matcher.find()) continue;

            for (QuizStats quiz : quizStats){
                int quizID = Integer.parseInt(matcher.group(1));
                //If is a baseline quiz, we remove it from the list
                if (quiz.getQuizId() == quizID ){
                    quizStats.remove(quiz);
                    pretestScore = quiz.getPercent();
                }
            }
        }

        for (QuizStats quiz : quizStats){
            if (quiz.isAttempted()){
                quizzesAttempted++;
                if (quiz.isPassed()) quizzesPassed++;
            }
        }

        highlightPretest.setText(pretestScore >= 0 ? (pretestScore + "%") : "-");
        highlightAttempted.setText("" + quizzesAttempted);
        highlightPassed.setText("" + quizzesPassed);
        quizzesAdapter.notifyDataSetChanged();

        quizzesProgressBar.setMax(quizStats.size());
        quizzesProgressBar.setProgress(quizzesPassed);
        quizzesProgressBar.setSecondaryProgress(quizzesAttempted);

        AlphaAnimation fadeInAnimation = new AlphaAnimation(0f, 1f);
        fadeInAnimation.setDuration(700);
        fadeInAnimation.setFillAfter(true);

        quizzesView.setVisibility(View.VISIBLE);
        quizzesView.startAnimation(fadeInAnimation);
    }

    @Override
    public void onStop(){
        super.onStop();
        if (xmlTask != null){
            xmlTask.setListener(null);
        }
    }

    @Override
    public void onParseError() {

    }
}
