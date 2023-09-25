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

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.text.HtmlCompat
import com.google.android.material.snackbar.Snackbar
import org.digitalcampus.mobile.learning.BuildConfig
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.activity.CourseActivity
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.application.App
import org.digitalcampus.oppia.application.SessionManager.getUsername
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate
import org.digitalcampus.oppia.listener.OnInputEnteredListener
import org.digitalcampus.oppia.model.Activity
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.model.Media
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener.Companion.getIntentToInstallAppForResource
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener.Companion.getIntentToOpenResource
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener.Companion.getResourcesFromContent
import org.digitalcampus.oppia.utils.resources.JSInterface
import org.digitalcampus.oppia.utils.resources.JSInterfaceForBackwardsCompat
import org.digitalcampus.oppia.utils.resources.JSInterfaceForInlineInput
import org.digitalcampus.oppia.utils.resources.JSInterfaceForResourceImages
import org.digitalcampus.oppia.utils.storage.FileUtils.readFile
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Serializable

class PageWidget : BaseWidget(), OnInputEnteredListener {

    companion object {
        val TAG = PageWidget::class.simpleName
        private const val VIDEO_SUBPATH = "/video/"
        private const val RESOURCE_SUBPATH = "/resources/"

        @JvmStatic
        fun newInstance(activity: Activity, course: Course, isBaseline: Boolean): PageWidget {
            val myFragment = PageWidget()
            val args = Bundle()
            args.putSerializable(Activity.TAG, activity)
            args.putSerializable(Course.TAG, course)
            args.putBoolean(CourseActivity.BASELINE_TAG, isBaseline)
            myFragment.arguments = args
            return myFragment
        }
    }

