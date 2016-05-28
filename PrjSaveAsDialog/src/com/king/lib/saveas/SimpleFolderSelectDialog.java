package com.king.lib.saveas;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class SimpleFolderSelectDialog extends CustomPage
		implements OnItemClickListener, OnItemLongClickListener{

	public static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
	private List<File> fileList;
	private ListView listView;
	private TextView parentView;
	private SimpleListAdapter adapter;

	private File currentFolder;
	private File selectFolder;
	private View lastSelectView;
	
	public SimpleFolderSelectDialog(Context context,
			OnCustomPageActionListener actionListener) {
		super(context, actionListener);

		requestAddAction(true);
		setTitle(R.string.saveas_select_path);
		
		currentFolder = new File(SDCARD);
		changeFileList();
		
		adapter = new SimpleListAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);

		applyGreyStyle();
		computeHeight();
	}

	@Override
	protected View getCustomView() {

		View view = LayoutInflater.from(getContext()).inflate(R.layout.dlg_folder_select, null);
		listView = (ListView) view.findViewById(R.id.folderselect_list);
		parentView = (TextView) view.findViewById(R.id.folderselect_parent);
		parentView.setOnClickListener(this);
		return view;
	}

	@Override
	protected View getCustomToolbar() {
		// TODO Auto-generated method stub
		return null;
	}

	FileFilter folderFilter = new FileFilter() {
		
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	};

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		selectFolder(position, view);
		return true;
	}

	@SuppressLint("NewApi") 
	private void selectFolder(int position, View view) {

		SimpleListAdapter.ContainerHolder holder = (SimpleListAdapter.ContainerHolder) view.getTag();
		if (holder.bkLayout == lastSelectView) {
			holder.bkLayout.setBackground(null);
			selectFolder = null;
			lastSelectView = null;
			requestOkAction(false);
		}
		else {
			if (lastSelectView != null) {
				lastSelectView.setBackground(null);
			}
			holder.bkLayout.setBackgroundColor(getContext().getResources().getColor(R.color.dlg_folder_item_pressed));
			selectFolder = fileList.get(position);
			lastSelectView = holder.bkLayout;
			requestOkAction(true);
		}
		
	}

	private void refreshFileList() {
		requestOkAction(false);
		adapter.notifyDataSetChanged();
	}
	
	@SuppressLint("NewApi")
	private void changeFileList() {
		if (!isRootFolder(currentFolder.getPath())) {
			parentView.setVisibility(View.VISIBLE);
		}
		selectFolder = null;
		if (lastSelectView != null) {
			lastSelectView.setBackground(null);
		}
		lastSelectView = null;
		File[] files = currentFolder.listFiles(folderFilter);
		if (files != null && files.length > 0) {
			fileList = new ArrayList<File>();
			for (File file:files) {
				fileList.add(file);
			}
			Collections.sort(fileList, new Comparator<File>() {

				@Override
				public int compare(File file0, File file1) {

					return file0.getName().toLowerCase(Locale.CHINA)
							.compareTo(file1.getName().toLowerCase(Locale.CHINA));
				}
			});
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {

		File file = fileList.get(position);
		if (file.listFiles(folderFilter).length == 0) {
			selectFolder(position, view);
			return;
		}
		
		currentFolder = fileList.get(position);
		changeFileList();
		if (fileList.size() > 0) {
			refreshFileList();
		}
	}
	
	@Override
	public void onClick(View view) {

		if (view == parentView) {
			currentFolder = currentFolder.getParentFile();
			if (isRootFolder(currentFolder.getPath())) {
				parentView.setVisibility(View.GONE);
			}
			changeFileList();
			refreshFileList();
		}
		else if (view == okView) {
			actionListener.onSelect(selectFolder.getPath());
			dismiss();
		}
		else if (view == addView) {
			showTextInputDialog(getContext()
					, getContext().getResources().getString(R.string.saveas_filename));
		}
		super.onClick(view);
	}

	public void showTextInputDialog(Context context, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);

		EditText edit = new EditText(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		
		edit.setLayoutParams(params);
		edit.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
		builder.setView(edit);
		
		final EditText inputEdit = edit;
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int which) {
				if (inputEdit.getText().toString().length() == 0) {
					inputEdit.setError(inputEdit.getResources().getString(R.string.folderselect_new));
					return;
				}

				String folderName = inputEdit.getText().toString();
				String path = null;
				if (selectFolder == null) {
					path = currentFolder.getPath() + "/" + folderName;
				}
				else {
					path = selectFolder.getPath() + "/" + folderName;
				}
				
				File file = new File(path);
				
				if (file.exists()) {
					Toast.makeText(getContext(), R.string.folderselect_folder_exist, Toast.LENGTH_LONG).show();
				}
				else {
					file.mkdir();
					changeFileList();
					refreshFileList();
					Toast.makeText(getContext(), R.string.save_success, Toast.LENGTH_LONG).show();
				}
			}
		});
		builder.setNegativeButton(R.string.cancel, null);
		builder.show();
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (inputEdit.getParent() != null && inputEdit.getParent() instanceof View) {
					View parent = (View) inputEdit.getParent();
					parent.setPadding(30, 30, 30, 30);
				}
			}
		}, 50);
	}
	
	private boolean isRootFolder(String path) {
		if (path.equals(SDCARD)) {
			return true;
		}
		return false;
	}
	
	private class SimpleListAdapter extends BaseAdapter {

		private int iconSize;
		private int expandIconSize;
		private int textLeftSpace;
		private Drawable icon, hasExpandIcon;
		
		public SimpleListAdapter() {

			icon = getContext().getResources().getDrawable(R.drawable.folderdlg_icon_folder);
			hasExpandIcon = getContext().getResources().getDrawable(R.drawable.folderdlg_expand);
			iconSize = getContext().getResources().getDimensionPixelSize(R.dimen.saveas_list_icon_size);
			expandIconSize = iconSize / 2;
			textLeftSpace = getContext().getResources().getDimensionPixelSize(R.dimen.folderdlg_text_left_space);
		}
		@Override
		public int getCount() {

			return fileList == null ? 0 : fileList.size();
		}

		@Override
		public Object getItem(int arg0) {
			
			return fileList == null ? 0 : fileList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			ContainerHolder containerHolder = null;
			if (convertView == null) {
				containerHolder = new ContainerHolder();

				LinearLayout container = new LinearLayout(context);
				AbsListView.LayoutParams params0 = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				container.setLayoutParams(params0);
				container.setOrientation(LinearLayout.HORIZONTAL);
				container.setGravity(Gravity.CENTER_VERTICAL);
				container.setPadding(20, 20, 20, 20);

				containerHolder.arrowIconView = new ImageView(getContext());
				LinearLayout.LayoutParams params = new LayoutParams(expandIconSize, expandIconSize);
				//int leftMargin = levelSpace;
				int leftMargin = 0;
				((MarginLayoutParams) params).leftMargin = leftMargin;
				containerHolder.arrowIconView.setImageDrawable(hasExpandIcon);
				containerHolder.arrowIconView.setLayoutParams(params);
				containerHolder.arrowIconView.setScaleType(ScaleType.FIT_CENTER);
				container.addView(containerHolder.arrowIconView);
				containerHolder.arrowIconView.setVisibility(View.INVISIBLE);
				
				containerHolder.folderIconView = new ImageView(getContext());
				params = new LayoutParams(iconSize, iconSize);
				containerHolder.folderIconView.setImageDrawable(icon);
				containerHolder.folderIconView.setLayoutParams(params);
				container.addView(containerHolder.folderIconView);
				
				containerHolder.nameView = new TextView(getContext());
				params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				leftMargin = textLeftSpace;
				((MarginLayoutParams) params).leftMargin = leftMargin;
				containerHolder.nameView.setLayoutParams(params);
				containerHolder.nameView.setTextColor(Color.BLACK);
				containerHolder.nameView.setGravity(Gravity.CENTER_VERTICAL);
				container.addView(containerHolder.nameView);
				
				containerHolder.bkLayout = container;
				convertView = container;
				convertView.setTag(containerHolder);
			}
			else {
				containerHolder = (ContainerHolder) convertView.getTag();
			}
			
			File file = fileList.get(position);
			containerHolder.nameView.setText(file.getName());
			if (file.listFiles(folderFilter).length > 0) {
				containerHolder.arrowIconView.setVisibility(View.VISIBLE);
			}
			else {
				containerHolder.arrowIconView.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}

		public class ContainerHolder {
			LinearLayout bkLayout;
			ImageView arrowIconView;
			ImageView folderIconView;
			TextView nameView;
		}
	}

	public void notifyOrientaionChanged() {
		computeHeight();
	}

}
