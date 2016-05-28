package com.king.app.fileencryption.spicture.view;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.king.app.fileencryption.R;

public class VerticalChooser implements Chooser {

	private ListView chooser;
	private VerticalChooserAdapter adapter;
	private Context context;
	private View contentView;
	private List<String> fileList;
	
	public VerticalChooser (Context context, View contentView) {
		this.context = context;
		this.contentView = contentView;
		chooser = (ListView) contentView.findViewById(R.id.spicture_chooser_portrait);
		adapter = new VerticalChooserAdapter(context, fileList);
		chooser.setCacheColorHint(0);
		chooser.setAdapter(adapter);
	}
	
	@Override
	public void reInit() {
		chooser = (ListView) contentView.findViewById(R.id.spicture_chooser_portrait);
		chooser.setCacheColorHint(0);
		notifyAdapterRefresh();
	}

	@Override
	public void notifyAdapterRefresh() {
		if (fileList != null) {
			//Log.i(TAG, "image number = " + imgList.size());
			if (adapter == null) {
				adapter = new VerticalChooserAdapter(context, fileList);
				chooser.setAdapter(adapter);
			} else {
				adapter.updateList(fileList);
				adapter.notifyDataSetChanged();
			}
			chooser.setAdapter(adapter);
		}
	}

	@Override
	public void setVisibility(int visibility) {
		chooser.setVisibility(visibility);
	}

	@Override
	public void updateList(List<String> fileList) {
		this.fileList = fileList;
	}

	@Override
	public View getChildAt(int index) {
		
		return chooser.getChildAt(index);
	}

	@Override
	public void setOnChooseListener(Object listener) {
		chooser.setOnItemClickListener((OnItemClickListener) listener);
		chooser.setOnItemLongClickListener((OnItemLongClickListener) listener);
	}

	@Override
	public void prepareRecycle() {
		if (adapter != null) {
			adapter.updateList(null);
			adapter.notifyDataSetChanged();
		}
	}
}
