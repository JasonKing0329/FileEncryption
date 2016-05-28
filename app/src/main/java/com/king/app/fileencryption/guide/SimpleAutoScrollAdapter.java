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

public class SimpleAutoScrollAdapter extends AutoScrollAdapter {

	private List<String> list;
	private Encrypter encrypter;
	private Random random;

	private int width, height;
	
	public SimpleAutoScrollAdapter(List<String> list) {
		this.list = list;
		random = new Random();
		encrypter = EncrypterFactory.create();
	}

	public void setImageSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void loadNextImage(ImageView view) {

		if (list != null) {
			int index = Math.abs(random.nextInt()) % list.size();
			view.setTag(list.get(index));
			new LoadImageTask(view).execute(list.get(index));
		}
	}

	private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

		private ImageView view;
		
		public LoadImageTask(ImageView view) {
			this.view = view;
		}
		
		@Override
		protected Bitmap doInBackground(String... params) {
			
			return ImageFactory.getInstance(encrypter).createEncryptedThumbnail(params[0], width * height, null);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null) {
				view.setImageResource(R.drawable.icon_loading);
			}
			else {
				view.setImageBitmap(result);
			}
		}
		
	}
}
