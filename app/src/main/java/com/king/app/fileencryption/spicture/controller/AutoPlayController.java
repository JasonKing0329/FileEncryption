package com.king.app.fileencryption.spicture.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.setting.SettingProperties;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AutoPlayController {

	public static final int AUTO_SPECIFIED_LIST = 101;
	public static final int AUTO_WHOLE_RANDOM = 102;
	
	private final String TAG = "AutoPlayController";
	private List<String> fileNameList;
	private Handler handler;
	private boolean isAutoPlay;
	private Context context;
	
	public AutoPlayController(Context context, Callback callback) {
		this.context = context;
		handler = new Handler(callback);
	}

	public void setFileNameList(List<String> fileNameList) {
		this.fileNameList = fileNameList;
	}

	public boolean isAutoPlaying() {
		return isAutoPlay;
	}

	public void startAutoPlay(int time) {
		isAutoPlay = true;
		new AutoPlayThread(time).start();
	}

	public void startWholeRandomAutoPlay(int time) {
		isAutoPlay = true;
		new WholeRandomAutoPlayThread(time).start();
	}

	public void stopAutoPlay() {
		isAutoPlay = false;
	}

	private class WholeRandomAutoPlayThread extends Thread {

		private int time;
		
		public WholeRandomAutoPlayThread(int time) {
			this.time = time;
		}
		
		public void run() {
			while (isAutoPlaying()) {
				try {
					Message msg = new Message();
					msg.what = AUTO_WHOLE_RANDOM;
					handler.sendMessage(msg);
					Thread.sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
		
	}

	private class AutoPlayThread extends Thread {

		private int time;
		
		private List<Integer> historyList;
		private int mode;
		
		public AutoPlayThread(int time) {
			this.time = time;
			mode = SettingProperties.getAutoPlayMode(context);
			if (fileNameList != null && fileNameList.size() > 0) {

				if (historyList != null) {
					historyList.clear();
				}
				else {
					historyList = new ArrayList<Integer>();
				}
				for (int i = 0; i < fileNameList.size(); i ++) {
					if (fileNameList.get(i) != null) {
						historyList.add(i);
					}
				}
			}
		}
		
		public void run() {
			while (isAutoPlaying()) {
				try {
					int index = 0;
					Random random = new Random();
					Message msg = new Message();
					msg.what = AUTO_SPECIFIED_LIST;
					Bundle bundle = new Bundle();
					if (mode == SettingProperties.AUTOPLAY_MODE_SEQUENCE) {//sequence
						if (historyList.size() == 0) {
							bundle.putString("autoplay_finish", "true");
						}
						else {
							index = historyList.get(0);
							historyList.remove(0);
						}
					}
					else if (mode == SettingProperties.AUTOPLAY_MODE_RANDOM) {//random
						if (historyList.size() == 0) {
							bundle.putString("autoplay_finish", "true");
						}
						else {
							int temp = Math.abs(random.nextInt()) % historyList.size();
							index = historyList.get(temp);
							historyList.remove(temp);
						}
					}
					else {
						index = Math.abs(random.nextInt()) % fileNameList.size();
					}
					Log.i(TAG, "handle autoplay " + index);
					bundle.putInt("autoplay_index", index);
					bundle.putBoolean("autoplay_scroll", false);
					msg.setData(bundle);
					handler.sendMessage(msg);
					Thread.sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
		
	}

	public boolean canPlay() {
		if (fileNameList == null) return false;
		
		return fileNameList.size() >= SettingProperties.getMinNumberToPlay(context);
	}
	
	public Animation randomAnimation() {
		Animation animation = null;
		String[] arrays = context.getResources().getStringArray(R.array.anim_type);
		int total = arrays.length;
		int index = Math.abs(new Random().nextInt()) % total;
		int animId = R.anim.hold;
		switch (index) {
		case 0:
			animId = R.anim.fade;
			break;
		case 1:
			animId = R.anim.my_scale_action;
					//R.anim.my_alpha_action);
			break;
		case 2:
			animId = R.anim.scale_rotate;
					//R.anim.my_alpha_action);
			break;
		case 3:
			animId = R.anim.scale_translate_rotate;
					//R.anim.my_alpha_action);
			break;
		case 4:
			animId = R.anim.scale_translate;
					//R.anim.my_alpha_action);
			break;
		case 5:
			animId = R.anim.hyperspace_in;
					//R.anim.hyperspace_out);
			break;
		case 6:
			animId = R.anim.push_left_in;
					//R.anim.push_left_out);
			break;
		case 7:
			animId = R.anim.push_up_in;
					//R.anim.push_up_out);
			break;
		case 8:
			animId = R.anim.slide_left;
					//R.anim.slide_right);
			break;
		case 9:
			animId = R.anim.wave_scale;
					//R.anim.my_alpha_action);
			break;
		case 10:
			animId = R.anim.zoom_enter;
					//R.anim.zoom_exit);
			break;
		case 11:
			animId = R.anim.slide_up_in;
					//R.anim.slide_down_out);
			break;
		}
		animation = AnimationUtils.loadAnimation(context, animId);
		return animation;
	}
	
}
