package com.king.app.fileencryption.filemanager.view;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.open.image.ImageValue;
import com.king.app.fileencryption.open.image.ImageValueController;
import com.king.app.fileencryption.setting.SettingProperties;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter {

	private List<FilePageItem> list;
	private List<Integer> imgList;
	private Context context;
	private SimpleDateFormat dayFormat;
	private ImageValueController imageValueController;
	private boolean showImageWH;

	public FileListAdapter(List<FilePageItem> list, Context context) {
		this.context = context;
		this.list = list;
		dayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
		imageValueController = new ImageValueController();
		setImageList();
	}

	public void updateList(List<FilePageItem> list) {
		this.list = list;
		setImageList();
	}

	public void showImageWH (boolean show) {
		showImageWH = show;
	}

	private void setImageList() {
		if (list != null && list.size() > 0) {

			//按照application约束，一个文件目录里要么全是文件，要么全是文件夹
			if (!list.get(0).getFile().isDirectory()) {
				imageValueController.queryImagePixelFrom(list);
			}

			imgList = new ArrayList<Integer>();
			File file = null;
			for (int i = 0; i < list.size(); i ++) {
				file = list.get(i).getFile();
				if (file.isDirectory()) {
					imgList.add(R.drawable.directory_icon);
				}
				else {
					imgList.add(R.drawable.ic_launcher);
				}

				long dt = list.get(i).getDate();
				String date = dayFormat.format(new Date(dt));
				list.get(i).setStrDate(date);
			}
		}
	}

	@Override
	public int getCount() {
		return list == null ? 0:list.size();
	}

	@Override
	public Object getItem(int position) {

		return list == null ? null:list.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_filelist, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.adapter_filelist_icon);
			holder.name = (TextView) convertView.findViewById(R.id.adapter_filelist_name);
			holder.date = (TextView) convertView.findViewById(R.id.adapter_filelist_date);
			holder.imageWH = (TextView) convertView.findViewById(R.id.adapter_filelist_imagewh);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.icon.setBackgroundResource(imgList.get(position));

		FilePageItem filePageItem = list.get(position);
		if (SettingProperties.isShowFileOriginMode(context)) {
			String name = filePageItem.getOriginName();
			if (name == null) {
				holder.name.setText(filePageItem.getFile().getName());
			}
			else {
				holder.name.setText(name);
			}
		}
		else {
			holder.name.setText(filePageItem.getFile().getName());
		}

		if (showImageWH) {
			holder.imageWH.setVisibility(View.VISIBLE);
			ImageValue value = filePageItem.getImageValue();
			if (value == null) {
				holder.imageWH.setText("");
			}
			else {
				holder.imageWH.setText(value.getWidth() + " * " + value.getHeight());
			}
		}
		else {
			holder.imageWH.setVisibility(View.GONE);
		}

		holder.date.setText(filePageItem.getStrDate());
		return convertView;
	}

	private class ViewHolder {
		public ImageView icon;
		public TextView name, date, imageWH;
	}
}
