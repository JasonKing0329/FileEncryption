package com.king.app.fileencryption.publicview;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.util.ScreenUtils;

import android.content.Context;
import android.os.Handler.Callback;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/**
 * extends from HorizontalScrollView
 * there is recycle event when scroll
 * accept HoriBaseAdapter
 * @author Jing
 *
 */
public class HorizontalGallery extends HorizontalScrollView {

	public interface OnItemSelectListener {
		public void onGalleryItemClick(View view, int position);
		public void onGalleryItemLongClick(View view, int position);
		public void onGalleryItemSelectStatus(View view, int position);
	}

	private final String TAG = "HorizontalGallery";
	private final String TAG_SCROLL = "Scroll";
	private final int INIT_ITEM = 20;

	private OnItemSelectListener listener;
	private LinearLayout container;
	private HoriBaseAdapter adapter;

	private int selectedIndex;
	private int screenWidth;

	/**
	 * 外部执行scrollToxx方法时，会触发onScrollChanged，这在view resource的cache政策下会造成冲突
	 * 因此用该变量控制，当外部执行scrollToxx方法时，该值为true。onScrollChanged执行一次后即设置为false.
	 * (scrollTo 只会触发onScrollChanged执行一次)
	 */
	private boolean isForceControl;

	/**
	 * 用来判断scroll方向
	 */
	private int scrollPosition;
	/**
	 * 一次offset=cacheNumber*itemWidth的滑动距离则为一次有效滑动，刷新cacheNumber个item，同时回收cacheNumber个item
	 */
	private int itemWidth = -1;
	private int cacheNumber;
	/**
	 * 防止itemWidth为0
	 */
	private int setItemWidth;

	/**
	 * cache区域的scroll position边界
	 */
	private int cacheStart, cacheEnd;

	private boolean hasLoadViewParams;

	public HorizontalGallery(Context context) {
		super(context);
		init();
	}

	public HorizontalGallery(Context context, AttributeSet attr) {
		super(context, attr);
		init();
	}

	private void init() {
		screenWidth = ScreenUtils.getScreenWidth(getContext());
		cacheStart = 0;
		cacheEnd = -1;//itemWidth didn't be calculated until view has displayed in screen
	}

	public void setOnItemSelectListener(OnItemSelectListener listener) {
		this.listener = listener;
	}

	public void setAdapter(HoriBaseAdapter adapter) {
		this.adapter = adapter;
		if (container == null) {
			container = new LinearLayout(getContext());
			android.view.ViewGroup.LayoutParams params = new LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			container.setLayoutParams(params);
			container.setOrientation(LinearLayout.HORIZONTAL);
			addView(container);
		}
		else {
			if (container.getChildCount() > 0) {
				container.removeAllViews();
			}
		}

		for (int i = 0; i < adapter.getCount(); i++) {
			View v = adapter.getView(i, null, null);

			// 为视图设定点击监听器
			final int position = i;
			final View view = v;
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					selectedIndex = position;
					if (listener != null) {
						listener.onGalleryItemClick(view, position);
						listener.onGalleryItemSelectStatus(view, position);
					}
				}
			});
			v.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if (listener != null) {
						listener.onGalleryItemLongClick(view, position);
					}
					return true;
				}
			});
			container.addView(view);
		}

		new UpdateThread(0, INIT_ITEM - 1).start();

