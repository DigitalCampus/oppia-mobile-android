package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.model.Course;

import java.util.List;
import java.util.Locale;

import androidx.preference.PreferenceManager;

public class ScorecardsGridAdapter extends RecyclerViewClickableAdapter<ScorecardsGridAdapter.ScorecardsGridViewHolder> {


    private final String prefLang;
    private List<Course> courses;
    private Context context;

    public ScorecardsGridAdapter(Context context, List<Course> courses) {
        this.context = context;
        this.courses = courses;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
    }

    @Override
    public ScorecardsGridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_scorecard, parent, false);
        // Return a new holder instance
        return new ScorecardsGridViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(final ScorecardsGridViewHolder viewHolder, final int position) {

        final Course course = getItemAtPosition(position);
        viewHolder.courseTitle.setText(course.getTitle(prefLang));

        int totalActivities = course.getNoActivities();
        int completedActivities = course.getNoActivitiesCompleted();

        viewHolder.activitiesCompleted.setText(String.valueOf(completedActivities));
        viewHolder.activitiesTotal.setText(String.valueOf(totalActivities));

        viewHolder.circularProgressBar.setProgressMax(totalActivities);
        viewHolder.circularProgressBar.setProgressWithAnimation(completedActivities, App.SCORECARD_ANIM_DURATION);

    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public Course getItemAtPosition(int position) {
        return courses.get(position);
    }

    class ScorecardsGridViewHolder extends RecyclerViewClickableAdapter.ViewHolder {

        private TextView courseTitle;
        private TextView activitiesCompleted;
        private TextView activitiesTotal;
        private CircularProgressBar circularProgressBar;

        ScorecardsGridViewHolder(final View itemView) {

            super(itemView);
            courseTitle = itemView.findViewById(R.id.course_title);
            circularProgressBar = itemView.findViewById(R.id.cpb_scorecard);
            activitiesCompleted = itemView.findViewById(R.id.scorecard_activities_completed);
            activitiesTotal = itemView.findViewById(R.id.scorecard_activities_total);

        }

    }

}
 

