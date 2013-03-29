package com.nguyenmp.puushforjava.things;

import java.io.Serializable;
import java.net.URI;

public class DisplayedPool extends Pool implements Serializable {
	private final Image[] mImages;
	private final int mCurrentPage;
	private final int mMaxPage;
	
	public DisplayedPool(String title, String id, int size, URI thumbnail, int currentPage, int maxPage, Image[] images) {
		super(title, id, size, thumbnail);
		mImages = images;
		mCurrentPage = currentPage;
		mMaxPage = maxPage;
	}
	
	public DisplayedPool(Pool pool, int currentPage, int maxPage, Image[] images) {
		super(pool.getTitle(), pool.getID(), pool.getSize(), pool.getThumbnail());
		mImages = images;
		mCurrentPage = currentPage;
		mMaxPage = maxPage;
	}
	
	public Image[] getImages() {
		return mImages;
	}
	
	public int getCurrentPage() {
		return mCurrentPage;
	}
	
	public int getMaxPage() {
		return mMaxPage;
	}
}
