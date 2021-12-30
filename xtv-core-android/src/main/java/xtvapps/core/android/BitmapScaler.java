package xtvapps.core.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class BitmapScaler {
	@SuppressWarnings("unused")
	public enum Scaling {FAST, SMOOTH}

    private final int targetWidth;
	private final int targetHeight;
	private int cropTop = 0;
	private int cropBottom = 0;
	private Scaling scaling = Scaling.SMOOTH;
	
	private float referenceWidth  = 0;
	
	public BitmapScaler(int targetWidth, int targetHeight) {
		this.targetWidth = targetWidth;
		this.targetHeight = targetHeight;
	}
	
	public void setCrop(int crop) {
		cropTop = crop;
		cropBottom = crop;
	}
	
	public void setReference(int width) {
		this.referenceWidth = width;
	}
	
	public void setScaling(Scaling scaling) {
		this.scaling = scaling;
	}
	
	public Bitmap scale(Bitmap srcBitmap) {
		Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		Rect dstRect = new Rect(0, 0, targetWidth, targetHeight);
		Rect srcRect = null;
		if (cropTop>0 || cropBottom>0) {
			int imageCropTop     = referenceWidth == 0 ? cropTop    : (int) ((srcBitmap.getHeight() / referenceWidth) * cropTop);  
			int imageCropBottom  = referenceWidth == 0 ? cropBottom : (int) ((srcBitmap.getHeight() / referenceWidth) * cropBottom);  
			
			srcRect = new Rect(0, imageCropTop, srcBitmap.getWidth(), srcBitmap.getHeight() - imageCropBottom - 1); 
		}
		
		Paint imagePaint = null;
		if (scaling == Scaling.SMOOTH) {
			imagePaint = new Paint();
			imagePaint.setFilterBitmap(true);
		}
		
		canvas.drawBitmap(srcBitmap, srcRect, dstRect, imagePaint);
		return bitmap;
	}
}