    private var webview: WebView? = null
    private val jsInterfaces: MutableList<JSInterface> = ArrayList()
    private val inlineInput: MutableList<String?> = ArrayList()
    private var pageResources: List<String> = ArrayList()
    private val openedResources: MutableList<String?> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vv = inflater.inflate(R.layout.fragment_webview, container, false)
        activity = requireArguments().getSerializable(Activity.TAG) as Activity?
        course = requireArguments().getSerializable(Course.TAG) as Course?
        isBaseline = requireArguments().getBoolean(CourseActivity.BASELINE_TAG)
        vv.id = activity?.actId ?: 0
        if (savedInstanceState?.getSerializable(WIDGET_CONFIG) != null) {
            setWidgetConfig((savedInstanceState.getSerializable(WIDGET_CONFIG) as HashMap<String, Any>))
        }
        return vv
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(WIDGET_CONFIG, getWidgetConfig() as Serializable)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // get the location data
        val url = course!!.getLocation() + activity?.getLocation(prefLang)
        val defaultFontSize = prefs!!.getString(PrefsActivity.PREF_TEXT_SIZE, "16")!!.toInt()
        webview = super.requireActivity().findViewById(activity?.actId!!)
        webview?.settings?.defaultFontSize = defaultFontSize
        webview?.settings?.allowFileAccess = true
        try {
            val contents = readFile(url)
            pageResources = getResourcesFromContent(contents)
            webview?.settings?.javaScriptEnabled = true
            val backwardsCompatJSInterface = JSInterfaceForBackwardsCompat(requireContext())
            jsInterfaces.add(backwardsCompatJSInterface)
            webview?.addJavascriptInterface(
                backwardsCompatJSInterface,
                backwardsCompatJSInterface.getInterfaceExposedName()
            )

            //We inject the interface to launch intents from the HTML
            val imagesJSInterface = JSInterfaceForResourceImages(requireContext(), course!!.getLocation())
            jsInterfaces.add(imagesJSInterface)
            webview?.addJavascriptInterface(imagesJSInterface, imagesJSInterface.getInterfaceExposedName())
            val inputJSInterface = JSInterfaceForInlineInput(requireContext())
            inputJSInterface.setOnInputEnteredListener(this)
            jsInterfaces.add(inputJSInterface)
            webview?.addJavascriptInterface(inputJSInterface, inputJSInterface.getInterfaceExposedName())
            webview?.loadDataWithBaseURL(
                "file://" + course!!.getLocation() + File.separator,
                contents,
                "text/html",
                "utf-8",
                null
            )
        } catch (e: IOException) {
            webview?.loadUrl("file://$url")
        }
        webview?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                //We execute the necessary JS code to bind our JavascriptInterfaces
                for (jsInterface in jsInterfaces) {
                    view.loadUrl(jsInterface.getJavascriptInjection())
                }
            }

            // set up the page to intercept videos
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return handleUrl(request.url.path)
            }

            private fun handleUrl(url: String?): Boolean {
                if (url!!.contains(VIDEO_SUBPATH)) {
                    // extract video name from url
                    val startPos = url.indexOf(VIDEO_SUBPATH) + VIDEO_SUBPATH.length
                    val mediaFileName = url.substring(startPos)
                    startMediaPlayerWithFile(mediaFileName)
                    return true
                } else if (url.contains(RESOURCE_SUBPATH)) {
                    openResourceExternally(url)
                }
                return false
            }
        }
    }

    override fun setIsBaseline(isBaseline: Boolean) {
        this.isBaseline = isBaseline
    }

    override fun onTextSizeChanged(fontSize: Int) {
        webview!!.settings.defaultFontSize = fontSize
    }

    private fun openResourceExternally(fileUrl: String?) {
        val fileToOpen = File(fileUrl)
        try {
            val intent = getIntentToOpenResource(requireContext(), fileToOpen)
            if (intent != null) {
                requireActivity().startActivity(intent)
                openedResources.add(fileToOpen.name)
            } else {
                showResourceOpenerNotFoundMessage(fileToOpen)
            }
        } catch (anfe: ActivityNotFoundException) {
            Log.d(TAG, "Activity not found", anfe)
        }
    }

    private fun showResourceOpenerNotFoundMessage(fileToOpen: File) {
        val filename = fileToOpen.name
        val installAppIntent = getIntentToInstallAppForResource(requireContext(), fileToOpen)
        val snackbar = Snackbar.make(
            requireView(),
            requireContext().getString(R.string.external_resource_app_missing, filename),
            Snackbar.LENGTH_LONG
        )
        snackbar.show()
        snackbar.setActionTextColor(Color.WHITE)
        if (installAppIntent != null) {
            snackbar.setAction(R.string.external_resource_install_app) {
                requireActivity().startActivity(installAppIntent)
            }
        }
    }

    fun activityMediaCompleted(): Boolean {
        // only show as being complete if all the videos on this page have been played
        return if (activity?.hasMedia() == true) {
            val mediaList = activity?.media as ArrayList<Media>
            var completed = true
            val db = DbHelper.getInstance(super.requireActivity())
            val userId = db.getUserId(getUsername(requireActivity()))
            for (m in mediaList) {
                if (!db.activityCompleted(course!!.courseId, m.digest, userId)) {
                    completed = false
                }
            }
            completed
        } else {
            true
        }
    }

    override fun getActivityCompleted(): Boolean {
        if (!activityMediaCompleted()) {
            return false
        }
        if (pageResources.isNotEmpty() && BuildConfig.PAGE_COMPLETION_VIEW_FILE) {
            //If there are files, they have to be opened to mark the activity has completed
            for (resource in pageResources) {
                if (!openedResources.contains(resource)) {
                    return false
                }
            }
        }
        val timetaken = getSpentTime()
        if (BuildConfig.PAGE_COMPLETED_METHOD == App.PAGE_COMPLETED_METHOD_TIME_SPENT) {
            Log.d(TAG, "Time spent: " + timetaken + " | Min time (fixed): " + BuildConfig.PAGE_COMPLETED_TIME_SPENT)
            return timetaken > BuildConfig.PAGE_COMPLETED_TIME_SPENT
        }
        if (BuildConfig.PAGE_COMPLETED_METHOD == App.PAGE_COMPLETED_METHOD_WPM) {
            val a = DbHelper.getInstance(context).getActivityByDigest(activity?.digest)
            val minTimeToRead = a.wordCount * 60 / BuildConfig.PAGE_COMPLETED_WPM
            Log.d(TAG, "Time spent: $timetaken | Min time (WPM): $minTimeToRead")
            return timetaken > minTimeToRead
        }
        return timetaken > App.PAGE_READ_TIME
    }

    override fun saveTracker() {
        if (activity == null) {
            return
        }
        val timetaken = getSpentTime()
        // only save tracker if over the time
        if (timetaken < App.PAGE_READ_TIME) {
            return
        }
        val delegate = GamificationServiceDelegate(requireActivity()).createActivityIntent(course!!, activity, getActivityCompleted(), isBaseline)
        if (inlineInput.isNotEmpty()) {
            delegate.addExtraEventData("inline_input", TextUtilsJava.join(",", inlineInput))
        }
        if (openedResources.isNotEmpty()) {
            delegate.addExtraEventData("opened_resources", TextUtilsJava.join(",", openedResources))
        }
        delegate.registerPageActivityEvent(timetaken, readAloud)
    }

    override fun setWidgetConfig(config: HashMap<String, Any>) {
        if (config.containsKey(PROPERTY_ACTIVITY_STARTTIME)) {
            setStartTime((config[PROPERTY_ACTIVITY_STARTTIME] as Long?)!!)
        }
        if (config.containsKey(PROPERTY_ACTIVITY)) {
            activity = config[PROPERTY_ACTIVITY] as Activity?
        }
        if (config.containsKey(PROPERTY_COURSE)) {
            course = config[PROPERTY_COURSE] as Course?
        }
    }

    override fun getWidgetConfig(): HashMap<String, Any> {
        val config = HashMap<String, Any>()
        config[PROPERTY_ACTIVITY_STARTTIME] = getStartTime()
        config[PROPERTY_ACTIVITY] = activity ?: "" // TODO KOTLIN: Review this is working
        config[PROPERTY_COURSE] = course!!
        return config
    }

    override fun getContentToRead(): String {
        val f = File(File.separator + course!!.getLocation() + File.separator + activity?.getLocation(prefLang))
        val text = StringBuilder()
        try {
            BufferedReader(FileReader(f)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    text.append(line)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "getContentToRead: ", e)
            return ""
        }
        return HtmlCompat.fromHtml(text.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }

    override fun inlineInputReceived(input: String?) {
        if (!inlineInput.contains(input)) {
            inlineInput.add(input)
        }
    }
}