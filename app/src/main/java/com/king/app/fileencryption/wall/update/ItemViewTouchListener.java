package com.king.app.fileencryption.wall.update;

import java.util.Timer;
import java.util.TimerTask;

import com.king.app.fileencryption.wall.update.NewWallAdapter.ViewHolder;
import com.nineoldandroids.view.ViewHelper;

import android.os.Handler.Callback;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

/**
 * @author JingYang
 * @version create time：2016-2-1 下午1:23:17
 *
 * the function of this class is to handle wall item's click, long click, image zoom event.
 * it depends on NewWallAdapter.ViewHolder, will handle ImageView zoom event.
 */
public class ItemViewTouchListener implements OnTouchListener, Callback {

	private final boolean DEBUG = true;
	private final String TAG = "ItemViewTouchListener";

	private final int MSG_PERFORM_LONGCLICK = 1;

	private final int TIME_LONG_CLICK = 1000;
	private final int TIME_CLICK = 200;

	public static final float ZOOM_MAX = 2.5f;
	public static final float ZOOM_MIN = 0.5f;

	private float startX, startY;
	/**
	 * action is zooming in ACTION_MOVE
	 */
	private boolean isZoom;
	/**
	 * value to record start distance of two finger when ACTION_POINTER_DOWN
	 */
	private float oldDist;
	/**
	 * when zoom out to ZOOM_MAX, need force finish zoom
	 */
	private boolean forceZoomOver;

	private LongClickTimerTask timerTask;
	private Handler handler;

	/**
	 * current touched fake view
	 */
	private View touchView;

	/**
	 * notify mirror process when zoom out
	 */
	private MirrorListener mirrorListener;

	public ItemViewTouchListener(MirrorListener listener) {
		handler = new Handler(this);
		mirrorListener = listener;
	}

	/**
	 * 图片变化将变化全部交给ImageView
	 * click/long click的响应交给view
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {

		ImageView v = ((ViewHolder) view.getTag()).image;

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				startX = event.getRawX();
				startY = event.getRawY();

				// start counting long click time
				touchView = view;
				if (timerTask != null) {
					timerTask.cancel();
				}
				timerTask = new LongClickTimerTask();
				Timer timer = new Timer();
				timer.schedule(timerTask, TIME_LONG_CLICK);

				break;
			case MotionEvent.ACTION_MOVE:
				if (isZoom && event.getPointerCount() > 1 && !forceZoomOver) {
//				Log.d(TAG, "ACTION_MOVE left=" + v.getLeft());
//				int[] pos = new int[2];
//				v.getLocationOnScreen(pos);
//				Log.d(TAG, "ACTION_MOVE pos=[" + pos[0] + "," + pos[1] + "]");

					float x = event.getX(0) - event.getX(1);
					float y = event.getY(0) - event.getY(1);
//				Log.d(TAG, "ACTION_MOVE pointer0=[" + event.getX(0) + "," + event.getY(0) + "]");
//				Log.d(TAG, "ACTION_MOVE pointer1=[" + event.getX(1) + "," + event.getY(1) + "]");
					float newDist = (float) Math.sqrt(x * x + y * y);
					if (newDist > 10f) {
						float zoom = newDist/oldDist;
//					Log.d(TAG, "ACTION_MOVE zoom=" + zoom);
						if (zoom >= ZOOM_MAX) {
							zoom = ZOOM_MAX;
							if (mirrorListener != null) {
								forceZoomOver = true;
								mirrorListener.endMirror(v);
								break;
							}
						}
						else if(zoom < ZOOM_MIN) {
							zoom = ZOOM_MIN;
						}

						ViewHelper.setScaleX(v, zoom);
						ViewHelper.setScaleY(v, zoom);

						if (mirrorListener != null) {
							mirrorListener.processMirror(v, zoom);
						}
					}
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
				Log.d(TAG, "ACTION_POINTER_UP");
				restoreView(v);

				cancleLongClickTask();
				break;
			case MotionEvent.ACTION_UP:
				restoreView(v);

				cancleLongClickTask();

				float curX = event.getRawX();
				float curY = event.getRawY();
				if (Math.abs(curX - startX) < 50 && Math.abs(curY - startY) < 50) {
					long time = event.getEventTime() - event.getDownTime();
					if (time < TIME_CLICK) {
						view.performClick();
					}
				}
				break;
			case MotionEvent.ACTION_OUTSIDE:
				Log.d(TAG, "ACTION_OUTSIDE");
			case MotionEvent.ACTION_CANCEL:
				Log.d(TAG, "ACTION_CANCEL");
				restoreView(v);

				cancleLongClickTask();
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				Log.d(TAG, "ACTION_POINTER_DOWN");
				isZoom = true;
				forceZoomOver = false;
				float x = event.getX(0) - event.getX(1);
				float y = event.getY(0) - event.getY(1);
				oldDist = (float) Math.sqrt(x * x + y * y);
				// disable touch event of parent view
				v.getParent().requestDisallowInterceptTouchEvent(true);

				cancleLongClickTask();

				if (mirrorListener != null) {
					mirrorListener.startMirror(v);
				}
				break;

			default:
				break;
		}
		return true;
	}

	private void restoreView(View v) {
		ViewHelper.setScaleX(v, 1);
		ViewHelper.setScaleY(v, 1);
		isZoom = false;
		// enable touch event of parent view
		v.getParent().requestDisallowInterceptTouchEvent(false);

		if (!forceZoomOver) {
			if (mirrorListener != null) {
				mirrorListener.cancelMirror(touchView);
			}
		}
	}

	private void cancleLongClickTask() {
		if (timerTask != null) {
			timerTask.setExecutable(false);
			timerTask.cancel();
		}
	}

	/**
	 *
	 * @author JingYang
	 * TIME_LONG_CLICK time after ACTION_DOWN, need perform long click event.
	 * if other action happened which resulted in long click should be intercepted,
	 * then don't perform long click event
	 */
	private class LongClickTimerTask extends TimerTask {

		private boolean exec;

		public LongClickTimerTask() {
			exec = true;
		}

		public void setExecutable(boolean exec) {
			this.exec = exec;
		}
		@Override
		public void run() {
			if (DEBUG) {
				Log.d(TAG, "run exec " + exec);
			}
			if (exec) {
				handler.sendEmptyMessage(MSG_PERFORM_LONGCLICK);
			}
		}

		@Override
		public boolean cancel() {
			if (DEBUG) {
				Log.d(TAG, "cancel");
			}
			return super.cancel();
		}

	}

	@Override
	public boolean handleMessage(Message msg) {

		if (msg.what == MSG_PERFORM_LONGCLICK) {
			touchView.performLongClick();
		}
		return false;
	}

}
