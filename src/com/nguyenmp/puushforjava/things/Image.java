package com.nguyenmp.puushforjava.things;

import java.io.Serializable;
import java.net.URI;

public class Image implements Serializable {
	private final URI mUrl;
	private final URI mThumbnail;
	private final String mTitle;
	private final String mViews;
	private final String mID;
	
	public Image(URI url, URI thumbnail, String title, String views, String id) {
		mUrl = url;
		mThumbnail = thumbnail;
		mTitle = title;
		mViews = views;
		mID = id;
	}
	
	public String getID() {
		return mID;
	}
	
	public URI getUrl() {
		return mUrl;
	}
	
	public URI getThumbnail() {
		return mThumbnail;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public String getViews() {
		return mViews;
	}
}
