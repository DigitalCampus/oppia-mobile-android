package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.kano.training.oppia.BuildConfig;
import org.kano.training.oppia.R;
import org.digitalcampus.oppia.utils.storage.ExternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File; 

import static android.support.v4.content.FileProvider.getUriForFile;

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
        Uri targetUri = Storage.getStorageStrategy() instanceof ExternalStorageStrategy ? Uri.fromFile(new File(fileUrl))
                :getUriForFile(_ctx, BuildConfig.APPLICATION_ID, new File(fileUrl));
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