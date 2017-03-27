package retrobox.keyboard;

import android.view.View;

public class KeyDef {

	private String label;
	private int value;
	private int size = 1;
	private View view;

	public KeyDef(String label, int value) {
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	
}
