package retrobox.themes;

import java.util.HashMap;
import java.util.Map;

import xtvapps.core.Utils;

public class Color {
	private static final String LOGTAG = Color.class.getSimpleName();
	private static Map<String, Color> namedColors = new HashMap<String, Color>();
	
	public int r;
	public int g;
	public int b;
	public int a;

	public static void addNamedColor(String name, String spec) {
		if (name == null || spec == null) {
			throw new RuntimeException("Invalid named color name:" + name + ", spec:" + spec);
		}
		Color color = build(spec);
		if (color!=null) namedColors.put(name, color);
	}
	
	public static Color getNamedColor(String name) {
		Color color = namedColors.get(name);
		if (color == null) {
			throw new RuntimeException("Unknown color:" + name );
		}
		return color;
	}
	
	public static Color build(String spec, Color defaultColor) {
		if (spec == null) return defaultColor;
		return build(spec);
	}
	
	public static Color build(String spec) {
		if (spec != null) {
			
			Color namedColor = namedColors.get(spec);
			if (namedColor!=null) {
				return clone(namedColor);
			}
			
			if (spec.startsWith("#") && spec.length()==9) {
				spec = spec.substring(1);
				
				Color color = new Color();
				color.a = buildComponent(spec.substring(0));
				color.r = buildComponent(spec.substring(2));
				color.g = buildComponent(spec.substring(4));
				color.b = buildComponent(spec.substring(6));
				return color;
			} else {
				throw new RuntimeException("Unknown color:" + spec);
			}
		}

		return null;
	}

	private static int buildComponent(String hex) {
		return Utils.strHex2i(hex, 0);
	}

	public int asInt() {
		return a << 24 | r << 16 | g << 8 | b;
	}

	public String asHex() {
		return String.format("#%02x%02x%02x%02x", a, r, g, b);
	}
	
	private static Color clone(Color color) {
		Color newColor = new Color();
		newColor.a = color.a;
		newColor.r = color.r;
		newColor.g = color.g;
		newColor.b = color.b;
		return newColor;
	}
}
