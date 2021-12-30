package retrox.utils.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import xtvapps.core.ListOption;
import xtvapps.core.android.AndroidFonts;

public class ListOptionAdapter extends BaseAdapter {
	
	final List<ListOption>options;

	static ViewCustomizer viewCustomizer = null;
	
	public static final String fontName = RetroXUtils.FONT_DEFAULT_R;
	
	public ListOptionAdapter(List<ListOption> options) {
		this.options = options;
	}
	
	@Override
	public int getCount() {
		return options.size();
	}

	@Override
	public Object getItem(int position) {
		return options.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) parent.getContext()
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View optionView = convertView!=null?convertView:inflater.inflate(R.layout.listitem_options, parent, false);
		
		TextView txtName = optionView.findViewById(R.id.optionName);
		TextView txtValue = optionView.findViewById(R.id.optionValue);
		
		AndroidFonts.setViewFont(txtName, fontName);
		AndroidFonts.setViewFont(txtValue, fontName);
		
		ListOption kv = options.get(position);
		String name = kv.getValue();
		String preset = kv.getPreset();
		
		txtName.setText(name);
		if (preset!=null) {
			txtValue.setText(preset);
			txtValue.setVisibility(View.VISIBLE);
		} else {
			txtValue.setVisibility(View.GONE);
		}

		if (viewCustomizer!=null) viewCustomizer.customize(txtName, txtValue);
		
		return optionView;
	}

	public static void setViewCustomizer(ViewCustomizer viewCustomizer) {
		ListOptionAdapter.viewCustomizer = viewCustomizer;
	}

	public interface ViewCustomizer {
		void customize(TextView txtName, TextView txtValue);
	}
	
}
