package com.opera.link.apilib.android.items;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;



public class FolderContext <T extends FolderEntry<?> > {
	protected static final String TRASH_FOLDER_TYPE = "trash";
	
	protected static final String TARGET_PROPERTY_KEY = "target";
	protected static final String TARGET_MINI = "mini";
	
	protected static final String MOVE_IS_COPY_FOLDER = "move_is_copy";
	protected static final String DELETABLE_FOLDER = "deletable";
	protected static final String ALLOWS_SUB_FOLDERS = "sub_folders_allowed";
	protected static final String ALLOWS_SEPARATORS = "separators_allowed";
	protected static final String MAX_ITEMS = "max_items";
	
	private ArrayList<T> children;
	
	/**
	 * @return {@code true} if the content of this folder is displayed on Opera Mini
	 */
	public boolean isTargetMini() {
		return isTargetMini;
	}

	
	/**
	 * @return {@code true} if moving to this folder results in copying the moved element
	 */
	public boolean moveIsCopy() {
		return moveIsCopy;
	}

	/**
	 * @return {@code true} if it is allowed to delete the content of this folder
	 */
	public boolean isDeletable() {
		return isDeletable;
	}

	/**
	 * @return {@code true} if it is allowed to add folders to the content of this folder
	 */
	public boolean allowsSubFolders() {
		return allowsSubFolders;
	}

	/**
	 * @return {@code true} if it is allowed to add separators to the content of this folder
	 */
	public boolean allowsSeparators() {
		return allowsSeparators;
	}

	/**
	 * Specifies maximal number of items that this folder can contain.
	 * If there is no such limit then returns 0.
	 */
	public int getMaxItems() {
		return maxItems;
	}

	private boolean isTargetMini = false;
	private boolean isTrash = false;
	private boolean moveIsCopy = false;
	private boolean isDeletable = true;
	private boolean allowsSubFolders = true;
	private boolean allowsSeparators = true;
	private int maxItems = 0; // when 0, then there is no limit on number of items
	
	public void setType(String type) {
		this.isTrash = TRASH_FOLDER_TYPE.equals(type); 
	}

	public ArrayList<T> getChildren() {
		return this.children;
	}

	public boolean isTrash() {
		return isTrash;
	}

	public void setChildren(ArrayList<T> children) {
		if (children == null) {
			this.children = new ArrayList<T>();
		} else {
			this.children = children;
		}
	}

	public void loadTargetFolderProperties(JSONObject properties) throws JSONException {
		if (properties == null || !properties.has(TARGET_PROPERTY_KEY)) {
			return;
		}
		if (!properties.getString(TARGET_PROPERTY_KEY).equals(TARGET_MINI)) {
			return;
		}
		
		isTargetMini = true;
		if (properties.has(MOVE_IS_COPY_FOLDER)) {
			moveIsCopy = properties.getBoolean(MOVE_IS_COPY_FOLDER);
		}
		if (properties.has(DELETABLE_FOLDER)) {
			isDeletable  = properties.getBoolean(DELETABLE_FOLDER);
		}
		if (properties.has(ALLOWS_SUB_FOLDERS)) {
			allowsSubFolders = properties.getBoolean(ALLOWS_SUB_FOLDERS);
		}
		if (properties.has(ALLOWS_SEPARATORS)) {
			allowsSeparators = properties.getBoolean(ALLOWS_SEPARATORS);
		}
		if (properties.has(MAX_ITEMS)) {
			maxItems = properties.getInt(MAX_ITEMS);
		}
	}

}
