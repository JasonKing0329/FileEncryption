package com.king.app.fileencryption.setting;

import android.content.Context;

public class SettingMemo {

	private static SettingMemo instance = null;
	
	private SettingMemo() {
		
	}
	
	public static SettingMemo getInstance() {
		if (instance == null) {
			instance = new SettingMemo();
		}
		return instance;
	}
	
	private boolean oldPageModeEnable;
	private int oldPageNumber;
	private int oldCascadeNumber;
	private boolean oldSlidingEnbale;
	private int oldSlidingMode;
	
	public void setOldPageModeEnable(boolean oldPageModeEnable) {
		this.oldPageModeEnable = oldPageModeEnable;
	}

	public void setOldPageNumber(int oldPageNumber) {
		this.oldPageNumber = oldPageNumber;
	}

	public void setOldCascadeNumber(int oldCascadeNumber) {
		this.oldCascadeNumber = oldCascadeNumber;
	}

	public void setOldSlidingEnbale(boolean oldSlidingEnbale) {
		this.oldSlidingEnbale = oldSlidingEnbale;
	}

	public void setOldSlidingMode(int oldSlidingMode) {
		this.oldSlidingMode = oldSlidingMode;
	}

	public boolean isSlidingEnableChanged(Context context) {
		boolean newEnable = SettingProperties.isMainViewSlidingEnable(context);
		return newEnable != oldSlidingEnbale;
	}
	public boolean isSlidingModeChanged(Context context) {
		int newMode = SettingProperties.getSlidingMenuMode(context);
		return newMode != oldSlidingMode;
	}
	public boolean isPageModeChanged(Context context) {
		boolean newEnable = SettingProperties.isPageModeEnable(context);
		return newEnable != oldPageModeEnable;
	}
	public boolean isPageNumberChanged(Context context) {
		int newNumber = SettingProperties.getSOrderPageNumber(context);
		return newNumber != oldPageNumber;
	}
	public boolean isCascadeCoverChanged(Context context) {
		int newNumber = SettingProperties.getCascadeCoverNumber(context);
		return newNumber != oldCascadeNumber;
	}
}
