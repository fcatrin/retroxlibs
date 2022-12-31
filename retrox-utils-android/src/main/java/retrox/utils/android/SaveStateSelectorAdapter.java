package retrox.utils.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrox.utils.android.content.SaveStateInfo;
import xtvapps.core.android.AndroidFonts;

public class SaveStateSelectorAdapter extends BaseAdapter {
	private static final String LOGTAG = SaveStateSelectorAdapter.class.getSimpleName();
	final List<SaveStateInfo> content;
	private final Activity activity;
	public static boolean isCRT = false;
	public static int shotFontSize = 0;
	
	public SaveStateSelectorAdapter(Activity activity, List<SaveStateInfo> content, int selected) {
		this.activity = activity;
		this.content = content;

		List<SaveStateInfo> ordered = new ArrayList<>(content);
		
		Collections.sort(ordered, (lhs, rhs) -> {
			long lts = lhs.getTimestamp();
			long rts = rhs.getTimestamp();
			return Long.compare(rts, lts);
		});
		
		for(int i=0; i<ordered.size(); i++) {
			ordered.get(i).setOrder(i);
		}
		
		for(int i=0; i<content.size(); i++) {
			SaveStateInfo info = content.get(i);
			
			info.setSelected(i==selected);
			info.setSlotInfo("Slot " + (i+1) + ":");
			
			String infoText = activity.getString(R.string.slot_empty);
			long ts = info.getTimestamp();
			if (ts!=0) {
				DateFormat df = DateFormat.getDateTimeInstance();
				infoText = df.format(new Date(ts));
				if (info.getOrder() == 0) {
					infoText += " (" +
							activity.getString(R.string.slot_latest)
							+  ")";
				} else {
					infoText += " (" +
							activity.getString(R.string.slot_latest_n).replace("{n}", String.valueOf(info.getOrder()))
							+ ")";
				}
			}
			info.setInfo(infoText);

		}
		
	}
	
	public void loadImages() {
		for(SaveStateInfo info : content) {
			if (info.getImageResourceId()!=0) return;

			File file = info.getScreenshot();
			Bitmap bitmap = null;
			if (file != null && file.exists()) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDither = false;
				System.out.println("loading shot " + file.getAbsolutePath());
				bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
			}
			info.setBitmap(bitmap);
		}
	}
	
	public void releaseImages() {
		for(SaveStateInfo info : content) {
			info.setBitmap(null);
		}
	}
	
	@Override
	public Object getItem(int index) {
		return content.get(index);
	}
	
	@Override
	public int getCount() {
		return content.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(view == null) {
			LayoutInflater layout =  LayoutInflater.from(parent.getContext());
			view = layout.inflate(R.layout.savestate_cell, parent, false);
			AndroidFonts.setViewFont(view.findViewById(R.id.savestate_label), RetroXUtils.FONT_DEFAULT_M);
		}
		
		ImageView imageScreenshot = view.findViewById(R.id.savestate_image);
		TextView textView = view.findViewById(R.id.savestate_label);

		if (isCRT) {
			int imageWidth = (int)(activity.getResources().getDimensionPixelSize(R.dimen.screenshot_width) *  4.0f / 3.0f);
			((ViewGroup.MarginLayoutParams)imageScreenshot.getLayoutParams()).width = imageWidth;

			int textWidth = (int)(activity.getResources().getDimensionPixelSize(R.dimen.text_normal) *  4.0f / 3.0f);
			((ViewGroup.MarginLayoutParams)textView.getLayoutParams()).width = textWidth;

			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, shotFontSize);
		}

		updateGridSize(view);

		final SaveStateInfo info = (SaveStateInfo)getItem(position);
		
		imageScreenshot.setImageBitmap(null);
		
		int imageResourceId = info.getImageResourceId();
		if (imageResourceId!=0) {
			imageScreenshot.setImageResource(info.exists()?imageResourceId:0);
		} else {
			imageScreenshot.setImageBitmap(info.getBitmap());
		}
		
		if (!info.exists()) {
			textView.setText(activity.getString(R.string.slot_v_empty));
		} else if (info.getOrder() == 0) {
			textView.setText(activity.getString(R.string.slot_v_latest));
		} else {
			String chars = activity.getString(R.string.slot_v_latest_n);
			textView.setText(chars.substring(0,  (info.getOrder())*2));
		}

		return view;
	}

	private boolean gridSizeUpdated = false;
	private void updateGridSize(View view) {
		if (gridSizeUpdated) return;
		gridSizeUpdated = true;

		GridView grid = (GridView)activity.findViewById(R.id.savestates_grid);

		int padding = view.getPaddingLeft() + view.getPaddingRight();
		int imageWidth = view.findViewById(R.id.savestate_image).getLayoutParams().width;
		int textWidth  = view.findViewById(R.id.savestate_label).getLayoutParams().width;
		int separator = grid.getHorizontalSpacing();
		int cellWidth = imageWidth + textWidth + padding;
		int columns = grid.getNumColumns();

		int width = columns * cellWidth + (columns-1) * separator;
		grid.getLayoutParams().width = width;
		grid.setVisibility(View.VISIBLE);
	}

}
