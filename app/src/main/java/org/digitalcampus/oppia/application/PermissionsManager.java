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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.digitalcampus.mobile.learning.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionsManager {

    public static final String TAG = PermissionsManager.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST = 1246;
    private static final List<String> PERMISSIONS_REQUIRED = Arrays.asList(
        //Remember to update this when the Manifest permissions change!
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    );

    private PermissionsManager() {
        throw new IllegalStateException("Utility class");
    }

    private static boolean isFirstTimeAsked(SharedPreferences prefs, String permission){
        return !prefs.getBoolean(permission + "_asked", false);
    }

    private static void setAsked(SharedPreferences prefs, String permission){
         prefs.edit().putBoolean(permission + "_asked", true).apply();
    }

    public static boolean checkPermissionsAndInform(final Activity act){

        final List<String> permissionsToAsk = filterNotGrantedPermissions(act, PERMISSIONS_REQUIRED);

        ViewGroup container = act.findViewById(R.id.permissions_explanation);
        if (!permissionsToAsk.isEmpty()) {
            //Show the permissions informative view
            LayoutInflater layoutInflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            container.removeAllViews();
            View explanation = layoutInflater.inflate(R.layout.view_permissions_explanation, container);
            container.setVisibility(View.VISIBLE);

            Button reqPermsBtn = explanation.findViewById(R.id.btn_permissions);
            View permsNotAskable = explanation.findViewById(R.id.not_askable_description);
            //Check the user has not selected the "Don't ask again" option for any permission yet
            if (canAskForAllPermissions(act, permissionsToAsk)) {
                //First, set the permissions as asked
                reqPermsBtn.setVisibility(View.VISIBLE);
                permsNotAskable.setVisibility(View.GONE);
                reqPermsBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Open the dialog to ask for permissions
                        requestPermissions(act, permissionsToAsk);
                    }
                });
            }
            else{
                //Just show the informative option
                reqPermsBtn.setVisibility(View.GONE);
                permsNotAskable.setVisibility(View.VISIBLE);
            }
        }
        else{
            container.setVisibility(View.GONE);
        }

        return (permissionsToAsk.isEmpty());

    }

    public static void requestPermissions(final Activity act, List<String> permissions){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            act.requestPermissions( permissions.toArray(new String[0]), PERMISSIONS_REQUEST );
        }
    }


    public static boolean canAskForAllPermissions(final Activity act, List<String> permissions){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act.getApplicationContext());
        for (String permission : permissions) {
            if (!act.shouldShowRequestPermissionRationale(permission) && !isFirstTimeAsked(prefs, permission)) {
                //The permission has been asked before, and the user answered "dont ask again"
                return false;
            }
        }
        return true;
    }

    public static List<String> filterNotGrantedPermissions(final Activity act, List<String> permissions){

        final List<String> permissionsToAsk = new ArrayList<>();

        for (String permission : permissions){
            int permitted = act.checkSelfPermission( permission );
            if( permitted != PackageManager.PERMISSION_GRANTED ) {
                permissionsToAsk.add(permission);
            }
        }
        return permissionsToAsk;
    }


    public static boolean onRequestPermissionsResult(Context ctx, int requestCode, String[] permissions, int[] grantResults){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        int permissionsGranted = 0;
        if ( requestCode == PERMISSIONS_REQUEST) {
            for( int i = 0; i < permissions.length; i++ ) {
                setAsked(prefs, permissions[i]);

                if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                    Log.d( "Permissions", "Permission Granted: " + permissions[i] );
                    permissionsGranted++;
                } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                    Log.d( "Permissions", "Permission Denied: " + permissions[i] );
                }
            }
        }
        return permissions.length == permissionsGranted;
    }

}
