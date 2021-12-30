package retrox.utils.android.vinput.overlay;

import retrox.utils.android.vinput.VirtualEvent;

public class ExtraButton {
	public static float textSize;
	public int x;
	public int y;
	public int w;
	public int h;
	public String label;
	public int color;
	public int colorPressed;
	public boolean pressed;
	public int pointerId = Overlay.POINTER_ID_NONE;
	
	public final boolean visible = true;
	
	public VirtualEvent event;
}
