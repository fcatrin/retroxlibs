package retrobox.vinput;

import retrobox.vinput.AnalogGamepad.Axis;

public interface AnalogGamepadListener {
	public void onAxisChange(GenericGamepad gamepad, float axisx, float axisy, float hatX, float hatY, float raxisx, float raxisy);
	public void onMouseMove(int mousex, int mousey);
	public void onMouseMoveRelative(float mousex, float mousey);
	public void onDigitalX(GenericGamepad gamepad, Axis axis, boolean on);
	public void onDigitalY(GenericGamepad gamepad, Axis axis, boolean on);
	public void onTriggers(String deviceDescriptor, int deviceId, boolean left, boolean right);
}
