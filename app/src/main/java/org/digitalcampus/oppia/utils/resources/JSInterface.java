package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class JSInterface {

    private static final String TAG = JSInterface.class.getSimpleName();
    private static final String JS_FILE_REGEX = "{{INTERFACE_EXPOSED_NAME}}";
    private static final String JS_EXECUTION_PREFIX = "javascript: ";

    protected final Context context;
    private String javascriptInjection;

    /** Instantiate the interface and set the context */
    public JSInterface(Context ctx) {
        this.context = ctx;
    }

    public abstract String getInterfaceExposedName();

    public String getJavascriptInjection(){
        return JS_EXECUTION_PREFIX + javascriptInjection;
    }

    protected void loadJSInjectionSourceFile(String filename){
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            InputStream is = context.getAssets().open("js_injects/" + filename);
            reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8 ));
            String str;
            while ((str = reader.readLine()) != null) { sb.append(str); }

            javascriptInjection = sb.toString();
            javascriptInjection = javascriptInjection.replace(JS_FILE_REGEX, getInterfaceExposedName());

        } catch (IOException e) {

        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing asset JS input file");
                }
            }
        }
    }
}
