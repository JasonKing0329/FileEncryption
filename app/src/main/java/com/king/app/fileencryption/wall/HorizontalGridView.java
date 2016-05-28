package com.king.app.fileencryption.wall;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class HorizontalGridView extends HorizontalScrollView {

	public interface OnScrollListener {
		public void onScroll(int position, int direction);
		public void onScrollRange(int first, int last, int direction);
	}

	private final String TAG = "HorizontalGridView";
	private final int DEFAULT_COLUMN = 6;

	private LinearLayout container;
	private List<LinearLayout> colList;

	/**
	 * 用该boolean list记录当前缓冲区域，也是用该list进行scroll的边界判断
	 */
	private List<Boolean> colVisibleList;
	private int firstVisibleCol;
	private int lastVisibleCol;

	private int row;
	private int width;
	private int height;
	private int itemWidth;

	private BaseAdapter adapter;

	private OnScrollListener onScrollListener;

	public HorizontalGridView(Context context) {
		super(context);
		init();
	}

	public HorizontalGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HorizontalGridView(Context context, AttributeSet attrs,
							  int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		container = new LinearLayout(getContext());
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		container.setLayoutParams(params);
		container.setOrientation(LinearLayout.HORIZONTAL);
		addView(container);

		colList = new ArrayList<LinearLayout>();
		colVisibleList = new ArrayList<Boolean>();
	}

	public void reset() {
		firstVisibleCol = 0;
		lastVisibleCol = 0;
		colList.clear();
		colVisibleList.clear();
		container.removeAllViews();
	}

	public void setOnScrollListener(OnScrollListener listener) {
		onScrollListener = listener;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getRow() {
		return row;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setItemWidth(int width) {
		this.itemWidth = width;
	}

	public int getItemWidth() {
		if (colList.size() > 0) {
			itemWidth = colList.get(0).getWidth();
		}
		return itemWidth;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d(TAG, "onLayout");
		super.onLayout(changed, l, t, r, b);
	}

	public void setAdapter(BaseAdapter adapter) {
		this.adapter = adapter;
		if (adapter != null && adapter.getCount() > 0) {
			View view = adapter.getView(0, null, null);

			firstVisibleCol = 0;
			int end = 0;
			if (view != null) {
				if (itemWidth == 0) {
					lastVisibleCol = DEFAULT_COLUMN - 1;
					end = row * DEFAULT_COLUMN - 1;
				}
				else {
					lastVisibleCol = width / itemWidth + 2 - 1;
					end = (width / itemWidth + 2) * row - 1;

				}

				if (end > adapter.getCount() - 1) {
					end = adapter.getCount() - 1;
					lastVisibleCol = end / row;
				}
			}

			initArea(0, end);

			//后面判断scroll load需要使用itemWidth的具体值
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					if (colList.size() > 0) {
						itemWidth = colList.get(0).getWidth();
						Log.d(TAG, "itemWidth=" + itemWidth);
					}
				}
			}, 100);
		}
	}

	/**
	 *
	 * @param start view position
	 * @param end view position
	 * @return column actually init
	 */
	private int initArea(int start, int end) {
		Log.d(TAG, "initArea [" + start + "," + end + "]");
		int column = 0;
		if (adapter != null) {
			LinearLayout colLayout = null;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
			LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, 0);
			subParams.weight = 1;

			if (start < 0) {
				start = 0;
			}
			for (int i = start; i <= end && i < adapter.getCount(); i ++) {
				if (i / row < colList.size()) {
					colLayout = colList.get(i / row);
					colVisibleList.set(i / row, true);
				}
				else {
					colLayout = null;
				}

				if (colLayout == null) {
					colLayout = new LinearLayout(getContext());
					colLayout.setLayoutParams(params);
					colLayout.setGravity(Gravity.CENTER);
					colLayout.setOrientation(LinearLayout.VERTICAL);
					container.addView(colLayout);
					colList.add(colLayout);
					colVisibleList.add(true);
				}

				if (i % row == 0) {//第一行
					column ++;
					colLayout.removeAllViews();
				}
				View view = adapter.getView(i, null, null);
				colLayout.addView(view, subParams);
			}
		}
		return column;
	}

	@Override
	public void onScrollChanged(int x, int y, int oldx, int oldy) {
		onScrollChanged(x, x - oldx);
		if (onScrollListener != null) {
			onScrollListener.onScroll(x, x - oldx);
		}
	}

	private void onScrollChanged(int position, int direction) {
		if (direction > 0) {
			if (needReload(position, direction)) {
				Log.d(TAG, "onScrollChanged " + position);

				colVisibleList.set(firstVisibleCol, false);

				firstVisibleCol ++;
				int start = (lastVisibleCol + 1) * row;
				int end = start + row - 1;
				int column = initArea(start, end);
				lastVisibleCol += column;

				if (onScrollListener != null) {
					onScrollListener.onScrollRange(getFirstVisibleIndex(), getLastVisibleIndex(), direction);
					//onScrollListener.onScrollRange(firstVisibleCol * row, lastVisibleCol * row + row - 1, direction);
				}
			}
		}
		else if (direction < 0) {
			if (needReload(position, direction)) {
				Log.d(TAG, "onScrollChanged " + position);

				colVisibleList.set(lastVisibleCol, false);

				lastVisibleCol --;

				int start = (firstVisibleCol - 1) * row;
				int end = start + row - 1;
				int column = initArea(start, end);
				firstVisibleCol -= column;

				if (onScrollListener != null) {
					onScrollListener.onScrollRange(getFirstVisibleIndex(), getLastVisibleIndex(), direction);
					//onScrollListener.onScrollRange(firstVisibleCol * row, lastVisibleCol * row + row - 1, direction);
				}
			}
		}
	}

	public int getFirstVisibleIndex() {
		if (itemWidth > 0) {
			return getScrollX() / itemWidth * row;
		}
		return 0;
	}
	public int getLastVisibleIndex() {
		if (itemWidth > 0) {
			return (getScrollX() + width) / itemWidth * row + row - 1;
		}
		return 0;
	}
	/**
	 * 通过列的boolean list进行边界判断，当划过一列之后，就在尾列/首列重新加载另一行
	 * @param position
	 * @param direction
	 * @return
	 */
	private boolean needReload(int position, int direction) {

		if (direction > 0) {
			int visibleCol = position / itemWidth;
			if (visibleCol == 1) {
				if (colVisibleList.get(0)) {
					return true;
				}
			}
			else if (visibleCol > 1) {
				boolean last = colVisibleList.get(visibleCol - 1);
				boolean llast = colVisibleList.get(visibleCol - 2);
				if (last && !llast) {
					return true;
				}
			}
		}
		else if (direction < 0) {
			int visibleCol = (position + width) / itemWidth;
			if (visibleCol == colVisibleList.size() - 2) {
				if (colVisibleList.get(colVisibleList.size() - 1)) {
					return true;
				}
			}
			else if (visibleCol < colVisibleList.size() - 2) {
				boolean last = colVisibleList.get(visibleCol + 1);
				boolean llast = colVisibleList.get(visibleCol + 2);
				if (last && !llast) {
					return true;
				}
			}
		}
		return false;
	}

	public void notifyDataSetChanged() {
		Log.d(TAG, "notifyDataSetChanged firstVisibleCol=" + firstVisibleCol + " lastVisibleCol=" + lastVisibleCol);
		initArea(firstVisibleCol * row, lastVisibleCol * row + row - 1);
	}

}
