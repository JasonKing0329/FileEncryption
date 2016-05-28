package com.king.app.fileencryption.wall;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.controller.PictureManagerUpdate;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler.Callback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class WallResManager implements Callback {

	private final String TAG = "WallResManager";
	private int MAX = 30;
	private List<Bitmap> cacheList;
	private Bitmap defaultBitmap;
	private Context mContext;
	
	private WallAdapterUpdate wallAdapter;
	private Handler handler;
	
	public WallResManager(Context context, WallAdapterUpdate adapter) {
		mContext = context;
		wallAdapter = adapter;
		defaultBitmap = PictureManagerUpdate.getInstance().getUnavailableItemImage(context);
		handler = new Handler(this);
	}
	
	public void initCacheList(List<String> pathList) {
		reset();
		
		cacheList = new ArrayList<Bitmap>(pathList.size());
		for (int i = 0; i < pathList.size(); i ++) {
			cacheList.add(null);
		}
		
		if (MAX > pathList.size()) {
			loadBitmap(0, pathList.size() - 1, pathList);
		}
		else {
			loadBitmap(0, MAX - 1, pathList);
		}
	}
	
	public Bitmap getBitmap(int position) {
		return cacheList.get(position);
	}
	
	public int getCacheListRealSize() {
		int sum = 0;
		if (cacheList != null) {
			for (int i = 0; i < cacheList.size(); i ++) {
				if (cacheList.get(i) != null) {
					sum ++;
				}
			}
		}
		return sum;
	}
	/**
	 * specify the range that must include bitmap loaded
	 * end - start must less than MAX
	 * @param start
	 * @param end
	 * @param pathList
	 * @param direction 
	 */
	public void setVisibleRange(int start, int end, final List<String> pathList, int direction) {

		boolean needLoad = false;
		boolean needRecycle = false;
		if (end >= pathList.size() - 1) {
			end = pathList.size() - 1;
		}
		
		for (int i = start; i <= end; i ++) {
			if (cacheList.get(i) == null) {
				needLoad = true;
				needRecycle = true;
				break;
			}
		}

		if (getCacheListRealSize() > MAX) {
			needRecycle = true;
		}

		Log.d(TAG, "needRecycle " + needRecycle);
		if (needRecycle) {
			int loadStart = start - (MAX - (end - start + 1)) / 2;
			int loadEnd = start + MAX - 1;
			if (loadStart < 0) {
				loadStart = 0;
			}
			if (loadEnd > pathList.size() - 1) {
				loadEnd = pathList.size() - 1;
			}
			
			/**
			 * call this out of thread, cause it'll notify grid view refresh recycled item
			 */
			recycleOutOf(loadStart, loadEnd, direction);
		}
		
		Log.d(TAG, "needLoad " + needLoad);
		
		if (needLoad) {

			int loadStart = start - (MAX - (end - start + 1)) / 2;
			int loadEnd = start + MAX - 1;
			if (loadStart < 0) {
				loadStart = 0;
			}
			if (loadEnd > pathList.size() - 1) {
				loadEnd = pathList.size() - 1;
			}
			final int ls = loadStart;
			final int le = loadEnd;

			new Thread() {
				public void run() {
					Position position = loadBitmap(ls, le, pathList);
					if (position.start == -1 && position.end == -1) {
						return;
					}
					Message message = new Message();
					message.what = 1;
					Bundle bundle = new Bundle();
					bundle.putInt("start", position.start);
					bundle.putInt("end", position.end);
					message.setData(bundle);
					handler.sendMessage(message);
					
				}
			}.start();
		}
	}

	/**
	 * in quickly scroll process
	 * it normally scroll to one direction
	 * so it's better to recycle items in opposite direction
	 * @param loadStart
	 * @param loadEnd
	 * @param direction
	 */
	private void recycleOutOf(int loadStart, int loadEnd, int direction) {
		Log.d(TAG, "recycleOutOf [" + loadStart + "," + loadEnd + "] dir=" + direction);

		int start = 0, end = cacheList.size();
		if (direction > 0) {//scroll to right, then recycle bitmap at left
			end = loadStart;
		}
		else if (direction < 0) {//scroll to left, then recycle bitmap at right
			start = loadEnd;
		}
		
		int realStart = -1, realEnd = -1;
		for (int i = start; i < end; i ++) {
			if (i < loadStart || i > loadEnd) {
				Bitmap bitmap = cacheList.get(i);
				if (bitmap != null && bitmap != defaultBitmap) {
					if (direction > 0) {
						if (realStart == -1) {
							realStart = i;
							realEnd = end;
						}
					}
					else if (direction < 0) {
						if (realStart == -1) {
							realStart = i;
						}
						if (i == cacheList.size() - 1) {
							realEnd = i;
						}
						else {
							if (cacheList.get(i + 1) == null) {
								realEnd = i;
							}
						}
					}
					
					bitmap.recycle();
					cacheList.set(i, null);
				}
			}
		}
		Log.d(TAG, "recycleOutOf reality[" + realStart + "," + realEnd + "]");
		
		/**
		 * it must notify image view place to set image reference as others
		 * only by this way could it make sure that 'try to use recycled bitmap' exception will not occur
		 */
		if (realStart != -1 && realEnd != -1) {
			wallAdapter.notifyRecycled(realStart, realEnd);
		}
	}

	/**
	 * call this in new thread, as this method cost more time
	 * @param loadStart
	 * @param loadEnd
	 * @param pathList
	 * @return
	 */
	private synchronized Position loadBitmap(int loadStart, int loadEnd, List<String> pathList) {

		Log.d(TAG, "loadBitmap [" + loadStart + "," + loadEnd + "]");
		Position position = new Position();
		for (int i = loadStart; i <= loadEnd; i ++) {
			if (cacheList.get(i) == null) {
				if (position.start == -1) {
					position.start = i;
				}
				
				if (i == loadEnd) {
					position.end = i;
				}
				else {
					if (cacheList.get(i + 1) != null) {
						position.end = i;
					}
				}
				Bitmap bitmap = PictureManagerUpdate.getInstance().createWallItem(pathList.get(i), mContext);
				cacheList.set(i, bitmap);
			}
		}
		Log.d(TAG, "loadBitmap reality[" + position.start + "," + position.end + "]");
		return position;
	}

	private class Position {
		int start;
		int end;
		public Position() {
			start = -1;
			end = -1;
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {

		if (msg.what == 1) {
			Bundle bundle = msg.getData();
			int start = bundle.getInt("start");
			int end = bundle.getInt("end");
			wallAdapter.notifyBitmapLoaded(start, end);
		}
		return false;
	}

	public void reset() {
		Log.d(TAG, "reset");
		if (cacheList != null) {
			for (Bitmap bitmap:cacheList) {
				if (bitmap != null && bitmap != defaultBitmap) {
					bitmap.recycle();
				}
			}
			cacheList.clear();
		}
	}
}
