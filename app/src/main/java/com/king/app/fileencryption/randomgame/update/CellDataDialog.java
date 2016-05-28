package com.king.app.fileencryption.randomgame.update;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class CellDataDialog extends Dialog implements OnClickListener {

	public interface OnCellDataChangeListener {
		public void onCellDataChange(TableData data);
	}
	
	private TableData cellData;
	private EditText scoreEdit;
	private TextView okButton, cancelButton;
	private OnCellDataChangeListener onDataChangeListener;
	private ImageView sceneImageView;
	private EditText sceneEdit, roleEdit;
	
	public CellDataDialog(Context context, TableData tableData, OnCellDataChangeListener listener) {
		super(context);
		cellData = tableData;
		onDataChangeListener = listener;
		
		if (Application.isLollipop()) {
			setContentView(R.layout.layout_rgame_celldialog_l);
		}
		else {
			setContentView(R.layout.layout_rgame_celldialog);
		}
		setTitle(null);
		
		scoreEdit = (EditText) findViewById(R.id.celldatadlg_score);
		sceneEdit = (EditText) findViewById(R.id.celldatadlg_scene);
		roleEdit = (EditText) findViewById(R.id.celldatadlg_role);
		sceneImageView = (ImageView) findViewById(R.id.celldatadlg_scene_image);
		okButton = (TextView) findViewById(R.id.celldatadlg_ok);
		cancelButton = (TextView) findViewById(R.id.celldatadlg_cancel);
		okButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		sceneImageView.setOnClickListener(this);
		
		if (tableData.getScore() != 0.0f) {
			scoreEdit.setText("" + tableData.getScore());
		}
		if (tableData.getScene() != null) {
			sceneEdit.setText("" + tableData.getScene());
		}
		if (tableData.getScenePath() != null) {
			sceneImageView.setImageBitmap(PictureManagerUpdate.getInstance()
					.getSpictureItem(tableData.getScenePath(), context));
		}
		if (tableData.getRole() != null) {
			roleEdit.setText("" + tableData.getRole());
		}
	}

	@Override
	public void onClick(View view) {
		if (view == okButton) {
			String scoreStr = scoreEdit.getText().toString();
			if (scoreStr.equals("")) {
				scoreEdit.setError("score couldn't be null");
				return;
			}
			float score = Float.parseFloat(scoreStr);
			cellData.setScore(score);
			cellData.setScene(sceneEdit.getText().toString());
			cellData.setRole(roleEdit.getText().toString());
			if (onDataChangeListener != null) {
				onDataChangeListener.onCellDataChange(cellData);
			}
			dismiss();
		}
		else if (view == cancelButton) {
			dismiss();
		}
		else if (view == sceneImageView) {
			
		}
	}

}
