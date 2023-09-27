/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */
package org.digitalcampus.oppia.widgets

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.activity.CourseActivity
import org.digitalcampus.oppia.analytics.Analytics.logException
import org.digitalcampus.oppia.application.App
import org.digitalcampus.oppia.application.Tracker
import org.digitalcampus.oppia.gamification.GamificationEngine
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate
import org.digitalcampus.oppia.model.Activity
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.utils.MetaDataUtils
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener.Companion.getIntentToOpenResource
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.Serializable

class ResourceWidget : BaseWidget() {

    companion object {
        val TAG = ResourceWidget::class.simpleName
        private const val PROPERTY_RESOURCE_VIEWING = "Resource_Viewing"
        private const val PROPERTY_RESOURCE_STARTTIME = "Resource_StartTime"
        private const val PROPERTY_RESOURCE_FILENAME = "Resource_FileName"
        private const val STR_WIDGET = "widget_"
        private const val STR_ACT_STARTTIME = "_Activity_StartTime"
        private const val STR_RESOURCE_VIEWING = "_Resource_Viewing"
        private const val STR_RESOURCE_STARTTIME = "_Resource_StartTime"
        private const val STR_RESOURCE_FILENAME = "_Resource_FileName"

        @JvmStatic
        fun newInstance(activity: Activity, course: Course, isBaseline: Boolean): ResourceWidget {
            val myFragment = ResourceWidget()
            val args = Bundle()
            args.putSerializable(Activity.TAG, activity)
            args.putSerializable(Course.TAG, course)
            args.putBoolean(CourseActivity.BASELINE_TAG, isBaseline)
            myFragment.arguments = args
            return myFragment
        }
    }

    private var isResourceViewing = false
    private var resourceStartTime: Long = 0
    private var resourceFileName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        course = requireArguments().getSerializable(Course.TAG) as Course?
        activity = requireArguments().getSerializable(Activity.TAG) as Activity?
        setIsBaseline(requireArguments().getBoolean(CourseActivity.BASELINE_TAG))
        val vv = inflater.inflate(R.layout.widget_resource, container, false)
        vv.id = activity?.actId ?: 0
        if (savedInstanceState?.getSerializable(WIDGET_CONFIG) != null) {
            setWidgetConfig((savedInstanceState.getSerializable(WIDGET_CONFIG) as HashMap<String, Any>))
        }
        return vv
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(WIDGET_CONFIG, getWidgetConfig() as Serializable)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val ll = view?.findViewById<LinearLayout>(R.id.widget_resource_object)
        val fileUrl = course!!.getLocation() + activity?.getLocation(prefLang)

