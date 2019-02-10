package xtvapps.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.json.JSONArray;

import android.util.Log;

public final class Utils {
	private static final String LOGTAG = Utils.class.getSimpleName();
	private static final int BUF_SIZE = 65536;
	private static final int HEX_BASE = 16;
	private static final int BYTE_MASK = 0xFF;

	private static final long SIZE_GIGABYTE = 1024*1024*1024;
	private static final long SIZE_MEGABYTE = 1024*1024;
	private static final long SIZE_KILOBYTE = 1024;
	
	private Utils() {}

	
	public static String size2human(long size) {
		if (size > SIZE_GIGABYTE) {
			DecimalFormat df = new DecimalFormat("#.00");
			return df.format(((size + SIZE_GIGABYTE/2) / SIZE_GIGABYTE)) + " GB";
		}
		if (size > SIZE_MEGABYTE) {
			return ((size + SIZE_MEGABYTE/2) / SIZE_MEGABYTE) + " MB";
		}
		return ((size + SIZE_KILOBYTE/2) / SIZE_KILOBYTE) + " KB";
	}
	
	public static String size2humanDetailed(long size) {
		if (size > SIZE_GIGABYTE) {
			DecimalFormat df = new DecimalFormat("#.00");
			return df.format((float)size / SIZE_GIGABYTE) + " GB";
		}
		if (size > SIZE_MEGABYTE) {
			DecimalFormat df = new DecimalFormat("#.00");
			return df.format((float)size / SIZE_MEGABYTE) + " MB";
		}
		return (size / SIZE_KILOBYTE) + " KB";
	}


	/*
*/
	
	public static String loadString(File f) throws IOException  {
		return loadString(f, "UTF-8");
	}
	
	public static List<String> loadLines(File f) throws IOException  {
		return loadLines(f, "UTF-8");
	}
	
	public static String loadString(File f, String encoding) throws IOException {
		return loadString(new FileInputStream(f), encoding);
	}
	
	public static List<String> loadLines(File f, String encoding) throws IOException {
		return loadLines(new FileInputStream(f), encoding);
	}
	
	public static String loadString(InputStream is, String encoding) throws IOException {
		List<String> lines = loadLines(is, encoding);
		StringBuffer s = new StringBuffer();
		for(String line : lines) {
			s.append(line).append("\n");
		}
		return s.toString();
	}
	
	public static List<String> loadLines(InputStream is, String encoding) throws IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is, encoding));
			
			String line = null;
			while ((line = reader.readLine())!=null) {
				lines.add(line);
			}
			return lines;
		} finally {
			if (reader!=null) reader.close();
		}
	}
	
	public static byte[] loadBytes(File f) throws IOException {
		FileInputStream is = null;
		try {
			is = new FileInputStream(f);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buf = new byte[BUF_SIZE];
			while (true) {
				int rc = is.read(buf);
				if (rc <= 0)
					break;
				else
					bout.write(buf, 0, rc);
			}
			return bout.toByteArray();
		} finally {
			if (is!=null) is.close();
		}
	}
	
	public static void saveString(File f, String s) throws IOException {
		saveBytes(f, s.getBytes());
	}
	
	public static void saveBytes(File f, byte[] content) throws IOException {
		FileOutputStream os = null;
		ByteArrayInputStream bais = null;
		try {
			os = new FileOutputStream(f);
			bais = new ByteArrayInputStream(content);
			byte[] buf = new byte[BUF_SIZE];
			while (true) {
				int rc = bais.read(buf);
				if (rc <= 0)
					break;
				else
					os.write(buf, 0, rc);
			}
		} finally {
			if (os!=null) os.close();
			if (bais!=null) bais.close();
		}
	}

	public static int str2i(String value) {
		return str2i(value, 0);
	}

	public static int str2i(String value, int defaultValue) {
		try {
			if (value == null) return defaultValue;
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static long str2l(String value) {
		return str2l(value, 0);
	}
	
	public static long str2l(String value, long defaultValue) {
		try {
			if (value == null) return defaultValue;
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	
	public static float str2f(String value) {
		return str2f(value, 0);
	}

	public static float str2f(String value, float defaultValue) {
		try {
			if (value == null) return defaultValue;
			return Float.parseFloat(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static int strHex2i(String value, int defaultValue) {
		try {
			if (value == null) return defaultValue;
			return (int) Long.parseLong(value.trim(), HEX_BASE);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static String md5(String s) {
		try {
			return md5(s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String sha1(String s) {
		try {
			return sha1(s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String md5(byte[] data) {
		return digest("MD5", data);
	}
	
	public static String sha1(byte[] data) {
		return digest("SHA-1", data);
	}
	
	private static String digest(String method, byte[] data) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance(method);
			digest.update(data);
			return hexString(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String hexString(byte[] data) {
		// Create Hex String
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			String s = Integer.toHexString(BYTE_MASK & data[i]);
			if (s.length()<2) s = "0" + s;
			hexString.append(s);
		}
		return hexString.toString();
	}

	public static boolean isEmptyString(String s) {
		return s == null || s.trim().length() == 0;
	}
	

}
