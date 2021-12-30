package xtvapps.core.android.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

import xtvapps.core.LocalContext;
import xtvapps.core.cache.DiskCache;

public class BitmapDiskCache extends DiskCache<Bitmap> {
	private static final int KILOBYTES = 1024;
	private static final int PNG_QUALITY = 100;
	
	private int quality = PNG_QUALITY;
	private Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;

	public BitmapDiskCache(LocalContext context, String location, int size, float memSize) {
		super(context, location, size, memSize, item -> item.getByteCount() / KILOBYTES);
	}

	public BitmapDiskCache(LocalContext context, String location, int size, int memSize) {
		super(context, location, size, memSize);
	}

	public void setDiskImageFormat(Bitmap.CompressFormat format, int quality) {
		this.format = format;
		this.quality = quality;
	}
	
	@Override
	protected Bitmap fromBytes(byte[] raw) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = false;
		return BitmapFactory.decodeByteArray(raw , 0, raw .length, options);
	}

	@Override
	protected byte[] toBytes(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(format, quality, stream);
		return stream.toByteArray();
	}
}
