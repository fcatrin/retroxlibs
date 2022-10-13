package xtvapps.core.android;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xtvapps.core.BackgroundTask;
import xtvapps.core.FileUtils;
import xtvapps.core.SimpleBackgroundTask;
import xtvapps.core.net.NetworkUtils;

public class BitmapWorkerTaskPersistent extends SimpleBackgroundTask {
	private static final String LOGTAG = BitmapWorkerTaskPersistent.class.getSimpleName();
	private static final ExecutorService PARALLEL_EXECUTOR = Executors.newFixedThreadPool(8);
	Animation animation = null;
	
	private boolean fromNetwork = false;
	
	private final WeakReference<ImageView> imageViewReference;
	private final String location;

	private final BitmapTransformer transformer;
	private final File destination;
	private File destinationTransformed = null;
	private static final Map<String, String> loadingBitmapLock = new HashMap<>();
	private ContentResolver resolver = null;
	
	private String imageTag = "";
	private BitmapFallback bitmapFallback = null;
	private BitmapCallback bitmapCallback = null;
	
	private CompressFormat compressFormat = null;
	
	public interface BitmapFallback {
		Bitmap getBitmap();
	}
	
	public interface BitmapCallback {
		void onBackground(Bitmap bitmap);
		void onPostExecute(Bitmap bitmap);
	}
	
	public BitmapWorkerTaskPersistent(File destination, String location, ImageView imageView) {
		this(destination, location, imageView, null);
	}
	
	public BitmapWorkerTaskPersistent(File destination, String location, ImageView imageView, BitmapTransformer transformer) {
		imageViewReference = new WeakReference<>(imageView);
		this.transformer = transformer;
		this.destination = destination;
		this.location = location;
	}
	
	public void setBitmapFallback(BitmapFallback bitmapFallback) {
		this.bitmapFallback = bitmapFallback;
	}
	
	public void setCachedExtension(String extension) {
		String destinationName = destination.getAbsolutePath();
		int p = destinationName.lastIndexOf(".");
		int s = destinationName.lastIndexOf("/");
		String destinationFileName = s < p ? destinationName.substring(0, p) : destinationName;
		String destinationExtension = s < p ? destinationName.substring(p) : "";
		destinationTransformed = new File(destinationFileName + "." + extension + destinationExtension);
	}
	
	public void setContentResolver(ContentResolver resolver) {
		this.resolver = resolver;
	}
	
	public void setBitmapCallback(BitmapCallback callback) {
		this.bitmapCallback = callback;
	}
	
	public void setCompressFormat(CompressFormat compressFormat) {
		this.compressFormat = compressFormat;
	}

	private void setLoadingTag(String tag) {
		imageTag = tag;

		ImageView imageView = imageViewReference.get();
		imageView.setTag(R.string.image_loading_id, imageTag);
	}
	
	private void setLoadedTag(String tag) {
		ImageView imageView = imageViewReference.get();
		imageView.setTag(R.string.image_loaded_id, tag);
	}

	public static void resetImage(ImageView imageView) {
		imageView.setImageBitmap(null);
		imageView.setTag(R.string.image_loading_id, "");
		imageView.setTag(R.string.image_loaded_id, "");
	}

	private String getLoadingTag() {
		ImageView imageView = imageViewReference.get();
		return (String)imageView.getTag(R.string.image_loading_id);
	}

	private String getLoadedTag() {
		ImageView imageView = imageViewReference.get();
		return (String)imageView.getTag(R.string.image_loaded_id);
	}
	
	public Bitmap executeInThisThread() {
		if (location.equals(getLoadedTag())) return null;
		if (location.equals(getLoadingTag())) return null;
		
		setLoadedTag("");
		setLoadingTag(location);
		
		Bitmap bitmap;
		if (destinationTransformed!=null && destinationTransformed.exists()) {
			bitmap = decodeBitmap(destinationTransformed);
		} else {
			onBackgroundTask();
			bitmap = decodeAndTransform();
		}
		if (bitmapCallback!=null) bitmapCallback.onBackground(bitmap);
		return bitmap;
	}

	public void postExecuteInThisThread(Bitmap bitmap) {
		setBitmapOnView(bitmap);
	}

	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	@Override
	public void onBackgroundTask() {
		ImageView imageView = getImageView();
		if (imageView == null) return; // Abort if not valid
		
		byte[] raw;
		String keyLock = destination.getAbsolutePath();
		if (!destination.exists()) {
			Log.d(LOGTAG, "Loading from network " + location);
			boolean wasLocked = true;
			String destinationLock;
			synchronized (loadingBitmapLock) {
				destinationLock = loadingBitmapLock.get(keyLock);
				if (destinationLock==null) {
					wasLocked = false;
					destinationLock = keyLock;
					loadingBitmapLock.put(keyLock, destinationLock);
				}
			}
			
			synchronized(destinationLock) {
				if (wasLocked)
					try {
						if (loadingBitmapLock.containsKey(keyLock))	destinationLock.wait();
					} catch (InterruptedException e) {
						Log.e(LOGTAG, "Interrupted loading image from " + location, e);
					}
				else {
					fromNetwork = true;
					try {
						if (destination.getParentFile()!=null) //noinspection ResultOfMethodCallIgnored
							destination.getParentFile().mkdirs();

						raw = resolver==null?NetworkUtils.httpGet(location):resolver.resolve(location);
						FileUtils.saveBytes(destination, raw);
					} catch (Exception e) {
						//noinspection ResultOfMethodCallIgnored
						destination.delete();
						Log.e(LOGTAG, "Error loading image from " + location + " " + e.getMessage());
					} finally {
						synchronized (loadingBitmapLock) {
							destinationLock.notifyAll();
							loadingBitmapLock.remove(keyLock);
						}
					}
				}
				
			}
		}
	}
	
