package com.king.app.fileencryption.thumbfolder;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.util.ScreenUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IndexView extends LinearLayout implements OnClickListener {

	public interface OnIndexSelectListener {
		public void onSelect(String index);
	}
	
	private OnIndexSelectListener listener;
	
	public IndexView(Context context) {
		super(context);
	}
	public IndexView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IndexView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public IndexView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public void clearAllIndex() {
		removeAllViews();
	}
	
	public void addIndex(String index) {
		TextView tv = new TextView(getContext());
		tv.setGravity(Gravity.CENTER);
		tv.setText(index);
		tv.setTag(index);
		tv.setOnClickListener(this);
		addView(tv);
	}
	
	public void build() {
		int size = getChildCount();
		int minIndex = getResources().getDimensionPixelSize(R.dimen.thumbfolder_index_min_height);
		int normalHeight = getResources().getDimensionPixelSize(R.dimen.thumbfolder_index_height);
		int totalHeight = ScreenUtils.getScreenHeight(getContext())
				- getResources().getDimensionPixelSize(R.dimen.actionbar_height);
		int height = totalHeight / size;
		if (height < minIndex) {
			height = minIndex;
		}
		if (height > normalHeight) {
			height = normalHeight;
		}
		int margin = getResources().getDimensionPixelSize(R.dimen.thumbfolder_index_margin);
		TextView child = null;
		LayoutParams params = null;
		MarginLayoutParams mParams = null;
		for (int i = 0; i < size; i ++) {
			child = (TextView) getChildAt(i);
			child.setBackgroundResource(Application.isLollipop() ? R.drawable.index_item_bk_l : R.drawable.index_item_bk);
			child.setTextColor(getResources().getColor(R.color.white));
			params = (LayoutParams) child.getLayoutParams();
			params.height = height;
			mParams = params;
			mParams.topMargin = margin;
			mParams.bottomMargin = margin;
		}
	}
	
	public void setOnIndexSelectListener(OnIndexSelectListener listener) {
		this.listener = listener;
	}
	@Override
	public void onClick(View view) {
		if (listener != null) {
			String index = (String) view.getTag();
			listener.onSelect(index);
		}
	}
	
	public int getLeftPosition() {
		if (getChildAt(0) != null) {
			return getChildAt(0).getLeft();
		}
		return 0;
	}
	
	public int getTopPosition() {
		if (getChildAt(0) != null) {
			return getChildAt(0).getTop();
		}
		return 0;
	}
}
