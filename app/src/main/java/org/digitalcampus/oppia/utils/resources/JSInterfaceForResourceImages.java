package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;

import java.io.File;

public class JSInterfaceForResourceImages {

    private static final String TAG = JSInterfaceForResourceImages.class.getSimpleName();

    //Name of the JS interface to add to the webView
    public static final String INTERFACE_EXPOSED_NAME = "OppiaAndroid";

    //Script to inject in the webView after load
    public static final String JS_INJECTION = "javascript: (function(){var imgs = document.querySelectorAll('img'); Array.prototype.forEach.call(imgs, function(img, i){if (img.parentNode.nodeName.toLowerCase()!=='a'){img.addEventListener('click', function(){"+ INTERFACE_EXPOSED_NAME +".openFile(img.getAttribute('src'));});}});})();";
    Context context;
    String resourcesLocation;

    /** Instantiate the interface and set the context */
    public JSInterfaceForResourceImages(Context ctx, String location) {
        this.context = ctx;
        this.resourcesLocation = location;
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