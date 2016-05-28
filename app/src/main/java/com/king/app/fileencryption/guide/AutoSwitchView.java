package com.king.app.fileencryption.guide;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

/**
 * 采用2个ImageView轮流更替显示的方法实现
 * 显示的图像由AutoSwitchAdapter适配
 * 模式参考ListView/GridView与Adapter
 * @author JingYang
 *
 */
public class AutoSwitchView extends RelativeLayout implements OnClickListener {

	private static long viewId;

	private long mId;

	public enum AnimationType {
		NONE, /** 无动画效果 **/
		ALPHA_SCALE, /** foreground:alpha 1->0, scale:1->0; background reverse **/
		ALPHA, /** foreground:alpha 1->0; background reverse **/
		TRANSLATE, /**  **/
		TRANSLATE_ALPHA /**  **/
	}
	private class AnimationPackage {
		Animation disAppear;
		Animation appear;
	}

	public interface ActionListener {
		public void onAutoSwitchViewClick(View parent, View view);
	}

	private ImageView imageView1, imageView2;
	private ImageView forgroundView, backgoundView;
	private ActionListener actionListener;

	private AutoSwitchAdapter adapter;
	private DisapplearAnimationListener disListener;

	private final int MSG_PLAY_NEXT = 1;

	private final long DEFAULT_PLAY_TIME = 4000;
	private final long ANIM_TIME = 1500;

	private long playTime;
	private AnimationPackage animationPackage;

	private boolean isRun;