//itemWidth始终是0
//        new Handler().postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				loadViewParams();
//				cacheStart = 0;
//				cacheEnd = cacheNumber * 2 * itemWidth;
//				HorizontalGallery.this.adapter.refresh(container, 0, cacheNumber * 2);
//			}
//		}, 200);
	}

	public void scrollToNext() {
		loadViewParams();
		if (container != null && container.getChildCount() > 0) {
			selectedIndex ++;
			if (selectedIndex >= container.getChildCount()) {
				selectedIndex = 0;

				int recycleStart = getItemIndexFromPosition(cacheStart);
				int recycleEnd = getItemIndexFromPosition(cacheEnd);
				int start = 0;
				int end = INIT_ITEM - 1;
				updateAndRecycle(start, end, recycleStart, recycleEnd);
				cacheStart = start * itemWidth;
				cacheEnd = (end + 1) * itemWidth;
				isForceControl = true;
			}
			scrollToPos(selectedIndex);
		}
	}

	public void scrollToPrevious() {
		loadViewParams();
		if (container != null && container.getChildCount() > 0) {
			selectedIndex --;
			if (selectedIndex < 0) {
				selectedIndex = container.getChildCount() - 1;

				int recycleStart = getItemIndexFromPosition(cacheStart);
				int recycleEnd = getItemIndexFromPosition(cacheEnd);
				int start = container.getChildCount() - 1 - INIT_ITEM;
				int end = container.getChildCount() - 1;
				updateAndRecycle(start, end, recycleStart, recycleEnd);
				cacheStart = start * itemWidth;
				cacheEnd = (end + 1) * itemWidth;
				isForceControl = true;
			}
			scrollToPos(selectedIndex);
		}
	}

	/**
	 * design for scrollToNext & scrollToPrevious.
	 * compared with below 'public void scrollToPosition', their view resource cache strategy is different
	 * place selected view at center in horizontal(except for most-left and most-right views)
	 * @param index
	 */
	private void scrollToPos(int index) {
		int centerPos = (screenWidth - itemWidth) / 2;
		int viewPos = index * itemWidth;
		if (viewPos > centerPos) {
			scrollPosition = viewPos - centerPos;
			scrollTo(scrollPosition, 0);
		}
		else {
			scrollPosition = 0;
			scrollTo(0, 0);
		}
		if (listener != null) {
			listener.onGalleryItemSelectStatus(container.getChildAt(index), index);
		}
	}

	/**
	 * interface to the outside.
	 * place selected view at center in horizontal(except for most-left and most-right views)
	 * @param index
	 */
	public void scrollToPosition(int position) {
		loadViewParams();
		int centerPos = (screenWidth - itemWidth) / 2;
		int viewPos = position * itemWidth;

		if (viewPos > centerPos) {
			scrollPosition = viewPos - centerPos;
		}
		else {
			scrollPosition = 0;
		}

		/*******************************update and recycle***********************************/
		int itemIndex = getItemIndexFromPosition(scrollPosition);
		int start = itemIndex - INIT_ITEM/2;
		int end = itemIndex + INIT_ITEM/2;
		if (container.getChildCount() <= INIT_ITEM) {
			start = 0;
			end = container.getChildCount() - 1;
		}
		else {
			if (start < 0) {
				start = 0;
				end = INIT_ITEM - 1;
			}
			else if (end > container.getChildCount() - 1) {
				end = container.getChildCount() - 1;
				start = end - INIT_ITEM + 1;
			}
		}
		int recycleStart = getItemIndexFromPosition(cacheStart);
		int recycleEnd = getItemIndexFromPosition(cacheEnd);
		updateAndRecycle(start, end, recycleStart, recycleEnd);
		cacheStart = start * itemWidth;
		cacheEnd = (end + 1) * itemWidth;
		isForceControl = true;
		/******************************************************************/

		scrollTo(scrollPosition, 0);
		if (listener != null) {
			listener.onGalleryItemSelectStatus(container.getChildAt(position), position);
		}
	}

	public void setItemWidth(int width) {
		setItemWidth = width;
	}

	/**
	 * call this after view has been displayed in screen
	 */
	private void loadViewParams() {
		if (!hasLoadViewParams) {
			if (container != null) {
				itemWidth = container.getChildAt(0).getWidth();//if gallery is invisible, itemWidth will be 0
				if (itemWidth == 0) {
					itemWidth = setItemWidth;
				}
				cacheNumber = screenWidth / itemWidth + 1;

				/**
				 * as itemWidth is calculated after view display on screen
				 * so cacheEnd is -1 before that
				 */
				cacheEnd = itemWidth * INIT_ITEM;
				Log.d(TAG, "loadViewParams itemWidth = " + itemWidth + " cacheNumber = " + cacheNumber);
			}
			hasLoadViewParams = true;
		}
	}

