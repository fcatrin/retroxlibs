package xtvapps.emumovies;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xtvapps.core.DownloadManager;
import xtvapps.core.Log;
import xtvapps.core.xml.SimpleXML;
import xtvapps.core.xml.SimpleXML.ParserException;

public class EmuMoviesService {
	private static final String LOGTAG = EmuMoviesService.class.getSimpleName();
	
	private static final String BACKEND = "https://api.gamesdbase.com";
	private static final String URL_LOGIN = "/login.aspx";
	private static final String URL_GET_SYSTEMS = "/getsystems.aspx?sessionid={sessionId}";
	
	private static final String DATA_LOGIN = "user={user}&api={pass}&product={productId}";
	private static final String ENCODING = "UTF-8";
	private static final String MIME_FORM_URL = "application/x-www-form-urlencoded";
	
	private static String sessionId = "";
	
	public static void login(String user, String password, String productId) throws IOException, ParserException, EmuMoviesException {
		String data = DATA_LOGIN
				.replace("{user}", URLEncoder.encode(user, ENCODING))
				.replace("{pass}", URLEncoder.encode(password, ENCODING))
				.replace("{productId}", URLEncoder.encode(productId, ENCODING));
		
		Log.d(LOGTAG, data);
		String url = buildURL(URL_LOGIN);
		Document xml = SimpleXML.parse(DownloadManager.postContent(url, null, MIME_FORM_URL, data));
		Element result = SimpleXML.getElementXpath(xml.getDocumentElement(), "Result");
		
		boolean success = SimpleXML.getBoolAttribute(result, "Success", false);
		Log.d(LOGTAG, new String(SimpleXML.asString(xml.getDocumentElement())));
		if (success) {
			sessionId = SimpleXML.getAttribute(result, "Session");
		} else {
			throw new EmuMoviesException(SimpleXML.getAttribute(result, "MSG"));
		}
	}
	
	private static String buildURL(String url) {
		return BACKEND + url.replace("{sessionId}", sessionId);
	}
	
	public static void getSystems() throws UnsupportedEncodingException, IOException {
		String url = buildURL(URL_GET_SYSTEMS);
		Log.d(LOGTAG, new String(DownloadManager.download(url), ENCODING));
	}
	
	public static class EmuMoviesException extends Exception {
		private static final long serialVersionUID = 1L;

		public EmuMoviesException(String message) {
			super(message);
		}
	}

}
