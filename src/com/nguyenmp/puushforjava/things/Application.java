package com.nguyenmp.puushforjava.things;

public class Application {
	private final String mURL;
	private final String mThumbnail;
	private final String mTitle;
	private final String mSummary;
	private final String mAPIEndpoint;
	private final String mInstructions;
	private final String mCustomURL;
	
	public Application(String url, String thumbnail, String title, String summary, String apiEndpoint, String instructions, String customUrl) {
		mURL = url;
		mThumbnail = thumbnail;
		mTitle = title;
		mSummary = summary;
		mAPIEndpoint = apiEndpoint;
		mInstructions = instructions;
		mCustomURL = customUrl;
	}
	
	public String getThumbnail() {
		return mThumbnail;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public String getSummary() {
		return mSummary;
	}
	
	public String getAPIEndpoint() {
		return mAPIEndpoint;
	}
	
	public String getInstructions() {
		return mInstructions;
	}
	
	public String getCustomUrl() {
		return mCustomURL;
	}
	
	public String getURL() {
		return mURL;
	}
}
