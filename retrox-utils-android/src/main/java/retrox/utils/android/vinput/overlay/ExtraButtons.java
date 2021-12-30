package retrox.utils.android.vinput.overlay;

import android.content.Context;
import android.view.KeyEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrox.utils.android.vinput.Mapper;

public class ExtraButtons {
	
	public static void initExtraButtons(Context context, String json, int w, int h, boolean isMouseOnly){
		int maxButtons = 10;
		
		final float scale = context.getResources().getDisplayMetrics().density;
		int margin = (int)(10 * scale);
		int gap = (int)(4 * scale);
		int size = (w - (maxButtons-1)*gap + margin*2 ) / maxButtons;

		if (json != null && json.trim().length() >= 0) {
			try {
				int nTopButtons = 0;
				JSONArray a = new JSONArray(json);
				for(int i=0; i<a.length(); i++) {
					JSONObject o = a.getJSONObject(i);
					String key = o.getString("key");
					if (!key.startsWith("MOUSE_") && !key.startsWith("BTN_")) nTopButtons++;
				}
				
				int left = nTopButtons == 0 ? 0 : (w - (nTopButtons*size) - (nTopButtons-1) * gap) / 2;
				for(int i=0; i<a.length(); i++) {
					JSONObject o = a.getJSONObject(i);
					String name = o.getString("name");
					String key = o.getString("key");
					if (key.startsWith("MOUSE_")) {
						if (isMouseOnly) OverlayExtra.addExtraButton(OverlayExtra.createMouseButton(name, key, w, margin, h, size));
					} else if (key.startsWith("BTN_")) {
						String trigger = null;
						if (key.equals("BTN_A")) trigger = "a";
						if (key.equals("BTN_B")) trigger = "b";
						if (key.equals("BTN_X")) trigger = "x";
						if (key.equals("BTN_Y")) trigger = "y";
						if (key.equals("BTN_TL")) trigger = "l";
						if (key.equals("BTN_TR")) trigger = "r";
						if (key.equals("BTN_TL2")) trigger = "l2";
						if (key.equals("BTN_TR2")) trigger = "r2";
						if (trigger!=null) {
							if (!name.equals("_hide_")) Overlay.setTriggerLabel(trigger, name);
							continue;
						}
						int dpadEvent = 0;
						if (key.equals("BTN_UP")) dpadEvent = KeyEvent.KEYCODE_DPAD_UP;
						if (key.equals("BTN_DOWN")) dpadEvent = KeyEvent.KEYCODE_DPAD_DOWN;
						if (key.equals("BTN_LEFT")) dpadEvent = KeyEvent.KEYCODE_DPAD_LEFT;
						if (key.equals("BTN_RIGHT")) dpadEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
						if (dpadEvent>0 && name.equals("_hide_")) {
							Mapper.setTargetEvent(Mapper.gamepadDevices[0], dpadEvent, null);
						}
					} else {
						OverlayExtra.addExtraButton(OverlayExtra.createExtraButton(name, key, left, margin, size));
						left += size + gap;
					}
				}
			} catch (JSONException je) {
				je.printStackTrace();
			}
		}

	}
	


}
