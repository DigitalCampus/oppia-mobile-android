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
package org.digitalcampus.oppia.model

import android.content.SharedPreferences
import android.util.Log
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.analytics.Analytics
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.util.Locale

open class MultiLangInfoModel : Serializable {

    companion object {
        val TAG = MultiLangInfoModel::class.simpleName
        const val DEFAULT_NOTITLE = "No title set"
    }

    private var langs: MutableList<Lang> = ArrayList()
    private var titles: MutableList<Lang> = ArrayList()
    private var descriptions: MutableList<Lang> = ArrayList()

    fun getTitle(lang: String?): String {
        val title = getInfo(lang, titles)
        return title?.trim() ?: DEFAULT_NOTITLE
    }

    fun getTitle(prefs: SharedPreferences): String {
        return this.getTitle(
            prefs.getString(
                PrefsActivity.PREF_CONTENT_LANGUAGE,
                Locale.getDefault().language
            )
        )
    }

    fun setTitles(titles: MutableList<Lang>) {
        this.titles = titles
    }

    fun setTitlesFromJSONString(jsonStr: String) {
        setInfoFromJSONString(jsonStr, titles, false)
    }

    fun getTitleJSONString(): String {
        return getInfoJSONString(titles)
    }

    fun getDescription(lang: String?): String? {
        return getInfo(lang, descriptions)
    }

    fun getDescription(prefs: SharedPreferences): String? {
        return this.getDescription(
            prefs.getString(
                PrefsActivity.PREF_CONTENT_LANGUAGE,
                Locale.getDefault().language
            )
        )
    }

    fun setDescriptions(descriptions: MutableList<Lang>) {
        this.descriptions = descriptions
    }

    fun setDescriptionsFromJSONString(jsonStr: String) {
        setInfoFromJSONString(jsonStr, descriptions, false)
    }

    fun getDescriptionJSONString(): String {
        return getInfoJSONString(descriptions)
    }

    fun getLangs(): List<Lang> {
        return langs
    }

    fun setLangs(langs: MutableList<Lang>) {
        this.langs = langs
    }

    fun getLangsJSONString(): String {
        return getInfoJSONString(langs)
    }

    fun setLangsFromJSONString(jsonStr: String) {
        setInfoFromJSONString(jsonStr, langs, true)
    }

    private fun getInfo(lang: String?, values: List<Lang>): String? {
        for ((language, content) in values) {
            if (language.equals(lang, ignoreCase = true)) {
                return content.trim()
            }
        }
        return if (values.isNotEmpty()) {
            values[0].content.trim()
        } else null
    }

    private fun getInfoJSONString(values: List<Lang>): String {
        val array = JSONArray()
        for ((language, content) in values) {
            val obj = JSONObject()
            try {
                obj.put(language, content)
            } catch (e: JSONException) {
                Analytics.logException(e)
                Log.d(TAG, "JSON error: ", e)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun setInfoFromJSONString(jsonStr: String, values: MutableList<Lang>, isLangs: Boolean) {
        try {
            val infoArray = JSONArray(jsonStr)
            for (i in 0 until infoArray.length()) {
                val infoObj = infoArray.getJSONObject(i)
                val iter = infoObj.keys()
                while (iter.hasNext()) {
                    val key = iter.next()
                    var info: String? = ""
                    if (!isLangs) {
                        info = infoObj.getString(key)
                    }
                    val l = Lang(key, info!!)
                    values.add(l)
                }
            }
        } catch (e: JSONException) {
            Analytics.logException(e)
            Log.d(TAG, "JSON error: ", e)
        } catch (npe: NullPointerException) {
            Analytics.logException(npe)
            Log.d(TAG, "Null pointer error: ", npe)
        }
    }

    @Throws(JSONException::class)
    fun setTitlesFromJSONObjectMap(jsonObjectMultilang: JSONObject) {
        val localLangs = parseLangs(jsonObjectMultilang)
        titles = localLangs
    }

    @Throws(JSONException::class)
    fun setDescriptionsFromJSONObjectMap(jsonObjectMultilang: JSONObject) {
        val localLangs = parseLangs(jsonObjectMultilang)
        descriptions = localLangs
    }

    @Throws(JSONException::class)
    private fun parseLangs(jsonObjectMultilang: JSONObject): MutableList<Lang> {
        val keys = jsonObjectMultilang.keys()
        val localLangs: MutableList<Lang> = ArrayList()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObjectMultilang.getString(key)
            if (!TextUtilsJava.isEmpty(value) && !TextUtilsJava.equals(value, "null")) {
                localLangs.add(Lang(key, value))
            }
        }
        return localLangs
    }
}