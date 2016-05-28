package com.king.app.fileencryption.slidingmenu;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.util.DisplayHelper;
import com.king.app.fileencryption.util.ScreenUtils;

public class SlidingMenuCreator {

	private Context mContext;
	private OnClickListener onClickListener;
	public SlidingMenuCreator(Context context, OnClickListener listener) {
		mContext = context;
		onClickListener = listener;
	}
	
	public void onConfigurationChanged(ViewGroup group) {

		int size = group.getChildCount();
		if (size > 0) {
			int screenHeight = ScreenUtils.getScreenHeight(mContext);
			int menuHeight = screenHeight * 4 / 5;
			int baseNum = mContext.getResources().getInteger(R.integer.slidingmenu_base_item);
			if (!DisplayHelper.isTabModel(mContext) && size > baseNum) {
				menuHeight = screenHeight - 40;
			}
			if (size > baseNum) {
				baseNum = size;
			}
			
			ViewGroup.LayoutParams params = group.getLayoutParams();
			params.height = menuHeight;
			params.width = ViewGroup.LayoutParams.MATCH_PARENT;
			int topPadding = mContext.getResources().getDimensionPixelOffset(R.dimen.slidingmenu_margin_top);
			menuHeight = menuHeight - topPadding * 2;
			group.setPadding(0, topPadding, 0, topPadding);

			//rule: menuHeight = itemHeight*menuRes.length + itemPadding(menuRes.length - 1)
			//itemHeight : itemPadding = 3 : 1
			//--> itemHeight = 3*menuHeight / (4*menuRes.length - 1)
			int itemHeight = 3 * menuHeight / (4 * baseNum - 1);
			int itemMargin = menuHeight / (4 * baseNum - 1);
			
			TextView textView = null;
			ViewGroup.LayoutParams tParams = null;
			MarginLayoutParams marginParams = null;
			for (int i = 0; i < size; i ++) {
				textView = (TextView) group.getChildAt(i);
				tParams = textView.getLayoutParams();
				tParams.height = itemHeight;
				marginParams = (MarginLayoutParams) tParams;
				if (i != size - 1) {
					marginParams.bottomMargin = itemMargin;
				}
				textView.setTextSize(((float)itemHeight/2)/DisplayHelper.getDpiDensityNum(mContext));
			}
		}
	}
	
	public void loadMenu(int[] menuRes, ViewGroup group, int leftOrRight) {
		if (leftOrRight == SettingProperties.SLIDINGMENU_LEFT) {
			group.setBackgroundResource(Application.isLollipop() ? R.drawable.shape_slidingmenu_bk_l : R.drawable.shape_slidingmenu_bk);
		}
		else {
			group.setBackgroundResource(Application.isLollipop() ? R.drawable.shape_slidingmenu_bk_right_l : R.drawable.shape_slidingmenu_bk_right);
			if (group instanceof LinearLayout) {
				((LinearLayout) group).setGravity(Gravity.RIGHT);
			}
		}

		int screenHeight = ScreenUtils.getScreenHeight(mContext);
		int menuHeight = screenHeight * 4 / 5;
		int baseNum = mContext.getResources().getInteger(R.integer.slidingmenu_base_item);
		if (!DisplayHelper.isTabModel(mContext) && menuRes.length > baseNum) {
			menuHeight = screenHeight - 40;
		}
		if (menuRes.length > baseNum) {
			baseNum = menuRes.length;
		}
		
		ViewGroup.LayoutParams params = group.getLayoutParams();
		params.height = menuHeight;
		params.width = ViewGroup.LayoutParams.MATCH_PARENT;
		int topPadding = mContext.getResources().getDimensionPixelOffset(R.dimen.slidingmenu_margin_top);
		menuHeight = menuHeight - topPadding * 2;
		group.setPadding(0, topPadding, 0, topPadding);

		//rule: menuHeight = itemHeight*menuRes.length + itemPadding(menuRes.length - 1)
		//itemHeight : itemPadding = 3 : 1
		//--> itemHeight = 3*menuHeight / (4*menuRes.length - 1)
		int itemHeight = 3 * menuHeight / (4 * baseNum - 1);
		int itemMargin = menuHeight / (4 * baseNum - 1);
		
		for (int i = 0; i < menuRes.length; i ++) {
			if (i == menuRes.length - 1) {
				itemMargin = 0;
			}
			addMenuItem(menuRes, i, group, leftOrRight, itemHeight, itemMargin);
		}
	}
	private void addMenuItem(int[] menuRes, int index, ViewGroup group, int leftOrRight, int itemHeight, int itemMargin) {
		TextView view = new TextView(mContext);
		view.setText(menuRes[index]);
		view.setId(menuRes[index]);
		view.setGravity(Gravity.CENTER_VERTICAL);
		view.setOnClickListener(onClickListener);
		view.setTextColor(mContext.getResources().getColor(R.color.white));
		//view.setTextSize(mContext.getResources().getDimensionPixelOffset(R.dimen.slidingmenu_text_size));
		view.setTextSize(((float)itemHeight/2)/DisplayHelper.getDpiDensityNum(mContext));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, itemHeight);
		MarginLayoutParams params2 = params;
		params2.bottomMargin = itemMargin;
		
		if (leftOrRight == SettingProperties.SLIDINGMENU_RIGHT) {
			view.setBackgroundResource(R.drawable.selector_slidingmenuitem_bk_right);
			view.setGravity(Gravity.RIGHT);
			params2.rightMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.slidingmenu_margin_left);
		}
		else {
			view.setBackgroundResource(R.drawable.selector_slidingmenuitem_bk);
			params2.leftMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.slidingmenu_margin_left);
		}
		group.addView(view, params);
	}

	public Bitmap loadBackground(String key) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		String file = preferences.getString(key, null);
		if (file == null) {
			return null;
		}
		else {
			Bitmap bitmap = PictureManagerUpdate.getInstance().createHDBitmap(file);
			if (bitmap == null) {
				SharedPreferences.Editor editor = preferences.edit();
				editor.remove(key);
				editor.commit();
				return null;
			}
			else {
				return bitmap;
			}
		}
	}
	
}
