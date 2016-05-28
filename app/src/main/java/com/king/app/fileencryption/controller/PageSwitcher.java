package com.king.app.fileencryption.controller;

import android.os.Bundle;

import com.king.app.fileencryption.sorder.entity.SOrder;

public interface PageSwitcher {

	public void switchToPictureOrderView(SOrder order);
	public void openRandomGame(SOrder order);
	public void openWallGallery(Bundle bundle);
}
