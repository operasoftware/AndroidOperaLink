package com.opera.link.apilib.android.items;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class NoteFolder extends NoteFolderEntry implements FolderInterface<NoteFolderEntry> {

	protected static final String ITEM_TYPE = "note_folder";
	
	protected FolderContext<NoteFolderEntry> folder = new FolderContext<NoteFolderEntry>();
	
	public String title;

	public String target;
	
	protected NoteFolder() {
		
	}
	
	public NoteFolder(String title) {
		this();
		this.title = title;
	}

	@Override
	public boolean isFolder() {
		return true;
	}
	

	@Override
	void loadParameters(JSONObject json) throws JSONException {
		folder.loadTargetFolderProperties(json);
		
		if (json.has(Element.TITLE_JSON_FIELD)) {
			title = json.getString(Element.TITLE_JSON_FIELD);
		}
		if (json.has(Element.TARGET_JSON_FIELD)) {
			target = json.getString(Element.TARGET_JSON_FIELD);
		}
		if (json.has(Element.TYPE_JSON_FIELD)) {
			this.folder.setType(json.getString(Element.TYPE_JSON_FIELD));
		}
	}

	@Override
	public HashMap<String, String> createParamsDict() {
		HashMap<String, String> params = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put(Element.TITLE_JSON_FIELD, title);
			}
		};
		return skipNullParams(params);
	}
	
	@Override
	public String getItemType() {
		return ITEM_TYPE;
	}

	public ArrayList<NoteFolderEntry> getChildren() {
		return this.folder.getChildren();
	}

	public FolderContext<NoteFolderEntry> getFolderContext() {
		return this.folder;
	}

	public boolean isTrash() {
		return this.folder.isTrash();
	}

}
