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

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.activity.CourseActivity
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.analytics.Analytics.logException
import org.digitalcampus.oppia.application.App
import org.digitalcampus.oppia.application.Tracker
import org.digitalcampus.oppia.gamification.GamificationEngine
import org.digitalcampus.oppia.model.Activity
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.utils.MetaDataUtils
import org.json.JSONException
import org.json.JSONObject

class UrlWidget : BaseWidget() {

    companion object {
        val TAG = UrlWidget::class.simpleName

        @JvmStatic
        fun newInstance(activity: Activity, course: Course, isBaseline: Boolean): UrlWidget {
            val myFragment = UrlWidget()
            val args = Bundle()
            args.putSerializable(Activity.TAG, activity)
            args.putSerializable(Course.TAG, course)
            args.putBoolean(CourseActivity.BASELINE_TAG, isBaseline)
            myFragment.arguments = args
            return myFragment
        }
    }

    private var webview: WebView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        course = requireArguments().getSerializable(Course.TAG) as Course?
        activity = requireArguments().getSerializable(Activity.TAG) as Activity?
        setIsBaseline(requireArguments().getBoolean(CourseActivity.BASELINE_TAG))
        val vv = inflater.inflate(R.layout.widget_url, container, false)
        vv.id = activity?.actId ?: 0
        if (savedInstanceState?.getSerializable("widget_config") != null) {
            setWidgetConfig((savedInstanceState.getSerializable("widget_config") as HashMap<String, Any>))
        }
        return vv
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // show description if any
        val desc = activity?.getDescription(prefLang)
        val descTV = view?.findViewById<TextView>(R.id.widget_url_description)
        if (!desc.isNullOrEmpty()) {
            descTV?.text = desc
        } else {
            descTV?.visibility = View.GONE
        }
        webview = view?.findViewById(R.id.widget_url_webview)
        val defaultFontSize = prefs!!.getString(PrefsActivity.PREF_TEXT_SIZE, "16")!!.toInt()
        webview?.getSettings()?.defaultFontSize = defaultFontSize
        webview?.getSettings()?.javaScriptEnabled = true
        webview?.getSettings()?.allowFileAccess = true
        webview?.setWebViewClient(object : WebViewClient() {

            @Deprecated("(replace as soon as possible)")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }
        })
        webview?.loadUrl(activity?.getLocation(prefLang)!!)
    }

    override fun onTextSizeChanged(fontSize: Int) {
        webview!!.settings.defaultFontSize = fontSize
    }

    override fun getActivityCompleted(): Boolean {
        return false
    }

    override fun saveTracker() {
        val timetaken = getSpentTime()
        if (timetaken < App.URL_READ_TIME) {
            return
        }
        val t = Tracker(super.requireActivity())
        var obj = JSONObject()

        // add in extra meta-data
        try {
            val mdu = MetaDataUtils(super.requireActivity())
            obj.put("timetaken", timetaken)
            obj = mdu.getMetaData(obj)
            obj.put("lang", prefLang)
            val gamificationEngine = GamificationEngine(requireActivity())
            val gamificationEvent = gamificationEngine.processEventURLActivity(course, activity)

            // if it's a baseline activity then assume completed
            if (isBaseline) {
                t.saveTracker(course!!.courseId, activity?.digest!!, obj, true, gamificationEvent)
            } else {
                t.saveTracker(course!!.courseId, activity?.digest!!, obj, getActivityCompleted(), gamificationEvent)
            }
        } catch (jsone: JSONException) {
            Log.d(TAG, "Error generating json for url widget", jsone)
            logException(jsone)
        } catch (npe: NullPointerException) {
            Log.d(TAG, "Null pointer in generating json for url widget", npe)
            logException(npe)
        }
    }

    override fun getContentToRead(): String? {
        return null
    }

    override fun getWidgetConfig(): HashMap<String, Any>? {
        return null
    }

    override fun setWidgetConfig(config: HashMap<String, Any>) {
        // do nothing
    }
}