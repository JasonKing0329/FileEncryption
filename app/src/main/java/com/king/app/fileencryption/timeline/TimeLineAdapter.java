package com.king.app.fileencryption.timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.filemanager.entity.FileBean;
import com.king.app.fileencryption.thumbfolder.ShowImageDialog;
import com.king.app.fileencryption.timeline.controller.TimeLineController;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@Deprecated
public class TimeLineAdapter extends BaseAdapter {

	private Context mContext;
	private TimeLineController controller;
	private Random random;

	private List<ViewHolder> holderList;

	/**
	 *
	 * @param context
	 * @param controller
	 */
	public TimeLineAdapter(Context context, TimeLineController controller) {
		mContext = context;
		this.controller = controller;
		random = new Random();
		holderList = new ArrayList<TimeLineAdapter.ViewHolder>();
	}

	@Override
	public int getCount() {
		int count = controller.getGroupList().size();
		if (count > holderList.size()) {
			for (int i = holderList.size(); i < count; i ++) {
				holderList.add(null);
			}
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		return controller.getGroupList().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_timeline_item, null);
			holder = new ViewHolder();
			holder.gridView = (NoScrollGridView) convertView.findViewById(R.id.timeline_item_gridview);
			holder.indicator = convertView.findViewById(R.id.timeline_grid_indicator);
			holder.imageView = (ImageView) convertView.findViewById(R.id.timeline_grid_indicator_image);
			holder.time = (TextView) convertView.findViewById(R.id.timeline_grid_indicator_text);
			holder.number = (TextView) convertView.findViewById(R.id.timeline_grid_indicator_num);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		holderList.set(position, holder);
		holder.timeTag = controller.getGroupList().get(position).get("time");

		List<FileBean> list = controller.getContentMap().get(holder.timeTag);
		if (list == null) {
			controller.loadFileBeans(holder.timeTag);
			list = controller.getContentMap().get(holder.timeTag);
		}

		// 很奇怪这样就会出现从第4个开始重复第一个开始
//		if (holder.adapter == null) {
//			holder.adapter = new TimeLineGridAdapter(mContext, list);
//			holder.gridView.setAdapter(holder.adapter);
//		}
//		else {
//			holder.adapter.notifyDataSetChanged();
//		}

		holder.flag = ViewHolder.FLAG_TIME_NORMAL;
		if (holder.timeTag.contains(":")) {
			if (holder.timeTag.split(":")[1].equals("0")) {
				holder.flag = ViewHolder.FLAG_TIME_FIRSTPAGE;
			}
			else {
				holder.flag = ViewHolder.FLAG_TIME_PAGE;
			}
		}

		if (Constants.DEBUG) {
			Log.d(Constants.LOG_ADAPTER, "getView " + position);
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < holderList.size(); i ++) {
				if (holderList.get(i) == null) {
					break;
				}
				else {
					buffer.append("flag" + i + ":" + holderList.get(i).flag + ", ");
				}
			}
			Log.d(Constants.LOG_ADAPTER, buffer.toString());
		}

		if (holder.flag == ViewHolder.FLAG_TIME_PAGE) {// 该日期被拆分
			holder.indicator.setVisibility(View.GONE);
		}
		else {
			holder.indicator.setVisibility(View.VISIBLE);
			holder.indicatorIndex = Math.abs(random.nextInt()) % list.size();

			new LoadImageTask(holder.imageView).execute(list.get(holder.indicatorIndex).getPath());

			if (holder.flag == ViewHolder.FLAG_TIME_FIRSTPAGE) {
				holder.time.setText(holder.timeTag.split(":")[0]);
			}
			else {
				holder.time.setText(holder.timeTag);
			}

			//第一页要显示该日期内全部图片张数
			holder.number.setText(controller.getGroupList().get(position).get("count") + " items");
		}

		holder.adapter = new TimeLineGridAdapter(mContext, list);
//		AlphaInAnimationAdapter adapter = new AlphaInAnimationAdapter(holder.adapter);
//		adapter.setAbsListView(holder.gridView);
//		holder.gridView.setAdapter(adapter);
		holder.gridView.setAdapter(holder.adapter);
		final int listIndex = position;
		holder.gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				ShowImageDialog dialog = new ShowImageDialog(mContext, null, 0);
				String key = controller.getGroupList().get(listIndex).get("time");
				dialog.setImagePath(controller.getContentMap().get(key).get(position).getPath());
				dialog.show();
			}
		});
		return convertView;
	}

	private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

		private ImageView imageView;
		public LoadImageTask(ImageView imageView) {
			this.imageView = imageView;
		}
		@Override
		protected Bitmap doInBackground(String... params) {
			// 不能用getOrderCircleCover
			Bitmap bitmap = PictureManagerUpdate.getInstance().createCircleBitmap(
					params[0], mContext);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			imageView.setImageBitmap(bitmap);
			super.onPostExecute(bitmap);
		}

	}
	public static class ViewHolder {

		public static final int FLAG_TIME_NORMAL = 0;
		public static final int FLAG_TIME_FIRSTPAGE = 1;
		public static final int FLAG_TIME_PAGE = 2;

		int flag;
		ImageView imageView;
		TextView time;
		TextView number;
		View indicator;
		NoScrollGridView gridView;
		TimeLineGridAdapter adapter;
		int indicatorIndex;
		String timeTag;
	}

	public int getIndicatorIndex(int position) {
		return holderList.get(position).indicatorIndex;
	}

	public View getIndicator(int position) throws IndexOutOfBoundsException {
		return holderList.get(position).indicator;
	}

	public List<ViewHolder> getHolderList() {
		return holderList;
	}

//	public View getLatestIndicator(int firstVisibleItem, int top, int height) {
//		View current = holderList.get(firstVisibleItem - 1).indicator;
//		View next = null;
//		if (firstVisibleItem < holderList.size()) {
//			next = holderList.get(firstVisibleItem).indicator;
//		}
//		if (next != null) {
//			int[] locCur = new int[2];
//			int[] locNext = new int[2];
//			current.getLocationOnScreen(locCur);
//			next.getLocationOnScreen(locNext);
//			if (locCur[1] > loc) {
//				
//			}
//		}
//		return current;
//	}
}
