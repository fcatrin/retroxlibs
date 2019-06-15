package retrobox.vinput;

import java.io.File;

import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import retrobox.utils.R;

public class GenericGamepad {
	
	public enum Analog {LEFT, RIGHT};
	
	public static String eventNames[] = { 
		"UP", "DOWN", "LEFT", "RIGHT", 
		"BTN_A", "BTN_B", "BTN_X", "BTN_Y", 
		"TL", "TR", "TL2", "TR2",
		"TL3", "TR3", "SELECT", "START"
	};
	
	public static int eventLabelResourceIds[] = {
			R.string.gamepad_label_up,
			R.string.gamepad_label_down,
			R.string.gamepad_label_left,
			R.string.gamepad_label_right,
			R.string.gamepad_label_a,
			R.string.gamepad_label_b,
			R.string.gamepad_label_x,
			R.string.gamepad_label_y,
			R.string.gamepad_label_tl,
			R.string.gamepad_label_tr,
			R.string.gamepad_label_tl2,
			R.string.gamepad_label_tr2,
			R.string.gamepad_label_tl3,
			R.string.gamepad_label_tr3,
			R.string.gamepad_label_select,
			R.string.gamepad_label_start,
	};

	SparseBooleanArray triggerState = new SparseBooleanArray();

	String deviceDescriptor;
	int deviceId;
	public int player;
	
	public File keymapFile;
	
	public int originCodes[] = {
		KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
		KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_BUTTON_Y,
		KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_BUTTON_L2, KeyEvent.KEYCODE_BUTTON_R2,
		KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR, KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_START
	};

	public int axisRx = 0;
	public int axisRy = 0;
	
	public int translatedCodes[] = new int[originCodes.length];

	public VirtualEvent virtualEvents[] = new VirtualEvent[eventNames.length];
	
	public GenericGamepad() {
		for(int i=0; i<translatedCodes.length; i++) translatedCodes[i] = 0;
	}
	
	public int getOriginCode(int keyCode) {
		for(int i=0; i<translatedCodes.length; i++) {
			if (translatedCodes[i] == keyCode) {
				return originCodes[i];
			}
		}
		return 0;
	}
	
	public int getOriginIndex(int keyCode) {
		for(int i=0; i<originCodes.length; i++) {
			if (originCodes[i] == keyCode) return i; 
		}
		return -1;
	}
	
	public String getDeviceDescriptor() {
		return deviceDescriptor;
	}

	public void setDeviceDescriptor(String deviceDescriptor) {
		this.deviceDescriptor = deviceDescriptor;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	@Override
	public String toString() {
		return "GenericGamepad {descriptor:" + deviceDescriptor + ", deviceId:" + deviceId + "}";
	}

	public void setTriggerState(int code, boolean down) {
		triggerState.put(code, down);
	}
	
	public boolean getTriggerState(int code) {
		Boolean down = triggerState.get(code);
		return down!=null && down.booleanValue();
	}
	
	public void setDpad(boolean left, boolean right, boolean up, boolean down) {
		boolean leftChanged  = getTriggerState(KeyEvent.KEYCODE_DPAD_LEFT)  != left;
		boolean rightChanged = getTriggerState(KeyEvent.KEYCODE_DPAD_RIGHT) != right;
		boolean upChanged    = getTriggerState(KeyEvent.KEYCODE_DPAD_UP)    != up;
		boolean downChanged  = getTriggerState(KeyEvent.KEYCODE_DPAD_DOWN)  != down;
		
		setTriggerState(KeyEvent.KEYCODE_DPAD_LEFT , left);
		setTriggerState(KeyEvent.KEYCODE_DPAD_RIGHT, right);
		setTriggerState(KeyEvent.KEYCODE_DPAD_UP   , up);
		setTriggerState(KeyEvent.KEYCODE_DPAD_DOWN , down);
		
		if (leftChanged)  virtualEvents[2].sendEvent(this, left);
		if (rightChanged) virtualEvents[3].sendEvent(this, right);
		if (upChanged)    virtualEvents[0].sendEvent(this, up);
		if (downChanged)  virtualEvents[1].sendEvent(this, down);
	}
	
}
