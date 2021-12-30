package xtvapps.core.tonido;

import org.json.JSONException;
import org.json.JSONObject;

public class TonidoServer {
	
	String name;
	String root;
	boolean isWindows;

	public TonidoServer() {}
	
	public static TonidoServer fromJSON(JSONObject o) throws JSONException {
		TonidoServer server = new TonidoServer();
		server.setName(o.getString("name"));
		server.setRoot(o.getString("root"));
		server.setWindows(o.getBoolean("is_windows"));
		return server;
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject o = new JSONObject();
		o.put("name", getName());
		o.put("root", getRoot());
		o.put("is_windows", isWindows());
		return o;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public boolean isWindows() {
		return isWindows;
	}

	public void setWindows(boolean isWindows) {
		this.isWindows = isWindows;
	}

	@Override
	public String toString() {
		try {
			return toJSON().toString();
		} catch (JSONException e) {
			return super.toString();
		}
	}

}
