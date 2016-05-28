package com.king.app.fileencryption.publicview;

import java.io.File;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.view.SOrderChooser;
import com.king.app.fileencryption.spicture.controller.SpictureController;
import com.king.app.fileencryption.tool.SimpleEncrypter;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Toast;

@Deprecated
public class FlingGalleryActivity extends Activity {

	private FlingGallery mGallery;
	private SOrderPictureBridge bridge;
	private SpictureController spictureController;
	public interface OnGalleryActionListener extends OnLongClickListener {
		//public void onLongClick(String filename);
	}

	private OnGalleryActionListener actionListener = new OnGalleryActionListener() {

		@Override
		public boolean onLongClick(View view) {

			if (view.getTag() == null) {
				return true;
			}
			final String filePath = (String) view.getTag();
			if (!new File(filePath).exists()) {
				return true;
			}
			SOrderChooser chooser = new SOrderChooser(FlingGalleryActivity.this, new SOrderChooser.OnOrderChooseListener() {
				
				@Override
				public void chooseOrder(final SOrder order) {
					if (spictureController.isItemExist(filePath, order.getId())) {
						String title = getResources().getString(R.string.spicture_myorders_item_exist);
						title = String.format(title, order.getName());
						new AlertDialog.Builder(FlingGalleryActivity.this)
							.setMessage(title)
							.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									addToOrder(filePath, order);
								}
							})
							.setNegativeButton(R.string.cancel, null)
							.show();
					}
					else {
						addToOrder(filePath, order);
					}
				}
			});
			chooser.setTitleCustom(getResources().getString(R.string.add_to_order));
			chooser.show();
			return true;
		}
//		@Override
//		public void onLongClick(final String filename) {
//		}

	};

	private void addToOrder(String path, SOrder order) {
		String msg = null;
		if (spictureController.addItemToOrder(path, order)) {
			msg = getResources().getString(R.string.spicture_myorders_add_ok);
		}
		else {
			msg = getResources().getString(R.string.spicture_myorders_add_fail);
		}
		if (order.getName() != null) {
			msg = msg.replace("%s", order.getName());
		}
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGallery.onGalleryTouchEvent(event);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spicture_new_win);

		bridge = SOrderPictureBridge.getInstance(this);
		spictureController = new SpictureController(this);
		
		mGallery = (FlingGallery) findViewById(R.id.new_win_flinggallery);
		mGallery.setPaddingWidth(5);
		mGallery.setAdapter(new ImageWindowAdapter(this, new SimpleEncrypter(), actionListener));
		mGallery.setIsGalleryCircular(true);
		mGallery.setSnapBorderRatio(0.9f);

	}

	@Override
	protected void onPause() {
		finish();
		super.onPause();
	}

	@Override
	protected void onStop() {
		finish();
		super.onStop();
	}

}
