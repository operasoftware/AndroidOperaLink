package com.opera.link.apilib.android.items;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class NoteSeparator extends NoteFolderEntry {

	public static final String ITEM_TYPE = "note_separator";
	
	public NoteSeparator() {

	}

	@Override
	void loadParameters(JSONObject json) throws JSONException {

	}

	@Override
	public HashMap<String, String> createParamsDict() {
		return new HashMap<String, String>();
	}
	
	@Override
	public String getItemType() {
		return ITEM_TYPE;
	}
}
