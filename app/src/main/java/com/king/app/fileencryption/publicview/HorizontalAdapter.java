package com.king.app.fileencryption.publicview;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.data.Configuration;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class HorizontalAdapter extends HoriBaseAdapter {

	private final String TAG = "HorizontalAdapter";
	private List<String> fileList;
	private Context context;
	private List<Bitmap> cacheImageList;
	private Bitmap defaultBitmap;

	private int selection;

	public HorizontalAdapter(Context context, List<String> fileList) {
		this.context = context;
		selection = -1;
		cacheImageList = new ArrayList<Bitmap>();
		defaultBitmap = PictureManagerUpdate.getInstance().getUnavailableItemImage(context);
		setList(fileList);
	}

	public void updateList(List<String> fileList) {
		//setList(imageList);
		this.fileList = fileList;
		recycle();
		initCacheImageList();
	}

	private void initCacheImageList() {
		if (fileList != null) {
			for (int i = 0; i < fileList.size(); i ++) {
				cacheImageList.add(null);
			}
		}
	}

	public void setSelection(int position) {
		selection = position;
	}

	private void setList(List<String> fileList) {
		if (this.fileList == null) {
			this.fileList = fileList;
		}
		else {
			this.fileList.clear();
			this.fileList = fileList;
		}

		recycle();
		initCacheImageList();
	}
	@Override
	public int getCount() {

		return fileList == null ? 0:fileList.size();
	}

	@Override
	public Object getItem(int position) {

		return fileList == null ? 0:fileList.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ImageView view = new ImageView(context);
		view.setLayoutParams(new LinearLayout.LayoutParams(Configuration.getChooserItemWidth(), Configuration.getChooserItemWidth()));
		view.setScaleType(ScaleType.FIT_XY);
		view.setBackgroundResource(R.drawable.icon_loading);
		if (selection == position) {
			view.setImageResource(R.drawable.surf_guide_border);
		}
		else {
			view.setImageDrawable(null);
		}
		return view;
	}

	/**
	 * interface for outside
	 */
	public void recycle() {
//		try {
//			int n = 1/0;
//		} catch (Exception e) {
//			StringBuffer buffer = new StringBuffer();
//			for (StackTraceElement element:e.getStackTrace()) {
//				buffer.append(element.toString()).append("\n");
//			}
//			Log.e(TAG, buffer.toString());
//		}
		Log.d(TAG, "recycle");
		if (cacheImageList != null) {
			for (Bitmap bitmap:cacheImageList) {
				if (bitmap != null && bitmap != defaultBitmap) {
					bitmap.recycle();
				}
			}
			cacheImageList.clear();
		}
	}

	@Override
	public synchronized void refreshData(int pos1, int pos2) {
		Log.d(TAG, "refreshData[" + pos1 + "," + pos2 + "]");
		if (fileList == null) {
			return;
		}
		if (pos1 < 0) {
			pos1 = 0;
		}
		if (pos2 >= fileList.size()) {
			pos2 = fileList.size() - 1;
		}
		if (cacheImageList == null || cacheImageList.size() == 0) {
			initCacheImageList();
		}
		Bitmap bitmap = null;
		for (int i = pos1; i <= pos2; i ++) {
			bitmap = cacheImageList.get(i);
			if (bitmap == null) {
				//getSpictureItem是从缓存池里获得的(key是图片的路径)，如果前后有相同路径的图片，就会造成try to use recycled bitmap error
				//这里的图片缓存完全靠cacheImageList实现
				//bitmap = PictureManagerUpdate.getInstance().getSpictureItem(fileList.get(i), context);
				bitmap = PictureManagerUpdate.getInstance().createSpictureItem(fileList.get(i), context);
				cacheImageList.set(i, bitmap);
			}
		}
	}

	@Override
	public void recycle(LinearLayout container, int pos1, int pos2) {
		Log.d(TAG, "recycle[" + pos1 + "," + pos2 + "]");
		if (fileList == null) {
			return;
		}
		ImageView view = null;
		Bitmap bitmap = null;
		if (pos1 < 0) {
			pos1 = 0;
		}
		if (pos2 >= fileList.size()) {
			pos2 = fileList.size() - 1;
		}
		for (int i = pos1; i <= pos2; i ++) {
			bitmap = cacheImageList.get(i);
			if (bitmap != null) {
				view = (ImageView) container.getChildAt(i);
				view.setBackgroundResource(R.drawable.icon_loading);
				if (bitmap != defaultBitmap) {
					bitmap.recycle();
				}
				cacheImageList.set(i, null);
			}
		}
	}

	@Override
	public void onRefreshOver(LinearLayout container, int pos1, int pos2) {
		Log.d(TAG, "onRefreshOver[" + pos1 + "," + pos2 + "]");
		if (fileList == null) {
			return;
		}
		ImageView view = null;
		Bitmap bitmap = null;
		if (pos1 < 0) {
			pos1 = 0;
		}
		if (pos2 >= fileList.size()) {
			pos2 = fileList.size() - 1;
		}
		if (cacheImageList == null || cacheImageList.size() == 0) {
			initCacheImageList();
		}
		for (int i = pos1; i <= pos2; i ++) {
			view = (ImageView) container.getChildAt(i);
			bitmap = cacheImageList.get(i);
			if (bitmap == null || bitmap.isRecycled()) {
				view.setBackgroundResource(R.drawable.icon_loading);
			}
			else {
				view.setBackground(new BitmapDrawable(context.getResources(), cacheImageList.get(i)));
			}
		}
	}

}
