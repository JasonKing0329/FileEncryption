package com.king.app.fileencryption.wall.update;

import android.view.View;

/**
 * @author JingYang
 * @version create time：2016-2-1 下午3:08:23
 *
 */
public interface MirrorListener {

	public void startMirror(View view);
	public void cancelMirror(View view);
	public void endMirror(View view);
	public void processMirror(View view, float scale);
}
