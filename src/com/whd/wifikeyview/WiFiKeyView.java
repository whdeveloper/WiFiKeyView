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

import java.util.List;

import com.possebom.openwifipasswordrecover.interfaces.NetworkListener;
import com.possebom.openwifipasswordrecover.model.Network;
import com.possebom.openwifipasswordrecover.parser.NetworkParser;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
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
public class WiFiKeyView implements IXposedHookLoadPackage {
	
	// Debugging
	private static final String  TAG = "WHD - WiFiKeyView";
	private static final boolean DBG = false;
	
	// Context menu id's
	private static final int MENU_ID_MODIFY       = Menu.FIRST + 8;
	private static final int MENU_ID_SHOWPASSWORD = Menu.FIRST + 9;
	
	
	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("com.android.settings")) return; // Settings package
		
		// Reference to the class we want to hook
		final Class<?> SettingsClazz = XposedHelpers.findClass("com.android.settings.wifi.WifiSettings", lpparam.classLoader);
		
		// Hook com.android.settings.wifi.WifiSettings#onCreateContextMenu(ContextMenu, View, ContextMenuInfo)
		// If the network is known, and the modify is showing
		XposedHelpers.findAndHookMethod(SettingsClazz, "onCreateContextMenu", ContextMenu.class, View.class, ContextMenuInfo.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// If the MenuItem for modify is found, add show password
				if ( ((ContextMenu)param.args[0]).findItem(MENU_ID_MODIFY) != null) {
					((ContextMenu) param.args[0]).add(Menu.NONE, Menu.FIRST + 9, 3, "Show password");
				}
			}
		});
		
		// Hook com.android.settings.wifi.WifiSettings#onContextItemSelected(MenuItem)
		// After the normal method has run, check if our show password is selected
		XposedHelpers.findAndHookMethod(SettingsClazz, "onContextItemSelected", MenuItem.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				MenuItem menuItem = (MenuItem) param.args[0];
				
				// We only want to do something on our own menu item
				if ( menuItem.getItemId() == MENU_ID_SHOWPASSWORD) {
					
					// Context for the Toast TODO Dialog
					final Context context = (Context) ((PreferenceFragment) param.thisObject).getActivity();
					
					// The selected position in the network list
					int selectedListPosition = ((AdapterContextMenuInfo) menuItem.getMenuInfo()).position; 
					
					// Get an reference to the preference fragment
					PreferenceFragment wifiPreferenceFragment = ((PreferenceFragment)param.thisObject);
					
					// Search the ListView
					View rawListView = wifiPreferenceFragment.getView().findViewById(android.R.id.list);
					
					// Be sure it is an ListView, and not something else using id 'android.R.id.list'
					if (!(rawListView instanceof ListView)) {
						throw new RuntimeException("Content has view with id attribute 'android.R.id.list' that is not a ListView class");
					}
					
					// Cast the View to an Listview
					ListView mList = (ListView) rawListView;
					
					// Get the selected ListView item through the adapter
					Preference pref = (Preference) mList.getAdapter().getItem(selectedListPosition);
					
					// Get the ssid (title) of the preference
					final String ssid = pref.getTitle().toString();
					
					// When debugging, show we clicked the item, and the ssid
					if (DBG) Log.v(TAG, "MENU_ID_SHOWPASSWORD clicked!! SSID: " + ssid);
					
					// Run the Task, this will check all entries in WPA_SUPPLICANT file
					new NetworkParser(new NetworkListener() {
						
						@Override
						public void onParserDone(List<Network> networkList) {
							
							// Run through all found networks
							for (Network network : networkList) {
								
								// Log all networks if we are debugging
								if (DBG) {
									Log.v(TAG, "SSID: " + network.getSsid());
									Log.v(TAG, "PASS: " + network.getPassword());
								}
								
								// If we found the network with the same ssid
								if (network.getSsid().equals(ssid)) {
									
									// TODO Show password in dialog
									// TODO Copy to clip board
									// Show the password in a Toast
									Toast.makeText(context, network.getPassword(), Toast.LENGTH_LONG).show();
									
									// Because we found what we wanted, we stop searching
									break;
								}
								
				            }
							
						}
						
					}).execute();
					
					// Set result to true
					param.setResult(true);
				}
			}
		});
    }
}