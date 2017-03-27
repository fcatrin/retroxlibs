package retrobox.vinput.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

public class GamepadView extends View {

	private ViewGroup _viewGroup;
	private Overlay overlay;
	
	public GamepadView(Context context, Overlay overlay) {
		super(context);
		this.overlay = overlay;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		overlay.draw(canvas);
	}
	
    public void addToLayout(ViewGroup viewGroup) {
        if (_viewGroup == null) {
            _viewGroup = viewGroup;
        }
    }
    
    public void hidePanel() {
        if (_viewGroup != null) {
            _viewGroup.removeView(this);
        }
    }
    
    public void showPanel() {
        if (_viewGroup != null) {
            if (getParent() != null) _viewGroup.removeView(this);
            _viewGroup.addView(this);
        }
    }
    
    public boolean isEnabled() {
    	return _viewGroup != null;
    }

    public boolean isVisible() {
        return (null != getParent());
    }
    
    public void toggleView() {
    	if (isVisible()) hidePanel();
    	else showPanel();
    }
}
