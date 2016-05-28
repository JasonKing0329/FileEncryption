package com.king.app.fileencryption.fragment;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.FragmentAction;
import com.king.app.fileencryption.controller.MainViewAction;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.spicture.view.SPicturePageUpdate;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SPictureFragment extends Fragment {

	private final String TAG = "SPictureFragment";
	private SPicturePageUpdate sPicturePage;
	private View contentView;
	private FragmentAction fragmentAction;
	
	public static final String INIT_MODE_FOLDER = "folder";
	public static final String INIT_MODE_ORDER = "order";

	public void setFragmentAction (FragmentAction action) {
		fragmentAction = action;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d(TAG, "onCreateView");
		if (contentView == null) {
			Log.d(TAG, "reload view & page");
			contentView = inflater.inflate(R.layout.page_spicture_gallery_update, null);
			sPicturePage = new SPicturePageUpdate(getActivity(), contentView);
			reInit(getArguments());//only in first time
		}
		
		return contentView;
	}

	public void reInit(Bundle bundle) {
		if (bundle != null) {
			String mode = bundle.getString(Constants.KEY_SPICTURE_INIT_MODE);
			if (mode != null) {
				if (mode.equals(INIT_MODE_FOLDER)) {
					String folder = bundle.getString(Constants.KEY_SPICTURE_INIT_MODE_VALUE);
					sPicturePage.initFromFolder(folder);
				}
				else if (mode.equals(INIT_MODE_ORDER)) {
					int orderId = bundle.getInt(Constants.KEY_SPICTURE_INIT_MODE_VALUE);
					sPicturePage.initFromOrder(orderId);
				}
			}
		}
	}

	public MainViewAction getMainViewAction() {

		return sPicturePage;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		fragmentAction.onFragmentInitEnd(this);
	}

	public boolean isFolderMode() {
		return sPicturePage.isFolderMode();
	}
	
	public String getCurrentPath() {
		return sPicturePage.getCurrentPath();
	}

	public void updateCurrentPath(String path) {
		sPicturePage.updateCurrentPath(path);
	}

}
