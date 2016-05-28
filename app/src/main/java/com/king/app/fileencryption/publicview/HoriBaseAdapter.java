package com.king.app.fileencryption.publicview;

import android.widget.BaseAdapter;
import android.widget.LinearLayout;

public abstract class HoriBaseAdapter extends BaseAdapter {

	/**
	 * thread method
	 * this method should be set to synchronized, this method is working in thread, only refresh data
	 * it mustn't handle UI event
	 * @param pos1 left view position
	 * @param pos2 right view position
	 */
	public abstract void refreshData(int pos1, int pos2);

	/**
	 * normal method
	 * when refreshData method end, use this method to handle UI event
	 * @param container view container, use getChildAt to get view
	 * @param pos1 left view position
	 * @param pos2 right view position
	 */
	public abstract void onRefreshOver(LinearLayout container, int pos1, int pos2);
	
	/**
	 * normal method
	 * recycle specific range view resource
	 * @param container view container, use getChildAt to get view
	 * @param pos1 left view position
	 * @param pos2 right view position
	 */
	public abstract void recycle(LinearLayout container, int pos1, int pos2);
}
