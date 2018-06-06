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
 * 
 * - See more at: http://www.brightec.co.uk/blog/custom-android-media-controller#sthash.v281GcNw.dpuf
 */

package org.digitalcampus.oppia.utils.mediaplayer;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.gamification.GamificationEngine;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

public class VideoPlayerActivity extends AppActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControl {

	public static final String TAG = VideoPlayerActivity.class.getSimpleName();
	public static final String MEDIA_TAG = "mediaFileName";
	
    SurfaceView videoSurface;
    MediaPlayer player;
    VideoControllerView controller;
    
    private String mediaFileName;
    private long startTime = System.currentTimeMillis()/1000;
    private Activity activity;
    private Course course;
    protected SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        player = new MediaPlayer();
        controller = new VideoControllerView(this);
        
        Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			mediaFileName = (String) bundle.getSerializable(MEDIA_TAG);
			activity = (Activity) bundle.getSerializable(Activity.TAG);
			course = (Course) bundle.getSerializable(Course.TAG);
		} else {
			this.finish();
		}
        
        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, Uri.parse(Storage.getMediaPath(this) + mediaFileName));
            player.setOnPreparedListener(this);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onStart(){
        super.onStart();
        videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        videoSurface.setKeepScreenOn(true); //prevents player going into sleep mode
        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);
    }

    
    @Override
    protected void onStop(){
        super.onStop();

    	saveTracker();
        controller.setMediaPlayer(null);
        if (player != null ) player.reset();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        return false;
    }

    private void saveTracker(){
    	long endTime = System.currentTimeMillis() / 1000;
		long timeTaken = endTime - startTime;
		// track that the video has been played (or at least clicked on)
		Tracker t = new Tracker(this);
		// digest should be that of the video not the page
        Log.d(TAG, "Attempting to save media tracker. Time: " + timeTaken);
		for (Media m : this.activity.getMedia()) {
		    Log.d(TAG, mediaFileName + "/" + m.getFilename());
			if (m.getFilename().equals(mediaFileName)) {
				Log.d(TAG,"saving tracker... " + m.getLength());
				boolean completed = false;
				if (timeTaken >= m.getLength()) {
					completed = true;
				}
				JSONObject data = new JSONObject();
				try {
					data.put("media", "played");
					data.put("mediafile", mediaFileName);
					data.put("timetaken", timeTaken);
					String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault()
							.getLanguage());
					data.put("lang", lang);
					Log.d(TAG,data.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				MetaDataUtils mdu = new MetaDataUtils(this);
				// add in extra meta-data
				try {
					data = mdu.getMetaData(data);
				} catch (JSONException e) {
					// Do nothing
				}
                GamificationEngine gamificationEngine = new GamificationEngine(this);
				GamificationEvent gamificationEvent = gamificationEngine.processEventMediaPlayed(this.course, this.activity, mediaFileName, timeTaken);

				t.saveTracker(this.course.getCourseId(), m.getDigest(), data, completed, gamificationEvent);
			}
		}
	}

    // Implement MediaPlayer.OnPreparedListener
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
        player.start();
    }
    // End MediaPlayer.OnPreparedListener

    // Implement VideoMediaController.MediaPlayerControl
    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return true;
    }

    public boolean canSeekForward() {
        return true;
    }

    public int getBufferPercentage() {
        return 0;
    }

    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public int getDuration() {
        return player.getDuration();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void pause() {
        player.pause();
    }

    public void seekTo(int i) {
        player.seekTo(i);
    }

    public void start() {
        player.start();
    }

    public boolean isFullScreen() {
        return false;
    }

    public void toggleFullScreen() {
        
    }
    // End VideoMediaController.MediaPlayerControl

    // Implement SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        
    }

    public void surfaceCreated(SurfaceHolder holder) {
    	player.setDisplay(holder);
        player.stop();
        player.reset();
        try{
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, Uri.parse(Storage.getMediaPath(this) + mediaFileName));
            player.setOnPreparedListener(this);
            player.prepareAsync();

        } catch (IOException e) {
            //If the source is not available, close the activity
            e.printStackTrace();
            Mint.logException(e);
            player.release();
            this.finish();

        } catch (IllegalStateException e){
            //If the player state was illegal, try to reset it again
            player.reset();
            player.prepareAsync();
            e.printStackTrace();
            Mint.logException(e);
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    // End SurfaceHolder.Callback

}
