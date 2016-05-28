package com.king.app.fileencryption.randomgame.update;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameDataController {

	public void saveGameData(GameData gameData) {
		if (gameData == null) {
			return;
		}
		
		FileOutputStream stream = null;
		ObjectOutputStream oout = null;
		try {
			stream = new FileOutputStream(gameData.getFileName());
			oout = new ObjectOutputStream(stream);

			oout.writeUTF(gameData.getTableName());
			TableData[][] datas = gameData.getTableDatas();
			oout.writeInt(datas.length);
			oout.writeInt(datas[0].length);
			for (int i = 0; i < datas.length; i ++) {
				for (int j = 0; j < datas.length; j ++) {
					oout.writeObject(datas[i][j].toIOData());
				}
			}
			gameData.getRandomController().saveData(oout);
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (oout != null) {
				try {
					oout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void readGameData(GameData gameData) {
		
		FileInputStream stream = null;
		ObjectInputStream oin = null;
		try {
			stream = new FileInputStream(gameData.getFileName());
			oin = new ObjectInputStream(stream);

			gameData.setTableName(oin.readUTF());
			int row = oin.readInt();
			int col = oin.readInt();
			if (row > 0) {
				TableData[][] datas = new TableData[row][col];
				for (int i = 0; i < datas.length; i ++) {
					for (int j = 0; j < datas.length; j ++) {
						TableIOData ioData = (TableIOData) oin.readObject();
						TableData data = new TableData(ioData);
						datas[i][j] = data;
					}
				}
				gameData.setTableDatas(datas);
				gameData.getRandomController().setDataFromFile(datas);
				gameData.getRandomController().readData(oin);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (oin != null) {
				try {
					oin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
