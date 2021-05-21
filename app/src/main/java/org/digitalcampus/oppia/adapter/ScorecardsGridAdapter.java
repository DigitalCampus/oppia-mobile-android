package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.RowScorecardBinding;
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
        viewHolder.binding.courseTitle.setText(course.getTitle(prefLang));

        int totalActivities = course.getNoActivities();
        int completedActivities = course.getNoActivitiesCompleted();

        viewHolder.binding.scorecardActivitiesCompleted.setText(String.valueOf(completedActivities));
        viewHolder.binding.scorecardActivitiesTotal.setText(String.valueOf(totalActivities));

        viewHolder.binding.cpbScorecard.setProgressMax(totalActivities);
        viewHolder.binding.cpbScorecard.setProgressWithAnimation(completedActivities, App.SCORECARD_ANIM_DURATION);

    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public Course getItemAtPosition(int position) {
        return courses.get(position);
    }

    class ScorecardsGridViewHolder extends RecyclerViewClickableAdapter.ViewHolder {

        private final RowScorecardBinding binding;

        ScorecardsGridViewHolder(final View itemView) {

            super(itemView);
            binding = RowScorecardBinding.bind(itemView);

        }

    }

}
 

