package com.king.app.fileencryption.open.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.tool.ScreenInfor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.Log;

public class ImageFactory {

	private final String TAG = "ImageFactory";
	private Encrypter encrypter;

	private static ImageFactory factoryWithEncrypter = null;

	private ImageFactory(Encrypter en) {
		encrypter = en;
	}


	public static ImageFactory getInstance(Encrypter en) {
		if (en == null) {
			return null;
		}
		if (factoryWithEncrypter == null) {
			factoryWithEncrypter = new ImageFactory(en);
		}
		return factoryWithEncrypter;
	}

	public void setEncrypter(Encrypter en) {
		encrypter = en;
	}

	public boolean isImage(String name) {
		return name.endsWith(".jpg") || name.endsWith(".jpeg")
				|| name.endsWith(".png") || name.endsWith(".gif")
				|| name.endsWith(".bmp");
	}

	public Bitmap createImage(File file) {
		Bitmap bitmap = null;
		if (isImage(file.getPath())) {
			try {
				FileInputStream stream = new FileInputStream(file);
				bitmap = BitmapFactory.decodeStream(stream);
				stream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}

	public Bitmap createEncryptedImage(File file) {
		Bitmap bitmap = null;
		byte datas[] = encrypter.decipherToByteArray(file);
		if (datas != null) {
			try {
				bitmap = BitmapFactory.decodeByteArray(datas, 0, datas.length);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}

	public Bitmap createThumbForEncrypted(String file, Context context, int orientation) {
		Bitmap bitmap = null;
		int screenWidth = ScreenInfor.getWidth((Activity) context);
		int screenHeight = ScreenInfor.getHeight((Activity) context);
		int chooserSize = context.getResources().getDimensionPixelSize(R.dimen.spicture_chooser_item_width);

		int maxWidth = screenWidth;
		int maxHeight = screenHeight;
		if (orientation == 1) {
			maxHeight = screenHeight - chooserSize;
		}
		else if (orientation == 2) {
			maxWidth = screenWidth - chooserSize;
		}

		byte datas[] = encrypter.decipherToByteArray(new File(file));
		if (datas != null) {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;// 对bitmap不分配空间，只是用于计算文件options的各种属性(本程序需要计算width,height)
			BitmapFactory.decodeByteArray(datas, 0, datas.length, opts);

			int realWidth = opts.outWidth;
			int realHeight = opts.outHeight;
			int tarW = realWidth;
			int tarH = realHeight;
			int maxPixel = -1;
			if (realWidth > maxWidth) {
				float divW = (float)realWidth / (float)maxWidth;
				tarH = (int)((float)realHeight / divW + 0.5);
				if (tarH < maxHeight) {
					maxPixel = maxWidth * tarH;
					tarW = maxWidth;

				}
				else {
					float divH = (float)realHeight / (float)maxHeight;
					tarW = (int)((float)realWidth / divH + 0.5);
					maxPixel = tarW * maxHeight;
					tarH = maxHeight;
				}
			}
			else if (realHeight > maxHeight) {
				float divH = (float)realHeight / (float)maxHeight;
				tarW = (int)((float)realWidth / divH + 0.5);
				if (tarW < maxWidth) {
					maxPixel = tarW * maxHeight;
					tarH = maxHeight;
				}
				else {
					float divW = (float)realWidth / (float)maxWidth;
					tarH = (int)((float)realHeight / divW + 0.5);
					maxPixel = maxWidth * tarH;
					tarW = maxWidth;
				}
			}
			if (maxPixel != -1) {
				opts.inSampleSize = computeSampleSize(opts, -1, maxPixel);// 通过计算出的options属性获取
				// inSampleSize参数，该参数用于决定为缩略图分配空间的适当等级
				opts.inJustDecodeBounds = false;
				try {
					bitmap = BitmapFactory.decodeByteArray(datas, 0, datas.length, opts);
				} catch (Exception e) {
					bitmap = null;
				}
			}
			else {
				bitmap = BitmapFactory.decodeByteArray(datas, 0, datas.length);
			}
		}

		return bitmap;
	}

	public Bitmap createThumb(String file, Context context, int orientation) {
		if (!isImage(file)) {
			return null;
		}
		Bitmap bitmap = null;
		int screenWidth = ScreenInfor.getWidth((Activity) context);
		int screenHeight = ScreenInfor.getHeight((Activity) context);
		int chooserSize = context.getResources().getDimensionPixelSize(R.dimen.spicture_chooser_item_width);

		int maxWidth = screenWidth;
		int maxHeight = screenHeight;
		if (orientation == 1) {
			maxHeight = screenHeight - chooserSize;
		}
		else if (orientation == 2) {
			maxWidth = screenWidth - chooserSize;
		}

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;// 对bitmap不分配空间，只是用于计算文件options的各种属性(本程序需要计算width,height)
		BitmapFactory.decodeFile(file, opts);

		int realWidth = opts.outWidth;
		int realHeight = opts.outHeight;
		int tarW = realWidth;
		int tarH = realHeight;
		int maxPixel = -1;
		if (realWidth > maxWidth) {
			float divW = (float)realWidth / (float)maxWidth;
			tarH = (int)((float)realHeight / divW + 0.5);
			if (tarH < maxHeight) {
				maxPixel = maxWidth * tarH;
				tarW = maxWidth;

			}
			else {
				float divH = (float)realHeight / (float)maxHeight;
				tarW = (int)((float)realWidth / divH + 0.5);
				maxPixel = tarW * maxHeight;
				tarH = maxHeight;
			}
		}
		else if (realHeight > maxHeight) {
			float divH = (float)realHeight / (float)maxHeight;
			tarW = (int)((float)realWidth / divH + 0.5);
			if (tarW < maxWidth) {
				maxPixel = tarW * maxHeight;
				tarH = maxHeight;
			}
			else {
				float divW = (float)realWidth / (float)maxWidth;
				tarH = (int)((float)realHeight / divW + 0.5);
				maxPixel = maxWidth * tarH;
				tarW = maxWidth;
			}
		}
		if (maxPixel != -1) {
			opts.inSampleSize = computeSampleSize(opts, -1, maxPixel);// 通过计算出的options属性获取
			// inSampleSize参数，该参数用于决定为缩略图分配空间的适当等级
			opts.inJustDecodeBounds = false;
			try {
				bitmap = BitmapFactory.decodeFile(file, opts);
				//bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, opts);
			} catch (Exception e) {
				bitmap = null;
			}
		}
		else {
			bitmap = BitmapFactory.decodeFile(file);
		}

		return bitmap;
	}

	/**
	 * create thumb nail mode image, to save the memory
	 * @param file
	 * @param maxPixel  use parameter like 128*128
	 * @param listener 用于统计图片的原始大小
	 * @return
	 */
	public Bitmap createEncryptedThumbnail(String file, int maxPixel, ImageValueListener listener) {
		Bitmap bitmap = null;
		byte datas[] = encrypter.decipherToByteArray(new File(file));
		if (datas != null) {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;// 对bitmap不分配空间，只是用于计算文件options的各种属性(本程序需要计算width,height)
			BitmapFactory.decodeByteArray(datas, 0, datas.length, opts);
			//统计图片的原始大小
			if (listener != null) {
				listener.onCreate(file, opts.outWidth, opts.outHeight);
			}

			opts.inSampleSize = computeSampleSize(opts, -1, maxPixel);// 通过计算出的options属性获取
			// inSampleSize参数，该参数用于决定为缩略图分配空间的适当等级
			opts.inJustDecodeBounds = false;

			try {
				bitmap = BitmapFactory.decodeByteArray(datas, 0, datas.length, opts);
			} catch (Exception e) {
				bitmap = null;
			}
		}
		return bitmap;
	}

	/**
	 * create thumb nail mode image, to save the memory
	 *
	 * @param file
	 * @param maxPixel  use parameter like 128*128
	 * @return
	 */
	public Bitmap createImageThumbnail(String file, int maxPixel) {
		Bitmap bitmap = null;
		if (isImage(file)) {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;// 对bitmap不分配空间，只是用于计算文件options的各种属性(本程序需要计算width,height)
			BitmapFactory.decodeFile(file, opts);

			opts.inSampleSize = computeSampleSize(opts, -1, maxPixel);// 通过计算出的options属性获取
			// inSampleSize参数，该参数用于决定为缩略图分配空间的适当等级
			opts.inJustDecodeBounds = false;

			try {
				bitmap = BitmapFactory.decodeFile(file, opts);
			} catch (Exception e) {
				bitmap = null;
			}
		}
		return bitmap;
	}

	/**
	 *
	 * @param options
	 * @param minSideLength
	 * @param maxNumOfPixels
	 *            图片被缩略后的最大像素，可以用如128*128表示
	 * @return
	 */
	private int computeSampleSize(BitmapFactory.Options options,
								  int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private int computeInitialSampleSize(BitmapFactory.Options options,
										 int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}



	/*******************************************************************************************/
	/****************公用的图像处理方法，不依赖于encrypter，统统设置为static引用*******************/
	/*******************************************************************************************/

	/**
	 * 缩放图片
	 * @param origin
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap origin, int newWidth, int newHeight) {
		// 获取这个图片的宽和高
		int width = origin.getWidth();
		int height = origin.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算缩放率，新尺寸除原始尺寸
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(origin, 0, 0, width, height,
				matrix, true);
		return bitmap;
	}
	/**
	 * 将彩色图转换为灰度图
	 * @param img 位图
	 * @return  返回转换好的位图
	 */
	public static Bitmap convertGreyImg(Bitmap img) {
		int width = img.getWidth();         //获取位图的宽
		int height = img.getHeight();       //获取位图的高

		int []pixels = new int[width * height]; //通过位图的大小创建像素点数组

		img.getPixels(pixels, 0, width, 0, 0, width, height);
		int alpha = 0xFF << 24;
		for(int i = 0; i < height; i++)  {
			for(int j = 0; j < width; j++) {
				int grey = pixels[width * i + j];

				int red = ((grey  & 0x00FF0000 ) >> 16);
				int green = ((grey & 0x0000FF00) >> 8);
				int blue = (grey & 0x000000FF);

				grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
				grey = alpha | (grey << 16) | (grey << 8) | grey;
				pixels[width * i + j] = grey;
			}
		}
		Bitmap result = Bitmap.createBitmap(width, height, Config.RGB_565);
		result.setPixels(pixels, 0, width, 0, 0, width, height);
		return result;
	}

	/**
	 * 切割图片
	 *
	 */
	public static class ImagePiece {
		public int index = 0;
		public Bitmap bitmap = null;
	}

	/**
	 * 切割图片
	 *
	 */
	public static class ImageSplitter {

		/**
		 * 将图片等份切割
		 * @param bitmap
		 * @param xPiece
		 * @param yPiece
		 * @return
		 */
		public List<ImagePiece> split(Bitmap bitmap, int xPiece, int yPiece) {

			List<ImagePiece> pieces = new ArrayList<ImagePiece>(xPiece * yPiece);
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			int pieceWidth = width / 3;
			int pieceHeight = height / 3;
			for (int i = 0; i < yPiece; i++) {
				for (int j = 0; j < xPiece; j++) {
					ImagePiece piece = new ImagePiece();
					piece.index = j + i * xPiece;
					int xValue = j * pieceWidth;
					int yValue = i * pieceHeight;
					piece.bitmap = Bitmap.createBitmap(bitmap, xValue, yValue,
							pieceWidth, pieceHeight);
					pieces.add(piece);
				}
			}

			return pieces;
		}

	}

	/**
	 * 设置图片透明度
	 * @param sourceImg
	 * @param number 透明度
	 * @return
	 */
	public static Bitmap setAlpha(Bitmap sourceImg, int number) {
		int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
		sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0,sourceImg.getWidth(), sourceImg.getHeight());// 获得图片的ARGB值
		number = number * 255 / 100;
		for (int i = 0; i < argb.length; i++) {
			argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);// 修改最高2位的值
		}
		sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg.getHeight(), Config.ARGB_8888);
		return sourceImg;
	}
	/**
	 * 获得圆角图片
	 * @param bitmap
	 * @param roundPx
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}
	/**
	 * @param bitmap 原图片
	 * @return 圆角矩形图片
	 */
	public static Bitmap getCircleBitmap(Bitmap bitmap) {

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		if (width > height) {
			width = height;
		}
		else {
			height = width;
		}
		int radius = width / 2;

		Bitmap output = Bitmap.createBitmap(width,
				height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, width, height);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawCircle(radius, radius, radius, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}
	/**
	 * 获得带倒影的图片
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
		final int reflectionGap = 4;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2, width, height / 2, matrix, false);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2), Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);

		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);
		return bitmapWithReflection;
	}
}
