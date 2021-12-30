package xtvapps.core.android;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import xtvapps.core.content.KeyValueMap;

public class IconMenuAdapter extends ArrayAdapter<KeyValueMap>{

	private final int resourceId;
	
	public IconMenuAdapter(Context context, int resourceId, List<KeyValueMap> items) {
		super(context, resourceId, items);
		this.resourceId = resourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
	
		if(row==null){
			LayoutInflater inflater = LayoutInflater.from(getContext());
			row=inflater.inflate(resourceId, parent, false);
		}
	 
		TextView label= row.findViewById(R.id.menu_item_text);
		label.setText(getItem(position).getValue());
		
		ImageView icon= row.findViewById(R.id.menu_item_icon);
	 
		icon.setImageDrawable((Drawable)getItem(position).get("icon"));
		return row;
	}

}
