package com.king.app.fileencryption.controller;

import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.tool.Generater;
import com.king.app.fileencryption.tool.SimpleEncrypter;
import com.king.app.fileencryption.tool.SimpleNameGenerater;

public class EncrypterFactory {

	public static Encrypter create() {
		
		return new SimpleEncrypter();
	}
	
	public static Generater generater() {
		return new SimpleNameGenerater();
	}
}
