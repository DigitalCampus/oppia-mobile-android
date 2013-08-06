package org.digitalcampus.mobile.learning.gesture;

import org.digitalcampus.mobile.learning.activity.ModuleActivity;

import android.util.Log;
import android.view.MotionEvent;

public class ResourceGestureDetector extends OppiaMobileGesture {
	
	public final static String TAG = ResourceGestureDetector.class.getSimpleName();
	private ModuleActivity modAct;
	
	public ResourceGestureDetector(ModuleActivity modAct){
		this.modAct = modAct;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
			if (Math.abs(e1.getY() - e2.getY()) > OppiaMobileGesture.SWIPE_MAX_OFF_PATH){
				Log.d(TAG,"not flung enough...");
				return false;
			}
			// right to left swipe
			if (e1.getX() - e2.getX() > OppiaMobileGesture.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > OppiaMobileGesture.SWIPE_THRESHOLD_VELOCITY) {
				Log.d(TAG,"here1");
				if (modAct.hasNext()) {
					modAct.moveNext();
					Log.d(TAG,"here2");
				}
			} else if (e2.getX() - e1.getX() > OppiaMobileGesture.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > OppiaMobileGesture.SWIPE_THRESHOLD_VELOCITY) {
				Log.d(TAG,"here3");
				if (modAct.hasPrev()) {
					Log.d(TAG,"here4");
					modAct.movePrev();
				}
			}
		} catch (Exception e) {
			// nothing
			Log.d(TAG,"exception...");
		}
		return false;
	}

}
