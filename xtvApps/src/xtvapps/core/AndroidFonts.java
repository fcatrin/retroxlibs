package xtvapps.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public final class AndroidFonts {
	private static final String LOGTAG = AndroidFonts.class.getSimpleName();
	
	private static Map<String, Typeface> knownFonts = new HashMap<String, Typeface>();
	
	private AndroidFonts() {
	}
	
	public static Typeface getFont(Context context, String name) {
		Typeface font = knownFonts.get(name);
		if (font == null) {
			if (name.startsWith("/")) {
				font = Typeface.createFromFile(name);
			} else {
				font = Typeface.createFromAsset(context.getAssets(),"fonts/" + name);
			}
			knownFonts.put(name, font);
		}
		//Log.d(LOGTAG, "typeface for " + name + " is " + (font == null?"null":"not null"));
		return font;
	}
	
	public static void setViewFont(View v, File file) {
		setViewFont(v, file.getAbsolutePath());
	}
	
	public static void setViewFont(View v, String name) {
		if (!(v instanceof TextView)) {
			Log.d(LOGTAG, "view " + v.getId() + " is not a TextView");
			return;
		}
		TextView tv = (TextView)v;
		Typeface tf = getFont(v.getContext(), name);
		tv.setTypeface(tf);
	}

	public static void setViewFontRecursive(View v, String name) {
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup)v;
			for(int i=0; i<vg.getChildCount(); i++) {
				View child = vg.getChildAt(i);
				setViewFontRecursive(child, name);
			}
		} else {
			setViewFont(v, name);
		}
	}
}
