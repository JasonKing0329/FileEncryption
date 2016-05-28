package com.king.app.fileencryption.service;

import java.io.File;
import java.io.FileFilter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.data.DBInfor;
import com.king.app.fileencryption.filemanager.controller.FileBeanController;
import com.king.app.fileencryption.filemanager.entity.FileBean;
import com.king.app.fileencryption.sorder.db.SqlOperator;
import com.king.app.fileencryption.tool.SimpleEncrypter;

public class InsertFileBeanThread extends Thread implements FileFilter {

	private final int MSG_DONE = 1;
	private final int MSG_PROGRESS = 2;
	private final int PROGRESS_UNIT = 20;//比较耗时，因此取20分之一就更新一次进度

	public interface Callback {
		public void onInsertThreadDone();
		public void onInsertThreadCancel();
		public void onInsertProgress(int progress);
	}

	private Callback mCallback;

	private Connection connection;
	private FileBeanController fileBeanController;
	private SqlOperator sqlOperator;

	/** 指定待检测的文件 **/
	private List<String> targetList;

	// 添加操作一次性添加50，避免太多次重复创建statement
	private final int NUM = 50;
	private List<FileBean> list;

	private int total;
	private int countForProgress;
	private int countAlreadyHandled;

	/**
	 * @param targetList if null, traverse whole application file content
	 */
	public InsertFileBeanThread(List<String> targetList, Callback callback) {
		this.targetList = targetList;
		mCallback = callback;
		sqlOperator = new SqlOperator();
		fileBeanController = new FileBeanController();
		list = new ArrayList<FileBean>();

		countTotal();
	}

	private void countTotal() {
		File root = new File(Configuration.APP_DIR_IMG);
		traverseCount(root);
		if (Constants.DEBUG) {
			Log.e(Constants.LOG_TAG_SERVICE_FILE, "traverse total = " + total);
		}
	}

	private void traverseCount(File root) {
		File[] files = root.listFiles(this);
		for (int i = 0; i < files.length; i ++) {
			if (files[i].isDirectory()) {
				traverseCount(files[i]);
			}
			else {
				total ++;
			}
		}
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (msg.what == MSG_DONE) {
				if (mCallback != null) {
					mCallback.onInsertThreadDone();
				}
			}
			else if (msg.what == MSG_PROGRESS) {
				if (mCallback != null) {
					int progress = (Integer) msg.obj;
					mCallback.onInsertProgress(progress);
				}
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public void run() {

		try {
			connection = sqlOperator.connect(DBInfor.DB_PATH);
			File root = new File(Configuration.APP_DIR_IMG);

			if (targetList == null) {//traverse all
				traverse(root);
			}
			else {
				for (int i = 0; i < targetList.size(); i ++) {
					FileBean bean = fileBeanController.createFileBean(new File(targetList.get(i)));
					list.add(bean);
					insertList(NUM);
				}
			}
			// 最后不足NUM，仍要添加最后剩余的
			if (list.size() > 0) {
				insertList(list.size());
			}

			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			Message message = new Message();
			message.what = MSG_DONE;
			handler.sendMessage(message);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	private void traverse(File root) throws InterruptedException {
		File[] files = root.listFiles(this);
		for (int i = 0; i < files.length; i ++) {
			if (files[i].isDirectory()) {
				traverse(files[i]);
			}
			else {
				if (!sqlOperator.isFileBeanExist(files[i].getPath(), connection)) {
					recordFileBean(files[i]);
				}

				countForProgress ++;
				countAlreadyHandled ++;

				if (countForProgress >= total / PROGRESS_UNIT) {

					Message message = new Message();
					message.what = MSG_PROGRESS;
					message.obj = (int) ((float) countAlreadyHandled / (float) total * 100);
					handler.sendMessage(message);

					countForProgress = 0;
				}

			}
		}

		Thread.sleep(10);
	}

	private void recordFileBean(File file) {
		FileBean bean = fileBeanController.createFileBean(file);

		// 添加操作一次性添加NUM，避免太多次重复创建statement
		list.add(bean);
		if (list.size() == NUM) {
			insertList(NUM);
		}
	}

	private void insertList(int num) {
		if (Constants.DEBUG) {
			Log.e(Constants.LOG_TAG_SERVICE_FILE, "insertList size " + num);
		}
		sqlOperator.insertFileBeans(list, connection);
		list.clear();
	}
	@Override
	public boolean accept(File file) {

		return file.isDirectory() || file.getName().endsWith(SimpleEncrypter.FILE_EXTRA);
	}

}