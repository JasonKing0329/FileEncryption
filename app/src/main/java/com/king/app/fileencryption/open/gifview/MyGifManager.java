package com.king.app.fileencryption.open.gifview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.open.gifview.GifView.GifImageType;
import com.king.app.fileencryption.tool.Encrypter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;

public class MyGifManager {

	private final String TAG = "MyGifManager";
	private Context mContext;
	private LinearLayout container;
	private GifView gifView;
	private Encrypter encrypter;

	/**
	 * v6.3.10 add support for match_parent
	 */
	public static final int MATCH_PARENT = -1;
	public static final int WRAP_CONTENT = 0;
	private int defaultFactor;
	private int parentWidth, parentHeight;

	public MyGifManager(Context context, LinearLayout container) {
		this.container = container;
		mContext = context;
		encrypter = EncrypterFactory.create();
	}

	/**
	 * when set defaultFactor as MATCH_PARENT, need call this
	 * call this before call showGifView
	 * @param width
	 * @param height
	 */
	public void setParentSize(int width, int height) {
		parentHeight = height;
		parentWidth = width;
	}

	/**
	 * call this when screen orientation changed
	 * @param width
	 * @param height
	 */
	public void updateParentSize(int width, int height) {
		parentHeight = height;
		parentWidth = width;
		if (defaultFactor == MATCH_PARENT && gifView != null) {
			gifView.matchToParent(parentWidth, parentHeight);
		}
	}

	/**
	 * add support for MATCH_PARENT
	 * @param filePath
	 * @param showAnim
	 * @param factor MATCH_PARENT or WRAP_CONTENT
	 * @return
	 */
	public boolean showGifView (String filePath, Animation showAnim, int factor) {
		defaultFactor = factor;
		boolean result = showGifView(filePath);
		if (result) {
			if (showAnim != null) {
				container.startAnimation(showAnim);
			}
		}
		return result;
	}
	public boolean showGifView (String filePath) {
		if (filePath == null) {
			return false;
		}

		if (filePath.toLowerCase().endsWith(".gif")) {
			playGifImage(filePath);
			return true;
		}

		if (encrypter.isEncrypted(new File(filePath))) {
			String originName = encrypter.decipherOriginName(new File(filePath));
			Log.i(TAG, "chooseImage -> originName = " + originName);
			if (originName != null && originName.toLowerCase().endsWith(".gif")) {
				playGifImage(encrypter.decipherToByteArray(new File(filePath)));
				return true;
			}
		}
		return false;
	}

	public void setZoomFactor(float factor) {
		gifView.setZoomFactor(factor);
	}

	private void playGifImage(byte[] datas) {
		Log.i(TAG, "playGifImage byte[]");

		//change gif resource is not ideal by only setImage (because there is thread conflict and ondraw view width/height conflict
		//)it's very complex to modify that
		//so modify it to create new GifView everytime, before that, force to finish and recycle resource
		if (container.getChildCount() > 0) {
			gifView = (GifView) container.getChildAt(0);
		}

		if (gifView != null) {
			container.removeAllViews();
			gifView.finish();
		}

		try {
			/**
			 * refer包里的gifview处理有bug,直接用setGifImage切换图片会有FC问题,
			 * 只能另辟蹊径每次重新创建一个gifview
			 */
			gifView = new GifView(mContext);
			/**
			 * v6.3.10 add support for match_parent
			 */
			if (defaultFactor == MATCH_PARENT) {
				gifView.matchToParent(parentWidth, parentHeight);
			}

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			gifView.setLayoutParams(params);
			gifView.setGifImageType(GifImageType.COVER);
			gifView.setGifImage(datas);

			//v6.2.4 bug fix: sometimes tap some gif image, the view not display it
			//operation is right, but from log then I see, in GifView.onMeasure, the width and height is
			//get from GifDetector.width/height, and the values are get from file bytes.
			//in setGifDecoderImage method, it start new thread to execute GifDetector read values process
			//so the process has time conflict with view and layout make relationship process
			//post to execute this process can protect from this issue
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					container.addView(gifView);
					container.setVisibility(View.VISIBLE);
				}
			}, 100);
		} catch (Exception e) {
			container.setVisibility(View.INVISIBLE);
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}


	private void playGifImage(String filePath) {
		Log.i(TAG, "playGifImage " + filePath);
		//my modification:
		//change gif resource is not ideal by only setImage (because there is thread conflict and ondraw view width/height conflict
		//)it's very complex to modify that
		//so modify it to create new GifView everytime, before that, force to finish and recycle resource
		if (container.getChildCount() > 0) {
			gifView = (GifView) container.getChildAt(0);
		}

		if (gifView != null) {
			container.removeAllViews();
			gifView.finish();
		}


		try {
			/**
			 * refer包里的gifview处理有bug,直接用setGifImage切换图片会有FC问题,
			 * 只能另辟蹊径每次重新创建一个gifview
			 */
			gifView = new GifView(mContext);
			/**
			 * v6.3.10 add support for match_parent
			 */
			if (defaultFactor == MATCH_PARENT) {
				gifView.matchToParent(parentWidth, parentHeight);
			}

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			gifView.setLayoutParams(params);
			gifView.setGifImageType(GifImageType.COVER);
			gifView.setGifImage(new FileInputStream(filePath));

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					container.addView(gifView);
					container.setVisibility(View.VISIBLE);
				}
			}, 100);
		} catch (FileNotFoundException e) {
			container.setVisibility(View.INVISIBLE);
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}

	public void finish() {
		if (gifView != null) {
			gifView.finish();
		}
	}

	public void refresh() {
		if (gifView != null) {
			gifView.requestLayout();
		}
	}

	public int getGifWidth() {
		if (gifView != null) {
			return gifView.getWidth();
		}
		return 0;
	}

	public int getGifOriginWidth() {
		if (gifView != null) {
			return gifView.getBitmapWidth();
		}
		return 0;
	}

	public int getGifOriginHeight() {
		if (gifView != null) {
			return gifView.getBitmapHeight();
		}
		return 0;
	}
}
