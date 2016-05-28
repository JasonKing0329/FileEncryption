package com.king.app.fileencryption.waterfall;

import android.graphics.Bitmap;

public interface ImageProvider {
	public Bitmap loadImage(String filePath);
}
