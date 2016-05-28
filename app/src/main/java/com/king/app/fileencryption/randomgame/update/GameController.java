package com.king.app.fileencryption.randomgame.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.king.app.fileencryption.randomgame.RandomRules;

public class GameController {

	private List<Integer> historyList;
	private List<String> sceneList;
	private RandomRules randomRules;
	private int min, max;
	private Random random;
	
	public void registRule(RandomRules rules) {
		randomRules = rules;
	}
	
	public boolean hasRegistedRules() {
		return randomRules != null;
	}
	
	public void registRange(int min, int max) {
		this.max = max;
		this.min = min;
	}
	
	public void startOneCircle() {
		if (randomRules != null) {
			random = new Random();
			if (!randomRules.isRepeatable()) {
				if (historyList == null) {
					historyList = new ArrayList<Integer>();
				}
				else {
					historyList.clear();
				}
				for (int i = min; i <= max; i ++) {
					historyList.add(i);
				}
				initSceneList();
			}
		}
	}
	
	public void closeOneCircle() {
		if (randomRules != null) {
			if (!randomRules.isRepeatable()) {
				if (historyList != null) {
					historyList.clear();
				}
				if (sceneList != null) {
					sceneList.clear();
				}
			}
			random = null;
		}
	}
	
	public void cancelRegist() {
		randomRules = null;
	}
	
	public int randomProcessing() {
		int index = -1;
		if (randomRules != null) {
			if (random == null) {
				random = new Random();
			}
			if (randomRules.isRepeatable()) {
				index = min + Math.abs(random.nextInt()) % (max - min + 1);
			}
			else {
				if (historyList.size() > 0) {
					index = Math.abs(random.nextInt()) % historyList.size();
				}
			}
		}
		return index;
	}
	
	public int randomSelect() {
		int index = -1;
		if (randomRules != null) {
			index = randomProcessing();
			if (!randomRules.isRepeatable()) {
				if (index > -1 && index < historyList.size()) {
					index = historyList.remove(index);
				}
			}
		}
		return index;
	}

	public void initSceneList() {
		String datas[] = new String[] {"kitchen", "bed", "sofa", "washroom", "gym", "swimpool", "boat", "hotel", "ship", "gard", "bar"};
		if (sceneList == null) {
			sceneList = new ArrayList<String>();
		}
		else {
			sceneList.clear();
		}
		for (String data:datas) {
			sceneList.add(data);
		}
	}
	
	public String getRandomScene() {
		String scene = null;
		if (randomRules != null) {
			if (sceneList == null || sceneList.size() == 0) {
				initSceneList();
			}
			if (sceneList.size() > 0) {
				int index = Math.abs(random.nextInt()) % sceneList.size();
				scene = sceneList.get(index);
				if (!randomRules.isRepeatable()) {
					sceneList.remove(index);
				}
			}
		}
		return scene;
	}
}
