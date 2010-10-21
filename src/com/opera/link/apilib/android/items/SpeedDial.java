package com.opera.link.apilib.android.items;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.opera.link.apilib.android.Base64;


public class SpeedDial extends Element {

	public static final String ITEM_TYPE = "speeddial";
	
	@Override
	public String getDatatype() {
		return ITEM_TYPE;
	}
	
	public String title;
	public String uri;
	
	private int position;
	public int getPosition() {
		return position;
	}

	public byte[] thumbnail;
	
	protected SpeedDial() {
		
	}
	
	public SpeedDial(String uri, String title, int position) {
		this.uri = uri;
		this.title = title;
		this.position = position;
	}
	
	@Override
	void loadParameters(JSONObject json) throws JSONException {
		if (json.has(Element.TITLE_JSON_FIELD)) {
			title = json.getString(Element.TITLE_JSON_FIELD);
		}
		if (json.has(Element.URI_JSON_FIELD)) {
			uri = json.getString(Element.URI_JSON_FIELD);
		}
		if (json.has(Element.THUMBNAIL_JSON_FIELD)) {
			try {
				thumbnail = Base64.decode(json.getString(Element.THUMBNAIL_JSON_FIELD), Base64.NO_OPTIONS);
			} catch (IOException e) {
				e.printStackTrace();
				thumbnail = null;
			}
		}
	}

	@Override
	public HashMap<String, String> createParamsDict() {
		final String thumbnailString;
		if (thumbnail != null) {
			thumbnailString = Base64.encodeBytes(thumbnail);
		} else {
			thumbnailString = null;
		}
		
		HashMap<String, String> params = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;

			{
				put(Element.TITLE_JSON_FIELD, title);
				put(Element.URI_JSON_FIELD, uri);
				put(Element.THUMBNAIL_JSON_FIELD, thumbnailString);
			}
		};
		return skipNullParams(params);
	}

	@Override
	public String getItemType() {
		return ITEM_TYPE;
	}
	
}
