package xtvapps.core.android;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class FixedSpeedScroller extends Scroller {

    private final int mDuration;

    public FixedSpeedScroller(Context context, int duration) {
        super(context);
        this.mDuration = duration;
    }

    public FixedSpeedScroller(Context context, Interpolator interpolator, int duration) {
        super(context, interpolator);
        this.mDuration = duration;
    }

    public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel, int duration) {
        super(context, interpolator, flywheel);
        this.mDuration = duration;
    }


    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}