package retrobox.vinput;

import retrobox.vinput.GamepadMapping.Analog;
import retrobox.vinput.Mapper.ShortCut;
import retrobox.vinput.VirtualEvent.MouseButton;

public interface VirtualEventDispatcher {
	void sendKey(GenericGamepad gamepad, int keyCode, boolean down);
	void sendMouseButton(MouseButton button, boolean down);
	boolean handleShortcut(ShortCut shortcut, boolean down);
	void sendAnalog(GenericGamepad gamepad, Analog index, double x, double y, double hatx, double haty);
}
