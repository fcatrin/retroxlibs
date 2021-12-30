package xtvapps.core.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import xtvapps.core.AsyncExecutor;
import xtvapps.core.AppContext;
import xtvapps.core.Callback;
import xtvapps.core.CoreUtils;
import xtvapps.core.DialogUtils;
import xtvapps.core.FileUtils;
import xtvapps.core.ListOption;
import xtvapps.core.LocalContext;
import xtvapps.core.ProgressListener;
import xtvapps.core.content.KeyValue;
import xtvapps.core.content.KeyValueMap;

public final class AndroidCoreUtils {
	private static final String LOGTAG = AndroidCoreUtils.class.getSimpleName();

	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void init(Handler handler) {
		AppContext.asyncExecutor = new AsyncExecutor(new AndroidUIThreadExecutor(handler));
		AppContext.logger = new AndroidLogger();
		AppContext.dialogFactory = new AndroidStandardDialogs();
	}

	private AndroidCoreUtils(){}
	
	public static void openGooglePlay(Context context, String packageName) {
		try {
			Log.d(LOGTAG, "opening market for " + packageName);
		    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
		} catch (ActivityNotFoundException e) {
		    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
		}
	}

	public static void toast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	public static void createNoMediaFile(File folder) {
		File noMediaFile = new File(folder, ".nomedia");
		if (!noMediaFile.exists()) {
			try {
				//noinspection ResultOfMethodCallIgnored
				noMediaFile.createNewFile();
			} catch (IOException e) {
				Log.d(LOGTAG, "Cannot create .nomedia file on " + folder);
			}
		}
	}

	public static void showSoftKeyboard(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		if (imm != null) {
			imm.showSoftInput(view, 0);
		}
	}

