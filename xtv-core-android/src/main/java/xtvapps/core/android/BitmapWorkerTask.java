package xtvapps.core.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xtvapps.core.BackgroundTask;
import xtvapps.core.LocalContext;
import xtvapps.core.android.cache.BitmapDiskCache;
import xtvapps.core.cache.NetworkCache;
import xtvapps.core.net.NetworkUtils;

public class BitmapWorkerTask extends BackgroundTask<Bitmap> {
	private static final String LOGTAG = BitmapWorkerTask.class.getSimpleName();
	private static final ExecutorService PARALLEL_EXECUTOR = Executors.newFixedThreadPool(8);
	private final String location;

	Animation animation = null;
	
	private static NetworkCache networkCache;
	private static final Integer networkCacheLock = 0;
	private final BitmapDiskCache cache;
	private boolean fromNetwork = false;
	
	private final WeakReference<ImageView> imageViewReference;

	private final BitmapTransformer transformer;
	
	public BitmapWorkerTask(LocalContext context, String location, BitmapDiskCache cache, ImageView imageView) {
		this(context, location, cache, imageView, null);
	}
	
	public BitmapWorkerTask(LocalContext context, String location, BitmapDiskCache cache, ImageView imageView, BitmapTransformer transformer) {
		synchronized (networkCacheLock) {
			if (networkCache==null) {
				networkCache = new NetworkCache(context, "bitmap.network", 200);
				Log.d(LOGTAG, "Network cache created");
			}
		}
		imageViewReference = new WeakReference<>(imageView);
		this.transformer = transformer;
		this.cache = cache;
		this.location = location;
	}
	
	@Override
	public Bitmap onBackground() {
		try {
			ImageView imageView = imageViewReference.get();
			if (imageView == null || imageView.getVisibility() == View.GONE) return null; // Abort download
			
			Log.d(LOGTAG, "Loading from cache " + location);
			
			Bitmap bitmap = cache!=null?cache.getQueued(location):null;
			
			if (bitmap == null) {
				byte[] raw = networkCache.getQueued(location);
				if (raw == null) {
					fromNetwork = true;
					raw = NetworkUtils.httpGet(location);
					networkCache.putQueued(location, raw);
				}
						
				Log.d(LOGTAG, "Decoding " + location);
				bitmap = decodeBitmap(raw);
				if (bitmap!=null) {
					if (transformer != null) bitmap = transformer.transform(bitmap);
					Log.d(LOGTAG, "Storing in cache " + location);
					if (cache!=null) cache.putQueued(location, bitmap);
				} else {
					networkCache.remove(location);
				}
			}
			return bitmap;
		} catch (Exception e) {
			Log.e(LOGTAG, "Error processing image on " + location, e);
			if (cache!=null) cache.putQueued(location, null);
			return null;
		}
	}
	
	@Override
	public void onSuccess(Bitmap bitmap) {
		if (imageViewReference != null && bitmap != null) {
			final ImageView imageView = imageViewReference.get();
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
				if (animation!=null && fromNetwork) imageView.startAnimation(animation);
			}
		}
	}
	
	private Bitmap decodeBitmap(byte[] raw) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = false;
		return BitmapFactory.decodeByteArray(raw , 0, raw .length, options);
	}
	
	public void executeMulti() {
		executeMulti(PARALLEL_EXECUTOR);
	}
	
	public void setAnimation(Animation animation) {
		this.animation = animation;
	}
	
}
