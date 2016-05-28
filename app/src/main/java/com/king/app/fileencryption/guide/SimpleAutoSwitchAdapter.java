package com.king.app.fileencryption.guide;

import java.util.List;
import java.util.Random;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.open.image.ImageFactory;
import com.king.app.fileencryption.tool.Encrypter;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class SimpleAutoSwitchAdapter extends AutoSwitchAdapter {

	private List<String> list;

	private int width, height;
	private Random random;
	private Encrypter encrypter;

	private Bitmap curBitmap;
	private Bitmap nextBitmap;
	private String nextPath;

	public SimpleAutoSwitchAdapter(List<String> list) {
		this.list = list;
		random = new Random();
		encrypter = EncrypterFactory.create();
	}

	public void setImageSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public int getCount() {
		return list == null ? 0:list.size();
	}

	@Override
	public void loadNextImage(ImageView view) {
		/**
		 * 为优化显示效果，缓存下一张图片
		 */
		if (nextBitmap == null) {
			int index = Math.abs(random.nextInt()) % list.size();
			view.setTag(list.get(index));
			new LoadImageTask(view, list.get(index)).execute();
		}
		else {
			view.setTag(nextPath);
			curBitmap = nextBitmap;
			view.setImageBitmap(curBitmap);
		}
		preloadNextBitmap();
	}

	@Override
	public void recycleAll() {
		if (curBitmap != null) {
			curBitmap.recycle();
		}
		if (nextBitmap != null) {
			nextBitmap.recycle();
		}
	}

	private void preloadNextBitmap() {
		int index = Math.abs(random.nextInt()) % list.size();
		nextPath = list.get(index);
		new LoadImageTask(null, nextPath).execute();
	}

	/**
	 * pre-load 的情况只将bitmap赋给nextBitmap
	 * @author JingYang
	 *
	 */
	private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

		private ImageView view;
		private String filePath;

		public LoadImageTask(ImageView view, String filePath) {
			this.view = view;
			this.filePath = filePath;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {

			return ImageFactory.getInstance(encrypter).createEncryptedThumbnail(filePath, width * height, null);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (view == null) {
				nextBitmap = result;
			}
			else {
				if (result == null) {
					view.setImageResource(R.drawable.icon_loading);
				}
				else {
					view.setImageBitmap(result);
				}
			}
		}

	}

}
