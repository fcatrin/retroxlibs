package xtvapps.core;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

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

	public static void toast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	public static void createNoMediaFile(File folder) {
		File nomediaFile = new File(folder, ".nomedia");
		if (!nomediaFile.exists()) {
			try {
				nomediaFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
