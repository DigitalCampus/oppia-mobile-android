package org.digitalcampus.oppia.utils.ui;

import android.animation.ValueAnimator;
import android.view.View;

public class SimpleAnimator {

    public static int DEFAULT_ANIM_DURATION = 700;
    public static boolean FADE_IN = true;
    public static boolean FADE_OUT = false;

    public static void fade(final View view, boolean visible, int duration){
        ValueAnimator animator = ValueAnimator.ofFloat(visible?1f:0f, visible?0f:1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                view.setAlpha(1f - (Float) valueAnimator.getAnimatedValue());
            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    public static void fadeFromTop(final View view, boolean visible, int duration){
        ValueAnimator animator = ValueAnimator.ofFloat(visible?1f:0f, visible?0f:1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                view.setTranslationY( (Float) valueAnimator.getAnimatedValue() * -80 );
                view.setAlpha(1f - (Float) valueAnimator.getAnimatedValue());
            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    public static void fade(final View view, boolean visible){
        fade(view, visible, DEFAULT_ANIM_DURATION);
    }

    public static void fadeFromTop(final View view, boolean visible){
        fadeFromTop(view, visible, DEFAULT_ANIM_DURATION);
    }
}
