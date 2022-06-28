package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JSInterfaceForResourceImages extends JSInterface{

    private static final String TAG = JSInterfaceForResourceImages.class.getSimpleName();

    //Name of the JS interface to add to the webView
    public static final String INTERFACE_EXPOSED_NAME = "OppiaAndroid_ResourceImages";
    private static final String JS_RESOURCE_FILE = "open_file.js";

    private final String resourcesLocation;

    public JSInterfaceForResourceImages(Context ctx, String location) {
        super(ctx);
        this.resourcesLocation = location;
        loadJSInjectionSourceFile(JS_RESOURCE_FILE);
    }

    @Override
    public String getInterfaceExposedName() {
        return INTERFACE_EXPOSED_NAME;
    }

    @JavascriptInterface   // must be added for API 17 or higher
    public void openFile(String relativeFilePath) {
        File fileToOpen = new File(resourcesLocation + relativeFilePath);
        Log.d(TAG, "File to open externally: " + fileToOpen.getPath());
        Intent intent = ExternalResourceOpener.getIntentToOpenResource(context, fileToOpen);
        if(intent != null){
            context.startActivity(intent);
        } else {
            Toast.makeText(context,
                    context.getString(R.string.error_resource_app_not_found, relativeFilePath),
                    Toast.LENGTH_LONG).show();
        }

    }
}