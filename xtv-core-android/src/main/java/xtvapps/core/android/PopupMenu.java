package xtvapps.core.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;

import xtvapps.core.Callback;

public class PopupMenu<T> implements OnItemClickListener {
	private final ListPopupWindow listPopupWindow;
	private final Callback<T> callback;
	
	@SuppressLint("UseCompatLoadingForDrawables")
	public PopupMenu(Context context, View anchorView, int selector, ArrayAdapter<T> adapter, Callback<T> callback) {
		this.callback = callback;
		
		listPopupWindow = new ListPopupWindow(context);
		listPopupWindow.setAdapter(adapter);
		
		listPopupWindow.setAnchorView(anchorView);
		if (selector>0) listPopupWindow.setListSelector(context.getResources().getDrawable(selector));
		
		listPopupWindow.setModal(true);
		listPopupWindow.setOnItemClickListener(this);
	}
	
	public void show() {
        listPopupWindow.show();
	}
	
	public void setWidth(int width) {
		listPopupWindow.setWidth(width);
	}

	public void setHeight(int height) {
		listPopupWindow.setHeight(height);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		callback.onResult((T)adapter.getAdapter().getItem(position));
		listPopupWindow.dismiss();
	}
}
