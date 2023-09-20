package org.digitalcampus.oppia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowCourseDownloadBinding
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.adapter.DownloadCoursesAdapter.DownloadCoursesViewHolder
import org.digitalcampus.oppia.listener.OnItemClickListener
import org.digitalcampus.oppia.model.CourseInstallViewAdapter
import org.digitalcampus.oppia.utils.TextUtilsJava
import java.util.Locale

class DownloadCoursesAdapter(private val context: Context, private val courses: List<CourseInstallViewAdapter>)
    : MultiChoiceRecyclerViewAdapter<DownloadCoursesViewHolder>() {

    private var itemClickListener: OnItemClickListener? = null
    private val prefLang: String?
    private val updateDescription: String
    private val installDescription: String
    private val installedDescription: String
    private val cancelDescription: String

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefLang = prefs.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().language)
        updateDescription = context.getString(R.string.update)
        installDescription = context.getString(R.string.install)
        installedDescription = context.getString(R.string.installed)
        cancelDescription = context.getString(R.string.cancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadCoursesViewHolder {
        val contactView = LayoutInflater.from(context).inflate(R.layout.row_course_download, parent, false)
        return DownloadCoursesViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: DownloadCoursesViewHolder, position: Int) {
        updateViewHolder(viewHolder, position)
        val c = getItemAtPosition(position)
        viewHolder.binding.downloadCourseBtn.visibility = if (isMultiChoiceMode) View.INVISIBLE else View.VISIBLE
        updateCourseDescription(viewHolder, c)
        updateActionButton(viewHolder, c)
    }

    private fun updateCourseDescription(viewHolder: DownloadCoursesViewHolder, course: CourseInstallViewAdapter) {
        viewHolder.binding.courseTitle.text = course.getTitle(prefLang)
        viewHolder.binding.viewCourseStatus.setCourseStatus(course.status)
        val desc = course.getDescription(prefLang)
        if (desc != null) {
            viewHolder.binding.courseDescription.visibility = View.VISIBLE
            viewHolder.binding.courseDescription.text = desc
        } else {
            viewHolder.binding.courseDescription.visibility = View.GONE
        }
        val organisation = course.organisationName
        if (!TextUtilsJava.isEmpty(organisation) && !(course.isDownloading || course.isInstalling)) {
            viewHolder.binding.labelAuthor.visibility = View.VISIBLE
            viewHolder.binding.courseAuthor.visibility = View.VISIBLE
            viewHolder.binding.courseAuthor.text = organisation
        } else {
            viewHolder.binding.labelAuthor.visibility = View.GONE
            viewHolder.binding.courseAuthor.visibility = View.GONE
        }
    }

    private fun updateActionButton(viewHolder: DownloadCoursesViewHolder, course: CourseInstallViewAdapter) {
        val actionBtnImageRes: Int
        if (course.isDownloading || course.isInstalling) {
            actionBtnImageRes = R.drawable.ic_action_cancel
            viewHolder.binding.downloadCourseBtn.contentDescription = cancelDescription
            viewHolder.binding.downloadCourseBtn.isEnabled = !course.isInstalling
            viewHolder.binding.downloadProgress.visibility = View.VISIBLE
            if (course.progress > 0) {
                viewHolder.binding.downloadProgress.isIndeterminate = false
                viewHolder.binding.downloadProgress.progress = course.progress
            } else {
                viewHolder.binding.downloadProgress.isIndeterminate = true
            }
        } else {
            viewHolder.binding.downloadProgress.visibility = View.GONE
            if (course.isInstalled) {
                if (course.isToUpdate) {
                    actionBtnImageRes = R.drawable.ic_action_refresh
                    viewHolder.binding.downloadCourseBtn.contentDescription = updateDescription
                    viewHolder.binding.downloadCourseBtn.isEnabled = true
                } else {
                    actionBtnImageRes = R.drawable.ic_action_accept
                    viewHolder.binding.downloadCourseBtn.contentDescription = installedDescription
                    viewHolder.binding.downloadCourseBtn.isEnabled = false
                    viewHolder.binding.downloadCourseBtn.visibility = View.VISIBLE
                }
            } else {
                actionBtnImageRes = R.drawable.ic_action_download
                viewHolder.binding.downloadCourseBtn.contentDescription = installDescription
                viewHolder.binding.downloadCourseBtn.isEnabled = true
            }
        }
        viewHolder.binding.downloadCourseBtn.setImageResource(actionBtnImageRes)
        viewHolder.binding.downloadCourseBtn.tag = actionBtnImageRes
    }

    override fun getItemCount(): Int {
        return courses.size
    }

    fun getItemAtPosition(position: Int): CourseInstallViewAdapter {
        return courses[position]
    }

    inner class DownloadCoursesViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding: RowCourseDownloadBinding = RowCourseDownloadBinding.bind(itemView)

        init {
            binding.downloadCourseBtn.setOnClickListener { v: View? ->
                itemClickListener?.onItemClick(v, adapterPosition)
            }
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }
}