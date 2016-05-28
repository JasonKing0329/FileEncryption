package com.king.app.fileencryption.surf;

import com.king.app.fileencryption.R;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.widget.ImageView;

/**
 * @author JingYang
 * @version create time：2016-1-28 上午11:30:14
 *
 */
public class RecycleAdapterLoadController {

	public interface ImageProvider {
		public Bitmap createBitmap(String path);
	}

	private final boolean DEBUG = true;
	private final String TAG = "GalleryLoadController";

	private ImageProvider imageProvider;
	/**
	 * 当前缓存的所有图片
	 */
	private SparseArray<Bitmap> imageMap;
	/**
	 * 记录已回收的位置，防止异步操作中给已回收的imageView再设置bitmap
	 */
	private SparseBooleanArray recycleTags;

	private int debugCountLoad, debugCountRecycle, debugCountBgr, debugCountPost;

	public RecycleAdapterLoadController(ImageProvider provider) {
		imageProvider = provider;
		imageMap = new SparseArray<Bitmap>();
		recycleTags = new SparseBooleanArray();
	}

	/**
	 * 回收图片并记录已回收的位置，防止异步操作中给已回收的imageView再设置bitmap
	 * @param imageView
	 * @param position
	 */
	public void onRecycle(ImageView imageView, int position) {
		if (DEBUG) {
			Log.d(TAG, "onRecycle " + position);
			debugCountRecycle ++;
		}

		imageView.setImageBitmap(null);
		Bitmap bitmap = imageMap.get(position);
		if (bitmap != null) {
			bitmap.recycle();
			imageMap.remove(position);
		}

		recycleTags.put(position, true);
	}

	/**
	 * 加载adapter position处的图片
	 * 判断缓存中是否已有，没有则重新加载
	 * @param imageView
	 * @param position
	 * @param path
	 */
	public void onLoad(ImageView imageView, int position, String path) {
		if (DEBUG) {
			Log.d(TAG, "onLoad " + position);
			debugCountLoad ++;
		}
		recycleTags.put(position, false);
		Bitmap bitmap = imageMap.get(position);

		//缓存中有图片则直接加载图片
		//Bitmap是靠position来关联的，但是当adapter发生了增删操作后，position对应的bitmap是不一样的
		//这种情况下也要重新加载
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		}
		else {
//			imageView.setImageResource(R.drawable.icon_loading);
			imageView.setImageBitmap(null);
			new LoadImageTask(imageView, position).execute(path);
		}
	}

	/**
	 * 删除操作发生需要通知管理关系变化，否则会造成从position获取的图片不是实际上要显示的图片
	 * @param index
	 */
	public void notifyRemoved(int index) {
		SparseArray<Bitmap> newMap = new SparseArray<Bitmap>();
		imageMap.remove(index);
		for (int i = 0; i < imageMap.size(); i ++) {
			if (imageMap.keyAt(i) > index) {
				newMap.put(imageMap.keyAt(i) - 1, imageMap.valueAt(i));
			}
			else {
				newMap.put(imageMap.keyAt(i), imageMap.valueAt(i));
			}
		}
		imageMap.clear();
		imageMap = newMap;
	}

	/**
	 * 增加操作发生需要通知管理关系变化，否则会造成从position获取的图片不是实际上要显示的图片
	 * @param index
	 */
	public void notifyInserted(int index) {
		SparseArray<Bitmap> newMap = new SparseArray<Bitmap>();
		for (int i = 0; i < imageMap.size(); i ++) {
			if (imageMap.keyAt(i) >= index) {
				newMap.put(imageMap.keyAt(i) + 1, imageMap.valueAt(i));
			}
			else {
				newMap.put(imageMap.keyAt(i), imageMap.valueAt(i));
			}
		}
		imageMap.clear();
		imageMap = newMap;
	}

	/**
	 * 异步加载图片，由于RecyclerView的快速滑动因素
	 * 滑动过程中有的已发起加载图片请求的imageView已经被回收了
	 * 这时候就要避免再给已回收的imageView设置bitmap
	 * @author JingYang
	 *
	 */
	private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

		private ImageView imageView;
		private int position;
		private String path;

		public LoadImageTask(ImageView imageView, int position) {
			this.imageView = imageView;
			this.position = position;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = null;
			if (!recycleTags.get(position)) {
				if (DEBUG) {
					Log.d(TAG, "doInBackground " + position);
					debugCountBgr ++;
				}
				path = params[0];
				bitmap = imageProvider.createBitmap(path);
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (!recycleTags.get(position)) {
				if (DEBUG) {
					Log.d(TAG, "onPostExecute " + position);
					debugCountPost ++;
				}
				if (result == null) {
					imageView.setImageResource(R.drawable.ic_launcher);
				}
				else {
					imageView.setImageBitmap(result);
					imageMap.put(position, result);
				}
			}
			if (DEBUG) {
				Log.e(TAG, "count recycle=" + debugCountRecycle + ", load=" + debugCountLoad
						+ ", bgr=" + debugCountBgr + ", post=" + debugCountPost);
			}
			super.onPostExecute(result);
		}

	}

	public Bitmap getBitmap(int position) {
		return imageMap.get(position);
	}

}
