package com.king.app.fileencryption.randomgame.update;

import java.io.File;
import java.util.HashMap;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.filemanager.view.LoadFromDialog;
import com.king.app.fileencryption.publicview.ActionBar;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.publicview.DefaultDialogManager;
import com.king.app.fileencryption.publicview.ActionBar.ActionBarListener;
import com.king.app.fileencryption.randomgame.RandomRules;
import com.king.app.fileencryption.randomgame.update.CellDataDialog.OnCellDataChangeListener;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.view.PreviewDialog;
import com.king.app.fileencryption.sorder.view.SOrderChooserUpdate;
import com.king.app.fileencryption.sorder.view.SOrderChooserUpdate.OnOrderSelectListener;
import com.king.app.fileencryption.thumbfolder.ThumbFolderActivity;
import com.king.app.fileencryption.util.DisplayHelper;
import com.king.app.fileencryption.util.ScreenUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public class GameActivity extends Activity implements OnClickListener, ActionBarListener
		, OnMenuItemClickListener, OnGameTableActionListener, OnCellDataChangeListener
		, OnLongClickListener {

	private final int HEAD_REQUEST_CODE = 100;
	private GameTableView gameTable;
	private TextView initTableButton;
	private EditText rowEdit, colEdit, tableNameEdit;
	private TextView startButton1, restartButton1;
	private TextView startButton2, restartButton2;
	private ImageView coverView1, coverView2;
	private ImageView randomPreView, randomNextView;
	private TextView roundTextView;
	private LinearLayout randomLayout1, randomLayout2;
	private SOrder order1, order2;
	private LinearLayout initTableLayout;
	private LinearLayout radioGroup;
	private RadioButton randomRadioButton, editRadioButton;

	private int orientation;
	private RandomRules randomRules;
	private GameController gameController1, gameController2;
	private boolean isRandoming;

	private int curLeftNumber;

	private String randomPath1, randomPath2;
	private Bitmap randomBitmap1, randomBitmap2;
	private TableData highLightData;

	private RandomController randomController;
	private GameData gameData;
	private ActionBar actionBar;

	private ListPopupWindow headEditPopupWindow;
	private HeadClickListener headClickListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(new ThemeManager(this).getDefaultTheme());
		super.onCreate(savedInstanceState);

		DisplayHelper.enableFullScreen(this);
		DisplayHelper.disableScreenshot(this);

		if (Application.isLollipop()) {
			setContentView(R.layout.layout_randomgame_update_l);
		}
		else {
			setContentView(R.layout.layout_randomgame_update);
		}

		gameController1 = new GameController();
		gameController2 = new GameController();
		randomController = new RandomController();

		initView();
		orientation = getResources().getConfiguration().orientation;

		actionBar.setTitle("GameActivity");

	}

	private void initView() {
		initTableButton = (TextView) findViewById(R.id.rgame_table_init);
		initTableButton.setOnClickListener(this);
		rowEdit = (EditText) findViewById(R.id.rgame_edit_row);
		colEdit = (EditText) findViewById(R.id.rgame_edit_col);
		tableNameEdit = (EditText) findViewById(R.id.rgame_edit_tablename);
		startButton1 = (Button) findViewById(R.id.rgame_start1);
		restartButton1 = (Button) findViewById(R.id.rgame_restart1);
		startButton2 = (Button) findViewById(R.id.rgame_start2);
		restartButton2 = (Button) findViewById(R.id.rgame_restart2);
		roundTextView = (TextView) findViewById(R.id.rgame_round);
		coverView1 = (ImageView) findViewById(R.id.rgame_image1);
		coverView2 = (ImageView) findViewById(R.id.rgame_image2);
		randomPreView = (ImageView) findViewById(R.id.rgame_previous);
		randomNextView = (ImageView) findViewById(R.id.rgame_next);
		initTableLayout = (LinearLayout) findViewById(R.id.layout_init_table);
		randomLayout1 = (LinearLayout) findViewById(R.id.rgame_random_layout_button1);
		randomLayout2 = (LinearLayout) findViewById(R.id.rgame_random_layout_button2);
		radioGroup = (LinearLayout) findViewById(R.id.rgame_radio_group);
		randomRadioButton = (RadioButton) findViewById(R.id.rgame_radio_random);
		editRadioButton = (RadioButton) findViewById(R.id.rgame_radio_edit);
		startButton1.setOnClickListener(this);
		startButton2.setOnClickListener(this);
		restartButton1.setOnClickListener(this);
		restartButton2.setOnClickListener(this);
		coverView1.setOnClickListener(this);
		coverView2.setOnClickListener(this);
		coverView1.setOnLongClickListener(this);
		coverView2.setOnLongClickListener(this);
		randomPreView.setOnClickListener(this);
		randomNextView.setOnClickListener(this);

		gameTable = (GameTableView) findViewById(R.id.rgame_table);
		gameTable.setOnTableActionListener(this);
		initTableSize(getResources().getConfiguration());

		actionBar = new ActionBar(this, this);
		actionBar.addMenuIcon();
		actionBar.addSaveIcon();
		actionBar.addShowIcon();

		roundTextView.setText("Round " + randomController.getRound());
	}

	private void initTableSize(android.content.res.Configuration configuration) {
		if (configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
			int width = ScreenUtils.getScreenWidth(this);
			gameTable.setTableSize(width, width);
		}
		else {
			int height = ScreenUtils.getScreenHeight(this);
			height = height - getResources().getDimensionPixelSize(R.dimen.actionbar_height);
			gameTable.setTableSize(height, height);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onConfigurationChanged(
			android.content.res.Configuration newConfig) {

		if (newConfig.orientation != orientation) {
			orientation = newConfig.orientation;

			//save params before orientation change
			int radioGroupVisi = radioGroup.getVisibility();
			int initTableVisi = initTableLayout.getVisibility();
			int randomVisi = randomLayout1.getVisibility();
			boolean randomRadioCheck = randomRadioButton.isChecked();
			boolean editRadioCheck = editRadioButton.isChecked();
			if (gameData != null) {
				gameData.setTableDatas(gameTable.getTableDatas());
			}

			//load new orientation layout
			if (Application.isLollipop()) {
				setContentView(R.layout.layout_randomgame_update_l);
			}
			else {
				setContentView(R.layout.layout_randomgame_update);
			}
			//re-init view control
			initView();
			//re-init view status
			radioGroup.setVisibility(radioGroupVisi);
			initTableLayout.setVisibility(initTableVisi);
			randomLayout1.setVisibility(randomVisi);
			randomLayout2.setVisibility(randomVisi);
			randomRadioButton.setChecked(randomRadioCheck);
			editRadioButton.setChecked(editRadioCheck);
			if (gameData != null) {
				gameTable.createFrom(gameData.getTableDatas());
			}
			if (randomBitmap1 != null) {
				coverView1.setScaleType(ScaleType.FIT_XY);
				coverView1.setImageBitmap(randomBitmap1);
			}
			if (randomBitmap2 != null) {
				coverView2.setScaleType(ScaleType.FIT_XY);
				coverView2.setImageBitmap(randomBitmap2);
			}

			//initTableVisi=VISIBLE情况下，gameTable已removeAllView
			if (initTableVisi != View.VISIBLE && highLightData != null) {
				gameTable.setCellHightLight(highLightData.getRow(), highLightData.getCol());
			}
			roundTextView.setText("Round " + randomController.getRound());
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View view) {
		if (view == initTableButton) {
			String text = rowEdit.getText().toString();
			if (text == null || text.length() == 0) {
				rowEdit.setError("Please input row number");
				return;
			}
			int row = Integer.parseInt(text);
			if (row < 1 || row > 16) {
				rowEdit.setError("Only support row number larger than 1 and less than 17");
				return;
			}

			text = colEdit.getText().toString();
			if (text == null || text.length() == 0) {
				colEdit.setError("Please input row number");
				return;
			}
			int col = Integer.parseInt(text);
			if (col < 1 || col > 16) {
				rowEdit.setError("Only support row number larger than 1 and less than 17");
				return;
			}

			gameTable.create(row, col);
			initTableLayout.setVisibility(View.GONE);
			radioGroup.setVisibility(View.VISIBLE);

			newGameData();
			gameData.setTableName(tableNameEdit.getText().toString());
			gameData.setTableDatas(gameTable.getTableDatas());

			randomController.setData(gameTable.getTableDatas());
			roundTextView.setText("Round " + randomController.getRound());

			actionBar.setTitle(gameData.getTableName());
		}
		else if (view == coverView1) {
			SOrderChooserUpdate chooser = new SOrderChooserUpdate(this, new OnOrderSelectListener() {

				@Override
				public void onSelect(SOrder order) {
					int width = getResources().getDimensionPixelSize(R.dimen.rgame_image_thread_width);
					Bitmap bitmap = PictureManagerUpdate.getInstance().createImage(order.getCoverPath()
							, width * width, GameActivity.this, 0);
					order1 = order;
					coverView1.setImageBitmap(bitmap);
					coverView1.setScaleType(ScaleType.FIT_XY);
				}
			});
			chooser.setTitle(getResources().getString(R.string.rgame_select_order));
			chooser.show();
		}
		else if (view == coverView2) {
			SOrderChooserUpdate chooser = new SOrderChooserUpdate(this, new OnOrderSelectListener() {

				@Override
				public void onSelect(SOrder order) {
					order2 = order;
					int width = getResources().getDimensionPixelSize(R.dimen.rgame_image_thread_width);
					Bitmap bitmap = PictureManagerUpdate.getInstance().createImage(order.getCoverPath()
							, width * width, GameActivity.this, 0);
					coverView2.setImageBitmap(bitmap);
					coverView2.setScaleType(ScaleType.FIT_XY);
				}
			});
			chooser.setTitle(getResources().getString(R.string.rgame_select_order));
			chooser.show();
		}
		else if (view == startButton1) {
			if (order1 == null) {
				return;
			}
			if (order1.getImgPathList() == null) {
				SOrderPictureBridge.getInstance(this).getOrderItemList(order1);
			}
			clickStart(startButton1, coverView1, order1, gameController1);
		}
		else if (view == startButton2) {
			if (order2 == null) {
				return;
			}
			if (order2.getImgPathList() == null) {
				SOrderPictureBridge.getInstance(this).getOrderItemList(order2);
			}
			clickStart(startButton2, coverView2, order2, gameController2);
		}
		else if (view == restartButton1) {
			if (isRandoming()) {
				stopRandoming(coverView1, order1, gameController1);
			}
			clickRestartButton(startButton1, gameController1);
		}
		else if (view == restartButton2) {
			if (isRandoming()) {
				stopRandoming(coverView2, order2, gameController2);
			}
			clickRestartButton(startButton2, gameController2);
		}
		else if (view == randomPreView) {
			TableData data = randomController.previous();
			highLightData = data;
			if (data != null) {
				updateCover(coverView1, gameTable.getTableDatas()[data.getRow()][0]);
				updateCover(coverView2, gameTable.getTableDatas()[0][data.getCol()]);
				gameTable.setCellHightLight(data.getRow(), data.getCol());
			}
		}
		else if (view == randomNextView) {
			roundTextView.setText("Round " + randomController.getRound());
			TableData data = randomController.next();
			highLightData = data;
			if (data != null) {
				updateCover(coverView1, gameTable.getTableDatas()[data.getRow()][0]);
				updateCover(coverView2, gameTable.getTableDatas()[0][data.getCol()]);
				gameTable.setCellHightLight(data.getRow(), data.getCol());
			}
		}
	}

	private void newGameData() {
		gameData = new GameData();
		gameData.setRandomController(randomController);
	}

	private void clickRestartButton(TextView startButton, GameController targetController) {
		startButton.setText(getResources().getString(R.string.rgame_start));
		targetController.closeOneCircle();
		targetController.cancelRegist();
		restoreLeftNumber();
	}

	private void clickStart(TextView startButton, ImageView targetView
			, SOrder targetOrder, GameController targetController) {
		if (randomRules == null) {
			startButton.setText(getResources().getString(R.string.rgame_start));
			Toast.makeText(this, R.string.rgame_set_rule_first, Toast.LENGTH_LONG).show();
			return;
		}
		if (randomRules.isImgThreadOn()) {
			if (startButton.getText().equals(getResources().getString(R.string.rgame_start))) {
				startButton.setText(getResources().getString(R.string.rgame_stop));
				startRandoming();
				new RandomThread(targetView, targetOrder, targetController).start();
			}
			else if (startButton.getText().equals(getResources().getString(R.string.rgame_stop))){
				stopRandoming(targetView, targetOrder, targetController);
				if (randomRules.getNumber() > 1 && curLeftNumber > 0) {
					startButton.setText(getResources().getString(R.string.rgame_continue));
				}
				else {
					startButton.setText(getResources().getString(R.string.rgame_start));
				}
				if (curLeftNumber == 0) {
					restoreLeftNumber();
				}

				if (startButton == startButton1) {
					updateTableHead(randomPath1, randomBitmap1);
				}
				else {
					updateTableHead(randomPath2, randomBitmap2);
				}
			}
			else if (startButton.getText().equals(getResources().getString(R.string.rgame_continue))) {
				startButton.setText(getResources().getString(R.string.rgame_stop));
				startRandoming();
				new RandomThread(targetView, targetOrder, targetController).start();
			}
		}
		else {
			if (!targetController.hasRegistedRules()) {
				targetController.registRule(randomRules);
				targetController.registRange(0, targetOrder.getItemNumber() - 1);
				targetController.startOneCircle();
			}
			stopRandoming(targetView, targetOrder, targetController);
		}
	}

	private void restoreLeftNumber() {
		if (randomRules != null) {
			curLeftNumber = randomRules.getNumber();
		}
	}

	private void reduceLeftNumber() {
		curLeftNumber --;
	}

	private boolean isRandoming() {
		return isRandoming;
	}

	private void startRandoming() {
		isRandoming = true;
	}

	private void stopRandoming(ImageView targetView, SOrder targetOrder, GameController targetController) {
		isRandoming = false;
		int index = targetController.randomSelect();

		String path = null;
		Bitmap bitmap = null;

		if (index == -1) {
			Toast.makeText(GameActivity.this, R.string.rgame_no_random_item, Toast.LENGTH_LONG).show();
		}
		else {
			try {
				path = targetOrder.getImgPathList().get(index);
				bitmap = PictureManagerUpdate.getInstance().getSpictureItem(path, GameActivity.this);
				targetView.setImageBitmap(bitmap);
			} catch (Exception e) {
				Toast.makeText(GameActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
			}

		}

		if (targetController == gameController1) {
			randomPath1 = path;
			randomBitmap1 = bitmap;
		}
		else {
			randomPath2 = path;
			randomBitmap2 = bitmap;
		}
	}

	private class RandomThread extends Thread implements Callback {

		private ImageView targetView;
		private SOrder targetOrder;
		private GameController targetController;

		public RandomThread(ImageView targetView, SOrder order, GameController controller) {
			this.targetView = targetView;
			targetOrder = order;
			targetController = controller;
		}

		private Handler handler = new Handler(this);
		public void run() {
			while (isRandoming()) {
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

			if (!targetController.hasRegistedRules()) {
				targetController.registRule(randomRules);
				targetController.registRange(0, targetOrder.getItemNumber() - 1);
				targetController.startOneCircle();
			}
			int index = targetController.randomProcessing();
			if (index == -1) {
				stopRandoming(targetView, targetOrder, targetController);
			}
			else {
				try {
					String path = targetOrder.getImgPathList().get(index);
					Bitmap bitmap = PictureManagerUpdate.getInstance().getSpictureItem(path, GameActivity.this);
					targetView.setImageBitmap(bitmap);
				} catch (Exception e) {
					Toast.makeText(GameActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
				}
			}
			reduceLeftNumber();

			return true;
		}

	}

	@Override
	public void onResume() {
		if (isRandoming()) {
			isRandoming = false;
		}
		super.onResume();
	}

	public void updateTableHead(String path, Bitmap bitmap) {
		gameTable.updateHead(path, bitmap);
	}

	@Override
	public void onBack() {
		onBackPressed();
	}

	@Override
	public void onDelete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onIconClick(View view) {
		switch (view.getId()) {
			case R.id.actionbar_save1:
				if (gameData != null) {
					if (gameData.isNewFile()) {
						saveGameDataAs();
					}
					else {
						new GameDataController().saveGameData(gameData);
						gameData.setFlag(GameData.FLAG_LOADED);
					}
				}
				break;
			case R.id.actionbar_show:
				if (randomLayout1.getVisibility() == View.VISIBLE) {
					randomLayout1.setVisibility(View.GONE);
					randomLayout2.setVisibility(View.GONE);
				}
				else {
					randomLayout1.setVisibility(View.VISIBLE);
					randomLayout2.setVisibility(View.VISIBLE);
				}
				break;

			default:
				break;
		}
	}

	private void saveGameDataAs() {
		if (gameData != null) {
			new DefaultDialogManager().openSaveFileDialog(this
					, new DefaultDialogManager.OnDialogActionListener() {

						@Override
						public void onOk(String name) {
							gameData.setFileName(Configuration.APP_DIR_GAME + "/" + name);
							new GameDataController().saveGameData(gameData);
							gameData.setFlag(GameData.FLAG_LOADED);
						}
					}, ".jg");
		}
	}

	@Override
	public void createMenu(MenuInflater menuInflater, Menu menu) {
		onPrepareMenu(menuInflater, menu);
	}

	@Override
	public void onPrepareMenu(MenuInflater menuInflater, Menu menu) {
		menu.clear();
		menuInflater.inflate(R.menu.random_game, menu);
	}

	@Override
	public OnMenuItemClickListener getMenuItemListener() {

		return this;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_random_rule:
				openSetRuleDialog();
				break;

			case R.id.menu_random_new:
				gameData = null;
				highLightData = null;
				gameTable.removeAllViews();
				initTableLayout.setVisibility(View.VISIBLE);
				radioGroup.setVisibility(View.GONE);
				break;
			case R.id.menu_random_load:
				openLoadFromDialog();
				break;
			case R.id.menu_random_saveas:
				saveGameDataAs();
				break;
			default:
				break;
		}
		return false;
	}

	private void openLoadFromDialog() {
		new LoadFromDialog(this, LoadFromDialog.DATA_GAME, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				if (object == null) {
					return false;
				}

				File file = (File) object;
				initTableLayout.setVisibility(View.GONE);
				radioGroup.setVisibility(View.VISIBLE);
				if (gameData == null) {
					newGameData();
				}
				gameData.setFlag(GameData.FLAG_LOADED);
				gameData.setFileName(file.getPath());

				new GameDataController().readGameData(gameData);
				for (int i = 0; i < gameData.getTableDatas()[0].length - 1; i ++) {
					String path = gameData.getTableDatas()[0][i].getImagePath();
					if (path != null) {
						Bitmap bitmap = PictureManagerUpdate.getInstance()
								.getSpictureItem(path, GameActivity.this);
						gameData.getTableDatas()[0][i].setBitmap(bitmap);
					}
				}
				for (int i = 0; i < gameData.getTableDatas().length - 1; i ++) {
					String path = gameData.getTableDatas()[i][0].getImagePath();
					if (path != null) {
						Bitmap bitmap = PictureManagerUpdate.getInstance()
								.getSpictureItem(path, GameActivity.this);
						gameData.getTableDatas()[i][0].setBitmap(bitmap);
					}
				}
				gameTable.createFrom(gameData.getTableDatas());

				roundTextView.setText("Round " + randomController.getRound());
				actionBar.setTitle(gameData.getTableName());
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

	public void openSetRuleDialog() {
		new GameRuleDialog(this, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {

				if (object != null) {
					randomRules = (RandomRules) object;
					curLeftNumber = randomRules.getNumber();
					Toast.makeText(GameActivity.this, R.string.rgame_rule_setok, Toast.LENGTH_LONG).show();
				}
				return true;
			}

			@Override
			public void onLoadData(HashMap<String, Object> data) {
				data.put("data", randomRules);
			}

			@Override
			public boolean onCancel() {
				return false;
			}
		}).show();
	}


	@Override
	public void onTextChanged(String text, int start, int before, int count) {

	}

	@Override
	public void onHeadClick(TableData data) {
		if (editRadioButton.isChecked()) {
			if (headClickListener == null) {
				headClickListener = new HeadClickListener(data);
			}
			else {
				headClickListener.updateTableData(data);
			}

			if (headEditPopupWindow == null) {
				headEditPopupWindow = new ListPopupWindow(this);
				headEditPopupWindow.setWidth(400);
				String[] menuItems = getResources().getStringArray(R.array.game_edit_drop);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this
						, android.R.layout.simple_list_item_1, menuItems);
				headEditPopupWindow.setAdapter(adapter);
				headEditPopupWindow.setOnItemClickListener(headClickListener);
			}
			headEditPopupWindow.setAnchorView(data.getView());
			headEditPopupWindow.show();
			headEditPopupWindow.getListView().setDivider(null);

		}

		if (data.getRow() == 0) {
			updateCover(coverView2, data);
		}
		else {
			updateCover(coverView1, data);
		}
	}

	private class HeadClickListener implements OnItemClickListener {

		private TableData tableData;
		public HeadClickListener(TableData data) {
			tableData = data;
		}
		public void updateTableData(TableData data) {
			tableData = data;
		}
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {
			if (position == 0) {//folder
				Intent intent = new Intent();
				intent.setClass(GameActivity.this, ThumbFolderActivity.class);
				Bundle bundle = new Bundle();
				bundle.putBoolean(Constants.KEY_THUMBFOLDER_CHOOSER_MODE, true);
				bundle.putInt(Constants.KEY_THUMBFOLDER_INIT_MODE, ThumbFolderActivity.SRC_MODE_FOLDER);
				intent.putExtras(bundle);
				startActivityForResult(intent, HEAD_REQUEST_CODE);
			}
			else if (position == 1) {//order
				Intent intent = new Intent();
				intent.setClass(GameActivity.this, ThumbFolderActivity.class);
				Bundle bundle = new Bundle();
				bundle.putBoolean(Constants.KEY_THUMBFOLDER_CHOOSER_MODE, true);
				bundle.putInt(Constants.KEY_THUMBFOLDER_INIT_MODE, ThumbFolderActivity.SRC_MODE_ORDER);
				intent.putExtras(bundle);
				startActivityForResult(intent, HEAD_REQUEST_CODE);
			}
			else if (position == 2) {//add to order
				if (tableData.getImagePath() != null) {
					SOrderChooserUpdate chooser = new SOrderChooserUpdate(GameActivity.this, new CustomDialog.OnCustomDialogActionListener() {

						@Override
						public boolean onSave(Object object) {

							if (object != null) {
								SOrder order = (SOrder) object;
								SOrderPictureBridge.getInstance(GameActivity.this).addToOrder(tableData.getImagePath(), order.getId());
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
				updateTableHead(null, null);
			}
			headEditPopupWindow.dismiss();
		}
	}

	private void updateCover(ImageView coverView, TableData data) {
		if (data.getImagePath() != null) {
			coverView.setScaleType(ScaleType.FIT_XY);
			if (coverView == coverView1) {
				randomBitmap1 = PictureManagerUpdate.getInstance().getSpictureItem(data.getImagePath(), this);
				coverView.setImageBitmap(randomBitmap1);
			}
			else {
				randomBitmap2 = PictureManagerUpdate.getInstance().getSpictureItem(data.getImagePath(), this);
				coverView.setImageBitmap(randomBitmap2);
			}
		}
	}

	@Override
	public void onCellClick(TableData data) {
		new CellDataDialog(this, data, this).show();
	}

	@Override
	public void onCountClick(TableData data) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == HEAD_REQUEST_CODE) {
			if (data != null) {
				String path = data.getStringExtra(Constants.KEY_THUMBFOLDER_CHOOSE_CONTENT);
				if (path != null) {
					Bitmap bitmap = PictureManagerUpdate.getInstance().getSpictureItem(path, this);
					updateTableHead(path, bitmap);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCellDataChange(TableData data) {
		TextView textView = (TextView) data.getView();
		textView.setText("" + data.getScore());
		gameTable.notifyCellDataChanged(data.getRow(), data.getCol());
	}

	@Override
	public boolean onLongClick(View view) {
		if (view == coverView1) {
			if (order1 != null) {
				new PreviewDialog(this, order1).show();
			}
		}
		else if (view == coverView2) {
			if (order2 != null) {
				new PreviewDialog(this, order2).show();
			}
		}
		return true;
	}

}
