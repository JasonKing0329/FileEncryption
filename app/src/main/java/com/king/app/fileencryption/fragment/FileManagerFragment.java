package com.king.app.fileencryption.fragment;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.FragmentAction;
import com.king.app.fileencryption.controller.MainViewAction;
import com.king.app.fileencryption.filemanager.view.FileManagerPageUpdate;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FileManagerFragment extends Fragment {

	private final String TAG = "FileManagerFragment";
	private FileManagerPageUpdate fileManagerPage;
	private View contentView;
	private FragmentAction fragmentAction;

	public void setFragmentAction (FragmentAction action) {
		fragmentAction = action;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d(TAG, "onCreateView");
		if (contentView == null) {
			Log.d(TAG, "reload view & page");
			contentView = inflater.inflate(R.layout.page_file_manager, null);
			fileManagerPage = new FileManagerPageUpdate(getActivity(), contentView);
		}
		return contentView;
	}

	public MainViewAction getMainViewAction() {

		return fileManagerPage;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (fragmentAction != null) {
			fragmentAction.onFragmentInitEnd(this);
		}
	}

	public String getCurrentPath() {
		return fileManagerPage.getCurrentPath();
	}
	
}
