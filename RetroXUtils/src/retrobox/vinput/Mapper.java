package retrobox.vinput;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import retrobox.utils.RetroBoxUtils;

public class Mapper {
	
	public static final int MAX_PLAYERS = 4;
	private static final int MAX_MAPPINGS = 100;
	
	public enum ShortCut {NONE, LOAD_STATE, SAVE_STATE, SWAP_DISK, MENU, EXIT, SCREENSHOT};
	private static int keyShortCuts[] = {0, KeyEvent.KEYCODE_BUTTON_L2, KeyEvent.KEYCODE_BUTTON_R2, KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_L1};

	private static final String LOGTAG = "vinput.Mapper"; 
	private boolean inShortcutSequence = false;
	private boolean wasShortcutSent = false;
	public static VirtualEventDispatcher listener;
	public static AnalogGamepadListener analogListener;
	public static Mapper instance;
	private static GestureDetector mDetector;
	public static boolean joinPorts = false;

	private static GamepadMapping defaultGamepadMapping;

	public static GenericGamepad[] genericGamepads = new GenericGamepad[MAX_PLAYERS];
	public static GamepadKeyMapping[] knownKeyMappings = new GamepadKeyMapping[MAX_PLAYERS];
	public static Map<String, GamepadMapping> knownMappings = new HashMap<String, GamepadMapping>();
	
	static {
		for(int i=0; i<MAX_PLAYERS; i++) {
			genericGamepads[i] = new GenericGamepad();
			genericGamepads[i].player = i;
		}
	}

	public Mapper(Intent intent, VirtualEventDispatcher listener) {
		Mapper.instance = this;
		Mapper.listener = listener;
		
		defaultGamepadMapping = GamepadMapping.buildDefaultMapping();
		
		KeyTranslator.init();
		initVirtualEvents(intent);
		initGenericJoystick(intent);
	}
	
	public static void initGestureDetector(Activity activity) {
		SwipeListener listener = new SwipeListener() {

			@Override
			public void onSwipe(Swipe swipe) {
				if (swipe == Swipe.Left) sendShortcutMenu();
			}
			
		};
		
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mDetector = new GestureDetector(activity, new SwipeDetector(listener, metrics.widthPixels, metrics.heightPixels));
	}
	
	public static void setJoystickAnalogListener(AnalogGamepadListener analogListener) {
		Mapper.analogListener = analogListener;
	}
	
