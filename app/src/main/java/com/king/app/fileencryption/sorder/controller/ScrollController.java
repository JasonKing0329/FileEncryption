package com.king.app.fileencryption.sorder.controller;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.view.SOrderGridAdapter;
import com.king.app.fileencryption.sorder.view.SOrderGridAdapter.CoverMode;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class ScrollController implements OnScrollListener {

	private final String TAG = "ScrollController";
	private final String TAG1 = "OnScroll";
	private SOrderGridAdapter gridAdapter;

	private final int CACHE_NUMBER = 28;//12(屏幕上显示最多包含的数量) + SCROLL_NUMBER*2 + SPACE_NUMBER*2
	private final int SCROLL_NUMBER = 6;//the number each request should load
	private final int SPACE_NUMBER = 2;//distance between critical point and cacheEndIndex

	private boolean isFirst;
	private List<List<Bitmap>> wholeCoverList;

	private int cacheStartIndex, cacheEndIndex;//cache region. to control bitmaps' number in storage
	private int lastVisiblePosition;//to judge scroll direction

	private int multiNum_cascade;//number of cascade cover's bitmap
	private int multiNum = 4;
	private SOrderPictureBridge bridge;
	private Bitmap defaultCover;
	private Bitmap defaultItemImage;
	private Context mContext;

	public ScrollController(Context context) {

		mContext = context;
		isFirst = true;
		bridge = SOrderPictureBridge.getInstance(context);
		multiNum_cascade = SettingProperties.getCascadeCoverNumber(context);
		defaultCover = PictureManagerUpdate.getInstance().getDefaultOrderCover(context);
		defaultItemImage = PictureManagerUpdate.getInstance().getUnavailableItemImage(context);
	}

	public void reloadCascadeNum(Context context) {
		multiNum_cascade = SettingProperties.getCascadeCoverNumber(context);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
			case SCROLL_STATE_FLING:
				Log.d(TAG, "onScrollStateChanged SCROLL_STATE_FLING");
				break;
			case SCROLL_STATE_IDLE:
				Log.d(TAG, "onScrollStateChanged SCROLL_STATE_IDLE");
				break;
			case SCROLL_STATE_TOUCH_SCROLL:
				lastVisiblePosition = view.getLastVisiblePosition();
				Log.d(TAG, "onScrollStateChanged SCROLL_STATE_TOUCH_SCROLL");
				Log.d(TAG, "onScrollStateChanged lastVisiblePosition = " + lastVisiblePosition);
				break;

			default:
				Log.d(TAG, "onScrollStateChanged default");
				break;
		}
	}

	@Override
	/**
	 * cache strategy:
	 * keep cacheNumber item in current
	 * scroll scrollNumber item, recycle scrollNumber item, load new scrollNumber item
	 */
	public void onScroll(AbsListView view, int firstVisibleItem,
						 int visibleItemCount, int totalItemCount) {
		Log.i(TAG1, "onScroll");
		if (gridAdapter == null) {//gridview发起 onScroll信息先于notifyupdate，所以在SOrderPage中是在notifyupdate中才初始化gridAdapter的
			return;
		}
		if (isFirst) {
			Log.i(TAG, "onScroll isFirst");
			isFirst = false;
			loadCover(0, CACHE_NUMBER - 1);
			setCacheRange(0, CACHE_NUMBER - 1);
		}
		else {
			int pageStart = firstVisibleItem;
			int pageEnd = firstVisibleItem + visibleItemCount - 1;

			int dir = pageEnd - lastVisiblePosition;
			if (arrivedEdge(pageStart, pageEnd, dir)) {
				int newCacheStartIndex = 0, newCacheEndIndex = 0;
				if (dir < 0) {// scroll up
					Log.i(TAG, "onScroll scroll up");
					int realNumber = loadCover(cacheStartIndex - 1 - SCROLL_NUMBER, cacheStartIndex - 1);
					if (realNumber > 0) {
						recycleCover(cacheEndIndex - (realNumber - 1), cacheEndIndex);
					}
					newCacheStartIndex = cacheStartIndex - realNumber;
					newCacheEndIndex = cacheEndIndex - realNumber;
					setCacheRange(newCacheStartIndex, newCacheEndIndex);
				}
				else if (dir > 0) {// scroll down
					Log.i(TAG, "onScroll scroll down");
					int realNumber = loadCover(cacheEndIndex + 1, cacheEndIndex + 1 + SCROLL_NUMBER);
					if (realNumber > 0) {
						recycleCover(cacheStartIndex, cacheStartIndex + (realNumber - 1));
					}
					newCacheStartIndex = cacheStartIndex + realNumber;
					newCacheEndIndex = cacheEndIndex + realNumber;
					setCacheRange(newCacheStartIndex, newCacheEndIndex);
				}
			}
		}
	}

	/**
	 * check the critical edge of loading&recycle covers
	 * @param pageStart index of first visible item in current page
	 * @param pageEnd index of last visible item in current page
	 * @param dir scroll direction
	 * @return if catch to load edge return true, otherwise false
	 */
	private boolean arrivedEdge(int pageStart, int pageEnd, int dir) {
		Log.i(TAG1, "arrivedEdge pageStart=" + pageStart + ", pageEnd" + pageEnd + ",dir = " + dir);
		boolean result = false;
		if (dir > 0) {// scroll down
			if (pageEnd >= cacheEndIndex - SPACE_NUMBER && cacheEndIndex < wholeCoverList.size() - 1) {
				result = true;
			}
		}
		else if (dir < 0) {// scroll up
			if (pageStart <= cacheStartIndex + SPACE_NUMBER && cacheStartIndex > 0) {
				result = true;
			}
		}
		return result;
	}

	/**
	 *
	 * @param start start index in wholeCoverList
	 * @param end end index in wholeCoverList
	 * @return real loaded number
	 */
	private int loadCover(int start, int end) {

		Log.i(TAG, "loadCover (" + start + ", " + end + ")");
		List<SOrder> sOrders = gridAdapter.getSorderList();
		if (sOrders == null) {
			return 0;
		}
		if (start > end) {
			return 0;
		}
		if (start < 0) {
			start = 0;
		}
		if (end > sOrders.size() - 1) {
			end = sOrders.size() - 1;
		}
		Log.i(TAG, "loadCover actually(" + start + ", " + end + ")");

		new LoadCoverTask(start, end, sOrders, gridAdapter.getCoverMode()).execute();
		return end - start + 1;
	}

	private void recycleCover(int start, int end) {
		Log.i(TAG, "recycleCover (" + start + ", " + end + ")");
		List<SOrder> sOrders = gridAdapter.getSorderList();
		if (sOrders == null) {
			return;
		}
		if (start > end) {
			return;
		}
		if (start < 0) {
			start = 0;
		}
		if (end > sOrders.size() - 1) {
			end = sOrders.size() - 1;
		}
		Log.i(TAG, "recycleCover actually(" + start + ", " + end + ")");
		List<Bitmap> list = null;
		for (int i = start; i <= end; i ++) {
			list = wholeCoverList.get(i);
			if (list != null) {
				for (Bitmap bitmap:list) {
					if (bitmap != defaultCover && bitmap != defaultItemImage) {//default cover is referred by many items
						bitmap.recycle();
					}
				}
				list.clear();
				list = null;
			}
		}
	}

	/**
	 * update cache range, generally keep the range total as 
	 * @param start
	 * @param end
	 */
	private void setCacheRange(int start, int end) {
		Log.i(TAG, "setCacheRange (" + start + ", " + end + ")");
		List<SOrder> sOrders = gridAdapter.getSorderList();
		if (sOrders == null) {
			return;
		}
		if (start < 0) {
			start = 0;
		}
		if (end > sOrders.size() - 1) {
			end = sOrders.size() - 1;
		}
		Log.i(TAG, "setCacheRange actually(" + start + ", " + end + ")");
		cacheStartIndex = start;
		cacheEndIndex = end;
	}

	public void setGridAdapter(SOrderGridAdapter gridAdapter) {
		Log.i(TAG, "setGridAdapter");
		this.gridAdapter = gridAdapter;
	}

	/**
	 * call this before adapter notifyDataSetChanged
	 */
	public void notifyCoverDataChanged() {
		Log.i(TAG, "notifyCoverDataChanged");
		if (wholeCoverList == null) {
			wholeCoverList = new ArrayList<List<Bitmap>>();
		}
		else {
			recycleCover(0, wholeCoverList.size() - 1);
			wholeCoverList.clear();
		}

		int totalSize = gridAdapter.getCount();
		for (int i = 0; i < totalSize; i ++) {
			wholeCoverList.add(null);
		}
		gridAdapter.setMultiCoverList(wholeCoverList);
		if (cacheEndIndex > totalSize - 1) {
			cacheEndIndex = totalSize - 1;
		}
		if (cacheStartIndex > totalSize - 1) {
			cacheStartIndex = 0;
		}
		isFirst = true;
	}

	private class LoadCoverTask extends AsyncTask<Object, Object, Object> {

		private int start;
		private int end;
		private List<SOrder> sorderList;
		private CoverMode mCoverMode;
		public LoadCoverTask(int start, int end, List<SOrder> list, CoverMode coverMode) {
			this.start = start;
			this.end = end;
			this.sorderList = list;
			mCoverMode = coverMode;
		}

		@Override
		protected Object doInBackground(Object... params) {

			SOrder sOrder = null;
			List<Bitmap> bitmaps = null;
			for (int i = start; i <= end; i ++) {
				sOrder = sorderList.get(i);
				bitmaps = null;
				if (mCoverMode == CoverMode.SINGLE) {
					bitmaps = new ArrayList<Bitmap>();
					Bitmap bitmap = PictureManagerUpdate.getInstance().createSingleOrderCover(sOrder.getCoverPath(), mContext);
					bitmaps.add(bitmap);
				}
				else {
					List<String> list = null;
					if (mCoverMode == CoverMode.CASCADE) {
						list = bridge.loadSOrderCovers(sOrder, multiNum_cascade);
					}
					else {
						list = bridge.loadSOrderCovers(sOrder, multiNum);
					}
					if (list != null) {
						bitmaps = new ArrayList<Bitmap>();
						Bitmap bitmap = null;
						for (String path:list) {
							bitmap = PictureManagerUpdate.getInstance().createCascadeOrderCover(path, mContext);
							bitmaps.add(bitmap);
						}
					}
				}

				wholeCoverList.set(i, bitmaps);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
		}

		@Override
		protected void onPostExecute(Object value) {
			gridAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onPreExecute() {
			//coverView.clearBitmaps();
			super.onPreExecute();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

	}
}
