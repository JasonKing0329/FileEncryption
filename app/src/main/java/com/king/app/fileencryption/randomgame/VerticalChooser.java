package com.king.app.fileencryption.randomgame;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.widget.ListView;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.spicture.view.VerticalChooserAdapter;

@Deprecated
public class VerticalChooser implements Chooser {

	private ListView chooser;
	private VerticalChooserAdapter adapter;
	private Context context;
	private List<String> fileList;
	
	public VerticalChooser (Context context, List<String> fileList) {
		this.context = context;
		this.fileList = fileList;
		Activity view = (Activity) context;
		chooser = (ListView) view.findViewById(R.id.rgame_chooser_portrait);
		adapter = new VerticalChooserAdapter(context, fileList);
		chooser.setCacheColorHint(0);
		chooser.setAdapter(adapter);
	}
	
	@Override
	public void reInit() {
		Activity view = (Activity) context;
		chooser = (ListView) view.findViewById(R.id.rgame_chooser_portrait);
		if (adapter == null) {
			adapter = new VerticalChooserAdapter(context, fileList);
		}
		chooser.setCacheColorHint(0);
		chooser.setAdapter(adapter);
	}
}
