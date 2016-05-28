package com.king.app.fileencryption.randomgame.team;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TeamGameController {

	private boolean hasBuild;
	private boolean repeatable;
	private List<Integer> originList;
	private List<Integer> list;
	private Random random;
	
	public TeamGameController() {
		random = new Random();
	}
	
	public void setRange(int start, int end) {
		if (end - start <= 0) {
			return;
		}
		else {
			originList = new ArrayList<Integer>(end - start + 1);
			list = new ArrayList<Integer>(end - start + 1);
			for (int i = start; i <= end; i ++) {
				originList.add(i);
				list.add(i);
			}
		}
	}
	
	public void setRepeatable(boolean repeat) {
		repeatable = repeat;
	}
	
	public boolean build() {
		if (originList == null) {
			return false;
		}
		hasBuild = true;
		return true;
	}
	
	public void reset() {
		if (originList != null) {
			for (int item:originList) {
				list.add(item);
			}
		}
	}

	/**
	 * happened at random process
	 * @return
	 */
	public int randomProcessing() {
		if (!hasBuild || list == null || list.size() == 0) {
			return -1;
		}
		int offset = Math.abs(random.nextInt()) % list.size();
		int index = list.get(offset);
		return index;
	}

	/**
	 * happened after random process finished
	 * @return
	 */
	public int randomSelect() {
		if (!hasBuild || list == null || list.size() == 0) {
			return -1;
		}
		int offset = Math.abs(random.nextInt()) % list.size();
		int index = list.get(offset);
		if (!repeatable) {
			list.remove(offset);
		}
		return index;
	}
	
}
