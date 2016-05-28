package com.king.app.fileencryption.book;

import java.util.Collections;
import java.util.List;

import com.aphidmobile.flip.FlipViewController;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.open.image.ImageValue;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.thumbfolder.ShowImageDialog;
import com.king.app.fileencryption.util.DisplayHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;

public class BookActivity extends Activity implements OnClickListener {

	private static final String TAG = "BookActivity";
	public static final int FOLDER = 1;
	public static final int ORDER = 2;
	private int currentMode;
	
	private KeywordsFlow keywordsFlow;
	private List<SOrder> sorderList;
	private List<String> pathList;
	private FlipViewController flipView;
	private BookPageAdapter bookPageAdapter;
	private FrameLayout bookFrameLayout;
	private int currentPageIndex;
	
	private ShowImageDialog showImageDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DisplayHelper.enableFullScreen(this);
		DisplayHelper.disableScreenshot(this);
		setContentView(R.layout.activity_book);
		bookFrameLayout = (FrameLayout) findViewById(R.id.view_bookpage);
		
		Bundle bundle = getIntent().getExtras();
		currentMode = bundle.getInt(Constants.KEY_BOOK_INIT_MODE);
		if (currentMode == FOLDER) {
			String folderPath = bundle.getString(Constants.KEY_BOOK_INIT_FOLDER, null);
			if (folderPath == null) {
				startKeywordsFlow();
			}
			else {
				showPage(folderPath);
			}
		}
		else if (currentMode == ORDER) {
			int orderId = bundle.getInt(Constants.KEY_BOOK_INIT_ORDER, -1);
			if (orderId == -1) {
				startKeywordsFlow();
			}
			else {
				SOrder order = BookHelper.queryOrder(orderId, this);
				showPage(order);
			}
		}
	}

	private void startKeywordsFlow() {
		if (keywordsFlow == null) {
			keywordsFlow = (KeywordsFlow) findViewById(R.id.view_keyword_flow);
			keywordsFlow.setKeywordsNumber(15);
			keywordsFlow.setDuration(800l);
			keywordsFlow.setOnItemClickListener(this);
		}
		
		if (currentMode == ORDER) {
			if (sorderList == null) {
				sorderList = BookHelper.createOrderIndex(this);
			}
		}
		else if (currentMode == FOLDER) {
			if (pathList == null) {
				pathList = BookHelper.createFolderIndex();
			}
		}
		
		feedKeywords();
		keywordsFlow.go2Show(KeywordsFlow.ANIMATION_IN);
	}

	private void feedKeywords() {
		if (currentMode == ORDER) {
			Collections.shuffle(sorderList);
			Keyword keyword = null;
			for (int i = 0; i < KeywordsFlow.MAX && i < sorderList.size(); i ++) {
				keyword = new Keyword();
				keyword.setDisplayName(sorderList.get(i).getName());
				keyword.setObject(sorderList.get(i));
				keywordsFlow.feedKeyword(keyword);
			}
		}
		else if (currentMode == FOLDER) {
			Collections.shuffle(pathList);
			Keyword keyword = null;
			for (int i = 0; i < KeywordsFlow.MAX && i < pathList.size(); i ++) {
				keyword = new Keyword();
				String[] array = pathList.get(i).split("/");
				keyword.setDisplayName(array[array.length - 1]);
				keyword.setObject(pathList.get(i));
				keywordsFlow.feedKeyword(keyword);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v instanceof KeywordTextView) {
			KeywordTextView tv = (KeywordTextView) v;
			Keyword keyword = tv.getKeyword();
			if (currentMode == ORDER) {
				SOrder sOrder = (SOrder) keyword.getObject();
				BookHelper.accessOrder(this, sOrder);
				Log.d(TAG, sOrder.getName() + " selected");
				showPage(sOrder);
			}
			else if (currentMode == FOLDER) {
				String folder = (String) keyword.getObject();
				showPage(folder);
			}
		}
	}

	private void showPage(String folder) {
		List<String> list = BookHelper.getPathList(folder);
		showPage(list);
	}

	private void showPage(SOrder sOrder) {
		BookHelper.getPathList(sOrder, this);
		showPage(sOrder.getImgPathList());
	}

	private void showPage(List<String> pathList) {
		
		if (pathList == null || pathList.size() == 0) {
			Toast.makeText(this, R.string.no_content, Toast.LENGTH_LONG).show();
			return;
		}

		if (keywordsFlow != null) {
			keywordsFlow.setVisibility(View.GONE);
		}
		bookFrameLayout.setVisibility(View.VISIBLE);
		
		if (flipView == null) {
		    flipView = new FlipViewController(this);
		    bookPageAdapter = new BookPageAdapter(this);
			bookPageAdapter.updateData(BookHelper.orderPageItems(pathList));
			bookPageAdapter.setOnImageClickListener(bookImageListener);
		    flipView.setAdapter(bookPageAdapter);
		    bookFrameLayout.addView(flipView);
		    currentPageIndex = 0;
		}
		else {
			bookPageAdapter.updateData(BookHelper.orderPageItems(pathList));
			bookPageAdapter.setOnImageClickListener(bookImageListener);
			bookPageAdapter.notifyDataSetChanged();
		}
		
	}
	
	OnClickListener bookImageListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			if (showImageDialog == null) {
				showImageDialog = new ShowImageDialog(BookActivity.this, null, 0);
			}
			ImageValue value = (ImageValue) view.getTag();
			showImageDialog.setImagePath(value.getPath());
			showImageDialog.show();
		}
	};
	
	private float lastX;
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (keywordsFlow != null && keywordsFlow.getVisibility() == View.VISIBLE) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				lastX = event.getX();
				break;
			case MotionEvent.ACTION_MOVE:
				
				break;
			case MotionEvent.ACTION_UP:
				float x = event.getX();
				if (x - lastX > 100) {
					keywordsFlow.rubKeywords();
					feedKeywords();
					keywordsFlow.go2Show(KeywordsFlow.ANIMATION_OUT);
				}
				else if (x - lastX < -100) {
					keywordsFlow.rubKeywords();
					feedKeywords();
					keywordsFlow.go2Show(KeywordsFlow.ANIMATION_IN);
				}
				break;

			default:
				break;
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void onBackPressed() {

		if (keywordsFlow == null) {
			super.onBackPressed();
		}
		else {
			if (bookFrameLayout != null && bookFrameLayout.getVisibility() == View.VISIBLE) {
				//strange that setVisibility is not work in this process after change flip library
//				bookFrameLayout.setVisibility(View.GONE);
//				keywordsFlow.setVisibility(View.VISIBLE);
				reload();
			}
			else {
				super.onBackPressed();
			}
		}
	}

	protected void reload() {
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
		bookPageAdapter.recycleAll();
	}

	
}
