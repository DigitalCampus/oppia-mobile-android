package org.digitalcampus.mobile.learning.gesture;

import org.digitalcampus.mobile.learning.activity.ModuleActivity;
import org.digitalcampus.mobile.learning.widgets.MQuizWidget;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class QuizGestureDetector extends SimpleOnGestureListener {
	
	public final static String TAG = QuizGestureDetector.class.getSimpleName();
	private ModuleActivity modAct;
	
	public QuizGestureDetector(ModuleActivity modAct){
		this.modAct = modAct;
	}

	@Override
	public boolean onDown(MotionEvent e){
		return true;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
			if (Math.abs(e1.getY() - e2.getY()) > OppiaMobileGestureParams.SWIPE_MAX_OFF_PATH)
				return false;
			// right to left swipe
			if (e1.getX() - e2.getX() > OppiaMobileGestureParams.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > OppiaMobileGestureParams.SWIPE_THRESHOLD_VELOCITY) {
				if (this.modAct.getCurrentActivity() instanceof MQuizWidget) {
					if (((MQuizWidget) this.modAct.getCurrentActivity()).getMquiz().hasNext()) {
						((MQuizWidget) this.modAct.getCurrentActivity()).nextBtn.performClick();
					}
				}

			} else if (e2.getX() - e1.getX() > OppiaMobileGestureParams.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > OppiaMobileGestureParams.SWIPE_THRESHOLD_VELOCITY) {
				if (this.modAct.getCurrentActivity() instanceof MQuizWidget) {
					if (((MQuizWidget) this.modAct.getCurrentActivity()).getMquiz().hasPrevious()) {
						((MQuizWidget) this.modAct.getCurrentActivity()).prevBtn.performClick();
					}
				}
			}
		} catch (Exception e) {
			// nothing
		}
		return false;
	}
}
