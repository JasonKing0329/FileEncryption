package com.king.app.fileencryption.surf;

import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.surf.RecycleAdapterLoadController.ImageProvider;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * @author JingYang
 * @version create time：2016-1-28 上午9:54:20
 *
 */
public class GalleryAdapter extends Adapter<GalleryAdapter.ViewHolder>
		implements OnClickListener, OnLongClickListener, ImageProvider{

	private final boolean DEBUG = false;
	private final String TAG = "GalleryAdapter";
	private Context mContext;
	private List<String> mList;
	private int selection;

	private RecycleAdapterLoadController loadController;

	public interface OnGalleryClickListener {
		public void onGalleryItemClick(View view, int position);
		public void onGalleryItemLongClick(View view, int position);
	}
	private OnGalleryClickListener galleryListener;

	public GalleryAdapter(Context context, List<String> list) {
		mContext = context;
		mList = list;
		selection = 0;
		loadController = new RecycleAdapterLoadController(this);
	}

	public void setSelection(int position) {
		if (selection != position) {
			notifyItemChanged(selection);
			selection = position;
			notifyItemChanged(position);
		}
	}

	public void setGalleryListener(OnGalleryClickListener listener) {
		this.galleryListener = listener;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {

		public ImageView image;
		public ImageView border;
		public int position;
		public ViewHolder(View view) {
			super(view);
		}

	}

	@Override
	public int getItemCount() {
		return mList == null ? 0:mList.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.position = position;
		holder.image.setTag(position);
		if (DEBUG) {
			Log.d(TAG, "onBindViewHolder " + position);
		}
		if (position == selection) {
			holder.border.setVisibility(View.VISIBLE);
		}
		else {
			holder.border.setVisibility(View.GONE);
		}

		if (galleryListener != null) {
			holder.image.setOnClickListener(this);
			holder.image.setOnLongClickListener(this);
		}

		loadController.onLoad(holder.image, position, mList.get(position));
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup group, int viewType) {
		if (DEBUG) {
			Log.d(TAG, "onCreateViewHolder " + viewType);
		}
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.adapter_surf_gallery_item, group, false);
		ViewHolder holder = new ViewHolder(view);
		holder.border = (ImageView) view.findViewById(R.id.surf_gallery_item_img_border);
		holder.image = (ImageView) view.findViewById(R.id.surf_gallery_item_img);
		return holder;
	}

	@Override
	public void onViewRecycled(ViewHolder holder) {
		if (DEBUG) {
			Log.d(TAG, "onViewRecycled " + holder.position);
		}
		loadController.onRecycle(holder.image, holder.position);
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

	@Override
	public boolean onLongClick(View v) {

		int position = (Integer) v.getTag();
		galleryListener.onGalleryItemLongClick(v, position);
		return true;
	}

	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		galleryListener.onGalleryItemClick(v, position);
	}

	@Override
	public Bitmap createBitmap(String path) {
		Bitmap bitmap = PictureManagerUpdate.getInstance().createSpictureItem(
				path, mContext);
		return bitmap;
	}

}
