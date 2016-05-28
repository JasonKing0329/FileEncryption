package com.king.app.fileencryption.guide;

import java.util.LinkedList;

import com.king.app.fileencryption.R;
import com.nineoldandroids.view.ViewHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;

/**
 * 采用4个ImageView循环交替的方式实现自动滚动视图
 * 初始化时4个ImageView从左到右排列，仅2个在科室区域
 * 利用线程每隔50ms发送位置更新通知，使每个ImageView位置左移
 * 当首个ImageView完全隐藏后将其拼接到最末端，采用循环队列的思想
 * @author JingYang
 *
 */
public class AutoScrollView extends RelativeLayout implements OnClickListener {

	private class ScrollImage {
		ImageView imageView;
		LayoutParams layoutParams;
		float xPos;
	}

	public interface ActionListener {
		public void onAutoScrollViewClick(View parent, View view);
	}

	/** 循环队列，实现ImageView的循环拼接。为了方便处理，用LinkedList扩展Queue的功能 **/
	private LinkedList<ScrollImage> imageQueue;

	private int itemWidth;
	private int SCROLL_DISTANCE_PER_TIME = 5;

	private AutoScrollAdapter autoScrollAdapter;

	private boolean isRun;
	private final int MSG_UPDATE_POSITION = 0;
	private ScrollThread scrollThread;

	private ActionListener actionListener;

	public AutoScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AutoScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		itemWidth = getResources().getDimensionPixelSize(R.dimen.guide_line3_width);

		ScrollImage scrollImage1 = new ScrollImage();
		scrollImage1.imageView = new ImageView(getContext());
		scrollImage1.imageView.setScaleType(ScaleType.CENTER_CROP);
		scrollImage1.imageView.setOnClickListener(this);
		scrollImage1.layoutParams = new LayoutParams(itemWidth, LayoutParams.MATCH_PARENT);
		scrollImage1.xPos = 0;
		addView(scrollImage1.imageView, scrollImage1.layoutParams);

		ScrollImage scrollImage2 = new ScrollImage();
		scrollImage2.imageView = new ImageView(getContext());
		scrollImage2.imageView.setScaleType(ScaleType.CENTER_CROP);
		scrollImage2.imageView.setOnClickListener(this);
		scrollImage2.layoutParams = new LayoutParams(itemWidth, LayoutParams.MATCH_PARENT);
		addView(scrollImage2.imageView, scrollImage2.layoutParams);
		scrollImage2.xPos = itemWidth;
		ViewHelper.setTranslationX(scrollImage2.imageView, itemWidth);

		ScrollImage scrollImage3 = new ScrollImage();
		scrollImage3.imageView = new ImageView(getContext());
		scrollImage3.imageView.setScaleType(ScaleType.CENTER_CROP);
		scrollImage3.imageView.setOnClickListener(this);
		scrollImage3.layoutParams = new LayoutParams(itemWidth, LayoutParams.MATCH_PARENT);
		addView(scrollImage3.imageView, scrollImage3.layoutParams);
		scrollImage3.xPos = itemWidth * 2;
		ViewHelper.setTranslationX(scrollImage3.imageView, itemWidth * 2);

		ScrollImage scrollImage4 = new ScrollImage();
		scrollImage4.imageView = new ImageView(getContext());
		scrollImage4.imageView.setScaleType(ScaleType.CENTER_CROP);
		scrollImage4.imageView.setOnClickListener(this);
		scrollImage4.layoutParams = new LayoutParams(itemWidth, LayoutParams.MATCH_PARENT);
		addView(scrollImage4.imageView, scrollImage4.layoutParams);
		scrollImage4.xPos = itemWidth * 3;
		ViewHelper.setTranslationX(scrollImage4.imageView, itemWidth * 3);

		imageQueue = new LinkedList<ScrollImage>();
		imageQueue.offer(scrollImage1);
		imageQueue.offer(scrollImage2);
		imageQueue.offer(scrollImage3);
		imageQueue.offer(scrollImage4);
	}

	public void setAdapter(AutoScrollAdapter adapter) {
		autoScrollAdapter = adapter;
		refreshData();
	}

	public void setActionListener (ActionListener listener) {
		actionListener = listener;
	}

	private void refreshData() {
		if (autoScrollAdapter != null) {
			autoScrollAdapter.loadNextImage(imageQueue.get(0).imageView);
			autoScrollAdapter.loadNextImage(imageQueue.get(1).imageView);
			autoScrollAdapter.loadNextImage(imageQueue.get(2).imageView);
			autoScrollAdapter.loadNextImage(imageQueue.get(3).imageView);
		}
	}

	public void startScroll() {
		scrollThread = new ScrollThread();
		scrollThread.start();
	}

	public void stop() {
		isRun = false;
		if (scrollThread != null) {
			scrollThread.interrupt();
		}
	}

	public void restart() {
		if (scrollThread != null) {
			scrollThread = new ScrollThread();
			scrollThread.start();
		}
	}

	private class ScrollThread extends Thread {

		public ScrollThread() {
			isRun = true;
		}

		@Override
		public void run() {
			while (isRun) {
				try {
					Thread.sleep(50);
					mHandler.sendEmptyMessage(MSG_UPDATE_POSITION);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}

	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case MSG_UPDATE_POSITION:
					//所有view位置一起左移相同距离
					for (int i = 0; i < imageQueue.size(); i ++) {
						imageQueue.get(i).xPos = imageQueue.get(i).xPos - SCROLL_DISTANCE_PER_TIME;
						ViewHelper.setTranslationX(imageQueue.get(i).imageView, imageQueue.get(i).xPos);
					}

					//第一个view完全隐藏时将其拼接到末端
					ScrollImage head = imageQueue.peek();
					if (head.xPos < -itemWidth) {
						head.xPos = imageQueue.get(imageQueue.size() - 1).xPos + itemWidth;
						autoScrollAdapter.loadNextImage(head.imageView);
						imageQueue.offer(imageQueue.poll());
					}
					break;

				default:
					break;
			}
			super.handleMessage(msg);
		}

	};

	public void notifyDataSetChanged() {
		refreshData();
	}

	@Override
	public void onClick(View v) {
		if (actionListener != null) {
			actionListener.onAutoScrollViewClick(this, v);
		}
	}

}
