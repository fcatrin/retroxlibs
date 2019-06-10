package retrobox.keyboard;

import android.app.Activity;
import android.widget.TextView;
import retrobox.utils.R;
import retrobox.utils.RetroBoxUtils;
import xtvapps.core.AndroidFonts;

public class KeyboardMapper {
	public KeyboardMapper(Activity activity) {
		TextView txtMapperLeft   = (TextView)activity.findViewById(R.id.txtGamepadMappingMessage);
		TextView txtMapperButton = (TextView)activity.findViewById(R.id.txtGamepadButtonName);

		AndroidFonts.setViewFont(txtMapperLeft,   RetroBoxUtils.FONT_DEFAULT_R);
		AndroidFonts.setViewFont(txtMapperButton, RetroBoxUtils.FONT_DEFAULT_R);

	}
}
