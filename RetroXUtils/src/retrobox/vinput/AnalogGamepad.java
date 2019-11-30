package retrobox.vinput;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AnalogGamepad {
	private static final String LOGTAG = AnalogGamepad.class.getSimpleName();
	public enum Axis {MIN, CENTER, MAX}
	
	float deadzone = 0.2f;
	
	Axis axisX = Axis.CENTER;
	Axis axisY = Axis.CENTER;
	
	static int maxGamepadX = 640;
	static int maxGamepadY = 480;
	static int lastGamepadX = 0;
	static int lastGamepadY = 0;
	static int gamepadX = 0;
	static int gamepadY = 0;
	static float gamepadMouseMoveX = 0;
	static float gamepadMouseMoveY = 0;
	boolean gamepadMouseThreadRunning = false;
	private AnalogGamepadListener listener;
	
	public AnalogGamepad(int maxX, int maxY, AnalogGamepadListener listener) {
		this.listener = listener;
		maxGamepadX = maxX;
		maxGamepadY = maxY;
		lastGamepadX = maxX / 2;
		lastGamepadY = maxY / 2;
		gamepadX = lastGamepadX;
		gamepadY = lastGamepadY;
	}

	public void stopGamepadMouseMoveThread() {
		gamepadMouseThreadRunning = false;
	}
	
	public void startGamepadMouseMoveThread() {
		if (gamepadMouseThreadRunning) return;
		
		Thread t = new Thread() {
			@Override
			public void run() {
				while (gamepadMouseThreadRunning) {
					gamepadX += gamepadMouseMoveX;
					gamepadY += gamepadMouseMoveY;
	
					if (gamepadX<0) gamepadX = 0;
					if (gamepadY<0) gamepadY = 0;
	
					if (gamepadX>maxGamepadX) gamepadX = maxGamepadX;
					if (gamepadY>maxGamepadY) gamepadY = maxGamepadY;
					
					if (lastGamepadX != gamepadX || lastGamepadY != gamepadY) {
						lastGamepadX = gamepadX;
						lastGamepadY = gamepadY;
						listener.onMouseMove(gamepadX, gamepadY);
					}
					listener.onMouseMoveRelative(gamepadMouseMoveX, gamepadMouseMoveY);
					try {
						Thread.sleep(40);
					} catch (InterruptedException e) {}
				}
			}
		};
		gamepadMouseThreadRunning = true;
		t.start();
	}
	
	public boolean onGenericMotionEvent (final MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_MOVE && (event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK) {
			GenericGamepad gamepad = Mapper.instance.resolveGamepad(event.getDevice().getDescriptor(), event.getDeviceId());
			
			if (gamepad == null) {
				Log.d(LOGTAG, "Event from unknown descriptor " + event.getDevice().getDescriptor());
				return false;
			}
			
			float axisRx = 0;
			float axisRy = 0;
			
			if (gamepad.axisRx != 0) {
				axisRx = event.getAxisValue(Math.abs(gamepad.axisRx)) * (gamepad.axisRx > 0 ? 1 : -1);
			}
			if (gamepad.axisRx != 0) {
				axisRy = event.getAxisValue(Math.abs(gamepad.axisRy)) * (gamepad.axisRy > 0 ? 1 : -1);
			}
			if (Math.abs(axisRx) < 0.1) {
				axisRx = 0;
			}
			if (Math.abs(axisRy) < 0.1) {
				axisRy = 0;
			}

			gamepadMouseMoveX = axisRx * 2;
			gamepadMouseMoveY = axisRy * 2;
			
			float hatx = event.getAxisValue(MotionEvent.AXIS_HAT_X);
			float haty = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
			
			float axisx = event.getAxisValue(MotionEvent.AXIS_X);
			float axisy = event.getAxisValue(MotionEvent.AXIS_Y);
			
			listener.onAxisChange(gamepad, axisx, axisy, hatx, haty, axisRx, axisRy);
			
			String deviceName = event.getDevice().getName();
			int deviceId = event.getDevice().getId();
			
			float triggerL = event.getAxisValue(MotionEvent.AXIS_LTRIGGER);
			float triggerR = event.getAxisValue(MotionEvent.AXIS_RTRIGGER);
			if (triggerL == 0) {
				triggerL = event.getAxisValue(MotionEvent.AXIS_BRAKE);
			}
			if (triggerR == 0) {
				triggerR = event.getAxisValue(MotionEvent.AXIS_GAS);
			}
			
			listener.onTriggers(deviceName, deviceId, Math.abs(triggerL)>=deadzone, Math.abs(triggerR)>=deadzone);
			listener.onTriggersAnalog(gamepad, deviceId, triggerL, triggerR);

			return true;

		}
		return false;
	}
	
	public void analogToDigital(GenericGamepad gamepad, float x, float y) {
		if (Math.abs(x)<deadzone) x = 0;
		if (Math.abs(y)<deadzone) y = 0;
		
		Axis newAxisX = x == 0? Axis.CENTER: (x<0?Axis.MIN:Axis.MAX);
		Axis newAxisY = y == 0? Axis.CENTER: (y<0?Axis.MIN:Axis.MAX);
		
		if (axisX!=newAxisX) {
			if (axisX != Axis.CENTER) listener.onDigitalX(gamepad, axisX, false);
			axisX = newAxisX;
			if (axisX != Axis.CENTER) listener.onDigitalX(gamepad, axisX, true);
		}
		if (axisY!=newAxisY) {
			if (axisY != Axis.CENTER) listener.onDigitalY(gamepad, axisY, false);
			axisY = newAxisY;
			if (axisY != Axis.CENTER) listener.onDigitalY(gamepad, axisY, true);
		}
	}
	
}
