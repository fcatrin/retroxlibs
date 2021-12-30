package xtvapps.core;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class CoreUtils {
	private static final int BUF_SIZE = 65536;
	private static final int HEX_BASE = 16;
	private static final int BYTE_MASK = 0xFF;

	private static final long SIZE_GIGABYTE = 1024*1024*1024;
	private static final long SIZE_MEGABYTE = 1024*1024;
	private static final long SIZE_KILOBYTE = 1024;

	private static final int SECONDS_PER_HOUR = 3600;
	private static final int MINUTES_PER_HOUR = 60;
	private static final int SECONDS_PER_MINUTE = 60;

	private CoreUtils() {}
	
	public static String size2human(long size) {
		if (size > SIZE_GIGABYTE) {
			DecimalFormat df = new DecimalFormat("#.#");
			return df.format((float)size/ SIZE_GIGABYTE) + "G";
		}
		if (size > SIZE_MEGABYTE) {
			DecimalFormat df = new DecimalFormat("#.#");
			return df.format((float)size / SIZE_MEGABYTE) + "M";
		}
		return ((size + SIZE_KILOBYTE/2) / SIZE_KILOBYTE) + "K";
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
		if (size > SIZE_KILOBYTE) {
			DecimalFormat df = new DecimalFormat("#.00");
			return df.format((float)size / SIZE_KILOBYTE) + " KB";
		}
		return size + " B";
	}

	public static String loadString(InputStream is, String encoding) throws IOException {
		List<String> lines = loadLines(is, encoding);
		StringBuilder s = new StringBuilder();
		for(String line : lines) {
			s.append(line).append("\n");
		}
		return s.toString();
	}
	
	public static List<String> loadLines(InputStream is, String encoding) throws IOException {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding))) {

			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			return lines;
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
		return md5(s.getBytes(StandardCharsets.UTF_8));
	}

	public static String sha1(String s) {
		return sha1(s.getBytes(StandardCharsets.UTF_8));
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
			return bytes2hex(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String bytes2hex(byte[] data) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : data) {
			String s = Integer.toHexString(BYTE_MASK & b);
			if (s.length() < 2) s = "0" + s;
			hexString.append(s);
		}
		return hexString.toString();
	}

	public static byte[] hex2bytes(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for(int i=0; i+2<=hex.length(); i+=2) {
			bytes[i / 2] = (byte)hex2i(hex.substring(i));
		}
		return bytes;
	}

	public static int hex2i(String hex) {
		return Integer.parseInt(hex.substring(0, 2), HEX_BASE);
	}

	public static String findValue(Map<String, String> map, String value) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue().equals(value))
				return entry.getKey();
		}
		return null;
	}

	public static boolean isEmptyString(String s) {
		return s == null || s.trim().isEmpty();
	}
	
	public static void copy(InputStream is, OutputStream os) throws IOException {
		copy(is, os, null, 0);
	}
	
	public static void copy(InputStream is, OutputStream os, ProgressListener progressListener, long max) throws IOException {
        byte[] buffer = new byte[BUF_SIZE];
		int bufferLength;

		if (progressListener!=null) progressListener.update(0, (int)max);
		try {
			int pos = 0;
			while ((bufferLength = is.read(buffer)) > 0) {
				os.write(buffer, 0, bufferLength);
				pos += bufferLength;
				if (progressListener!=null) progressListener.update(pos, (int)max);
			}
		} finally {
			is.close();
			os.close();
		}
	}
	
	public static void delTree(File dir) {
		if (!dir.exists() || !dir.isDirectory()) return;

		final File[] files = dir.listFiles();
		if (files != null) {
			for(File f : files) {
				if (f.isDirectory()) delTree(f);
				else f.delete();
			}
		}
		dir.delete();
	}
	public static boolean unzip(File file, File dstPath) throws IOException {
		return unzip(file, dstPath, null);
	}
	
	public static boolean unzip(File file, File dstPath, ProgressListener listener) throws IOException {
		
		ZipFile zipFile = null;
		long max = 0;
		long pos = 0;
		
		boolean hasSizeInfo = true;
		if (listener!=null) {
			try {
				zipFile = new ZipFile(file);
				Enumeration<? extends ZipEntry> e = zipFile.entries();
		        while (e.hasMoreElements()) {
		        	ZipEntry entry = e.nextElement();
		        	long size = entry.getSize();
		        	max += size>0?size:0;
		        }
	        
		        if (max == 0) {
		        	hasSizeInfo = false;
		        	max = zipFile.size();
		        }
			} finally {
				if (zipFile!=null)	zipFile.close();
				zipFile = null;
			}
	        
			listener.update(0, (int)max);
		}
		
		dstPath.mkdirs();
		
		long maxFileSize = dstPath.getFreeSpace();
		if (max > maxFileSize) throw new NotEnoughStorageException(maxFileSize, max);
		
		try {
	        zipFile = new ZipFile(file);
			int customBufferSize = 0;
			if (listener!=null) {
				customBufferSize = listener.getBufferSize((int)max);
			}
	        byte[] buffer = new byte[customBufferSize>0?customBufferSize:BUF_SIZE];
	        boolean cancel = false;
			Enumeration<? extends ZipEntry> e = zipFile.entries();
	          while (e.hasMoreElements()) {
	              ZipEntry entry = e.nextElement();
	              if (entry.getName().startsWith("._")) continue;
	              
	              File destinationPath = new File(dstPath, entry.getName());
	              destinationPath.getParentFile().mkdirs();
	              if (entry.isDirectory()) continue;
	              
	              BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
	              int b;
	              FileOutputStream fos = new FileOutputStream(destinationPath);
	              BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);
	              bos.flush();
	              while ((b = bis.read(buffer, 0, buffer.length)) != -1) {
	                  bos.write(buffer, 0, b);
	                  
	                  if (hasSizeInfo && listener!=null) {
	                      pos+=b;
	                	  cancel = listener.update((int)pos, (int)max);
	                	  if (cancel) break;
	                  }
	              }
	              bos.close();
	              bis.close();
	              
	              if (listener!=null && !hasSizeInfo) {
	            	  pos++;
	            	  cancel = listener.update((int)pos, (int)max);
	              }
	              if (cancel) return false;
	          }
	          return true;
		} finally {
			if (zipFile!=null) zipFile.close();
		}
	}

	public static String[] buildStringArray(String s) {
		try {
			JSONArray a = new JSONArray(s);
			String[] values = new String[a.length()];
			for (int i = 0; i < a.length(); i++) {
				values[i] = a.getString(i);
			}
			return values;
		} catch (Exception e) {
			return null;
		}
	}

	public static String buildArrayString(String[] values) {
		JSONArray a = new JSONArray();
		for (String value : values) {
			a.put(value);
		}
		return a.toString();
	}

	public static int findArrayValue(String[] a, String value) {
		for (int i = 0; i < a.length; i++)
			if (a[i].equals(value))
				return i;

		return -1;
	}

	public static String padz(String s, int n) {
		String ns = "00000000" + s;
		return ns.substring(ns.length()-n);
	}

	public static String formatTime(int time) {
		int hours = time / SECONDS_PER_HOUR;
		int minutes = (time - hours * SECONDS_PER_HOUR) / MINUTES_PER_HOUR;
		int seconds = time % SECONDS_PER_MINUTE;

		if (hours == 0) return padz(minutes + "", 2) + ":" + padz(seconds + "", 2);
		return hours + "h" + padz(minutes + "", 2);
	}

	public static String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	public static String getSecurityInfo() {
		StringBuilder sb = new StringBuilder();
		Provider[] providers = Security.getProviders();
		for (Provider provider : providers) {
			sb.append("provider: ").append(provider.getName()).append("\n");
			Set<Provider.Service> services = provider.getServices();
			for (Provider.Service service : services) {
				sb.append("  algorithm: ").append(service.getAlgorithm()).append("\n");
			}
		}
		return sb.toString();
	}

	public static String addNoCache(String url) {
		return url.replace("{nocache}", String.valueOf(System.currentTimeMillis()));
	}

	public static Properties loadProperties(String s) {
		Properties p = new Properties();
		try {
			p.load(new ByteArrayInputStream(s.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}

	public static Properties loadProperties(File file) {
		Properties p = new Properties();
		try {
			if (file.exists()) p.load(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}

	public static void saveProperties(File file, Properties p) throws IOException {
		FileUtils.saveString(file, propertiesAsString(p));
	}

	public static String propertiesAsString(Properties p) {
		StringBuilder str = new StringBuilder();
		for(Map.Entry<Object, Object> entry : p.entrySet()) {
			str.append(entry.getKey()).append("=");
			str.append(entry.getValue()).append("\n");
		}
		return str.toString();
	}

	public static String buildList(String[] parts, String separator, String quotes) {
		StringBuilder result = new StringBuilder();
		for(String part : parts) {
			if (result.length() != 0) {
				result.append(separator);
			}
			if (quotes!=null) result.append(quotes);
			result.append(part);
			if (quotes!=null) result.append(quotes);
		}
		return result.toString();
	}

	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void close(InputStream is) {
		try {
			if (is!=null) is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
