package retrobox.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;
import xtvapps.core.AndroidFonts;

public class GamepadLayoutManager {
	private static final int REF_GAMEPAD_WIDTH = 540;
	private static final int REF_GAMEPAD_HEIGHT = 360;

	public enum ButtonId {
		BTN_L2, BTN_L1, BTN_R2, BTN_R1, BTN_L3, BTN_R3, 
		BTN_A, BTN_B, BTN_X, BTN_Y, BTN_SELECT, BTN_START 
	}

	private enum Pivot {START, END, MIDDLE}

	private int buttonWidth;
	private int buttonHeight;

	private ButtonLabelBox labelBoxes[] = new ButtonLabelBox[ButtonId.values().length];
	private Activity activity;
	private OnGlobalLayoutListener onGlobalLayoutListener;
	
	public GamepadLayoutManager(Activity activity, int imageResourceId, int[] labelResourceIds) {
		this.activity = activity;
		
		labelBoxes[ButtonId.BTN_L2.ordinal()] = new ButtonLabelBox(labelResourceIds[0], 92, 8, Pivot.MIDDLE, Pivot.MIDDLE);
		labelBoxes[ButtonId.BTN_L1.ordinal()] = new ButtonLabelBox(labelResourceIds[1], 92, 40, Pivot.MIDDLE, Pivot.MIDDLE);
		labelBoxes[ButtonId.BTN_R2.ordinal()] = new ButtonLabelBox(labelResourceIds[2], 420, 8, Pivot.MIDDLE, Pivot.MIDDLE);
		labelBoxes[ButtonId.BTN_R1.ordinal()] = new ButtonLabelBox(labelResourceIds[3], 420, 40, Pivot.MIDDLE, Pivot.MIDDLE);
		labelBoxes[ButtonId.BTN_L3.ordinal()] = new ButtonLabelBox(labelResourceIds[4], 175, 204, Pivot.MIDDLE, Pivot.MIDDLE);
		labelBoxes[ButtonId.BTN_R3.ordinal()] = new ButtonLabelBox(labelResourceIds[5], 334, 204, Pivot.MIDDLE, Pivot.MIDDLE);
		labelBoxes[ButtonId.BTN_A.ordinal()]  = new ButtonLabelBox(labelResourceIds[6], 451, 127, Pivot.MIDDLE, Pivot.MIDDLE);
		labelBoxes[ButtonId.BTN_B.ordinal()]  = new ButtonLabelBox(labelResourceIds[7], 415, 163, Pivot.MIDDLE, Pivot.MIDDLE);
		labelBoxes[ButtonId.BTN_X.ordinal()]  = new ButtonLabelBox(labelResourceIds[8], 415, 92, Pivot.MIDDLE, Pivot.MIDDLE);
		labelBoxes[ButtonId.BTN_Y.ordinal()]  = new ButtonLabelBox(labelResourceIds[9], 379, 127, Pivot.MIDDLE, Pivot.MIDDLE);
		labelBoxes[ButtonId.BTN_SELECT.ordinal()] = new ButtonLabelBox(labelResourceIds[10], 225, 126, Pivot.MIDDLE, Pivot.MIDDLE);
		labelBoxes[ButtonId.BTN_START.ordinal()]  = new ButtonLabelBox(labelResourceIds[11], 285, 126, Pivot.MIDDLE, Pivot.MIDDLE);
		
		for (ButtonLabelBox labelBox : labelBoxes) {
			View v = activity.findViewById(labelBox.labelResourceId);
			AndroidFonts.setViewFont(v, RetroBoxUtils.FONT_DEFAULT_B);
		}
		
		ViewTreeObserver viewTreeObserver = activity.findViewById(imageResourceId).getViewTreeObserver();
		viewTreeObserver.addOnGlobalLayoutListener(getLayoutListener());
	}
	
	public OnGlobalLayoutListener getLayoutListener() {
		if (onGlobalLayoutListener == null) {
			onGlobalLayoutListener = new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					layout();
				}
			};
		}
		return onGlobalLayoutListener;
	}

	public void layout() {
		final float gamepadWidth = activity.getResources().getDimension(R.dimen.gamepad_width)  ; 
		final float gamepadHeight = activity.getResources().getDimension(R.dimen.gamepad_height) ;
		buttonWidth = (int)(gamepadWidth / (18 ));
		buttonHeight = (int)(gamepadHeight / (12 ));

		float ratioWidth = gamepadWidth / REF_GAMEPAD_WIDTH;
		float ratioHeight = gamepadHeight / REF_GAMEPAD_HEIGHT;
		
		for (ButtonLabelBox labelBox : labelBoxes) {
			labelBox.layout(activity, ratioWidth, ratioHeight);
		}
	}
	
	public ButtonLabelBox[] getLabelBoxes() {
		return labelBoxes;
	}

	
	public class ButtonLabelBox {
		int x;
		int y;
		Pivot hPivot;
		Pivot vPivot;
		int labelResourceId;
		
		public ButtonLabelBox(int labelResourceId, int x, int y, Pivot hPivot, Pivot vPivot) {
			this.labelResourceId = labelResourceId;
			this.x = x;
			this.y = y;
			this.hPivot = hPivot;
			this.vPivot = vPivot;
		}
		
		public void setLabel(Activity activity, String label) {
			TextView v = (TextView)activity.findViewById(labelResourceId);
			v.setText(label);
		}
		
		public String getLabel(Activity activity) {
			TextView v = (TextView)activity.findViewById(labelResourceId);
			return v.getText().toString();
		}

		public void layout(Activity activity, float ratioWidth, float ratioHeight) {
			View v = activity.findViewById(labelResourceId);

			int top = 0;
			int left = 0;
			int w = v.getWidth();
			int h = v.getHeight();
			
			switch (hPivot) {
			case START  : left = x; break;
			case END    : left = x - w; break;
			case MIDDLE : left = x - (w/2); break;
			}
			switch (vPivot) {
			case START  : top = y; break;
			case END    : top = y - h; break;
			case MIDDLE : top = y - (h/2); break;
			}
			
			left += buttonWidth / 2;
			top  += buttonHeight / 2;
			
			v.setTranslationX((int) (left * ratioWidth));
			v.setTranslationY((int) (top  * ratioHeight));
		}
		
	}

}
