package org.digitalcampus.oppia.gamification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.digitalcampus.oppia.listener.GamificationEventListener;

public class GamificationBroadcastReceiver extends BroadcastReceiver {

    private GamificationEventListener listener;


    @Override
    public void onReceive(Context context, Intent intent) {

        String message = intent.getStringExtra(GamificationService.SERVICE_MESSAGE);
        int points = intent.getIntExtra(GamificationService.SERVICE_POINTS, 0);

        if (listener != null){
            listener.onEvent(message, points);
        }

        abortBroadcast();
    }

    public void setGamificationEventListener(GamificationEventListener listener){
        this.listener = listener;
    }
}