	private void initVirtualEvents(Intent intent) {
		Log.d("REMAP", "Intent " + intent.getExtras());
		for(int player = 0; player<MAX_PLAYERS; player++) {
			
			// first try to load from file
			String keymapFileKey = "KEYMAP_" + (player+1);
			String keymapFileName = intent.getStringExtra(keymapFileKey);
			if (keymapFileName!=null) {
				try {
					loadVirtualEvents(player, new File(keymapFileName));
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			// if anything fails, read the old way
	    	String prefix = "kmap" + (player+1);
		    for(int i=0; i<GamepadMapping.eventNames.length; i++) {
		    	String keyName = GamepadMapping.eventNames[i];
		    	String keyNameLinux = intent.getStringExtra(prefix + keyName); 
		    	Log.d("REMAP", "Keyname Linux  " + prefix + keyName + "=" + keyNameLinux);
		    	
		    	if (keyNameLinux!=null) {
		    		Log.d("REMAP", "Key for " + keyName + " is " + keyNameLinux);
		    		VirtualEvent event = KeyTranslator.translate(keyNameLinux);
		    		knownKeyMappings[player].virtualEvents[i] = event;
		    		Log.d("REMAP", "Linux key " + keyNameLinux + " mapped to event " + event);
		    	} else knownKeyMappings[player].virtualEvents[i] = null;
		    }
		}
	}
	
	public static void loadVirtualEvents(int player, File keymapFile) throws IOException {
		Map<String, String> mapping = RetroBoxUtils.loadMapping(keymapFile);
		Log.d("KEYMAPFILE", "load " + keymapFile.getAbsolutePath() + " as " + mapping);
		for(int i=0; i<GamepadMapping.eventNames.length; i++) {
	    	String keyName = GamepadMapping.eventNames[i];
	    	String keyNameLinux = mapping.get(keyName);
	    	
	    	VirtualEvent event = null;
	    	
	    	if (keyNameLinux!=null) {
	    		event = KeyTranslator.translate(keyNameLinux);
	    	}
	    	knownKeyMappings[player].virtualEvents[i] = event;
	    	Log.d("KEYMAPFILE", "Linux key " + keyNameLinux + " mapped to event " + event);
		}
		genericGamepads[player].keymapFile = keymapFile;
	}
	
	public static File getKeymapFile(int player) {
		return genericGamepads[player-1].keymapFile;
	}

	private void initGenericJoystick(Intent intent) {
		for(int mapping = 0; mapping < MAX_MAPPINGS; mapping++) {
			String prefix = "gmap_" + mapping;
			String deviceName = intent.getStringExtra(prefix);
			if (deviceName == null) return;

			GamepadMapping gamepadMapping = new GamepadMapping(deviceName);
			for(int i=0; i<GamepadMapping.eventNames.length; i++) {
				String eventName = GamepadMapping.eventNames[i];
				Integer keyCode = intent.getIntExtra(prefix + eventName, 0);
				if (keyCode>0) {
					Log.d(LOGTAG, "keyCode " +  prefix + " for " + eventName + ":" + keyCode);
					gamepadMapping.translatedCodes[i] = keyCode;
				}
			}
			knownMappings.put(deviceName, gamepadMapping);
		}
		
		for(int player = 0; player<MAX_PLAYERS; player++) { // TODO is this still used??
	    	String prefix = "j" + (player+1);
			genericGamepads[player].axisRx = intent.getIntExtra(prefix + "RX", 0) / 1000;
			genericGamepads[player].axisRy = intent.getIntExtra(prefix + "RY", 0) / 1000;
		}
		
	}
	
	public static int getTranslatedVirtualEvent(GenericGamepad gamepad, int genericCode) {
		GamepadMapping gamepadMapping = gamepad.getGamepadMapping();
		return gamepadMapping.getTranslatedVirtualEvent(genericCode);
	}
	
	public static VirtualEvent getVirtualEvent(GenericGamepad gamepad, int translatedCode) {
		GamepadMapping gamepadMapping = gamepad.getGamepadMapping();
		for(int i=0; i<gamepadMapping.translatedCodes.length; i++) {
			if (gamepadMapping.translatedCodes[i] == translatedCode) {
				VirtualEvent ev = knownKeyMappings[gamepad.player].virtualEvents[i];
				return ev;
			}
		}
		return null;
	}
	
	
	public static VirtualEvent getTargetEventIndex(GenericGamepad gamepad, int index) {
		return knownKeyMappings[gamepad.player].virtualEvents[index];
	}
	
	public static VirtualEvent getTargetEvent(GenericGamepad gamepad, int genericCode) {
		GamepadMapping gamepadMapping = gamepad.getGamepadMapping();
		for(int i=0; i<gamepadMapping.originCodes.length; i++) {
			if (gamepadMapping.originCodes[i] == genericCode) {
				VirtualEvent ev = knownKeyMappings[gamepad.player].virtualEvents[i];
				return ev;
			}
		}
		return null;
	}
	
	public static void setTargetEvent(GenericGamepad gamepad, int genericCode, VirtualEvent ev) {
		GamepadMapping gamepadMapping = gamepad.getGamepadMapping();

		for(int i=0; i<gamepadMapping.originCodes.length; i++) {
			if (gamepadMapping.originCodes[i] == genericCode) {
				knownKeyMappings[gamepad.player].virtualEvents[i] = ev;
				return;
			}
		}
	}
	
	
	private void sendKeyPress(GenericGamepad gamepad, int keyCode) {
		listener.sendKey(gamepad, keyCode, true);
		try {
			Thread.sleep(100);
		} catch (Exception e) {}
		listener.sendKey(gamepad, keyCode, false);
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public boolean handleKeyEvent(KeyEvent event, int keyCode, boolean down) {
		GenericGamepad gamepad = resolveGamepad(event.getDevice().getDescriptor(), event.getDeviceId());
		if (gamepad == null) return false;
		
		return handleKeyEvent(gamepad, keyCode, down);
	}
	
	public boolean handleTriggerEvent(String descriptor, int deviceId, boolean left, boolean right) {
		GenericGamepad gamepad = resolveGamepad(descriptor, deviceId);
		if (gamepad == null) return false;
		
		boolean leftChanged  = gamepad.getTriggerState(MotionEvent.AXIS_LTRIGGER) != left;
		boolean rightChanged = gamepad.getTriggerState(MotionEvent.AXIS_RTRIGGER) != right;
		if (leftChanged) {
			gamepad.setTriggerState(MotionEvent.AXIS_LTRIGGER, left);
			VirtualEvent ev = getTargetEvent(gamepad, KeyEvent.KEYCODE_BUTTON_L2);
			if (ev!=null) ev.sendEvent(gamepad, left);
		}
		if (rightChanged) {
			gamepad.setTriggerState(MotionEvent.AXIS_RTRIGGER, right);
			VirtualEvent ev = getTargetEvent(gamepad, KeyEvent.KEYCODE_BUTTON_R2);
			if (ev!=null) ev.sendEvent(gamepad, right);
		}
		return true;
	}
	
	public boolean handleKeyEvent(GenericGamepad gamepad, int keyCode, boolean down) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) { // NVIDIA Shield
			if (!down) {
				sendShortcutMenu();
			}
			return true;
		}

		if (gamepad == null) return false;
		
		if (handleShortcut(gamepad, keyCode, down)) return true;
		
		VirtualEvent ev = getVirtualEvent(gamepad, keyCode);
		if (ev != null) {
			ev.sendEvent(gamepad, down);
			return true;
		}
		return false;
	}
	
	protected boolean handleDetectedShortcut(int keyCode, boolean down) {
		ShortCut shortcut = ShortCut.NONE;
		for(int i=1; i<keyShortCuts.length; i++) {
			if (keyShortCuts[i] == keyCode) {
				shortcut = ShortCut.values()[i];
				break;
			}
		}
		if (shortcut!=ShortCut.NONE) {
			Log.d(LOGTAG, "Shortcut sent: " + shortcut + " pressed:" + down);
			return listener.handleShortcut(shortcut, down);
		}
		return false;
	}
	
	public static void sendShortcutMenu() {
		listener.handleShortcut(ShortCut.MENU, false);
	}
	
	protected void sendStartKeyPress(GenericGamepad gamepad) {
		VirtualEvent ve = getTargetEvent(gamepad, KeyEvent.KEYCODE_BUTTON_START);
		if (ve!=null) sendKeyPress(gamepad, ve.keyCode);
	}
	
	protected int getOriginCode(GenericGamepad gamepad, int keyCode) {
		return gamepad.getGamepadMapping().getOriginCode(keyCode);
	}
	
	public static int getOriginCodeByIndex(GenericGamepad gamepad, int index) {
		return gamepad.getGamepadMapping().originCodes[index];
	}

	protected boolean isStartButton(GenericGamepad gamepad, int keyCode) {
		return getOriginCode(gamepad, keyCode) == KeyEvent.KEYCODE_BUTTON_START;
	}
	
	boolean isL3down = false;
	boolean isR3down = false;
	
	public boolean handleShortcut(GenericGamepad gamepad, int keyCode, boolean down) {
		if (gamepad == null) return false;
		
		boolean wasScreenshotComboDown = isL3down && isR3down;

		if (getOriginCode(gamepad, keyCode) == KeyEvent.KEYCODE_BUTTON_THUMBL) isL3down = down;
		if (getOriginCode(gamepad, keyCode) == KeyEvent.KEYCODE_BUTTON_THUMBR) isR3down = down;
		
		if (!wasScreenshotComboDown) {
			if (isL3down && isR3down) {
				boolean handled = listener.handleShortcut(ShortCut.SCREENSHOT, down);
				if (handled) return true;
			}
		}
		
		if (isStartButton(gamepad, keyCode)) {
			if (down) {
				inShortcutSequence = true;
				return true;
			} else {
				inShortcutSequence = false; 
				if (!wasShortcutSent) sendStartKeyPress(gamepad);  // send pending Start if no shortcut was generated
				wasShortcutSent = false;
				return true;
			}
		}
		if (inShortcutSequence) {
			wasShortcutSent = handleDetectedShortcut(getOriginCode(gamepad, keyCode), down);
			if (wasShortcutSent && !down) inShortcutSequence = false; // make sure that we clean this. Another window could receive the keyUp
			return wasShortcutSent;
		}
		return false;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public boolean isSystemKey(KeyEvent event, int keyCode) {
		GenericGamepad gamepad = event==null ? null : resolveGamepad(event.getDevice().getDescriptor(), event.getDeviceId());
		if (gamepad!=null && gamepad.getGamepadMapping().getOriginCode(keyCode)!=0) return false;
		
		return 
			keyCode == KeyEvent.KEYCODE_BACK || 
			keyCode == KeyEvent.KEYCODE_MENU ||
			keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || 
			keyCode == KeyEvent.KEYCODE_VOLUME_UP || 
			keyCode == KeyEvent.KEYCODE_VOLUME_MUTE;
	}

	public static void sendJoystickAnalogMove(GenericGamepad gamepad, float xval, float yval) {
		if (analogListener!=null) analogListener.onAxisChange(gamepad, xval,  yval, xval, yval, 0, 0);
	}
	
	public void onTouchEvent(MotionEvent me) {
		if (mDetector!=null) mDetector.onTouchEvent(me);
	}
	
	public static GenericGamepad resolveGamepad(String deviceName, int deviceId) {
		for(int i=0; i<MAX_PLAYERS; i++) {
			GenericGamepad gamepad = genericGamepads[i];
			if (deviceName.equals(gamepad.getDeviceName()) && (joinPorts || gamepad.getDeviceId() == 0 || gamepad.getDeviceId() == deviceId)) {
				gamepad.setDeviceId(deviceId);
				return gamepad;  
			}
		}
		return null;
	}
	
	public static void registerGamepad(String deviceName, int deviceId) {
		GenericGamepad existingGamepad = resolveGamepad(deviceName, deviceId);
		if (existingGamepad != null) return;
		
		for(int i=0; i<MAX_PLAYERS; i++) {
			GenericGamepad gamepad = genericGamepads[i];
			if (gamepad.getDeviceName() == null) {
				gamepad.setDeviceName(deviceName);
				gamepad.setDeviceId(deviceId);
				
				GamepadMapping gamepadMapping = knownMappings.get(deviceName);
				if (gamepadMapping == null) gamepadMapping = defaultGamepadMapping;
				
				gamepad.setGamepadMapping(gamepadMapping);
				
				Log.d(LOGTAG, "Register gamepad for player " + (i+1) + " device:" + deviceName + " deviceId:" + deviceId + " mapping:" + gamepadMapping.getDeviceName());
			}
		}		
	}
	
	public static boolean hasGamepads() {
		return hasGamepad(0) || hasGamepad(1);  // check player one or two
	}

	public static boolean hasGamepad(int player) {
		return genericGamepads[player].getDeviceName()!=null;
	}
}
