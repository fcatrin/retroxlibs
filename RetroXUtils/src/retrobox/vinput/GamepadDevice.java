package retrobox.vinput;

import java.io.File;

import android.util.SparseBooleanArray;
import android.view.KeyEvent;

public class GamepadDevice {
	
	SparseBooleanArray triggerState = new SparseBooleanArray();

	private String deviceName;
	private int deviceId;
	public int player;
	
	public File keymapFile;
	
	private GamepadMapping gamepadMapping;
	
	public GamepadDevice() {}
	
	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	@Override
	public String toString() {
		return "GenericGamepad {device:" + deviceName + ", deviceId:" + deviceId + "}";
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
		
		GamepadKeyMapping gamepadKeyMapping = Mapper.knownKeyMappings[player];
		
		if (leftChanged)  gamepadKeyMapping.virtualEvents[2].sendEvent(this, left);
		if (rightChanged) gamepadKeyMapping.virtualEvents[3].sendEvent(this, right);
		if (upChanged)    gamepadKeyMapping.virtualEvents[0].sendEvent(this, up);
		if (downChanged)  gamepadKeyMapping.virtualEvents[1].sendEvent(this, down);
	}

	public GamepadMapping getGamepadMapping() {
		return gamepadMapping;
	}

	public void setGamepadMapping(GamepadMapping gamepadMapping) {
		this.gamepadMapping = gamepadMapping;
	}
	
	
}
