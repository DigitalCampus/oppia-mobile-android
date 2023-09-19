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
package org.digitalcampus.oppia.application

import android.app.Activity
import android.content.SharedPreferences
import org.digitalcampus.mobile.learning.BuildConfig
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.fragments.PasswordDialogFragment
import org.digitalcampus.oppia.utils.TextUtilsJava
import javax.inject.Inject
import kotlin.Int
import kotlin.String

class AdminSecurityManager(var context: Activity) {

    companion object {
        val TAG = AdminSecurityManager::class.simpleName

        @JvmStatic
        fun with(context: Activity): AdminSecurityManager {
            return AdminSecurityManager(context)
        }
    }

    @JvmField
    @Inject
    var prefs: SharedPreferences? = null

    interface AuthListener {
        fun onPermissionGranted()
    }

    init {
        initializeDaggerBase()
    }

    private fun initializeDaggerBase() {
        val app = context.application as App
        app.component.inject(this)
    }

    private fun isAdminProtectionEnabled(): Boolean {
        return prefs?.getBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false) ?: false
    }

    fun checkAdminPermission(actionId: Int, authListener: AuthListener) {
        if (isActionProtected(actionId)) {
            promptAdminPassword(authListener)
        } else {
            //If the admin password is not needed, we simply call the listener method
            authListener.onPermissionGranted()
        }
    }

    fun promptAdminPassword(authListener: AuthListener?) {
        val passDialog = PasswordDialogFragment()
        passDialog.setListener(authListener)
        passDialog.show(context.fragmentManager, TAG)
    }

    fun isActionProtected(actionId: Int): Boolean {
        if (!isAdminProtectionEnabled()) return false

        // Only for automated testing. Only way I could "mock" BuildConfig fields
        return if (testForzeActionProtected()) {
            getTestActionProtectedValue()
        } else when (actionId) {
            R.id.course_context_delete -> BuildConfig.ADMIN_PROTECT_COURSE_DELETE
            R.id.course_context_reset -> BuildConfig.ADMIN_PROTECT_COURSE_RESET
            R.id.course_context_update_activity -> BuildConfig.ADMIN_PROTECT_COURSE_UPDATE
            R.id.menu_download -> BuildConfig.ADMIN_PROTECT_COURSE_INSTALL
            R.id.menu_settings -> BuildConfig.ADMIN_PROTECT_SETTINGS
            R.id.menu_sync -> BuildConfig.ADMIN_PROTECT_ACTIVITY_SYNC
            R.id.action_export_activity -> BuildConfig.ADMIN_PROTECT_ACTIVITY_EXPORT
            else -> false
        }
    }

    fun isPreferenceProtected(preferenceKey: String?): Boolean {
        if (TextUtilsJava.equals(preferenceKey, PrefsActivity.PREF_ADMIN_PROTECTION)
            || TextUtilsJava.equals(preferenceKey, PrefsActivity.PREF_ADMIN_PASSWORD)) {
            return true
        }
        if (!isAdminProtectionEnabled()) return false

        // Only for automated testing. Only way I could "mock" BuildConfig fields
        return if (testForzeActionProtected()) {
            getTestActionProtectedValue()
        } else when (preferenceKey) {
            PrefsActivity.PREF_SERVER -> BuildConfig.ADMIN_PROTECT_SERVER
            PrefsActivity.PREF_ADVANCED_SCREEN -> BuildConfig.ADMIN_PROTECT_ADVANCED_SETTINGS
            PrefsActivity.PREF_SECURITY_SCREEN -> BuildConfig.ADMIN_PROTECT_SECURITY_SETTINGS
            PrefsActivity.PREF_DISABLE_NOTIFICATIONS -> BuildConfig.ADMIN_PROTECT_NOTIFICATIONS
            PrefsActivity.PREF_COURSES_REMINDER_ENABLED -> BuildConfig.ADMIN_PROTECT_ENABLE_REMINDER_NOTIFICATIONS
            PrefsActivity.PREF_COURSES_REMINDER_INTERVAL -> BuildConfig.ADMIN_PROTECT_REMINDER_INTERVAL
            PrefsActivity.PREF_COURSES_REMINDER_DAYS -> BuildConfig.ADMIN_PROTECT_REMINDER_DAYS
            PrefsActivity.PREF_COURSES_REMINDER_TIME -> BuildConfig.ADMIN_PROTECT_REMINDER_TIME
            else -> false
        }
    }

    private fun testForzeActionProtected(): Boolean {
        val actionProtected = prefs?.getString(PrefsActivity.PREF_TEST_ACTION_PROTECTED, null)
        return actionProtected != null
    }

    private fun getTestActionProtectedValue(): Boolean {
        return prefs?.getBoolean(
            PrefsActivity.PREF_TEST_ACTION_PROTECTED,
            false
        ) ?: false
    }

    fun checkAdminPassword(pass: String?): kotlin.Boolean {
        if (!isAdminProtectionEnabled()) return false
        val adminPass = prefs?.getString(PrefsActivity.PREF_ADMIN_PASSWORD, "") ?: ""
        return pass != null && (adminPass.isEmpty() || adminPass == pass)
    }
}