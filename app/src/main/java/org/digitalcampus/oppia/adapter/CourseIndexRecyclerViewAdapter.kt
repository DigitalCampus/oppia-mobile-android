package org.digitalcampus.oppia.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.CourseTitleBarBinding
import org.digitalcampus.mobile.learning.databinding.RowCourseIndexSectionHeaderBinding
import org.digitalcampus.mobile.learning.databinding.RowCourseIndexSectionItemBinding
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.adapter.CourseIndexRecyclerViewAdapter.ChildViewHolder
import org.digitalcampus.oppia.adapter.CourseIndexRecyclerViewAdapter.SectionViewHolder
import org.digitalcampus.oppia.application.App
import org.digitalcampus.oppia.model.Activity
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.model.Section
import org.digitalcampus.oppia.utils.CircleTransform
import org.digitalcampus.oppia.utils.ui.ExpandableRecyclerView
import org.digitalcampus.oppia.utils.ui.ExpandableRecyclerView.GroupViewHolder
import java.io.File
import java.util.Locale

class CourseIndexRecyclerViewAdapter(
    context: Context,
    prefs: SharedPreferences,
    private val sectionList: List<Section>,
    course: Course
) : ExpandableRecyclerView.Adapter<ChildViewHolder, SectionViewHolder, CourseIndexRecyclerViewAdapter.HeaderViewHolder, Activity, Section>() {
    private val showSectionNumbers: Boolean
    private val highlightCompleted: Boolean
    private var prefLang: String?
    private val courseLocation: String
    private val courseTitle: String
    private val courseIcon: String
    private val highlightColor: Int
    private val normalColor: Int

    init {
        prefLang = prefs.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().language)
        showSectionNumbers = prefs.getBoolean(PrefsActivity.PREF_SHOW_SECTION_NOS, false)
        highlightCompleted = prefs.getBoolean(PrefsActivity.PREF_HIGHLIGHT_COMPLETED, App.DEFAULT_DISPLAY_COMPLETED)
        val startCollapsed = prefs.getBoolean(PrefsActivity.PREF_START_COURSEINDEX_COLLAPSED, false)
        startExpanded = !startCollapsed
        courseLocation = course.getLocation()
        highlightColor = ContextCompat.getColor(context, R.color.course_index_highlight)
        normalColor = ContextCompat.getColor(context, R.color.text_dark)
        courseTitle = course.getTitle(prefLang)
        courseIcon = course.getImageFileFromRoot()
        setHeaderVisible(true)
    }

    fun expandCollapseAllSections(expand: Boolean) {
        for (i in sectionList.indices) {
            if (expand) {
                expand(i)
            } else {
                collapse(i)
            }
        }
        notifyDataSetChanged()
    }

    fun reloadLanguage(prefs: SharedPreferences) {
        prefLang = prefs.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().language)
        super.notifyDataSetChanged()
    }

    override fun getGroupItemCount(): Int {
        return sectionList.size
    }

    override fun getChildItemCount(i: Int): Int {
        return sectionList[i].activities.size
    }

    override fun getGroupItem(i: Int): Section? {
        return if (i < sectionList.size) sectionList[i] else null
    }

    override fun getChildItem(group: Int, child: Int): Activity {
        return sectionList[group].activities[child]
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): HeaderViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.course_title_bar, parent, false)
        return HeaderViewHolder(rootView)
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup): SectionViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.row_course_index_section_header, parent, false)
        return SectionViewHolder(rootView)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.row_course_index_section_item, parent, false)
        return ChildViewHolder(rootView)
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder) {
        holder.binding.courseTitle.text = courseTitle
        Picasso.get().load(File(courseIcon))
            .placeholder(R.drawable.course_icon_placeholder)
            .error(R.drawable.course_icon_placeholder)
            .transform(CircleTransform())
            .into(holder.binding.courseIcon)
    }

    override fun onBindGroupViewHolder(holder: SectionViewHolder, group: Int) {
        super.onBindGroupViewHolder(holder, group)
        val section = getGroupItem(group)
        section?.let {
            var title = if (showSectionNumbers) section.order.toString() + ". " else ""
            title += section.getTitle(prefLang)
            holder.binding.title.text = title
            holder.binding.sectionIcon.visibility = if (section.hasCustomImage()) View.VISIBLE else View.GONE
            if (section.isProtectedByPassword) {
                holder.binding.lockIndicator.visibility = View.VISIBLE
                val iconId = if (section.isUnlocked) R.drawable.ic_unlock else R.drawable.ic_lock
                holder.binding.lockIndicator.tag = iconId // Needed for tests
                holder.binding.lockIndicator.setImageResource(iconId)
            } else {
                holder.binding.lockIndicator.visibility = View.GONE
            }
            if (section.hasCustomImage()) {
                val image = section.getImageFilePath(courseLocation)
                Picasso.get().load(File(image)).into(holder.binding.sectionIcon)
            }
            holder.binding.activitiesCompleted.text = section.getCompletedActivities().toString() + "/" + section.activities.size
        }
    }

    override fun onBindChildViewHolder(holder: ChildViewHolder, group: Int, position: Int) {
        super.onBindChildViewHolder(holder, group, position)
        val activity = getChildItem(group, position)
        val highlightActivity = highlightCompleted && activity.completed
        holder.binding.title.text = activity.getTitle(prefLang)
        holder.binding.title.setTextColor(if (highlightActivity) highlightColor else normalColor)
        holder.binding.badge.visibility = if (highlightActivity) View.VISIBLE else View.GONE
        if (activity.hasCustomImage()) {
            val image = activity.getImageFilePath(courseLocation)
            Picasso.get().load(File(image)).into(holder.binding.icon)
        } else {
            val defaultActivityDrawable = activity.getDefaultResourceImage()
            holder.binding.icon.setImageResource(defaultActivityDrawable)
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: CourseTitleBarBinding

        init {
            binding = CourseTitleBarBinding.bind(itemView)
        }
    }

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowCourseIndexSectionItemBinding

        init {
            binding = RowCourseIndexSectionItemBinding.bind(itemView)
        }
    }

    inner class SectionViewHolder(itemView: View) : GroupViewHolder(itemView) {
        val binding: RowCourseIndexSectionHeaderBinding

        init {
            binding = RowCourseIndexSectionHeaderBinding.bind(itemView)
            itemView.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}