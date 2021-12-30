package retrox.utils.android.vinput;

import android.view.GestureDetector;
import android.view.MotionEvent;

class SwipeDetector extends GestureDetector.SimpleOnGestureListener {
    private static final String DEBUG_TAG = SwipeDetector.class.getSimpleName();
    
	final float minDistanceX = 0.25f;
	final float minDistanceY = 0.25f;
	final float minVelocityX = 1.0f;
	final float minVelocityY = 1.0f;
	final float screenWidth;
	final float screenHeight;
    
    private final SwipeListener listener;
    
    public SwipeDetector(SwipeListener listener, int screenWidth, int screenHeight) {
    	this.listener = listener;
    	this.screenWidth = screenWidth;
    	this.screenHeight = screenHeight;
    }
    
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    	if (e1 == null || e2 == null) return false;
    	
    	final float distanceX = Math.abs(e1.getX() - e2.getX()) / screenWidth;
    	final float distanceY = Math.abs(e1.getY() - e2.getY()) / screenHeight;
    	
    	velocityX = velocityX / screenWidth;
    	velocityY = velocityY / screenHeight;

        if (Math.abs(velocityX)>minVelocityX && distanceX>minDistanceX) {
    		if (velocityX<0) listener.onSwipe(SwipeListener.Swipe.Left);
    		else listener.onSwipe(SwipeListener.Swipe.Right);
    		return true;
    	}
    	if (Math.abs(velocityY)>minVelocityY && distanceY>minDistanceY) {
    		if (velocityY<0) listener.onSwipe(SwipeListener.Swipe.Up);
    		else listener.onSwipe(SwipeListener.Swipe.Down);
    		return true;
    	}
    	return true;

    }
}

