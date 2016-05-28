package com.king.app.fileencryption.thumbfolder;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.util.Log;

public class MoveController {

	private final String TAG = "MoveController";
	private Context mContext;
	private Handler moveEventHandler;
	private ProgressDialog moveProgressDialog;
	private String moveProgressText;
	private int moveIndex;
	
	private Queue<String> errorQueue;
	
	public MoveController(Context context, Callback callback) {
		mContext = context;
		moveEventHandler = new Handler(callback);
		moveProgressText = context.getResources().getString(R.string.folderdlg_moving);
		errorQueue = new LinkedList<String>();
	}
	
	public void showProgress() {
		Log.d(TAG, "showProgress");
		errorQueue.clear();
		if (moveProgressDialog == null) {
			moveProgressDialog = new ProgressDialog(mContext);
		}
		moveIndex = 0;
		String text = String.format(moveProgressText, moveIndex);
		moveProgressDialog.setMessage(text);
		moveProgressDialog.show();
	}
	
	public void updateProgress() {
		Log.d(TAG, "updateProgress");
		moveIndex ++;
		String text = String.format(moveProgressText, moveIndex);
		moveProgressDialog.setMessage(text);
	}
	
	public void cancleProgress(boolean delay) {

		Log.d(TAG, "cancleProgress delay " + delay);
		if (delay) {
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					moveProgressDialog.cancel();
				}
			}, 1000);
		}
		else {
			moveProgressDialog.cancel();
		}
		
		showErrorQueue();
	}

	private void showErrorQueue() {
		if (errorQueue.size() > 0) {
			StringBuffer buffer = new StringBuffer();
			for (String error:errorQueue) {
				buffer.append(error).append("\n");
			}
			new AlertDialog.Builder(mContext)
			.setTitle(R.string.error_msg_title)
			.setMessage(buffer.toString())
			.setPositiveButton(R.string.ok, null)
			.show();
		}
	}

	public Handler getHandler() {

		return moveEventHandler;
	}

	public void moveToFolder(List<String> pathList, File targetFile, Handler handler) {
		if (pathList != null && targetFile.exists() && targetFile.isDirectory()) {
			SOrderPictureBridge.getInstance(mContext).moveToFolder(pathList, targetFile, handler);
		}
	}

	public void addError(String error) {
		errorQueue.add(error);
	}
}
