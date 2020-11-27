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
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.IOException;

import androidx.preference.PreferenceManager;

public class VideoPlayerActivity extends AppActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControl, MediaPlayer.OnCompletionListener {

	public static final String TAG = VideoPlayerActivity.class.getSimpleName();
	public static final String MEDIA_TAG = "mediaFileName";

    SurfaceView videoSurface;
    MediaPlayer player;
    VideoControllerView controller;

    private String mediaFileName;
    private long startTime = System.currentTimeMillis()/1000;
    private Activity activity;
    private Course course;

    private View endContainer;

    protected SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prevent activity from going to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
            Mint.logException(e);
            Log.d(TAG, "IllegalArgumentException:", e);
        } catch (SecurityException e) {
            Mint.logException(e);
            Log.d(TAG, "SecurityException:", e);
        } catch (IllegalStateException e) {
            Mint.logException(e);
            Log.d(TAG, "ExceIllegalStateExceptionption:", e);
        } catch (IOException e) {
            Mint.logException(e);
            Log.d(TAG, "IOException:", e);
        }
    }

    protected void onStart(){
        super.onStart();
        videoSurface = findViewById(R.id.videoSurface);
        endContainer = findViewById(R.id.end_container);
        ImageButton replayBtn = findViewById(R.id.replay_button);
        ImageButton continueBtn = findViewById(R.id.continue_button);

        replayBtn.setOnClickListener(v -> start());
        continueBtn.setOnClickListener(v -> VideoPlayerActivity.this.finish());

        videoSurface.setKeepScreenOn(true); //prevents player going into sleep mode
        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);
    }


    @Override
    protected void onStop(){
        super.onStop();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        return false;
    }

    @Override
    public void finish() {

        saveTracker();
        controller.setMediaPlayer(null);
        if (player != null ) {
            try {
                player.reset();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        super.finish();
    }

    private void saveTracker() {
        long endTime = System.currentTimeMillis() / 1000;
        long timeTaken = endTime - startTime;

        // digest should be that of the video not the page
        Log.d(TAG, "Attempting to save media tracker. Time: " + timeTaken);

        Media media = getMedia();
        Log.d(TAG, "saving tracker... " + media.getLength());
        boolean completed = (timeTaken >= media.getLength());
        new GamificationServiceDelegate(this)
                .createActivityIntent(course, activity, completed, false)
                .registerMediaPlaybackEvent(timeTaken, mediaFileName);

        setResult(completed ? RESULT_OK : RESULT_CANCELED); // For testing purposes

    }

    private Media getMedia() {

        for (Media m : this.activity.getMedia()) {
            Log.d(TAG, mediaFileName + "/" + m.getFilename());
            if (m.getFilename().equals(mediaFileName)) {
                return m;
            }
        }
        return null;
    }

    // Implement MediaPlayer.OnPreparedListener
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.videoSurfaceContainer));
        player.start();
        player.setOnCompletionListener(this);
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
        endContainer.setVisibility(View.GONE);
        player.setOnCompletionListener(this);
    }

    public boolean isFullScreen() {
        return false;
    }

    public void toggleFullScreen() {
        // do nothing
    }
    // End VideoMediaController.MediaPlayerControl

    // Implement SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // do nothing
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
            Log.d(TAG, "IOException:", e);
            Mint.logException(e);
            player.release();
            this.finish();

        } catch (IllegalStateException e){
            //If the player state was illegal, try to reset it again
            player.reset();
            player.prepareAsync();
            Mint.logException(e);
            Log.d(TAG, "IllegalStateException:", e);
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // do nothing
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "Video completed!");
        endContainer.setVisibility(View.VISIBLE);
    }
    // End SurfaceHolder.Callback

}
