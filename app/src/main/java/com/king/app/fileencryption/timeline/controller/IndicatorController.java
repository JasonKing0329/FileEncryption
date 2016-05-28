package com.king.app.fileencryption.timeline.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.open.image.ImageFactory;
import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.util.ScreenUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class IndicatorController {

	private final boolean DEBUG = true;
	private final String TAG = "IndicatorController";

	private Context mContext;
	private Encrypter encrypter;

	private HashMap<String, Indicator> indicatorMap;
	private Queue<Indicator> indicatorQueue;

	private int bkWidth, bkHeight;

	// 缓存第1个图片，否则因为异步原因和sticky gridview的bug，第1个header总是显示不出来图片
	private Bitmap firstHeadBitmap;

	public IndicatorController(Context context) {
		mContext = context;
		encrypter = EncrypterFactory.create();
		indicatorMap = new HashMap<String, Indicator>();
		indicatorQueue = new LinkedList<Indicator>();
		bkWidth = context.getResources().getDimensionPixelOffset(R.dimen.timeline_indicator_height);
		bkHeight = ScreenUtils.getScreenWidth(context);
	}

	/**
	 * 缓存第1个图片，否则因为异步原因和sticky gridview的bug，第1个header总是显示不出来图片
	 * @param path
	 * @return
	 */
	public Bitmap getFirstHeadBitmap(String path) {
		if (firstHeadBitmap == null) {
			firstHeadBitmap = PictureManagerUpdate.getInstance().createCircleBitmap(
					path, mContext);
		}
		return firstHeadBitmap;
	}

	/**
	 * 加载head以及bk
	 * @param time
	 * @param headView
	 * @param headPath
	 * @param bkView
	 * @param bkPath
	 * @param useCache
	 */
	public void loadIndicator(String time, ImageView headView, String headPath,
							  ImageView bkView, String bkPath, boolean useCache) {
		if (DEBUG) {
			Log.d(TAG, "loadIndicator " + time);
		}

		Indicator indicator = indicatorMap.get(time);
		if (indicator == null) {
			indicator = new Indicator();
		}
		indicator.bk = bkView;
		indicator.head = headView;
		indicator.timeTag = time;

		if (useCache) {
			loadIndicatorWithCache(indicator, headPath, bkPath);
//			loadIndicatorWithHighCache(indicator, headPath, bkPath);
		}
		else {
			new LoadImageTask(indicator).execute(headPath, bkPath);
		}
	}

	/**
	 * 只加载head
	 * @param time
	 * @param imageView
	 * @param path
	 * @param useCache
	 */
	public void loadIndicator(String time, ImageView headView, String headPath,
							  boolean useCache) {

		if (DEBUG) {
			Log.d(TAG, "loadIndicator " + time);
		}

		Indicator indicator = indicatorMap.get(time);
		if (indicator == null) {
			indicator = new Indicator();
		}
		indicator.head = headView;
		indicator.timeTag = time;

		if (useCache) {
			loadIndicatorWithCache(indicator, headPath);
//			loadIndicatorWithHighCache(indicator, headPath, bkPath);
		}
		else {
			new LoadImageTask(indicator).execute(headPath);
		}
	}

	private class LoadImageTask extends AsyncTask<String, Void, Void> {

		private Indicator indicator;

		public LoadImageTask(Indicator indicator) {
			this.indicator = indicator;
		}
		@Override
		protected Void doInBackground(String... params) {

			indicator.headBitmap = PictureManagerUpdate.getInstance().createCircleBitmap(
					params[0], mContext);
			if (Constants.FEATURE_TIMELINE_ENABLE_BK) {
				indicator.bkBitmap = ImageFactory.getInstance(encrypter).createEncryptedThumbnail(params[1]
						, bkWidth * bkHeight, null);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			if (DEBUG) {
				Log.d(TAG, "head.setImageBitmap");
			}

			indicator.head.setImageBitmap(indicator.headBitmap);
			if (Constants.FEATURE_TIMELINE_ENABLE_BK) {
				indicator.bk.setImageBitmap(indicator.bkBitmap);
			}
			super.onPostExecute(param);
		}

	}

	/**
	 * 只缓存图片路径，确保position处的header每次都加载的一样的数据
	 * @param indicator
	 * @param headPath
	 * @param bkPath
	 */
	private void loadIndicatorWithCache(Indicator indicator, String headPath,
										String bkPath) {
		if (indicator.headPath == null) {
			indicator.headPath = headPath;
		}
		if (indicator.bkPath == null) {
			indicator.bkPath = bkPath;
		}

		if (indicatorMap.get(indicator.timeTag) == null) {
			indicatorMap.put(indicator.timeTag, indicator);
		}
		new LoadImageTask(indicator).execute(indicator.headPath, indicator.bkPath);
	}

	/**
	 * 缓存图片路径以及bitmap
	 * @param indicator
	 * @param headPath
	 * @param bkPath
	 */
	private void loadIndicatorWithHighCache(Indicator indicator, String headPath,
											String bkPath) {

	}

	/**
	 * 只缓存图片路径，确保position处的header每次都加载的一样的数据
	 * @param indicator
	 * @param headPath
	 * @param bkPath
	 */
	private void loadIndicatorWithCache(Indicator indicator, String headPath) {
		if (indicator.headPath == null) {
			indicator.headPath = headPath;
		}

		if (indicatorMap.get(indicator.timeTag) == null) {
			indicatorMap.put(indicator.timeTag, indicator);
		}
		new LoadImageTask(indicator).execute(indicator.headPath);
	}

	/*
	private class LoadImageTask extends AsyncTask<String, Void, Void> {

		private Indicator indicator;

		public LoadImageTask(Indicator indicator) {
			this.indicator = indicator;
		}
		@Override
		protected Void doInBackground(String... params) {

			if (!pack.isRemoved && pack.headBitmap == null) {
				// 不能用getOrderCircleCover
				pack.headBitmap = PictureManagerUpdate.getInstance().createCircleBitmap(
						params[0], mContext);
			}
			
			if (!pack.isRemoved && pack.bkBitmap == null) {
				pack.bkBitmap = ImageFactory.getInstance(encrypter).createEncryptedThumbnail(params[1]
						, pack.bk.getWidth() * pack.bk.getHeight(), null);
			}
			return pack;
		}

		@Override
		protected void onPostExecute(Void param) {
			if (!pack.isRemoved) {
				Log.d(Constants.LOG_ADAPTER, "setImageBitmap " + pack.timeTag);
				pack.head.setImageBitmap(pack.headBitmap);
				pack.bk.setImageBitmap(pack.bkBitmap);
			}
			super.onPostExecute(pack);
		}
		
	}
	*/

}
