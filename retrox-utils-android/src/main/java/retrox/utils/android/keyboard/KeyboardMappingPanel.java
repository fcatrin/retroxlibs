package retrox.utils.android.keyboard;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrox.utils.android.GamepadView;

import retrox.utils.android.R;
import retrox.utils.android.RetroXDialogs;
import retrox.utils.android.RetroXUtils;
import xtvapps.core.CoreUtils;
import xtvapps.core.SimpleCallback;
import xtvapps.core.android.AndroidFonts;

public class KeyboardMappingPanel {
	
	// keys from retrobox.vinput.KeyTranslator
	// special keys can be added with retrobox.vinput.KeyTranslator.addTranslation
	
	final Map<String, String> keymap = new HashMap<>();
	
	private int currentButtonIndex = 0;
	
	private final Activity activity;
	private final GamepadView gamepadView;
	
	private final GamepadView.EventId[] orderedEvents = {
			GamepadView.EventId.LEFT, GamepadView.EventId.UP,
			GamepadView.EventId.DOWN, GamepadView.EventId.RIGHT,
			GamepadView.EventId.BTN_SELECT, GamepadView.EventId.BTN_START,
			GamepadView.EventId.BTN_X, GamepadView.EventId.BTN_Y,
			GamepadView.EventId.BTN_B, GamepadView.EventId.BTN_A,
			GamepadView.EventId.BTN_L2, GamepadView.EventId.BTN_L1,
			GamepadView.EventId.BTN_R2, GamepadView.EventId.BTN_R1,
			GamepadView.EventId.BTN_L3, GamepadView.EventId.BTN_R3,
			GamepadView.EventId.RX, GamepadView.EventId.RY
	};

	private final File keymapFile;
	private final KeyboardView kbView;
	private final SimpleCallback callback;

	public KeyboardMappingPanel(final Activity activity, File keymapFile, KeyboardLayout[] keyboardLayout, final SimpleCallback callback) {
		this.activity = activity; 
		this.keymapFile = keymapFile;
		this.callback = callback;

		load();
		
		TextView txtMapperLeft   = activity.findViewById(R.id.txtGamepadMappingMessage);
		TextView txtMapperButton = activity.findViewById(R.id.txtGamepadButtonName);

		AndroidFonts.setViewFont(txtMapperLeft,   RetroXUtils.FONT_DEFAULT_R);
		AndroidFonts.setViewFont(txtMapperButton, RetroXUtils.FONT_DEFAULT_R);

		kbView = activity.findViewById(R.id.keyboard_map_view);

		kbView.init(activity, keyboardLayout);
		
		kbView.setOnVirtualKeyListener(this::setKeyMap);
		
		Button btnButtonPrev = activity.findViewById(R.id.btnGamepadButtonPrev);
		Button btnButtonNext = activity.findViewById(R.id.btnGamepadButtonNext);
		
		btnButtonPrev.setOnClickListener(arg0 -> switchToButton(-1));
		btnButtonNext.setOnClickListener(arg0 -> switchToButton(1));
		
		gamepadView = activity.findViewById(R.id.keyboardMapGamepad);
		gamepadView.init();
		gamepadView.layout();
		
		switchToButton(0);

		updateButtonLabels();
		
		Button btnSave = activity.findViewById(R.id.btnKeymapSave);
		Button btnClose = activity.findViewById(R.id.btnKeymapClose);
		
		btnSave.setOnClickListener(v -> {
            try {
                save();
                close();
                callback.onResult();
            } catch(Exception e) {
                String msg = "There was an error trying to save the keymap: " + e.getMessage();
                RetroXDialogs.message(activity, msg);
            }
        });
		
		btnClose.setOnClickListener(v -> dismiss());
	}
	
	public boolean isVisible() {
		return activity.findViewById(R.id.keyboard_mapping_panel).getVisibility() == View.VISIBLE;	
	}

	public void dismiss() {
		close();
		callback.onError();
	}
	
	protected void setKeyMap(String code) {
		int eventIndex = orderedEvents[currentButtonIndex].ordinal();
		keymap.put(GamepadView.eventNames[eventIndex], code);
		
		GamepadView.ButtonLabelBox button = gamepadView.getButton(eventIndex);
		button.setLabel(event2human(code));
		
		updateButtonDisplay();
	}

	public void open() {
		setVisible(true);
		
		kbView.post(() -> kbView.getChildAt(0).requestFocus());
	}

	private void setVisible(boolean visible) {
		int visibility = visible ? View.VISIBLE : View.GONE;
		activity.findViewById(R.id.keyboard_mapping_panel).setVisibility(visibility);
	}
	
	private void close() {
		setVisible(false);
	}

	private void switchToButton(int delta) {
		currentButtonIndex += delta;
		if (currentButtonIndex<0) currentButtonIndex = orderedEvents.length - 1;
		if (currentButtonIndex>= orderedEvents.length) currentButtonIndex = 0;
		
		updateButtonDisplay();
	}
	
	private void updateButtonDisplay() {
		int eventIndex = orderedEvents[currentButtonIndex].ordinal();
		GamepadView.ButtonLabelBox button = gamepadView.getButton(eventIndex);
		gamepadView.highLightButton(button);

		String buttonName = event2human(GamepadView.eventNames[eventIndex]);
		TextView buttonNameView = activity.findViewById(R.id.txtGamepadButtonName);

		String buttonMapName = button.getLabel();
		if (CoreUtils.isEmptyString(buttonMapName)) buttonMapName = " is not mapped";
		else buttonMapName = " is mapped to " + buttonMapName;

		buttonNameView.setText(buttonName + buttonMapName);
		
	}
	
	private void updateButtonLabels() {
		for(int i=0; i<GamepadView.eventNames.length; i++) {
			String label = keymap.get(GamepadView.eventNames[i]);
			if (label == null) label = "";
			
			GamepadView.ButtonLabelBox button = gamepadView.getButton(i);
			button.setLabel(event2human(label));
		}
	}
	
	private String event2human(String event) {
		if (event.startsWith("KEY_")) return event.substring("KEY_".length());
		if (event.startsWith("ATR_")) return event.substring("ATR_".length());
		if (event.startsWith("BTN_")) return "Button " + event.substring("BTN_".length());
		if (event.equals("RX")) return "Analog Right X";
		if (event.equals("RY")) return "Analog Right Y";
		if (event.equals("TL")) return "Thumb Left";
		if (event.equals("TR")) return "Thumb Right";
		if (event.equals("TL2")) return "Thumb Left 2";
		if (event.equals("TR2")) return "Thumb Right 2";
		if (event.equals("TL3")) return "Thumb Left 3";
		if (event.equals("TR3")) return "Thumb Right 3";
		return event;
	}
	
	private void save() throws IOException {
		RetroXUtils.saveMapping(keymapFile, keymap);
	}
	
	private void load() {
		try {
			keymap.clear();
			keymap.putAll(RetroXUtils.loadMapping(keymapFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
