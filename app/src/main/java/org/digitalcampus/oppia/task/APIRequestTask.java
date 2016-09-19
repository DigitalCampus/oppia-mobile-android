package org.digitalcampus.oppia.task;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.RemoteApiEndpoint;

public abstract class APIRequestTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected Context ctx;
    protected SharedPreferences prefs;
    protected ApiEndpoint apiEndpoint;

    protected APIRequestTask(Context ctx) {
        this(ctx, new RemoteApiEndpoint());
    }

    protected APIRequestTask(Context ctx, ApiEndpoint api){
        this.ctx = ctx;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        apiEndpoint = api;
    }
}
