package com.king.app.fileencryption.timeline.update;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.filemanager.entity.FileBean;
import com.king.app.fileencryption.open.image.ImageFactory;
import com.king.app.fileencryption.timeline.controller.IndicatorController;
import com.king.app.fileencryption.timeline.controller.TimeLineController;
import com.king.app.fileencryption.timeline.gridhelper.ImageShowManager;
import com.king.app.fileencryption.tool.Encrypter;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersBaseAdapter;

public class TimeLineAdapter extends BaseAdapter implements
		StickyGridHeadersBaseAdapter, OnClickListener {

	public interface OnHeadImageClickListener {
		public void onHeadImageClicked(View parent, View view, int position);
	}

	private Context mContext;
	private TimeLineController timeLineController;
	private List<FileBean> fileBeanList;
	private List<String> bkList;

	private IndicatorController indicatorController;

	private Random random;

	private Encrypter encrypter;

	private ImageShowManager imageManager;

	private OnHeadImageClickListener onHeadImageClickListener;

	public TimeLineAdapter(Context context, OnHeadImageClickListener listener
			, TimeLineController timeLineController) {
		mContext = context;
		onHeadImageClickListener = listener;
		this.timeLineController = timeLineController;

		fileBeanList = timeLineController.getFileBeanList();
		if (Constants.FEATURE_TIMELINE_ENABLE_BK) {
			bkList = timeLineController.getIndicatorBkList();
		}
		random = new Random();
		encrypter = EncrypterFactory.create();
		imageManager = ImageShowManager.from((Activity) context);
		indicatorController = new IndicatorController(context);
	}

	@Override
	public int getCount() {

		return fileBeanList == null ? 0:fileBeanList.size();
	}

	@Override
	public Object getItem(int position) {
		return fileBeanList == null ? 0:fileBeanList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_timeline_grid_item, null);
			holder = new ItemViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.timeline_grid_item_image);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

