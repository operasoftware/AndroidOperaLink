package com.opera.link.apilib.android.items;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class BookmarkFolder extends BookmarkFolderEntry implements FolderInterface<BookmarkFolderEntry> {

	protected static final String ITEM_TYPE = "bookmark_folder"; 
	
	protected FolderContext<BookmarkFolderEntry> folder = new FolderContext<BookmarkFolderEntry>();
	
	public String title;

	public String nickname;
	public String description;
	public String target;
	
	protected BookmarkFolder() {

	}

	public BookmarkFolder(String title) {
		this();
		this.title = title;
	}	
	
	@Override
	public boolean isFolder() {
		return true;
	}
	
	public ArrayList<BookmarkFolderEntry> getChildren() {
		if (folder != null) {
			return this.folder.getChildren();
		}
		return null;
	}

	
	@Override
	void loadParameters(JSONObject json) throws JSONException {
		folder.loadTargetFolderProperties(json);
		
		if (json.has(Element.TITLE_JSON_FIELD)) {
			title = json.getString(Element.TITLE_JSON_FIELD);
		}
		if (json.has(Element.NICKNAME_JSON_FIELD)) {
			nickname = json.getString(Element.NICKNAME_JSON_FIELD);
		}
		if (json.has(Element.DESCRIPTION_JSON_FIELD)) {
			description = json.getString(Element.DESCRIPTION_JSON_FIELD);
		}
		if (json.has(Element.TARGET_JSON_FIELD)) {
			target = json.getString(Element.TARGET_JSON_FIELD);
		}
		if (json.has(Element.TYPE_JSON_FIELD)) {
			folder.setType(json.getString(Element.TYPE_JSON_FIELD));
		}

	}

	@Override
	public HashMap<String, String> createParamsDict() {
		HashMap<String, String> params = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put(Element.TITLE_JSON_FIELD, title);
				put(Element.NICKNAME_JSON_FIELD, nickname);
				put(Element.DESCRIPTION_JSON_FIELD, description);
			}
		};
		return skipNullParams(params);
	}
	
	@Override
	public String getItemType() {
		return ITEM_TYPE;
	}

	public FolderContext<BookmarkFolderEntry> getFolderContext() {
		return this.folder;
	}

	public boolean isTrash() {
		return this.folder.isTrash();
	}

}
