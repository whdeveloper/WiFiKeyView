package com.whd.wifikeyview;

/*
 * Copyright WHDeveloper (Wout Hendrickx)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static de.robv.android.xposed.XposedHelpers.callMethod;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import com.whd.wifikeyview.hooks.LongPressNetworkClickedHook;
import com.whd.wifikeyview.hooks.LongPressNetworkHook;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * 
 * @author WHD
 * 
 * WifiKeyView is an XPosed module which adds an option to show the password in the context menu of an wifi network
 * 
 * com.android.settings.wifi.WifiSettings: http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android-apps/4.2.2_r1/com/android/settings/wifi/WifiSettings.java/
 *
 * WiFiKeyView uses code from:
 *  	WifiPasswordRecover created by Alexandre Possebom which can be found here:
 *  			https://github.com/coxande/WifiPasswordRecover
 *  	libsuperuser created by Chainfire which can be found here:
 *  			https://github.com/Chainfire/libsuperuser
 *  
 *  The code from the other projects are still in original state, and haven't been changed.
 */
public class WiFiKeyView implements IXposedHookLoadPackage  {
	
	// Debugging
	private static final String  TAG          = "WHD - WiFiKeyView || ";
	private static final boolean DBGSensitive = false;
	
	// What to hook
	private static final String PACKAGE_SETTINGS   = "com.android.settings";
	private static final String CLASS_WIFISETTINGS = "com.android.settings.wifi.WifiSettings";
	
	private Context mContext;
	
	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(PACKAGE_SETTINGS)) return; // Settings package
		
		// Reference to the class we want to hook
		final Class<?> SettingsClazz = XposedHelpers.findClass(CLASS_WIFISETTINGS, lpparam.classLoader);
		
		// Hook com.android.settings.wifi.WifiSettings#onCreateContextMenu(ContextMenu, View, ContextMenuInfo)
		XposedHelpers.findAndHookMethod(
				SettingsClazz, 											// Class to hook
				"onCreateContextMenu", 									// Method to hook
				ContextMenu.class, View.class, ContextMenuInfo.class, 	// Method parameters
				new LongPressNetworkHook()								// Class to handle the hook
		);
		
		// Hook com.android.settings.wifi.WifiSettings#onContextItemSelected(MenuItem)
		XposedHelpers.findAndHookMethod(
				SettingsClazz, 						// Class to hook
				"onContextItemSelected", 			// Method to hook
				MenuItem.class, 					// Method parameters
				new LongPressNetworkClickedHook()	// Class to handle the hook
		);
    }
	
	/**
	 * Log to the XposedBridge
	 * 
	 * @param msg
	 * 		The text to log (TAG will be added automatically)
	 */
	public static void log(String msg) {
		XposedBridge.log(TAG + msg);
	}
	
	public static boolean isDebugging(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("debug", false);
	}
	
	public static Context getContext(MethodHookParam param) {
        Context ret = null;
        
		try {
			// Get the current activity
        	Context wifiSettingsContext = ((Activity) callMethod(param.thisObject, "getActivity"))
        			.getApplicationContext();
        	
        	// Create our own Context from the WifiSettings Context
			ret = wifiSettingsContext.createPackageContext
					("com.whd.wifikeyview", Context.CONTEXT_IGNORE_SECURITY);
			
		// Thrown if the package could not be found
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
        
		return ret;
	}
	
	private AlertDialog getDialog(Context context, String ssid, String password) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		
		// Set the title and message
		dialog.setTitle(ssid);
		dialog.setMessage((password.equals("")) ? mContext.getString(R.string.network_no_password) : (mContext.getString(R.string.password) + " " + password));
		
		// Make it cancelable
		dialog.setCancelable(true);
		
		// Create and return the dialog
		return dialog.create();
	}
	
}