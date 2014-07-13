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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.possebom.openwifipasswordrecover.interfaces.NetworkListener;
import com.possebom.openwifipasswordrecover.model.Network;
import com.whd.wifikeyview.hooks.LongPressNetworkClickedHook;
import com.whd.wifikeyview.hooks.LongPressNetworkHook;

import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
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

    // Modules context
    private static Context mContext;
	
	// What to hook
	private static final String PACKAGE_SETTINGS   = "com.android.settings";
	private static final String CLASS_WIFISETTINGS = "com.android.settings.wifi.WifiSettings";
	
	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(PACKAGE_SETTINGS)) return; // Settings package
		
		// Reference to the class we want to hook
		final Class<?> SettingsClazz = XposedHelpers.findClass(CLASS_WIFISETTINGS, lpparam.classLoader);
		
		// Get the Context
		XposedHelpers.findAndHookMethod(SettingsClazz, "onAttach", Activity.class, new XC_MethodHook() {
			@Override
			public void beforeHookedMethod(MethodHookParam param) {
				try {
					mContext = ((Context) param.args[0]).createPackageContext("com.whd.wifikeyview", Context.CONTEXT_IGNORE_SECURITY);
				} catch (NameNotFoundException nnfe) {
					nnfe.printStackTrace();
				}
			}
		});
		
		// Hook com.android.settings.wifi.WifiSettings#onCreateContextMenu(ContextMenu, View, ContextMenuInfo)
		// If the network is known, and the modify is showing
		XposedHelpers.findAndHookMethod(
				SettingsClazz, 											// Class to hook
				"onCreateContextMenu", 									// Method to hook
				ContextMenu.class, View.class, ContextMenuInfo.class, 	// Method parameters
				new LongPressNetworkHook(mContext)						// Class to handle the hook
		);
		
		// Hook com.android.settings.wifi.WifiSettings#onContextItemSelected(MenuItem)
		// After the normal method has run, check if our show password is selected
		XposedHelpers.findAndHookMethod(
				SettingsClazz, 								// Class to hook
				"onContextItemSelected", 					// Method to hook
				MenuItem.class, 							// Method parameters
				new LongPressNetworkClickedHook(mContext)	// Class to handle the hook
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
	
	private AlertDialog getDialog(Context context, String ssid, String password) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		
		// Set the title and message
		dialog.setTitle(ssid);
		dialog.setMessage((password.equals("")) ? mContext.getString(R.string.network_no_password) : (mContext.getString(R.string.password) + " " + password));
		
		// Make it cancelable
		dialog.setCancelable(true);
		
		// Positive button is for copy to clipboard
		dialog.setPositiveButton(mContext.getString(R.string.copy), new CopyClickListener(context, password));
		
		// Negative buttons is for sharing
		dialog.setNegativeButton(mContext.getString(R.string.share), new ShareClickListener(context, ssid, password));
		
		// Create and return the dialog
		return dialog.create();
	}
	
	private class CopyClickListener implements OnClickListener {
		
		private Context context;
		private String  password;
		
		public CopyClickListener(Context context, String password) {
			this.context  = context;
			this.password = password;
		}
		
		@SuppressWarnings("deprecation") // Support all versions of android
		@Override
		public void onClick(DialogInterface dialog, int which) {
			
			// Get reference to the clipboardmanager, we do not know what version we have
			Object clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE);
			
			// If it is an instance of the ClipboardManager in content package, cast it
			if (clipboardManager instanceof android.content.ClipboardManager) {
				((android.content.ClipboardManager) clipboardManager).setPrimaryClip(ClipData.newPlainText("ssid", password));
			
			// Otherwise it is the one in text package
			} else {
				((android.text.ClipboardManager) clipboardManager).setText(password);
			}
			
			// Visual feedback
			Toast.makeText(context, mContext.getString(R.string.password_copied), Toast.LENGTH_SHORT).show();
		}
		
	}
	
	private class ShareClickListener implements OnClickListener {
		
		private Context context;
		private String  ssid;
		private String  password;
		
		public ShareClickListener(Context context, String ssid, String password) {
			this.context  = context;
			this.ssid     = ssid;
			this.password = password;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			
			// Create intent for sharing and set data
			Intent share = new Intent();
			share.setAction(Intent.ACTION_SEND);
			share.putExtra(Intent.EXTRA_TEXT, "SSID: " + ssid + "\n" +  mContext.getString(R.string.password) + " " + password);
			share.setType("text/plain");
			
			// Send the intent
			context.startActivity(Intent.createChooser(share, mContext.getString(R.string.share_password_to)));
		}
	}
	
	public class WiFiNetworkListener implements NetworkListener {
		
		private Context context;
		private String  ssid;
		
		public WiFiNetworkListener(Context context, String ssid) {
			this.context = context;
			this.ssid    = ssid;
		}
		
		@Override
		public void onParserDone(List<Network> networkList) {
			
			// Run through all found networks
			for (Network network : networkList) {
				
				// Log all networks if we are debugging
				if (DBGSensitive) {
					log("SSID: " + network.getSsid());
					log("PASS: " + network.getPassword());
				}
				
				// If we found the network with the same ssid
				if (network.getSsid().equals(ssid)) {
					
					if (DBGSensitive) {
						log("Found network: " + ssid + ", with password: " + network.getPassword());
					}
					
					// Show the password in a Dialog
					getDialog(context, ssid, network.getPassword()).show();
					
					// Because we found what we wanted, we stop searching
					break;
				}
            }
		}
	}
}