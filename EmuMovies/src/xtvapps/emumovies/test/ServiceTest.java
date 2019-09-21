package xtvapps.emumovies.test;

import org.json.JSONObject;

import xtvapps.core.Log;
import xtvapps.core.SharedPreferences;
import xtvapps.emumovies.EmuMoviesService;

public class ServiceTest {
	public static void main(String args[]) throws Exception {
		Log.TRACE = true;

		JSONObject prefs = new SharedPreferences("emumovies").load();
		String user = prefs.getString("user");
		String pass = prefs.getString("pass");
		String apiKey = prefs.getString("api_key");
		
		EmuMoviesService.login(user, pass, apiKey);
		// EmuMoviesService.getSystems();
		// EmuMoviesService.getMedias();
		System.out.println(EmuMoviesService.searchSingle("Bomberman '94", "NEC_PC_Engine", "Box"));
	}

}