	public AutoSwitchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AutoSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	/**
	 * 按照图层关系，最开始imageView2是前景view
	 */
	private void init() {
		mId = viewId;
		viewId ++;

		playTime = DEFAULT_PLAY_TIME;

		imageView1 = new ImageView(getContext());
		imageView1.setScaleType(ScaleType.CENTER_CROP);
		imageView1.setOnClickListener(this);
		addView(imageView1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		backgoundView = imageView1;

		imageView2 = new ImageView(getContext());
		imageView2.setScaleType(ScaleType.CENTER_CROP);
		imageView2.setOnClickListener(this);
		addView(imageView2, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		forgroundView = imageView2;
	}

	/**
	 * 关联adapter，
	 * @param adapter
	 */
	public void setAdapter(AutoSwitchAdapter adapter) {
		this.adapter = adapter;
		adapter.setAutoSwitchView(this);
	}

	public void setActionListener(ActionListener listener) {
		actionListener = listener;
	}

	/**
	 * 前景view显示时长，该时间后切换前景view与背景view
	 * @param time 单位：毫秒
	 */
	public void setPlayTime(long time) {
		playTime = time;
	}

	public void setAnimationType(AnimationType type) {
		animationPackage = new AnimationManager().getAnimation(type);
	}

	/**
	 * 开始播放
	 */
	public void startPlay() {
		if (adapter != null && adapter.getCount() > 1) {
//			forgroundView.setImageResource(R.drawable.icon_loading);
			isRun = true;
			adapter.loadNextImage(forgroundView);
			new PlayThread().start();
		}
	}

	/**
	 * 延时播放
	 * @param time
	 */
	public void postStartPlay(long time) {
		if (adapter != null && adapter.getCount() > 1) {
			adapter.loadNextImage(forgroundView);
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					isRun = true;
					new PlayThread().start();
				}
			}, time);
		}
	}

	/**
	 * 停止播放线程
	 */
	public void stop() {
		isRun = false;
	}

	/**
	 * 每隔playTime时间开始通知切换前景view背景view
	 * @author JingYang
	 *
	 */
	private class PlayThread extends Thread {

		@Override
		public void run() {
			while (isRun) {
				try {
					Thread.sleep(playTime);
					Log.d("AutoSwitchView", "play next");
					Message message = new Message();
					message.what = MSG_PLAY_NEXT;
					message.obj = mId;
					mHandler.sendMessage(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_PLAY_NEXT:
					long id = (Long) msg.obj;
					if (id == mId) {
						playNext();
					}
//				playNext();
					break;

				default:
					break;
			}
		}

	};

	/**
	 * 重启播放
	 */
	public void restart() {
		forgroundView.setImageBitmap(null);
		backgoundView.setImageBitmap(null);
		forgroundView = imageView2;
		backgoundView = imageView1;
		adapter.recycleAll();
		startPlay();
	}

	/**
	 * 前景view背景view的切换过程
	 */
	public void playNext() {
		if (animationPackage == null) {//无动画效果
			if (forgroundView == imageView1) {
				forgroundView = imageView2;
				backgoundView = imageView1;
			}
			else {
				forgroundView = imageView1;
				backgoundView = imageView2;
			}
			forgroundView.setVisibility(View.VISIBLE);
			backgoundView.setVisibility(View.GONE);
			adapter.loadNextImage(forgroundView);

			onForBackChanged();
		}
		else {
			disappear(forgroundView);
			appear(backgoundView);
		}
	}

	/**
	 * 前景背景交换完成
	 */
	private void onForBackChanged() {
		forgroundView.setOnClickListener(this);
		backgoundView.setOnClickListener(null);
	}

	/**
	 * disappear view由于要执行动画，需要在动画结束后再清空imageView对图片的引用
	 * @param view
	 */
	private void disappear(ImageView view) {
		if (disListener == null) {
			disListener = new DisapplearAnimationListener();
		}
		animationPackage.disAppear.setAnimationListener(disListener);
		view.startAnimation(animationPackage.disAppear);
	}
	/**
	 * appear view立即VISIBLE并加载图片
	 * @param view
	 */
	private void appear(ImageView view) {
		view.setVisibility(View.VISIBLE);
		adapter.loadNextImage(view);

		view.startAnimation(animationPackage.appear);
	}

	private class DisapplearAnimationListener implements AnimationListener {

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {

			forgroundView.setVisibility(View.GONE);
//			forgroundView.setImageBitmap(null);

			if (forgroundView == imageView1) {
				backgoundView = imageView1;
				forgroundView = imageView2;
			}
			else {
				backgoundView = imageView2;
				forgroundView = imageView1;
			}

			onForBackChanged();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

	}

	private class AnimationManager {

		public AnimationPackage getAnimation(AnimationType type) {
			if (type == AnimationType.NONE) {
				return null;
			}
			else if (type == AnimationType.ALPHA_SCALE) {
				return createAlphaScaleAnim();
			}
			else if (type == AnimationType.ALPHA) {
				return createAlphaAnim();
			}
			else if (type == AnimationType.TRANSLATE) {
				return createTranslateAnim();
			}
			else if (type == AnimationType.TRANSLATE_ALPHA) {
				return createTranslateAlphaAnim();
			}
			return null;
		}

		private AnimationPackage createTranslateAlphaAnim() {
			AnimationPackage anim = new AnimationPackage();

			TranslateAnimation tAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0
					, Animation.RELATIVE_TO_SELF, -1.0f
					, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
			tAnim.setDuration(ANIM_TIME);
			anim.disAppear = tAnim;

			AnimationSet set = new AnimationSet(true);
			Animation aAnim = new AlphaAnimation(0, 1.0f);
			set.addAnimation(aAnim);
			Animation sAnim = new ScaleAnimation(0, 1.0f, 0, 1.0f
					, Animation.RELATIVE_TO_SELF, 0.5f
					, Animation.RELATIVE_TO_SELF, 0.5f);
			set.addAnimation(sAnim);
			set.setDuration(ANIM_TIME);
			anim.appear = set;
			return anim;
		}

		/**
		 * 横向轮播
		 * @return
		 */
		private AnimationPackage createTranslateAnim() {
			AnimationPackage anim = new AnimationPackage();

			TranslateAnimation tAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0
					, Animation.RELATIVE_TO_SELF, -1.0f
					, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
			tAnim.setDuration(ANIM_TIME);
			anim.disAppear = tAnim;
			tAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f
					, Animation.RELATIVE_TO_SELF, 0
					, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
			tAnim.setDuration(ANIM_TIME);
			anim.appear = tAnim;
			return anim;
		}

		/**
		 * 前景透明度逐渐变化至全透明，背景透明度逐渐变化至全不透明
		 * @return
		 */
		private AnimationPackage createAlphaAnim() {
			AnimationPackage anim = new AnimationPackage();

			AlphaAnimation aAnim = new AlphaAnimation(1.0f, 0);
			anim.disAppear = aAnim;
			aAnim.setDuration(ANIM_TIME);

			aAnim = new AlphaAnimation(0, 1.0f);
			anim.appear = aAnim;
			aAnim.setDuration(ANIM_TIME);
			return anim;
		}

		/**
		 * 透明度同上，与此同时前景缩放至消失，背景从消失放大至完整大小
		 * @return
		 */
		private AnimationPackage createAlphaScaleAnim() {

			AnimationPackage anim = new AnimationPackage();

			AnimationSet set = new AnimationSet(true);
			AlphaAnimation aAnim = new AlphaAnimation(1.0f, 0);
			set.addAnimation(aAnim);
			ScaleAnimation sAnim = new ScaleAnimation(1.0f, 0, 1.0f, 0
					, Animation.RELATIVE_TO_SELF, 0.5f
					, Animation.RELATIVE_TO_SELF, 0.5f);
			set.addAnimation(sAnim);
			set.setDuration(ANIM_TIME);
			anim.disAppear = set;

			set = new AnimationSet(true);
			aAnim = new AlphaAnimation(0, 1.0f);
			set.addAnimation(aAnim);
			sAnim = new ScaleAnimation(0, 1.0f, 0, 1.0f
					, Animation.RELATIVE_TO_SELF, 0.5f
					, Animation.RELATIVE_TO_SELF, 0.5f);
			set.addAnimation(sAnim);
			set.setDuration(ANIM_TIME);
			anim.appear = set;
			return anim;
		}
	}

	@Override
	public void onClick(View v) {
		if (actionListener != null) {
			if (v == forgroundView) {
				actionListener.onAutoSwitchViewClick(this, v);
			}
		}
	}
}
