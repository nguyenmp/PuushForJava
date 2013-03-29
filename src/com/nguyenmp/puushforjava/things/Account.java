package com.nguyenmp.puushforjava.things;

import java.io.Serializable;


public class Account implements Serializable {
	private final String mUsername;
	private final String mCurrentSpace;
	private final String mMaximumSpace;
	private final String mPercentageSpace;
	private final String mAccountType;
	
	public Account(String username, String current, String maximum, String percent, String type) {
		mUsername = username;
		mCurrentSpace = current;
		mMaximumSpace = maximum;
		mPercentageSpace = percent;
		mAccountType = type;
	}
	
	public String getUsername() {
		return mUsername;
	}
	
	public String getCurrentSpace() {
		return mCurrentSpace;
	}
	
	public String getMaximumSpace() {
		return mMaximumSpace;
	}
	
	public String getPercentageSpace() {
		return mPercentageSpace;
	}
	
	public String getAccountType() {
		return mAccountType;
	}
}
