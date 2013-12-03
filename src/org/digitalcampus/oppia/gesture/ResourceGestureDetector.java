package org.digitalcampus.oppia.gesture;


import org.digitalcampus.oppia.activity.CourseActivity;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class ResourceGestureDetector extends SimpleOnGestureListener {
	
	public final static String TAG = ResourceGestureDetector.class.getSimpleName();
	private CourseActivity modAct;
	
	public ResourceGestureDetector(CourseActivity modAct){
		this.modAct = modAct;
	}
	
	@Override
	public boolean onDown(MotionEvent e){
		return true;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
			if (Math.abs(e1.getY() - e2.getY()) > OppiaMobileGestureParams.SWIPE_MAX_OFF_PATH){
				return false;
			}
			// right to left swipe
			if (e1.getX() - e2.getX() > OppiaMobileGestureParams.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > OppiaMobileGestureParams.SWIPE_THRESHOLD_VELOCITY) {
				/*if (modAct.hasNext()){
					modAct.moveNext();
					return true;
				}*/
			} else if (e2.getX() - e1.getX() > OppiaMobileGestureParams.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > OppiaMobileGestureParams.SWIPE_THRESHOLD_VELOCITY) {
				/*if (modAct.hasPrev()) {
					modAct.movePrev();
				}*/
			}
		} catch (Exception e) {
			// nothing
		}
		return false;
	}

}
