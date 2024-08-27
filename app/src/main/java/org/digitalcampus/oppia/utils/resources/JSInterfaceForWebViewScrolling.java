package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.webkit.JavascriptInterface;
import org.digitalcampus.oppia.utils.ui.ExtendedWebView;

public class JSInterfaceForWebViewScrolling extends JSInterface {

    private static final String TAG = JSInterfaceForWebViewScrolling.class.getSimpleName();

    //Name of the JS interface to add to the webView
    public static final String INTERFACE_EXPOSED_NAME = "OppiaAndroid_WebviewScrolling";
    private static final String JS_RESOURCE_FILE = "webview_scrolling.js";

    private ExtendedWebView webView;

    public JSInterfaceForWebViewScrolling(Context ctx, ExtendedWebView webView) {
        super(ctx);
        this.webView = webView;
        loadJSInjectionSourceFile(JS_RESOURCE_FILE);
    }

    @Override
    public String getInterfaceExposedName() {
        return INTERFACE_EXPOSED_NAME;
    }

    @JavascriptInterface   // must be added for API 17 or higher
    public void canScroll(boolean value) {
        webView.setCanScroll(value);
    }
}