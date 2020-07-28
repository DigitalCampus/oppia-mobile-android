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

import android.animation.ValueAnimator;
import android.view.View;

public class SimpleAnimator {

    public static final int DEFAULT_ANIM_DURATION = 700;
    public static final boolean FADE_IN = true;
    public static final boolean FADE_OUT = false;

    private SimpleAnimator() {
        throw new IllegalStateException("Utility class");
    }

    public static void fade(final View view, boolean visible, int duration){
        ValueAnimator animator = ValueAnimator.ofFloat(visible?1f:0f, visible?0f:1f);
        animator.addUpdateListener(valueAnimator -> view.setAlpha(1f - (Float) valueAnimator.getAnimatedValue()));
        animator.setDuration(duration);
        animator.start();
    }

    public static void fadeFromTop(final View view, boolean visible, int duration){
        ValueAnimator animator = ValueAnimator.ofFloat(visible?1f:0f, visible?0f:1f);
        animator.addUpdateListener(valueAnimator -> {
            view.setTranslationY( (Float) valueAnimator.getAnimatedValue() * -80 );
            view.setAlpha(1f - (Float) valueAnimator.getAnimatedValue());
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
