package com.king.app.fileencryption.randomgame;

import java.util.List;

import android.app.Activity;
import android.content.Context;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.publicview.HorizontalAdapter;
import com.king.app.fileencryption.publicview.HorizontalGallery;

@Deprecated
public class HorizontalChooser implements Chooser {
	private HorizontalGallery horizontalGallery;
	private HorizontalAdapter horizontalAdapter;
	private Context context;
	private List<String> fileList;
	
	public HorizontalChooser (Context context, List<String> fileList) {
		this.context = context;
		this.fileList = fileList;
		Activity view = (Activity) context;
		horizontalGallery = (HorizontalGallery) view.findViewById(R.id.horizontalGallery);
		horizontalAdapter = new HorizontalAdapter(context, fileList);
		horizontalGallery.setAdapter(horizontalAdapter);
	}

	@Override
	public void reInit() {
		Activity view = (Activity) context;
		horizontalGallery = (HorizontalGallery) view.findViewById(R.id.horizontalGallery);
		if (horizontalAdapter == null) {
			horizontalAdapter = new HorizontalAdapter(context, fileList);
		}
		horizontalGallery.setAdapter(horizontalAdapter);
	}
}
