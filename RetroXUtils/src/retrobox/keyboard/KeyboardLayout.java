package retrobox.keyboard;

import java.util.ArrayList;
import java.util.List;

public class KeyboardLayout {
	List<List<KeyDef>> keys = new ArrayList<List<KeyDef>>();

	public KeyboardLayout() {}
	
	public void addRow(String labels[], int codes[]) {
		List<KeyDef> row = new ArrayList<KeyDef>();
		for(int i=0; i<labels.length; i++) {
			row.add(new KeyDef(labels[i], codes[i]));
		}
		keys.add(row);
	}

	public List<List<KeyDef>> getKeys() {
		return keys;
	}

	public void setKeySize(String label, int size) {
		for(List<KeyDef> row : keys) {
			for(KeyDef keydef : row) {
				if (label.equals(keydef.getLabel())) {
					keydef.setSize(size);
				}
			}
		}
	}
	
	public void setKeyCode(String label, int code) {
		for(List<KeyDef> row : keys) {
			for(KeyDef keydef : row) {
				if (label.equals(keydef.getLabel())) {
					keydef.setValue(code);
				}
			}
		}
	}
}
