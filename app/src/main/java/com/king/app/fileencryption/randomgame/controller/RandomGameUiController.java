package com.king.app.fileencryption.randomgame.controller;

import java.util.HashMap;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.randomgame.RandomRules;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.view.SOrderChooserUpdate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

@Deprecated
public class RandomGameUiController implements OnLongClickListener {

	private Context context;
	private RandomRules randomRules;
	
	private class ImageViewTag {
		SOrder sorder;
		int index;
		public ImageViewTag(SOrder order, int index) {
			sorder = order;
			this.index = index;
		}
	}
	public RandomGameUiController(Context context) {
		this.context = context;
	}
	
	public void registRule(RandomRules rules) {
		randomRules = rules;
	}
	
	public void cancelRegistRule() {
		randomRules = null;
	}
	
	public boolean hasRegistedRule() {
		return randomRules != null;
	}
	
	public LinearLayout followbyResult(LinearLayout layout, SOrder order, int index) {

		Bitmap bitmap = null;
		try {
			bitmap = PictureManagerUpdate.getInstance().getSpictureItem(order.getImgPathList().get(index), context);
		} catch (Exception e) {
			Toast.makeText(context, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
		}

		LinearLayout itemLayout = null;
		ImageView view = new ImageView(context);
		view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		view.setImageBitmap(bitmap);
		view.setTag(new ImageViewTag(order, index));
		view.setOnLongClickListener(this);
		
		if (randomRules != null && randomRules.getNumber() > 1) {
			itemLayout = new LinearLayout(context);
			itemLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			itemLayout.setOrientation(LinearLayout.HORIZONTAL);
			itemLayout.addView(view);
			layout.addView(itemLayout);
		}
		else {
			layout.addView(view);
		}
		return itemLayout;
	}

	public void followbyCurrentItem(LinearLayout layout, SOrder order, int index) {

		Bitmap bitmap = null;
		try {
			bitmap = PictureManagerUpdate.getInstance().getSpictureItem(order.getImgPathList().get(index), context);
		} catch (Exception e) {
			Toast.makeText(context, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
		}

		ImageView view = new ImageView(context);
		view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		view.setImageBitmap(bitmap);
		view.setTag(new ImageViewTag(order, index));
		view.setOnLongClickListener(this);
		layout.addView(view);
	}

	public void followbyCurrentItem(LinearLayout layout, String scene) {
		TextView view = new TextView(context);
		view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		view.setText(scene);
		view.setTextColor(Color.WHITE);
		layout.addView(view);
	}

	public LinearLayout replaceResult(LinearLayout layout, SOrder order, int index) {

		LinearLayout itemLayout = null;
		layout.removeAllViews();
		itemLayout = followbyResult(layout, order, index);
		return itemLayout;
	}

	@Override
	public boolean onLongClick(View view) {
		final ImageViewTag tag = (ImageViewTag) view.getTag();
		SOrderChooserUpdate chooser = new SOrderChooserUpdate(context, new CustomDialog.OnCustomDialogActionListener() {
			
			@Override
			public boolean onSave(Object object) {
				if (object != null) {
					SOrder order = (SOrder) object;
					SOrderPictureBridge bridge = SOrderPictureBridge.getInstance(context);
					String path = tag.sorder.getImgPathList().get(tag.index);
					if (bridge.addToOrder(path, order.getId())) {
						Toast.makeText(context, context.getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(context, context.getResources().getString(R.string.error_app_root_create_fail), Toast.LENGTH_SHORT).show();
					}
				}
				return false;
			}
			
			@Override
			public void onLoadData(HashMap<String, Object> data) {
				
			}
			
			@Override
			public boolean onCancel() {
				return false;
			}
		});
		chooser.setTitle(context.getResources().getString(R.string.add_to_order));
		chooser.show();
		return true;
	}

	
}
