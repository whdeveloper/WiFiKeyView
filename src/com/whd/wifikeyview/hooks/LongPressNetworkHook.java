package com.whd.wifikeyview.hooks;

import android.content.Context;
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
	
	@Override
	public void beforeHookedMethod(MethodHookParam param) throws Exception {
		// Get the Context
		mContext = WiFiKeyView.getContext(param);
		
		// Are we debugging?
		debug = WiFiKeyView.isDebugging(mContext);
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
		/*
		 * If an network is not known there is only 1 option
		 * 1. Connect
		 * 
		 * If an network is already known there are 2 options for the menu items:
		 * 1. Connect, Modify, Forget	-> When not connected to the network
		 * 2. Modify, Forget			-> When connected to the network
		 * 
		 * As we can only see the password previously entered, this only supports known networks
		 */
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