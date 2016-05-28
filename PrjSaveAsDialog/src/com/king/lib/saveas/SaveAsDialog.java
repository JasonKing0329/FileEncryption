package com.king.lib.saveas;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class SaveAsDialog extends CustomPage implements OnItemSelectedListener {

	public interface OnSaveAsListener {
		public void onSaveAs(String folderPath, String name);
		public void onSaveAsCancel();
		public String getDefaultFolderPath();
		public String getDefaultName();
	}

	protected TextView urlView;
	protected Spinner pathSpinner;
	private ImageView browseButton;
	protected EditText nameEdit;
	private TextView okButton, cancelButton;

	private OnSaveAsListener onSaveAsListener;

	protected List<String> pathList;

	private PathListAdapter pathAdapter;

	private SimpleFolderSelectDialog folderSelectDialog;

	public SaveAsDialog(Context context, OnSaveAsListener actionListener) {
		super(context, null);
		onSaveAsListener = actionListener;
		setTitle(R.string.saveas_title);
		requestCloseAction(false);
		applyGreyStyle();

		String defaultPath = onSaveAsListener == null ? null:onSaveAsListener.getDefaultFolderPath();
		pathList = HistoryController.getLatestPaths(context, defaultPath);
		//ArrayAdapter的下拉列表，每一项都是限定长度，文字超过会被省略，由于360dp的手机宽度太窄，因此对于长路径需要多行显示，因此自定义适配
//		pathAdapter = new ArrayAdapter<String>(context
//				, android.R.layout.simple_dropdown_item_1line, pathList);
		pathAdapter = new PathListAdapter();
		pathSpinner.setAdapter(pathAdapter);
		pathSpinner.setOnItemSelectedListener(this);
		pathSpinner.setSelection(0);

		presetDialog();
	}

	protected void presetDialog() {
		if (onSaveAsListener != null && onSaveAsListener.getDefaultName() != null) {
			nameEdit.setText(onSaveAsListener.getDefaultName());
		}
		else {
			nameEdit.setText(System.currentTimeMillis() + ".png");
		}
		nameEdit.selectAll();
		nameEdit.post(new Runnable() {

			@Override
			public void run() {
				nameEdit.requestFocus();
			}
		});
	}

	@Override
	protected View getCustomView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.dlg_saveas_main, null);
		pathSpinner = (Spinner) view.findViewById(R.id.saveas_path);
		browseButton = (ImageView) view.findViewById(R.id.saveas_path_browse);
		nameEdit = (EditText) view.findViewById(R.id.saveas_filename);
		okButton = (TextView) view.findViewById(R.id.saveas_ok);
		cancelButton = (TextView) view.findViewById(R.id.saveas_cancel);
		browseButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		okButton.setOnClickListener(this);
		if (Configs.isLollipop()) {
//			okButton.setBackgroundResource(R.drawable.browser_ripple_rect_grey_bk);
//			cancelButton.setBackgroundResource(R.drawable.browser_ripple_rect_grey_bk);
//			browseButton.setBackgroundResource(R.drawable.browser_ripple_oval_grey_bk);
		}
		return view;
	}

	@Override
	protected View getCustomToolbar() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.dlg_saveas_toolbar, null);
		urlView = (TextView) view.findViewById(R.id.saveas_url);
		return view;
	}

	@Override
	public void onClick(View view) {

		if (view == okButton) {
			String name = nameEdit.getText().toString();
			if (name.trim().length() == 0) {
				nameEdit.setError(getContext().getResources().getString(R.string.saveas_title_notnull));
				return;
			}

			setPathAsLatest();
			HistoryController.saveLatestPath(context, pathList);
			onClickOkAction(name);
			dismiss();
		}
		else if (view == cancelButton) {
			if (onSaveAsListener != null) {
				onSaveAsListener.onSaveAsCancel();
			}
			dismiss();
		}
		else if (view == browseButton) {
			folderSelectDialog = new SimpleFolderSelectDialog(context, new OnCustomPageActionListener() {

				@Override
				public void onSelect(Object object) {
					String path = (String) object;
					onSelectNewPath(path);
				}

				@Override
				public void onLoadData(HashMap<String, Object> data) {
				}

				@Override
				public boolean onCancel() {
					return false;
				}

				@Override
				public void onAction(Object object) {
				}
			});
			folderSelectDialog.show();
		}
		super.onClick(view);
	}

	protected void onClickOkAction(String filename) {
		if (onSaveAsListener != null) {
			onSaveAsListener.onSaveAs(pathList.get(pathSpinner.getSelectedItemPosition()), filename);
		}
	}

	private void setPathAsLatest() {
		int index = pathSpinner.getSelectedItemPosition();
		if (index != 0) {
			String path = pathList.remove(index);
			pathList.add(0, path);
		}
	}

	protected void onSelectNewPath(String path) {
		int existIndex = -1;
		for (int i = 0; i < pathList.size(); i ++) {
			if (pathList.get(i).equals(path)) {
				existIndex = i;
				break;
			}
		}
		if (existIndex == -1) {
			pathList.add(0, path);
			pathAdapter.notifyDataSetChanged();
			pathSpinner.setSelection(0);
		}
		else {
			pathSpinner.setSelection(existIndex);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int index,
							   long arg3) {

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

	public void notifyOrientaionChanged(int orientation) {
		if (folderSelectDialog != null && folderSelectDialog.isShowing()) {
			folderSelectDialog.notifyOrientaionChanged();
		}
	}

	private class PathListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return pathList.size();
		}

		@Override
		public Object getItem(int position) {
			return pathList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			TextView textView = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.dlg_saveas_spinner_item, null);
				textView = (TextView) convertView.findViewById(R.id.saveas_spinner_text);
				convertView.setTag(textView);
			}
			else {
				textView = (TextView) convertView.getTag();
			}

			textView.setText(pathList.get(position));
			return convertView;
		}

	}
}
