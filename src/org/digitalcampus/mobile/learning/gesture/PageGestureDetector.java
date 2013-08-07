package org.digitalcampus.mobile.learning.gesture;

import org.digitalcampus.mobile.learning.activity.ModuleActivity;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

public class PageGestureDetector extends SimpleOnGestureListener {
	
	public final static String TAG = PageGestureDetector.class.getSimpleName();
	private ModuleActivity modAct;

	public PageGestureDetector(ModuleActivity modAct){
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
				if (this.modAct.hasNext()) {
					this.modAct.moveNext();
				}
			} else if (e2.getX() - e1.getX() > OppiaMobileGestureParams.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > OppiaMobileGestureParams.SWIPE_THRESHOLD_VELOCITY) {
				if (this.modAct.hasPrev()) {
					this.modAct.movePrev();
				}
			}
		} catch (Exception e) {
			// nothing
		}
		return false;
	}
}
