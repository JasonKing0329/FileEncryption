package com.king.app.fileencryption.tool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.king.app.fileencryption.data.Constants;

import android.util.Log;

public class SimpleEncrypter implements Encrypter {

	public static final String TAG = "SimpleEncrypter";
	public static final String FILE_EXTRA = ".jfe";
	public static final String NAME_EXTRA = ".jne";
	@Override
	public boolean encrypt(File src, File target) {
		if (src == null || !src.exists() || src.isDirectory()) {
			Log.i(TAG, "encrypt -> src not availabe");
			return false;
		}
		try {
			FileInputStream in = new FileInputStream(src);
			FileOutputStream out = new FileOutputStream(target);
			byte[] datas = new byte[2048];
			int length = 0;
			while (length != -1) {
				length = in.read(datas);
				if (length != -1) {
					encryptDatas(datas, length);
					out.write(datas, 0, length);
				}
			}
			out.close();
			in.close();
			if (Constants.DEBUG) {
				Log.i(TAG, "encrypt jfe file -> success [src: " + src.getPath() + "][target: " + target + "]");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.i(TAG, "encrypt -> " + e.getMessage());
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			Log.i(TAG, "encrypt -> " + e.getMessage());
			return false;
		}
		return true;
	}

	private void encryptDatas(byte datas[], int length) {
		if (length > 20) {
			byte temp = 0;
			for (int i = 0; i < 10; i ++) {
				temp = datas[i];
				datas[i] = datas[length - 1 - i];
				datas[length - 1 - i] = temp;
			}
		}
	}

	@Override
	public List<Byte> decipher(File src) {
		if (src == null || !src.exists() || src.isDirectory()) {
			Log.i(TAG, "decipher -> src not availabe");
			return null;
		}
		List<Byte> list = null;
		try {
			FileInputStream in = new FileInputStream(src);
			int length = 0;
			byte datas[] = new byte[2048];
			while (length != -1) {
				if (list == null) {
					list = new ArrayList<Byte>();
				}
				length = in.read(datas);
				if (length != -1) {
					decipherDatas(datas, length);
					for (int i = 0; i < length; i ++) {
						list.add(datas[i]);
					}
				}
			}
			in.close();
			if (Constants.DEBUG) {
				Log.i(TAG, "decipher -> success [src: " + src.getPath() + "]");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.i(TAG, "decipher -> " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			Log.i(TAG, "decipher -> " + e.getMessage());
		}

		return list;
	}

	public byte[] decipherToByteArray(File src) {
		if (src == null || !src.exists() || src.isDirectory()) {
			Log.i(TAG, "decipher -> src not availabe");
			return null;
		}
		int totalSize = (int) src.length();
		byte[] datas = new byte[totalSize];
		try {
			FileInputStream in = new FileInputStream(src);
			int length = 0;
			int index = 0;
			while (length != -1) {
				length = totalSize - 2048*index;
				if (length < 2048) {
					in.read(datas, 2048*index, length);
					decipherDatas(datas, 2048*index, length);
					length = -1;
					break;
				}
				else {
					in.read(datas, 2048*index, 2048);
					decipherDatas(datas, 2048*index, 2048);
				}
				index ++;
			}
			in.close();
			if (Constants.DEBUG) {
				Log.i(TAG, "decipher -> success [src: " + src.getPath() + "]");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.i(TAG, "decipher -> " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			Log.i(TAG, "decipher -> " + e.getMessage());
		}

		return datas;
	}

	private void decipherDatas(byte datas[], int length) {
		if (length > 20) {
			byte temp = 0;
			for (int i = 0; i < 10; i ++) {
				temp = datas[i];
				datas[i] = datas[length - 1 - i];
				datas[length - 1 - i] = temp;
			}
		}
	}

	private void decipherDatas(byte datas[], int start, int length) {
		if (length > 20) {
			int end = start + length - 1;
			byte temp = 0;
			for (int i = 0; i < 10; i ++) {
				temp = datas[start + i];
				datas[start + i] = datas[end - i];
				datas[end - i] = temp;
			}
		}
	}

	@Override
	public boolean saveDecipher(List<Byte> list, String filePath) {
		try {
			FileOutputStream outputStream = new FileOutputStream(filePath);
			int index = 0;
			int start = 0;
			int realLength = 0;
			byte[] datas = new byte[2048];
			while (start < list.size()) {
				if (list.size() - start < 2048) {
					realLength = list.size() - start;
					for (int i = start; i < list.size(); i ++) {
						datas[i - start] = list.get(i);
					}
					outputStream.write(datas, 0, realLength);
					break;
				}
				else {
					for (int i = start; i < start + 2048; i ++) {
						datas[i - start] = list.get(i);
					}
					outputStream.write(datas, 0, 2048);
				}
				index ++;
				start = 2048 * index;
			}
			outputStream.close();
			if (Constants.DEBUG) {
				Log.i(TAG, "saveDecipher -> success [filePath: " + filePath + "]");

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.i(TAG, "saveDecipher -> " + e.getMessage());
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			Log.i(TAG, "saveDecipher -> " + e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public String encrypt(File src, String targetNameNoExtra) {
		if (src == null || !src.exists() || src.isDirectory()) {
			Log.i(TAG, "encrypt(File, String) -> src not availabe");
			return null;
		}
		boolean result = false;
		String targetFileName = targetNameNoExtra;
		String filePath = src.getParent() + "/" + targetFileName + FILE_EXTRA;
		result = encrypt(src, new File(filePath));
		if (result) {
			src.delete();
			try {
				String nameFilePath = src.getParent() + "/" + targetFileName + NAME_EXTRA;
				FileOutputStream stream = new FileOutputStream(nameFilePath);
				DataOutputStream dout = new DataOutputStream(stream);
				dout.writeUTF(encryptFileName(src.getName()));
				dout.close();
				stream.close();
				result = true;
				if (Constants.DEBUG) {
					Log.i(TAG, "encrypt(File, String) jne file -> success [filePath: " + nameFilePath + "]");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.i(TAG, "encrypt(File, String) -> " + e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				Log.i(TAG, "encrypt(File, String) -> " + e.getMessage());
			}
		}
		return filePath;
	}

	@Override
	public boolean restore(File src, String target) {
		if (src == null || !src.exists() || src.isDirectory()) {
			Log.i(TAG, "restore -> src not availabe");
			return false;
		}
		String fileName = src.getName();
		String filenames[] = fileName.split("\\.");
		List<Byte> list = decipher(src);
		if (list != null) {
			if (target == null) {// 只是decipher
				src.delete();
			}
			String targetFileName = null;;
			fileName = src.getParent() + "/" + filenames[0] + NAME_EXTRA;
			File file = new File(fileName);
			if (file.exists()) {
				try {
					FileInputStream stream = new FileInputStream(file);
					DataInputStream din = new DataInputStream(stream);
					try {
						targetFileName = decipherFileName(din.readUTF());
					} catch (IOException e) {
						targetFileName = System.currentTimeMillis() + ".data";
						e.printStackTrace();
					}
					din.close();
					stream.close();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				targetFileName = System.currentTimeMillis() + ".data";
			}

			if (target == null) {// 只是decipher
				file.delete();
				fileName = src.getParent() + "/" + targetFileName;
			}
			else {// decipher后保存到其他目录
				fileName = target + "/" + targetFileName;
			}

			saveDecipher(list, fileName);
			if (Constants.DEBUG) {
				Log.i(TAG, "restore -> success [filePath: " + src.getPath() + "]");
			}
		}
		return true;
	}
	@Override
	public String encryptFileName(String origin) {
		String result = origin;
		int length = origin.length();
		//byte originDatas[] = origin.getBytes();
		//byte resultDatas[] = new byte[length * 4];
		//Random random = new Random();
		byte datas[] = origin.getBytes();
		byte temp = 0;
		for (int i = 0; i < length/2; i ++) {
			temp = datas[i];
			datas[i] = datas[length - 1 - i];
			datas[length - 1 - i] = temp;
		}
		result = new String(datas);
		return result;
	}

	@Override
	public String decipherFileName(String name) {
		String result = name;
		int length = name.length();
		//byte originDatas[] = origin.getBytes();
		//byte resultDatas[] = new byte[length * 4];
		//Random random = new Random();
		byte datas[] = name.getBytes();
		byte temp = 0;
		for (int i = 0; i < length/2; i ++) {
			temp = datas[i];
			datas[i] = datas[length - 1 - i];
			datas[length - 1 - i] = temp;
		}
		result = new String(datas);
		return result;
	}

	@Override
	public String getFileExtra() {

		return FILE_EXTRA;
	}

	@Override
	public String getNameExtra() {

		return NAME_EXTRA;
	}

	@Override
	public boolean isEncrypted(File file) {

		return file.getName().endsWith(FILE_EXTRA);
	}

	@Override
	public String decipherOriginName(File file) {
		if (file == null) {
			return null;
		}
		String fileName = file.getName();
		String filenames[] = fileName.split("\\.");
		String targetFileName = null;
		fileName = file.getParent() + "/" + filenames[0] + NAME_EXTRA;
		if (file.exists()) {
			try {
				FileInputStream stream = new FileInputStream(fileName);
				DataInputStream din = new DataInputStream(stream);
				try {
					targetFileName = decipherFileName(din.readUTF());
				} catch (IOException e) {
					e.printStackTrace();
				}
				din.close();
				stream.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return targetFileName;
	}

	@Override
	public boolean isGifFile(String filePath) {
		if (filePath == null) {
			return false;
		}

		if (filePath.toLowerCase(Locale.CHINA).endsWith(".gif")) {
			return true;
		}

		if (isEncrypted(new File(filePath))) {
			String originName = decipherOriginName(new File(filePath));
			Log.i(TAG, "chooseImage -> originName = " + originName);
			if (originName != null && originName.toLowerCase(Locale.CHINA).endsWith(".gif")) {
				return true;
			}
		}
		return false;
	}

}
