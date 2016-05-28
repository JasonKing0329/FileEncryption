package com.king.app.fileencryption.setting;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.AccessController;
import com.king.app.fileencryption.controller.FingerPrintController;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.publicview.ActionBar;
import com.king.app.fileencryption.publicview.ActionBar.ActionBarListener;
import com.king.app.fileencryption.util.DisplayHelper;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class SettingActivity extends Activity implements ActionBarListener {

	private ActionBar actionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setTheme(new ThemeManager(this).getDefaultTheme());
		super.onCreate(savedInstanceState);
		if (DisplayHelper.isFullScreen()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		setContentView(Application.isLollipop() ? R.layout.settings_l : R.layout.settings);
		actionBar = new ActionBar(this, this);
		actionBar.setTitle(getResources().getString(R.string.action_settings));
		// getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
		//通过观察源码看出android.R.id.content代表的是LinearLayout(属于ViewGroup)
		getFragmentManager().beginTransaction().replace(R.id.setting_container, new SettingFragment()).commit();
	}

	public static class SettingFragment extends PreferenceFragment implements OnPreferenceChangeListener {

		private CheckBoxPreference accessModeBox, enablePageBox, enableSlidingBox, enableFingerBox;
		private EditTextPreference pageNumberPreference, casualLookPreference, minItemPreference;
		private ListPreference speedList, autoplayModeList, orderShowList
				, colNumPref, horColNumPref, slidingMenuPref, cascadeNumberPref, startViewPref;
		private static Toast fpNotSupportToast;

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.setting_main);

			accessModeBox = (CheckBoxPreference) findPreference("setting_enable_private");
			enablePageBox = (CheckBoxPreference) findPreference("setting_sorder_enable_page");
			enableSlidingBox = (CheckBoxPreference) findPreference("setting_slidingmenu_enable");
			enableFingerBox = (CheckBoxPreference) findPreference("setting_enable_fingerprint");
			speedList = (ListPreference) findPreference("setting_auto_play_speed");
			autoplayModeList = (ListPreference) findPreference("setting_auto_play_mode");
			orderShowList = (ListPreference) findPreference("setting_sorder_mode");
			colNumPref = (ListPreference) findPreference("setting_waterfall_colnumber");
			horColNumPref = (ListPreference) findPreference("setting_waterfall_colnumber_hor");
			slidingMenuPref = (ListPreference) findPreference("setting_slidingmenu_mode");
			pageNumberPreference = (EditTextPreference) findPreference("setting_sorder_page_numbers");
			casualLookPreference = (EditTextPreference) findPreference("setting_edit_casual");
			minItemPreference = (EditTextPreference) findPreference("setting_min_items");
			cascadeNumberPref = (ListPreference) findPreference("setting_cover_cascade_num");
			startViewPref = (ListPreference) findPreference("setting_start_view");
			speedList.setOnPreferenceChangeListener(this);
			orderShowList.setOnPreferenceChangeListener(this);
			autoplayModeList.setOnPreferenceChangeListener(this);
			accessModeBox.setOnPreferenceChangeListener(this);
			enablePageBox.setOnPreferenceChangeListener(this);
			horColNumPref.setOnPreferenceChangeListener(this);
			pageNumberPreference.setOnPreferenceChangeListener(this);
			casualLookPreference.setOnPreferenceChangeListener(this);
			minItemPreference.setOnPreferenceChangeListener(this);
			cascadeNumberPref.setOnPreferenceChangeListener(this);
			colNumPref.setOnPreferenceChangeListener(this);
			horColNumPref.setOnPreferenceChangeListener(this);
			slidingMenuPref.setOnPreferenceChangeListener(this);
			startViewPref.setOnPreferenceChangeListener(this);

			initPreferenceData();
		}

		private void initPreferenceData() {
			String speeds[] = getResources().getStringArray(R.array.setting_auto_play_time);
			String autoPlayModes[] = getResources().getStringArray(R.array.setting_auto_play_mode);
			String orderShow[] = getResources().getStringArray(R.array.setting_sorder_mode);
			String slidingMenuModes[] = getResources().getStringArray(R.array.setting_slidingmenu_mode);

			if (speedList.getValue() == null) {
				speedList.setSummary(speeds[1]);
			}
			else {
				speedList.setSummary(getSpeedText(speedList.getValue()));
			}

			if (autoplayModeList.getValue() == null) {
				autoplayModeList.setSummary(autoPlayModes[2]);
			}
			else {
				autoplayModeList.setSummary(getAutoPlayModeText(autoplayModeList.getValue()));
			}

			if (orderShowList.getValue() == null) {
				orderShowList.setSummary(orderShow[0]);
			}
			else {
				orderShowList.setSummary(getOrderShowText(orderShowList.getValue()));
			}

			SettingMemo memo = SettingMemo.getInstance();
			int n = SettingProperties.getSOrderPageNumber(getActivity());
			pageNumberPreference.setSummary("" + n);
			memo.setOldPageNumber(n);

			casualLookPreference.setSummary("" + SettingProperties.getCasualLookNumber(getActivity()));
			minItemPreference.setSummary("" + SettingProperties.getMinNumberToPlay(getActivity()));

			n = SettingProperties.getCascadeCoverNumber(getActivity());
			cascadeNumberPref.setSummary("" + n);
			memo.setOldCascadeNumber(n);

			colNumPref.setSummary("" + SettingProperties.getWaterfallColNum(getActivity()));
			horColNumPref.setSummary("" + SettingProperties.getWaterfallHorColNum(getActivity()));

			startViewPref.setSummary(getStartViewText(SettingProperties.getStartViewMode(getActivity())));

			n = SettingProperties.getSlidingMenuMode(getActivity());
			slidingMenuPref.setSummary(slidingMenuModes[n]);
			memo.setOldSlidingMode(n);

			memo.setOldPageModeEnable(SettingProperties.isPageModeEnable(getActivity()));
			memo.setOldSlidingEnbale(SettingProperties.isMainViewSlidingEnable(getActivity()));

			if (!new FingerPrintController(getActivity()).isSupported()) {
				enableFingerBox.setEnabled(false);
				if (fpNotSupportToast == null) {
					fpNotSupportToast = Toast.makeText(getActivity(), R.string.login_finger_not_support, Toast.LENGTH_LONG);
					fpNotSupportToast.show();
				}
			}
		}

		private CharSequence getStartViewText(int startViewMode) {
			String startViewModes[] = getResources().getStringArray(R.array.setting_start_view);
			return startViewModes[startViewMode];
		}

		private CharSequence getStartViewText(String key) {
			String startViewKeys[] = getResources().getStringArray(R.array.setting_start_view_key);
			String startViews[] = getResources().getStringArray(R.array.setting_start_view);
			int index = 0;
			if (key.equals(startViewKeys[0])) {
				index = 0;
			}
			else if (key.equals(startViewKeys[1])) {
				index = 1;
			}
			else if (key.equals(startViewKeys[2])) {
				index = 2;
			}
			return startViews[index];
		}

		private String getSpeedText(String key) {
			String speedKeys[] = getResources().getStringArray(R.array.setting_auto_play_time_key);
			String speeds[] = getResources().getStringArray(R.array.setting_auto_play_time);
			int index = 0;
			if (key.equals(speedKeys[0])) {
				index = 0;
			}
			else if (key.equals(speedKeys[1])) {
				index = 1;
			}
			else if (key.equals(speedKeys[2])) {
				index = 2;
			}
			return speeds[index];
		}

		private String getAutoPlayModeText(String key) {
			int index = 0;
			String autoPlayModes[] = getResources().getStringArray(R.array.setting_auto_play_mode);
			String autoPlayModeKeys[] = getResources().getStringArray(R.array.setting_auto_play_mode_key);
			if (key.equals(autoPlayModeKeys[0])) {
				index = 0;
			}
			else if (key.equals(autoPlayModeKeys[1])) {
				index = 1;
			}
			else if (key.equals(autoPlayModeKeys[2])) {
				index = 2;
			}
			return autoPlayModes[index];
		}

		private String getOrderShowText(String key) {
			int index = 0;
			String orderShow[] = getResources().getStringArray(R.array.setting_sorder_mode);
			String orderShowKeys[] = getResources().getStringArray(R.array.setting_sorder_mode_key);
			if (key.equals(orderShowKeys[0])) {
				index = 0;
			}
			else if (key.equals(orderShowKeys[1])) {
				index = 1;
			}
			else if (key.equals(orderShowKeys[2])) {
				index = 2;
			}
			else if (key.equals(orderShowKeys[3])) {
				index = 3;
			}
			return orderShow[index];
		}

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (preference == accessModeBox) {
				if (accessModeBox.isChecked()) {
					AccessController.getInstance().changeAccessMode(AccessController.ACCESS_MODE_PRIVATE);
				}
			}
			else if (preference == startViewPref) {
				startViewPref.setSummary(getStartViewText(newValue.toString()));
			}
			else if (preference == speedList) {
				speedList.setSummary(getSpeedText(newValue.toString()));
			}
			else if (preference == autoplayModeList) {
				autoplayModeList.setSummary(getAutoPlayModeText(newValue.toString()));
			}
			else if (preference == orderShowList) {
				orderShowList.setSummary(getOrderShowText(newValue.toString()));
			}
			else if (preference == pageNumberPreference) {
				pageNumberPreference.setSummary(newValue.toString());
			}
			else if (preference == colNumPref) {
				colNumPref.setSummary(newValue.toString());
			}
			else if (preference == horColNumPref) {
				horColNumPref.setSummary(newValue.toString());
			}
			else if (preference == casualLookPreference) {
				casualLookPreference.setSummary(newValue.toString());
			}
			else if (preference == minItemPreference) {
				minItemPreference.setSummary(newValue.toString());
			}
			else if (preference == cascadeNumberPref) {
				cascadeNumberPref.setSummary(newValue.toString());
			}
			else if (preference == slidingMenuPref) {
				String slidingMenuModes[] = getResources().getStringArray(R.array.setting_slidingmenu_mode);
				String values[] = getResources().getStringArray(R.array.setting_slidingmenu_mode_value);
				String value = newValue.toString();
				for (int i = 0; i < values.length; i ++) {
					if (value.equals(values[i])) {
						value = slidingMenuModes[i];
						break;
					}
				}
				slidingMenuPref.setSummary(value);
			}
			return true;
		}
	}

	@Override
	public void onBack() {
		finish();
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void createMenu(MenuInflater menuInflater, Menu menu) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPrepareMenu(MenuInflater menuInflater, Menu menu) {
		// TODO Auto-generated method stub

	}

	@Override
	public OnMenuItemClickListener getMenuItemListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onTextChanged(String text, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDelete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onIconClick(View view) {
		// TODO Auto-generated method stub

	}
}
