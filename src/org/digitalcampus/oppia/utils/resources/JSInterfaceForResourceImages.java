package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;
import java.util.List;

public class JSInterfaceForResourceImages {

    //Name of the JS interface to add to the webView
    public static String InterfaceExposedName = "OppiaAndroid";

    //Script to inject in the webView after load
    public static String JSInjection = "javascript: (function(){var imgs = document.querySelectorAll('img'); Array.prototype.forEach.call(imgs, function(img, i){if (img.parentNode.nodeName.toLowerCase()!=='a'){img.addEventListener('click', function(){"+InterfaceExposedName+".openFile(img.getAttribute('src'));});}});})();";

    /**
     The JS not minified:

     //CURRENT VERSION (plain JS)
     (function(){
       var images = document.querySelectorAll('img');
       Array.prototype.forEach.call(images, function(img, i){
         if (img.parentNode.nodeName.toLowerCase()  !== 'a'){
             img.addEventListener('click', function(){
                  #InterfaceExposedName##.openFile(img.getAttribute('src'));
             });
         }
       });
     })();

     //OLD VERSION (using jQuery)
     $(function(){
        $('img').on('click', function(){
            if ($(this).parent()[0].nodeName.toLowerCase() !== 'a'){
                ##InterfaceExposedName##.openFile($(this).attr('src'));
            }
        });
     });

     */
     //public static String JSInjection = "javascript: $(function(){$('img').on('click',function(){if ($(this).parent()[0].nodeName.toLowerCase()!=='a'){" + InterfaceExposedName + ".openFile($(this).attr('src'));}});});";

    Context _ctx;
    String resourcesLocation;

    /** Instantiate the interface and set the context */
    public JSInterfaceForResourceImages(Context ctx, String location) {
        this._ctx = ctx;
        this.resourcesLocation = location;
    }

    @JavascriptInterface   // must be added for API 17 or higher
    public void openFile(String relativeFilePath) {
        String fileUrl = resourcesLocation + relativeFilePath;
        Uri targetUri = Uri.fromFile(new File(fileUrl));
        String filetype = FileUtils.getMimeType(fileUrl);

        Intent intent = ExternalResourceOpener.getIntentToOpenResource(_ctx, targetUri, filetype);
        if(intent != null){
            _ctx.startActivity(intent);
        } else {
            Toast.makeText(_ctx,
                    _ctx.getString(R.string.error_resource_app_not_found, relativeFilePath),
                    Toast.LENGTH_LONG).show();
        }

    }
}