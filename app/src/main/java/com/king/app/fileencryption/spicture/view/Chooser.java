package com.king.app.fileencryption.spicture.view;

import java.util.List;

import android.view.View;

public interface Chooser {

	public void reInit();
	public void notifyAdapterRefresh();
	public void setVisibility(int visibility);
	public void updateList(List<String> fileList);
	public View getChildAt(int index);
	public void setOnChooseListener(Object listener);
	public void prepareRecycle();
}
