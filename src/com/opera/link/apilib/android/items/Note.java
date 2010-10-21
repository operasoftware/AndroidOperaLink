package com.opera.link.apilib.android.items;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class Note extends NoteFolderEntry {

	public static final String ITEM_TYPE = "note";
	
	public String content;
	
	public Date created;
	public String uri;

	protected Note() {

	}
	
	public Note(String content) {
		this();
		this.content = content;
		this.created = new Date();
	}

	@Override
	void loadParameters(JSONObject json) throws JSONException {
		if (json.has(Element.CONTENT_JSON_FIELD)) {
			content = json.getString(Element.CONTENT_JSON_FIELD);
		}
		if (json.has(Element.URI_JSON_FIELD)) {
			uri = json.getString(Element.URI_JSON_FIELD);
		}
		if (json.has(Element.CREATED_JSON_FIELD)) {
			created = parseDate(json.getString(Element.CREATED_JSON_FIELD));
		}		
	}

	@Override
	public HashMap<String, String> createParamsDict() {
		final String createdString;
		if (created != null) {
			createdString =  dateToString(created);
		} else {
			createdString = null;
		}

		HashMap<String, String> params = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;

			{
				put(Element.CONTENT_JSON_FIELD, content);
				put(Element.URI_JSON_FIELD, uri);
				put(Element.CREATED_JSON_FIELD, createdString);
			}
		};
		return skipNullParams(params);
	}
	
	@Override
	public String getItemType() {
		return ITEM_TYPE;
	}
	
	@Override
	public boolean isNote() {
		return true;
	}
}
