package com.king.app.fileencryption.book;

import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.open.image.ImageValue;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class BookPage extends FrameLayout {

	public static final int ITEM_ONE = 1;
	public static final int ITEM_TWO = 2;
	public static final int ITEM_THREE = 3;
	public static final int ITEM_FOUR = 4;
	private static final String TAG = "BookPage";
	private List<Bitmap> imageList;
	private List<ImageValue> imagePathList;
	private OnClickListener imageListener;
	public BookPage(Context context) {
		super(context);
	}

	public BookPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BookPage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setItems(List<ImageValue> list) {
		this.imagePathList = list;
	}

	public void show() {
		if (imagePathList != null) {
			removeAllViews();

			int size = imagePathList.size();
			if (size == ITEM_ONE) {
				applyOneItem();
			}
			else if (size == ITEM_TWO) {
				applyTwoItem();
			}
			else if (size == ITEM_THREE) {
				applyThreeItem();
			}
			else if (size == ITEM_FOUR) {
				applyFourItem();
			}
			else {
				applyOneItem();
			}
		}
	}

	public void setOnImageClickListener(OnClickListener listener) {
		imageListener = listener;
	}

	private void applyFourItem() {
		imageList = BookHelper.getInstance().loadImage(imagePathList, getContext());
		View view = LayoutInflater.from(getContext()).inflate(R.layout.bookpage_4item, null);
		ImageView imageView1 = (ImageView) view.findViewById(R.id.bookpage_4item_image1);
		ImageView imageView2 = (ImageView) view.findViewById(R.id.bookpage_4item_image2);
		ImageView imageView3 = (ImageView) view.findViewById(R.id.bookpage_4item_image3);
		ImageView imageView4 = (ImageView) view.findViewById(R.id.bookpage_4item_image4);
		imageView1.setImageBitmap(imageList.get(0));
		imageView2.setImageBitmap(imageList.get(1));
		imageView3.setImageBitmap(imageList.get(2));
		imageView4.setImageBitmap(imageList.get(3));
		imageView1.setTag(imagePathList.get(0));
		imageView2.setTag(imagePathList.get(1));
		imageView3.setTag(imagePathList.get(2));
		imageView4.setTag(imagePathList.get(3));
		imageView1.setOnClickListener(imageListener);
		imageView2.setOnClickListener(imageListener);
		imageView3.setOnClickListener(imageListener);
		imageView4.setOnClickListener(imageListener);
		addView(view);
	}

	private void applyThreeItem() {
		imageList = BookHelper.getInstance().loadImage(imagePathList, getContext());

		View view = null;
		int mode = (Integer) imagePathList.get(0).getTag();
		switch (mode) {
			case 1:
				view = LayoutInflater.from(getContext()).inflate(R.layout.bookpage_3item_1, null);
				break;
			case 2:
				view = LayoutInflater.from(getContext()).inflate(R.layout.bookpage_3item_2, null);
				break;
			case 3:
				view = LayoutInflater.from(getContext()).inflate(R.layout.bookpage_3item_3, null);
				break;
		}
		ImageView imageView1 = (ImageView) view.findViewById(R.id.bookpage_3item_image1);
		ImageView imageView2 = (ImageView) view.findViewById(R.id.bookpage_3item_image2);
		ImageView imageView3 = (ImageView) view.findViewById(R.id.bookpage_3item_image3);
		imageView1.setImageBitmap(imageList.get(0));
		imageView2.setImageBitmap(imageList.get(1));
		imageView3.setImageBitmap(imageList.get(2));
		imageView1.setTag(imagePathList.get(0));
		imageView2.setTag(imagePathList.get(1));
		imageView3.setTag(imagePathList.get(2));
		imageView1.setOnClickListener(imageListener);
		imageView2.setOnClickListener(imageListener);
		imageView3.setOnClickListener(imageListener);
		addView(view);
	}

	private void applyTwoItem() {
		imageList = BookHelper.getInstance().loadImage(imagePathList, getContext());
		View view = LayoutInflater.from(getContext()).inflate(R.layout.bookpage_2item, null);
		ImageView imageView1 = (ImageView) view.findViewById(R.id.bookpage_2item_image1);
		ImageView imageView2 = (ImageView) view.findViewById(R.id.bookpage_2item_image2);
		imageView1.setImageBitmap(imageList.get(0));
		imageView2.setImageBitmap(imageList.get(1));
		imageView1.setTag(imagePathList.get(0));
		imageView2.setTag(imagePathList.get(1));
		imageView1.setOnClickListener(imageListener);
		imageView2.setOnClickListener(imageListener);
		addView(view);
	}

	private void applyOneItem() {
		imageList = BookHelper.getInstance().loadImage(imagePathList, getContext());

		LinearLayout layout = new LinearLayout(getContext());
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layout.setLayoutParams(params);

		ImageView view = new ImageView(getContext());
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		view.setLayoutParams(params1);
		view.setScaleType(ScaleType.FIT_CENTER);
		view.setImageBitmap(imageList.get(0));
		view.setBackgroundColor(getResources().getColor(R.color.white));
		view.setOnClickListener(imageListener);
		view.setTag(imagePathList.get(0));
		layout.addView(view);

		addView(layout);
	}

	/*
	private void applyThreeItem() {
		imageList = BookHelper.getInstance().loadImage(imagePathList, getContext());
		
		int mode1 = imageList.get(0).getHeight() > imageList.get(0).getWidth() ? 1:2;
		int mode2 = imageList.get(1).getHeight() > imageList.get(0).getWidth() ? 1:2;
		int mode3 = imageList.get(2).getHeight() > imageList.get(0).getWidth() ? 1:2;
		
		View view = null;
		ImageView imageView1 = null;
		ImageView imageView2 = null;
		ImageView imageView3 = null;
		switch (mode1 + mode2 + mode3) {
		case 3://1 1 1
			view = LayoutInflater.from(getContext()).inflate(R.layout.bookpage_3item_2, null);
			imageView1 = (ImageView) view.findViewById(R.id.bookpage_3item_image1);
			imageView2 = (ImageView) view.findViewById(R.id.bookpage_3item_image2);
			imageView3 = (ImageView) view.findViewById(R.id.bookpage_3item_image3);
			imageView1.setImageBitmap(imageList.get(0));
			imageView2.setImageBitmap(imageList.get(1));
			imageView3.setImageBitmap(imageList.get(2));
			imageView1.setTag(imagePathList.get(0));
			imageView2.setTag(imagePathList.get(1));
			imageView3.setTag(imagePathList.get(2));
			break;
		case 4://1 1 2
			view = LayoutInflater.from(getContext()).inflate(R.layout.bookpage_3item_3, null);
			imageView1 = (ImageView) view.findViewById(R.id.bookpage_3item_image1);
			imageView2 = (ImageView) view.findViewById(R.id.bookpage_3item_image2);
			imageView3 = (ImageView) view.findViewById(R.id.bookpage_3item_image3);
			List<Integer> heightIndexs = new ArrayList<Integer>();
			int widthIndex = 0;
			if (mode1 == 1) heightIndexs.add(0);
			else widthIndex = 0;
			if (mode2 == 1) heightIndexs.add(1);
			else widthIndex = 1;
			if (mode3 == 1) heightIndexs.add(2);
			else widthIndex = 2;
			imageView3.setImageBitmap(imageList.get(widthIndex));
			imageView1.setImageBitmap(imageList.get(heightIndexs.get(0)));
			imageView2.setImageBitmap(imageList.get(heightIndexs.get(1)));
			imageView3.setTag(imagePathList.get(widthIndex));
			imageView1.setTag(imagePathList.get(heightIndexs.get(0)));
			imageView2.setTag(imagePathList.get(heightIndexs.get(1)));
			break;
		case 5://2 2 1
			view = LayoutInflater.from(getContext()).inflate(R.layout.bookpage_3item_2, null);
			imageView1 = (ImageView) view.findViewById(R.id.bookpage_3item_image1);
			imageView2 = (ImageView) view.findViewById(R.id.bookpage_3item_image2);
			imageView3 = (ImageView) view.findViewById(R.id.bookpage_3item_image3);
			List<Integer> widthIndexs = new ArrayList<Integer>();
			int  heightIndex= 0;
			if (mode1 == 2) widthIndexs.add(0);
			else heightIndex = 0;
			if (mode2 == 2) widthIndexs.add(1);
			else heightIndex = 1;
			if (mode3 == 2) widthIndexs.add(2);
			else heightIndex = 2;
			imageView3.setImageBitmap(imageList.get(heightIndex));
			imageView1.setImageBitmap(imageList.get(widthIndexs.get(0)));
			imageView2.setImageBitmap(imageList.get(widthIndexs.get(1)));
			imageView3.setTag(imagePathList.get(heightIndex));
			imageView1.setTag(imagePathList.get(widthIndexs.get(0)));
			imageView2.setTag(imagePathList.get(widthIndexs.get(1)));
			break;
		case 6://2 2 2
			view = LayoutInflater.from(getContext()).inflate(R.layout.bookpage_3item_1, null);
			imageView1 = (ImageView) view.findViewById(R.id.bookpage_3item_image1);
			imageView2 = (ImageView) view.findViewById(R.id.bookpage_3item_image2);
			imageView3 = (ImageView) view.findViewById(R.id.bookpage_3item_image3);
			imageView1.setImageBitmap(imageList.get(0));
			imageView2.setImageBitmap(imageList.get(1));
			imageView3.setImageBitmap(imageList.get(2));
			imageView1.setTag(imagePathList.get(0));
			imageView2.setTag(imagePathList.get(1));
			imageView3.setTag(imagePathList.get(2));
			break;

		default:
			break;
		}
		if (imageView1 != null) {
			imageView1.setOnClickListener(imageListener);
			imageView2.setOnClickListener(imageListener);
			imageView3.setOnClickListener(imageListener);
		}
		if (view != null) {
			addView(view);
		}
	}
	*/

	public void recycleBitmaps() {
		Log.d(TAG, "recycleBitmaps");
		if (imageList != null) {
			for (Bitmap bitmap:imageList) {
				bitmap.recycle();
			}
			imageList.clear();
		}
	}
}
