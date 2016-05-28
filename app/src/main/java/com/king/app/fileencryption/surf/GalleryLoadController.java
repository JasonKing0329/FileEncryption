package com.king.app.fileencryption.surf;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.surf.GalleryAdapter.ViewHolder;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseBooleanArray;

/**
 * @author JingYang
 * @version create time：2016-1-28 上午11:30:14
 *
 */
public class GalleryLoadController {

	private final boolean DEBUG = true;
	private final String TAG = "GalleryLoadController";
	private Context mContext;
	//	private HashMap<Integer, Bitmap> imageMap;
	private SparseBooleanArray recycleTags;

	private int debugCountLoad, debugCountRecycle, debugCountBgr, debugCountPost;

	public GalleryLoadController(Context context) {
		mContext = context;
//		imageMap = new HashMap<Integer, Bitmap>();
		recycleTags = new SparseBooleanArray();
	}

	public void onRecycle(ViewHolder holder, int position) {
		if (DEBUG) {
			Log.d(TAG, "onRecycle " + position);
			debugCountRecycle ++;
		}
		holder.image.setImageBitmap(null);
		recycleTags.put(position, true);
	}

	public void onLoad(ViewHolder holder, int position, String path) {
		if (DEBUG) {
			Log.d(TAG, "onLoad " + position);
			debugCountLoad ++;
		}
		recycleTags.put(position, false);
		holder.image.setImageResource(R.drawable.icon_loading);
		new LoadImageTask(holder, position).execute(path);
	}

	private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

		private ViewHolder holder;
		private int position;

		public LoadImageTask(ViewHolder holder, int position) {
			this.holder = holder;
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
				bitmap = PictureManagerUpdate.getInstance().createSpictureItem(
						params[0], mContext);
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
					holder.image.setImageResource(R.drawable.ic_launcher);
				}
				else {
					holder.image.setImageBitmap(result);
				}
			}
			if (DEBUG) {
				Log.e(TAG, "count recycle=" + debugCountRecycle + ", load=" + debugCountLoad
						+ ", bgr=" + debugCountBgr + ", post=" + debugCountPost);
			}
			super.onPostExecute(result);
		}

	}
}
