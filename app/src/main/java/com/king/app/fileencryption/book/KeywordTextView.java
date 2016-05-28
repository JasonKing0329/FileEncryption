package com.king.app.fileencryption.book;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class KeywordTextView extends TextView {
	
	private Keyword keyEntity;

	public KeywordTextView(Context context) {
		super(context);
	}

	public KeywordTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public KeywordTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setKeyword(Keyword keyword) {
		keyEntity = keyword;
	}
	
	public Keyword getKeyword() {
		return keyEntity;
	}
}
