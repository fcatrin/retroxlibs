package xtvapps.core.tonido;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import xtvapps.core.CoreUtils;
import xtvapps.core.LocalContext;
import xtvapps.core.Log;
import xtvapps.core.Preferences;
import xtvapps.core.PreferencesEditor;
import xtvapps.core.cache.EntriesCache;
import xtvapps.core.net.HttpPostRequest;
import xtvapps.core.net.NetworkUtils;
import xtvapps.core.xml.ParserException;
import xtvapps.core.xml.SimpleXML;

@SuppressWarnings("unused")
public class TonidoService {
	private static final String LOGTAG = TonidoService.class.getSimpleName();
	private static final int MAX_FILES = 50;
	
	private static final String KEY_SERVERS = "servers";

	private static String server;
	private static final Map<String, String> cookies = new HashMap<>();
	
	private static final EntriesCache<List<TonidoFile>> dirCache = new EntriesCache<>(5, 10000);
	
	private static final List<TonidoServer> servers = new ArrayList<>();
	
	private static String getUrl(String resource, String params) {
		return "https://" + server + "/" + resource + "?" + params;
	}
	
	private static String buildParams(Map<String, String> params) throws UnsupportedEncodingException {
		StringBuilder text = new StringBuilder();
		for(Entry<String, String> param : params.entrySet()) {
			if (text.length()!=0) {
				text.append("&");
			}
			
			text.append(param.getKey());
			text.append("=");
			text.append(URLEncoder.encode(param.getValue(), "UTF-8"));
		}
		return text.toString();
	}
	
	private static byte[] getCmd(String resource, Map<String, String> params) throws IOException {
		Map<String, String> headers = new HashMap<>();
		if (cookies.size()!=0) {
			headers.put("Cookie", buildCookies());
		}
		String url = getUrl(resource, buildParams(params));
		Log.d(LOGTAG, "GET " + url);
		return NetworkUtils.httpGet(url, headers);
	}

	private static byte[] postCmd(String resource, Map<String, String> params) throws IOException {
		return postCmd(resource, params, null);
	}

	private static byte[] postCmd(String resource, Map<String, String> params, Map<String, List<String>> responseHeaders) throws IOException {
		Map<String, String> headers = new HashMap<>();
		if (cookies.size() != 0) {
			headers.put("Cookie", buildCookies());
		}
		String url = getUrl(resource, "");
		Log.d(LOGTAG, "Tonido POST " + url + " " + params);

		HttpPostRequest postRequest = new HttpPostRequest(url, params);
		postRequest.requestHeaders = headers;

		NetworkUtils.httpPost(postRequest);
		if (responseHeaders != null) {
			responseHeaders.putAll(postRequest.responseHeaders);
		}
		return postRequest.response;
	}
	
	public static void auth(String server, String profile, String password) throws IOException, ParserException {
		if (CoreUtils.isEmptyString(server) || CoreUtils.isEmptyString(profile) || CoreUtils.isEmptyString(password)) {
			throw new TonidoAuthException("Log in required");
		}
		
		if (server.equals(TonidoService.server)) {
			if (!cookies.isEmpty()) return;
			
			Log.d(LOGTAG, "Authorizing server " + server);
			
			String resource = "core/getauthenticationinfo";
			Map<String, String> params = new HashMap<>();
			params.put("profile", profile);
			Document xml = SimpleXML.parse(getCmd(resource, params));
			
			Element authElement = SimpleXML.getElementXpath(xml.getDocumentElement(), "info/authenticated");
			boolean authenticated = CoreUtils.str2i(SimpleXML.getText(authElement)) == 1;
			
			if (authenticated) return;
		}
		
		login(server, profile, password);
	}

	
	private static void login(String server, String profile, String password) throws IOException, ParserException {
		TonidoService.server = server;
		cookies.clear();
		
		String resource = "core/loginprofile";
		Map<String, String> params = new HashMap<>();
		params.put("profile", profile);
		params.put("password", CoreUtils.sha1(password));

		Map<String, List<String>> responseHeaders = new HashMap<>();

		Document xml = SimpleXML.parse(postCmd(resource, params, responseHeaders));
		
		Element commandElement = SimpleXML.getElement(xml.getDocumentElement(), "command");
		int result  = CoreUtils.str2i(SimpleXML.getText(commandElement, "result"));
		String message = SimpleXML.getText(commandElement, "message");

		if (result == 1) {
			fetchCookies(responseHeaders);
		} else {
			throw new TonidoAuthException(message);
		}
		
	}

	private static void fetchCookies(Map<String, List<String>> lastHeaderFields) {
		List<String> cookiesSet = lastHeaderFields.get("Set-Cookie");
		for(String cookieString : cookiesSet) {
			String part = cookieString.split(";")[0];
			String[] cookie = part.split("=");
			if (cookie.length != 2) continue;
			
			cookies.put(cookie[0], cookie[1]);
		}
		Log.d(LOGTAG, cookies.toString());
	}
	
	private static String buildCookies() {
		StringBuilder buffer = new StringBuilder();
		for(Entry<String, String> cookie : cookies.entrySet()) {
			if (buffer.length() != 0) {
				buffer.append("; ");
			}
			
			buffer.append(cookie.getKey()).append("=").append(cookie.getValue());
		}
		return buffer.toString();
	}
	
	public static InputStream open(TonidoFile file) throws IOException {
		if (file.isDirectory()) return null;
		
		String resource = "core/downloadfile";
		
		Map<String, String> headers = new HashMap<>();
		if (cookies.size()!=0) {
			headers.put("Cookie", buildCookies());
		}
		
		Map<String, String> params = new HashMap<>();
		params.put("filepath", local2remote(file));
		params.put("filename", file.getName());
		params.put("disposition", "attachment");
		
		String url = getUrl(resource, buildParams(params));
		URLConnection connection = NetworkUtils.httpOpenConnection(url, headers);
		return connection.getInputStream();
	}

