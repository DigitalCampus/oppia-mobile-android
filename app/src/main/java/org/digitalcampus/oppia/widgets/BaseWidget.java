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

package org.digitalcampus.oppia.widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.preference.PreferenceManager;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.mediaplayer.VideoPlayerActivity;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.util.HashMap;
import java.util.Locale;

public abstract class BaseWidget extends Fragment {
	
	public static final String TAG = BaseWidget.class.getSimpleName();

	protected static final String QUIZ_EXCEPTION_MESSAGE = "Invalid Quiz Error: ";

    public static final String ACTION_TEXT_SIZE_CHANGED = BuildConfig.APPLICATION_ID + ".ACTION_TEXT_SIZE_CHANGED";

    protected static final String WIDGET_CONFIG = "widget_config";
    protected static final String PROPERTY_QUIZ = "quiz";
    protected static final String PROPERTY_ACTIVITY_STARTTIME = "Activity_StartTime";
    protected static final String PROPERTY_ON_RESULTS_PAGE = "OnResultsPage";
    protected static final String PROPERTY_ATTEMPT_SAVED = "attemptSaved";
    protected static final String PROPERTY_COURSE = "Course";
    protected static final String PROPERTY_ACTIVITY = "Activity";

	protected Activity activity = null;
	protected Course course = null;
	protected SharedPreferences prefs;
	protected boolean isBaseline = false;
    protected boolean readAloud = false;
    protected String prefLang;

	protected long startTime = 0;
    protected long spentTime = 0;
	protected boolean currentTimeAccounted = false;

	
	public abstract boolean getActivityCompleted();
	public abstract void saveTracker();
	
	public abstract String getContentToRead();
	public abstract HashMap<String,Object> getWidgetConfig();
	public abstract void setWidgetConfig(HashMap<String,Object> config);

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), ACTION_TEXT_SIZE_CHANGED)) {
                int fontSize = Integer.parseInt(prefs.getString(PrefsActivity.PREF_TEXT_SIZE, "16"));
                onTextSizeChanged(fontSize);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate WidgetFactory: " + this.getClass().getSimpleName());

        prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
        prefLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());

        getActivity().registerReceiver(receiver, new IntentFilter(ACTION_TEXT_SIZE_CHANGED));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }

    public void setReadAloud(boolean readAloud){
		this.readAloud = readAloud;
	}
	
	protected String getDigest() {
		return activity.getDigest();
	}

	public void setIsBaseline(boolean isBaseline) {
		this.isBaseline = isBaseline;
	}

	protected void setStartTime(long startTime){
		this.startTime = (startTime != 0) ? startTime : (System.currentTimeMillis()/1000);
        currentTimeAccounted = false;
	}

    public AppComponent getAppComponent(){
        App app = (App) getActivity().getApplication();
        return app.getComponent();
    }

	public long getStartTime(){
		return (startTime != 0) ? startTime : (System.currentTimeMillis()/1000);
	}

    private void addSpentTime(){
        long start = getStartTime();
        long now = System.currentTimeMillis()/1000;

        long spent = now - start;
        spentTime += spent;
        currentTimeAccounted = true;
    }

    public void resetTimeTracking(){
        spentTime = 0;
        startTime = System.currentTimeMillis() / 1000;
        currentTimeAccounted = false;
    }

    public void resumeTimeTracking(){
        startTime = System.currentTimeMillis() / 1000;
        currentTimeAccounted = false;
    }

    public void pauseTimeTracking(){
        addSpentTime();
    }

    public long getSpentTime(){
        if (!currentTimeAccounted){
            addSpentTime();
        }
        return this.spentTime;
    }

    protected void startMediaPlayerWithFile(String mediaFileName){

        Context ctx = getActivity();
        // check media file exists
        if (!Storage.mediaFileExists(ctx, mediaFileName)) {
            //Try if the filename is URL encoded
            mediaFileName = Uri.decode(mediaFileName);
            if (!Storage.mediaFileExists(ctx, mediaFileName)){
                Toast.makeText(ctx,
                    getString(R.string.error_media_not_found, mediaFileName),
                    Toast.LENGTH_LONG).show();
                return;
            }
        }

        String mimeType = FileUtils.getMimeType(Storage.getMediaPath(ctx) + mediaFileName);
        if (!FileUtils.isSupportedMediafileType(mimeType)) {
            Toast.makeText(ctx,
                    getString(R.string.error_media_unsupported, mediaFileName),
                    Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(ctx, VideoPlayerActivity.class);
        Bundle tb = new Bundle();
        tb.putSerializable(VideoPlayerActivity.MEDIA_TAG, mediaFileName);
        tb.putSerializable(Activity.TAG, activity);
        tb.putSerializable(Course.TAG, course);
        intent.putExtras(tb);
        startActivity(intent);
    }


    protected void onTextSizeChanged(int fontSize) {
        // pages that need this can override
    }
}
