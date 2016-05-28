package com.king.app.fileencryption.randomgame.team;

import java.util.HashMap;

import android.content.Context;
import android.os.Handler.Callback;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.randomgame.update.GameData;
import com.king.app.fileencryption.randomgame.update.GameDataController;
import com.king.app.fileencryption.randomgame.update.RandomController;
import com.king.app.fileencryption.randomgame.update.TableData;

public class TurnDataDialog extends CustomDialog {

	private final String fileExtra = ".jg";
	private EditText title1Edit, title2Edit, title3Edit, title4Edit;
	private EditText file1Edit, file2Edit, file3Edit, file4Edit;
	private EditText[] checkEdit;

	private TeamGameData teamGameData;

	public TurnDataDialog(Context context,
						  OnCustomDialogActionListener actionListener) {
		super(context, actionListener);
		requestOkAction(true);
		requestCancelAction(true);
		applyGreyStyle();
		HashMap<String, Object> map = new HashMap<String, Object>();
		actionListener.onLoadData(map);
		teamGameData = (TeamGameData) map.get("data");
	}

	@Override
	protected View getCustomView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_turn_team_game_data, null);
		title1Edit = (EditText) view.findViewById(R.id.turn_data_title1);
		title2Edit = (EditText) view.findViewById(R.id.turn_data_title2);
		title3Edit = (EditText) view.findViewById(R.id.turn_data_title3);
		title4Edit = (EditText) view.findViewById(R.id.turn_data_title4);
		file1Edit = (EditText) view.findViewById(R.id.turn_data_file1);
		file2Edit = (EditText) view.findViewById(R.id.turn_data_file2);
		file3Edit = (EditText) view.findViewById(R.id.turn_data_file3);
		file4Edit = (EditText) view.findViewById(R.id.turn_data_file4);
		checkEdit = new EditText[] {
				title1Edit, title2Edit, title3Edit, title4Edit, file1Edit, file2Edit, file3Edit, file4Edit
		};
		return view;
	}

	@Override
	protected View getCustomToolbar() {
		return null;
	}

	@Override
	public void onClick(View view) {
		if (view == saveIcon) {
			for (int i = 0; i < checkEdit.length; i ++) {
				if (checkEdit[i].getText().toString().trim().length() == 0) {
					checkEdit[i].setError(
							getContext().getResources().getString(R.string.input_no_null));
					return;
				}
			}
			new TurnThread().start();
		}
		super.onClick(view);
	}

	private class TurnThread extends Thread implements Callback{

		private Handler handler = new Handler(this);

		@Override
		public void run() {
			GameDataController gameDataController = new GameDataController();
			//file 1
			GameData gameData = new GameData();
			gameData.setFileName(Configuration.APP_DIR_GAME + "/"
					+ file1Edit.getText().toString() + fileExtra);
			gameData.setTableName(title1Edit.getText().toString());
			int num = teamGameData.getDatas().length;
			TableData[][] datas = new TableData[num + 2][num + 2];
			for (int i = 1; i <= num; i ++) {
				TableData topData = new TableData();
				topData.setImagePath(teamGameData.getDatas()[i - 1][0]);
				topData.setRow(i);
				topData.setCol(0);
				TableData bottomData = new TableData();
				bottomData.setImagePath(teamGameData.getDatas()[i - 1][1]);
				bottomData.setRow(0);
				bottomData.setCol(i);
				datas[0][i] = bottomData;
				datas[i][0] = topData;
			}
			for (int i = 0; i < num + 2; i ++) {
				for (int j = 0; j < num + 2; j ++) {
					TableData data = datas[i][j];
					if (data == null) {
						data = new TableData();
						data.setRow(i);
						data.setCol(j);
						datas[i][j] = data;
					}
				}
			}
			//步骤均是按照GameData的IO规则执行的，缺一不可
			gameData.setTableDatas(datas);
			RandomController controller = new RandomController();
			controller.setData(datas);
			gameData.setRandomController(controller);
			gameDataController.saveGameData(gameData);
			//file 2
			gameData = new GameData();
			gameData.setFileName(Configuration.APP_DIR_GAME + "/"
					+ file2Edit.getText().toString() + fileExtra);
			gameData.setTableName(title2Edit.getText().toString());
			num = teamGameData.getDatas().length;
			datas = new TableData[num + 2][num + 2];
			for (int i = 1; i <= num; i ++) {
				TableData topData = new TableData();
				topData.setImagePath(teamGameData.getDatas()[i - 1][2]);
				topData.setRow(i);
				topData.setCol(0);
				TableData bottomData = new TableData();
				bottomData.setImagePath(teamGameData.getDatas()[i - 1][3]);
				bottomData.setRow(0);
				bottomData.setCol(i);
				datas[0][i] = bottomData;
				datas[i][0] = topData;
			}
			for (int i = 0; i < num + 2; i ++) {
				for (int j = 0; j < num + 2; j ++) {
					TableData data = datas[i][j];
					if (data == null) {
						data = new TableData();
						data.setRow(i);
						data.setCol(j);
						datas[i][j] = data;
					}
				}
			}
			gameData.setTableDatas(datas);
			controller = new RandomController();
			controller.setData(datas);
			gameData.setRandomController(controller);
			gameDataController.saveGameData(gameData);
			//file 3
			gameData = new GameData();
			gameData.setFileName(Configuration.APP_DIR_GAME + "/"
					+ file3Edit.getText().toString() + fileExtra);
			gameData.setTableName(title3Edit.getText().toString());
			num = teamGameData.getDatas().length;
			datas = new TableData[num + 2][num + 2];
			for (int i = 1; i <= num; i ++) {
				TableData topData = new TableData();
				topData.setImagePath(teamGameData.getDatas()[i - 1][4]);
				topData.setRow(i);
				topData.setCol(0);
				TableData bottomData = new TableData();
				bottomData.setImagePath(teamGameData.getDatas()[i - 1][5]);
				bottomData.setRow(0);
				bottomData.setCol(i);
				datas[0][i] = bottomData;
				datas[i][0] = topData;
			}
			for (int i = 0; i < num + 2; i ++) {
				for (int j = 0; j < num + 2; j ++) {
					TableData data = datas[i][j];
					if (data == null) {
						data = new TableData();
						data.setRow(i);
						data.setCol(j);
						datas[i][j] = data;
					}
				}
			}
			gameData.setTableDatas(datas);
			controller = new RandomController();
			controller.setData(datas);
			gameData.setRandomController(controller);
			gameDataController.saveGameData(gameData);
			//file 4
			gameData = new GameData();
			gameData.setFileName(Configuration.APP_DIR_GAME + "/"
					+ file4Edit.getText().toString() + fileExtra);
			gameData.setTableName(title4Edit.getText().toString());
			num = teamGameData.getDatas().length;
			datas = new TableData[num + 2][num + 2];
			for (int i = 1; i <= num; i ++) {
				TableData topData = new TableData();
				topData.setImagePath(teamGameData.getDatas()[i - 1][6]);
				topData.setRow(i);
				topData.setCol(0);
				TableData bottomData = new TableData();
				bottomData.setImagePath(teamGameData.getDatas()[i - 1][7]);
				bottomData.setRow(0);
				bottomData.setCol(i);
				datas[0][i] = bottomData;
				datas[i][0] = topData;
			}
			for (int i = 0; i < num + 2; i ++) {
				for (int j = 0; j < num + 2; j ++) {
					TableData data = datas[i][j];
					if (data == null) {
						data = new TableData();
						data.setRow(i);
						data.setCol(j);
						datas[i][j] = data;
					}
				}
			}
			gameData.setTableDatas(datas);
			controller = new RandomController();
			controller.setData(datas);
			gameData.setRandomController(controller);
			gameDataController.saveGameData(gameData);

			handler.sendEmptyMessage(0);
		}

		@Override
		public boolean handleMessage(Message msg) {
			actionListener.onSave(getContext().getResources().getString(R.string.team_game_turndata_success));
			return true;
		}

	}
}
