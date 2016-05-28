package com.king.app.fileencryption.service;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.data.DBInfor;
import com.king.app.fileencryption.filemanager.entity.FileBean;
import com.king.app.fileencryption.sorder.db.SqlOperator;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * InsertFileBeanThread由于耗时较长，如果其间执行了onDestroy，需要手动打断线程，否则线程会一直运行下去
 * UpdateDbThread耗时很短，不用手动打断。如果强行设置Thread.sleep(10)会极大拖慢速度不知为何，就不设置这层监听了
 * @author Administrator
 *
 */
public class FileDBService extends Service implements InsertFileBeanThread.Callback {

	private final int MSG_DONE = 1;
	private final int MSG_PROGRESS = 2;

	public static final String KEY_INSERT_PROCESS = "key_insert_process";

	private OnServiceProgressListener onServiceProgressListener;

	private InsertFileBeanThread insertFileBeanThread;

	public void setOnProgressListener(OnServiceProgressListener listener) {
		onServiceProgressListener = listener;
	}

	public class FileDbBinder extends Binder {

		public FileDBService getService() {
			return FileDBService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (Constants.DEBUG) {
			Log.e(Constants.LOG_TAG_SERVICE_FILE, "onBind");
		}
		return new FileDbBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Constants.DEBUG) {
			Log.e(Constants.LOG_TAG_SERVICE_FILE, "onStartCommand");
		}

		/**
		 * 为了避免两个线程访问数据库引起冲突，两个线程需要按序执行
		 */
		boolean insert = intent.getBooleanExtra(KEY_INSERT_PROCESS, false);
		if (insert) {
			new InsertFileBeanThread(null, this).start();
		}
		else {
			startUpdateDb();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 通过bind方式启动service后，调养此方法开始工作
	 * @param executeInsertProcess
	 */
	public void startWork(boolean executeInsertProcess) {
		if (Constants.DEBUG) {
			Log.e(Constants.LOG_TAG_SERVICE_FILE, "startWork");
		}

		if (executeInsertProcess) {
			insertFileBeanThread = new InsertFileBeanThread(null, this);
			insertFileBeanThread.start();
		}
		else {
			startUpdateDb();
		}
	}

	@Override
	public void onDestroy() {
		if (Constants.DEBUG) {
			Log.e(Constants.LOG_TAG_SERVICE_FILE, "onDestroy");
		}

		if (insertFileBeanThread != null) {
			insertFileBeanThread.interrupt();
		}
	}

	@Override
	public void onInsertProgress(int progress) {
		if (Constants.DEBUG) {
			Log.e(Constants.LOG_TAG_SERVICE_FILE, "onInsertProgress " + progress);
		}
		if (onServiceProgressListener != null) {
			onServiceProgressListener.onServiceProgress(progress / 2);
		}
	}

	@Override
	public void onInsertThreadDone() {
		if (Constants.DEBUG) {
			Log.e(Constants.LOG_TAG_SERVICE_FILE, "onInsertThreadDone Done");
		}
		if (onServiceProgressListener != null) {
			onServiceProgressListener.onServiceProgress(50);
		}
		startUpdateDb();
	}

	private void startUpdateDb() {
		if (Constants.DEBUG) {
			Log.e(Constants.LOG_TAG_SERVICE_FILE, "startUpdateDb");
		}
		new UpdateDbThread().start();
	}

	private class UpdateDbThread extends Thread {

		private final int PROGRESS_UNIT = 10;//耗时短，取10分之一

		private SqlOperator sqlOperator;
		private Connection connection;

		private List<FileBean> list;
		private int total;
		private int countForProgress;
		private int countAlreadyHandled;

		private UpdateDbThread () {
			sqlOperator = new SqlOperator();
			connection = sqlOperator.connect(DBInfor.DB_PATH);
			list = sqlOperator.queryAllFileBeans(connection, null);
			if (list != null) {
				total = list.size();
			}
		}

		@Override
		public void run() {

			try {
				if (list != null) {
					for (int i = 0; i < list.size(); i ++) {
						File file = new File(list.get(i).getPath());
						if (!file.exists()) {
							Log.e(Constants.LOG_TAG_SERVICE_FILE, "delete file on db:" + list.get(i).getPath());
							sqlOperator.deleteFileBean(list.get(i).getId(), connection);
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

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

			if (Constants.DEBUG) {
				Log.e(Constants.LOG_TAG_SERVICE_FILE, "UpdateDbThread Done");
			}
			handler.sendEmptyMessage(MSG_DONE);
		}

	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (msg.what == MSG_DONE) {
				if (onServiceProgressListener != null) {
					onServiceProgressListener.onServiceProgress(100);
					onServiceProgressListener.onServiceDone();
				}
			}
			else if (msg.what == MSG_PROGRESS) {
				if (onServiceProgressListener != null) {
					int progress = (Integer) msg.obj / 2 + 50;
					onServiceProgressListener.onServiceProgress(progress);
				}
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public void onInsertThreadCancel() {
		// TODO Auto-generated method stub

	}

}
