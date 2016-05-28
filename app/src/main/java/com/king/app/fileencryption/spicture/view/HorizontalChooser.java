package com.king.app.fileencryption.spicture.view;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.publicview.HorizontalAdapter;
import com.king.app.fileencryption.publicview.HorizontalGallery;
import com.king.app.fileencryption.publicview.HorizontalGallery.OnItemSelectListener;

public class HorizontalChooser implements Chooser {

	private final String TAG = "HorizontalChooser";
	private HorizontalGallery horizontalGallery;
	private HorizontalAdapter horizontalAdapter;
	private Context context;
	private List<String> fileList;
	
	public HorizontalChooser (Context context) {
		this.context = context;
		Activity view = (Activity) context;
		horizontalGallery = (HorizontalGallery) view.findViewById(R.id.horizontalGallery);
	}

	@Override
	public void reInit() {
		Activity view = (Activity) context;
		horizontalGallery = (HorizontalGallery) view.findViewById(R.id.horizontalGallery);
		notifyAdapterRefresh();
	}

	@Override
	public void notifyAdapterRefresh() {
		Log.d(TAG, "notifyAdapterRefresh");
		if (fileList != null) {
			//Log.i(TAG, "image number = " + imageList.size());
			if (horizontalAdapter == null) {
				horizontalAdapter = new HorizontalAdapter(context, fileList);
			}
			else {
				horizontalAdapter.updateList(fileList);
			}
//			if (horizontalAdapter != null) {
//				horizontalAdapter.recycle();
//			}
//			horizontalAdapter = new HorizontalAdapter(context, fileList);
			horizontalGallery.setAdapter(horizontalAdapter);
		}
	}

	@Override
	public void setVisibility(int visibility) {
		horizontalGallery.setVisibility(visibility);
	}

	@Override
	public void updateList(List<String> fileList) {
		this.fileList = fileList;
	}

	@Override
	public void setOnChooseListener(Object listener) {
		horizontalGallery.setOnItemSelectListener((OnItemSelectListener) listener);
	}

	@Override
	public void prepareRecycle() {
		Log.d(TAG, "prepareRecycle");
//		if (horizontalAdapter != null) {
//			horizontalAdapter.updateList(null);
//			horizontalAdapter.recycle();
//		}
	}

	@Override
	public View getChildAt(int index) {
		
		return horizontalGallery.getItemView(index);
	}
}
