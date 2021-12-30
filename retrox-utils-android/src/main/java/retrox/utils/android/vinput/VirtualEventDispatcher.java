package retrox.utils.android.vinput;

import retrox.utils.android.vinput.GamepadMapping.Analog;

public interface VirtualEventDispatcher {
	void sendKey(GamepadDevice gamepad, int keyCode, boolean down);
	void sendMouseButton(VirtualEvent.MouseButton button, boolean down);
	boolean handleShortcut(Mapper.ShortCut shortcut, boolean down);
	void sendAnalog(GamepadDevice gamepad, Analog index, double x, double y, double hatx, double haty);
}
