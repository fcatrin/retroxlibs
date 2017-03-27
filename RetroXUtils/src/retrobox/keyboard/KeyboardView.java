package retrobox.keyboard;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import retrobox.utils.R;
import retrobox.utils.RetroBoxUtils;
import xtvapps.core.AndroidFonts;

public class KeyboardView extends FrameLayout {
	
	public static final int SWITCH_LAYOUT = 0x1000000;

	private List<KeyboardLayout> keylayouts = new ArrayList<KeyboardLayout>();
	private OnKeyListener onKeyListener;
	int activeLayout = 0;

	public KeyboardView(Context context) {
		super(context);
	}

	public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public KeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void init(Context ctx, KeyboardLayout keylayout) {
		Log.d("KEYB", "init");
		keylayouts.add(keylayout);
		if (keylayouts.size() == 1) switchLayout(ctx, 0);
		
	}
	
	private void switchLayout(final Context ctx, int index) {
		activeLayout = index;
		
		removeAllViews();
		
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				int keyCode = (Integer)view.getTag();
				if ((keyCode & SWITCH_LAYOUT) != 0) {
					switchLayout(ctx, keyCode & ~SWITCH_LAYOUT);
					getChildAt(0).requestFocus();
				}
				if (onKeyListener!=null) {
					onKeyListener.onKey(view, keyCode, null);
				}
			}
		};
		
		KeyboardLayout keylayout = keylayouts.get(index);
		for(List<KeyDef> row : keylayout.getKeys()) {
			for(KeyDef keydef : row) {
				final Button view = new Button(ctx);
				view.setLayoutParams(new LinearLayout.LayoutParams(
				                                     LinearLayout.LayoutParams.MATCH_PARENT,
				                                     LinearLayout.LayoutParams.WRAP_CONTENT));
				view.setBackgroundResource(R.drawable.button_bar);
				view.setTextSize(ctx.getResources().getDimensionPixelSize(R.dimen.text_normal));
				view.setText(keydef.getLabel());
				view.setTag(keydef.getValue());
				
				AndroidFonts.setViewFont(view, RetroBoxUtils.FONT_DEFAULT_R);
				
				keydef.setView(view);
				
				view.setOnClickListener(listener);
				
				addView(view);
			}
		}
		
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d("KEYB", "onLayout "  + l + "," + t + " " + r + ", " + b);
		
		if (keylayouts.size() == 0) return;
		KeyboardLayout keylayout = keylayouts.get(activeLayout);
		
		int width  = r - l - getPaddingLeft() - getPaddingRight();
		int height = b - t - getPaddingTop() - getPaddingBottom();
		int top  = getPaddingTop();
		
		int rowHeight = height / keylayout.getKeys().size();
		
		for(List<KeyDef> row : keylayout.getKeys()) {
			int totalSize = 0;
			for(KeyDef keydef : row) {
				totalSize += keydef.getSize();
			}
			
			int keySize = width / totalSize;
			int left = getPaddingLeft();
			for(int k=0; k<row.size(); k++) {
				KeyDef keydef = row.get(k);
				int w = keySize*keydef.getSize();
				
				if (k+1 == row.size()) {
					// fill all remaining space
					w = width - left;
				}
				
				Button view = (Button)keydef.getView();
				// force relayout inside the button
				view.measure(
						MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), 
						MeasureSpec.makeMeasureSpec(LinearLayout.LayoutParams.WRAP_CONTENT, MeasureSpec.AT_MOST));
				
				view.layout(left, top, left + w, top + rowHeight);
				Log.d("KEYB", "onLayout " + keydef.getLabel() + " " + left + "," + top + " " + w + "," + rowHeight);
				left += w;
			}
			top += rowHeight;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d("KEYB", "onMeasure " + widthMeasureSpec + ", " + heightMeasureSpec);
		if (keylayouts.size() == 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
		
		KeyboardLayout keylayout = keylayouts.get(activeLayout);
		
		// measure one child to get height
		View sampleChild = getChildAt(0);
		getChildAt(0).measure(
				MeasureSpec.makeMeasureSpec(LinearLayout.LayoutParams.MATCH_PARENT, MeasureSpec.AT_MOST), 
				MeasureSpec.makeMeasureSpec(LinearLayout.LayoutParams.WRAP_CONTENT, MeasureSpec.AT_MOST));
		Log.d("KEYB", "measured button " + getChildAt(0).getMeasuredHeight());
		
		int height = keylayout.getKeys().size() * sampleChild.getMeasuredHeight();
		int width = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(width, height);
		Log.d("KEYB", "onMeasured " + width + "," + height);
	}
	
	public void setOnKeyListener(OnKeyListener listener) {
		this.onKeyListener = listener;
	}
	
}
