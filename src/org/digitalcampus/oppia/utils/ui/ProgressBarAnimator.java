package org.digitalcampus.oppia.utils.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.widget.ProgressBar;

public class ProgressBarAnimator implements ValueAnimator.AnimatorUpdateListener{


    private static final int DEFAULT_ANIM_DURATION = 1500;
    private ProgressBar bar; //reference to the view to which the animation is going to be applied
    private boolean animated = false;
    private int duration = DEFAULT_ANIM_DURATION;

    public ProgressBarAnimator (ProgressBar progressBar){
        bar = progressBar;
    }

    public boolean isAnimated() { return animated; }
    public void setAnimated(boolean animated) { this.animated = animated;  }

    public int getAnimDuration() { return duration;  }
    public void setAnimDuration(int animDuration) { this.duration = animDuration; }

    @Override
    public void onAnimationUpdate(ValueAnimator animator) {
        bar.setProgress((Integer) animator.getAnimatedValue());
        bar.invalidate();
    }

    public void animate(int progressStart, int progressEnd){
        if (!isAnimated()){
            ValueAnimator anim = ObjectAnimator.ofInt(progressStart, progressEnd);
            anim.addUpdateListener(this);
            anim.setDuration(duration).start();
        }
    }

    public void animate(int progress){
        animate(0, progress);
    }
}