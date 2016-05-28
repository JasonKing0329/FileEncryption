package com.king.app.fileencryption.abview;

/*
 * Copyright (C) 2015 www.amengsoft.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

// TODO: Auto-generated Javadoc
/**
 * The Class AbViewUtil.
 */
public class AbViewUtil {

	/***
	 * 动态设置listview的高度 还可以在scrollview里使用屏蔽滚动
	 *
	 * @param listView
	 */
	public static void setListViewHeightBasedOnChildren(ExpandableListView view) {
		ExpandableListAdapter adapter = (ExpandableListAdapter) view.getExpandableListAdapter();
		if (adapter == null) {
			return;
		}
		int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		DisplayMetrics dm = new DisplayMetrics();
		android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
		// DisplayMetrics dm = new DisplayMetrics();
		((Activity) view.getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);

		// // 窗口的宽度
//		params.width = (int) ((dm.widthPixels / 720f) * w);
//		params.height = (int) ((dm.heightPixels / 1280f) * h);
//	    view.measure(w, h);
		int groupHeight = 0;
		for (int i = 0; i < adapter.getGroupCount(); i++) {
//			View listItem = adapter.getGroupView(i, true, null, view);
//			listItem.measure(w, h);
			// 避免ListView在ScrollView里显示不完全
//			int desiredWidth = MeasureSpec.makeMeasureSpec(view.getWidth(),
//					MeasureSpec.AT_MOST);
//			listItem.measure(listItem.getWidth(), listItem.getHeight());

//			groupHeight += listItem.getMeasuredHeight();
			groupHeight+=((dm.heightPixels / 1280f) * 312);
//			int childHight=0;
//			for (int j = 0; j < adapter.getChildrenCount(i); j++) {

//				View childItem = adapter.getChildView(i, 0, true, null, (ViewGroup) listItem.getParent());
//				childItem.measure(w, h);
//				childHight+=childItem.getMeasuredHeight();
//				Log.e("", childItem.toString()+"----计算子view高度-->>"+childItem.getHeight());
//			}
//			groupHeight+=(childHight+listItem.getMeasuredHeight());
		}
//		 for (int i =
//		ViewGroup.LayoutParams params = view.getLayoutParams();
//		params.height = totalHeight;
		params.height = (groupHeight
				+ (view.getDividerHeight() * (adapter.getGroupCount() - 1)));
		params.height += 5;// if without this statement,the listview will be
		// a
		// little short
		// listView.getDividerHeight()获取子项间分隔符占用的高度
		// params.height最后得到整个ListView完整显示需要的高度
		view.setLayoutParams(params);
	}

	/**
	 * 描述：重置AbsListView的高度.
	 * item 的最外层布局要用 RelativeLayout,如果计算的不准，就为RelativeLayout指定一个高度
	 * @param absListView the abs list view
	 * @param lineNumber 每行几个  ListView一行一个item
	 * @param verticalSpace the vertical space
	 */
	public static void setAbsListViewHeight(AbsListView absListView,int lineNumber,int verticalSpace) {

		int totalHeight = getAbsListViewHeight(absListView,lineNumber,verticalSpace);
		ViewGroup.LayoutParams params = absListView.getLayoutParams();
		params.height = totalHeight;
		((MarginLayoutParams) params).setMargins(0, 0, 0, 0);
		absListView.setLayoutParams(params);
	}

	/**
	 * 描述：获取AbsListView的高度.
	 * @param absListView the abs list view
	 * @param lineNumber 每行几个  ListView一行一个item
	 * @param verticalSpace the vertical space
	 */
	public static int getAbsListViewHeight(AbsListView absListView,int lineNumber,int verticalSpace) {
		int totalHeight = 0;
		int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		absListView.measure(w, h);
		ListAdapter mListAdapter = absListView.getAdapter();
		if (mListAdapter == null) {
			return totalHeight;
		}

		int count = mListAdapter.getCount();
		if(absListView instanceof ListView){
			for (int i = 0; i < count; i++) {
				View listItem = mListAdapter.getView(i, null, absListView);
				listItem.measure(w, h);
				totalHeight += listItem.getMeasuredHeight();
			}
			if (count == 0) {
				totalHeight = verticalSpace;
			} else {
				totalHeight = totalHeight + (((ListView)absListView).getDividerHeight() * (count - 1));
			}

		}else if(absListView instanceof GridView){
			int remain = count % lineNumber;
			if(remain>0){
				remain = 1;
			}
			if(mListAdapter.getCount()==0){
				totalHeight = verticalSpace;
			}else{
				View listItem = mListAdapter.getView(0, null, absListView);
				listItem.measure(w, h);
				int line = count/lineNumber + remain;
				totalHeight = line*listItem.getMeasuredHeight()+(line-1)*verticalSpace;
			}

		}
		return totalHeight;

	}

	/**
	 * 测量这个view，最后通过getMeasuredWidth()获取宽度和高度.
	 *
	 * @param v 要测量的view
	 * @return 测量过的view
	 */
	public static void measureView(View v){
		if(v == null){
			return;
		}
		int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		v.measure(w, h);
	}

	/**
	 * 描述：根据分辨率获得字体大小.
	 *
	 * @param screenWidth the screen width
	 * @param screenHeight the screen height
	 * @param textSize the text size
	 * @return the int
	 */
	public static int resizeTextSize(int screenWidth,int screenHeight,int textSize){
		float ratio =  1;
		try {
			float ratioWidth = (float)screenWidth / 480;
			float ratioHeight = (float)screenHeight / 800;
			ratio = Math.min(ratioWidth, ratioHeight);
		} catch (Exception e) {
		}
		return Math.round(textSize * ratio);
	}

	/**
	 *
	 * 描述：dip转换为px
	 * @param context
	 * @param dipValue
	 * @return
	 * @throws
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 *
	 * 描述：px转换为dip
	 * @param context
	 * @param pxValue
	 * @return
	 * @throws
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

}
