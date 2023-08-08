package org.digitalcampus.oppia.utils.course_status

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.ViewStatusBadgeBinding
import org.digitalcampus.oppia.model.Course

class CourseStatusBadgeView : FrameLayout {
    private val courseStatusBadgeDataMap: Map<String, CourseStatusBadgeData> = object : HashMap<String, CourseStatusBadgeData>() {
        init {
            put(Course.STATUS_DRAFT, CourseStatusBadgeData(
                    R.string.status_draft, R.drawable.ic_status_draft, R.color.bg_badge_status_draft))
            put(Course.STATUS_NEW_DOWNLOADS_DISABLED, CourseStatusBadgeData(
                    R.string.status_new_download_disabled, R.drawable.ic_new_download_disabled, R.color.bg_badge_status_new_download_disabled))
            put(Course.STATUS_ARCHIVED, CourseStatusBadgeData(
                    R.string.status_archived, R.drawable.ic_status_archived, R.color.bg_badge_status_archived))
            put(Course.STATUS_READ_ONLY, CourseStatusBadgeData(
                    R.string.status_read_only, R.drawable.ic_status_read_only, R.color.bg_badge_status_read_only))
        }
    }
    private lateinit var binding: ViewStatusBadgeBinding

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        binding = ViewStatusBadgeBinding.inflate(LayoutInflater.from(context))
        addView(binding.root)
        visibility = GONE
    }

    fun setCourseStatus(status: String) {
        if (Course.STATUS_LIVE == status || Course.STATUS_NEW_DOWNLOADS_DISABLED == status) {
            visibility = GONE
            return
        }
        require(courseStatusBadgeDataMap.containsKey(status)) { "Invalid status: $status" }
        visibility = VISIBLE
        val courseStatusBadgeData = courseStatusBadgeDataMap[status]
        binding.root.background.setColorFilter(
                ContextCompat.getColor(context, courseStatusBadgeData!!.color), PorterDuff.Mode.SRC_ATOP)
        binding.imgStatusBadge.setImageResource(courseStatusBadgeData.icon)
        binding.tvStatusBadge.setText(courseStatusBadgeData.text)
    }

    val text: String
        get() = binding.tvStatusBadge.text.toString()
}