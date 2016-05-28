package com.king.app.fileencryption.publicview;

import com.king.app.fileencryption.util.ScreenUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class FixGridLayout extends ViewGroup {

	private final String TAG = "FixGridLayout";
	private int mCellWidth;
	private int mCellHeight;
	private int nColumn;
	private int nRow;
	private int mWidth = -1;

	public FixGridLayout(Context context) {
		super(context);
	}

	public FixGridLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FixGridLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setmCellWidth(int w) {
		mCellWidth = w;
	}

	public void setmCellHeight(int h) {
		mCellHeight = h;
	}

	public void setWidth(int width) {
		mWidth = width;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d(TAG, "onLayout");
		int cellWidth = mCellWidth;
		int cellHeight = mCellHeight;
		int horPadding = getPaddingLeft();
		int verPadding = getPaddingTop();
		if (nColumn < 0) {
			nColumn = 1;
		}
		int x = horPadding;
		int y = verPadding;
		int i = 0;
		int count = getChildCount();
		View childView = null;
		int w = 0;
		int h = 0;
		int left = 0;
		int top = 0;
		for (int j = 0; j < count; j++) {
			childView = getChildAt(j);
			w = childView.getMeasuredWidth();
			h = childView.getMeasuredHeight();
			// 计算子控件的顶点坐标
			left = x + ((cellWidth - w) / 2);
			top = y + ((cellHeight - h) / 2);
			// int left = x;
			// int top = y;
			// 布局子控件
			childView.layout(left, top, left + w, top + h);

			if (i >= (nColumn - 1)) {
				i = 0;
				x = horPadding;
				y += cellHeight;
			} else {
				i++;
				x += cellWidth;

			}
		}
	}

	/**
	 * 计算控件及子控件所占区域
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(TAG, "onMeasure");
		// 创建测量参数
		int cellWidthSpec = MeasureSpec.makeMeasureSpec(mCellWidth,
				MeasureSpec.AT_MOST);
		int cellHeightSpec = MeasureSpec.makeMeasureSpec(mCellHeight,
				MeasureSpec.AT_MOST);
		// 记录ViewGroup中Child的总个数
		int count = getChildCount();
		Log.d(TAG, "cellWidthSpec = " + cellWidthSpec
				+ ", cellHeightSpec=" + cellHeightSpec + ", count=" + count);
		// 设置子空间Child的宽高
		int horPadding = getPaddingLeft();
		int verPadding = getPaddingTop();
		int totalWidth = mWidth == -1? ScreenUtils.getScreenWidth(getContext()):mWidth;
		//应该用实际宽度
		nColumn = (totalWidth - horPadding * 2) / mCellWidth;
		nRow = count / nColumn + 1;
		for (int i = 0; i < count; i++) {
			View childView = getChildAt(i);
			/*
			 * 090 This is called to find out how big a view should be. 091 The
			 * parent supplies constraint information in the width and height
			 * parameters. 092 The actual mesurement work of a view is performed
			 * in onMeasure(int, int), 093 called by this method. 094 Therefore,
			 * only onMeasure(int, int) can and must be overriden by subclasses.
			 * 095
			 */
			childView.measure(cellWidthSpec, cellHeightSpec);
		}
		// 设置容器控件所占区域大小
		// 注意setMeasuredDimension和resolveSize的用法
		setMeasuredDimension(resolveSize(totalWidth, widthMeasureSpec),
				resolveSize(mCellHeight * nRow + verPadding * 2, heightMeasureSpec));
		// setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);

		// 不需要调用父类的方法
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
