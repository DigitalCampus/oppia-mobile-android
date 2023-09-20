package org.digitalcampus.oppia.adapter

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.digitalcampus.mobile.learning.BuildConfig
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowCourseListBinding
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.adapter.CoursesListAdapter.CourseListViewHolder
import org.digitalcampus.oppia.application.App
import org.digitalcampus.oppia.listener.CourseItemClickListener
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.utils.CircleTransform
import org.digitalcampus.oppia.utils.TextUtilsJava
import java.io.File
import java.util.Locale

class CoursesListAdapter(private val context: Context, private val courses: List<Course>) :
    RecyclerView.Adapter<CourseListViewHolder?>() {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private lateinit var itemClickListener: CourseItemClickListener
    private lateinit var contextMenuDialog: Dialog
    private var currentSelectedItem = 0
    private var selectedOption = 0

    init {
        createDialog()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseListViewHolder {
        val contactView = LayoutInflater.from(context).inflate(R.layout.row_course_list, parent, false)
        return CourseListViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: CourseListViewHolder, position: Int) {
        val course = getItemAtPosition(position)
        val lang = prefs.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().language)
        viewHolder.binding.courseTitle.text = course.getTitle(lang)
        val description = course.getDescription(lang)
        if (!TextUtilsJava.isEmpty(description) && prefs.getBoolean(PrefsActivity.PREF_SHOW_COURSE_DESC, BuildConfig.SHOW_COURSE_DESCRIPTION)) {
            viewHolder.binding.courseDescription.text = description
            viewHolder.binding.courseDescription.visibility = View.VISIBLE
        } else {
            viewHolder.binding.courseDescription.visibility = View.GONE
        }
        if (prefs.getBoolean(PrefsActivity.PREF_SHOW_PROGRESS_BAR, App.DEFAULT_DISPLAY_PROGRESS_BAR)) {
            val courseProgress = course.getProgressPercent().toInt()
            viewHolder.binding.circularProgressBar.visibility = View.VISIBLE
            viewHolder.binding.circularProgressBar.setProgressWithAnimation(courseProgress.toFloat(), 1000L)
        } else {
            viewHolder.binding.circularProgressBar.visibility = View.GONE
        }

        // set image
        if (course.imageFile != null) {
            val image = course.getImageFileFromRoot()
            Picasso.get().load(File(image))
                .placeholder(R.drawable.course_icon_placeholder)
                .transform(CircleTransform())
                .into(viewHolder.binding.courseImage)
        } else {
            viewHolder.binding.courseImage.setImageResource(R.drawable.course_icon_placeholder)
        }
        if (course.isToUpdate) {
            viewHolder.binding.imgSyncStatus.visibility = View.VISIBLE
            viewHolder.binding.imgSyncStatus.setImageResource(R.drawable.ic_action_refresh)
        } else if (course.isToDelete) {
            viewHolder.binding.imgSyncStatus.visibility = View.VISIBLE
            viewHolder.binding.imgSyncStatus.setImageResource(R.drawable.dialog_ic_action_delete)
        } else {
            viewHolder.binding.imgSyncStatus.visibility = View.GONE
        }
        viewHolder.binding.viewCourseStatus.setCourseStatus(course.status)
    }

    override fun getItemCount(): Int {
        return courses.size
    }

    fun getItemAtPosition(position: Int): Course {
        return courses[position]
    }

    inner class CourseListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: RowCourseListBinding

        init {
            binding = RowCourseListBinding.bind(itemView)
            itemView.setOnClickListener {
                itemClickListener.onItemClick(adapterPosition)
            }
            itemView.setOnLongClickListener {
                currentSelectedItem = adapterPosition
                selectedOption = -1
                contextMenuDialog.show()
                true
            }
        }
    }

    private fun createDialog() {
        contextMenuDialog = Dialog(context)
        contextMenuDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        contextMenuDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        contextMenuDialog.setContentView(R.layout.dialog_course_contextmenu)
        registerMenuClick(R.id.course_context_reset)
        registerMenuClick(R.id.course_context_delete)
        registerMenuClick(R.id.course_context_update_activity)

        //@Override
        contextMenuDialog.setOnDismissListener {
            itemClickListener.onContextMenuItemSelected(currentSelectedItem, selectedOption)
        }
    }

    private fun registerMenuClick(id: Int) {
        //@Override
        contextMenuDialog.findViewById<View>(id).setOnClickListener {
            selectedOption = id
            contextMenuDialog.dismiss()
        }
    }

    fun setOnItemClickListener(listener: CourseItemClickListener) {
        itemClickListener = listener
    }
}