// 用ACTION_MOVE监听只能是手touch在屏幕上的过程，由于scrollview在手滑动并离开屏幕后还会滑动一段距离，所以后面的距离在ontouch中是无法判断的
// 还是应该用onScrollChanged来监听
//	@Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//        case MotionEvent.ACTION_DOWN:
//        	break;
//        case MotionEvent.ACTION_MOVE:
//        	int newPos = getScrollX();
//        	int direction = newPos - scrollPosition;
//            onScrollChanged(scrollPosition, direction);
//        	scrollPosition = newPos;
//            break;
//        }
//        return super.onTouchEvent(ev);
//    }

	@Override
	public void onScrollChanged(int x, int y, int oldx, int oldy) {
		if (!isForceControl) {
			onScrollChanged(x, x - oldx);
		}
		scrollPosition = x;
		isForceControl = false;
	}

	private void onScrollChanged(int position, int direction) {
		loadViewParams();
		arrivedCacheRange(position, direction);
	}

	private void arrivedCacheRange(int position, int direction) {
		int left = cacheStart;
		int right = cacheEnd == -1 ? INIT_ITEM * itemWidth : cacheEnd;
		if (Application.DEBUG) {
			Log.d(TAG_SCROLL, "arrivedCacheRange cacheStart = " + cacheStart + " cacheEnd = " + cacheEnd);
		}
		if (direction < 0) {//scroll to left
			int offset = itemWidth * cacheNumber;
			if (offset <= left) {//判断左边的view数量足够加载cacheNumber个数量

				//达到加载边界
				if (position <= left + offset) {
					left -= offset;
					right -= offset;
					if (Application.DEBUG) {
						Log.d(TAG, "arrivedCacheRange left, newleft = " + left + " newright = " + right);
						Log.d(TAG, "current scrollx=" + getScrollX() + " itemIndex=" + getItemIndexFromPosition(getScrollX()));
					}
					updateAndRecycle(getItemIndexFromPosition(left), getItemIndexFromPosition(right)
							, getItemIndexFromPosition(right) + 1, getItemIndexFromPosition(cacheEnd));
					cacheStart = left;
					cacheEnd = right;
				}

				//当出现左边不足cacheNumber数量情况，保证只加载一次
				if (cacheStart < offset) {
					int realNumber = cacheStart / itemWidth;
					if (realNumber > 0) {
						int loadIndex = 0;
						int recycleIndex = getItemIndexFromPosition(cacheEnd);
						Log.d(TAG, "totalWidth - cacheEnd < offset realNumber =" + realNumber + " loadIndex" + loadIndex
								+ " recycleIndex" + recycleIndex);

						updateAndRecycle(loadIndex, loadIndex + realNumber - 1
								, recycleIndex - realNumber + 1, recycleIndex);
					}
				}
			}
		}
		else if (direction > 0) {//scroll to right
			int offset = itemWidth * cacheNumber;
			int totalWidth = container.getChildCount() * itemWidth;
			if (offset <= totalWidth - cacheEnd) {//判断右边的view数量足够加载cacheNumber个数量

				//达到加载边界
				if (position + screenWidth >= right - offset) {
					left += offset;
					right += offset;
					if (Application.DEBUG) {
						Log.d(TAG, "arrivedCacheRange right, newleft = " + left + " newright = " + right);
						Log.d(TAG, "current scrollx=" + getScrollX() + " itemIndex=" + getItemIndexFromPosition(getScrollX()));
					}
					updateAndRecycle(getItemIndexFromPosition(left), getItemIndexFromPosition(right)
							, getItemIndexFromPosition(cacheStart), getItemIndexFromPosition(left) - 1);
					cacheStart = left;
					cacheEnd = right;
				}

				//右边不足cacheNumber数量，保证只加载一次
				if (totalWidth - cacheEnd < offset) {
					int realNumber = (totalWidth - cacheEnd) / itemWidth;
					if (realNumber > 0) {
						int loadIndex = getItemIndexFromPosition(cacheEnd) + 1;
						int recycleIndex = getItemIndexFromPosition(cacheStart);
						Log.d(TAG, "totalWidth - cacheEnd < offset realNumber =" + realNumber + " loadIndex" + loadIndex
								+ " recycleIndex" + recycleIndex);

						updateAndRecycle(loadIndex, loadIndex + realNumber - 1
								, recycleIndex, recycleIndex + realNumber - 1);
					}
				}
			}
		}
	}

	private int getItemIndexFromPosition(int position) {
		int index = position/itemWidth;
		if (index >= container.getChildCount()) {
			index = container.getChildCount() - 1;
		}
		else if (index < 0) {
			index = 0;
		}
		return index;
	}

	private void updateAndRecycle(int start, int end, int recycleStart, int recycleEnd) {
		Log.d(TAG, "updateAndRecycle[" + start + "," +  end + "," +  recycleStart + "," +  recycleEnd + "]");
		adapter.recycle(container, recycleStart, recycleEnd);
		new UpdateThread(start, end).start();
	}

	private class UpdateThread extends Thread implements Callback {

		private int start, end;
		private Handler handler;
		public UpdateThread(int start, int end) {
			this.start = start;
			this.end = end;
			handler = new Handler(this);
		}
		@Override
		public void run() {
			adapter.refreshData(start, end);
			handler.sendMessage(new Message());
		}

		@Override
		public boolean handleMessage(Message msg) {
			adapter.onRefreshOver(container, start, end);
			return true;
		}

	}

	public View getItemView(int index) {
		if (container != null) {
			return container.getChildAt(index);
		}
		return null;
	}
}