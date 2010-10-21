package com.opera.link.apilib.android.items;


public abstract class BookmarkFolderEntry extends FolderEntry<BookmarkFolderEntry> {

	public static final String DATATYPE = "bookmark";
	

	@Override
	public String getDatatype() {
		return DATATYPE;
	}
	
	public boolean isBookmark() {
		return false;
	}
}
