package com.opera.link.apilib.android.items;


public abstract class FolderEntry <T extends FolderEntry<?> > extends Element {
	
	public boolean isFolder() {
		return false;
	}

}
