package xtvapps.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import xtvapps.core.net.NetworkUtils;

@SuppressWarnings({"unused", "RedundantThrows", "RedundantSuppression"})
public abstract class JSONProcessor<T> {
	private static final String LOGTAG = JSONProcessor.class.getSimpleName();
	
	public T download(String url) throws Exception {
		byte[] json = NetworkUtils.httpGet(url);
		String js = new String(json).trim();
		Log.d(LOGTAG, url + " -> " + js);

		JSONObject o = new JSONObject(js);
		return build(o);
	}
	
	public List<T> downloadList(String url) throws Exception {
		List<T> list = new ArrayList<>();
		byte[] json = NetworkUtils.httpGet(url);
		String js = new String(json).trim();

		Log.d(LOGTAG, url + " -> " + js);

		JSONArray a;
		if (js.startsWith("{")) a = extractList(new JSONObject(js));
		else a = new JSONArray(js);		
		
		for(int i=0; a!=null && i<a.length(); i++) {
			JSONObject ao = a.getJSONObject(i);
			T item = build(ao);
			list.add(item);
		}
		return list;
	}
	
	@SuppressWarnings("unused")
	protected JSONArray extractList(JSONObject o) throws Exception {
		throw new Exception("Result is an object, but list was expected");
	}
	
	public abstract T build(JSONObject o) throws Exception;
}