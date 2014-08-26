package com.whd.wifikeyview.hooks;

import android.app.Fragment;
import android.preference.Preference;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.whd.wifikeyview.ShowPassword;
import com.whd.wifikeyview.WiFiKeyView;
import com.whd.wifikeyview.network.NetworkParseTask;

import de.robv.android.xposed.XC_MethodHook;

public class LongPressNetworkClickedHook extends XC_MethodHook {
	
	// Context menu id's
	private static int MENU_ID_SHOWPASSWORD = 999;
	
	// Is debugging enabled?
	private boolean debug;
	
	@Override
	public void beforeHookedMethod(MethodHookParam param) throws Exception {
		// Are we debugging?
		debug = WiFiKeyView.isDebugging(WiFiKeyView.getContext(param));
	}
	
	@Override
	public void afterHookedMethod(MethodHookParam param) {
		MenuItem menuItem = (MenuItem) param.args[0];

		// We only want to do something on our own menu item
		if ( menuItem.getItemId() == MENU_ID_SHOWPASSWORD) {
			
			// Output the class of the object
			if (debug) {
				Object   objectWifiSettings = param.thisObject;
				Class<?> classWifiSettings  = objectWifiSettings.getClass();
				while (classWifiSettings.getSuperclass() != null) {
					WiFiKeyView.log("WiFiSettings subclass: " + classWifiSettings.getCanonicalName());
					classWifiSettings = classWifiSettings.getSuperclass();
				}
			}
			
			// The selected position in the network list
			int selectedListPosition = ((AdapterContextMenuInfo) menuItem.getMenuInfo()).position; 
			
			if (!(param.thisObject instanceof Fragment)) {
				throw new RuntimeException("param.thisObject is not an instance of Fragment");
			}
			
			// Get an reference to the preference fragment
			Fragment wifiFragment = ((Fragment)param.thisObject);
			
			// Search the ListView
			View rawListView = wifiFragment.getView().findViewById(android.R.id.list);
			
			// Be sure it is an ListView, and not something else using id 'android.R.id.list'
			if (!(rawListView instanceof ListView)) {
				throw new RuntimeException("Content has view with id attribute 'android.R.id.list' that is not a ListView class");
			}
			
			// Cast the View to an ListView
			ListView mList = (ListView) rawListView;
			
			// Get the selected ListView item through the adapter
			Object object = mList.getAdapter().getItem(selectedListPosition);
			if (debug) {
				Class<?> classObject = object.getClass();
				while (classObject.getSuperclass() != null) {
					WiFiKeyView.log("ListView item subclass: " + classObject.getCanonicalName());
					classObject = classObject.getSuperclass();
				}
			}
			Preference pref = (Preference) object;
			
			// Get the ssid (title) of the preference
			String ssid = pref.getTitle().toString();
			
			// When debugging, show we clicked the item, and the ssid
			if (debug) {
				WiFiKeyView.log("MENU_ID_SHOWPASSWORD clicked!! SSID: " + ssid);
			}
			
			// Execute the task with given ssid, and return to ShowPasswordDialog
			new NetworkParseTask(new ShowPassword(wifiFragment.getActivity())).execute(ssid);
			
			// Set result to true
			param.setResult(true);
		}
	}
	
}