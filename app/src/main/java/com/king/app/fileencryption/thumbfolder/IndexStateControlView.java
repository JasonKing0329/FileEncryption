package com.king.app.fileencryption.thumbfolder;

import com.king.app.fileencryption.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;

@Deprecated
/**
 * drag effect ok, but apply in layout, it has some issues
 * click on other view on page or change orientation or click controller itsself, this view always back to the origin position
 * the position relationship of view and screen is not good
 * @author tstcit
 *
 */
public class IndexStateControlView extends View implements OnTouchListener {

	private final String TAG = "IndexStateControlView";
	protected int lastX;
	protected int lastY;
	protected int screenWidth;
	protected int screenHeight;
	private int offset;
	private Paint paint = new Paint();
	private Bitmap bitmap;
	private long lastTime;
	private OnClickListener onClickListener;

	public IndexStateControlView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public IndexStateControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public IndexStateControlView(Context context) {
		super(context);
		init();
	}

	public void init() {
		setOnTouchListener(this);
		screenHeight = getResources().getDisplayMetrics().heightPixels;
		screenWidth = getResources().getDisplayMetrics().widthPixels;
		BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.index_control);
		bitmap = drawable.getBitmap();
		//SIZE_TEXT = getResources().getDimensionPixelSize(R.dimen.cropinfor_text_size);
	}

	public int getViewWidth() {
		return bitmap.getWidth();
	}

	public int getViewHeight() {
		return bitmap.getHeight();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawBitmap(bitmap, 0, 0, paint);

		super.onDraw(canvas);
	}

	public void setArea(int left, int top, int right, int bottom) {
		/**
		 * onTouch事件中，用layout和invalidate可以实时刷新view的位置大小，但是在UI主线程调用这种方法却又无法改变
		 * 不知道具体原因，可能是跟UI线程有关
		 //    	layout(left, top, right, bottom);
		 //    	invalidate();
		 */
		//采用这种方法可以
		ViewGroup.LayoutParams params = getLayoutParams();
		params.width = right - left;
		params.height = bottom - top;
		((MarginLayoutParams) params).leftMargin = left;
		((MarginLayoutParams) params).topMargin = top;
	}

	public void setOffset(int left, int top) {
		ViewGroup.LayoutParams params = getLayoutParams();
		((MarginLayoutParams) params).leftMargin = left;
		((MarginLayoutParams) params).topMargin = top;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				Log.d(TAG, "ACTION_DOWN");
				lastY = (int) event.getRawY();
				lastX = (int) event.getRawX();
				lastTime = System.currentTimeMillis();
				break;
			case MotionEvent.ACTION_MOVE:
				Log.d(TAG, "ACTION_MOVE");
				int dx = (int) event.getRawX() - lastX;
				int dy = (int) event.getRawY() - lastY;
				move(v, dx, dy);
				lastX = (int) event.getRawX();
				lastY = (int) event.getRawY();
				break;
			case MotionEvent.ACTION_UP:
				Log.d(TAG, "ACTION_UP");
				if (onClickListener != null) {
					dx = (int) event.getRawX() - lastX;
					dy = (int) event.getRawY() - lastY;
					long dTime = System.currentTimeMillis() - lastTime;
					Log.d(TAG, "ACTION_UP dx = " + dx + ", dy = " + dy + ", dTime=" + dTime);
					if (dTime < 100 && dx < 50 && dy < 50) {
						onClickListener.onClick(this);
					}
				}
				break;

			default:
				break;
		}
		invalidate();
		return false;
	}

	private void move(View v, int dx, int dy) {
		int left = v.getLeft() + dx;
		int top = v.getTop() + dy;
		int right = v.getRight() + dx;
		int bottom = v.getBottom() + dy;
		if (left < -offset) {
			left = -offset;
			right = left + v.getWidth();
		}
		if (right > screenWidth + offset) {
			right = screenWidth + offset;
			left = right - v.getWidth();
		}
		if (top < -offset) {
			top = -offset;
			bottom = top + v.getHeight();
		}
		if (bottom > screenHeight + offset) {
			bottom = screenHeight + offset;
			top = bottom - v.getHeight();
		}
		Log.d(TAG, "layout " + left + "," + top + "," + right + "," + bottom);
		v.layout(left, top, right, bottom);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {

		onClickListener = l;
		//super.setOnClickListener(l);
	}

}
