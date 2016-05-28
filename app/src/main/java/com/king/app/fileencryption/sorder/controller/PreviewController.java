package com.king.app.fileencryption.sorder.controller;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler.Callback;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class PreviewController {

	private List<Bitmap> previewList;
	private List<ImageView> imageViewList;
	private List<String> pathList;
	private int imageSize;
	private Context mContext;
	private Handler handler;
	private Bitmap defUnavaiBitmap;
	
	public PreviewController(Context context, Callback callback) {
		mContext = context;
		handler = new Handler(callback);
		int itemWidth = context.getResources().getDimensionPixelSize(R.dimen.sorder_preview_width);
		imageSize = itemWidth * itemWidth;
		defUnavaiBitmap = PictureManagerUpdate.getInstance().getUnavailableItemImage(context);
	}
	
	public void setImageView(int position, ImageView view) {
		imageViewList.set(position, view);
	}

	public void loadImages(List<String> imgPathList) {
		this.pathList = imgPathList;
		if (imgPathList != null) {
			imageViewList = new ArrayList<ImageView>();
			previewList = new ArrayList<Bitmap>();
			for (int i = 0 ; i < imgPathList.size(); i ++) {
				imageViewList.add(null);
				previewList.add(null);
			}
			
			new Thread() {
				public void run() {
					Bitmap bitmap = null;
					for (int i = 0 ; i < pathList.size(); i ++) {
						bitmap = PictureManagerUpdate.getInstance().createImage(
								pathList.get(i), imageSize, mContext, 1);
						previewList.set(i, bitmap);
					}
					handler.sendMessage(new Message());
				}
			}.start();
		}
	}

	public Bitmap getBitmap(int position) {

		if (previewList != null) {
			if (position < previewList.size()) {
				return previewList.get(position);
			}
		}
		return null;
	}

	public void recycleAll() {
		if (previewList != null) {
			for (Bitmap bitmap:previewList) {
				if (bitmap != null && bitmap != defUnavaiBitmap) {
					bitmap.recycle();
				}
			}
		}
	}
}
