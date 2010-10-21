package com.opera.link.apilib.android.items;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.opera.link.apilib.android.Base64;


public class Bookmark extends BookmarkFolderEntry {
	
	public static final String ITEM_TYPE = "bookmark";
	
	public String title;
	public String uri;
	
	public String nickname;
	public String description;
	public byte[] icon;
	public Date visited;
	public Date created;
	
	protected Bookmark() { 

	}
	
	public Bookmark(String title, String uri) {
		this();
		this.title = title;
		this.uri = uri;
		this.created = new Date();
	}
	
	@Override
	void loadParameters(JSONObject json) throws JSONException {
		if (json.has(Element.TITLE_JSON_FIELD)) {
			title = json.getString(Element.TITLE_JSON_FIELD);
		}
		if (json.has(Element.URI_JSON_FIELD)) {
			uri = json.getString(Element.URI_JSON_FIELD);
		}
		if (json.has(Element.NICKNAME_JSON_FIELD)) {
			nickname = json.getString(Element.NICKNAME_JSON_FIELD);
		}
		if (json.has(Element.DESCRIPTION_JSON_FIELD)) {
			description = json.getString(Element.DESCRIPTION_JSON_FIELD);
		}
		if (json.has(Element.ICON_JSON_FIELD)) {
			
			try {
				icon = Base64.decode(json.getString(Element.ICON_JSON_FIELD), Base64.NO_OPTIONS);
			} catch (IOException e) {
				e.printStackTrace();
				icon = null;
			}
		}
		if (json.has(Element.CREATED_JSON_FIELD)) {
			created = parseDate(json.getString(Element.CREATED_JSON_FIELD));
		}		
		if (json.has(Element.VISITED_JSON_FIELD)) {
			visited = parseDate(json.getString(Element.VISITED_JSON_FIELD));
		}
	}
	
	@Override
	public boolean isBookmark() {
		return true;
	}

	@Override
	public HashMap<String, String> createParamsDict() {
		final String iconString;
		if (icon != null) {
			iconString = Base64.encodeBytes(icon); 
		} else {
			iconString = null;
		}
		final String createdString;
		if (created != null) {
			createdString =  dateToString(created);
		} else {
			createdString = null;
		}
		final String visitedString;
		if (visited != null) {
			visitedString =  dateToString(created);
		} else {
			visitedString = null;
		}
		
		HashMap<String, String> params = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;

			{
				put(Element.TITLE_JSON_FIELD, title);
				put(Element.URI_JSON_FIELD, uri);
				put(Element.NICKNAME_JSON_FIELD, nickname);
				put(Element.DESCRIPTION_JSON_FIELD, description);
				put(Element.ICON_JSON_FIELD, iconString);
				put(Element.CREATED_JSON_FIELD, createdString);
				put(Element.VISITED_JSON_FIELD, visitedString);
			}
		};
		return skipNullParams(params);
	}

	@Override
	public String getItemType() {
		return ITEM_TYPE;
	}

}
