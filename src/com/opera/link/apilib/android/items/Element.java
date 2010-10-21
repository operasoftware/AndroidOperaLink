package com.opera.link.apilib.android.items;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Element {
	
	private String id = null;
	
	/**
	 * @return Returns unique ID of the element at the server, 
	 * if the element not added then returns null
	 */
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	protected static String ITEM_TYPE;
	
	protected static final String PROPERTIES_JSON_KEY = "properties";
	public static final String ITEM_TYPE_JSON_KEY = "item_type";
	protected static final String ID_JSON_KEY = "id";
	protected static final String CHILDREN_JSON_KEY = "children";
	
	protected static final String TITLE_JSON_FIELD = "title";
	protected static final String NICKNAME_JSON_FIELD = "nickname";
	protected static final String DESCRIPTION_JSON_FIELD = "description";
	protected static final String URI_JSON_FIELD = "uri";
	protected static final String CREATED_JSON_FIELD = "created";
	protected static final String VISITED_JSON_FIELD = "visited";
	protected static final String ICON_JSON_FIELD = "icon";
	protected static final String THUMBNAIL_JSON_FIELD = "thumbnail";
	protected static final String TARGET_JSON_FIELD = "target";
	protected static final String TYPE_JSON_FIELD = "type";
	protected static final String CONTENT_JSON_FIELD = "content";
	
	
	abstract void loadParameters(JSONObject json) throws JSONException;
	
	public abstract String getDatatype();
	public abstract String getItemType();

	protected static final SimpleDateFormat timezoneDateFormat = 
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	protected static final SimpleDateFormat localDateFormat = 
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	protected static <T extends FolderEntry<?>> void createChildren(JSONObject jsonElement, 
			FolderContext<T> folderContent, Class<T> cls) throws JSONException {
		
		if (jsonElement.has(Element.CHILDREN_JSON_KEY)) {
			folderContent.setChildren(
				elementsFactory(jsonElement.getJSONArray(Element.CHILDREN_JSON_KEY), cls)
				);
		} else {
			folderContent.setChildren(null);
		}
	}
		
	
	@SuppressWarnings("unchecked")
	public static <T extends Element> ArrayList<T> elementsFactory(JSONArray jsonList, Class<T> cls) throws JSONException {
		int size = jsonList.length();
		ArrayList<T> elements = new ArrayList<T>(size);
		
		for (int i = 0; i < size; i++) {
			
			JSONObject jsonElement = (JSONObject) jsonList.get(i);
			T element = null;
			
			// create object according to the item_type
			String item_type = jsonElement.getString(Element.ITEM_TYPE_JSON_KEY);
			if (item_type.equals(Bookmark.ITEM_TYPE)) {
				element = (T) new Bookmark();
			}
			if (item_type.equals(BookmarkFolder.ITEM_TYPE)) {
				BookmarkFolder bookmarkFolder = new BookmarkFolder();
				element = (T) bookmarkFolder;
				// read folder content
				createChildren(jsonElement, bookmarkFolder.folder, BookmarkFolderEntry.class);
			}
			if (item_type.equals(BookmarkSeparator.ITEM_TYPE)) {
				element = (T) new BookmarkSeparator();
			}
			if (item_type.equals(Note.ITEM_TYPE)) {
				element = (T) new Note();
			}
			if (item_type.equals(NoteFolder.ITEM_TYPE)) {
				NoteFolder noteFolder = new NoteFolder();
				element = (T) noteFolder;
				// read folder content
				createChildren(jsonElement, noteFolder.folder, NoteFolderEntry.class);
			}
			if (item_type.equals(NoteSeparator.ITEM_TYPE)) {
				element = (T) new NoteSeparator();
			}
			if (item_type.equals(SpeedDial.ITEM_TYPE)) {
				element = (T) new SpeedDial();
			}
			
			// set properties of new created object
			elements.add(element);

			element.updateParameters(jsonElement);
		}
		
		return elements;
	}
	
	public void updateParameters(JSONObject jsonElement) throws JSONException {
		this.id = jsonElement.getString(ID_JSON_KEY); 
		this.loadParameters(jsonElement.getJSONObject(Element.PROPERTIES_JSON_KEY));
	}
	
	public Date parseDate(String dateString) {
		try {
			return timezoneDateFormat.parse(dateString);
		} catch (ParseException e) {
			try { // try parse without a time zone
				return localDateFormat.parse(dateString);
			} catch (ParseException e1) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public String dateToString(Date date) {
		if (date != null) {
			return timezoneDateFormat.format(date);
		}
		return null;
	}

	public abstract HashMap<String, String> createParamsDict();

	protected HashMap<String, String> skipNullParams(
			HashMap<String, String> params) {
		HashMap<String, String> filteredParams = new HashMap<String, String>();
		for (Entry<String, String> paramEntry : params.entrySet()) {
			if (paramEntry.getValue() != null) {
				filteredParams.put(paramEntry.getKey(), paramEntry.getValue());
			}
		}
		return filteredParams;
	}
}
