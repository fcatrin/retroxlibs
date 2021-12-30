package xtvapps.core.content;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class MediaElement implements Persistent {
	private String id;
	private String title;
	private String description;
	private String image;
	
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject o = new JSONObject();
		o.put("id", id);
		o.put("title", title);
		o.put("description", description);
		o.put("image", image);
		return o;
	}
	@Override
	public void fromJSON(JSONObject o) throws JSONException {
		id = o.optString("id");
		title = o.getString("title");
		description = o.optString("description");
		image = o.optString("image");
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	
}
