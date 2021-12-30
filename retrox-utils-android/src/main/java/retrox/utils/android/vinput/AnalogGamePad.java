package retrox.utils.android.vinput;

import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import xtvapps.core.CoreUtils;

public class AnalogGamePad {
	private static final String LOGTAG = AnalogGamePad.class.getSimpleName();
	public enum Axis {MIN, CENTER, MAX}
	
	final float deadZone = 0.2f;
	
	Axis axisX = Axis.CENTER;
	Axis axisY = Axis.CENTER;
	
	static int maxGamePadX = 640;
	static int maxGamePadY = 480;
	static int lastGamePadX = 0;
	static int lastGamePadY = 0;
	static int gamePadX = 0;
	static int gamePadY = 0;
	static float gamePadMouseMoveX = 0;
	static float gamePadMouseMoveY = 0;
	boolean gamePadMouseThreadRunning = false;
	private final AnalogGamePadListener listener;
	
	public AnalogGamePad(int maxX, int maxY, AnalogGamePadListener listener) {
		this.listener = listener;
		maxGamePadX = maxX;
		maxGamePadY = maxY;
		lastGamePadX = maxX / 2;
		lastGamePadY = maxY / 2;
		gamePadX = lastGamePadX;
		gamePadY = lastGamePadY;
	}

	public void stopGamePadMouseMoveThread() {
		gamePadMouseThreadRunning = false;
	}
	
	public void startGamePadMouseMoveThread() {
		if (gamePadMouseThreadRunning) return;
		
		Thread t = new Thread() {
			@Override
			public void run() {
				while (gamePadMouseThreadRunning) {
					gamePadX += gamePadMouseMoveX;
					gamePadY += gamePadMouseMoveY;
	
					if (gamePadX <0) gamePadX = 0;
					if (gamePadY <0) gamePadY = 0;
	
					if (gamePadX > maxGamePadX) gamePadX = maxGamePadX;
					if (gamePadY > maxGamePadY) gamePadY = maxGamePadY;
					
					if (lastGamePadX != gamePadX || lastGamePadY != gamePadY) {
						lastGamePadX = gamePadX;
						lastGamePadY = gamePadY;
						listener.onMouseMove(gamePadX, gamePadY);
					}
					listener.onMouseMoveRelative(gamePadMouseMoveX, gamePadMouseMoveY);
					CoreUtils.sleep(40);
				}
			}
		};
		gamePadMouseThreadRunning = true;
		t.start();
	}
	
	public boolean onGenericMotionEvent (final MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_MOVE && (event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK) {
			GamepadDevice gamePad = Mapper.resolveGamepadByName(event.getDevice().getName(), event.getDeviceId());
			
			if (gamePad == null) {
				Log.d(LOGTAG, "Event from unknown device " + event.getDevice().getName());
				return false;
			}
			
			float axisRx = 0;
			float axisRy = 0;
			
			GamepadMapping gamepadMapping = gamePad.getGamepadMapping();
			
			if (gamepadMapping.axisRx != 0) {
				axisRx = event.getAxisValue(Math.abs(gamepadMapping.axisRx)) * (gamepadMapping.axisRx > 0 ? 1 : -1);
			}
			if (gamepadMapping.axisRx != 0) {
				axisRy = event.getAxisValue(Math.abs(gamepadMapping.axisRy)) * (gamepadMapping.axisRy > 0 ? 1 : -1);
			}
			if (Math.abs(axisRx) < 0.1) {
				axisRx = 0;
			}
			if (Math.abs(axisRy) < 0.1) {
				axisRy = 0;
			}

			gamePadMouseMoveX = axisRx * 2;
			gamePadMouseMoveY = axisRy * 2;
			
			float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
			float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
			
			float axisX = event.getAxisValue(MotionEvent.AXIS_X);
			float axisY = event.getAxisValue(MotionEvent.AXIS_Y);
			
			listener.onAxisChange(gamePad, axisX, axisY, hatX, hatY, axisRx, axisRy);
			
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
			
			listener.onTriggers(deviceName, deviceId, Math.abs(triggerL)>= deadZone, Math.abs(triggerR)>= deadZone);
			listener.onTriggersAnalog(gamePad, deviceId, triggerL, triggerR);

			return true;

		}
		return false;
	}
	
	public void analogToDigital(GamepadDevice gamePad, float x, float y) {
		if (Math.abs(x)< deadZone) x = 0;
		if (Math.abs(y)< deadZone) y = 0;
		
		Axis newAxisX = x == 0? Axis.CENTER: (x<0?Axis.MIN:Axis.MAX);
		Axis newAxisY = y == 0? Axis.CENTER: (y<0?Axis.MIN:Axis.MAX);
		
		if (axisX!=newAxisX) {
			if (axisX != Axis.CENTER) listener.onDigitalX(gamePad, axisX, false);
			axisX = newAxisX;
			if (axisX != Axis.CENTER) listener.onDigitalX(gamePad, axisX, true);
		}
		if (axisY!=newAxisY) {
			if (axisY != Axis.CENTER) listener.onDigitalY(gamePad, axisY, false);
			axisY = newAxisY;
			if (axisY != Axis.CENTER) listener.onDigitalY(gamePad, axisY, true);
		}
	}
	
}
