package retrox.utils.android.vinput;

public interface AnalogGamePadListener {
	void onAxisChange(GamepadDevice gamePad, float axisX, float axisY, float hatX, float hatY, float rAxisX, float rAxisY);
	void onMouseMove(int mouseX, int mouseY);
	void onMouseMoveRelative(float mouseX, float mouseY);
	void onDigitalX(GamepadDevice gamePad, AnalogGamePad.Axis axis, boolean on);
	void onDigitalY(GamepadDevice gamePad, AnalogGamePad.Axis axis, boolean on);
	void onTriggers(String deviceName, int deviceId, boolean left, boolean right);
	void onTriggersAnalog(GamepadDevice gamePad, int deviceId, float left, float right);
}
