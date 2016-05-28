package com.king.app.fileencryption.sorder.view;

import java.util.List;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.sorder.entity.SOrder;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SOrderGridAdapter extends BaseAdapter {

	public enum CoverMode {
		SINGLE, CASCADE, CASCADE_ROTATE, GRID
	}
	
	private Context context;
	private List<SOrder> sorderList;
	private int coverWidth;
	private CoverMode mCoverMode;
	private Bitmap defaultCover;
	private int multiNum_cascade;
	
	private List<List<Bitmap>> multiCoverList;
	
	public SOrderGridAdapter(Context context, List<SOrder> orders) {
		this.context = context;
		sorderList = orders;
		coverWidth = context.getResources().getDimensionPixelSize(R.dimen.sorder_grid_cover_width);
		mCoverMode = CoverMode.SINGLE;
		defaultCover = PictureManagerUpdate.getInstance().getDefaultOrderCover(context);
		multiNum_cascade = SettingProperties.getCascadeCoverNumber(context);
	}

	public void setMultiCoverList(List<List<Bitmap>> multiCoverList) {
		this.multiCoverList = multiCoverList;
	}

	public void setSorderList(List<SOrder> sorderList) {
		this.sorderList = sorderList;
	}

	public List<SOrder> getSorderList() {
		return sorderList;
	}
	
	public void setCoverMode(CoverMode mode) {
		mCoverMode = mode;
	}
	
	public CoverMode getCoverMode() {
		return mCoverMode;
	}

	@Override
	public int getCount() {
		
		return sorderList == null ? 0 : sorderList.size();
	}

	@Override
	public Object getItem(int position) {

		return sorderList == null ? null : sorderList.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public void notifyDataSetChanged() {

		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		SOrder order = sorderList.get(position);
		ViewHolder holder = null;
		if (convertView == null) {
			Log.d("SOrderGridAdapter", "getView " + position + " convertView == null");
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_sorder_grid, null);
			holder = new ViewHolder();
			holder.cover = (MultiCoverView) convertView.findViewById(R.id.sorder_grid_multicover);
			holder.name = (TextView) convertView.findViewById(R.id.sorder_grid_name);
			if (Application.isLollipop()) {
				convertView.findViewById(R.id.sorder_grid_item_layout).setBackgroundResource(R.drawable.selector_order_background_l);
			}
			holder.cover.setSize(coverWidth, coverWidth);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(order.getName() + "(" + order.getItemNumber() + ")");
		
		List<Bitmap> bitmaps = null;
		if (multiCoverList != null) {
			bitmaps = multiCoverList.get(position);
		}
		setCover(holder.cover, bitmaps, position);
		//new LoadCoverTask(order, holder.cover).execute();
		return convertView;
	}

	private class ViewHolder {
		public MultiCoverView cover;
		public TextView name;
	}

	private void setCover(MultiCoverView coverView, List<Bitmap> list, int position) {

		coverView.reset();
		if (list == null || list.size() == 0) {
			if (defaultCover.isRecycled()) {
				Log.d("ScrollController", "defaultCover isRecycled " + position);
			}
			coverView.setSingleCover(defaultCover);
		}
		else if (list.size() == 1) {
			if (list.get(0) != null && list.get(0).isRecycled()) {
				Log.d("ScrollController", "list.get(0) isRecycled " + position);
			}
			coverView.setSingleCover(list.get(0));
		}
		else {
			if (mCoverMode == CoverMode.CASCADE) {
				for (Bitmap bitmap:list) {
					if (bitmap != null && bitmap.isRecycled()) {
						Log.d("ScrollController", "bitmap isRecycled " + position);
					}
					coverView.addCover(bitmap);
				}
				
				int spaceX = 0, spaceY = 0;
				if (multiNum_cascade == 2) {
					spaceX = 80;
					spaceY = 110;
				}
				else if (multiNum_cascade == 3) {
					spaceX = 60;
					spaceY = 80;
				}
				else if (multiNum_cascade == 4) {
					spaceX = 40;
					spaceY = 50;
				}
				coverView.applyStyleCascade(spaceX, spaceY, multiNum_cascade);
			}
			else if (mCoverMode == CoverMode.CASCADE_ROTATE) {
				for (Bitmap bitmap:list) {
					if (bitmap != null && bitmap.isRecycled()) {
						Log.d("ScrollController", "bitmap isRecycled " + position);
					}
					coverView.addCover(bitmap);
				}
				
				coverView.applyStyleCascadeRotate();
			}
			else if (mCoverMode == CoverMode.GRID) {
				for (Bitmap bitmap:list) {
					if (bitmap != null && bitmap.isRecycled()) {
						Log.d("ScrollController", "bitmap isRecycled " + position);
					}
					coverView.addBitmap(bitmap);
				}
				
				coverView.applyStyleGrid();
			}
		}
	}
	
	/*
	private class LoadCoverTask extends AsyncTask<Object, Object, List<Bitmap>> {

		private SOrder sOrder;
		private MultiCoverView coverView;
		public LoadCoverTask(SOrder order, MultiCoverView cover) {
			sOrder = order;
			coverView = cover;
		}

		@Override
		protected List<Bitmap> doInBackground(Object... params) {

			if (mCoverMode == CoverMode.SINGLE) {
				List<Bitmap> bitmaps = new ArrayList<Bitmap>();
				Bitmap bitmap = PictureManagerUpdate.getInstance().createSingleOrderCover(sOrder.getCoverPath());
				bitmaps.add(bitmap);
				return bitmaps;
			}
			else {
				List<String> list = null;
				if (mCoverMode == CoverMode.CASCADE) {
					list = bridge.loadSOrderCovers(sOrder, multiNum_cascade);
				}
				else {
					list = bridge.loadSOrderCovers(sOrder, multiNum);
				}
				if (list != null) {
					List<Bitmap> bitmaps = new ArrayList<Bitmap>();
					Bitmap bitmap = null;
					for (String path:list) {
						bitmap = PictureManagerUpdate.getInstance().createCascadeOrderCover(path);
						bitmaps.add(bitmap);
					}
					return bitmaps;
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
		}

		@Override
		protected void onPostExecute(List<Bitmap> list) {
			//coverView.clearBitmaps();
			if (list == null || list.size() == 0) {
				coverView.setSingleCover(defaultCover);
			}
			else if (list.size() == 1) {
				coverView.setSingleCover(list.get(0));
			}
			else {
				if (mCoverMode == CoverMode.CASCADE) {
					for (Bitmap bitmap:list) {
						coverView.addCover(bitmap);
					}
					
					int spaceX = 0, spaceY = 0;
					if (multiNum_cascade == 2) {
						spaceX = 80;
						spaceY = 110;
					}
					else if (multiNum_cascade == 3) {
						spaceX = 60;
						spaceY = 80;
					}
					else if (multiNum_cascade == 4) {
						spaceX = 40;
						spaceY = 50;
					}
					coverView.applyStyleCascade(spaceX, spaceY, multiNum_cascade);
				}
				else if (mCoverMode == CoverMode.CASCADE_ROTATE) {
					for (Bitmap bitmap:list) {
						coverView.addCover(bitmap);
					}
					
					coverView.applyStyleCascadeRotate();
				}
				else if (mCoverMode == CoverMode.GRID) {
					for (Bitmap bitmap:list) {
						coverView.addBitmap(bitmap);
					}
					
					coverView.applyStyleGrid();
				}
			}
		
		}

		@Override
		protected void onPreExecute() {
			//coverView.clearBitmaps();
			super.onPreExecute();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		
	}
	*/
}
