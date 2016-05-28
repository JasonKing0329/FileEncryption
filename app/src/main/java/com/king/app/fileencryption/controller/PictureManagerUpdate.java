package com.king.app.fileencryption.controller;

import java.io.File;
import java.util.HashMap;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.open.image.ImageFactory;
import com.king.app.fileencryption.open.image.ImageValueController;
import com.king.app.fileencryption.tool.Encrypter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class PictureManagerUpdate {

	private static PictureManagerUpdate manager;
	private final String TAG = "PictureManagerUpdate";

	/**
	 * only getUnavailableItemImage and getDefaultOrderCover get image from defaultPool
	 * if other method want to get those two default image
	 * need by call getUnavailableItemImage or getDefaultOrderCover instead of get from defaultPool
	 * getUnavailableItemImage and getDefaultOrderCover make sure that if image was recylced in somewhere, application can load it again
	 */
	private HashMap<String, Bitmap> defaultPool;

	private HashMap<String, Bitmap> orderPool;
	private HashMap<String, Bitmap> expandOrderPool;
	private HashMap<String, Bitmap> circleOrderPool;
	private HashMap<String, Bitmap> orderPreviewPool;
	private HashMap<String, Bitmap> wallItemPool;
	private HashMap<String, Bitmap> spicturePool;

	private final String KEY_SORDER_COVER_DEFAULT = "sorder_cover_default";
	private final String KEY_ITEM_DEFAULT = "item_default";

	/**
	 * as create image value infor need execute database I/O
	 * for performance, it's better execute imageValue event in process that only open one image 
	 */
	private ImageValueController imageValueController;

	private PictureManagerUpdate() {
		orderPool = new HashMap<String, Bitmap>();
		expandOrderPool = new HashMap<String, Bitmap>();
		circleOrderPool = new HashMap<String, Bitmap>();
		defaultPool = new HashMap<String, Bitmap>();
		orderPreviewPool = new HashMap<String, Bitmap>();
		wallItemPool = new HashMap<String, Bitmap>();
		spicturePool = new HashMap<String, Bitmap>();

		imageValueController = new ImageValueController();
	}
	public static PictureManagerUpdate getInstance() {
		if (manager == null) {
			manager = new PictureManagerUpdate();
		}
		return manager;
	}

	/**
	 * 为加载图片开辟新的空间
	 * @param path
	 * @param context
	 * @return
	 */
	public Bitmap createCircleBitmap(String path, Context context) {
		Bitmap bitmap = loadCoverBitmap(path);
		if (bitmap == null) {
			bitmap = getDefaultOrderCover(context);
		}
		else {
			bitmap = ImageFactory.getCircleBitmap(bitmap);
		}

		if (bitmap == null) {
			bitmap = getDefaultOrderCover(context);
		}
		return bitmap;
	}

	/**
	 * 为加载图片开辟新的空间
	 * @param path
	 */
	public Bitmap createSingleOrderCover(String path, Context context) {
		return createImage(path, Configuration.getSorderCoverMaxPixel(), context, 0);
	}

	/**
	 * 为加载图片开辟新的空间
	 * @param path
	 */
	public Bitmap createCascadeOrderCover(String path, Context context) {
		return createImage(path, Configuration.getSorderCascadeCoverSize(), context, 1);
	}

	/**
	 * 为加载图片开辟新的空间
	 * @param path
	 * @param size the value is width * height
	 * @param context
	 * @param defaultImage if 0, return DefaultOrderCover; if 1, return UnavailableItemImage
	 * @return
	 */
	public Bitmap createImage(String path, int size, Context context, int defaultImage) {
		Bitmap bitmap = null;
		if (path == null) {
			bitmap = getDefaultOrderCover(context);
		}
		else {
			bitmap = loadBitmap(path, size);
			if (bitmap == null) {
				if (defaultImage == 0) {
					bitmap = getDefaultOrderCover(context);
				}
				else if (defaultImage == 1) {
					bitmap = getUnavailableItemImage(context);
				}
			}
		}
		return bitmap;
	}

	/**
	 * 慎用，该方法的bitmap放在缓存池里，外部不要recycle
	 * 有利于避免重复加载图片。但是在图片回收方面需要多加注意，往往在listview/gridview中会重复引用相同的图片，
	 * 如果在外部对bitmap进行了recycle，那么就会很容易造成bitmap has recycled exception
	 * @param path
	 * @return
	 */
	@Deprecated
	public Bitmap getOrderCover(String path, Context context) {
		Bitmap bitmap = orderPool.get(path);
		if (bitmap == null) {
			if (path == null) {
				bitmap = getDefaultOrderCover(context);
			}
			else {
				if (new File(path).exists()) {
					bitmap = loadCoverBitmap(path);
				}
				if (bitmap == null) {
					bitmap = getDefaultOrderCover(context);
				}
			}
			orderPool.put(path, bitmap);
			Log.d(TAG, "put OrderCover");
		}
		return bitmap;
	}

	public Bitmap getExpandOrderCover(String path, Context context) {
		Bitmap bitmap = expandOrderPool.get(path);
		if (bitmap == null) {
			if (path == null) {
				bitmap = getDefaultOrderCover(context);
			}
			else {
				if (new File(path).exists()) {
					bitmap = loadBitmap(path, Configuration.getExpandSorderCoverMaxPixel());
				}
				if (bitmap == null) {
					bitmap = getDefaultOrderCover(context);
				}
			}
			expandOrderPool.put(path, bitmap);
			Log.d(TAG, "put OrderCover");
		}
		return bitmap;
	}

	/**
	 * 为加载图片开辟新的空间
	 * @param path
	 */
	public Bitmap createSpictureItem(String path, Context context) {
		int width = Configuration.getChooserItemWidth();
		return createImage(path, width * width, context, 1);
	}

	public Bitmap getSpictureItem(String path, Context context) {
		Bitmap bitmap = spicturePool.get(path);
		if (bitmap != null && bitmap.isRecycled()) {
			bitmap = null;
			spicturePool.remove(path);
		}
		if (bitmap == null) {
			if (path == null) {
				bitmap = getUnavailableItemImage(context);
			}
			else {
				if (new File(path).exists()) {
					bitmap = loadSpictureItem(path);
				}
				if (bitmap == null) {
					bitmap = getUnavailableItemImage(context);
				}
			}
			spicturePool.put(path, bitmap);
			Log.d(TAG, "put SpictureItem");
		}
		return bitmap;
	}

	public Bitmap createHDSpicture(String path, Context context, int orientation) {
		Bitmap bitmap = loadSpictureItem(path, context, orientation);
		if (bitmap == null) {
			bitmap = getUnavailableItemImage(context);
		}
		return bitmap;
	}

	/**
	 * 为加载图片开辟新的空间
	 * @param path
	 */
	public Bitmap createWallItem(String path, Context context) {
		int size = Configuration.getSorderCoverMaxPixel();
		return createImage(path, size, context, 1);
	}

	@Deprecated
	public Bitmap getWallItem(String path, Context context) {
		Bitmap bitmap = wallItemPool.get(path);
		if (bitmap == null) {
			if (path == null) {
				bitmap = getUnavailableItemImage(context);
			}
			else {
				if (new File(path).exists()) {
					bitmap = loadWallItemBitmap(path);
				}
				if (bitmap == null) {
					bitmap = getUnavailableItemImage(context);
				}
			}
			wallItemPool.put(path, bitmap);
			Log.d(TAG, "put WallItem");
		}
		return bitmap;
	}

	public Bitmap getOrderCircleCover(String path, Context context) {
		Bitmap bitmap = circleOrderPool.get(path);
		if (bitmap != null && bitmap.isRecycled()) {
			bitmap = null;
			circleOrderPool.remove(path);
		}
		if (bitmap == null) {
			if (path == null) {
				bitmap = getDefaultOrderCover(context);
			}
			else {
				if (new File(path).exists()) {
					bitmap = loadCoverBitmap(path);
				}
				if (bitmap == null) {
					bitmap = getDefaultOrderCover(context);
				}
			}
			bitmap = ImageFactory.getCircleBitmap(bitmap);
			circleOrderPool.put(path, bitmap);
			Log.d(TAG, "put OrderCircleCover");
		}
		return bitmap;
	}

	public Bitmap getOrderPreview(String path, Context context) {
		Bitmap bitmap = orderPreviewPool.get(path);
		if (bitmap == null) {
			if (path == null) {
				bitmap = getDefaultOrderCover(context);
			}
			else {
				if (new File(path).exists()) {
					bitmap = loadCoverPreview(path);
				}
				if (bitmap == null) {
					bitmap = getDefaultOrderCover(context);
				}
			}
			orderPreviewPool.put(path, bitmap);
			Log.d(TAG, "put OrderPreview");
		}
		return bitmap;
	}

	public void recycleOrderCovers() {
		Log.d(TAG, "recycleOrderCovers");
		if (orderPool.size() > 0) {
			for (Bitmap bitmap:orderPool.values()) {
				if (bitmap != defaultPool.get(KEY_SORDER_COVER_DEFAULT)) {
					bitmap.recycle();
				}
			}
			orderPool.clear();
		}
	}

	public void recycleExpandOrderCovers() {
		Log.d(TAG, "recycleExpandOrderCovers");
		if (expandOrderPool.size() > 0) {
			for (Bitmap bitmap:expandOrderPool.values()) {
				if (bitmap != defaultPool.get(KEY_SORDER_COVER_DEFAULT)) {
					bitmap.recycle();
				}
			}
			expandOrderPool.clear();
		}
	}


	public void recycleOrderPreview() {
		Log.d(TAG, "recycleOrderPreview");
		if (orderPreviewPool.size() > 0) {
			for (Bitmap bitmap:orderPreviewPool.values()) {
				if (bitmap != defaultPool.get(KEY_SORDER_COVER_DEFAULT)) {
					if (bitmap != defaultPool.get(KEY_ITEM_DEFAULT)) {
						bitmap.recycle();
					}
				}
			}
			orderPreviewPool.clear();
		}
	}

	public void recycleWallItems() {
		Log.d(TAG, "recycleWallItems");
		if (wallItemPool.size() > 0) {
			for (Bitmap bitmap:wallItemPool.values()) {
				if (bitmap != defaultPool.get(KEY_ITEM_DEFAULT)) {
					bitmap.recycle();
				}
			}
			wallItemPool.clear();
		}
	}

	public void recycleSpictureItems() {
		Log.d(TAG, "recycleSpictureItems");
		if (spicturePool.size() > 0) {
			for (Bitmap bitmap:spicturePool.values()) {
				if (bitmap != defaultPool.get(KEY_ITEM_DEFAULT)) {
					bitmap.recycle();
				}
			}
			spicturePool.clear();
		}
	}

	public void recycleCircleOrderCover() {
		Log.d(TAG, "recycleCircleOrderCover");
		if (circleOrderPool.size() > 0) {
			for (Bitmap bitmap:circleOrderPool.values()) {
				if (bitmap != defaultPool.get(KEY_SORDER_COVER_DEFAULT)) {
					bitmap.recycle();
				}
			}
			circleOrderPool.clear();
		}
	}

	public Bitmap getUnavailableItemImage(Context context) {
		Bitmap bitmap = defaultPool.get(KEY_ITEM_DEFAULT);
		if (bitmap == null || bitmap.isRecycled()) {
			Drawable drawable = context.getResources().getDrawable(R.drawable.ic_launcher);
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			bitmap = ImageFactory.createReflectionImageWithOrigin(bitmapDrawable.getBitmap());
			defaultPool.put(KEY_ITEM_DEFAULT, bitmap);
		}
		return bitmap;
	}

	public Bitmap getDefaultOrderCover(Context context) {
		Bitmap bitmap = defaultPool.get(KEY_SORDER_COVER_DEFAULT);
		if (bitmap == null || bitmap.isRecycled()) {
			//Drawable drawable = context.getResources().getDrawable(R.drawable.directory_icon);
			Drawable drawable = context.getResources().getDrawable(new ThemeManager(context).getDefFolderResId());
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			bitmap = ImageFactory.createReflectionImageWithOrigin(bitmapDrawable.getBitmap());
			defaultPool.put(KEY_SORDER_COVER_DEFAULT, bitmap);
		}
		return bitmap;
	}

	private Bitmap loadBitmap(String path, int size) {

		Bitmap bitmap = null;
		Encrypter encrypter = EncrypterFactory.create();
		ImageFactory factory = ImageFactory.getInstance(encrypter);
		File file = new File(path);
		if (encrypter.isEncrypted(file)) {
			bitmap = factory.createEncryptedThumbnail(path, size, null);
		}
		else {
			bitmap = factory.createImageThumbnail(path, size);
		}
		return bitmap;
	}

	private Bitmap loadCoverBitmap(String path) {

		Bitmap bitmap = null;
		Encrypter encrypter = EncrypterFactory.create();
		ImageFactory factory = ImageFactory.getInstance(encrypter);
		File file = new File(path);
		if (encrypter.isEncrypted(file)) {
			bitmap = factory.createEncryptedThumbnail(path, Configuration.getSorderCoverMaxPixel(), null);
		}
		else {
			bitmap = factory.createImageThumbnail(path, Configuration.getSorderCoverMaxPixel());
		}
		return bitmap;
	}

	private Bitmap loadSpictureItem(String path) {

		Bitmap bitmap = null;
		Encrypter encrypter = EncrypterFactory.create();
		ImageFactory factory = ImageFactory.getInstance(encrypter);
		File file = new File(path);
		if (encrypter.isEncrypted(file)) {
			bitmap = factory.createEncryptedThumbnail(path, Configuration.getChooserItemWidth() * Configuration.getChooserItemWidth(), null);
		}
		else {
			bitmap = factory.createImageThumbnail(path, Configuration.getChooserItemWidth() * Configuration.getChooserItemWidth());
		}
		return bitmap;
	}

	private Bitmap loadSpictureItem(String path, Context context, int orientation) {

		Bitmap bitmap = null;
		Encrypter encrypter = EncrypterFactory.create();
		ImageFactory factory = ImageFactory.getInstance(encrypter);
		File file = new File(path);
		if (encrypter.isEncrypted(file)) {
			bitmap = factory.createThumbForEncrypted(path, context, orientation);
		}
		else {
			bitmap = factory.createThumb(path, context, orientation);
		}
		return bitmap;
	}

	private Bitmap loadWallItemBitmap(String path) {

		Bitmap bitmap = null;
		Encrypter encrypter = EncrypterFactory.create();
		ImageFactory factory = ImageFactory.getInstance(encrypter);
		File file = new File(path);
		if (encrypter.isEncrypted(file)) {
			bitmap = factory.createEncryptedThumbnail(path, Configuration.getSorderCoverMaxPixel(), null);
		}
		else {
			bitmap = factory.createImageThumbnail(path, Configuration.getSorderCoverMaxPixel());
		}
		return bitmap;
	}

	private Bitmap loadCoverPreview(String path) {

		Bitmap bitmap = null;
		Encrypter encrypter = EncrypterFactory.create();
		ImageFactory factory = ImageFactory.getInstance(encrypter);
		File file = new File(path);
		if (encrypter.isEncrypted(file)) {
			bitmap = factory.createEncryptedThumbnail(path, Configuration.getSorderCoverPreviewSize(), null);
		}
		else {
			bitmap = factory.createImageThumbnail(path, Configuration.getSorderCoverPreviewSize());
		}
		return bitmap;
	}

	/**
	 * as create image value infor need execute database I/O
	 * for performance, it's better execute this operation in process that only open one image 
	 * @param path
	 * @return
	 */
	public Bitmap createHDBitmap(String path) {
		Bitmap bitmap = null;
		Encrypter encrypter = EncrypterFactory.create();
		ImageFactory factory = ImageFactory.getInstance(encrypter);
		bitmap = factory.createEncryptedThumbnail(path, Configuration.getScreenWidth() * Configuration.getScreenHeight(), imageValueController);
		return bitmap;
	}

	public void destroy() {
		Log.d(TAG, "destroy");
		recycleCircleOrderCover();
		recycleExpandOrderCovers();
		recycleOrderCovers();
		recycleOrderPreview();
		recycleSpictureItems();
		recycleWallItems();
		recycleDefaultImage();
		manager = null;
	}

	private void recycleDefaultImage() {
		Log.d(TAG, "recycleDefaultImage");
		if (defaultPool.size() > 0) {
			for (Bitmap bitmap:defaultPool.values()) {
				bitmap.recycle();
			}
			defaultPool.clear();
		}
	}
}
