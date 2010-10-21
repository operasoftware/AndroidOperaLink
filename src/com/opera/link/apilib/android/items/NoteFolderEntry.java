package com.opera.link.apilib.android.items;


public abstract class NoteFolderEntry extends FolderEntry<NoteFolderEntry> {
	

	public static final String DATATYPE = "note";  
	
	@Override
	public String getDatatype() {
		return DATATYPE;
	}
	
	public boolean isNote() {
		return false;
	}
	
}
