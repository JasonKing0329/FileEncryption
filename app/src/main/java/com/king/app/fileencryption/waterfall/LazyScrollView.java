package com.king.app.fileencryption.waterfall;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
/**
 *  Be sure that load LazyScrollView by follow step:
 *  (lazyScrollView.reset();)//if switch from normal to endless mode, first call
 *  (lazyScrollView.setEndlessMode(true or false);)
 lazyScrollView.prepare(capacity, pageCount, colCount);
 lazyScrollView.setImagePathList(List);
 lazyScrollView.setImageProvider(com.king.app.fileencryption.waterfall.ImageProvider);
 lazyScrollView.setOnItemClickListener(LazyScrollView.OnItemClickListener);
 lazyScrollView.setup();
 *
 */
public class LazyScrollView extends ScrollView {

	/** 延迟发送message的handler */
	private DelayHandler delayHandler;
	/** 添加单元到瀑布流中的Handler */
	private AddItemHandler addItemHandler;

	/** ScrollView直接包裹的LinearLayout */
	private LinearLayout containerLayout;
	/** 存放所有的列Layout */
	private ArrayList<LinearLayout> colLayoutArray;

	/** 当前所处的页面（已经加载了几次） */
	private int currentPage;

	/** 存储每一列中向上方向的未被回收bitmap的单元的最小行号 */
	private int[] currentTopLineIndex;
	/** 存储每一列中向下方向的未被回收bitmap的单元的最大行号 */
	private int[] currentBomLineIndex;
	/** 存储每一列中已经加载的最下方的单元的行号 */
	private int[] bomLineIndex;
	/** 存储每一列的高度 */
	private int[] colHeight;

	/** 所有的图片资源路径 */
	private List<String> imagePathList;

	/** 瀑布流显示的列数 */
	private int colCount;
	/** 瀑布流每一次加载的单元数量 */
	private int pageCount;
	/** 瀑布流容纳量 */
	private int capacity;

	/** 列的宽度 */
	private int colWidth;

	private boolean isFirstPage;

	private ImageProvider imageProvider;

	private FlowingItemListener flowingItemListener;
	private OnItemClickListener onItemClickListener;
	private OnPageListener onPageListener;
	private boolean isEndless;

	public LazyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LazyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LazyScrollView(Context context) {
		super(context);
	}

	/**
	 *
	 * @param capacity total capacity, should be list's size
	 * @param pageCount item number for each available scroll synchronize
	 * @param colCount column number of view
	 */
	public void prepare(int capacity, int pageCount, int colCount) {
		this.capacity = capacity;
		this.pageCount = pageCount;
		this.colCount = colCount;
	}

	public void setEndlessMode(boolean endless) {
		isEndless = endless;
	}

	/** 基本初始化工作 */
	private void init() {
		delayHandler = new DelayHandler(this);
		addItemHandler = new AddItemHandler(this);
		colWidth = getResources().getDisplayMetrics().widthPixels / colCount;

		colHeight = new int[colCount];
		currentTopLineIndex = new int[colCount];
		currentBomLineIndex = new int[colCount];
		bomLineIndex = new int[colCount];
		colLayoutArray = new ArrayList<LinearLayout>();

		flowingItemListener = new FlowingItemListener();
	}

	public void setImagePathList(List<String> pathList) {
		imagePathList = pathList;
	}

	public void setImageProvider(ImageProvider provider) {
		imageProvider = provider;
	}

	public void reset() {
		scrollTo(0, 0);//必须这样，如果reset之前瀑布流已经翻滚到非第一页，那么在reset之后加载新的图片，会自动执行onscrollchanged向上翻滚
		//，而在onscrollchanged向上翻滚中有相关处理，而这时已然reset，会引起空指针异常而FC
		LinearLayout layout = null;
		FlowingView item = null;
		int size = containerLayout.getChildCount();
		for (int i = 0; i < size; i++) {
			layout = (LinearLayout) containerLayout.getChildAt(i);
			for (int j = 0; j < layout.getChildCount(); j ++) {
				item = (FlowingView) layout.getChildAt(j);
				item.recycle();
			}
		}
		removeAllViews();
		colLayoutArray.clear();
		currentPage = 0;
		isFirstPage = true;
	}

