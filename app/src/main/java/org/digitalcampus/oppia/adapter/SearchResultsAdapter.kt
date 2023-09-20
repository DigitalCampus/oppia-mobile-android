package org.digitalcampus.oppia.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import com.squareup.picasso.Picasso
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowSearchResultsBinding
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.adapter.SearchResultsAdapter.SearchResultsViewHolder
import org.digitalcampus.oppia.exception.ActivityNotFoundException
import org.digitalcampus.oppia.model.SearchResult
import java.io.File
import java.util.Locale

class SearchResultsAdapter(
    private val context: Context,
    private val searchResults: List<SearchResult>
) : RecyclerViewClickableAdapter<SearchResultsViewHolder>() {

    private val TAG = SearchResultsAdapter::class.simpleName
    private val prefLang: String?

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefLang = prefs.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().language)
    }

    inner class SearchResultsViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding: RowSearchResultsBinding = RowSearchResultsBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsViewHolder {
        val contactView = LayoutInflater.from(context).inflate(R.layout.row_search_results, parent, false)
        return SearchResultsViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: SearchResultsViewHolder, position: Int) {
        val sr = getItemAtPosition(position)
        val activity = sr.activity
        val course = sr.course
        val cTitle = course!!.getTitle(prefLang)
        val sTitle = sr.section!!.getTitle(prefLang)
        val aTitle = activity!!.getTitle(prefLang)
        viewHolder.binding.activityTitle.text = aTitle
        viewHolder.binding.sectionTitle.text = sTitle
        viewHolder.binding.courseTitle.text = cTitle
        Log.d(TAG, course.getLocation())
        viewHolder.binding.root.setTag(R.id.TAG_COURSE, course)
        viewHolder.binding.root.setTag(R.id.TAG_ACTIVITY_DIGEST, activity.digest)
        Log.d(TAG, activity.getImageFilePath(""))
        if (activity.hasCustomImage()) {
            val image = activity.getImageFilePath(course.getLocation())
            Log.d(TAG, if (File(image).exists()) "Exists" else "Noooo")
            Picasso.get().load(File(image)).into(viewHolder.binding.activityIcon)
        } else {
            val defaultActivityDrawable = activity.getDefaultResourceImage()
            viewHolder.binding.activityIcon.setImageResource(defaultActivityDrawable)
        }
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    fun getItemAtPosition(position: Int): SearchResult {
        return searchResults[position]
    }
}