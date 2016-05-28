package com.king.app.fileencryption.timeline;

import java.util.List;

import com.king.app.fileencryption.MainViewActivity;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.abview.AbOnListViewListener;
import com.king.app.fileencryption.abview.AbPullListView;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.filemanager.entity.FileBean;
import com.king.app.fileencryption.guide.GuideActivity;
import com.king.app.fileencryption.setting.SettingActivity;
import com.king.app.fileencryption.timeline.TimeLineAdapter.ViewHolder;
import com.king.app.fileencryption.timeline.controller.TimeLineController;
import com.king.app.fileencryption.util.DisplayHelper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

@Deprecated
public class TimeLineActivity extends Activity implements AbOnListViewListener
		, OnScrollListener, OnClickListener {

	private ImageView indicatorImageView;
	private TextView timeTextView;
	private TextView numTextView;
	private AbPullListView listView;
	private View indicatorView;
	private TimeLineAdapter timeLineAdapter;

	private TextView mainButton;
	private TextView orderButton;
	private TextView settingButton;
	private TextView closeButton;

	private TimeLineController timeLineController;

	private int currentPosition = -1;

	private int indicatorTop, indicatorHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DisplayHelper.enableFullScreen(this);
		DisplayHelper.disableScreenshot(this);

		setContentView(R.layout.activity_timeline);

		indicatorImageView = (ImageView) findViewById(R.id.timeline_indicator_image);
		timeTextView = (TextView) findViewById(R.id.timeline_indicator_text);
		numTextView = (TextView) findViewById(R.id.timeline_indicator_num);
		mainButton = (TextView) findViewById(R.id.timeline_indicator_main);
		orderButton = (TextView) findViewById(R.id.timeline_indicator_guide);
		settingButton = (TextView) findViewById(R.id.timeline_indicator_setting);
		closeButton = (TextView) findViewById(R.id.timeline_indicator_close);
		indicatorView = findViewById(R.id.timeline_indicator);
		listView = (AbPullListView) findViewById(R.id.timeline_listview);
		listView.setPullRefreshEnable(false);
		listView.setPullLoadEnable(true);
		listView.setAbOnListViewListener(this);

		timeLineController = new TimeLineController();
		boolean result = timeLineController.loadTimeGroups();
		if (result) {
			timeLineController.nextPage();
			timeLineAdapter = new TimeLineAdapter(this, timeLineController);
			listView.setAdapter(timeLineAdapter);
		}

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				listView.setOnScrollListener(TimeLineActivity.this);
			}
		}, 500);

		indicatorView.post(new Runnable() {

			@Override
			public void run() {
				int[] locs = new int[2];
				indicatorView.getLocationOnScreen(locs);
				indicatorTop = locs[1];
				indicatorHeight = indicatorView.getHeight();
				indicatorView.setVisibility(View.GONE);
				if (Constants.DEBUG) {
					Log.d(Constants.LOG_SCROLL, "indicatorTop=" + indicatorTop + ", indicatorHeight=" + indicatorHeight);
				}
			}
		});
		mainButton.setOnClickListener(this);
		orderButton.setOnClickListener(this);
		closeButton.setOnClickListener(this);
		settingButton.setOnClickListener(this);
		indicatorImageView.setOnClickListener(this);

		mainButton.setBackground(getOvalDrawable(getResources().getColor(R.color.timeline_menu_main_bk)));
		orderButton.setBackground(getOvalDrawable(getResources().getColor(R.color.timeline_menu_order_bk)));
		closeButton.setBackground(getOvalDrawable(getResources().getColor(R.color.timeline_menu_setting_bk)));
		settingButton.setBackground(getOvalDrawable(getResources().getColor(R.color.timeline_menu_close_bk)));
	}

	private ShapeDrawable getOvalDrawable(int color) {
		ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
		drawable.getPaint().setColor(color);
		return drawable;
	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadMore() {
		timeLineController.nextPage();
		// controller负责控制页面，在getGroupList里直接累加页面
		timeLineAdapter.notifyDataSetChanged();

		if (!timeLineController.hasNextPage()) {
			listView.setPullLoadEnable(false);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
			case SCROLL_STATE_FLING:
				break;
			case SCROLL_STATE_IDLE:
				break;
			case SCROLL_STATE_TOUCH_SCROLL:
				break;

			default:
				break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
						 int visibleItemCount, int totalItemCount) {//firstVisibleItem 是从1开始的

		boolean executed = false;
		for (int i = 0; i < timeLineAdapter.getHolderList().size(); i ++) {
			ViewHolder holder = timeLineAdapter.getHolderList().get(i);

			if (holder != null) {
				// 只检测tag页码为0的indicator
				if (holder.flag != ViewHolder.FLAG_TIME_PAGE) {
					View indicator = holder.indicator;
					int[] locs = new int[2];
					indicator.getLocationOnScreen(locs);
					int top = locs[1];
					if (i < firstVisibleItem - 1) {
						top = -2000;//很奇怪当滑到第4个item的时候，第二个和第四个的locs[1]一起变化了
					}
					executed = onIndicatorScroll(indicator, i, top);
					if (executed) {//只要有一个执行了就要break，不然后面的可能会冲掉
						break;
					}
				}
			}
		}

		if (Constants.DEBUG) {
			Log.d(Constants.LOG_SCROLL, "onScroll executed " + executed);
		}
		if (!executed) {//没有indicator达到变化范围，则一直显示当前第一个可见item的indicator
			indicatorView.setVisibility(View.VISIBLE);
			updateIndicator(firstVisibleItem - 1);
		}

	}

	private boolean onIndicatorScroll(View holderIndicator, int position, int topInScreen) {
		if (Constants.DEBUG) {
			Log.d(Constants.LOG_SCROLL, "onIndicatorScroll position=" + position + ", topInScreen=" + topInScreen);
		}

		boolean executed = false;
		if (topInScreen >= indicatorTop && topInScreen <= indicatorTop + indicatorHeight) {
			indicatorView.setVisibility(View.GONE);
			holderIndicator.setVisibility(View.VISIBLE);
			executed = true;
		}
		else if (topInScreen < indicatorTop && topInScreen > indicatorTop - indicatorHeight) {
			indicatorView.setVisibility(View.VISIBLE);
			updateIndicator(position);
			holderIndicator.setVisibility(View.INVISIBLE);
			executed = true;
		}
		else {
			holderIndicator.setVisibility(View.VISIBLE);
		}
		return executed;
	}

	private void updateIndicator(int position) {
		if (position != currentPosition) {
			currentPosition = position;

			if (position < timeLineAdapter.getHolderList().size()) {
				ViewHolder holder = timeLineAdapter.getHolderList().get(position);
				if (holder != null) {
					String timeTag = holder.timeTag;
					String num = timeLineController.getGroupList().get(position).get("count");
					List<FileBean> list = timeLineController.getContentMap().get(timeTag);
					String path = list.get(holder.indicatorIndex).getPath();

//					indicatorImageView.setImageBitmap(null);
					new LoadImageTask().execute(path);

					if (timeTag.contains(":")) {
						timeTextView.setText(timeTag.split(":")[0]);
					}
					else {
						timeTextView.setText(timeTag);
					}
					numTextView.setText(num + "张");
				}
			}
		}
	}

	private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... params) {
			// 不能用getOrderCircleCover
			Bitmap bitmap = PictureManagerUpdate.getInstance().createCircleBitmap(
					params[0], TimeLineActivity.this);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			indicatorImageView.setImageBitmap(bitmap);
			super.onPostExecute(bitmap);
		}

	}

	/**
	 * origin codes issue: LinearInterpolator is not working in xml definition
	 * must use java codes to set it
	 * @return
	 */
	private Animation getRotateAnimation() {
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.thumb_folder_click);
		LinearInterpolator interpolator = new LinearInterpolator();
		animation.setInterpolator(interpolator);
		return animation;
	}

	@Override
	public void onClick(View v) {
		if (v == mainButton) {
			Intent intent = new Intent().setClass(this, MainViewActivity.class);
			startActivity(intent);
			finish();
		}
		else if (v == indicatorImageView) {
			indicatorImageView.startAnimation(getRotateAnimation());
			mainButton.setVisibility(View.VISIBLE);
			orderButton.setVisibility(View.VISIBLE);
			settingButton.setVisibility(View.VISIBLE);
			closeButton.setVisibility(View.VISIBLE);
			mainButton.startAnimation(getMenuAppearAnimation(0));
			orderButton.startAnimation(getMenuAppearAnimation(1));
			settingButton.startAnimation(getMenuAppearAnimation(2));
			closeButton.startAnimation(getMenuAppearAnimation(3));
		}
		else if (v == orderButton) {
			Intent intent = new Intent().setClass(this, GuideActivity.class);
			startActivity(intent);
			finish();
		}
		else if (v == settingButton) {
			Intent intent = new Intent().setClass(this, SettingActivity.class);
			startActivity(intent);
		}
		else if (v == closeButton) {
			mainButton.setVisibility(View.GONE);
			orderButton.setVisibility(View.GONE);
			settingButton.setVisibility(View.GONE);
			closeButton.setVisibility(View.GONE);
			closeButton.startAnimation(getMenuDisappearAnimation(0));
			settingButton.startAnimation(getMenuDisappearAnimation(1));
			orderButton.startAnimation(getMenuDisappearAnimation(2));
			mainButton.startAnimation(getMenuDisappearAnimation(3));
		}
	}

	private Animation getMenuAppearAnimation(int index) {
		AnimationSet set = new AnimationSet(true);
		TranslateAnimation tanimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0
				, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0);
		AlphaAnimation aanimation = new AlphaAnimation(0, 1.0f);
		tanimation.setDuration(100 + index * 90);
		aanimation.setDuration(100 + index * 90);
		set.addAnimation(tanimation);
		set.addAnimation(aanimation);
		set.setStartOffset(index * 100 + index * (index - 1) * 90 / 2);//等差数列求和
		return set;
	}

	private Animation getMenuDisappearAnimation(int index) {
		AnimationSet set = new AnimationSet(true);
		TranslateAnimation tanimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0
				, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1.0f);
		tanimation.setDuration(150);
		AlphaAnimation aanimation = new AlphaAnimation(1.0f, 0);
		aanimation.setDuration(150);
		set.addAnimation(tanimation);
		set.addAnimation(aanimation);
		set.setStartOffset(index * 150);
		return set;
	}
}
