package com.king.app.fileencryption.tool;

import java.io.File;
import java.util.List;

public interface Encrypter {

	/**
	 * encrypt specific file and delete the origin file after encrypted successfully
	 * @param src  original file
	 * @param targetNameNoExtra  target file name without file extra
	 */
	public String encrypt(File src, String targetNameNoExtra);
	
	/**
	 * restore the encrypted file to the original file
	 * @param target null if just decipher, otherwise, the target path to save as
	 */
	public boolean restore(File src, String target);
	
	/**
	 * encrypt file name
	 * @param origin  original file name
	 * @return encrypted file name
	 */
	public String encryptFileName(String origin);
	
	/**
	 * decipher file name
	 * @param name  encrypted file name
	 * @return deciphered file name
	 */
	public String decipherFileName(String name);
	
	/**
	 * encrypt source file as a copy to the target file, don't delete source file
	 * also invoked by encrypt(File src, String targetNameNoExtra)
	 * @param src
	 * @param target
	 */
	public boolean encrypt(File src, File target);
	
	/**
	 * decipher encrypted file as byte list, used for creating data stream
	 * also invoked by restore
	 * @param src  encrypted file with FILE_EXTRA extra
	 * @return file data list
	 */
	public List<Byte> decipher(File src);
	public byte[] decipherToByteArray(File src);
	
	/**
	 * write deciphered file data List<Byte> to file
	 * @param list  deciphered file data
	 * @param filePath  new file path with file name and extra
	 */
	public boolean saveDecipher(List<Byte> list, String filePath);
	
	public String getFileExtra();
	public String getNameExtra();
	
	public boolean isEncrypted(File file);
	
	/**
	 * 
	 * @param file  extra with FILE_EXTRA
	 * @return
	 */
	public String decipherOriginName(File file);
	
	public boolean isGifFile(String path);
}