        // show description if any
        val desc = activity?.getDescription(prefLang)
        val descTV = view?.findViewById<TextView>(R.id.widget_resource_description)
        if (!desc.isNullOrEmpty()) {
            descTV?.text = desc
        } else {
            descTV?.visibility = View.GONE
        }
        val file = File(fileUrl)
        resourceFileName = file.name
        val orcl = OnResourceClickListener(super.requireActivity())
        // show image files
        if (activity?.mimeType == "image/jpeg" || activity?.mimeType == "image/png") {
            val iv = ImageView(super.requireActivity())
            val myBitmap = BitmapFactory.decodeFile(fileUrl)
            iv.setImageBitmap(myBitmap)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            ll?.addView(iv, lp)
            iv.tag = file
            iv.setOnClickListener(orcl)
        } else {
            // add button to open other filetypes in whatever app the user has installed as default for that filetype
            val btn = Button(super.requireActivity())
            btn.text = super.requireActivity().getString(R.string.widget_resource_open_file, file.name)
            btn.setTextAppearance(super.requireActivity(), R.style.ButtonText)
            ll?.addView(btn)
            btn.tag = file
            btn.setOnClickListener(orcl)
        }
    }

    override fun onPause() {
        super.onPause()
        val editor = prefs!!.edit()
        editor.putLong(STR_WIDGET + activity?.digest + STR_ACT_STARTTIME, getStartTime())
        editor.putBoolean(STR_WIDGET + activity?.digest + STR_RESOURCE_VIEWING, isResourceViewing)
        editor.putLong(STR_WIDGET + activity?.digest + STR_RESOURCE_STARTTIME, resourceStartTime)
        editor.putString(STR_WIDGET + activity?.digest + STR_RESOURCE_FILENAME, resourceFileName)
        editor.apply()
    }

    override fun onResume() {
        super.onResume()
        // check to see if the vars are stored in shared prefs
        if (prefs!!.contains(STR_WIDGET + activity?.digest + STR_ACT_STARTTIME)) {
            setStartTime(prefs!!.getLong(STR_WIDGET + activity?.digest + STR_ACT_STARTTIME, System.currentTimeMillis() / 1000))
        }
        if (prefs!!.contains(STR_WIDGET + activity?.digest + STR_RESOURCE_VIEWING)) {
            isResourceViewing = prefs!!.getBoolean(STR_WIDGET + activity?.digest + STR_RESOURCE_VIEWING, false)
        }
        if (prefs!!.contains(STR_WIDGET + activity?.digest + STR_RESOURCE_STARTTIME)) {
            resourceStartTime = prefs!!.getLong(STR_WIDGET + activity?.digest + STR_RESOURCE_STARTTIME, System.currentTimeMillis() / 1000)
        }
        if (prefs!!.contains(STR_WIDGET + activity?.digest + STR_RESOURCE_FILENAME)) {
            resourceFileName = prefs!!.getString(STR_WIDGET + activity?.digest + STR_RESOURCE_FILENAME, "")
        }
        if (isResourceViewing) {
            resourceStopped()
        }
        // clear the shared prefs
        val editor = prefs!!.edit()
        val keys = prefs!!.all
        for ((key) in keys) {
            if (key.startsWith(STR_WIDGET + activity?.digest)) {
                editor.remove(key)
            }
        }
        editor.apply()
    }

    private fun resourceStopped() {
        if (isResourceViewing) {
            val resourceEndTime = System.currentTimeMillis() / 1000
            val timeTaken = resourceEndTime - resourceStartTime
            isResourceViewing = false
            // track that the resource has been viewed (or at least clicked on)
            val t = Tracker(super.requireActivity())
            var data = JSONObject()
            try {
                data.put("resource", "viewed")
                data.put("resourcefile", resourceFileName)
                data.put("timetaken", timeTaken)
                data.put("lang", prefLang)
            } catch (e: JSONException) {
                logException(e)
                Log.d(TAG, "JSONException", e)
            }

            // add in extra meta-data
            val mdu = MetaDataUtils(super.requireActivity())
            data = mdu.getMetaData(data)
            val gamificationEngine = GamificationEngine(requireActivity())
            val gamificationEvent = gamificationEngine.processEventResourceStoppedActivity()
            t.saveTracker(course!!.courseId, activity?.digest!!, data, true, gamificationEvent)
        }
    }

    override fun getActivityCompleted(): Boolean {
        return true
    }

    override fun saveTracker() {
        val timetaken = getSpentTime()
        if (activity == null || timetaken < App.RESOURCE_READ_TIME) {
            return
        }
        GamificationServiceDelegate(requireActivity())
            .createActivityIntent(course!!, activity, getActivityCompleted(), isBaseline)
            .registerResourceEvent(timetaken)
    }

    override fun getWidgetConfig(): HashMap<String, Any> {
        val config = HashMap<String, Any>()
        config[PROPERTY_ACTIVITY_STARTTIME] = getStartTime()
        config[PROPERTY_RESOURCE_VIEWING] = isResourceViewing
        config[PROPERTY_RESOURCE_STARTTIME] = resourceStartTime
        config[PROPERTY_RESOURCE_FILENAME] = resourceFileName!!
        return config
    }

    override fun setWidgetConfig(config: HashMap<String, Any>) {
        if (config.containsKey(PROPERTY_ACTIVITY_STARTTIME)) {
            setStartTime((config[PROPERTY_ACTIVITY_STARTTIME] as Long?)!!)
        }
        if (config.containsKey(PROPERTY_RESOURCE_VIEWING)) {
            isResourceViewing = (config[PROPERTY_RESOURCE_VIEWING] as Boolean?)!!
        }
        if (config.containsKey(PROPERTY_RESOURCE_STARTTIME)) {
            resourceStartTime = (config[PROPERTY_RESOURCE_STARTTIME] as Long?)!!
        }
        if (config.containsKey(PROPERTY_RESOURCE_FILENAME)) {
            resourceFileName = config[PROPERTY_RESOURCE_FILENAME] as String?
        }
    }

    override fun getContentToRead(): String? {
        return null
    }

    private inner class OnResourceClickListener(private val ctx: Context?) : View.OnClickListener {
        override fun onClick(v: View) {
            val file = v.tag as File
            // check the file is on the file system (should be but just in case)
            if (!file.exists()) {
                Toast.makeText(ctx, ctx!!.getString(R.string.error_resource_not_found, file.name), Toast.LENGTH_LONG).show()
                return
            }
            val intent = getIntentToOpenResource(ctx!!, file)
            if (intent != null) {
                isResourceViewing = true
                resourceStartTime = System.currentTimeMillis() / 1000
                ctx.startActivity(intent)
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.error_resource_app_not_found, file.name), Toast.LENGTH_LONG).show()
            }
        }
    }
}