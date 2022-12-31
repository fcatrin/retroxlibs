package retrox.utils.android;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import xtvapps.core.android.AndroidFonts;

public class GamepadLayoutManager {
	private static final int REF_GAMEPAD_WIDTH  = 458;
	private static final int REF_GAMEPAD_HEIGHT = 300;

	public enum ButtonId {
		BTN_L2, BTN_L1, BTN_R2, BTN_R1, BTN_L3, BTN_R3,
		BTN_A, BTN_B, BTN_X, BTN_Y, BTN_SELECT, BTN_START,
		LEFT, RIGHT, UP, DOWN
	}

	private int buttonWidth;
	private int buttonHeight;

	private ButtonLabelBox labelBoxes[] = new ButtonLabelBox[ButtonId.values().length];
	private Activity activity;
	private OnGlobalLayoutListener onGlobalLayoutListener;

	public static boolean isCRT = false;

	public GamepadLayoutManager(Activity activity, int imageResourceId, int[] labelResourceIds) {
		this.activity = activity;

		addButtonLabelBox(labelResourceIds, ButtonId.UP.ordinal(),    95,  90);
		addButtonLabelBox(labelResourceIds, ButtonId.DOWN.ordinal(),  95,  146);
		addButtonLabelBox(labelResourceIds, ButtonId.LEFT.ordinal(),  67,  118);
		addButtonLabelBox(labelResourceIds, ButtonId.RIGHT.ordinal(), 122, 118);

		addButtonLabelBox(labelResourceIds, ButtonId.BTN_L2.ordinal(), 90,  16);
		addButtonLabelBox(labelResourceIds, ButtonId.BTN_L1.ordinal(), 90,  46);
		addButtonLabelBox(labelResourceIds, ButtonId.BTN_R2.ordinal(), 368, 18);
		addButtonLabelBox(labelResourceIds, ButtonId.BTN_R1.ordinal(), 368, 46);
		addButtonLabelBox(labelResourceIds, ButtonId.BTN_L3.ordinal(), 161, 183);
		addButtonLabelBox(labelResourceIds, ButtonId.BTN_R3.ordinal(), 296, 183);

		addButtonLabelBox(labelResourceIds, ButtonId.BTN_A.ordinal(), 364, 149);
		addButtonLabelBox(labelResourceIds, ButtonId.BTN_B.ordinal(), 395, 118);
		addButtonLabelBox(labelResourceIds, ButtonId.BTN_X.ordinal(), 334, 118);
		addButtonLabelBox(labelResourceIds, ButtonId.BTN_Y.ordinal(), 364, 87);

		addButtonLabelBox(labelResourceIds, ButtonId.BTN_SELECT.ordinal(), 203, 117);
		addButtonLabelBox(labelResourceIds, ButtonId.BTN_START.ordinal(),  255, 117);

		for (ButtonLabelBox labelBox : labelBoxes) {
			View v = activity.findViewById(labelBox.labelResourceId);
			AndroidFonts.setViewFont(v, RetroXUtils.FONT_DEFAULT_B);
		}

		ViewTreeObserver viewTreeObserver = activity.findViewById(imageResourceId).getViewTreeObserver();
		viewTreeObserver.addOnGlobalLayoutListener(getLayoutListener());
	}

	private void addButtonLabelBox(int[] labelResourceIds, int index, int x, int y) {
		labelBoxes[index] = new ButtonLabelBox(labelResourceIds[index], x, y);
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
		float gamepadWidth = activity.getResources().getDimension(R.dimen.gamepad_width)  ;
		float gamepadHeight = activity.getResources().getDimension(R.dimen.gamepad_height) ;

		if (isCRT) {
			gamepadWidth  *= 4.0f/3.0f;
			gamepadWidth  *= 1.25f;
			gamepadHeight *= 1.25f;
		}

		buttonWidth  = (int)(gamepadWidth  / 18);
		buttonHeight = (int)(gamepadHeight / 12);

		float ratioWidth  = gamepadWidth  / REF_GAMEPAD_WIDTH;
		float ratioHeight = gamepadHeight / REF_GAMEPAD_HEIGHT;

		for (ButtonLabelBox labelBox : labelBoxes) {
			labelBox.layout(activity, ratioWidth, ratioHeight);
		}

		ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) activity.findViewById(R.id.gamepadDialogFrame).getLayoutParams();
		layoutParams.width  = (int)gamepadWidth;
		layoutParams.height = (int)gamepadHeight;

		/* use this code to test button positions
		ViewGroup parent = activity.findViewById(R.id.gamepadDialogFrame);
		for (ButtonLabelBox labelBox : labelBoxes) {
			View view = new View(activity);
			parent.addView(view);

			ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
			lp.width  = 41;
			lp.height = 41;

			view.setBackgroundColor(0xFFA000A0);
			lp.leftMargin = (int)(labelBox.x * ratioWidth) - 20;
			lp.topMargin  = (int)(labelBox.y * ratioHeight) - 20;
		}
		*/
	}

	public ButtonLabelBox[] getLabelBoxes() {
		return labelBoxes;
	}

	public class ButtonLabelBox {
		int x;
		int y;
		int labelResourceId;

		public ButtonLabelBox(int labelResourceId, int x, int y) {
			this.labelResourceId = labelResourceId;
			this.x = x;
			this.y = y;
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
			TextView v = (TextView)activity.findViewById(labelResourceId);
			if (v == null) return;

			int w = buttonWidth * 4;
			int h = buttonHeight;

			ViewGroup.MarginLayoutParams textLayoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
			textLayoutParams.width = w;
			textLayoutParams.height = h;

			v.setGravity(Gravity.CENTER);
			textLayoutParams.leftMargin = (int)(x * ratioWidth)  - (w / 2);
			textLayoutParams.topMargin  = (int)(y * ratioHeight) - (h / 2);
		}
	}
}
