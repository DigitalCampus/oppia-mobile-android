package org.digitalcampus.oppia.model

import android.content.Context
import android.util.Log
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.activity.CourseIndexActivity
import org.digitalcampus.oppia.analytics.Analytics
import org.digitalcampus.oppia.exception.InvalidXMLException
import org.digitalcampus.oppia.task.ParseCourseXMLTask
import org.digitalcampus.oppia.task.ParseCourseXMLTask.OnParseXmlListener
import org.digitalcampus.oppia.utils.UIUtils
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader

class CompleteCourseProvider {

    companion object {
        val TAG = CompleteCourseProvider::class.simpleName
    }

    fun getCompleteCourseSync(ctx: Context, course: Course): CompleteCourse? {
        return try {
            val cxr = CourseXMLReader(course.courseXMLLocation, course.courseId.toLong(), ctx)
            cxr.parse(CourseXMLReader.ParseMode.COMPLETE)
            cxr.getParsedCourse()
        } catch (e: InvalidXMLException) {
            Analytics.logException(e)
            Log.d(TAG, "Error loading course XML: ", e)
            showErrorMessage(ctx)
            null
        }
    }

    fun getCompleteCourseAsync(ctx: Context?, course: Course?) {
        val task = ParseCourseXMLTask(ctx)
        task.setListener(ctx as OnParseXmlListener?)
        task.execute(course)
    }

    private fun showErrorMessage(ctx: Context) {
        UIUtils.showAlert(ctx, R.string.error, R.string.error_reading_xml) {
            (ctx as CourseIndexActivity).finish()
            true
        }
    }
}