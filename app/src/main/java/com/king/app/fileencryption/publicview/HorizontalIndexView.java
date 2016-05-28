package com.king.app.fileencryption.publicview;

import java.util.List;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.util.ScreenUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HorizontalIndexView extends LinearLayout implements OnClickListener {

	public static class IndexItem {
		public String index;
		public Object object;
	}

	public interface OnPageSelectListener {
		public void onSelect(int index);
	}

	private final String TAG = "HorizontalIndexView";
	private HorizontalScrollView pageIndexScrollViewParent;
	private LinearLayout pageIndexScrollView;
	private FixGridLayout pageIndexFixGridParent;
	private IndexProvider indexProvider;
	private OnPageSelectListener pageSelectListener;
	private int selectedIndex;

	private boolean enableScroll = true;
	private int size, minSize, space;
	private int maxWidth;

	public HorizontalIndexView(Context context) {
		super(context);
		init();
	}
	public HorizontalIndexView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HorizontalIndexView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		indexProvider = new IndexProvider();
		selectedIndex = 1;
	}

	public void setScroll(boolean scroll) {
		enableScroll = scroll;
	}

	public void show() {
		Log.d(TAG, "enableScroll " + enableScroll);
		if (enableScroll) {
			if (pageIndexScrollViewParent == null) {
				pageIndexScrollViewParent = new HorizontalScrollView(getContext());
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				pageIndexScrollViewParent.setLayoutParams(params);
				pageIndexScrollViewParent.setHorizontalScrollBarEnabled(false);

				pageIndexScrollView = new LinearLayout(getContext());
				params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				pageIndexScrollView.setLayoutParams(params);
				pageIndexScrollView.setOrientation(HORIZONTAL);
				pageIndexScrollViewParent.addView(pageIndexScrollView);
				addView(pageIndexScrollViewParent);
			}
		}
		else {
			if (pageIndexFixGridParent == null) {
				pageIndexFixGridParent = new FixGridLayout(getContext());
				pageIndexFixGridParent.setWidth(ScreenUtils.getScreenWidth(getContext()) -
						getResources().getDimensionPixelSize(R.dimen.page_index_padding_hor) * 2);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				pageIndexFixGridParent.setLayoutParams(params);
				addView(pageIndexFixGridParent);
			}
		}
		refresh();
	}

	public void setIndexSize(int size, int minSize) {
		this.size = size;
		this.minSize = minSize;
	}

	public void setItemSpace(int space) {
		this.space = space;
	}

	public void setMaxWidth(int width) {
		maxWidth = width;
	}

	public void setIndexList(List<IndexItem> list) {
		indexProvider.setIndexList(list);
	}

	public void setOnPageSelectListener(OnPageSelectListener listener) {
		pageSelectListener = listener;
	}

	public void refresh() {
		indexProvider.refreshPageIndexView();
	}

	public int getPagesNumber() {
		return indexProvider.getPagesNumber();
	}

	@Override
	public void onClick(View view) {

		selectedIndex = (Integer) view.getTag();
		if (pageSelectListener != null) {
			pageSelectListener.onSelect(selectedIndex);
		}
	}

	public void select(int index) {
		if (selectedIndex != index) {
			TextView textView = indexProvider.getPageIndexView(selectedIndex);
			if (textView != null) {
				textView.setSelected(false);
			}
			selectedIndex = index;
			textView = indexProvider.getPageIndexView(selectedIndex);
			if (textView != null) {
				textView.setSelected(true);
			}
		}
		if (pageSelectListener != null) {
			pageSelectListener.onSelect(index);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);//必须实现父类方法，否则不会显示
	}

	public static class PageIndexOutOfBoundsException extends Exception {

	}

	private class IndexProvider {

		private List<IndexItem> indexList;
		private TextView pageIndexView[];

		public IndexProvider() {
		}

		public void setIndexList(List<IndexItem> list) {
			indexList = list;
		}

		public void refreshPageIndexView() {
			if (enableScroll) {
				pageIndexScrollView.removeAllViews();
			}
			else {
				pageIndexFixGridParent.removeAllViews();
			}

			createPageIndexView();
			pageIndexView[selectedIndex].setSelected(true);
			for (int i = 0; i < indexList.size(); i ++) {
				pageIndexView[i + 1].setText(indexList.get(i).index);
			}
		}

		private void createPageIndexView() {

			if (!enableScroll) {
				int s = size;
				if (size * indexList.size() > maxWidth) {
					s = minSize;
				}
				pageIndexFixGridParent.setmCellHeight(s);
				pageIndexFixGridParent.setmCellWidth(s);
			}
			pageIndexView = new TextView[indexList.size() + 1];
			TextView view = null;

			int inflatId = R.layout.sorder_page_index;
			if (Application.isLollipop()) {
				inflatId = R.layout.sorder_page_index_l;
			}
			for (int i = 1; i <= indexList.size(); i ++) {
				/*
			int width = context.getResources().getDimensionPixelSize(R.dimen.page_index_layout_width);
			int padding = context.getResources().getDimensionPixelSize(R.dimen.page_index_padding);
			int textSize = context.getResources().getDimensionPixelSize(R.dimen.page_index_size);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
			view = new TextView(context);
				view.setText("" + i);
				view.setLayoutParams(params);
				view.setPadding(padding, padding, padding, padding);
				view.setOnClickListener(pageIndexListener);
				view.setTextSize(textSize);*/
				view = (TextView) LayoutInflater.from(getContext()).inflate(inflatId, null);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
				view.setLayoutParams(params);
				view.setOnClickListener(HorizontalIndexView.this);
				view.setTag(i);
				pageIndexView[i] = view;
				if (enableScroll) {
					if (i > 1) {//很奇怪在horizontalscroll包裹下的linearlayout里的textview，在最后的布局上给textview设置
						//的padding没有效果，最终导致没有边距，所以只能强行加margin
						MarginLayoutParams mParams = params;
						mParams.leftMargin = space/2;
					}
					pageIndexScrollView.addView(view);
				}
				else {
					pageIndexFixGridParent.addView(view);
				}
			}
		}

		public TextView getPageIndexView(int index) {
			return pageIndexView[index];
		}


		public int getPagesNumber() {
			if (indexList == null) {
				return 0;
			}
			return indexList.size();
		}
	}

	public void requestLayoutFixGrid() {
		if (pageIndexFixGridParent != null) {
			pageIndexFixGridParent.requestLayout();
		}
	}

}