	public static synchronized List<TonidoFile> list(LocalContext context, TonidoFile dir) throws IOException, ParserException {
		if (!dir.isDirectory()) return null;
		
		String dirCacheKey = dir.getCanonicalPath();
		Log.d(LOGTAG, "list dirCacheKey " + dirCacheKey);
		
		List<TonidoFile> files = dirCache.get(dirCacheKey);
		if (files!=null) return files;
		
		files = new ArrayList<>();

		String path = dir.getResource();
		boolean isRootDir = true;
		
		if (!CoreUtils.isEmptyString(path)) {
			isRootDir = false;
			path = local2remote(dir);
		}
		
		String resource = "core/getfilelist";
		Map<String, String> params = new HashMap<>();
		params.put("path", path);
		
		Log.d(LOGTAG, "request dir for path " + path);
		
		int start = 0;
		do {
			params.put("start", String.valueOf(start));
			params.put("limit", String.valueOf(MAX_FILES));
			
			Document xml = SimpleXML.parse(getCmd(resource, params));
			List<Element> elements = SimpleXML.getElements(xml.getDocumentElement(), "entry");
			for(Element element : elements) {
				TonidoFile entry = parseFile(dir, element);
				
				if (isRootDir) {
					TonidoServer server = buildTonidoServer(entry);
					registerServer(context, server);
				}
				
				entry.setResource(remote2local(entry));
				
				files.add(entry);
			}
			if (elements.size() < MAX_FILES) break;
			start += MAX_FILES;
		} while (true);
		
		dirCache.put(dirCacheKey, files);
		return files;
	}

	private static String getPathSeparator(TonidoServer server) {
		return server.isWindows() ? "\\" : "/";
	}
	
	
	private static String getRootPath(TonidoServer server) {
		String rootPath = server.getRoot();
		int p = rootPath.lastIndexOf(getPathSeparator(server));
		if (p>0) {
			rootPath = rootPath.substring(0, p);
		}
		return rootPath;
	}

	private static String getLastPath(TonidoServer server) {
		String rootPath = server.getRoot();
		int p = rootPath.lastIndexOf(getPathSeparator(server));
		if (p>0) {
			rootPath = rootPath.substring(p);
		}
		
		rootPath = rootPath.replace("\\", "/");
		return rootPath;
	}

	private static String local2remote(TonidoFile file) {
		String serverName = file.getServer();
		TonidoServer server = getServer(serverName);
		if (server == null) return null;
		
		return getRootPath(server) + file.getResource();
	}
	
	private static String remote2local(TonidoFile file) {
		String serverName = file.getServer();
		TonidoServer server = getServer(serverName);
		if (server == null) return null;

		String resource = file.getResource().replace(server.getRoot(), getLastPath(server));
		return resource.replace("\\",  "/");
	}

	private static TonidoFile parseFile(TonidoFile tonidoFile, Element element) {
		TonidoFile file = new TonidoFile(tonidoFile.getUrl());
		
		file.setServer(tonidoFile.getServer());
		file.setResource(SimpleXML.getText(element, "path"));
		file.setSize(CoreUtils.str2l(SimpleXML.getText(element, "fullsize")));
		file.setModified(CoreUtils.str2l(SimpleXML.getText(element, "modifiedepoch")));
		file.setDirectory("dir".equals(SimpleXML.getText(element, "type")));

		Log.d(LOGTAG, "parseFile server:" + file.getServer() + ", res:" + file.getResource());
		
		return file;
	}

	public static TonidoServer buildTonidoServer(TonidoFile rootDir){
		boolean isWindows = false;
		String rootPath = rootDir.getResource();
		if (rootPath.substring(1).startsWith(":")) {
			isWindows = true;
		}
		
		TonidoServer server = new TonidoServer();
		server.setName(rootDir.getServer());
		server.setRoot(rootPath);
		server.setWindows(isWindows);
		Log.d(LOGTAG, "buildTonidoServer " + server);
		return server;
	}
	
	public static void registerServer(LocalContext context, TonidoServer server) {
		for(int i=0; i<servers.size(); i++) {
			if (servers.get(i).getName().equals(server.getName())) {
				servers.remove(i);
				break;
			}
		}
		
		servers.add(server);
		save(context);
	}
	
	public static List<TonidoServer> getServers() {
		return servers;
	}
	
	public static TonidoServer getServer(String name) {
		// the list is short so there is no need for a map or similar
		for(TonidoServer server : servers) {
			if (server.getName().equals(name)) return server;
		}
		return null;
	}
	
	private static Preferences getPreferences(LocalContext context) {
		return context.getPreferences("tonido_service");
	}
	
	private static void save(LocalContext context) {
		JSONArray a = new JSONArray();
		for(TonidoServer server : TonidoService.getServers()) {
			try {
				a.put(server.toJSON());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		Preferences preferences = getPreferences(context);
		PreferencesEditor editor = preferences.edit();
		editor.putString(KEY_SERVERS, a.toString());
		editor.apply();
	}
	
	public static void load(LocalContext context) {
		if (!TonidoService.getServers().isEmpty()) return;
		
		Preferences preferences = getPreferences(context);
		String sServers = preferences.getString(KEY_SERVERS, null);
		
		if (sServers!=null) {
			try {
				JSONArray aServers = new JSONArray(sServers);
				for(int i=0; i<aServers.length(); i++) {
					JSONObject oServer = aServers.getJSONObject(i);
					TonidoServer server = TonidoServer.fromJSON(oServer);
					TonidoService.registerServer(context, server);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