	public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null && view!=null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
	}

	public static void unpackAssets(Context ctx, String dir, File dstDir) throws IOException {
		unpackAssets(ctx, dir, dstDir, null);
	}
	
	public static void unpackAssets(Context ctx, String dir, File dstDir, ProgressListener progressListener) throws IOException {
		File unpackDir = new File (dstDir, dir);
		if (unpackDir.exists()) CoreUtils.delTree(unpackDir);
		//noinspection ResultOfMethodCallIgnored
		unpackDir.mkdirs();

		AssetManager assets = ctx.getAssets();
		String[] files = assets.list(dir);
		
		int filesMax = files.length;
		int filesProgress = 0;
		
		for(String file : files) {
			String fileName = dir + "/" + file;
			if (progressListener!=null) progressListener.update(filesProgress++, filesMax);
			try {
				InputStream is = assets.open(fileName);
				File dstFile = new File(dstDir, fileName);
				CoreUtils.copy(is, new FileOutputStream(dstFile));
			} catch (FileNotFoundException e) {
				// this is a folder
				unpackAssets(ctx, fileName, dstDir, progressListener);
			}
		}
	}

	public static void installApk(Context context, String fileProviderDomain, File apk) {
		Log.d(LOGTAG, "Install APK from " + apk + " provider: " + fileProviderDomain);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			Uri apkUri = FileProvider.getUriForFile(context, fileProviderDomain, apk);
			Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
			intent.setData(apkUri);
			intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			context.startActivity(intent);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
			context.startActivity(intent);
		}
	}

	public static void openWeb(Activity context, String url) {
		openWeb(context, url, url);
	}

	public static void openWeb(Activity context, String url, String name) {
		try {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		} catch (Exception e) {
			String msg = context.getString(R.string.no_browser)
					.replace("{url}", name);
			DialogUtils.message(new AndroidLocalContext(context), msg);
		}
	}

	public static void tryStartActivity(Activity context, Intent intent) {
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			DialogUtils.message(new AndroidLocalContext(context), "Cannot start activity"); // TODO Translate
		}
	}

	public static void bindClick(Activity a, int id, View.OnClickListener listener) {
		View v = a.findViewById(id);
		if (v!=null) v.setOnClickListener(listener);
	}

	public static void bindClickListeners(Activity a, int[] ids, View.OnClickListener listener) {
		for(int id : ids) {
			bindClick(a, id, listener);
		}
	}

	public static void configureAsFullscreen(Activity activity) {
		configureAsFullscreen(activity, true);
	}

	public static void configureAsFullscreen(Activity activity, boolean hideNavigation) {
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
		activity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		hideSystemBar(activity, hideNavigation);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void hideSystemBar(Activity activity, boolean hideNavigation) {
		int requiredFlags = View.SYSTEM_UI_FLAG_FULLSCREEN |
				View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

		if (hideNavigation) {
			requiredFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		}

		// spacial cases for Moto X and Moto G
		boolean isMotoXorG = isMotoXorG();
		if (isMotoXorG) {
			requiredFlags = View.SYSTEM_UI_FLAG_LOW_PROFILE;
		}

		final int flags = requiredFlags;

		final View rootView = activity.getWindow().getDecorView().getRootView();
		rootView.setSystemUiVisibility(flags);
		rootView.postDelayed(() -> rootView.setSystemUiVisibility(flags), 500);

	}

	private static boolean isMotoXorG() {
		return Build.MODEL.contains("XT1032") || Build.MODEL.contains("XT1058");
	}

	public static File getCacheExternalStorage(Context context) {
		return
				(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) && context.getExternalCacheDir()!=null ?
						context.getExternalCacheDir():
						context.getCacheDir();
	}

	public static String getCacheExternalStoragePath(Context context) {
		return getCacheExternalStorage(context).getPath();
	}

	public static File getLocalDataDir(Context context) {
		return context.getFilesDir();
	}

	public static void allowScreenLock(Window window, boolean allow) {
		if (allow) window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		else window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public static String getUserEmail(Context context) {
		AccountManager accountManager = AccountManager.get(context);
		Account account = getAccount(context,  accountManager);
		if (account == null) return null;
		return account.name;
	}

	public static List<String> getGoogleUserEmails(Context context) {
		AccountManager accountManager = AccountManager.get(context);
		Account[] accounts = getGoogleAccounts(accountManager);
		if (accounts == null || accounts.length == 0) return null;

		List<String> emails = new ArrayList<>();
		for(Account account : accounts) {
			emails.add(account.name);
		}
		return emails;
	}

	public static Account[] getGoogleAccounts(AccountManager accountManager) {
		return accountManager.getAccountsByType("com.google");
	}

	private static Account getAccount(Context context, AccountManager accountManager) {
		Account[] accounts = getGoogleAccounts(accountManager);
		if (accounts.length > 0) return accounts[0];

		Pattern emailPattern = Patterns.EMAIL_ADDRESS;
		accounts = AccountManager.get(context).getAccounts();
		for (Account account : accounts) {
			if (emailPattern.matcher(account.name).matches()) return account;
		}
		return null;
	}

	public static void popupMenu(Context ctx, View v, final List<KeyValue> options, final Callback<KeyValue> callback) {
		android.widget.PopupMenu popup = new android.widget.PopupMenu(ctx, v);
		Menu menu = popup.getMenu();
		for(int i=0; i<options.size(); i++) {
			menu.add(Menu.NONE, i, Menu.NONE, options.get(i).getValue());
		}

		popup.setOnMenuItemClickListener(item -> {
			int index = item.getItemId();
			callback.onResult(options.get(index));
			return true;
		});
		popup.show();
	}

	public static String timeAgo(String sDate) {
		Date date = new Date();
		try {
			Date parsedDate = dateTimeFormatter.parse(sDate);
			if (parsedDate!=null) date = parsedDate;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return DateUtils.getRelativeTimeSpanString(date.getTime()).toString();
	}

	public static void saveBitmap(File f, Bitmap bitmap, int quality) throws IOException {
		Bitmap.CompressFormat format = f.getName().endsWith(".jpg")? Bitmap.CompressFormat.JPEG: Bitmap.CompressFormat.PNG;
		saveBitmap(f, bitmap, format, quality);
	}

	public static void saveBitmap(File f, Bitmap bitmap, Bitmap.CompressFormat format, int quality) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(format, quality, stream);
		FileUtils.saveBytes(f, stream.toByteArray());
	}

	// use optimized memory size from https://developer.android.com/topic/performance/graphics/load-bitmap.html
	public static Bitmap loadBitmap(File f, int reqWidth, int reqHeight) throws IOException {
		byte[] raw = FileUtils.loadBytes(f);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(raw , 0, raw .length, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		options.inDither = false;
		return BitmapFactory.decodeByteArray(raw , 0, raw .length, options);
	}

	private static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) >= reqHeight
					&& (halfWidth / inSampleSize) >= reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	@SuppressLint("PackageManagerGetSignatures")
	public static String[] getSignatures(Context context) throws PackageManager.NameNotFoundException, NoSuchAlgorithmException {
		PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
				context.getPackageName(), PackageManager.GET_SIGNATURES);

		String[] signatures = new String[packageInfo.signatures.length];

		for(int i=0; i<packageInfo.signatures.length; i++) {
			Signature signature = packageInfo.signatures[i];
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(signature.toByteArray());

			signatures[i] = CoreUtils.md5(md.digest());
		}
		return signatures;
	}

	public static boolean checkSignature(Context context, String targetSignature) {
		try {
			String[] signatures = getSignatures(context);
			for (String signature : signatures) {
				Log.d("Signature", "Include this string as a value for SIGNATURE:" + signature);

				if (targetSignature.equals(signature)) return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void relayoutChildren(View view) {
		if (view == null) return;
		view.measure(
				View.MeasureSpec.makeMeasureSpec(view.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
				View.MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(), View.MeasureSpec.EXACTLY));
		view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
	}

	public static boolean isAppRunning(Context ctx, String apkId) {

		// standard way but it seems to need root access
		ActivityManager activityManager = (ActivityManager) ctx.getSystemService( Context.ACTIVITY_SERVICE );
		List<ActivityManager.RunningAppProcessInfo> procInfoList = activityManager.getRunningAppProcesses();
		Log.d("RUNNING", "running processes " + procInfoList.size());
		for(ActivityManager.RunningAppProcessInfo procInfo : procInfoList) {
			Log.d("RUNNING", "Running process " + procInfo.processName + " " + Arrays.toString(procInfo.pkgList));
			if (procInfo.processName.equals(apkId)) return true;
		}

		// alternate way using the good old "ps" command
		List<String> procNames = getProcNames();
		Log.d("RUNNING", "proc names: " + procNames);
		return procNames.contains(apkId);
	}

	public static List<String> runCommand(String command) {
		List<String> lines = new ArrayList<>();
		BufferedReader in = null;
		try {
			Process p = Runtime.getRuntime().exec(command);
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in!=null)
				// flipping Java 7
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		return lines;
	}

	private static List<String> getProcNames() {
		List<String> procNames = new ArrayList<>();
		BufferedReader in = null;
		try {
			Process p = Runtime.getRuntime().exec("ps");
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;
			while ((line = in.readLine()) != null) {
				String[] parts = line.trim().split(" ");
				if (parts.length>0) {
					procNames.add(parts[parts.length-1]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in!=null)
				// flipping Java 7
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		return procNames;
	}

	public static void exec(String[] command) {
		try {
			Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getThisAPKInstalledVersionCode(Context ctx) {
		return getAPKInstalledVersionCode(ctx, ctx.getPackageName());
	}

	public static boolean isAPKInstalled(Context ctx, String apkId) {
		return getAPKInstalledVersionCode(ctx, apkId)>0;
	}

	public static int getAPKInstalledVersionCode(Context ctx, String apkId) {
		PackageManager packageManager = ctx.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(apkId, 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			return 0;
		}
	}

	public static String buildDeviceName() {
		String man = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.toLowerCase(Locale.US).contains(man.toLowerCase(Locale.US))) man = "";

		return (man + " " + model).trim();
	}

	public static String getDeviceIdsAsText(Context context) {
		JSONObject o = new JSONObject();
		Map<String, String> deviceIds = getDeviceIds(context);
		for(Map.Entry<String, String> entry : deviceIds.entrySet()) {
			try {
				o.put(entry.getKey(), entry.getValue());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return o.toString();
	}
	public static Map<String, String> getDeviceIds(Context context) {
		Map<String, String> deviceIds = new HashMap<>();

		String serial = getWifiMacAddress(context);
		if (isValidSerialNumber(serial)) deviceIds.put("WIFI", serial);

		serial = getMacAddress("wlan0");
		if (isValidSerialNumber(serial)) deviceIds.put("WLAN", serial);

		serial = getMacAddress("eth0");
		if (isValidSerialNumber(serial)) deviceIds.put("ETH", serial);

		serial = getSerialNumberFromCPUInfo();
		if (isValidSerialNumber(serial)) deviceIds.put("PROC", serial);

		deviceIds.put("DEVICE", buildDeviceName());
		deviceIds.put("SDK", String.valueOf(Build.VERSION.SDK_INT));
		return deviceIds;
	}

	@SuppressWarnings("SpellCheckingInspection")
	private static final String[] invalidSerialNumbers = {
			"unknown",
			"0123456789abcdef",
			"02:00:00:00:00:00",
			"02:15:B2:00:00:00"};

	private static boolean isValidSerialNumber(String serial) {
		if (CoreUtils.isEmptyString(serial)) return false;

		String lowerCaseSerial = serial.toLowerCase(Locale.US);
		for(String invalidSerialNumber : invalidSerialNumbers) {
			if (lowerCaseSerial.equals(invalidSerialNumber)) return false;
		}

		char c = serial.charAt(0);
		for(int i=1; i<serial.length(); i++) {
			if (serial.charAt(i) != c) return true;
		}

		return false;
	}

	@SuppressLint("HardwareIds")
	private static String getWifiMacAddress(Context context) {
		try {
			WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = wifiManager.getConnectionInfo();
			return wInfo.getMacAddress().toUpperCase(Locale.US);
		} catch (Exception e) {
			return null;
		}
	}

	private static String getMacAddress(String name) {
		try {
			List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface nif : all) {
				if (!nif.getName().equalsIgnoreCase(name)) continue;

				byte[] macBytes = nif.getHardwareAddress();
				if (macBytes == null) {
					return "";
				}

				StringBuilder res1 = new StringBuilder();
				for (byte b : macBytes) {
					String part = "0" + Integer.toHexString(b & 0xFF);
					part = part.substring(part.length() - 2);
					res1.append(part).append(":");
				}

				if (res1.length() > 0) {
					res1.deleteCharAt(res1.length() - 1);
				}
				return res1.toString().toUpperCase(Locale.US);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}

	private static String getSerialNumberFromCPUInfo() {
		@SuppressWarnings("SpellCheckingInspection")
		String cpuInfoFileName = "/proc/cpuinfo";
		List<String> cpuInfoLines;
		try {
			cpuInfoLines = FileUtils.loadLines(new File(cpuInfoFileName));
			for(String cpuInfoLine : cpuInfoLines) {
				String line = cpuInfoLine.toLowerCase(Locale.US);
				if (!line.startsWith("serial")) continue;

				String[] parts = line.split(":");
				if (parts.length != 2) continue;

				String serial = parts[1];
				return serial.trim();
			}
		} catch (IOException e) {
			Log.e(LOGTAG, e.getMessage(), e);
		}
		return null;
	}

	public static String buildMemStats() {
		Runtime runtime = Runtime.getRuntime();
		return "MEM: " + runtime.freeMemory() + " free of " + runtime.totalMemory() + " max:" + runtime.maxMemory();
	}


	public static File dumpStream(Context context, InputStream is) throws IOException {
		File outputDir = context.getCacheDir();
		File tmpFile = File.createTempFile("content", "tmp", outputDir);
		FileOutputStream os = new FileOutputStream(tmpFile);
		CoreUtils.copy(is, os);
		return tmpFile;
	}

	public static void setScrollSpeed(ViewPager viewPager, int time) {
		try {
			Field mScroller;
			mScroller = ViewPager.class.getDeclaredField("mScroller");
			mScroller.setAccessible(true);
			Interpolator sInterpolator = new AccelerateDecelerateInterpolator();
			FixedSpeedScroller scroller = new FixedSpeedScroller(viewPager.getContext(), sInterpolator, time);
			mScroller.set(viewPager, scroller);
		} catch (Exception e) {
			Log.e(LOGTAG, "Error setting scroll speed", e);
		}
	}

	public static void share(LocalContext localContext, final String subject, final String body, final String twit, final String link) {
		final Context context = ((AndroidLocalContext)localContext).getAndroidContext();

		final List<KeyValueMap>services = getShareServices(context);
		if (services.size() == 0) {
			DialogUtils.message(localContext, context.getString(R.string.no_share_title), context.getString(R.string.no_share_title));
			return;
		}

		List<ListOption> sortedServices = new ArrayList<>();
		for(int i=0; i<services.size(); i++) {
			KeyValueMap service = services.get(i);
			ListOption option = new ListOption(i, (String)service.get("name"));
			String pkg = (String) service.get("pkg");
			if (pkg.contains("facebook") || pkg.contains("twit") || pkg.contains("whatsapp"))
				sortedServices.add(0, option);
			else
				sortedServices.add(option);
		}

		// IconMenuAdapter adapter = new IconMenuAdapter(context, R.layout.menu_item_icon, sortedServices);
		DialogUtils.selectByIndex(localContext, context.getString(R.string.title_share_default), sortedServices, new Callback<Integer>() {
			@Override
			public void onResult(Integer index) {
				shareOnService(services.get(index), context, subject, body, twit, link);
			}
		});
	}

	private static void shareOnService(KeyValueMap map, final Context context, final String subject, final String body, final String twit, final String link) {
		String pkg = (String)map.get("pkg");
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setClassName(pkg, (String)map.get("name"));
		intent.setType("text/plain");

		boolean isShort = pkg.contains(".mms") || pkg.contains("tweet") || pkg.contains("twit");
		boolean isFacebook = pkg.contains("facebook");

		if (!isFacebook) intent.putExtra(Intent.EXTRA_SUBJECT, isShort?"":subject);
		intent.putExtra(Intent.EXTRA_TEXT, isShort?twit:(isFacebook?link:body));
		context.startActivity(intent);
	}

	private static List<KeyValueMap> getShareServices(Context context) {
		Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
		sendIntent.setType("text/plain");
		final List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(sendIntent, 0);

		List<KeyValueMap> services = new ArrayList<>();
		for(ResolveInfo activity : activities) {
			KeyValueMap item = new KeyValueMap();
			item.setValue(activity.activityInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
			item.put("icon", activity.activityInfo.applicationInfo.loadIcon(context.getPackageManager()));
			item.put("pkg", activity.activityInfo.packageName);
			item.put("name", activity.activityInfo.name);
			services.add(item);
		}
		return services;
	}

	public static boolean isCallable(Context ctx, Intent intent) {
		List<ResolveInfo> list = ctx.getPackageManager().queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public static void logKeyEvent(Activity activity, KeyEvent event, String prefix) {
		int keyCode     = event.getKeyCode();
		boolean isDown  = event.getAction() == KeyEvent.ACTION_DOWN;

		String focusedViewClassName = "null";
		String focusedViewText      = "null";

		if (activity != null) {
			View currentFocus = activity.getCurrentFocus();
			if (currentFocus!=null) {
				focusedViewClassName = currentFocus.getClass().getName();
				if (currentFocus instanceof TextView) {
					TextView txtCurrentFocus = (TextView)currentFocus;
					focusedViewText = txtCurrentFocus.getText().toString();
				}
			}
		}
		Log.d("logKeyEvent", prefix + " " + keyCode + " down:" + isDown + " to view " + focusedViewClassName + ":" + focusedViewText);
	}

	private static int getOpenGLESVersion(Activity activity) {
		ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo configurationInfo =  activityManager.getDeviceConfigurationInfo();
		return configurationInfo.reqGlEsVersion;
	}

	public static boolean isOpenGLESVersionSupported(Activity activity, int versionMajor, int versionMinor) {
		int versionToCheck = versionMajor << 16 | versionMinor;
		int glESVersion = getOpenGLESVersion(activity);
		Log.d(LOGTAG, String.format("Device GL ES version: %x, version to check: %x", glESVersion, versionToCheck));
		return glESVersion >= versionToCheck;
	}

	public static String getOpenGLESVersionName(Activity activity) {
		int glESVersion = getOpenGLESVersion(activity);
		int major = glESVersion >> 16;
		int minor = glESVersion & 0xFFFF;
		return major + "." + minor;
	}

	public static void setLayoutWeight(View view, float weight) {
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		layoutParams.weight = weight;
	}

}
