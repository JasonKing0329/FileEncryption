package com.king.app.fileencryption.book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.king.app.fileencryption.open.image.ImageValue;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;

public class BookPageAdapter extends BaseAdapter {

	private static final String TAG = "BookPageAdapter";
	private List<List<ImageValue>> imageList;
	private Context mContext;
	private List<BookPage> bookPageList;
	private OnClickListener imageListener;

	private TreeSet<Integer> cacheImagePositions;

	public BookPageAdapter(Context context) {
		mContext = context;
		bookPageList = new ArrayList<BookPage>();
		cacheImagePositions = new TreeSet<Integer>();
	}

	public void updateData(List<List<ImageValue>> list) {
		imageList = list;
		for (BookPage bookPage:bookPageList) {
			if (bookPage != null) {
				bookPage.recycleBitmaps();
			}
		}
		cacheImagePositions.clear();
		if (list != null) {
			bookPageList.clear();
			for (int i = 0; i < list.size(); i ++) {
				bookPageList.add(null);
			}
		}
	}

	public void setOnImageClickListener(OnClickListener listener) {
		imageListener = listener;
	}

	public BookPage getPage(int index) {
		if (bookPageList != null) {
			return bookPageList.get(index);
		}
		return null;
	}

	@Override
	public int getCount() {
		return imageList == null ? 0:imageList.size();
	}

	@Override
	public Object getItem(int position) {
		return imageList == null ? 0:imageList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Log.d(TAG, "getView " + position);
		BookPage bookPage = bookPageList.get(position);
		if (bookPage == null) {
			bookPage = new BookPage(mContext);
			bookPage.setOnImageClickListener(imageListener);
			bookPageList.set(position, bookPage);
		}
		cacheImagePositions.add(position);
		recycleUnused(position);
		bookPage.setItems(imageList.get(position));
		bookPage.show();
		return bookPage;
	}

	private void recycleUnused(int position) {
		BookPage bookPage = null;
		Iterator<Integer> iterator = cacheImagePositions.iterator();
		List<Integer> delList = new ArrayList<Integer>();
		while (iterator.hasNext()) {
			int index = iterator.next();
			if (index < position - 2 || index > position + 2) {
				bookPage = bookPageList.get(index);
				if (bookPage != null) {
					bookPage.recycleBitmaps();
				}
				delList.add(index);
			}
		}
		for (int index:delList) {
			cacheImagePositions.remove(index);
		}
	}

	public void recycleAll() {
		if (bookPageList != null) {
			for (BookPage page:bookPageList) {
				if (page != null) {
					page.recycleBitmaps();
				}
			}
		}
	}
}
