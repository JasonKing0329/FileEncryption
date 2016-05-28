package com.king.app.fileencryption.filemanager.view;

import java.io.File;
import java.io.FilenameFilter;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.publicview.CustomDialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class LoadFromDialog extends CustomDialog implements OnItemClickListener {

	public static final int DATA_HISTORY = 1;
	public static final int DATA_GAME = 2;
	public static final int DATA_TEAM_GAME = 3;

	private int loadData;

	private ListView listView;
	private ArrayAdapter<String> adapter;
	private String[] historyFiles;
	private OnCustomDialogActionListener actionListener;
	private View lastChosedItem;
	private String chosedItem;

	public LoadFromDialog(Context context, int loadData,
						  OnCustomDialogActionListener actionListener) {
		super(context, actionListener);
		this.loadData = loadData;
		this.actionListener = actionListener;
		setTitle(R.string.menu_load);
		requestOkAction(true);
		requestCancelAction(true);
		//FIXME 需要考虑转屏
		//setHeight(context.getResources().getDimensionPixelSize(R.dimen.dialog_loadfrom_height));
		initData();
	}

	private void initData() {
		if (loadData == DATA_HISTORY) {
			historyFiles = loadHistoryFiles();
		}
		else if (loadData == DATA_GAME) {
			historyFiles = loadGameFiles();
		}
		else if (loadData == DATA_TEAM_GAME) {
			historyFiles = loadTeamGameFiles();
		}
		if (historyFiles == null || historyFiles.length == 0) {

			return;
		}
		adapter = new ArrayAdapter<String>(context
				, android.R.layout.simple_dropdown_item_1line, historyFiles);
		listView.setAdapter(adapter);
	}

	private String[] loadTeamGameFiles() {
		File file = new File(Configuration.APP_DIR_GAME);
		String[] names = file.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {

				return filename.endsWith(".jag");
			}
		});
		return names;
	}

	private String[] loadHistoryFiles() {
		File file = new File(Configuration.APP_DIR_DB_HISTORY);
		String[] names = file.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {

				return filename.endsWith(".db");
			}
		});
		return names;
	}

	private String[] loadGameFiles() {
		File file = new File(Configuration.APP_DIR_GAME);
		String[] names = file.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {

				return filename.endsWith(".jg");
			}
		});
		return names;
	}

	@Override
	public void onClick(View view) {
		if (view == saveIcon) {
			if (chosedItem != null) {
				String path = null;
				if (loadData == DATA_GAME) {
					path = Configuration.APP_DIR_GAME + "/" + chosedItem;
				}
				if (loadData == DATA_TEAM_GAME) {
					path = Configuration.APP_DIR_GAME + "/" + chosedItem;
				}
				else if (loadData == DATA_HISTORY) {
					path = Configuration.APP_DIR_DB_HISTORY + "/" + chosedItem;
				}
				if (path == null) {
					return;
				}

				final File file = new File(path);
				if (loadData == DATA_HISTORY) {
					if (file.exists()) {
						new AlertDialog.Builder(context)
								.setTitle(R.string.warning)
								.setMessage(R.string.load_from_warning_msg)
								.setPositiveButton(R.string.ok, new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										actionListener.onSave(file);
										dismiss();
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.show();
					}
				}
				else if (loadData == DATA_GAME) {
					actionListener.onSave(file);
					dismiss();
				}
				else if (loadData == DATA_TEAM_GAME) {
					actionListener.onSave(file);
					dismiss();
				}
			}
			if (listView == null) {
				super.onClick(view);
			}
		}
		else {
			super.onClick(view);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		if (lastChosedItem != null) {
			lastChosedItem.setBackground(null);
		}
		view.setBackgroundColor(getContext().getResources().getColor(R.color.title_court_clay));
		lastChosedItem = view;
		chosedItem = historyFiles[position];
	}

	@Override
	protected View getCustomView() {
		listView = new ListView(context);
		listView.setDivider(null);
		listView.setOnItemClickListener(this);
		return listView;
	}

	@Override
	protected View getCustomToolbar() {
		// TODO Auto-generated method stub
		return null;
	}

}
