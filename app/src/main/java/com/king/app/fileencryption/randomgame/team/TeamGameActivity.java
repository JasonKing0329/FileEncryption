package com.king.app.fileencryption.randomgame.team;

import java.io.File;
import java.util.HashMap;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.filemanager.view.LoadFromDialog;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.publicview.DefaultDialogManager;
import com.king.app.fileencryption.publicview.DragSideBarTrigger;
import com.king.app.fileencryption.publicview.DragSideBar;
import com.king.app.fileencryption.randomgame.team.DragSideViewManager.OnSideListener;
import com.king.app.fileencryption.randomgame.team.TeamGameTable.OnTableListener;
import com.king.app.fileencryption.randomgame.update.GameData;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.view.SOrderChooserUpdate;
import com.king.app.fileencryption.sorder.view.SOrderChooserUpdate.OnOrderSelectListener;
import com.king.app.fileencryption.thumbfolder.ThumbFolderActivity;
import com.king.app.fileencryption.util.DisplayHelper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;

public class TeamGameActivity extends Activity implements OnClickListener
		, OnTableListener, OnSideListener {

	private final int REQUEST_CODE_UPDATE_DATA = 101;
	private final int REQUEST_CODE_UPDATE_TITLE = 100;

	private class RandomItem {
		public ImageView imageView;
		public SOrder order;
		public boolean isRunning;
		public TeamGameController gameController;
		public String path;
		public Bitmap bitmap;
	}
	private interface SaveCallBack {
		public void onSaveOk();
		public void onSaveFail();
	}

	private RandomItem randomItem1, randomItem2;
	private TeamGameTable gameTable;
	private View sideView;
	private DragSideBar dragSideBar;
	private DragSideBarTrigger dragSideBarTrigger;
	private DragSideViewManager dragSideViewManager;

	private RandomItem randomFinishItem;
	private TeamGameData gameData;
	private IOController ioController;

	private TableEditDropItemListener editDropItemListener;
	private ListPopupWindow tableEditPopupWindow;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(new ThemeManager(this).getDefaultTheme());
		super.onCreate(savedInstanceState);

		DisplayHelper.enableFullScreen(this);
		DisplayHelper.disableScreenshot(this);

		setContentView(R.layout.layout_team_game);

		randomItem1 = new RandomItem();
		randomItem2 = new RandomItem();
		randomItem1.imageView = (ImageView) findViewById(R.id.game_team_player1);
		randomItem2.imageView = (ImageView) findViewById(R.id.game_team_player2);
		dragSideBar = (DragSideBar) findViewById(R.id.game_team_sidebar);
		sideView = findViewById(R.id.game_team_side);
		sideView.setOnTouchListener(sideTouchListener);
		gameTable = (TeamGameTable) findViewById(R.id.game_team_table);

		randomItem1.imageView.setOnClickListener(this);
		randomItem2.imageView.setOnClickListener(this);

		gameTable.setOnTableListener(this);
		gameTable.setTitleColumn(4);
		gameTable.setDataColumn(8);
		gameTable.setDataRow(8);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				gameTable.build();
			}
		}, 100);

		dragSideBarTrigger = new DragSideBarTrigger(this, dragSideBar);
		dragSideViewManager = new DragSideViewManager(this, dragSideBar);
		dragSideViewManager.setOnSideListener(this);

		randomItem1.gameController = new TeamGameController();
		randomItem2.gameController = new TeamGameController();
		gameData = new TeamGameData();
		ioController = new IOController();
	}

	@Override
	public void onClick(View view) {
		if (view == randomItem1.imageView) {
			//未选择列表则选择列表
			if (randomItem1.order == null) {
				selectOrder(1);
			}
			else {
				//side bar列表判定
				//重新选择列表
				if (dragSideViewManager.isChangeOrder()) {
					selectOrder(1);
				}
				//执行随机过程
				else if (dragSideViewManager.isProcessRandom()) {
					if (randomItem1.order.getImgPathList() == null) {
						SOrderPictureBridge.getInstance(this).getOrderItemList(randomItem1.order);
					}
					processRandom(randomItem1);
				}
			}
		}
		else if (view == randomItem2.imageView) {
			if (randomItem2.order == null) {
				selectOrder(2);
			}
			else {
				if (dragSideViewManager.isChangeOrder()) {
					selectOrder(2);
				}
				else if (dragSideViewManager.isProcessRandom()) {
					if (randomItem2.order.getImgPathList() == null) {
						SOrderPictureBridge.getInstance(this).getOrderItemList(randomItem2.order);
					}
					processRandom(randomItem2);
				}
			}
		}
	}

	/**
	 * 为randomitem1和2选择随机列表
	 * @param index only 1 or 2
	 */
	private void selectOrder(final int index) {
		SOrderChooserUpdate chooser = new SOrderChooserUpdate(this, new OnOrderSelectListener() {

			@Override
			public void onSelect(SOrder order) {
				//列表选择完成后要重新(开始)构造game controller
				int width = randomItem1.imageView.getWidth();
				Bitmap bitmap = PictureManagerUpdate.getInstance().createImage(order.getCoverPath()
						, width * width, TeamGameActivity.this, 0);
				ImageView imageView = randomItem1.imageView;
				if (index == 1) {
					randomItem1.order = order;
					randomItem1.gameController.setRange(0, order.getItemNumber() - 1);
					randomItem1.gameController.build();
				}
				else {
					randomItem2.order = order;
					randomItem2.gameController.setRange(0, order.getItemNumber() - 1);
					randomItem2.gameController.build();
					imageView = randomItem2.imageView;
				}
				imageView.setImageBitmap(bitmap);
				imageView.setScaleType(ScaleType.FIT_XY);
			}
		});
		chooser.setTitle(getResources().getString(R.string.rgame_select_order));
		chooser.show();
	}

	/**
	 * 执行随机过程
	 * @param randomItem 试图层面上左ImageView与右ImageView
	 */
	private void processRandom(RandomItem randomItem) {

		if (randomItem.isRunning) {
			//结束随机过程
			randomItem.isRunning = false;
			stopRandoming(randomItem);
			//side bar判定，填充表格标题栏
			if (dragSideViewManager.isFillTitleAfterRandom()) {
				Bitmap bitmap = getPressedBitmap(gameTable.getTitleWidth(), randomItem.path);
				gameTable.setNextTitleData(randomItem.path, bitmap);
			}
		}
		else {
			randomItem.isRunning = true;
			new RandomThread(randomItem).start();
		}
	}

	/**
	 * 获取大小合适的bitmap
	 * @param width
	 * @param path
	 * @return
	 */
	private Bitmap getPressedBitmap(int width, String path) {
		return PictureManagerUpdate.getInstance().createImage(path
				, width * width, TeamGameActivity.this, 0);
	}

	/**
	 * 控制随机过程，通知ImageView更新视图
	 * @author Administrator
	 *
	 */
	private class RandomThread extends Thread implements Callback {

		private RandomItem randomItem;

		public RandomThread(RandomItem randomItem) {
			this.randomItem = randomItem;
		}

		private Handler handler = new Handler(this);
		public void run() {
			while (randomItem.isRunning) {
				try {
					Message message = new Message();
					handler.sendMessage(message);
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
		@Override
		public boolean handleMessage(Message msg) {

			//获取随机值
			int index = randomItem.gameController.randomProcessing();
			if (index == -1) {
				//已无可随机项
				stopRandoming(randomItem);
			}
			else {
				//成功获取随机index，实时更新随机结果
				try {
					String path = randomItem.order.getImgPathList().get(index);
					int size = randomItem.imageView.getWidth();
					Bitmap bitmap = getPressedBitmap(size, path);
					randomItem.imageView.setImageBitmap(bitmap);

					if (randomItem.bitmap != null) {
						randomItem.bitmap.recycle();
					}
					randomItem.path = path;
					randomItem.bitmap = bitmap;
				} catch (Exception e) {
					Toast.makeText(TeamGameActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
				}
			}

			return true;
		}

	}

	private void stopRandoming(RandomItem randomItem) {
		randomItem.isRunning = false;
		int index = randomItem.gameController.randomSelect();

		String path = null;
		Bitmap bitmap = null;

		if (index == -1) {
			//已无可随机项
			randomFinishItem = null;
			Toast.makeText(TeamGameActivity.this, R.string.rgame_no_random_item, Toast.LENGTH_LONG).show();
		}
		else {
			//成功获取随机index，最终更新随机结果
			try {
				path = randomItem.order.getImgPathList().get(index);
				int size = randomItem.imageView.getWidth();
				bitmap = PictureManagerUpdate.getInstance().createImage(path
						, size * size, TeamGameActivity.this, 0);
				randomItem.imageView.setImageBitmap(bitmap);

				if (randomItem.bitmap != null) {
					randomItem.bitmap.recycle();
				}
				randomItem.path = path;
				randomItem.bitmap = bitmap;

				//记录最终结果，以便单击title响应set as team member事件
				randomFinishItem = randomItem;
			} catch (Exception e) {
				Toast.makeText(TeamGameActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
			}

		}

	}

	/**
	 * 触发sidebar的touch事件
	 */
	OnTouchListener sideTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			if (dragSideBarTrigger.onTriggerTouch(event)) {
				return true;
			}
			return true;
		}
	};

	@Override
	public void onClickTableTitle(int col) {
		//side bar 判定，编辑单元格模式
		if (dragSideViewManager.isTitleEditable()) {
			if (editDropItemListener == null) {
				editDropItemListener = new TableEditDropItemListener();
			}
			else {
				editDropItemListener.updateTableData(0, col, REQUEST_CODE_UPDATE_TITLE);
			}

			showEditPopup(gameTable.getTitleViewAt(col));
		}
		//set as team member, after random event must be wait
		else if (dragSideViewManager.isAsTeamMember() && dragSideViewManager.isWaitAfterRandom()) {
			//column top
			int column = 2 * col;
			if (randomFinishItem != null) {
				if (randomFinishItem == randomItem2) {
					//column bottom
					column ++;
				}
				int size = gameTable.getCellWidth();
				Bitmap bitmap = getPressedBitmap(size, randomFinishItem.path);
				gameTable.setNextRowCellDataAtColumn(column, randomFinishItem.path, bitmap);
			}
		}
	}

	@Override
	public void onClickTableData(int row, int col) {

		//side bar 判定，编辑单元格模式
		if (dragSideViewManager.isDataEditable()) {
			if (editDropItemListener == null) {
				editDropItemListener = new TableEditDropItemListener();
			}
			else {
				editDropItemListener.updateTableData(row, col, REQUEST_CODE_UPDATE_DATA);
			}

			showEditPopup(gameTable.getCellViewAt(row, col));
		}
		//top栏显示在ImageView 1, bottom栏显示在ImageView 2
		else if (dragSideViewManager.isClickDataToShow()) {
			String path = gameTable.getCellData(row, col);
			if (path != null) {
				RandomItem item = randomItem1;
				if (col % 2 == 1) {
					item = randomItem2;
				}
				int size = item.imageView.getWidth();
				item.path = path;
				//先回收之前的bitmap
				if (item.bitmap != null) {
					item.imageView.setImageBitmap(null);
					item.bitmap.recycle();
				}
				Bitmap bitmap = PictureManagerUpdate.getInstance().createImage(path
						, size * size, TeamGameActivity.this, 0);
				item.bitmap = bitmap;
				item.imageView.setScaleType(ScaleType.FIT_XY);
				item.imageView.setImageBitmap(bitmap);
			}
		}
	}

	private void showEditPopup(View anchor) {
		if (tableEditPopupWindow == null) {
			tableEditPopupWindow = new ListPopupWindow(this);
			tableEditPopupWindow.setWidth(400);
			String[] menuItems = getResources().getStringArray(R.array.game_edit_drop);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this
					, android.R.layout.simple_list_item_1, menuItems);
			tableEditPopupWindow.setAdapter(adapter);
			tableEditPopupWindow.setOnItemClickListener(editDropItemListener);
		}
		tableEditPopupWindow.setAnchorView(anchor);
		tableEditPopupWindow.show();
		tableEditPopupWindow.getListView().setDivider(null);

	}

	@Override
	public void onSaveData() {
		onSaveData(null);
	}

	private void onSaveData(SaveCallBack callBack) {
		if (gameData != null) {
			if (gameData.isNewFile()) {
				saveGameDataAs(callBack);
			}
			else {
				saveGmeData();
				if (callBack != null) {
					callBack.onSaveOk();
				}
			}
		}
	}

	private void saveGmeData() {
		gameData.setTitles(gameTable.getTitleDatas());
		gameData.setDatas(gameTable.getCellDatas());
		ioController.saveTableData(gameData);
		gameData.setFlag(GameData.FLAG_LOADED);
	}

	private void saveGameDataAs(final SaveCallBack callBack) {
		if (gameData != null) {
			new DefaultDialogManager().openSaveFileDialog(this
					, new DefaultDialogManager.OnDialogActionListener() {

						@Override
						public void onOk(String name) {
							gameData.setFileName(Configuration.APP_DIR_GAME + "/" + name);
							saveGmeData();
							if (callBack != null) {
								callBack.onSaveOk();
							}
						}
					}, ".jag");
		}
	}

	@Override
	public void onSaveAs() {
		saveGameDataAs(null);
	}
	@Override
	public void onLoadData() {
		openLoadFromDialog();
	}

	private void openLoadFromDialog() {
		new LoadFromDialog(this, LoadFromDialog.DATA_TEAM_GAME, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				if (object == null) {
					return false;
				}

				File file = (File) object;
				new LoadThread(file.getPath()).start();

				return true;
			}

			@Override
			public void onLoadData(HashMap<String, Object> data) {

			}

			@Override
			public boolean onCancel() {
				return false;
			}
		}).show();
	}

	private class LoadThread extends Thread implements Callback {

		private final int FLAG_UPDATE_TITLE = 101;
		private final int FLAG_UPDATE_DATA = 102;
		private Handler handler;
		private String filePath;
		private Bitmap[] titleBitmaps;
		private Bitmap[][] dataBitmaps;

		public LoadThread(String filePath) {
			this.filePath = filePath;
			handler = new Handler(this);
		}

		@Override
		public void run() {

			gameData = ioController.readTableData(filePath);
			gameData.setFlag(GameData.FLAG_LOADED);
			gameData.setFileName(filePath);

			//title datas
			titleBitmaps = new Bitmap[gameData.getTitles().length];
			for (int i = 0; i < gameData.getTitles().length; i ++) {
				int size = gameTable.getTitleWidth();
				Bitmap bitmap = null;
				if (gameData.getTitles()[i] != null) {
					bitmap = PictureManagerUpdate.getInstance().createImage(gameData.getTitles()[i]
							, size * size, TeamGameActivity.this, 0);
				}
				titleBitmaps[i] = bitmap;
				Message message = new Message();
				message.what = FLAG_UPDATE_TITLE;
				message.obj = i;
				handler.sendMessage(message);
			}

			//cell datas
			String[][] datas = gameData.getDatas();
			dataBitmaps = new Bitmap[datas.length][];
			for (int i = 0; i <datas.length; i ++) {
				dataBitmaps[i] = new Bitmap[datas[0].length];
				for (int j = 0; j <datas[0].length; j ++) {
					int size = gameTable.getCellWidth();
					Bitmap bitmap = null;
					if (datas[i][j] != null) {
						bitmap = PictureManagerUpdate.getInstance().createImage(datas[i][j]
								, size * size, TeamGameActivity.this, 0);
					}
					dataBitmaps[i][j] = bitmap;
					Message message = new Message();
					message.what = FLAG_UPDATE_DATA;
					message.obj = i + "," + j;
					handler.sendMessage(message);
				}
			}
		}

		@Override
		public boolean handleMessage(Message message) {

			if (message.what == FLAG_UPDATE_TITLE) {
				int column = (Integer) message.obj;
				gameTable.setTitleDataAtColumn(column
						, gameData.getTitles()[column], titleBitmaps[column]);
			}
			else if (message.what == FLAG_UPDATE_DATA) {
				String string = (String) message.obj;
				String[] array = string.split(",");
				int row = Integer.parseInt(array[0]);
				int col = Integer.parseInt(array[1]);
				gameTable.setCellDataAt(row, col
						, gameData.getDatas()[row][col], dataBitmaps[row][col]);
			}
			return true;
		}

	}

	private class TableEditDropItemListener implements OnItemClickListener {

		private int requestCode;
		private int row, col;

		public TableEditDropItemListener() {

		}
		public void updateTableData(int row, int col, int requestCode) {
			this.row = row;
			this.col = col;
			this.requestCode = requestCode;
		}
		public int getCurrentRow() {
			return row;
		}
		public int getCurrentCol() {
			return col;
		}
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {
			if (position == 0) {//folder
				Intent intent = new Intent();
				intent.setClass(TeamGameActivity.this, ThumbFolderActivity.class);
				Bundle bundle = new Bundle();
				bundle.putBoolean(Constants.KEY_THUMBFOLDER_CHOOSER_MODE, true);
				bundle.putInt(Constants.KEY_THUMBFOLDER_INIT_MODE, ThumbFolderActivity.SRC_MODE_FOLDER);
				intent.putExtras(bundle);
				startActivityForResult(intent, requestCode);
			}
			else if (position == 1) {//order
				Intent intent = new Intent();
				intent.setClass(TeamGameActivity.this, ThumbFolderActivity.class);
				Bundle bundle = new Bundle();
				bundle.putBoolean(Constants.KEY_THUMBFOLDER_CHOOSER_MODE, true);
				bundle.putInt(Constants.KEY_THUMBFOLDER_INIT_MODE, ThumbFolderActivity.SRC_MODE_ORDER);
				intent.putExtras(bundle);
				startActivityForResult(intent, requestCode);
			}
			else if (position == 2) {//add to order
				final String path = gameTable.getCellData(row, col);
				if (path != null) {
					SOrderChooserUpdate chooser = new SOrderChooserUpdate(TeamGameActivity.this, new CustomDialog.OnCustomDialogActionListener() {

						@Override
						public boolean onSave(Object object) {

							if (object != null) {
								SOrder order = (SOrder) object;
								SOrderPictureBridge.getInstance(TeamGameActivity.this).addToOrder(path, order.getId());
							}
							return true;
						}

						@Override
						public void onLoadData(HashMap<String, Object> data) {

						}

						@Override
						public boolean onCancel() {

							return false;
						}
					});
					chooser.setTitle(getResources().getString(R.string.add_to_order));
					chooser.show();
				}
			}
			else if (position == 3) {//clear
				if (requestCode == REQUEST_CODE_UPDATE_TITLE) {
					gameTable.setTitleDataAtColumn(
							editDropItemListener.getCurrentCol(), null, null);
				}
				else if (requestCode == REQUEST_CODE_UPDATE_DATA) {
					gameTable.setCellDataAt(editDropItemListener.getCurrentRow()
							, editDropItemListener.getCurrentCol(), null, null);
				}
			}
			tableEditPopupWindow.dismiss();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_CODE_UPDATE_DATA) {
			if (data != null) {
				String path = data.getStringExtra(Constants.KEY_THUMBFOLDER_CHOOSE_CONTENT);
				if (path != null) {
					int size = gameTable.getCellWidth();
					Bitmap bitmap = PictureManagerUpdate.getInstance().createImage(path
							, size * size, TeamGameActivity.this, 0);
					gameTable.setCellDataAt(editDropItemListener.getCurrentRow()
							, editDropItemListener.getCurrentCol(), path, bitmap);
				}
			}
		}
		else if (requestCode == REQUEST_CODE_UPDATE_TITLE) {
			if (data != null) {
				String path = data.getStringExtra(Constants.KEY_THUMBFOLDER_CHOOSE_CONTENT);
				if (path != null) {
					int size = gameTable.getTitleWidth();
					Bitmap bitmap = PictureManagerUpdate.getInstance().createImage(path
							, size * size, TeamGameActivity.this, 0);
					gameTable.setTitleDataAtColumn(
							editDropItemListener.getCurrentCol(), path, bitmap);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onResetOrder(int index) {
		if (index == 1) {
			randomItem1.gameController.reset();
		}
		else if (index == 2) {
			randomItem2.gameController.reset();
		}
	}

	@Override
	public void onNew() {
		if (gameData != null) {
			new DefaultDialogManager().showWarningActionDialog(this
					, getResources().getString(R.string.team_game_msg_warning_save)
					, getResources().getString(R.string.save)
					, getResources().getString(R.string.giveup)
					, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int pos) {
							if (pos == DialogInterface.BUTTON_POSITIVE) {//save
								onSaveData(new SaveCallBack() {

									@Override
									public void onSaveOk() {
										newGame();
									}

									@Override
									public void onSaveFail() {

									}
								});
							}
							else if (pos == DialogInterface.BUTTON_NEUTRAL) {//give up
								newGame();
							}
							else if (pos == DialogInterface.BUTTON_NEGATIVE) {//cancel

							}
						}
					});
		}
	}

	private void newGame() {
		gameData = new TeamGameData();
		gameTable.setTitleColumn(4);
		gameTable.setDataColumn(8);
		gameTable.setDataRow(8);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				gameTable.build();
			}
		}, 100);
	}

	@Override
	public void onBackPressed() {
		if (dragSideBar.isOpen()) {
			dragSideBar.dismiss(true);
			return;
		}

		if (gameData != null) {
			new DefaultDialogManager().showWarningActionDialog(this
					, getResources().getString(R.string.team_game_msg_warning_save)
					, getResources().getString(R.string.save)
					, getResources().getString(R.string.giveup)
					, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int pos) {
							if (pos == DialogInterface.BUTTON_POSITIVE) {//save
								onSaveData(new SaveCallBack() {

									@Override
									public void onSaveOk() {
										onSuperBackPressed();
									}

									@Override
									public void onSaveFail() {

									}
								});
							}
							else if (pos == DialogInterface.BUTTON_NEUTRAL) {//give up
								onSuperBackPressed();
							}
							else if (pos == DialogInterface.BUTTON_NEGATIVE) {//cancel

							}
						}
					});
			return;
		}
		onSuperBackPressed();
	}

	public void onSuperBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onTurnData() {
		new TurnDataDialog(this, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				Toast.makeText(TeamGameActivity.this, object.toString(), Toast.LENGTH_LONG).show();
				return false;
			}

			@Override
			public void onLoadData(HashMap<String, Object> data) {
				gameData.setTitles(gameTable.getTitleDatas());
				gameData.setDatas(gameTable.getCellDatas());
				data.put("data", gameData);
			}

			@Override
			public boolean onCancel() {
				return false;
			}
		}).show();
	}

}