	/**
	 * 在外部调用 第一次装载页面 必须调用
	 */
	public void setup() {
		init();
		containerLayout = new LinearLayout(getContext());
		containerLayout.setBackgroundColor(Color.TRANSPARENT);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		addView(containerLayout, layoutParams);

		for (int i = 0; i < colCount; i++) {
			LinearLayout colLayout = new LinearLayout(getContext());
			LinearLayout.LayoutParams colLayoutParams = new LinearLayout.LayoutParams(
					colWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
			colLayout.setPadding(0, 0, 0, 0);
			colLayout.setOrientation(LinearLayout.VERTICAL);

			containerLayout.addView(colLayout, colLayoutParams);
			colLayoutArray.add(colLayout);
		}

		if (imagePathList != null) {
			addNextPageContent(true);
		}
	}

	/**
	 * my: to support update image width dynamically
	 * column not change, but orientation changed or refresh
	 */
	public void updateColumn() {
		setColumnChanged(colCount);
	}

	/**
	 * my: to support update image width dynamically
	 * column changed
	 * @param width
	 */
	public void updateColumn(int column) {

//		if (column > colCount) {
//			increaseColumn(colCount, column);
//		}
//		else if (column < colCount) {
//			decreaseColumn(column, colCount);
//		}

		colCount = column;
		setColumnChanged(column);
	}

//	private void decreaseColumn(int column, int colCount) {
//		int startCol = 0;
//		for (int i = 1; i < column; i ++) {
//			if (colLayoutArray.get(i).getChildCount() != colLayoutArray.get(i - 1).getChildCount()) {
//				startCol = i;
//			}
//		}
//
//		int count = 0;
//		for (int i = column; i < colCount; i ++) {
//			count = colLayoutArray.get(i).getChildCount();
//			for (int j = 0; j < count; j ++) {
//				colLayoutArray.get(startCol).addView(colLayoutArray.get(i).getChildAt(j));
//				if (startCol == column) {
//					startCol = 0;
//				}
//			}
//		}
//	}
//
//	private void increaseColumn(int colCount, int column) {
//
//		for (int i = colCount; i < column; i --) {
//			LinearLayout layout = new LinearLayout(getContext());1
//		}
//
//		int startCol = 0;
//		for (int i = 1; i < column; i ++) {
//			if (colLayoutArray.get(i).getChildCount() != colLayoutArray.get(i - 1).getChildCount()) {
//				startCol = i;
//			}
//		}
//
//	}

	/**
	 * my: to support update image width dynamically
	 */
	private void setColumnChanged(int column) {
		colWidth = getResources().getDisplayMetrics().widthPixels / column;
		LinearLayout.LayoutParams params = null;
		FlowingView iteView = null;
		int iteSize = 0;
		for (int i = 0; i < colLayoutArray.size(); i++) {
			params = (android.widget.LinearLayout.LayoutParams) colLayoutArray.get(i).getLayoutParams();
			params.width = colWidth;
			iteSize = colLayoutArray.get(i).getChildCount();
			for (int j = 0; j < iteSize; j++) {
				iteView = (FlowingView) colLayoutArray.get(i).getChildAt(j);
				params = (android.widget.LinearLayout.LayoutParams) iteView.getLayoutParams();
				params.width = colWidth;
				iteView.updateWidth(colWidth);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_UP:
				//手指离开屏幕的时候向DelayHandler延时发送一个信息，然后DelayHandler
				//届时来判断当前的滑动位置，进行不同的处理。
				delayHandler.sendMessageDelayed(delayHandler.obtainMessage(), 200);
				break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		//在滚动过程中，回收滚动了很远的bitmap,防止OOM
		/*---回收算法说明：
		 * 回收的整体思路是：
		 * 我们只保持当前手机显示的这一屏以及上方两屏和下方两屏 一共5屏内容的Bitmap,
		 * 超出这个范围的单元Bitmap都被回收。
		 * 这其中又包括了一种情况就是之前回收过的单元的重新加载。
		 * 详细的讲解：
		 * 向下滚动的时候：回收超过上方两屏的单元Bitmap,重载进入下方两屏以内Bitmap
		 * 向上滚动的时候：回收超过下方两屏的单元bitmao,重载进入上方两屏以内bitmap
		 * ---*/
		int viewHeight = getHeight();
		if (t > oldt) {//向下滚动
			if (t > 2 * viewHeight) {
				for (int i = 0; i < colCount; i++) {
					LinearLayout colLayout = colLayoutArray.get(i);
					//回收上方超过两屏bitmap
					FlowingView topItem = (FlowingView) colLayout.getChildAt(currentTopLineIndex[i]);
					if (topItem.getFootHeight() < t - 2 * viewHeight) {
						topItem.recycle();
						currentTopLineIndex[i] ++;
					}
					//重载下方进入(+1)两屏以内bitmap
					FlowingView bomItem = (FlowingView) colLayout.getChildAt(Math.min(currentBomLineIndex[i] + 1, bomLineIndex[i]));
					if (bomItem.getFootHeight() <= t + 3 * viewHeight) {
						bomItem.reload();
						currentBomLineIndex[i] = Math.min(currentBomLineIndex[i] + 1, bomLineIndex[i]);
					}
				}
			}
		} else {//向上滚动
			for (int i = 0; i < colCount; i++) {
				LinearLayout colLayout = colLayoutArray.get(i);
				//回收下方超过两屏bitmap
				FlowingView bomItem = (FlowingView) colLayout.getChildAt(currentBomLineIndex[i]);
				if (bomItem.getFootHeight() > t + 3 * viewHeight) {
					bomItem.recycle();
					currentBomLineIndex[i] --;
				}
				//重载上方进入(-1)两屏以内bitmap
				FlowingView topItem = (FlowingView) colLayout.getChildAt(Math.max(currentTopLineIndex[i] - 1, 0));
				if (topItem.getFootHeight() >= t - 2 * viewHeight) {
					topItem.reload();
					currentTopLineIndex[i] = Math.max(currentTopLineIndex[i] - 1, 0);
				}
			}
		}
		super.onScrollChanged(l, t, oldl, oldt);
	}

	/**
	 * 这里之所以要用一个Handler，是为了使用他的延迟发送message的函数
	 * 延迟的效果在于，如果用户快速滑动，手指很早离开屏幕，然后滑动到了底部的时候，
	 * 因为信息稍后发送，在手指离开屏幕到滑动到底部的这个时间差内，依然能够加载图片
	 * @author carrey
	 *
	 */
	private static class DelayHandler extends Handler {
		private WeakReference<LazyScrollView> waterFallWR;
		private LazyScrollView waterFall;
		public DelayHandler(LazyScrollView waterFall) {
			waterFallWR = new WeakReference<LazyScrollView>(waterFall);
			this.waterFall = waterFallWR.get();
		}

		@Override
		public void handleMessage(Message msg) {
			//判断当前滑动到的位置，进行不同的处理
			if (waterFall.getScrollY() + waterFall.getHeight() >=
					waterFall.getMaxColHeight() - 20) {
				//滑动到底部，添加下一页内容
				waterFall.addNextPageContent(false);
			} else if (waterFall.getScrollY() == 0) {
				//滑动到了顶部
			} else {
				//滑动在中间位置
			}
			super.handleMessage(msg);
		}
	}

	/**
	 * 添加单元到瀑布流中的Handler
	 * @author carrey
	 *
	 */
	private static class AddItemHandler extends Handler {
		private WeakReference<LazyScrollView> waterFallWR;
		private LazyScrollView waterFall;
		public AddItemHandler(LazyScrollView waterFall) {
			waterFallWR = new WeakReference<LazyScrollView>(waterFall);
			this.waterFall = waterFallWR.get();
		}
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0x00:
					FlowingView flowingView = (FlowingView)msg.obj;
					waterFall.addItem(flowingView);
					break;
			}
			super.handleMessage(msg);
		}
	}
	/**
	 * 添加单元到瀑布流中
	 * @param flowingView
	 */
	private void addItem(FlowingView flowingView) {
		int minHeightCol = getMinHeightColIndex();
		colLayoutArray.get(minHeightCol).addView(flowingView);
		colHeight[minHeightCol] += flowingView.getViewHeight();
		flowingView.setFootHeight(colHeight[minHeightCol]);

		if (!isFirstPage) {
			bomLineIndex[minHeightCol] ++;
			currentBomLineIndex[minHeightCol] ++;
		}
	}

	/**
	 * 添加下一个页面的内容
	 */
	private void addNextPageContent(boolean isFirstPage) {
		this.isFirstPage = isFirstPage;
		//capacity == currentPage * pageCount, 由于转屏是引用原list，所以不能重复加，这样会造成imagePathList加载过多
		if (isEndless && capacity == currentPage * pageCount) {
			capacity += onPageListener.onNextPage(imagePathList);
		}
		//添加下一个页面的pageCount个单元内容
		for (int i = pageCount * currentPage;
			 i < pageCount * (currentPage + 1) && i < capacity; i++) {
			new Thread(new PrepareFlowingViewRunnable(i)).run();
		}
		currentPage ++;
	}

	/**
	 * 异步加载要添加的FlowingView
	 * @author carrey
	 *
	 */
	private class PrepareFlowingViewRunnable implements Runnable {
		private int id;
		public PrepareFlowingViewRunnable (int id) {
			this.id = id;
		}

		@Override
		public void run() {
			FlowingView flowingView = new FlowingView(getContext(), id, colWidth);
			String path = imagePathList.get(id);
			flowingView.setImageFilePath(path);
			flowingView.setImageProvider(imageProvider);
			flowingView.loadImage();
			flowingView.setTag(id);
			flowingView.setOnClickListener(flowingItemListener);
			addItemHandler.sendMessage(addItemHandler.obtainMessage(0x00, flowingView));
		}

	}

	/**
	 * 获得所有列中的最大高度
	 * @return
	 */
	private int getMaxColHeight() {
		int maxHeight = colHeight[0];
		for (int i = 1; i < colHeight.length; i++) {
			if (colHeight[i] > maxHeight)
				maxHeight = colHeight[i];
		}
		return maxHeight;
	}

	/**
	 * 获得目前高度最小的列的索引
	 * @return
	 */
	private int getMinHeightColIndex() {
		int index = 0;
		for (int i = 1; i < colHeight.length; i++) {
			if (colHeight[i] < colHeight[index])
				index = i;
		}
		return index;
	}

	public interface OnPageListener {
		public int onNextPage(List<String> imagePathList);
	}

	public void addOnPageListener(OnPageListener listener) {
		onPageListener = listener;
	}

	private class FlowingItemListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			onItemClick(v, (Integer) v.getTag());
		}

	}

	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		onItemClickListener = listener;
	}

	public void onItemClick(View view, int position) {
		onItemClickListener.onItemClick(view, position);
	}
}