package com.king.app.fileencryption.thumbfolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;

public class GridAdapterProvider {

	private final String TAG = "GridAdapterProvider";
	private Context mContext;
	private GridView gridView;
	private List<String> imageFileList;
	private List<Bitmap> currentImages;
	private AlphaInAnimationAdapter adapter;
	private GridImageAdapter gridImageAdapter;

	public GridAdapterProvider(Context context, GridView view) {
		mContext = context;
		gridView = view;
	}

	public void createAdapter() {

		gridImageAdapter = new GridImageAdapter();
		adapter = new AlphaInAnimationAdapter(gridImageAdapter);
		//must call setAbsListView before setAdapter, otherwise, it'll throw IllegalStatementException
		adapter.setAbsListView(gridView);
		gridView.setAdapter(adapter);
	}

	public void refresh(List<String> fileList) {
		imageFileList = fileList;
		if (currentImages != null) {
			for (Bitmap bitmap:currentImages) {
				if (bitmap != null) {
					bitmap.recycle();
				}
			}
			currentImages.clear();
		}

		if (imageFileList != null) {
			currentImages = new ArrayList<Bitmap>();
			for (int i = 0; i < imageFileList.size(); i ++) {
				currentImages.add(null);
			}
		}
		refresh(true);
	}

	/**
	 * 如果是从actionmode模式返回普通模式就不重载动画了
	 * @param resetAnimation
	 */
	public void refresh(boolean resetAnimation) {
		if (gridImageAdapter == null) {
			Log.d(TAG, "createAdapter");
			createAdapter();
		}
		else {
			Log.d(TAG, "notifyDataSetChanged");
			if (resetAnimation) {
				adapter.reset();//不reset就不会重新加载动画
			}
			adapter.notifyDataSetChanged();
		}
	}

	public void selectAll() {
		if (gridImageAdapter != null) {
			gridImageAdapter.selectAll();
			gridImageAdapter.notifyDataSetChanged();
		}
	}

	public void deSelectAll() {
		if (gridImageAdapter != null) {
			gridImageAdapter.resetMap();
			gridImageAdapter.notifyDataSetChanged();
		}
	}

	public void showActionMode(boolean show) {
		if (gridImageAdapter != null) {
			gridImageAdapter.showActionMode(show);
		}
	}

	public List<Integer> getSelectedList() {
		List<Integer> list = null;
		if (gridImageAdapter != null) {
			HashMap<Integer, Boolean> map = gridImageAdapter.getCheckMap();
			Iterator<Integer> iterator = map.keySet().iterator();
			list = new ArrayList<Integer>();

			while (iterator.hasNext()) {
				list.add(iterator.next());
			}

			Collections.sort(list);
		}
		return list;
	}

	public void startActionMode(int position) {
		if (gridImageAdapter != null) {
			gridImageAdapter.resetMap();
			gridImageAdapter.getCheckMap().put(position, true);
			gridImageAdapter.showActionMode(true);
			gridImageAdapter.notifyDataSetChanged();
		}
	}

	public boolean isActionMode() {
		return gridImageAdapter != null && gridImageAdapter.isActionMode();
	}

	public int getCheckedNum() {
		if (gridImageAdapter != null) {
			return gridImageAdapter.getCheckMap().size();
		}
		return 0;
	}

	public int getCheckedPosition() {
		if (gridImageAdapter != null) {
			return gridImageAdapter.getCheckMap().keySet().iterator().next();
		}
		return 0;
	}

	private class GridImageAdapter extends BaseAdapter {

		private HashMap<Integer, Boolean> checkMap;
		private boolean showActionMode;

		public GridImageAdapter() {
			checkMap = new HashMap<Integer, Boolean>();
		}

		public void resetMap() {
			checkMap.clear();
		}

		public void selectAll() {
			for (int i = 0; i < imageFileList.size(); i ++) {
				checkMap.put(i, true);
			}
		}

		public HashMap<Integer, Boolean> getCheckMap() {
			return checkMap;
		}

		public void showActionMode(boolean show) {
			showActionMode = show;
		}

		public boolean isActionMode() {
			return showActionMode;
		}

		@Override
		public int getCount() {

			return currentImages == null ? 0:currentImages.size();
		}

		@Override
		public Object getItem(int position) {

			return currentImages == null ? position:currentImages.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			convertView = LayoutInflater.from(mContext).inflate(R.layout.thumb_image_item, null);
			ImageView view = (ImageView) convertView.findViewById(R.id.thumb_item_image);
			CheckBox cBox = (CheckBox) convertView.findViewById(R.id.thumb_item_checkbox);
			if (showActionMode) {
				cBox.setVisibility(View.VISIBLE);
				Boolean status = checkMap.get(position);
				if (status == null || !status) {
					cBox.setChecked(false);
				}
				else {
					cBox.setChecked(true);
				}
				cBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0, boolean checked) {
						if (checked) {
							checkMap.put(position, true);
						}
						else {
							checkMap.remove(position);
						}
					}
				});
			}
			else {
				cBox.setVisibility(View.GONE);
			}
			//由于这里是为listview适配，layoutparams必须用ListView的，还用会报出
			//ClassCastException: ...LinearLayout.LayoutParams cant be casted to ...ListView.LayoutParams
			//view.setLayoutParams(new LinearLayout.LayoutParams(imageViewWidth, imageViewWidth));

			//这里的参数对grid item起决定性作用
			//int width = (int)getResources().getDimension(R.dimen.thumbfolder_image_width);
			//view.setLayoutParams(new GridView.LayoutParams(width, width));

			//view.setPadding(15, 15, 15, 15);
			//view.setScaleType(ScaleType.FIT_XY);
			//view.setBackgroundResource(R.drawable.spicture_chooser_border_choose);
			//view.setImageBitmap(imageList.get(position));
			//Bitmap bitmap = PictureManager.getInstance().getChooserItem(fileList.get(position));
			Bitmap bitmap = currentImages.get(position);
			if (bitmap == null) {
				bitmap = PictureManagerUpdate.getInstance().createImage(imageFileList.get(position), 200*200
						, mContext, 1);
				if (bitmap == null) {
					bitmap = PictureManagerUpdate.getInstance().getDefaultOrderCover(mContext);
				}
				currentImages.set(position, bitmap);
			}
			//view.setBackground(new BitmapDrawable(mContext.getResources(), bitmap));
			view.setImageBitmap(bitmap);

			if (showItemLongClickAnim == position) {
				Log.d(TAG, "showItemLongClickAnim");
				convertView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.thumb_item_longclick));
				showItemLongClickAnim = -1;
			}
			return convertView;
		}

	}

	public void removeImage(int index) {
		if (currentImages != null) {
			try {
				currentImages.remove(index);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
				Log.d(TAG, "IndexOutOfBoundsException index");
			}
		}
	}

	private int showItemLongClickAnim = -1;
	public void notifyShowAnimation(int position) {
		showItemLongClickAnim = position;
	}

}
