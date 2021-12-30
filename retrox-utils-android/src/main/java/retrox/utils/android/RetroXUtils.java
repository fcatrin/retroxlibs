package retrox.utils.android;

import android.app.Activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import xtvapps.core.CoreUtils;
import xtvapps.core.FileUtils;
import xtvapps.core.Log;
import xtvapps.core.SimpleBackgroundTask;
import xtvapps.core.android.AndroidCoreUtils;
import xtvapps.core.net.NetworkUtils;

public class RetroXUtils {
	private static final String LOGTAG = RetroXUtils.class.getSimpleName();

	public static final String FONT_DEFAULT_M = "ubuntu/ubuntu-m.ttf";
	public static final String FONT_DEFAULT_B = "ubuntu/ubuntu-b.ttf";
	public static final String FONT_DEFAULT_R = "ubuntu/ubuntu-r.ttf";

	public static final String postTraceUrl = null;

	public static void initExceptionHandler(Activity activity, String appName, String userName) {
		final File traceFile = new File(activity.getFilesDir(), "rxtrace.log");
		if (traceFile.exists()) {
			try {
				String trace = FileUtils.loadString(traceFile);
				//noinspection ResultOfMethodCallIgnored
				traceFile.delete();
				sendTrace(activity, appName, userName, trace);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		final String packageName = activity.getPackageName();

		final Thread.UncaughtExceptionHandler oldHandler =
	            Thread.getDefaultUncaughtExceptionHandler();

		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			saveTrace(traceFile, throwable, packageName);
			throwable.printStackTrace();
			if (oldHandler!=null) {
				oldHandler.uncaughtException(thread, throwable);
			} else {
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(0);
			}
		});
	}
	
	private static void saveTrace(File traceFile, Throwable e, String packageName) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		Throwable cause = e.getCause();
		if (cause!=null) {
			cause.printStackTrace(pw);
		} else {
			e.printStackTrace(pw);
		}
		
		String trace = e.getMessage() + "\n\n" + e.toString() + "\n\n" + sw.toString();
		trace += "\n\n";
		try {
			Log.d(LOGTAG, "Saving trace to " + traceFile.getAbsolutePath());
			FileUtils.saveString(traceFile, trace);
			Log.d(LOGTAG, "Trace saved to " + traceFile.getAbsolutePath());
		} catch (IOException e1) {
			Log.e(LOGTAG, "Error saving trace to " + traceFile.getAbsolutePath(), e1);
		}
	}
	
	private static void sendTrace(Activity activity, final String appName, final String userName, final String trace) {
		SimpleBackgroundTask task = new SimpleBackgroundTask() {
			@Override
			public void onBackgroundTask() throws Exception {
				postTrace(appName, userName, trace);
			}
		};
		task.execute();
	}
	
	private static void postTrace(String appName, String userName, String trace) throws IOException {
		//noinspection ConstantConditions
		if (postTraceUrl == null) return;
		
		if (CoreUtils.isEmptyString(userName)) userName = "user";
		String deviceName = AndroidCoreUtils.buildDeviceName();
		
		Map<String, String> data = new HashMap<>();
		data.put("trace", trace);
		data.put("app", appName);
		data.put("email", URLEncoder.encode(userName, "UTF-8"));
		data.put("device", URLEncoder.encode(deviceName, "UTF-8"));
		
		String url = postTraceUrl;
		Log.d(LOGTAG, "Post trace to " + url);
		NetworkUtils.httpPost(url, null, data);
	}

	public static Map<String, String> loadMapping(File file) throws IOException {
		Properties p = new Properties();
		p.load(new FileInputStream(file));
		
		Map<String, String> result = new HashMap<>();
		for(Object oKey : p.keySet()) {
			String key = oKey.toString();
			result.put(key, p.getProperty(key));
		}
		return result;
	}
	
	public static void saveMapping(File file, Map<String, String> map) throws IOException {
		Properties p = new Properties();
		p.putAll(map);
		p.store(new FileOutputStream(file), null);
	}
}
