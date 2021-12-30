package xtvapps.core;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unused")
public class PerfUtils {
	private static final String LOGTAG = PerfUtils.class.getSimpleName();

	static final Map<String, Long> start = new HashMap<>();
	
	private PerfUtils() {}

	public static void start(String log) {
		start(log, null);
	}
	
	public static void start(String log, String extra) {
		long t = System.currentTimeMillis();
		start.put(log, t);

		String msg = extra == null ?
			String.format("%s [START]", log) :
			String.format("%s (%s) [START]", log, extra);
		
		Log.d(LOGTAG, msg);
	}
	
	public static void end(String log) {
		end(log, null);
	}
	
	public static void end(String log, String extra) {
		long t = System.currentTimeMillis();
		Long t0 = start.get(log);
		if (t0 != null) {
			long time = t - t0;
			String msg = extra == null ?
					String.format(Locale.US, "%s [TIME %dms]", log, time) :
					String.format(Locale.US, "%s (%s) [TIME %dms]", log, extra, time);

			Log.d(LOGTAG, msg);
		} else {
			String msg = extra == null ?
					String.format("%s [END]", log) :
					String.format("%s (%s) [END]", log, extra);
			Log.d(LOGTAG, msg);
		}
	}
	
	public static void restart(String log, String extra) {
		end(log, extra);
		
		long t = System.currentTimeMillis();
		start.put(log, t);
	}
	
}
