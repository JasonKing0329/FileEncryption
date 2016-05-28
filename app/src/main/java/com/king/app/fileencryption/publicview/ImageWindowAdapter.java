package com.king.app.fileencryption.publicview;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.open.image.ImageFactory;
import com.king.app.fileencryption.publicview.FlingGalleryActivity.OnGalleryActionListener;
import com.king.app.fileencryption.tool.Encrypter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageWindowAdapter extends BaseAdapter {

	private final String LOGTAG = "ImageWindowAdapter";
	private Context context;
	private Encrypter encrypter;
	private List<Integer> folderStep;
	private List<File> rootDirectories;
	private OnGalleryActionListener actionListener;
	
	public ImageWindowAdapter(Context context, Encrypter encrypter, OnGalleryActionListener actionListener) {
		this.context = context;
		this.encrypter = encrypter;
		this.actionListener = actionListener;
		countAvailableRandom();
	}
	
	private void countAvailableRandom() {
		if (folderStep == null) {
			folderStep = new ArrayList<Integer>();
		}
		else {
			folderStep.clear();
		}
		if (rootDirectories == null) {
			rootDirectories = new ArrayList<File>();
		}
		else {
			rootDirectories.clear();
		}
		
		File files[] = new File(Configuration.APP_DIR_IMG).listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File parent, String name) {

				return name.endsWith(encrypter.getFileExtra()) || new File(parent.getPath() + "/" + name).isDirectory();
			}
		});
		
		int sum = 0;
		File[] array = null;
		for (File file:files) {
			array = file.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File arg0, String name) {

					return name.endsWith(encrypter.getFileExtra());
				}
			});
			sum += array.length;
			folderStep.add(sum);
			rootDirectories.add(file);
		}
	}
	
	@Override
	public int getCount() {

		return 2;
	}

	@Override
	public Object getItem(int position) {

		return position;
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.spicture_new_win_layout_item, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.new_win_image);
			holder.filename = (TextView) convertView.findViewById(R.id.new_win_filename);
			holder.filename.setVisibility(View.VISIBLE);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (holder != null) {
			putRandomView(holder.image, holder.filename);
		}
		return convertView;
	}

	private void putRandomView(ImageView image, TextView nameText) {
		int index = Math.abs(new Random().nextInt()) % folderStep.get(folderStep.size() - 1);
		int begin = 0;
		String filename = null;
		Bitmap bitmap = null;
		String filePath = null;
		for (int i = 0; i < folderStep.size(); i ++) {
			if (index > begin && index < folderStep.get(i)) {

				if (rootDirectories.get(i).isDirectory()) {
					File f = null;
					try {
						File[] subs = rootDirectories.get(i).listFiles(new FilenameFilter() {
							
							@Override
							public boolean accept(File arg0, String name) {

								return name.endsWith(encrypter.getFileExtra());
							}
						});
						f = subs[index - begin];
					} catch (Exception exception) {
						countAvailableRandom();
						putRandomView(image, nameText);
						break;
					}

					if (encrypter.isEncrypted(f)) {
						String path = f.getParent();
						int index1 = path.indexOf("/img/");
						if (index1 >= 0 && index1 < path.length()) {
							path = path.substring(index1);
						}
						filename = encrypter.decipherOriginName(f) + "(" + path + ")";
					}
					else {
						filename = f.getName();
					}

					filePath = f.getPath();
					bitmap = PictureManagerUpdate.getInstance().createHDBitmap(filename);
					Log.i(LOGTAG, filePath);
				}
				break;
			}
			begin = folderStep.get(i);
		}
		if (bitmap == null) {
			image.setImageResource(R.drawable.ic_launcher);
		}
		else {
			image.setImageBitmap(bitmap);
			image.setTag(filePath);
		}
		nameText.setText(filename);
		image.setOnLongClickListener(actionListener);
	}

	private class ViewHolder {
		ImageView image;
		TextView filename;
	}
	
	private class LongTouchListener implements OnLongClickListener {

		@Override
		public boolean onLongClick(View arg0) {

			return false;
		}
		
	}
}
