package com.king.app.fileencryption.tool;

import java.util.Random;

public class SimpleNameGenerater implements Generater {

	@Override
	public String generateName() {
		String string = System.currentTimeMillis() + "";
		Random random = new Random();
		char[] numbers = string.toCharArray();
		char[] symbols = new char[string.length()];
		char datas[] = new char[string.length() * 2];
		for (int i = 0; i < string.length(); i ++) {
			symbols[i] = (char) ('A' + Math.abs(random.nextInt())%26);
		}
		for (int i = 0, j = 1; i < string.length() * 2; i += 2, j += 2) {
			datas[i] = symbols[i/2];
			datas[j] = numbers[i/2];
		}
		return new String(datas);
	}

}
