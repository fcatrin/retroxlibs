package retrobox.keyboard.layouts;

import retrobox.keyboard.KeyboardLayout;
import retrobox.keyboard.KeyboardView;

public class Atari800KeyboardLayout {

	String labels_a1[] = {"Esc", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "<", ">", "Back	", "Brk"};
	
	String labels_a2[] = {"Tab", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "-", "=", "Return" };
	String labels_a3[] = {"Ctrl", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", ":","+", "*"};
	String labels_a4[] = {"Shift", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/", "Shift", "Inv"};
	String labels_a5[] = {"Move", "Up", "Down","Space", "Left", "Right", "[!@?]"};
	
	String labels_b1[] = {"Help", "Start", "Select", "Option", "Reset", "Clear", "Insert", "Delete"};
	String labels_b2[] = {"Move", "J1 Btn", "J1 Up", "J1 Dn", "J1 Lt", "J1 Rt", "!", "\"", "#", "$", "&", "'", "@", "?"};
	String labels_b3[] = {"[abc]", "J2 Btn", "J2 Up", "J2 Dn", "J2 Lt", "J2 Rt", "(", ")", "_", "|", "\\", "^", "[", "]"};
	
				
	String codes_a1[] = {"ATR_ESCAPE", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "SHIFT+KEY_COMMA", "SHIFT+KEY_PERIOD", "ATR_BACKSPACE", "ATR_BREAK"};
	String codes_a2[] = {"TAB", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "MINUS", "EQUALS", "ATR_RETURN"};
	String codes_a3[] = {"LEFTCTRL", "A", "S", "D", "F", "G", "H", "J", "K", "L", "KEY_SEMICOLON", "SHIFT+KEY_SEMICOLON", "PLUS", "STAR"};
	String codes_a4[] = {"LEFTSHIFT", "Z", "X", "C", "V", "B", "N", "M", "COMMA", "DOT", "SLASH", "RIGHTSHIFT"};
	String codes_a5[] = {"", "", "ATR_KEY_UP", "ATR_KEY_DOWN", "SPACE", "ATR_KEY_LEFT", "ATR_KEY_RIGHT"};
	
	String codes_b1[] = {"ATR_HELP", "ATR_START", "ATR_SELECT", "ATR_OPTION", "ATR_START", "ATR_CLEAR", "ATR_INSERT", "ATR_DELETE"};
	String codes_b2[] = {"","ATR_TRIGGER", "ATR_UP", "ATR_DOWN", "ATR_LEFT", "ATR_RIGHT", 
			 "SHIFT+KEY_1", "SHIFT+KEY_APOSTROPHE", "POUND", "SHIFT+KEY_4",
			 "SHIFT+KEY_7", "KEY_APOSTROPHE", "AT", "SHIFT+KEY_SLASH"};
	String codes_b3[] = {"","ATR_TRIGGER2", "ATR_UP2", "ATR_DOWN2", "ATR_LEFT2", "ATR_RIGHT2",
			"SHIFT+KEY_9", "SHIFT+KEY_0", "SHIFT+KEY_MINUS", "SHIFT+KEY_BACKSLASH", 
			"BACKSLASH",  "SHIFT+KEY_STAR", "SHIFT+KEY_COMMA", "SHIFT+KEY_DOT"};
			

	public KeyboardLayout[] getKeyboardLayout() {
		KeyboardLayout layout[] = new KeyboardLayout[2];
		
		KeyboardLayout kl = new KeyboardLayout();
		kl.addRow(labels_a1, codes_a1);
		kl.addRow(labels_a2, codes_a2);
		kl.addRow(labels_a3, codes_a3);
		kl.addRow(labels_a4, codes_a4);
		kl.addRow(labels_a5, codes_a5);

		kl.setKeySize("Space", 4);
		kl.setKeyCode("[!@?]", KeyboardView.SWITCH_LAYOUT + 1);
		kl.setKeyCode("Move", KeyboardView.TOGGLE_POSITION);
		kl.setKeySize("Tab", 2);
		kl.setKeySize("Ctrl", 2);
		kl.setKeySize("Return", 2);
		kl.setKeySize("Back", 2);
		kl.setKeySize("Shift", 2);

		layout[0] = kl;
		kl = new KeyboardLayout();
		kl.addRow(labels_b1, codes_b1);
		kl.addRow(labels_b2, codes_b2);
		kl.addRow(labels_b3, codes_b3);
		kl.setKeyCode("Move", KeyboardView.TOGGLE_POSITION);
		kl.setKeySize("Move", 2);
		kl.setKeyCode("[abc]", KeyboardView.SWITCH_LAYOUT + 0);
		kl.setKeySize("[abc]", 2);
		
		layout[1] = kl;
		return layout;
	}

}
