package com.king.app.fileencryption.randomgame.update;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.Stack;

import android.util.Log;

public class RandomController {

	private final String TAG = "RandomController";

	private Stack<TableData> historyStack;
	private Stack<TableData> historyPopStack;

	private TableData[][] datas;
	private int[][] position;
	private int[][] positionTemp;
	private Random random;

	private int[] round;

	private int index;
	private int currentRound;

	public RandomController() {
		random = new Random();
		currentRound = 1;
	}

	public void setData(TableData[][] datas) {
		this.datas = datas;
		currentRound = 1;
		if (datas != null && datas[0] != null) {
			//减去统计行，保留head行用作统计剩余量
			position = new int[datas.length - 1][datas[0].length - 1];
			positionTemp = new int[datas.length - 1][datas[0].length - 1];

			historyStack = new Stack<TableData>();
			historyPopStack = new Stack<TableData>();

			round = new int[datas.length - 1];//减去统计行，下标从1开始有效，i即代表第i行

			index = 1;

			for (int j = 0; j < positionTemp.length; j ++) {
				position[j][0] = positionTemp[j].length - 1;
				positionTemp[j][0] = positionTemp[j].length - 1;
			}
		}
	}

	private void resetTempPosition() {
		for (int i = 1; i < positionTemp.length; i ++) {
			for (int j = 0; j < positionTemp[i].length; j ++) {
				positionTemp[i][j] = position[i][j];
			}
		}
	}

	/**
	 * 算法总结：
	 * 算法以行为移动向，以列为参考性
	 * 采用两个二维数组position和positionTemp记录当前的可访问区域
	 * position是实际上真正已访问过的位置阵列，即行列所对应的数据真正被选中过，则被置为已访问：1
	 * positionTemp是在当前round下，如果某个列标对应的数据被选中，那么其他行对应的该列数据也被置为已访问:1，并且该行所有的数据也置为1（这个没有实际作用，因为该算法是以列为参考项的，不过可以作为实际情况的标志）
	 *
	 * 用index表示当前应该进行随机选择的行
	 * 以行的顺序依次向下进行随机数选择。当选择行index所对应的列colIndex的数据后
	 * 将position[index][colIndex]置为1，positionTemp[index][all col]以及positionTemp[all row][colIndex]置为1
	 * 用positionTemp[0]记录该行剩余可访问列的数量
	 * 每一次的随机数范围就是通过positionTemp[0]来判断的
	 *
	 * 以上步骤完成后，下一步则是判断下一个进行随机的行，一般情况应该是顺序向下的，但是在以往手动的经验中可以感受到，这样到后面是可能会出现无法对应的情况
	 * round数组用来记录当前轮次（从1到Row总数）已参与随机的情况（比如第1行已随机完成，则round[1]置为1，第N行已随机完成，round[N]置为1）
	 * 这里采用以colIndex列为参考，从上往下依次判断还未被访问的行(position[row][colIndex]=0&&round[row]=0)，并且从这些行中选择剩余可访问列最小的行作为下一次的index
	 * 至于为什么选择剩余可访问列最小的行，这个是根据手动操作时的经验发现的。最少的因为可供选择的更少，所以先把他们安排好了，其他的就不容易出现无法对应的情况。
	 *
	 * 每一次随机完成后，将该位置tableData压入到历史栈中，以便previous操作
	 * @return
	 */
	public TableData next() {
		TableData tableData = null;

		if (historyPopStack.size() > 0) {
			/**
			 * 会先弹当前table data
			 */
			tableData = historyPopStack.pop();
		}
		else {
			int[] cols = positionTemp[index];
			int left = cols[0];

			if (left == 0) {
				return null;
			}

			int rindex = Math.abs(random.nextInt()) % left;
			int count = 0;
			int colIndex = 0;
			for (int i = 1; i < cols.length; i ++) {
				if (cols[i] != 1) {
					if (count == rindex) {
						colIndex = i;
						break;
					}
					count ++;
				}
			}

			tableData = datas[index][colIndex];
			position[index][colIndex] = 1;//position should be set as visited
			position[index][0] --;//count left number should decrease 1

			//for positionTemp, all data in index row and colIndex col should be set as visited
			for (int i = 1; i < positionTemp.length; i ++) {
				if (positionTemp[i][colIndex] == 0) {
					positionTemp[i][colIndex] = 1;
					positionTemp[i][0] --;//when colIndex was visited, all row count left number should decrease 1
				}
				positionTemp[index][i] = 1;
			}

			//remove in round
			round[index] = 1;

			//change index
			if (roundIsOver()) {
				index = 1;
				resetTempPosition();
			}
			else {
				boolean findNewIndex = false;
				//find colIndex other row and still in round
				//the row have less left number should be considered first
				int leftNumber = 9999;
				for (int i = 1; i < position.length; i ++) {
					int value = position[i][colIndex];//用实际的行做判断
					if (value == 0 && round[i] != 1) {
						if (positionTemp[i][0] < leftNumber) {//用temp行找下一个应该出现的行
							index = i;
							leftNumber = positionTemp[i][0];
						}
						findNewIndex = true;
					}
				}

				if (!findNewIndex) {
					for (int i = 1; i < round.length; i ++) {
						if (round[i] == 0) {
							index = i;
							findNewIndex = true;
							break;
						}
					}
					if (!findNewIndex) {
						Log.d(TAG, "all round is over");
					}
				}
			}

		}

		historyStack.push(tableData);
		return tableData;
	}

