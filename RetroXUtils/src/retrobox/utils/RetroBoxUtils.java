package retrobox.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import xtvapps.core.NetworkUtils;
import xtvapps.core.Utils;

public class RetroBoxUtils {
	private static final String LOGTAG = RetroBoxUtils.class.getSimpleName();
	private static final String URL_POST_TRACE = "http://retrobox2.xtvapps.com/services/postErrorAddOn.php";

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
	
	
	private static String getLastFatalLog(Activity activity) {
		String fatalLog = logcatFatal();
		if (!Utils.isEmptyString(fatalLog)) {
			SharedPreferences sharedPreferences = activity.getSharedPreferences("fatal_traces", Activity.MODE_PRIVATE);
			String hash = Utils.md5(fatalLog);
			String savedHash = sharedPreferences.getString("last_fatal_trace", ""); 
			if (!hash.equals(savedHash)) {
				Editor editor = sharedPreferences.edit();
				editor.putString("last_fatal_trace", hash);
				editor.commit();
				return fatalLog;
			}
		}
		return null;
	}
	
	public static void initExceptionHandler(Activity activity, String appName, String userName) {
		final File traceFile = new File(activity.getFilesDir(), "rxtrace.log");
		String trace = "";
		if (traceFile.exists()) {
			try {
				trace = Utils.loadString(traceFile);
				traceFile.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		String fatalLog = getLastFatalLog(activity);
		if (!Utils.isEmptyString(fatalLog)) {
			if (!Utils.isEmptyString(trace)) {
				trace += "\n\n";
			}
			trace += fatalLog;
		}
		
		if (!Utils.isEmptyString(trace)) {
			sendTrace(activity, appName, userName, trace);
		}
		
		
		final String packageName = activity.getPackageName();

		final Thread.UncaughtExceptionHandler oldHandler =
	            Thread.getDefaultUncaughtExceptionHandler();

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread thread, Throwable throwable) {
				if (throwable != null) {
					saveTrace(traceFile, throwable, packageName);
					throwable.printStackTrace();
					if (oldHandler!=null) {
	            		oldHandler.uncaughtException(thread, throwable);
	            	} else {
						android.os.Process.killProcess(android.os.Process.myPid());
						System.exit(0);
	                }
				}
			}
		});
	}
	
	private static String saveTrace(File traceFile, Throwable e, String packageName) {
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
			Utils.saveString(traceFile, trace);
			Log.d(LOGTAG, "Trace saved to " + traceFile.getAbsolutePath());
		} catch (IOException e1) {
			Log.e(LOGTAG, "Error saving trace to " + traceFile.getAbsolutePath(), e1);
		}
		return trace;
	}
	
	private static void sendTrace(Activity activity, final String appName, final String userName, final String trace) {
		ThreadedBackgroundTask task = new ThreadedBackgroundTask() {

			@Override
			public void onBackground() {
				try {
					postTrace(appName, userName, trace);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onUIThread() {}
			
		};
		Log.d(LOGTAG, "Posting trace on background");
		runOnBackground(activity, task);
	}
	
	private static void postTrace(String appName, String userName, String trace) throws IOException {
		
		if (Utils.isEmptyString(userName)) userName = "user";
		String deviceName = buildDeviceName(); 
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("trace", trace);
		data.put("app", appName);
		data.put("email", URLEncoder.encode(userName, "UTF-8"));
		data.put("device", URLEncoder.encode(deviceName, "UTF-8"));
		
		String url = URL_POST_TRACE;
		Log.d(LOGTAG, "Post trace to " + url);
		NetworkUtils.postContent(url, null, data);		
	}
	
	@SuppressLint("DefaultLocale")
	private static String buildDeviceName() {
		String man = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.toLowerCase(Locale.US).contains(man.toLowerCase(Locale.US))) man = "";
		
		return (man + " " + model).trim();
	}
	
	private static String getShellOutput(String cmdline[]) {
		StringBuilder log = new StringBuilder();

		Process mLogcatProc = null;
		BufferedReader reader = null;
		String line;
		
		try {
			mLogcatProc = Runtime.getRuntime().exec(cmdline);
			reader = new BufferedReader(new InputStreamReader(
					mLogcatProc.getInputStream()));
	
			while ((line = reader.readLine()) != null) {
				log.append(line);
				log.append("\n");
			}
			reader.close();
		} catch (IOException e) {
			log.append(e.toString());
		} finally {
			if (reader!=null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		log.append("\n");
		return log.toString();
	}
	
	/*
	private static String logcat(String packageName) {
		final StringBuilder log = new StringBuilder();
		log.append("AndroidRuntime Output\n");
		log.append(getShellOutput(
				new String[] { "logcat", "-d", "AndroidRuntime:E *:S" }));
		
		log.append("Fatal Output\n");
		log.append(logcatFatal());

		log.append("Application Output\n");
		int PID = android.os.Process.getUidForName(packageName);
		log.append(getShellOutput(
				new String[] { "logcat", "-d", "|", "grep " + PID}));
		
		return log.toString();
	}
	*/
	
	private static String logcatFatal() {
		String logcat = getShellOutput(
				new String[] { "logcat", "-d"});
		try {
			Utils.saveString(new File("/sdcard/logcat.txt"), logcat);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

}
