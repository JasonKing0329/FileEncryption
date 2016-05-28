package com.king.app.fileencryption.wall;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.EncrypterFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class WallAdapterUpdate extends BaseAdapter implements OnClickListener {

	private final String TAG = "WallAdapter";
	private Context context;
	private List<String> pathList;
	private boolean showName;
	private ImageView.ScaleType scaleType;
	private boolean isSelectMode;
	private HashMap<Integer, Boolean> checkMap;
	
	private WallResManager resManager;
	private List<ImageView> imageViewList;
	
	public WallAdapterUpdate(Context context, List<String> pathList) {
		this.context = context;

		scaleType = ImageView.ScaleType.FIT_XY;
		checkMap = new HashMap<Integer, Boolean>();

		resManager = new WallResManager(context, this);
		
		updatePathList(pathList);
		
	}

	public void setShowName(boolean show) {
		showName = show;
	}
	public void updatePathList(List<String> list) {
		pathList = list;
		if (list != null) {
			imageViewList = new ArrayList<ImageView>();
			for (int i = 0; i < list.size(); i ++) {
				imageViewList.add(null);
			}
			
			resManager.initCacheList(pathList);
		}
	}
	public void changeScaleType(ImageView.ScaleType type) {
		scaleType = type;
	}

	public void setSelectMode(boolean mode) {
		isSelectMode = mode;
	}
	
	public boolean isSelectMode() {
		return isSelectMode;
	}

	public void resetMap() {
		checkMap.clear();
	}
	
	public void selectAll() {
		for (int i = 0; i < pathList.size(); i ++) {
			checkMap.put(i, true);
		}
	}
	
	public void setChecked(int pos) {
		checkMap.put(pos, true);
	}
	
	public HashMap<Integer, Boolean> getCheckMap() {
		return checkMap;
	}
	

	@Override
	public int getCount() {

		return pathList == null ? 0:pathList.size();
	}

	@Override
	public Object getItem(int position) {

		return pathList == null ? null:pathList.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i(TAG, "getView " + position);
		convertView = LayoutInflater.from(context).inflate(R.layout.adapter_wall_items, null);
		ImageView iv = (ImageView) convertView.findViewById(R.id.wall_item_image);
		CheckBox cBox = (CheckBox) convertView.findViewById(R.id.wall_item_checkbox);
		String path = pathList.get(position);
		
		imageViewList.set(position, iv);
		Bitmap bitmap = resManager.getBitmap(position);
		if (bitmap == null) {
			iv.setImageResource(R.drawable.icon_loading);
		}
		else {
			iv.setImageBitmap(bitmap);
			
			iv.setScaleType(scaleType);
			if (showName) {
				TextView tv = (TextView) convertView.findViewById(R.id.wall_item_text);
				tv.setVisibility(View.VISIBLE);
				tv.setText(EncrypterFactory.create().decipherOriginName(new File(path)));
			}
		}
		if (isSelectMode) {
			cBox.setVisibility(View.VISIBLE);
			Boolean status = checkMap.get(position);
			if (status == null || !status) {
				cBox.setChecked(false);
			}
			else {
				cBox.setChecked(true);
			}
			cBox.setTag(position);
			//cBox.setOnCheckedChangeListener(this);

			convertView.setTag(cBox);
			convertView.setOnClickListener(this);
		}
		else {
			cBox.setVisibility(View.GONE);
			/**
			 * Fix bug:
			 * wall gallery>show file name>click or longclick item>no action
			 * >>strange issue, no idea why show file name there is no action
			 * >>try to set listener on convertview to fix the bug
			 */
			convertView.setTag(position);
			convertView.setOnClickListener(((WallActivity) context).getGridItemClickListener());
			convertView.setOnLongClickListener(((WallActivity) context).getGridItemLongClickListener());
		}
		return convertView;
	}

	@Override
	public void onClick(View v) {
		CheckBox cBox = (CheckBox) v.getTag();
		int position = (Integer) cBox.getTag();
		boolean isChecked = cBox.isChecked();
		if (isChecked) {
			cBox.setChecked(false);
			checkMap.remove(position);
		}
		else {
			cBox.setChecked(true);
			checkMap.put(position, true);
		}
	}

	public void notifyBitmapLoaded(int start, int end) {
		Log.d(TAG, "notifyBitmapLoaded [" + start + "," + end + "]");
		for (int i = start; i <= end; i ++) {
			ImageView view = imageViewList.get(i);
			if (view != null) {
				view.setImageBitmap(resManager.getBitmap(i));
				view.setScaleType(scaleType);
			}
		}
	}

	public void setVisibleRange(int start, int end, int direction) {
		Log.d(TAG, "setVisibleRange [" + start + "," + end + "]");
		resManager.setVisibleRange(start, end, pathList, direction);
	}

	public void notifyRecycled(int start, int end) {
		Log.d(TAG, "notifyRecycled [" + start + "," + end + "]");
		for (int i = start; i <= end; i ++) {
			ImageView view = imageViewList.get(i);
			if (view != null) {
				view.setImageResource(R.drawable.icon_loading);
			}
		}
	}
	
	public void resetWallRes() {
		resManager.initCacheList(pathList);
	}
	
	public void recycleResource() {
		resManager.reset();
	}
}