	private boolean roundIsOver() {
		for (int i = 1; i < round.length; i ++) {
			if (round[i] == 0) {
				return false;
			}
		}

		//reset round
		for (int i = 1; i < round.length; i ++) {
			round[i] = 0;
		}
		currentRound ++;
		return true;
	}

	/**
	 * 由于每一次next都将当前的table data push进入history stack中，所以每一次第一次调用previous会先弹出当前table data
	 * 这在view中体现为，调用previous时会先定格一下，然后再往history stack中弹
	 *
	 * 算法总结：
	 * 结合next操作
	 * 当从historyStack中弹出历史内容后，为避免重新执行next()的算法操作，破坏可能已经填入数据的table cell，引入historyPopStack
	 * 以该栈保存被弹出的历史内容，让next()在执行的时候先判断是否已弹过历史内容，如果弹过，则直接从该栈中取出下一个。直到取完才重新执行next()的算法
	 * @return
	 */
	public TableData previous() {

		TableData tableData = null;
		if (historyStack.size() > 0) {

			tableData = historyStack.pop();
			historyPopStack.push(tableData);
		}
		return tableData;
	}

	public void reset() {
		for (int i = 0; i < position.length; i ++) {
			for (int j = 0; j < datas[i].length; j ++) {
				position[i][j] = 0;
			}
		}
		historyStack.clear();
	}

	public int getRound() {
		return currentRound;
	}

	public void saveData(ObjectOutputStream oout) throws IOException {
		oout.writeInt(index);
		oout.writeInt(currentRound);

		oout.writeInt(round.length);
		for (int i = 0; i < round.length; i ++) {
			oout.writeInt(round[i]);
		}

		//只存行列值，避免再存对象
		oout.writeInt(historyStack.size());
		for (TableData data:historyStack) {
			oout.writeInt(data.getRow());
			oout.writeInt(data.getCol());
		}

		oout.writeInt(historyPopStack.size());
		for (TableData data:historyPopStack) {
			oout.writeInt(data.getRow());
			oout.writeInt(data.getCol());
		}

		oout.writeInt(position.length);
		for (int i = 0; i < position.length; i ++) {
			oout.writeInt(position[i].length);
			for (int j = 0; j < position.length; j ++) {
				oout.writeInt(position[i][j]);
			}
		}

		oout.writeInt(positionTemp.length);
		for (int i = 0; i < positionTemp.length; i ++) {
			oout.writeInt(positionTemp[i].length);
			for (int j = 0; j < positionTemp.length; j ++) {
				oout.writeInt(positionTemp[i][j]);
			}
		}

	}

	public void setDataFromFile(TableData[][] datas) {
		this.datas = datas;
	}

	/**
	 * must call setDataFromFile before call this
	 * cause it only save row/column value instead of TableData object for historyPopStack and historyPopStack in saveData method
	 * @param oin
	 * @throws IOException
	 */
	public void readData(ObjectInputStream oin) throws IOException {
		index = oin.readInt();
		currentRound = oin.readInt();

		int lengh = oin.readInt();
		round = new int[lengh];
		for (int i = 0; i < lengh; i ++) {
			round[i] = oin.readInt();
		}

		lengh = oin.readInt();
		TableData data = null;
		historyStack = new Stack<TableData>();
		for (int i = 0; i < lengh; i ++) {
			data = datas[oin.readInt()][oin.readInt()];
			historyStack.push(data);
		}

		lengh = oin.readInt();
		data = null;
		historyPopStack = new Stack<TableData>();
		for (int i = 0; i < lengh; i ++) {
			data = datas[oin.readInt()][oin.readInt()];
			historyPopStack.push(data);
		}

		lengh = oin.readInt();
		position = new int[lengh][];
		for (int i = 0; i < position.length; i ++) {
			int size = oin.readInt();
			position[i] = new int[size];
			for (int j = 0; j < position.length; j ++) {
				position[i][j] = oin.readInt();
			}
		}

		lengh = oin.readInt();
		positionTemp = new int[lengh][];
		for (int i = 0; i < positionTemp.length; i ++) {
			int size = oin.readInt();
			positionTemp[i] = new int[size];
			for (int j = 0; j < positionTemp.length; j ++) {
				positionTemp[i][j] = oin.readInt();
			}
		}
	}

}
