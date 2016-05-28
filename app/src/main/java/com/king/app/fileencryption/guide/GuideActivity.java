package com.king.app.fileencryption.guide;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.MainViewActivity;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.guide.AutoSwitchView.AnimationType;
import com.king.app.fileencryption.guide.SideViewManager.OnSideListener;
import com.king.app.fileencryption.guide.controller.GuideController;
import com.king.app.fileencryption.publicview.DragSideBar;
import com.king.app.fileencryption.publicview.DragSideBarTrigger;
import com.king.app.fileencryption.setting.SettingActivity;
import com.king.app.fileencryption.thumbfolder.ShowImageDialog;
import com.king.app.fileencryption.timeline.TimeLineActivity;
import com.king.app.fileencryption.timeline.update.TimeLineUpdateActivity;
import com.king.app.fileencryption.util.DisplayHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GuideActivity extends Activity implements AutoSwitchView.ActionListener
		, AutoScrollView.ActionListener, OnSideListener {

	private AutoSwitchView line1LeftView;
	private AutoSwitchView line1Right1View;
	private AutoSwitchView line1Right2View;
	private AutoSwitchView line2Square1View;
	private AutoSwitchView line2Square2View;
	private AutoSwitchView line2Square3View;
	private AutoSwitchView line2Square4View;
	private AutoSwitchView line2RightView;
	private SimpleAutoSwitchAdapter line1LeftAdapter;
	private SimpleAutoSwitchAdapter line1Right1Adapter;
	private SimpleAutoSwitchAdapter line1Right2Adapter;
	private SimpleAutoSwitchAdapter line2Square1Adapter;
	private SimpleAutoSwitchAdapter line2Square2Adapter;
	private SimpleAutoSwitchAdapter line2Square3Adapter;
	private SimpleAutoSwitchAdapter line2Square4Adapter;
	private SimpleAutoSwitchAdapter line2RightAdapter;
	private List<String> line1LeftList;
	private List<String> line1Right1List;
	private List<String> line1Right2List;
	private List<String> line2Square1List;
	private List<String> line2Square2List;
	private List<String> line2Square3List;
	private List<String> line2Square4List;
	private List<String> line2RightList;
	private List<String> line3List;

	private AutoScrollView line3AutoScrollView;
	private SimpleAutoScrollAdapter line3AutoScrollAdapter;

	//activity处于onStop状态时，通知所有的AutoSwitchView停止线程
	private List<AutoSwitchView> autoSwitchViews;

	private GuideController guideController;

	private ShowImageDialog showImageDialog;

	private PlayThread playThread;

	private View sideView;
	private DragSideBar dragSideBar;
	private DragSideBarTrigger dragSideBarTrigger;
	private SideViewManager sideViewManager;

	// 如果没有数据，所有线程不能开启
	private boolean executable;
	private View noContentView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		DisplayHelper.enableFullScreen(this);
		DisplayHelper.disableScreenshot(this);

		setContentView(R.layout.activity_guide);

		guideController = new GuideController(this);

		dragSideBar = (DragSideBar) findViewById(R.id.guide_sidebar);
		sideView = findViewById(R.id.guide_side);
		sideView.setOnTouchListener(sideTouchListener);
		dragSideBarTrigger = new DragSideBarTrigger(this, dragSideBar);
		sideViewManager = new SideViewManager(this, dragSideBar);
		sideViewManager.setOnSideListener(this);

		autoSwitchViews = new ArrayList<AutoSwitchView>();
		initLine1Left();
		initLine1Right();
		initLine2Square();
		initLine2Right();

		executable = false;
		if (line1LeftList != null) {
			line1LeftView.playNext();
			executable = true;
		}
		if (line1Right1List != null) {
			line1Right1View.playNext();
			executable = true;
		}
		if (line1Right2List != null) {
			line1Right2View.playNext();
			executable = true;
		}
		if (line2Square1List != null) {
			line2Square1View.playNext();
			executable = true;
		}
		if (line2Square2List != null) {
			line2Square2View.playNext();
			executable = true;
		}
		if (line2Square3List != null) {
			line2Square3View.playNext();
			executable = true;
		}
		if (line2Square4List != null) {
			line2Square4View.playNext();
			executable = true;
		}
		if (line2RightList != null) {
			line2RightView.playNext();
			executable = true;
		}
		if (executable) {
			/**
			 * 执行AutoSwitchView的startPlay或postStartPlay，在多个AutoSwitchView存在的时候
			 * 多线程运行混乱，达不到预期的效果，采用单线程按时序通知单个AutoSwitchView更新
			 */
			playThread = new PlayThread();
			playThread.start();
		}
		else {
			noContentView = findViewById(R.id.guide_nocontent);
			noContentView.setVisibility(View.VISIBLE);
		}

		initLine3AutoScroll();
	}
	private void initLine3AutoScroll() {
		line3AutoScrollView = (AutoScrollView) findViewById(R.id.guide_line3_autoscroll);
		line3List = guideController.getline3List();
		line3AutoScrollAdapter = new SimpleAutoScrollAdapter(line3List);
		line3AutoScrollView.setAdapter(line3AutoScrollAdapter);
		line3AutoScrollView.setActionListener(this);
		if (line3List != null) {
			line3AutoScrollView.startScroll();
		}
	}

	private void initLine1Left() {
		line1LeftView = (AutoSwitchView) findViewById(R.id.guide_autoview_line1_left);
		line1LeftList = guideController.getLine1LeftList();
		line1LeftAdapter = new SimpleAutoSwitchAdapter(line1LeftList);
		line1LeftView.setAdapter(line1LeftAdapter);
		line1LeftView.setAnimationType(AnimationType.ALPHA_SCALE);
		line1LeftView.setActionListener(this);
		autoSwitchViews.add(line1LeftView);
	}
	private void initLine1Right() {
		line1Right1View = (AutoSwitchView) findViewById(R.id.guide_autoview_line1_right1);
		line1Right1List = guideController.getLine1RightList();
		line1Right1Adapter = new SimpleAutoSwitchAdapter(line1Right1List);
		line1Right1View.setAdapter(line1Right1Adapter);
		line1Right1View.setAnimationType(AnimationType.TRANSLATE);
		line1Right1View.setActionListener(this);
		autoSwitchViews.add(line1Right1View);

		line1Right2View = (AutoSwitchView) findViewById(R.id.guide_autoview_line1_right2);
		line1Right2List = guideController.getLine1RightList();
		line1Right2Adapter = new SimpleAutoSwitchAdapter(line1Right2List);
		line1Right2View.setAdapter(line1Right2Adapter);
		line1Right2View.setAnimationType(AnimationType.ALPHA);
		line1Right2View.setActionListener(this);
		autoSwitchViews.add(line1Right2View);
	}

	private void initLine2Square() {
		line2Square1View = (AutoSwitchView) findViewById(R.id.guide_line2_square1);
		line2Square1List = guideController.getLine2SquareList();
		line2Square1Adapter = new SimpleAutoSwitchAdapter(line2Square1List);
		line2Square1View.setAdapter(line2Square1Adapter);
		line2Square1View.setAnimationType(AnimationType.TRANSLATE_ALPHA);
		line2Square1View.setActionListener(this);
		autoSwitchViews.add(line2Square1View);

		line2Square2View = (AutoSwitchView) findViewById(R.id.guide_line2_square2);
		line2Square2List = guideController.getLine2SquareList();
		line2Square2Adapter = new SimpleAutoSwitchAdapter(line2Square2List);
		line2Square2View.setAdapter(line2Square2Adapter);
		line2Square2View.setAnimationType(AnimationType.ALPHA);
		line2Square2View.setActionListener(this);
		autoSwitchViews.add(line2Square2View);

		line2Square3View = (AutoSwitchView) findViewById(R.id.guide_line2_square3);
		line2Square3List = guideController.getLine2SquareList();
		line2Square3Adapter = new SimpleAutoSwitchAdapter(line2Square3List);
		line2Square3View.setAdapter(line2Square3Adapter);
		line2Square3View.setAnimationType(AnimationType.ALPHA);
		line2Square3View.setActionListener(this);
		autoSwitchViews.add(line2Square3View);

		line2Square4View = (AutoSwitchView) findViewById(R.id.guide_line2_square4);
		line2Square4List = guideController.getLine2SquareList();
		line2Square4Adapter = new SimpleAutoSwitchAdapter(line2Square4List);
		line2Square4View.setAdapter(line2Square4Adapter);
		line2Square4View.setAnimationType(AnimationType.TRANSLATE);
		line2Square4View.setActionListener(this);
		autoSwitchViews.add(line2Square4View);

	}

	private void initLine2Right() {

		line2RightView = (AutoSwitchView) findViewById(R.id.guide_line2_right);
		line2RightList = guideController.getLine2RightList();
		line2RightAdapter = new SimpleAutoSwitchAdapter(line2RightList);
		line2RightView.setAdapter(line2RightAdapter);
		line2RightView.setAnimationType(AnimationType.ALPHA);
		line2RightView.setActionListener(this);
		autoSwitchViews.add(line2RightView);
	}

	/**
	 * 触发sidebar的touch事件
	 */
	OnTouchListener sideTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			if (dragSideBarTrigger.onTriggerTouch(event)) {
				return true;
			}
			return true;
		}
	};
	@Override
	protected void onStop() {
		//停止AutoSwitchView自动播放
		isRun = false;
		if (playThread != null && playThread.isAlive()) {
			playThread.interrupt();
		}

		//停止AutoSrollView自动滚动
		if (line3AutoScrollView != null) {
			line3AutoScrollView.stop();
		}
		super.onStop();
	}

	@Override
	protected void onResume() {

		if (executable) {
			//AutoSwitchView重新播放
			if (!isRun) {
				playThread = new PlayThread();
				playThread.start();
			}

			//AutoSrollView重新滚动
			if (line3AutoScrollView != null) {
				line3AutoScrollView.restart();
			}
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private final int MSG_NEXT_LINE1_LEFT = 1;
	private final int MSG_NEXT_LINE1_RIGHT = 2;
	private final int MSG_NEXT_LINE2_SQUARE = 3;
	private final int MSG_NEXT_LINE2_RIGHT = 4;
	private boolean isRun;

	private class PlayThread extends Thread {
		public PlayThread() {
			isRun = true;
		}

		@Override
		public void run() {
			while (isRun) {
				try {
					Thread.sleep(5000);

					mHandler.sendEmptyMessage(MSG_NEXT_LINE1_LEFT);

					Thread.sleep(500);

					mHandler.sendEmptyMessage(MSG_NEXT_LINE1_RIGHT);

					Thread.sleep(1000);

					mHandler.sendEmptyMessage(MSG_NEXT_LINE2_SQUARE);

					Thread.sleep(1000);

					mHandler.sendEmptyMessage(MSG_NEXT_LINE2_RIGHT);

				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}

	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_NEXT_LINE1_LEFT:
					line1LeftView.playNext();
					break;
				case MSG_NEXT_LINE1_RIGHT:
					line1Right1View.playNext();
					line1Right2View.playNext();
					break;
				case MSG_NEXT_LINE2_SQUARE:
					line2Square1View.playNext();
					line2Square2View.playNext();
					line2Square3View.playNext();
					line2Square4View.playNext();
					break;
				case MSG_NEXT_LINE2_RIGHT:
					line2RightView.playNext();
					break;

				default:
					break;
			}
		}

	};
	@Override
	public void onAutoSwitchViewClick(View parent, View view) {
		if (view.getTag() != null) {
			String path = (String) view.getTag();
			showImage(path);
		}
	}

	@Override
	public void onAutoScrollViewClick(View parent, View view) {
		if (view.getTag() != null) {
			String path = (String) view.getTag();
			showImage(path);
		}
	}

	private void showImage(String path) {
		if (Configuration.DEBUG) {
			Log.d(Configuration.TAG_AUTO_VIEW, "onAutoSwitchViewClick path=" + path);
		}

		if (showImageDialog == null) {
			showImageDialog = new ShowImageDialog(this, null, 0);
		}
		showImageDialog.setImagePath(path);
		showImageDialog.show();
	}
	@Override
	public void onMenuMainView() {

		Intent intent = new Intent().setClass(this, MainViewActivity.class);
		startActivity(intent);
		finish();
	}
	@Override
	public void onMenuTimeLine() {

		Intent intent = new Intent().setClass(this, TimeLineUpdateActivity.class);
		startActivity(intent);
		finish();
	}
	@Override
	public void onMenuSetting() {

		Intent intent = new Intent().setClass(this, SettingActivity.class);
		startActivity(intent);
	}
	@Override
	public void onMenuClose() {
		finish();
	}
}
