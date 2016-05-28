package com.king.app.fileencryption.data;

import com.king.app.fileencryption.R;

public class Constants {

	//set timeline indicator bk from order 'TimeLine'
	public static final boolean FEATURE_TIMELINE_ENABLE_BK = false;
	
	public static final boolean DEBUG = true;
	public static final String LOG_TAG_INIT = "fe_initialization";
	public static final String LOG_TAG_SERVICE_FILE = "fe_service_file";
	public static final String LOG_SQL = "fe_sql";
	public static final String LOG_SCROLL = "fe_scroll";
	public static final String LOG_ADAPTER = "fe_adapter";
	
	public static final boolean TEST_FILE_SERVICE = true;
	
	public static final int WATERFALL_MIN_NUMBER = 1;
	public static final int WATERFALL_NUM_PER_LOAD = 20;
	public static final int SLIDING_MENU_DEFAULT_BK = R.drawable.wall_bk5;
	
	public static final int[] WaterfallMenu = new int[] {
		R.string.menu_view_all,
		R.string.menu_exit,
		R.string.action_settings
	};

	public static final int[] fileManagerMenu = new int[] {
		R.string.menu_mode_switch,
		//R.string.menu_new_folder,
		R.string.menu_encrypt_current_folder,
		R.string.menu_decipher_current_folder,
		R.string.menu_export, 
		R.string.menu_load,
		R.string.menu_change_theme,
		R.string.menu_check_all_unencrypted,
		R.string.menu_edit,
		R.string.menu_exit
	};
	public static final int[] sorderMenu = new int[] {
		R.string.menu_mode_switch,
		R.string.menu_create_order,
		R.string.menu_view_list,
		R.string.menu_edit,
		R.string.menu_exit
	};
	@Deprecated
	public static final int[] spictureMenu = new int[] {
		R.string.menu_mode_switch,
		R.string.menu_view_current_folder,
		R.string.menu_order,
		R.string.menu_rank,
		R.string.menu_shuffle_gallery,
		R.string.menu_random_game,
		R.string.menu_edit,
		R.string.menu_exit
	};

	public static final String KEY_THUMBFOLDER_INIT_MODE = "thumb_init_mode";
	public static final String KEY_THUMBFOLDER_CHOOSER_MODE = "thumb_chooser_mode";
	public static final String KEY_THUMBFOLDER_CHOOSE_CONTENT = "thumb_choose_content";
	
	public static final String KEY_BOOK_INIT_MODE = "book_init_mode";
	public static final String KEY_BOOK_INIT_FOLDER = "book_init_folder";
	public static final String KEY_BOOK_INIT_ORDER = "book_init_order";

	public static final String KEY_FOLDERDLG_ROOT = "foderdlg_root_path";

	public static final String KEY_SPICTURE_INIT_MODE = "spic_init_mode";
	public static final String KEY_SPICTURE_INIT_MODE_VALUE = "spic_init_mode_value";

	public static final String KEY_RANDOM_GAME_SORDER = "randomgame_sorder_id";
	
	public static final int STATUS_MOVE_FILE_DONE = 10;
	public static final int STATUS_MOVE_FILE_FINISH = 11;
	public static final int STATUS_MOVE_FILE_UNSUPORT = 12;
	public static final String KEY_MOVETO_UNSUPPORT_SRC = "moveto_unsupport_src";
	public static final String KEY_MOVETO_UNSUPPORT_FINISH = "moveto_unsupport_src_finish";

	public static final int STATUS_LOAD_CHOOSERITEM_FINISH = 9;
	
	public static final String TIMELINE_INDICATOR_DEFAULT_ORDER = "TimeLine";
}
