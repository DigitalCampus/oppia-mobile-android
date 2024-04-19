package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.webkit.JavascriptInterface;

import java.io.File;

public class JSInterfaceForAudioPlayer extends JSInterface {

    private static final String TAG = JSInterfaceForAudioPlayer.class.getSimpleName();

    //Name of the JS interface to add to the webView
    public static final String INTERFACE_EXPOSED_NAME = "OppiaAndroid_AudioPlayer";
    private static final String JS_RESOURCE_FILE = "observe_audio_player.js";

    private int audioDuration;
    private OnPlayButtonClickListener onPlayButtonClickListener;
    private boolean playing = false;

    public interface OnPlayButtonClickListener {
        void onPlayButtonClick(boolean playing, int duration);
        void onAudioCompleted(String filename);
    }

    public void setOnPlayButtonClickListener(OnPlayButtonClickListener listener) {
        this.onPlayButtonClickListener = listener;
    }

    public JSInterfaceForAudioPlayer(Context ctx) {
        super(ctx);
        loadJSInjectionSourceFile(JS_RESOURCE_FILE);
    }

    @Override
    public String getInterfaceExposedName() {
        return INTERFACE_EXPOSED_NAME;
    }

    @JavascriptInterface   // must be added for API 17 or higher
    public void onAudioCompleted(String audioSource) {
        if (onPlayButtonClickListener != null) {
            File audioFile = new File(audioSource);
            onPlayButtonClickListener.onAudioCompleted(audioFile.getName());
        }
    }


    @JavascriptInterface   // must be added for API 17 or higher
    public void onPlayButtonClick(String audioSource) {

        // Get duration only once
        if (audioDuration == 0) {
            audioDuration = calculateAudioDuration(audioSource);
        }

        if (audioDuration > 0) {
            playing = !playing;
            if (onPlayButtonClickListener != null) {
                onPlayButtonClickListener.onPlayButtonClick(playing, audioDuration);
            }
        }
    }

    private int calculateAudioDuration(String audioSource) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioSource);
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            mediaPlayer.release();
            return duration;
        } catch (Exception e) {
            Log.e(TAG, "calculateAudioDuration: ", e);;
            return 0;
        }
    }
}