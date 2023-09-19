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

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.ViewPermissionsExplanationBinding
import java.util.Arrays

object PermissionsManager {

    val TAG = PermissionsManager::class.simpleName
    private const val PERMISSIONS_REQUEST = 1246
    
    private val STARTUP_PERMISSIONS_REQUIRED = listOf<String>()
    private val BLUETOOTH_PERMISSIONS_REQUIRED = listOf( //Remember to update this when the Manifest permissions change!
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    private val BLUETOOTH_API_31_PERMISSIONS_REQUIRED = listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE
    )
    private val STORAGE_PERMISSIONS_REQUIRED = listOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @JvmField
    val NOTIFICATIONS_PERMISSIONS_REQUIRED = listOf(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @JvmField
    val OFFLINE_COURSE_IMPORT_PERMISSIONS_REQUIRED = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    const val STARTUP_PERMISSIONS = 1
    const val BLUETOOTH_PERMISSIONS = 2
    const val STORAGE_PERMISSIONS = 3
    private const val NOTIFICATIONS_PERMISSIONS = 4
    const val BLUETOOTH_API_31_PERMISSIONS = 5

    private fun isFirstTimeAsked(prefs: SharedPreferences, permission: String): Boolean {
        return !prefs.getBoolean(permission + "_asked", false)
    }

    private fun setAsked(prefs: SharedPreferences, permission: String) {
        prefs.edit().putBoolean(permission + "_asked", true).apply()
    }

    private fun isPermissionDeniedOnce(prefs: SharedPreferences, permission: String): Boolean {
        return prefs.getBoolean(permission + "_denied", false)
    }

    private fun setDenied(prefs: SharedPreferences, permission: String) {
        prefs.edit().putBoolean(permission + "_denied", true).apply()
    }

    @JvmStatic
    fun checkPermissionsAndInform(act: Activity, perms: Int): Boolean {
        val container = act.findViewById<ViewGroup>(R.id.permissions_explanation)
        return checkPermissionsAndInform(act, perms, container)
    }

    @JvmStatic
    fun checkPermissionsAndInform(act: Activity, perms: Int, container: ViewGroup): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //If sdk version prior to 23 (Android M), the permissions are granted by manifest
            return true
        }
        var permissions: List<String> = ArrayList()
        when (perms) {
            STARTUP_PERMISSIONS -> permissions = STARTUP_PERMISSIONS_REQUIRED
            BLUETOOTH_PERMISSIONS -> permissions = BLUETOOTH_PERMISSIONS_REQUIRED
            BLUETOOTH_API_31_PERMISSIONS -> permissions = BLUETOOTH_API_31_PERMISSIONS_REQUIRED
            STORAGE_PERMISSIONS -> permissions = STORAGE_PERMISSIONS_REQUIRED
            NOTIFICATIONS_PERMISSIONS -> permissions = NOTIFICATIONS_PERMISSIONS_REQUIRED
        }
        val permissionsToAsk = filterNotGrantedPermissions(act, permissions)
        if (permissionsToAsk.isNotEmpty()) {
            //Show the permissions informative view
            val layoutInflater = LayoutInflater.from(act)
            container.removeAllViews()
            val bindingExplanation =
                ViewPermissionsExplanationBinding.inflate(layoutInflater, container, true)
            showPermissionDescriptions(act, bindingExplanation.root, permissionsToAsk)
            container.visibility = View.VISIBLE

            //Check the user has not selected the "Don't ask again" option for any permission yet
            if (canAskForAllPermissions(act, permissionsToAsk)) {
                //First, set the permissions as asked
                bindingExplanation.btnPermissions.visibility = View.VISIBLE
                bindingExplanation.notAskableDescription.visibility = View.GONE
                bindingExplanation.btnPermissions.setOnClickListener {    //Open the dialog to ask for permissions
                    requestPermissions(act, permissionsToAsk)
                }
            } else {
                //Just show the informative option
                bindingExplanation.btnPermissions.visibility = View.GONE
                bindingExplanation.notAskableDescription.visibility = View.VISIBLE
            }
        } else {
            container.visibility = View.GONE
        }
        return permissionsToAsk.isEmpty()
    }

    private fun showPermissionDescriptions(ctx: Context, container: View, permissions: List<String>) {
        for (permission in permissions) {
            val descriptionID = "permission_" + permission.substring(permission.lastIndexOf(".") + 1)
            Log.d(TAG, descriptionID)
            val viewID = ctx.resources.getIdentifier(descriptionID, "id", ctx.packageName)
            val description = container.findViewById<View>(viewID)
            if (description != null) {
                description.visibility = View.VISIBLE
            }
        }
    }

    fun requestPermission(act: Activity, permission: String) {
        requestPermissions(act, listOf(permission))
    }

    @JvmStatic
    fun requestPermissions(act: Activity, permissions: List<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            act.requestPermissions(permissions.toTypedArray(), PERMISSIONS_REQUEST)
        }
    }

    private fun canAskForAllPermissions(act: Activity, permissions: List<String>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //If sdk version prior to 23 (Android M), the permissions are granted by manifest
            return true
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(act.applicationContext)
        for (permission in permissions) {
            if (!act.shouldShowRequestPermissionRationale(permission) && !isFirstTimeAsked(
                    prefs,
                    permission
                )
            ) {
                //The permission has been asked before, and the user answered "dont ask again"
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun filterNotGrantedPermissions(act: Activity, permissions: List<String>): List<String> {
        val permissionsToAsk: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //If sdk version prior to 23 (Android M), the permissions are granted by manifest
            return permissionsToAsk
        }
        for (permission in permissions) {
            if (permission == Manifest.permission.POST_NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                continue
            }
            val permitted = act.checkSelfPermission(permission)
            if (permitted != PackageManager.PERMISSION_GRANTED) {
                permissionsToAsk.add(permission)
            }
        }
        return permissionsToAsk
    }

    @JvmStatic
    fun onRequestPermissionsResult(ctx: Context, requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext)
        var permissionsGranted = 0
        if (requestCode == PERMISSIONS_REQUEST) {
            for (i in permissions.indices) {
                setAsked(prefs, permissions[i])
                if (grantResults.isNotEmpty() && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions", "Permission Granted: " + permissions[i])
                    permissionsGranted++
                } else {
                    Log.d("Permissions", "Permission Denied: " + permissions[i])
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale((ctx as Activity), permissions[i])) {
                            setDenied(prefs, permissions[i])
                        } else if (isPermissionDeniedOnce(prefs, permissions[i])) {
                            showPermissionDeniedDialog(ctx)
                        }
                    }
                }
            }
        }
        return permissions.size == permissionsGranted
    }

    private fun showPermissionDeniedDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.permissions_denied))
        builder.setMessage(context.getString(R.string.permissions_not_askable_message))
        builder.setPositiveButton(context.getString(R.string.app_settings)) { dialog: DialogInterface?, which: Int ->
            openAppSettings(context)
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog: DialogInterface?, which: Int -> }
        builder.create().show()
    }

    private fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }
}