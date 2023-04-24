package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class JSInterfaceForInlineInput extends JSInterface{

    private static final String TAG = JSInterfaceForInlineInput.class.getSimpleName();

    //Name of the JS interface to add to the webView
    public static final String INTERFACE_EXPOSED_NAME = "OppiaAndroid_InlineInput";
    private static final String JS_RESOURCE_FILE = "register_inline_input.js";

    private OnInputEnteredListener listener;

    public interface OnInputEnteredListener{
        void inlineInputReceived(String input);
    }

    public JSInterfaceForInlineInput(Context ctx) {
        super(ctx);
        loadJSInjectionSourceFile(JS_RESOURCE_FILE);
    }

    @Override
    public String getInterfaceExposedName() {
        return INTERFACE_EXPOSED_NAME;
    }

    public void setOnInputEnteredListener(OnInputEnteredListener listener){
        this.listener = listener;
    }

    @JavascriptInterface   // must be added for API 17 or higher
    public void registerInlineInput(String userInput) {
        Log.d(TAG, "User input! " + userInput);
        if (listener != null){
            listener.inlineInputReceived(userInput);
        }
    }
}