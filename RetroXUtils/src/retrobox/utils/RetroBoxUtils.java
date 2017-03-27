package retrobox.utils;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class RetroBoxUtils {
	public static String FONT_DEFAULT_M = "ubuntu/ubuntu-m.ttf";
	public static String FONT_DEFAULT_B = "ubuntu/ubuntu-b.ttf";
	public static String FONT_DEFAULT_R = "ubuntu/ubuntu-r.ttf";
	public static String FONT_RETROBOX = "edunline.ttf";
	
	private static final long SIZE_TERABYTE = 1024*1024*1024*1024l;
	private static final long SIZE_GIGABYTE = 1024*1024*1024;
	private static final long SIZE_MEGABYTE = 1024*1024;
	private static final long SIZE_KILOBYTE = 1024;
	
	private static String size2humanPart(long size, long unit, String unitName, boolean exact) {
		DecimalFormat df = new DecimalFormat("#.0");
		String text = df.format((float)size / unit);
		if (!exact) text = text.replace(".0", "");
		return text + " " + unitName;
	}
	
	public static String size2human(long size) {
		return size2human(size, false);
	}
	public static String size2human(long size, boolean exact) {
		if (size > SIZE_TERABYTE) {
			return size2humanPart(size, SIZE_TERABYTE, "TB", exact);
		}
		if (size > SIZE_GIGABYTE) {
			return size2humanPart(size, SIZE_GIGABYTE, "GB", exact);
		}
		if (size > SIZE_MEGABYTE) {
			return size2humanPart(size, SIZE_MEGABYTE, "MB", exact);
		}
		if (size > SIZE_KILOBYTE) {
			return size2humanPart(size, SIZE_KILOBYTE, "KB", exact);
		}
		return size + " B";
	}
	
	public static void openWeb(Activity context, String url) {
		try {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		} catch (Exception e) {
			String msg = context.getString(R.string.no_browser)
					.replace("{url}", url);
			RetroBoxDialog.showAlert(context, msg);
		}
	}

	public static void runOnBackground(final Activity activity, final ThreadedBackgroundTask task) {
		Thread t = new Thread() {
			@Override
			public void run() {
				task.onBackground();
				activity.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						task.onUIThread();
					}
				});
			}
		};
		t.start();
	}
}