	@Override
	public void onSuccess() {
		ImageView imageView = getImageView();
		if (imageView!=null) {
			decodeFileInTask(true);
		}
	}
	
	// run on UI single thread
	private void decodeFileInTask(final boolean original) {
		BackgroundTask<Bitmap> task = new BackgroundTask<Bitmap>() {
			@Override
			public Bitmap onBackground() throws Exception {
				Bitmap bitmap = original ? decodeAndTransform() : decodeBitmap(destinationTransformed);
				if (bitmapCallback!=null) bitmapCallback.onBackground(bitmap);
				return bitmap;
			}

			@Override
			public void onSuccess(Bitmap bitmap) {
				setBitmapOnView(bitmap);
			}
		};
		task.execute();
	}

	// run on BG single thread
	private Bitmap decodeAndTransform() {
		try {
			if (destinationTransformed!=null && destinationTransformed.exists()) {
				return decodeBitmap(destinationTransformed);
			}
		} catch (Exception e) {
			Log.e(LOGTAG, "Error processing image from " + imageTag + " on " + destinationTransformed, e);
			//noinspection ResultOfMethodCallIgnored
			destinationTransformed.delete();
		}
		String memBefore = AndroidCoreUtils.buildMemStats();
		try {
			Bitmap bitmap = decodeBitmap(destination);
			do {
				if (bitmap !=null) {
					if (transformer != null) {
						Bitmap transformed = transformer.transform(bitmap);
						// save transformed file if requested
						if (destinationTransformed!=null) {
							// save to temp file to avoid caught in the middle
							File tmpTransformed = getTempTransformedFile();
							if (compressFormat!=null) {
								AndroidCoreUtils.saveBitmap(tmpTransformed, transformed, compressFormat, 80);
							} else {
								AndroidCoreUtils.saveBitmap(tmpTransformed, transformed, 80);
							}
							//noinspection ResultOfMethodCallIgnored
							tmpTransformed.renameTo(destinationTransformed);
						}
						return transformed;
					} else {
						return bitmap;
					}
				}
				bitmap = bitmapFallback != null ? bitmapFallback.getBitmap() : null;
			} while (bitmap!=null);
		} catch (Exception e) {
			//noinspection ResultOfMethodCallIgnored
			destination.delete();
			Log.e(LOGTAG, "Error processing image from " + imageTag, e);
		}  catch (OutOfMemoryError oe) {
			String memAfter = AndroidCoreUtils.buildMemStats();
			String msg = "Cannot decode bitmap " + destination.getAbsolutePath() + " size:" + destination.length() + 
					"\nBefore " + memBefore +
					"\nAfter " + memAfter;
			throw new RuntimeException(msg, oe);			
		}
		return null;
	}
	
	private File getTempTransformedFile() {
		return new File(destinationTransformed.getParentFile(), "tmp." + destinationTransformed.getName());
	}

	private Bitmap decodeBitmap(File file) {
		if (!file.exists() || file.length() == 0) return null;
		
		String memBefore = AndroidCoreUtils.buildMemStats();
		String fileName = file.getAbsolutePath();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = false;
		try {
			Bitmap decoded = BitmapFactory.decodeFile(fileName, options);
			if (decoded == null) {
				Log.e(LOGTAG, "Cannot decode bitmap " + fileName);
			} else {
				Log.d(LOGTAG, "GC decode bitmap " + file.length() + " bytes " + decoded.getWidth() + "x" + decoded.getHeight() + " " + fileName);
			}
			return decoded;
		} catch (OutOfMemoryError oe) {
			String memAfter = AndroidCoreUtils.buildMemStats();
			String msg = "Cannot decode bitmap " + fileName + " size:" + file.length() + 
					"\nBefore " + memBefore +
					"\nAfter " + memAfter;
			throw new RuntimeException(msg, oe);
		}
	}
	
	private ImageView getImageView() {
		ImageView imageView = imageViewReference.get();
		Object tag = imageView!=null ? imageView.getTag(R.string.image_loading_id) : "";
		if (imageView == null 
				|| imageView.getVisibility() == View.GONE 
				|| !tag.equals(imageTag)) return null; // Abort
		return imageView;
	}
	
	private synchronized void setBitmapOnView(Bitmap bitmap) {
		if (bitmap == null) return;
		
		ImageView imageView = getImageView();
		if (imageView != null) {

			if (imageTag.equals(getLoadedTag())) return;
			setLoadedTag(imageTag);

			if (bitmapCallback!=null) bitmapCallback.onPostExecute(bitmap);
			
			imageView.setImageBitmap(bitmap);
			if (animation!=null && fromNetwork) imageView.startAnimation(animation);
		}
	}

	// run on UI single thread
	public synchronized void executeMulti() {
		if (location.equals(getLoadedTag())) return;
		if (location.equals(getLoadingTag())) return;
		
		setLoadedTag("");
		setLoadingTag(location);
		
		if (destinationTransformed!=null && destinationTransformed.exists()) {
			decodeFileInTask(false);
			return;
		}

		executeMulti(PARALLEL_EXECUTOR);
	}

	public void setAnimation(Animation animation) {
		this.animation = animation;
	}
	
	private String getShortName(ImageView imageView) {
		String name = imageView.toString();
		return name.length() < 40 ? name : name.substring(0, 40);
	}
}
