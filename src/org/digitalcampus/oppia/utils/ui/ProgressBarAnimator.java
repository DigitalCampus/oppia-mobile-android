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

package org.digitalcampus.oppia.utils.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

public class ProgressBarAnimator implements ValueAnimator.AnimatorUpdateListener{

	public static final String TAG = ProgressBarAnimator.class.getSimpleName();
	
    private static final int ANIMATION_SCALE = 1000;
    private static final int DEFAULT_ANIM_DURATION = 1500;

    private ProgressBar bar; //reference to the view to which the animation is going to be applied
    private boolean animated = false;
    private int startDelay = 0;
    private int duration = DEFAULT_ANIM_DURATION;

    public ProgressBarAnimator (ProgressBar progressBar){
        bar = progressBar;
        bar.setMax(bar.getMax()*ANIMATION_SCALE);
    }

    public boolean isAnimated() { return animated; }
    public void setAnimated(boolean animated) { this.animated = animated;  }

    public void setStartDelay(int startDelay) { this.startDelay = startDelay; }
    public int getAnimDuration() { return duration;  }
    public void setAnimDuration(int animDuration) { this.duration = animDuration; }

    //@Override
    public void onAnimationUpdate(ValueAnimator animator) {
        bar.setProgress((Integer) animator.getAnimatedValue());
        bar.invalidate();
    }

    public void animate(int progressStart, int progressEnd){
        bar.setProgress(progressStart*ANIMATION_SCALE);
        animate(progressEnd);
    }

    public void animate(int progress){
        if (isAnimated()) {
            bar.setProgress(progress * ANIMATION_SCALE);
        }
        else{
            ObjectAnimator animation = ObjectAnimator.ofInt(bar, "progress", progress * ANIMATION_SCALE);
            animation.setDuration(duration);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setStartDelay(startDelay);
            animation.start();
        }
    }

    public void animateBoth(int fromPrimary, int toPrimary, int fromSecondary, int toSecondary){
        bar.setProgress(fromPrimary);
        bar.setSecondaryProgress(fromSecondary);
        animateBoth(toPrimary, toSecondary);
    }

    public void animateBoth(int progressPrimary, int progressSecondary){
        if (isAnimated()) {
            bar.setProgress(progressPrimary * ANIMATION_SCALE);
            bar.setSecondaryProgress(progressSecondary * ANIMATION_SCALE);
        }
        else{
            ObjectAnimator animation1 = ObjectAnimator.ofInt(bar, "progress", progressPrimary * ANIMATION_SCALE);
            animation1.setDuration(duration); // 0.5 second
            animation1.setInterpolator(new DecelerateInterpolator());
            animation1.setStartDelay(startDelay);
            animation1.start();

            ObjectAnimator animation2 = ObjectAnimator.ofInt(bar, "secondaryProgress", progressSecondary * ANIMATION_SCALE);
            animation2.setDuration(duration); // 0.5 second
            animation2.setInterpolator(new DecelerateInterpolator());
            animation2.setStartDelay(startDelay);
            animation2.start();
        }
    }
}