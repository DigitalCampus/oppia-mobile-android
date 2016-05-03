package org.digitalcampus.oppia.application;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionsManager {

    private static final int PERMISSIONS_REQUEST = 1246;
    private static final List<String> PERMISSIONS_REQUIRED = Arrays.asList(
        //Remember to update this when the Manifest permisssions change!
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    );

    public static boolean CheckPermissionsAndInform(Activity act){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //If sdk version prior to 23 (Android M), the permissions are granted by manifest
            return true;
        }

        int permissionsDenied = 0;
        List<String> permissionsToAsk = new ArrayList<>();

        for (String permission : PERMISSIONS_REQUIRED){
            int permitted = act.checkSelfPermission( permission );
            if( permitted != PackageManager.PERMISSION_GRANTED ) {
                permissionsDenied++;
                if (act.shouldShowRequestPermissionRationale(permission)){
                    permissionsToAsk.add(permission);
                }
            }
        }

        if (permissionsDenied > 0){
            if (permissionsToAsk.size() == permissionsDenied){
                //The user has not selected the "Don't show again" option for any permission yet
                act.requestPermissions( permissionsToAsk.toArray( new String[permissionsToAsk.size()] ), PERMISSIONS_REQUEST );
            }
            else{
                //Just show an informative option
                Toast.makeText(act, "Ouch!", Toast.LENGTH_LONG).show();
            }
        }

        return (permissionsDenied > 0);

    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if ( requestCode == PERMISSIONS_REQUEST) {
            for( int i = 0; i < permissions.length; i++ ) {
                if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                    Log.d( "Permissions", "Permission Granted: " + permissions[i] );
                } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                    Log.d( "Permissions", "Permission Denied: " + permissions[i] );
                }
            }
        }
    }

}
