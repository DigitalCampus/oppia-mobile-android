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

import android.content.Context
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.utils.CryptoUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

open class User {

    companion object {
        const val API_KEY = "api_key"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val EMAIL = "email"
        const val ORGANISATION = "organisation"
        const val JOB_TITLE = "job_title"
        const val SCORING_ENABLED = "scoring"
        const val BADGING_ENABLED = "badging"
        const val POINTS = "points"
        const val BADGES = "badges"
        const val COHORTS = "cohorts"
    }

    var userId: Long = 0
    var username: String? = null
    var email: String? = null
    var password: String? = null
    var passwordAgain: String? = null
    var firstname: String? = null
    var lastname: String? = null
    var apiKey: String? = null
    var jobTitle: String? = null
    var organisation: String? = null
    var phoneNo: String? = null
    private var passwordEncrypted: String? = null
    var isScoringEnabled = true
    var isBadgingEnabled = true
    var points = 0
    var badges = 0
    var isOfflineRegister = false
    var cohorts: List<Int> = ArrayList()
    var userCustomFields: MutableMap<String, CustomValue> = HashMap()
    var isLocalUser = false

    fun getDisplayName(): String {
        return "$firstname $lastname"
    }

    fun getPasswordHashed(): String {
        return if (password != null) {
            CryptoUtils.encryptExternalPassword(password!!)
        } else ""
    }

    fun getPasswordEncrypted() : String? {
        if (passwordEncrypted == null) {
            passwordEncrypted = CryptoUtils.encryptLocalPassword(password!!)
        }
        return passwordEncrypted
    }

    fun setPasswordEncrypted(passwordEncrypted: String?) {
        this.passwordEncrypted = passwordEncrypted
    }

    fun getCustomField(key: String): CustomValue? {
        return userCustomFields[key]
    }

    fun putCustomField(key: String, value: CustomValue) {
        userCustomFields[key] = value
    }

    private fun setCustomFieldsFromJSON(ctx: Context, json: JSONObject) {
        val cFields = DbHelper.getInstance(ctx).customFields
        for (field in cFields) {
            val key = field.key
            if (json.has(key)) {
                if (field.isString) {
                    val value = json.getString(key)
                    putCustomField(key, CustomValue(StringValue(value)))
                } else if (field.isBoolean) {
                    val value = json.getBoolean(key)
                    putCustomField(key, CustomValue(BooleanValue(value)))
                } else if (field.isInteger) {
                    val value = json.getInt(key)
                    putCustomField(key, CustomValue(IntValue(value)))
                } else if (field.isFloat) {
                    val value = json.getDouble(key).toFloat()
                    putCustomField(key, CustomValue(FloatValue(value)))
                }
            }
        }
    }

    fun setCohortsFromJSONArray(cohortsJson: JSONArray) {
        val cohorts: MutableList<Int> = ArrayList()
        for (i in 0 until cohortsJson.length()) {
            cohorts.add(cohortsJson.getInt(i))
        }
        this.cohorts = cohorts
    }

    fun updateFromJSON(ctx: Context, json: JSONObject) {
        firstname = json.getString(FIRST_NAME)
        lastname = json.getString(LAST_NAME)
        if (json.has(API_KEY)) {
            apiKey = json.getString(API_KEY)
        }
        if (json.has(EMAIL)) {
            email = json.getString(EMAIL)
        }
        if (json.has(ORGANISATION)) {
            organisation = json.getString(ORGANISATION)
        }
        if (json.has(JOB_TITLE)) {
            jobTitle = json.getString(JOB_TITLE)
        }
        setCustomFieldsFromJSON(ctx, json)

        // Set user cohorts
        if (json.has(COHORTS)) {
            val cohortsJson = json.getJSONArray(COHORTS)
            setCohortsFromJSONArray(cohortsJson)
        }

        // Set badging and scoring data
        try {
            points = json.getInt(POINTS)
            badges = json.getInt(BADGES)
            isScoringEnabled = json.getBoolean(BADGING_ENABLED)
            isBadgingEnabled = json.getBoolean(BADGING_ENABLED)
        } catch (e: JSONException) {
            points = 0
            badges = 0
            isScoringEnabled = true
            isBadgingEnabled = true
        }
    }
}