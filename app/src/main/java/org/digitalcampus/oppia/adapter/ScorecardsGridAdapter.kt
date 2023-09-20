package org.digitalcampus.oppia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowScorecardBinding
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.adapter.ScorecardsGridAdapter.ScorecardsGridViewHolder
import org.digitalcampus.oppia.application.App
import org.digitalcampus.oppia.model.Course
import java.util.Locale

class ScorecardsGridAdapter(private val context: Context, private val courses: List<Course>) :
    RecyclerViewClickableAdapter<ScorecardsGridViewHolder>() {

    private val prefLang: String?

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefLang = prefs.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().language)
    }

    inner class ScorecardsGridViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding: RowScorecardBinding = RowScorecardBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScorecardsGridViewHolder {
        val contactView = LayoutInflater.from(context).inflate(R.layout.row_scorecard, parent, false)
        return ScorecardsGridViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: ScorecardsGridViewHolder, position: Int) {
        val course = getItemAtPosition(position)
        viewHolder.binding.courseTitle.text = course.getTitle(prefLang)
        val totalActivities = course.noActivities
        val completedActivities = course.noActivitiesCompleted
        viewHolder.binding.scorecardActivitiesCompleted.text = completedActivities.toString()
        viewHolder.binding.scorecardActivitiesTotal.text = totalActivities.toString()
        viewHolder.binding.cpbScorecard.progressMax = totalActivities.toFloat()
        viewHolder.binding.cpbScorecard.setProgressWithAnimation(completedActivities.toFloat(), App.SCORECARD_ANIM_DURATION)
    }

    override fun getItemCount(): Int {
        return courses.size
    }

    fun getItemAtPosition(position: Int): Course {
        return courses[position]
    }
}