package com.whd.wifikeyview.hooks;

import static de.robv.android.xposed.XposedHelpers.callMethod;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;

import com.whd.wifikeyview.R;
import com.whd.wifikeyview.WiFiKeyView;

import de.robv.android.xposed.XC_MethodHook;

public class LongPressNetworkHook extends XC_MethodHook {
	
	// Context menu id
	private static int MENU_ID_SHOWPASSWORD = 999;
	
	private Context mContext;
	private boolean debug;
	
	public LongPressNetworkHook(Context context) {
		mContext = context;
		
		// Are we debugging?
		debug = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("", false);
	}
	
	@Override
	public void beforeHookedMethod(MethodHookParam param) throws Exception {
        if (mContext == null) {
            Context wifiSettingsContext = ((Activity) callMethod(param.thisObject, "getActivity")).getApplicationContext();
            mContext = wifiSettingsContext.createPackageContext("com.whd.wifikeyview", Context.CONTEXT_IGNORE_SECURITY);
        }
	}
	
	@Override
	public void afterHookedMethod(MethodHookParam param) {
		ContextMenu menu = null;
		try {
			menu = ((ContextMenu) param.args[0]);
		} catch (ClassCastException cce) {
			cce.printStackTrace();
		}
		
		if (menu == null) {
			WiFiKeyView.log("LongPressNetworkHook#afterHookedMethod(MethodHookParam); ContextMenu not found.");
			return;
		}
		
		// If the context menu has all options added (or not the connect)
		int size = menu.size(); 
		if ( (size == 2) || (size == 3) ) {
			menu.add(
					Menu.NONE, 										// I do not care about the group it is in
					MENU_ID_SHOWPASSWORD, 							// The id to listen for clicks
					Menu.NONE, 										// I do not care about the order the items are in
					mContext.getString(R.string.show_password)		// The text for the menu option
			);
			
			if (debug) {
				WiFiKeyView.log("Show password added to Context menu.");
			}
		}
	}
	
}