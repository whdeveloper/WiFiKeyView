package com.whd.wifikeyview.hooks;

import android.app.Fragment;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.possebom.openwifipasswordrecover.parser.NetworkParser;
import com.whd.wifikeyview.WiFiKeyView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class LongPressNetworkClickedHook extends XC_MethodHook {
	
	// Context menu id's
	private static int MENU_ID_SHOWPASSWORD = 999;
	
	private Context mContext;
	private boolean debug;
	
	public LongPressNetworkClickedHook(Context context) {
		mContext = context;
		
		// Are we debugging?
		debug = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("", false);
	}
	
	@Override
	public void beforeHookedMethod(MethodHookParam param) {
		
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
			
			// Context for the Dialog
			Context context = (Context) wifiFragment.getActivity();
			
			// Search the ListView
			View rawListView = wifiFragment.getView().findViewById(android.R.id.list);
			
			// Be sure it is an ListView, and not something else using id 'android.R.id.list'
			if (!(rawListView instanceof ListView)) {
				throw new RuntimeException("Content has view with id attribute 'android.R.id.list' that is not a ListView class");
			}
			
			// Cast the View to an Listview
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
			
			// Run the Task, this will check all entries in WPA_SUPPLICANT file
			new NetworkParser(new WiFiNetworkListener(context, ssid)).execute();
			
			// Set result to true
			param.setResult(true);
		}
	}
	
}