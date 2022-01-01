package retrox.utils.android.keyboard;

import android.app.Activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrox.utils.android.RetroXDialogs;
import retrox.utils.android.vinput.Mapper;
import xtvapps.core.Callback;
import xtvapps.core.CoreUtils;
import xtvapps.core.ListOption;
import xtvapps.core.SimpleCallback;
import xtvapps.core.android.AndroidCoreUtils;

public class KeyboardMappingUtils {

	private static KeyboardMappingPanel keyboardMappingPanel = null;
	
	public static void openKeymapSettings(final Activity activity, final KeyboardLayout[] keyboardLayout, final SimpleCallback returnHereCallback) {
		List<ListOption> options = new ArrayList<>();
		for(int i = 0; i< Mapper.MAX_PLAYERS; i++) {
			String gamepadNumber = String.valueOf(i+1);
			options.add(new ListOption(gamepadNumber, "Gamepad " + gamepadNumber));
		}
		
		
		RetroXDialogs.select(activity, "Select gamepad to map", options, new Callback<String>() {
			@Override
			public void onResult(String index) {
				final int gamepad = CoreUtils.str2i(index);
				if (gamepad > 0) {
					SimpleCallback resultCallback = new SimpleCallback() {

						@Override
						public void onResult() {
							String msg = "Keymap for gamepad " + gamepad + " has been saved";
							AndroidCoreUtils.toast(activity, msg);
							returnHereCallback.onResult();
						}
						
						@Override
						public void onError() {
							returnHereCallback.onResult();
						}
					};
					File gamepadFile = Mapper.getKeymapFile(gamepad);
					openKeyMapper(activity, gamepad, gamepadFile, keyboardLayout, resultCallback);
				}
			}
			
			@Override
			public void onError() {
				returnHereCallback.onResult();
			}
		});
	}

	
	private static void openKeyMapper(Activity activity, final int gamepad,
                                      final File keymapFile, KeyboardLayout[] keyboardLayout,
                                      final SimpleCallback returnCallback) {
		
		SimpleCallback resultCallback = new SimpleCallback() {

			@Override
			public void onResult() {
				keyboardMappingPanel = null;
				try {
					Mapper.loadVirtualEvents(gamepad-1, keymapFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				returnCallback.onResult();
			}
			
			@Override
			public void onError() {
				keyboardMappingPanel = null;
				returnCallback.onError();
			}
			
		};
		
		keyboardMappingPanel = new KeyboardMappingPanel(activity, keymapFile, keyboardLayout, resultCallback);
		keyboardMappingPanel.open();
	}
	
	public static boolean isKeyMapperVisible() {
		return keyboardMappingPanel!=null && keyboardMappingPanel.isVisible();
	}
	
	public static void closeKeyMapper() {
		if (keyboardMappingPanel!=null) keyboardMappingPanel.dismiss();
	}
}
