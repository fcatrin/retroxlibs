package xtvapps.core;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public final class AndroidCoreUtils {
	
	private static final String LOGTAG = AndroidCoreUtils.class.getSimpleName();
	
	private AndroidCoreUtils(){}
	
	public static void openGooglePlay(Context context, String packageName) {
		try {
			Log.d(LOGTAG, "opening market for " + packageName);
		    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
		} catch (android.content.ActivityNotFoundException anfe) {
		    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
		}
	}

}
