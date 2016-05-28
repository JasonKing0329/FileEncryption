package com.king.app.fileencryption.wall;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.controller.PictureManagerUpdate;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
@Deprecated
public class WallAdapter extends BaseAdapter implements OnClickListener {

	private final String TAG = "WallAdapter";
	private Context context;
	private List<String> pathList;
	private boolean showName;
	private ImageView.ScaleType scaleType;
	private boolean isSelectMode;
	private HashMap<Integer, Boolean> checkMap;
	
	public WallAdapter(Context context, List<String> pathList) {
		this.context = context;
		this.pathList = pathList;
		scaleType = ImageView.ScaleType.FIT_XY;
		checkMap = new HashMap<Integer, Boolean>();
	}
	
	public void setShowName(boolean show) {
		showName = show;
	}
	public void updatePathList(List<String> list) {
		pathList = list;
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
		if (path == null) {
			iv.setImageResource(R.drawable.ic_launcher);
		}
		else {
			iv.setImageBitmap(PictureManagerUpdate.getInstance().getWallItem(path, context));
			
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

}