//		Bitmap bitmap = ImageFactory.getInstance(encrypter).createEncryptedThumbnail(list.get(position).getPath(), 200*200, null);
//		if (bitmap == null) {
//			holder.image.setImageResource(R.drawable.icon_loading);
//		}
//		else {
//			holder.image.setImageBitmap(bitmap);
//		}

		String path = fileBeanList.get(position).getPath();
		// 首先检测是否已经有线程在加载同样的资源（如果则取消较早的），避免出现重复加载
		if (cancelPotentialLoad(path, holder.image)) {
			AsyncLoadImageTask task = new AsyncLoadImageTask(holder.image);
			holder.image.setImageDrawable(new LoadingDrawable(task));
			task.execute(path);
		}
		return convertView;
	}

	@Override
	public int getCountForHeader(int header) {
		String tag = timeLineController.getHeaderList().get(header);
		int count = timeLineController.getContentMap().get(tag).size();
		return count;
	}

	@Override
	public int getNumHeaders() {
		return timeLineController.getHeaderList().size();
	}

	@Override
	/**
	 * 经调试发现，非衔接情况和普通的listView模式相同
	 * 衔接情况较特殊，举例第index=2个衔接第index=1个时，当第2个把第1个推至刚好看不见时，这时候会调用一次position=2,
	 * 在此之后，一直到第3个应该出现和接替第2个之前，会无数次调用position=0，也就是在header的固定阶段都有调用position等于0的操作
	 * 不知这是什么原因，但是固定的确实时正确的index，只是不知道滑到过程为何会一直调用position=0
	 */
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (Constants.FEATURE_TIMELINE_ENABLE_BK) {
			if (position == 0) {
				if (convertView == null) {
					// Sticky grid view的bug，第一项一开始滑动后总是显示空白
					// 用假数据占据第一个header
					TextView textView = new TextView(mContext);
					textView.setVisibility(View.GONE);
					return textView;
				}
				return convertView;
			}
		}

		if (Constants.DEBUG) {
			Log.d(Constants.LOG_ADAPTER, "getHeaderView " + position);
		}
		HeaderViewHolder holder = null;
		//调试中发现position=1时执行了setTag，但是紧接着下一次执行position=1，getTag还是null，所以这里再加个判断
		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_timeline_grid_header, null);
			holder = new HeaderViewHolder();
			holder.bkView = (ImageView) convertView.findViewById(R.id.timeline_indicator_bk);
			holder.imageView = (ImageView) convertView.findViewById(R.id.timeline_indicator_image);
			holder.time = (TextView) convertView.findViewById(R.id.timeline_indicator_text);
			holder.number = (TextView) convertView.findViewById(R.id.timeline_indicator_num);
			convertView.setTag(holder);
		}
		else {
			holder = (HeaderViewHolder) convertView.getTag();
		}

		List<FileBean> list = timeLineController.getContentMap()
				.get(timeLineController.getHeaderList().get(position));

		String time = timeLineController.getHeaderList().get(position);
		holder.timeTag = time;
		holder.time.setText(time);
		holder.number.setText(list.size() + "张");

		int index = Math.abs(random.nextInt()) % list.size();

		holder.imageView.setTag(convertView);
		holder.imageView.setOnClickListener(this);

		if (Constants.FEATURE_TIMELINE_ENABLE_BK) {
			int bkIndex = Math.abs(random.nextInt()) % bkList.size();

			indicatorController.loadIndicator(time, holder.imageView, list.get(index).getPath()
					, holder.bkView, bkList.get(bkIndex), true);
		}
		else {
			if (position == 0) {
				//缓存第1个图片，否则因为异步原因和sticky gridview的bug，第1个header总是显示不出来图片
				Bitmap bitmap = indicatorController.getFirstHeadBitmap(list.get(index).getPath());
				holder.imageView.setImageBitmap(bitmap);
			}
			else {
				indicatorController.loadIndicator(time, holder.imageView, list.get(index).getPath(), true);
			}
		}
		return convertView;
	}

	private class HeaderViewHolder {

		ImageView bkView;
		ImageView imageView;
		TextView time;
		TextView number;
		String timeTag;
	}

	private class ItemViewHolder {
		ImageView image;
	}

	/**
	 * 判断当前的imageview是否在加载相同的资源
	 *
	 * @param url
	 * @param imageview
	 * @return
	 */
	private boolean cancelPotentialLoad(String url, ImageView imageview) {

		AsyncLoadImageTask loadImageTask = getAsyncLoadImageTask(imageview);
		if (loadImageTask != null) {
			String bitmapUrl = loadImageTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				loadImageTask.cancel(true);
			} else {
				// 相同的url已经在加载中.
				return false;
			}
		}
		return true;
	}

	/**
	 * 负责加载图片的异步线程
	 *
	 * @author Administrator
	 *
	 */
	class AsyncLoadImageTask extends AsyncTask<String, Void, Bitmap> {

		private final WeakReference<ImageView> imageViewReference;
		private String url = null;

		public AsyncLoadImageTask(ImageView imageview) {
			super();
			imageViewReference = new WeakReference<ImageView>(imageview);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			/**
			 * 具体的获取bitmap的部分，流程： 从内存缓冲区获取，如果没有向硬盘缓冲区获取，如果没有从sd卡/网络获取
			 */

			Bitmap bitmap = null;
			this.url = params[0];
			if (url == null) {
				return null;
			}

			// 从内存缓存区域读取
			bitmap = imageManager.getBitmapFromMemory(url);
			if (bitmap != null) {
				Log.d("dqq", "return by 内存");
				return bitmap;
			}
			// 从硬盘缓存区域中读取
			bitmap = imageManager.getBitmapFormDisk(url);
			if (bitmap != null) {
				imageManager.putBitmapToMemery(url, bitmap);
				Log.d("dqq", "return by 硬盘");
				return bitmap;
			}

			// 没有缓存则从原始位置读取
			bitmap = ImageFactory.getInstance(encrypter).createEncryptedThumbnail(url, 200*200, null);
			imageManager.putBitmapToMemery(url, bitmap);
			imageManager.putBitmapToDisk(url, bitmap);
			Log.d("dqq", "return by 原始读取");
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap resultBitmap) {
			if (isCancelled()) {
				// 被取消了
				resultBitmap = null;
			}
			if (imageViewReference != null) {
				ImageView imageview = imageViewReference.get();
				AsyncLoadImageTask loadImageTask = getAsyncLoadImageTask(imageview);
				if (this == loadImageTask) {
					imageview.setImageDrawable(null);
					imageview.setImageBitmap(resultBitmap);
				}

			}

			super.onPostExecute(resultBitmap);
		}
	}

	/**
	 * 根据imageview，获得正在为此imageview异步加载数据的函数
	 *
	 * @param imageview
	 * @return
	 */
	private AsyncLoadImageTask getAsyncLoadImageTask(ImageView imageview) {
		if (imageview != null) {
			Drawable drawable = imageview.getDrawable();
			if (drawable instanceof LoadingDrawable) {
				LoadingDrawable loadedDrawable = (LoadingDrawable) drawable;
				return loadedDrawable.getLoadImageTask();
			}
		}
		return null;
	}

	/**
	 * 记录imageview对应的加载任务，并且设置默认的drawable
	 *
	 * @author Administrator
	 *
	 */
	public static class LoadingDrawable extends ColorDrawable {
		// 引用与drawable相关联的的加载线程
		private final WeakReference<AsyncLoadImageTask> loadImageTaskReference;

		public LoadingDrawable(AsyncLoadImageTask loadImageTask) {
			super(Color.WHITE);
			loadImageTaskReference = new WeakReference<AsyncLoadImageTask>(
					loadImageTask);
		}

		public AsyncLoadImageTask getLoadImageTask() {
			return loadImageTaskReference.get();
		}
	}


	@Override
	public void onClick(View v) {
		View view = (View) v.getTag();
		if (onHeadImageClickListener != null) {
			onHeadImageClickListener.onHeadImageClicked(view, v, 0);
		}
	}

}
