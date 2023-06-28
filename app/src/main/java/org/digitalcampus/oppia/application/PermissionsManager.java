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

package org.digitalcampus.oppia.application;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ViewPermissionsExplanationBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PermissionsManager {

    public static final String TAG = PermissionsManager.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST = 1246;
    private static final List<String> STARTUP_PERMISSIONS_REQUIRED = Arrays.asList(
            //Remember to update this when the Manifest permissions change!
    );
    public static final List<String> BLUETOOTH_PERMISSIONS_REQUIRED = Arrays.asList(
            //Remember to update this when the Manifest permissions change!
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    );
    public static final List<String> BLUETOOTH_API_31_PERMISSIONS_REQUIRED = Arrays.asList(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
    );
    public static final List<String> STORAGE_PERMISSIONS_REQUIRED = Arrays.asList(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    );

    public static final List<String> NOTIFICATIONS_PERMISSIONS_REQUIRED = Arrays.asList(
            Manifest.permission.POST_NOTIFICATIONS
    );

    public static final List<String> OFFLINE_COURSE_IMPORT_PERMISSIONS_REQUIRED = Arrays.asList(
            Manifest.permission.READ_EXTERNAL_STORAGE
    );

    public static final int STARTUP_PERMISSIONS = 1;
    public static final int BLUETOOTH_PERMISSIONS = 2;
    public static final int STORAGE_PERMISSIONS = 3;
    public static final int NOTIFICATIONS_PERMISSIONS = 4;
    public static final int BLUETOOTH_API_31_PERMISSIONS = 5;

    private PermissionsManager() {
        throw new IllegalStateException("Utility class");
    }

    private static boolean isFirstTimeAsked(SharedPreferences prefs, String permission) {
        return !prefs.getBoolean(permission + "_asked", false);
    }

    private static void setAsked(SharedPreferences prefs, String permission) {
        prefs.edit().putBoolean(permission + "_asked", true).apply();
    }

    private static boolean isPermissionDeniedOnce(SharedPreferences prefs, String permission) {
        return prefs.getBoolean(permission + "_denied", false);
    }

    private static void setDenied(SharedPreferences prefs, String permission) {
        prefs.edit().putBoolean(permission + "_denied", true).apply();
    }

    public static boolean checkPermissionsAndInform(final Activity act, int perms) {
        ViewGroup container = act.findViewById(R.id.permissions_explanation);
        return checkPermissionsAndInform(act, perms, container);
    }

    public static boolean checkPermissionsAndInform(final Activity act, int perms, ViewGroup container) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //If sdk version prior to 23 (Android M), the permissions are granted by manifest
            return true;
        }

        List<String> permissions = new ArrayList<>();
        switch (perms) {
            case STARTUP_PERMISSIONS: {
                permissions = STARTUP_PERMISSIONS_REQUIRED;
                break;
            }
            case BLUETOOTH_PERMISSIONS: {
                permissions = BLUETOOTH_PERMISSIONS_REQUIRED;
                break;
            }
            case BLUETOOTH_API_31_PERMISSIONS: {
                permissions = BLUETOOTH_API_31_PERMISSIONS_REQUIRED;
                break;
            }
            case STORAGE_PERMISSIONS: {
                permissions = STORAGE_PERMISSIONS_REQUIRED;
                break;
            }
            case NOTIFICATIONS_PERMISSIONS: {
                permissions = NOTIFICATIONS_PERMISSIONS_REQUIRED;
                break;
            }
        }

        final List<String> permissionsToAsk = filterNotGrantedPermissions(act, permissions);
        if (!permissionsToAsk.isEmpty()) {
            //Show the permissions informative view
            LayoutInflater layoutInflater = LayoutInflater.from(act);
            container.removeAllViews();
            @NonNull ViewPermissionsExplanationBinding bindingExplanation = ViewPermissionsExplanationBinding.inflate(layoutInflater, container, true);
            showPermissionDescriptions(act, bindingExplanation.getRoot(), permissionsToAsk);
            container.setVisibility(View.VISIBLE);

            //Check the user has not selected the "Don't ask again" option for any permission yet
            if (canAskForAllPermissions(act, permissionsToAsk)) {
                //First, set the permissions as asked
                bindingExplanation.btnPermissions.setVisibility(View.VISIBLE);
                bindingExplanation.notAskableDescription.setVisibility(View.GONE);
                bindingExplanation.btnPermissions.setOnClickListener(v ->
                        //Open the dialog to ask for permissions
                        requestPermissions(act, permissionsToAsk)
                );
            } else {
                //Just show the informative option
                bindingExplanation.btnPermissions.setVisibility(View.GONE);
                bindingExplanation.notAskableDescription.setVisibility(View.VISIBLE);
            }
        } else {
            container.setVisibility(View.GONE);
        }

        return (permissionsToAsk.isEmpty());

    }


    private static void showPermissionDescriptions(Context ctx, View container, final List<String> permissions) {
        for (String permission : permissions) {
            String descriptionID = "permission_" + permission.substring(permission.lastIndexOf(".") + 1);
            Log.d(TAG, descriptionID);
            int viewID = ctx.getResources().getIdentifier(descriptionID, "id", ctx.getPackageName());
            View description = container.findViewById(viewID);
            if (description != null) {
                description.setVisibility(View.VISIBLE);
            }
        }
    }

    public static void requestPermission(final Activity act, String permission) {
        requestPermissions(act, Collections.singletonList(permission));
    }

    public static void requestPermissions(final Activity act, List<String> permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            act.requestPermissions(permissions.toArray(new String[0]), PERMISSIONS_REQUEST);
        }
    }


    public static boolean canAskForAllPermissions(final Activity act, List<String> permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //If sdk version prior to 23 (Android M), the permissions are granted by manifest
            return true;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act.getApplicationContext());
        for (String permission : permissions) {
            if (!act.shouldShowRequestPermissionRationale(permission) && !isFirstTimeAsked(prefs, permission)) {
                //The permission has been asked before, and the user answered "dont ask again"
                return false;
            }
        }
        return true;
    }

    public static List<String> filterNotGrantedPermissions(final Activity act, List<String> permissions) {

        final List<String> permissionsToAsk = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //If sdk version prior to 23 (Android M), the permissions are granted by manifest
            return permissionsToAsk;
        }

        for (String permission : permissions) {
            if (permission.equals(Manifest.permission.POST_NOTIFICATIONS) && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                continue;
            }
            int permitted = act.checkSelfPermission(permission);
            if (permitted != PackageManager.PERMISSION_GRANTED) {
                permissionsToAsk.add(permission);
            }
        }
        return permissionsToAsk;
    }


    public static boolean onRequestPermissionsResult(Context ctx, int requestCode, String[] permissions, int[] grantResults) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        int permissionsGranted = 0;
        if (requestCode == PERMISSIONS_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                setAsked(prefs, permissions[i]);

                if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions", "Permission Granted: " + permissions[i]);
                    permissionsGranted++;
                } else {
                    Log.d("Permissions", "Permission Denied: " + permissions[i]);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) ctx, permissions[i])) {
                            setDenied(prefs, permissions[i]);
                        } else if (isPermissionDeniedOnce(prefs, permissions[i])) {
                            showPermissionDeniedDialog(ctx);
                        }
                    }
                }
            }
        }
        return permissions.length == permissionsGranted;
    }

    public static void showPermissionDeniedDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.permissions_denied));
        builder.setMessage(context.getString(R.string.permissions_not_askable_message));
        builder.setPositiveButton(context.getString(R.string.app_settings), (dialog, which) -> PermissionsManager.openAppSettings(context));
        builder.setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> {});

        builder.create().show();
    }

    public static void openAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

}
