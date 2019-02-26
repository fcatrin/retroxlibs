package retrobox.utils;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import retrobox.utils.GamepadLayoutManager.ButtonId;
import retrobox.utils.GamepadLayoutManager.ButtonLabelBox;

public class GamepadView extends FrameLayout {
	private static final int REF_GAMEPAD_WIDTH = 540;
	private static final int REF_GAMEPAD_HEIGHT = 360;

	public enum EventId {
		UP, DOWN, LEFT, RIGHT,
		BTN_A, BTN_B, BTN_X, BTN_Y,
		BTN_L1, BTN_R1, BTN_L2, BTN_R2,
		BTN_L3, BTN_R3, BTN_SELECT, BTN_START,
		RX, RY
	}
	
	public static String eventNames[] = { 
			"UP", "DOWN", "LEFT", "RIGHT", 
			"BTN_A", "BTN_B", "BTN_X", "BTN_Y", 
			"TL", "TR", "TL2", "TR2",
			"TL3", "TR3", "SELECT", "START",
			"RX", "RY"
		};
	
	ButtonLabelBox labelBoxes[] = new ButtonLabelBox[eventNames.length]; 

	public GamepadView(Context context) {
		super(context);
	}

	public GamepadView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GamepadView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void init() {
		new ButtonLabelBox(this, ButtonId.BTN_L2, 92, 8);
		new ButtonLabelBox(this, ButtonId.BTN_L1, 92, 40);
		new ButtonLabelBox(this, ButtonId.BTN_R2, 420, 8);
		new ButtonLabelBox(this, ButtonId.BTN_R1, 420, 40);
		new ButtonLabelBox(this, ButtonId.BTN_L3, 175, 204);
		new ButtonLabelBox(this, ButtonId.BTN_R3, 334, 204);
		new ButtonLabelBox(this, ButtonId.BTN_A, 451, 127);
		new ButtonLabelBox(this, ButtonId.BTN_B, 415, 163);
		new ButtonLabelBox(this, ButtonId.BTN_X, 415, 92);
		new ButtonLabelBox(this, ButtonId.BTN_Y, 379, 127);
		new ButtonLabelBox(this, ButtonId.BTN_SELECT, 225, 126);
		new ButtonLabelBox(this, ButtonId.BTN_START, 285, 126);
		
		setBackgroundResource(R.drawable.gamepad);
		setAlpha(0.5f);
		
		this.post(new Runnable(){

			@Override
			public void run() {
				layout();
			}
		});
	}
	
	public void layout() {
		int gamepadWidth  = getMeasuredWidth();
		int gamepadHeight = getMeasuredHeight();
		
		for(ButtonLabelBox labelBox : labelBoxes) {
			if (labelBox!=null) labelBox.layout(gamepadWidth, gamepadHeight);
		}
	}
	
	public class ButtonLabelBox {
		ButtonId buttonId;
		float x;
		float y;
		String label;
		TextView  textView;
		ImageView imageView;
		
		public ButtonLabelBox(ViewGroup parent, ButtonId buttonId, float x, float y) {
			this.buttonId = buttonId;
			labelBoxes[buttonId.ordinal()] = this;
			
			this.x = x / REF_GAMEPAD_WIDTH;
			this.y = y / REF_GAMEPAD_HEIGHT;
			
			textView = new TextView(parent.getContext());
			imageView = new ImageView(parent.getContext());
			
			parent.addView(imageView);
			parent.addView(textView);
			
			textView.setText("BUTTON");
			imageView.setImageResource(R.drawable.gamepad_press);
		}
		
		public void setLabel(Activity activity, String label) {
			textView.setText(label);
		}
		
		public void layout(int gamepadWidth, int gamepadHeight) {
			int buttonWidth = (int)(gamepadWidth / (18 ));
			int buttonHeight = (int)(gamepadHeight / (12 ));

			int textWidth  = buttonWidth * 4;
			int textHeight = buttonHeight;
			
			FrameLayout.LayoutParams textLayoutParams = (FrameLayout.LayoutParams)textView.getLayoutParams();
			textLayoutParams.width = textWidth;
			textLayoutParams.height = textHeight;
			textLayoutParams.leftMargin = (int) (x * gamepadWidth - textWidth / 2);
			textLayoutParams.topMargin  = (int) (y * gamepadHeight - textHeight / 2); 
			textView.setGravity(Gravity.CENTER);
			
			
			FrameLayout.LayoutParams imageLayoutParams = (FrameLayout.LayoutParams)imageView.getLayoutParams();
			imageLayoutParams.width = buttonWidth;
			imageLayoutParams.height = buttonHeight;
			imageLayoutParams.leftMargin = (int) (x * gamepadWidth - buttonWidth / 2);
			imageLayoutParams.topMargin  = (int) (y * gamepadHeight - buttonHeight /2); 
			
		}
		
	}


}
