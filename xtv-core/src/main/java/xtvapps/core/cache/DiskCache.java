package xtvapps.core.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import xtvapps.core.CoreUtils;
import xtvapps.core.FileUtils;
import xtvapps.core.LocalContext;
import xtvapps.core.Log;
import xtvapps.core.SimpleBackgroundTask;

@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class DiskCache<T> implements Cache<T> {
	private static final String LOGTAG = DiskCache.class.getSimpleName();
	private final MemCache<T> memCache;
	private File cacheDir;
	private final Object mDiskCacheLock = new Object();
	private boolean diskCacheInitialized = false;
	private int size = 0;
	private final Map<String, Long> accessTime = new HashMap<>();
	private long lastSizeCheck = 0;
	private static final long CHECK_PERIOD = 10 * 10000; // 10[s]
	private static final long SLEEP_TIME_QUEUE = 800; //[ms]
	private static final long SLEEP_TIME_INIT = 800; //[ms]
	private static final long MAX_WAIT_TIME = 10000; // [ms]

	public DiskCache(LocalContext context, String location, int size, float memSize, CacheSizeDescriptor<T> cacheSizeDescriptor) {
		memCache = new MemCache<>(memSize, cacheSizeDescriptor);
		init(context, location, size);
	}

	public DiskCache(LocalContext context, String location, int size, int memSize) {
		memCache = new MemCache<>(memSize);
		init(context, location, size);
	}

	private void init(LocalContext context, String location, int size) {
		this.size = size;

		cacheDir = new File(context.getLocalStorage(), location);
   
		InitDiskCacheTask initCacheTask = new InitDiskCacheTask(cacheDir);
		initCacheTask.execute();
	}
	
	private File key2file(String key) {
		String md5 = CoreUtils.md5(key);
		return new File(cacheDir.getPath() + File.separator + md5 + ".cache");
	}
	
	protected abstract T fromBytes(byte[] b);
	protected abstract byte[] toBytes(T item);
	
	private final Set<String> queue = new HashSet<>();
	
	public T getQueued(String key) {
		T entry = get(key);
		if (entry!=null) return entry;
		synchronized(queue) {
			if (!queue.contains(key)) { // first in queue does the work
				queue.add(key);
				return null;
			}
		}
		while (queue.contains(key)) {
			CoreUtils.sleep(SLEEP_TIME_QUEUE);
			Log.d(LOGTAG, "Waiting for " + key);
			if (System.currentTimeMillis() > MAX_WAIT_TIME) return null;
		}
		Log.d(LOGTAG, "Retrying " + key);
		return getQueued(key);
	}
	
	public void putQueued(String key, T entry) {
		if (entry!=null) put(key, entry);
		queue.remove(key);
	}
	
	@Override
	public T get(String key) {
		T entry = memCache.get(key);
		
		if (entry!=null) {
			registerAccess(key);
			return entry;
		}
		
		File f = key2file(key);
		
		waitForInitialization("GET");
		synchronized(mDiskCacheLock) {
			if (!f.exists()) {
				Log.d(LOGTAG, "File not found in disk cache " + f.getAbsolutePath() + " for key " + key);
				return null;
			}
			
			try {
				Log.d(LOGTAG, "Loading from disk " + f.getAbsolutePath() + " as " + key);
				byte[] raw = FileUtils.loadBytes(f);
				entry = fromBytes(raw);
				memCache.put(key, entry);
				registerAccess(key);
				return entry;
			} catch (Exception e) {
				Log.e(LOGTAG, "Error retrieving from disk " + f.getAbsolutePath() + " as " + key, e);
				return null;
			}
		}
	}

	@Override
	public void put(String key, T entry) {
		registerAccess(key);
		memCache.put(key, entry);
		
		File f = key2file(key);
		
		waitForInitialization("PUT");
		synchronized(mDiskCacheLock) {
			try {
				Log.d(LOGTAG, "Save " + key + " to " + f.getAbsolutePath());
				FileUtils.saveBytes(f, toBytes(entry));
				
				long t = System.currentTimeMillis();
				if (t - lastSizeCheck > CHECK_PERIOD) {
					lastSizeCheck = t;
					resizeCache(size);
				}
			} catch (Exception e) {
				Log.e(LOGTAG, "Error saving " + key + " to " + f.getAbsolutePath(), e);
			}
		}
	}
	
	public void remove(String key) {
		memCache.remove(key);
		File f = key2file(key);
		f.delete();
	}
	
	private void registerAccess(String key) {
		Log.d(LOGTAG, "Register access for " + key);
		synchronized (accessTime) {
			accessTime.put(key2file(key).getName(), System.currentTimeMillis());
		}
	}
	
	private void resizeCache(int maxSize) {
		ResizeCacheTask task = new ResizeCacheTask(cacheDir, maxSize);
		task.execute();
	}
	
	private File getAccessTimeFile() {
		return new File(cacheDir.getPath() + File.separator + "access.log");
	}
	
	protected void writeAccessTime()  {
		File f = getAccessTimeFile();
		synchronized (accessTime) {
			Log.d(LOGTAG, "Write access time to " + f.getAbsolutePath() + " = " + accessTime);
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
				for (Entry<String, Long> entry : accessTime.entrySet()) {
					bw.write(entry.getKey() + "=" + entry.getValue() + "\n");
				}
			} catch (Exception e) {
				Log.e(LOGTAG, "Error writing access time to " + f.getAbsolutePath(), e);
			}
		}
	}
	
	protected void waitForInitialization(String parentTask) {
        while (!diskCacheInitialized) {
    		Log.d(LOGTAG, "Waiting for initialization from " + parentTask);
    		CoreUtils.sleep(SLEEP_TIME_INIT);
    		Log.d(LOGTAG, "Initialized");
        }
	}
	
	protected void readAccessTime() throws IOException {
		File f = getAccessTimeFile();
		if (!f.exists()) return;

		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			String line;
			synchronized (accessTime) {
				while ((line = reader.readLine()) != null) {
					String[] parts = line.split("=");
					accessTime.put(parts[0], Long.parseLong(parts[1]));
				}
			}
			Log.d(LOGTAG, "Read access time from " + f.getAbsolutePath() + " = " + accessTime);
		}
	}

	@Override
	public void flush() {
		Log.d(LOGTAG, "Flush");
		memCache.flush();
		resizeCache(0);
	}
	
	class InitDiskCacheTask extends SimpleBackgroundTask {
		private final File cacheDir;

		public InitDiskCacheTask(File cacheDir) {
			this.cacheDir = cacheDir;
		}

		@Override
		public void onBackgroundTask() {
			synchronized (mDiskCacheLock) {
				try {
					cacheDir.mkdirs();
					readAccessTime();
				} catch (IOException e) {
					Log.e(LOGTAG, "Error initializing cache on " + cacheDir.getAbsolutePath(), e);
				}
				diskCacheInitialized = true;
				Log.d(LOGTAG, "Disk Cache has been initialized");
				mDiskCacheLock.notifyAll();
			}
			resizeCache(size);
			Log.d(LOGTAG, "End of InitDiskCacheTask");
		}
	}
	
	class ResizeCacheTask extends SimpleBackgroundTask {
		private final File cacheDir;
		private final int targetSize;

		public ResizeCacheTask(File cacheDir, int targetSize) {
			this.cacheDir = cacheDir;
			this.targetSize = targetSize;
		}
		
		@Override
		public void onBackgroundTask() {
			File[] files = cacheDir.listFiles();
			
			Log.d(LOGTAG, "shrink cache");
			HashSet<File> filesToRemove = new HashSet<>();
			synchronized (mDiskCacheLock) {
				// remove all unknown cache files
				synchronized(accessTime) {
					if (files!=null) {
						for(File file : files) {
							if (!file.getName().endsWith(".cache")) continue;
							
							if (accessTime.get(file.getName()) == null) filesToRemove.add(file);
						}
						
						// remove oldest files
						int howManyFilesToRemove = accessTime.size() - targetSize;
						for(int i=0; i< howManyFilesToRemove; i++) {
							File oldestFile = getOldestAccessedFile(cacheDir);
							filesToRemove.add(oldestFile);
							accessTime.remove(oldestFile.getName());
						}
					}
					
					// remove from disk
					for(File file : filesToRemove) {
						try {
							Log.d(LOGTAG, "Removing file from cache " + file.getAbsolutePath());
							file.delete();
						} catch (Exception e) {
							Log.e(LOGTAG, "Error removing file from cache " + file.getAbsolutePath(), e);
						}
					}
				}
				writeAccessTime();
			}
			Log.d(LOGTAG, "End of ResizeCacheTask");
		}
		
		private File getOldestAccessedFile(File dir) {
			long oldestTime = Long.MAX_VALUE;
			String oldestFileName = null;
			synchronized (accessTime) {
				for(Entry<String, Long> entry : accessTime.entrySet()) {
					if (entry.getValue() < oldestTime) {
						oldestTime = entry.getValue();
						oldestFileName = entry.getKey();
					}
				}
			}
			return new File(dir.getPath() + File.separator + oldestFileName);
		}
	}

}
