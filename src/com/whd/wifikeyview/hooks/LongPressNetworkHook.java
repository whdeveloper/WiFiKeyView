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
		debug = WiFiKeyView.isDebugging();
	}
	
	@Override
	public void afterHookedMethod(MethodHookParam param) {
		WiFiKeyView.verboseLog(this, "afterHookedMethod(MethodHookParam)", "Injecting context menu item...");
		
		// Get the context menu from the arguments
		ContextMenu menu = null;
		try {
			menu = ((ContextMenu) param.args[0]);
		} catch (ClassCastException cce) {
			// Another method was hooked, otherwise the footprint would have been the same,
			// something went horribly wrong here
			cce.printStackTrace();
		}
		
		// Without the menu, there is nothing to do
		if (menu == null) {
			WiFiKeyView.verboseLog(this, "afterHookedMethod(MethodHookParam)", "Could not find ContextMenu in parameter -> null!");
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
					Menu.NONE, 												// Group 	(not used/needed)
					MENU_ID_SHOWPASSWORD, 									// Id 		(of the item)
					Menu.NONE, 												// Order    (not used/needed)
					mContext.getString(R.string.menu_option_show_password)	// Text		(for display in the item)
			);
			
			if (debug) {
				WiFiKeyView.verboseLog(this, "afterHookedMethod(MethodHookParam)", "Show password added to Context menu.");
			}
		}
	}
	
}