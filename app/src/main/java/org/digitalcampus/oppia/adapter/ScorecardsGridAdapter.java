package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;

import java.util.List;
import java.util.Locale;

public class ScorecardsGridAdapter extends RecyclerView.Adapter<ScorecardsGridAdapter.ViewHolder> {


    private final String prefLang;
    private List<Course> courses;
    private Context context;
    private OnItemClickListener itemClickListener;


    public ScorecardsGridAdapter(Context context, List<Course> courses) {
        this.context = context;
        this.courses = courses;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.scorecard_list_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        final Course course = getItemAtPosition(position);

        viewHolder.courseTitle.setText(course.getTitle(prefLang));

        int totalActivities = course.getNoActivities();
        int completedActivities = course.getNoActivitiesCompleted();

        viewHolder.activitiesCompleted.setText(String.valueOf(completedActivities));
        viewHolder.activitiesTotal.setText(String.valueOf(totalActivities));

        viewHolder.circularProgressBar.setProgressMax(totalActivities);
        viewHolder.circularProgressBar.setProgressWithAnimation(completedActivities, MobileLearning.SCORECARD_ANIM_DURATION);

    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public Course getItemAtPosition(int position) {
        return courses.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        TextView courseTitle;
        TextView activitiesCompleted;
        TextView activitiesTotal;
        CircularProgressBar circularProgressBar;

        public ViewHolder(final View itemView) {

            super(itemView);

            courseTitle = itemView.findViewById(R.id.course_title);
            circularProgressBar = itemView.findViewById(R.id.cpb_scorecard);
            activitiesCompleted = itemView.findViewById(R.id.scorecard_activities_completed);
            activitiesTotal = itemView.findViewById(R.id.scorecard_activities_total);

            rootView = itemView;

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(v, getAdapterPosition());
                    }
                }
            });
        }

    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
 

