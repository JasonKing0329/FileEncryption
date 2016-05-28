package com.king.app.fileencryption.filemanager.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.filemanager.view.FolderDlgItemView.OnFolderSelectListener;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.publicview.DefaultDialogManager;
import com.king.app.fileencryption.util.ScreenUtils;

public class FolderDialog extends CustomDialog implements
		OnFolderSelectListener{

	private FolderManager folderManager;
	private List<File> fileList;
	private List<FolderDlgItemView> itemList;
	private LinearLayout folderContainer;
	private FolderDlgItemView selectedView;
	private ImageView addButton;

	public FolderDialog(Context context,
						OnCustomDialogActionListener actionListener) {
		super(context, actionListener);
		requestCancelAction(true);
		requestOkAction(true);
		applyGreyStyle();
		folderManager = new FolderManager();
		itemList = new ArrayList<FolderDlgItemView>();
		initViewData();
	}

	private void initViewData() {
		HashMap<String, Object> data = new HashMap<String, Object>();
		actionListener.onLoadData(data);
		Object obj = data.get(Constants.KEY_FOLDERDLG_ROOT);
		if (obj != null && obj instanceof String) {
			String rootPath = (String) obj;
			fileList = folderManager.loadList(rootPath);
			Collections.sort(fileList, new Comparator<File>() {

				@Override
				public int compare(File f1, File f2) {

					return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
				}
			});
			FolderDlgItemView itemView = null;
			for (int i = 0; i < fileList.size(); i ++) {
				itemView = createNewItem(fileList.get(i), 0, folderManager.hasFolderChild(fileList.get(i)));
				itemList.add(itemView);
				folderContainer.addView(itemView);
			}
		}

		updateHeight();
	}

	private FolderDlgItemView createNewItem(File file, int level, boolean hasExpand) {
		FolderDlgItemView itemView = new FolderDlgItemView(getContext(), file, level);
		itemView.setOnFolderSelectListener(this);
		itemView.setHasExpand(hasExpand);
		return itemView;
	}

	public void updateHeight() {
		int height = ScreenUtils.getScreenHeight(getContext())
				- getContext().getResources().getDimensionPixelSize(R.dimen.folderdlg_space_top) * 2;
		setHeight(height);
	}

	@Override
	protected View getCustomView() {

		View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_folder_content, null);
		folderContainer = (LinearLayout) view.findViewById(R.id.folder_dlg_container);
		//listView.setOnItemClickListener(this);
		return view;
	}

	@Override
	protected View getCustomToolbar() {
		int layoutRes = Application.isLollipop() ? R.layout.folderdlg_actionbar_l:R.layout.folderdlg_actionbar;
		View view = LayoutInflater.from(getContext()).inflate(layoutRes, null);
		addButton = (ImageView) view.findViewById(R.id.folderdlg_action_add);
		addButton.setOnClickListener(this);
		return view;
	}

	@Override
	public void onFolderSelect(FolderDlgItemView itemView) {
		if (itemView.isExpand()) {
			itemView.setExpand(false);
		}
		else {
			//background change
			if (selectedView != null) {
				selectedView.setContainerSelected(false);
			}
			itemView.setContainerSelected(true);
			selectedView = itemView;

			//add sub view
			File file = itemView.getFile();
			List<File> subList = folderManager.addSubFolders(file);
			if (subList == null) {
			}
			else {
				int level = itemView.getLevel() + 1;
				for (int i = 0; i < subList.size(); i ++) {
					file = subList.get(i);
					itemView.addSubView(file, level, folderManager.hasFolderChild(file));
				}
				itemView.setExpand(true);
			}
		}
	}

	@Override
	public void onClick(View view) {

		if (view == saveIcon) {
			//Application 约束1, 不能在包含子文件夹的目录存放图片
			if (selectedView == null) {
				return;
			}
			if (selectedView.hasExpand()) {
				Toast.makeText(getContext(), R.string.folderdlg_option_rule1, Toast.LENGTH_LONG).show();
				return;
			}

			actionListener.onSave(selectedView.getFile());
		}
		else if (view == addButton) {
			openCreateFolderDialog();
		}
		super.onClick(view);
	}

	private void openCreateFolderDialog() {
		new DefaultDialogManager().openCreateFolderDialog(context
				, new DefaultDialogManager.OnDialogActionListener() {

					@Override
					public void onOk(String name) {
						String parent = null;//root path
						if (selectedView != null) {
							parent = selectedView.getFile().getPath();
						}
						File file = folderManager.createFolder(parent, name);
						if (file == null) {
							Toast.makeText(getContext(), R.string.folderdlg_alreadyexist, Toast.LENGTH_LONG).show();
							return;
						}

						FolderDlgItemView itemView = null;
						if (selectedView == null) {
							itemView = createNewItem(file, 0, false);
							itemList.add(itemView);
							folderContainer.addView(itemView);
						}
						else {
							int level = selectedView.getLevel() + 1;
							selectedView.addSubView(file, level, false);
							selectedView.setHasExpand(true);
							selectedView.refreshContainer();
							selectedView.setExpand(true);
						}
					}
				});
	}

}
