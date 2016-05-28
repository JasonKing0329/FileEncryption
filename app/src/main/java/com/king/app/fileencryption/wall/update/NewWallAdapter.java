package com.king.app.fileencryption.wall.update;

import java.io.File;
import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.surf.RecycleAdapterLoadController;
import com.king.app.fileencryption.surf.RecycleAdapterLoadController.ImageProvider;
import com.nineoldandroids.view.ViewHelper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * @author JingYang
 * @version create time：2016-1-29 下午5:03:43
 *
 */
public class NewWallAdapter extends Adapter<NewWallAdapter.ViewHolder>
		implements OnClickListener, OnLongClickListener, ImageProvider, MirrorListener {

	public interface OnWallItemListener {
		public void onWallItemClick(View view, int position);
		public void onWallItemLongClick(View view, int position);
//		public void onWallItemZoom(View view);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {

		public ImageView image;
		public CheckBox check;
		public TextView name;
		/**
		 * 本属于ImageView的click/long click/touch事件全部注册给fakeZoomView。
		 * 原因在于，ImageView的缩放处理是用ViewHelper设置scale实现的，
		 * 在缩放过程中onTouch里获得的getX/Y(0/1)都发生相对变化，导致缩放过程图片抖动的情况。
		 * 为了解决这个问题，只好用一个fake view覆盖于整个item之上，用它来监听所有的点击触摸事件。
		 * 事实证明这样处理也确实更好，首先fake view没有任何实质的缩放、点击变化发生，它所监听到的
		 * 事件全部用来控制ImageView和adapter item的状态变化。
		 * 其次，在click/long click处理上也更像List/GridView的onItemClick和onItemLongClick
		 */
		public View fakeZoomView;
		public int position;

		public ViewHolder(View arg0) {
			super(arg0);
		}

	}

	private final boolean DEBUG = true;
	private final String TAG = "NewWallAdapter";
	private Context context;
	private List<String> pathList;
	private boolean showName;
	private ImageView.ScaleType scaleType;
	private boolean isSelectMode;
	private SparseBooleanArray checkMap;

	private RecycleAdapterLoadController loadController;
	private OnWallItemListener onWallItemListener;

	private int imageItemWidth, imageItemHeight;
	private ItemViewTouchListener itemViewTouchListener;

	private View mirrorLayout;
	private ImageView mirrorView;

	public NewWallAdapter(Context context, List<String> pathList) {
		this.context = context;
		this.pathList = pathList;
		scaleType = ImageView.ScaleType.FIT_XY;
		checkMap = new SparseBooleanArray();
		loadController = new RecycleAdapterLoadController(this);
		itemViewTouchListener = new ItemViewTouchListener(this);
		mirrorLayout = ((Activity) context).findViewById(R.id.wall_mirror);
		mirrorView = (ImageView) ((Activity) context).findViewById(R.id.wall_mirror_img);

		imageItemWidth = context.getResources().getDimensionPixelSize(R.dimen.wall_item_image_width);
		imageItemHeight = context.getResources().getDimensionPixelSize(R.dimen.wall_item_image_height);
	}

	public void setImageItemSize(int width, int height) {
		imageItemWidth = width;
		imageItemHeight = height;
	}

	public void setOnWallItemListener(OnWallItemListener onWallItemListener) {
		this.onWallItemListener = onWallItemListener;
	}

	@Override
	public int getItemCount() {
		return pathList == null ? 0:pathList.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {

		holder.position = position;
		String path = pathList.get(position);

		holder.image.setScaleType(scaleType);

		holder.fakeZoomView.setTag(holder);
		holder.image.setTag(holder);
		if (onWallItemListener != null) {
			//click, long click, touch事件全部注册给fakeZoomView，在itemViewTouchListener
			//也对相应变化做了处理
			holder.fakeZoomView.setOnClickListener(this);
			holder.fakeZoomView.setOnLongClickListener(this);
			holder.fakeZoomView.setOnTouchListener(itemViewTouchListener);
		}
		loadController.onLoad(holder.image, position, path);

		if (showName) {
			holder.name.setVisibility(View.VISIBLE);
			holder.name.setText(EncrypterFactory.create().decipherOriginName(new File(path)));
		}
		else {
			holder.name.setVisibility(View.GONE);
		}

		if (isSelectMode) {
			holder.check.setVisibility(View.VISIBLE);
			Boolean status = checkMap.get(position);
			if (status == null || !status) {
				holder.check.setChecked(false);
			}
			else {
				holder.check.setChecked(true);
			}
			holder.check.setTag(position);
		}
		else {
			holder.check.setVisibility(View.GONE);
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup group, int viewType) {
		View view = LayoutInflater.from(context).inflate(
				R.layout.adapter_wall_items_new, group, false);
		ViewHolder holder = new ViewHolder(view);
		holder.image = (ImageView) view.findViewById(R.id.wall_item_image);
		holder.check = (CheckBox) view.findViewById(R.id.wall_item_checkbox);
		holder.check.setEnabled(false);
		holder.name = (TextView) view.findViewById(R.id.wall_item_text);
		holder.fakeZoomView = view.findViewById(R.id.wall_item_zoom_fake);

		//通过调试发现是RelativeLayout.LayoutParams
		RelativeLayout.LayoutParams params = new LayoutParams(imageItemWidth, imageItemHeight);
		holder.image.setLayoutParams(params);

		params = new LayoutParams(imageItemWidth, imageItemHeight);
		holder.fakeZoomView.setLayoutParams(params);

		params = (LayoutParams) holder.name.getLayoutParams();
		params.width = imageItemWidth;
		holder.name.setLayoutParams(params);
		return holder;
	}

	@Override
	public void onViewRecycled(ViewHolder holder) {
		if (!disableImageRecycle) {
			if (DEBUG) {
				Log.d(TAG, "onViewRecycled " + holder.position);
			}
			loadController.onRecycle(holder.image, holder.position);
		}
		super.onViewRecycled(holder);
	}

	/**
	 * don't direct call notifyItemRemoved, call this method to change cache reference about bitmap
	 * 通知删除同时通知loadController有增删操作发生
	 * 因为position的改变导致缓存的bitmap关联的position改变
	 * @param index
	 */
	public void notifyRemoved(int index) {
		notifyItemRemoved(index);
		loadController.notifyRemoved(index);
	}

	public void setShowName(boolean show) {
		showName = show;
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
		if (DEBUG) {
			Log.d(TAG, "setChecked " + pos);
		}
		if (checkMap.get(pos)) {
			checkMap.delete(pos);
		}
		else {
			checkMap.put(pos, true);
		}
		notifyItemChanged(pos);
	}

	public SparseBooleanArray getCheckMap() {
		return checkMap;
	}

	@Override
	public void onClick(View v) {
		if (DEBUG) {
			Log.d(TAG, "onClick");
		}
		ViewHolder tag = (ViewHolder) v.getTag();
		if (onWallItemListener != null) {
			onWallItemListener.onWallItemClick(v, tag.position);
		}
	}

	private boolean disableImageRecycle;
	@Override
	public boolean onLongClick(View v) {
		if (DEBUG) {
			Log.d(TAG, "onLongClick");
		}

		disableImageRecycle();

		ViewHolder tag = (ViewHolder) v.getTag();
		if (onWallItemListener != null) {
			onWallItemListener.onWallItemLongClick(v, tag.position);
		}
		return true;
	}

	/**
	 * 只要调用了notifyDataSetChanged，RecycleView会执行adapter的onRecycle(index)方法将视图内所有
	 * view都recycle。
	 * 所以当发生长按进入select模式以及按返回退出selectMode需要避免这期间的图片回收造成
	 * 这两个过程中所有图片重新加载，视觉效果会相当不好
	 *
	 * call this method when notifyDataSetChanged is called and current items are not changed
	 */
	public void disableImageRecycle() {
		disableImageRecycle = true;
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				disableImageRecycle = false;
			}
		}, 1000);

	}

	@Override
	public Bitmap createBitmap(String path) {
		Bitmap bitmap = PictureManagerUpdate.getInstance().createWallItem(
				path, context);
		return bitmap;
	}

	@Override
	public void startMirror(View view) {
		ViewHolder tag = (ViewHolder) view.getTag();
		Bitmap bitmap = loadController.getBitmap(tag.position);
		if (bitmap != null) {
			mirrorView.setImageBitmap(bitmap);
			int[] pos = new int[2];
			view.getLocationOnScreen(pos);

			RelativeLayout.LayoutParams params = new LayoutParams(imageItemWidth, imageItemHeight);
			params.leftMargin = pos[0];
			params.topMargin = pos[1];
			mirrorView.setScaleType(scaleType);
			mirrorView.setLayoutParams(params);

			mirrorView.post(new Runnable() {

				@Override
				public void run() {
					mirrorLayout.setVisibility(View.VISIBLE);
				}
			});
		}
	}

	@Override
	public void cancelMirror(View view) {
		closeMirror();
	}

	@Override
	public void endMirror(View view) {
		Log.d(TAG, "endMirror");

		ViewHolder tag = (ViewHolder) view.getTag();

		//这种方法是直接用mirrorLayout固定显示，可行
		//只不过为了用到ShowImageDialog已经封好的功能，还是调用ShowImageDialog
//		ViewHelper.setScaleX(mirrorView, 1);
//		ViewHelper.setScaleY(mirrorView, 1);
//
//		RelativeLayout.LayoutParams params = new LayoutParams(
//				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		mirrorView.setLayoutParams(params);
//		mirrorView.setScaleType(ScaleType.FIT_CENTER);
//		mirrorView.setImageBitmap(loadController.getBitmap(tag.position));
//
//		Bitmap bitmap = PictureManagerUpdate.getInstance()
//				.createHDBitmap(pathList.get(tag.position));
//		mirrorView.setImageBitmap(bitmap);

		((NewWallActivity) context).showImageWithDialog(pathList.get(tag.position));
		//延时200正好可以无缝衔接
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				closeMirror();
			}
		}, 200);
	}

	@Override
	public void processMirror(View view, float scale) {
		Log.d(TAG, "processMirror scale=" + scale);
		ViewHelper.setScaleX(mirrorView, scale);
		ViewHelper.setScaleY(mirrorView, scale);


		if (scale > 0) {
			int alpha = (int) (255 * (scale / ItemViewTouchListener.ZOOM_MAX));
			int color = Color.argb(alpha, 0, 0, 0);
			mirrorLayout.setBackgroundColor(color);
		}
		else {
			mirrorLayout.setBackgroundColor(Color.TRANSPARENT);
		}
	}

	public boolean isMirrorMode() {
		return mirrorLayout.getVisibility() == View.VISIBLE;
	}

	public void closeMirror() {
		ViewHelper.setScaleX(mirrorView, 1);
		ViewHelper.setScaleY(mirrorView, 1);
		RelativeLayout.LayoutParams params = new LayoutParams(imageItemWidth, imageItemHeight);
		mirrorView.setLayoutParams(params);

		mirrorLayout.setVisibility(View.GONE);
		mirrorView.setImageBitmap(null);
	}
}
