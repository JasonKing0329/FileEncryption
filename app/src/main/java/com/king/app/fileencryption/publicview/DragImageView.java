package com.king.app.fileencryption.publicview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 该view在layout布局中，layout_width,layout_height即为拖动区域
 * 即便设置为全屏，非bitmap区域也不会遮挡其他view
 * @author King
 *
 */
public class DragImageView extends View {

	private final String TAG = "DragView";
	private Rect rect;//绘制矩形的区域
	private int deltaX,deltaY;//点击位置和图形边界的偏移量
	private Paint paint = new Paint();//画笔
	private Bitmap bitmap;
	private int imageWidth, imageHeight;

	protected int lastX;
	protected int lastY;
	private long lastTime;
	private OnClickListener onClickListener;
	private Matrix matrix;//实现imageview的fit效果

	public DragImageView(Context context) {
		super(context);
		init();
	}

	public DragImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DragImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		paint = new Paint();
		rect = new Rect();
		//paint.setColor(Color.RED);//填充红色
	}

	private void initRect() {
		if (bitmap != null) {
			imageWidth = bitmap.getWidth();
			imageHeight = bitmap.getHeight();
			rect.right = rect.left + imageWidth;
			rect.bottom = rect.top + imageHeight;
		}
	}

	public void setImageResource(int resId) {
		Drawable drawable = getResources().getDrawable(resId);
		bitmap = ((BitmapDrawable) drawable).getBitmap();
		initRect();
	}

	public void setImageBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		initRect();
	}

	public int getImageWidth() {
		return imageWidth;
	}

	/**
	 * 缩放图片
	 * @param width 缩放后的宽度
	 * @param height 缩放后的高度
	 */
	public void fitImageSize(int width, int height) {
		if (matrix == null) {
			matrix = new Matrix();
		}
		if (bitmap != null) {
			float sx = (float) width / (float) bitmap.getWidth();
			float sy = (float) height / (float) bitmap.getHeight();
			matrix.postScale(sx, sy);
			imageWidth = width;
			imageHeight = height;
		}
	}

	public void setPosition(int left, int top) {
		Rect old = new Rect(rect);
		rect.left = left;
		rect.top = top;
		rect.right = rect.left + imageWidth;
		rect.bottom = rect.top + imageHeight;
		if (matrix == null) {
			old.union(rect);//要刷新的区域，求新矩形区域与旧矩形区域的并集
			invalidate(old);//出于效率考虑，设定脏区域，只进行局部刷新，不是刷新整个view
		}
		else {
			matrix.postTranslate(rect.left - old.left, rect.top - old.top);
			invalidate();
		}
	}

	@Override
	public void setOnClickListener(OnClickListener l) {

		onClickListener = l;
		//super.setOnClickListener(l);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//canvas.drawRect(rect, paint);//画矩形

		if (bitmap != null) {
			if (matrix == null) {
				canvas.drawBitmap(bitmap, rect.left, rect.top, paint);
			}
			else {
				canvas.drawBitmap(bitmap, matrix, paint);
			}
		}
	}

	@Override
	public boolean onTouchEvent (MotionEvent event) {
		if (rect != null) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					//Log.d(TAG, "ACTION_DOWN " + rect.toShortString());
					if(!rect.contains(x, y)) {
						return false;//没有在矩形上点击，不处理触摸消息
					}
					deltaX = x - rect.left;
					deltaY = y - rect.top;

					//onclick check
					lastY = (int) event.getRawY();
					lastX = (int) event.getRawX();
					lastTime = System.currentTimeMillis();
					break;
				case MotionEvent.ACTION_MOVE:
					Rect old = new Rect(rect);
					//更新矩形的位置
					rect.left = x - deltaX;
					rect.top = y - deltaY;
					rect.right = rect.left + imageWidth;
					rect.bottom = rect.top + imageHeight;
					if (matrix == null) {
						old.union(rect);//要刷新的区域，求新矩形区域与旧矩形区域的并集
						invalidate(old);//出于效率考虑，设定脏区域，只进行局部刷新，不是刷新整个view
					}
					else {
						matrix.postTranslate(rect.left - old.left, rect.top - old.top);
						invalidate();
					}
					//Log.d(TAG, "ACTION_MOVE " + rect.toShortString());

					break;
				case MotionEvent.ACTION_UP:
					//onclick check
					checkOnClick(event);
					break;
			}
		}
		return true;//处理了触摸消息，消息不再传递
	}

	private void checkOnClick(MotionEvent event) {
		int dx = 0, dy = 0;
		if (onClickListener != null) {
			dx = (int) event.getRawX() - lastX;
			dy = (int) event.getRawY() - lastY;
			long dTime = System.currentTimeMillis() - lastTime;
			Log.d(TAG, "ACTION_UP dx = " + dx + ", dy = " + dy + ", dTime=" + dTime);
			if (dTime < 100 && dx < 50 && dy < 50) {
				onClickListener.onClick(this);
			}
		}
	}

}
