package com.king.app.fileencryption.randomgame.team;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class IOController {

	public void saveTableData(TeamGameData gameData) {
		try {
			FileOutputStream stream = new FileOutputStream(gameData.getFileName());
			ObjectOutputStream oout = new ObjectOutputStream(stream);
			oout.writeInt(gameData.getTitles().length);
			String path = null;
			for (int i = 0; i < gameData.getTitles().length; i ++) {
				path = gameData.getTitles()[i];
				if (path == null) {
					oout.writeUTF("-1");
				}
				else {
					oout.writeUTF(path);
				}
			}
			
			oout.writeInt(gameData.getDatas().length);
			oout.writeInt(gameData.getDatas()[0].length);
			for (int i = 0; i < gameData.getDatas().length; i ++) {
				for (int j = 0; j < gameData.getDatas()[i].length; j ++) {
					path = gameData.getDatas()[i][j];
					if (path == null) {
						oout.writeUTF("-1");
					}
					else {
						oout.writeUTF(path);
					}
				}
			}
			oout.close();
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TeamGameData readTableData(String filePath) {
		TeamGameData gameData = null;
		try {
			FileInputStream stream = new FileInputStream(filePath);
			ObjectInputStream oin = new ObjectInputStream(stream);
			
			int size = oin.readInt();
			if (size > 0) {
				gameData = new TeamGameData();
				gameData.setTitles(new String[size]);
			}
			String path = null;
			for (int i = 0; i < size; i ++) {
				path = oin.readUTF();
				if (path.equals("-1")) {
					gameData.getTitles()[i] = null;
				}
				else {
					gameData.getTitles()[i] = path;
				}
			}
			
			int row = oin.readInt();
			if (row > 0) {
				int col = oin.readInt();
				String[][] datas = new String[row][col];
				for (int i = 0; i < row; i ++) {
					for (int j = 0; j < col; j ++) {
						path = oin.readUTF();
						if (path.equals("-1")) {
							datas[i][j] = null;
						}
						else {
							datas[i][j] = path;
						}
					}
					
				}
				gameData.setDatas(datas);
			}
			
			oin.close();
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gameData;
	}
}
