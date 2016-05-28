package com.king.app.fileencryption.wall.update;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.king.app.fileencryption.R;

/**
 * @author JingYang
 * @version create time：2016-1-30 下午1:15:17
 *
 */
public class AnimationManager {

	public static Animation getToolbarInAnim(Context context) {
		return AnimationUtils.loadAnimation(context, R.anim.push_down_in);
	}

	public static Animation getToolbarOutAnim(Context context) {
		return AnimationUtils.loadAnimation(context, R.anim.push_up_out);
	}

	public static Animation getBottombarInAnim(Context context) {
		return AnimationUtils.loadAnimation(context, R.anim.push_up_in);
	}

	public static Animation getBottombarOutAnim(Context context) {
		return AnimationUtils.loadAnimation(context, R.anim.push_down_out);
	}

	public static Animation getShakeZoomAnim(Context context) {
		return AnimationUtils.loadAnimation(context, R.anim.thumb_item_longclick);
	}
}
