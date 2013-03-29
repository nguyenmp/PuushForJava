package com.nguyenmp.puushforjava.things;

import java.io.Serializable;
import java.net.URI;

public class Pool implements Serializable {
	private final String mID;
	private final URI mThumbnail;
	private final String mTitle;
	private final int mSize;
	
	public Pool(String title, String id, int size, URI thumbnail) {
		mID = id;
		mThumbnail = thumbnail;
		mTitle = title;
		mSize = size;
	}
	
	public String getID() {
		return mID;
	}
	
	public URI getThumbnail() {
		return mThumbnail;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public int getSize() {
		return mSize;
	}
}
