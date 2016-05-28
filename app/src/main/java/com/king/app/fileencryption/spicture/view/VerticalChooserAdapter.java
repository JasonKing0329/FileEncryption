package com.king.app.fileencryption.spicture.view;

import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

public class VerticalChooserAdapter extends BaseAdapter {

	private List<String> fileList;
	private Context context;
	private int imageViewWidth;

	public VerticalChooserAdapter(Context context, List<String> fileList) {
		this.context = context;
		setList(fileList);
		imageViewWidth = context.getResources().getDimensionPixelSize(R.dimen.spicture_chooser_item_width);
	}

	public void updateList(List<String> fileList) {
		//setList(imageList);
		this.fileList = fileList;
	}

	private void setList(List<String> fileList) {
		if (this.fileList == null) {
			this.fileList = fileList;
		}
		else {
			this.fileList.clear();
			this.fileList = fileList;
		}
	}
	@Override
	public int getCount() {

		return fileList == null ? 0:fileList.size();
	}

	@Override
	public Object getItem(int position) {

		return fileList == null ? 0:fileList.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ImageView view = new ImageView(context);

		//由于这里是为listview适配，layoutparams必须用ListView的，还用会报出
		//ClassCastException: ...LinearLayout.LayoutParams cant be casted to ...ListView.LayoutParams
		//view.setLayoutParams(new LinearLayout.LayoutParams(imageViewWidth, imageViewWidth));
		view.setLayoutParams(new ListView.LayoutParams(imageViewWidth, imageViewWidth));

		//view.setPadding(15, 15, 15, 15);
		view.setScaleType(ScaleType.FIT_CENTER);
		//view.setBackgroundResource(R.drawable.spicture_chooser_border_choose);
		//view.setImageBitmap(imageList.get(position));
		Bitmap bitmap = PictureManagerUpdate.getInstance().createSpictureItem(fileList.get(position), context);
		view.setBackground(new BitmapDrawable(context.getResources(), bitmap));
		return view;
	}

}
