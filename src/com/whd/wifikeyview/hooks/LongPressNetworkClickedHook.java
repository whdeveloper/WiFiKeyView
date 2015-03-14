package com.whd.wifikeyview.hooks;

import android.app.Fragment;
import android.net.wifi.WifiConfiguration;
import android.preference.Preference;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.whd.wifikeyview.ShowPassword;
import com.whd.wifikeyview.WiFiKeyView;
import com.whd.wifikeyview.network.NetworkParseTask;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;

public class LongPressNetworkClickedHook extends XC_MethodHook {
	
	// Context menu id's
	private static int MENU_ID_SHOWPASSWORD = 999;
	
	// Keep an reference to AccessPoint for faster reference, and no unnecessary reflection
	private static Class<?> accessPointClass;
	
	@Override
	public void beforeHookedMethod(MethodHookParam param) throws Exception {
		// Do we need to do anything before everything is created?
	}
	
	@Override
	public void afterHookedMethod(MethodHookParam param) {
		MenuItem menuItem = (MenuItem) param.args[0];

		// We only want to do something on our own menu item
		if ( menuItem.getItemId() == MENU_ID_SHOWPASSWORD) {
			WiFiKeyView.verboseLog(this, "afterHookedMethod(MethodHookParam)", "Context menu show password clicked");
			
			// Output the class of the object
			if (WiFiKeyView.isDebugging()) {
				Class<?> clazz = param.thisObject.getClass();
				StringBuilder sb = new StringBuilder();
				
				sb.append("WifiSettings subclasses:\n");
				while (clazz != null) {
					sb.append("\t" + clazz.getCanonicalName());
					for (Class<?> theInterface : clazz.getInterfaces()) {
						sb.append("\t\t" + theInterface);
					}
				}
				
				WiFiKeyView.verboseLog(this, "afterHookedMethod(MethodHookParam)", sb.toString());
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
			if (WiFiKeyView.isDebugging()) {
				Class<?> classObject = object.getClass();
				while (classObject.getSuperclass() != null) {
					WiFiKeyView.verboseLog(this, "afterHookedMethod(MethodHookParam)", "ListView item subclass: " + classObject.getCanonicalName());
					classObject = classObject.getSuperclass();
				}
			}
			
			String ssid = "";
			// The object can be of the following types
			if (object instanceof Preference) {
				Preference pref = (Preference) object;
			
				// Get the ssid (title) of the preference
				ssid = pref.getTitle().toString();
			} else if ( (getAccessPointClass(param) != null) && getAccessPointClass(param).isInstance(object)) {
				// Call getConfig via reflection
				Object result = XposedHelpers.callMethod(object, "getConfig");
				
				if (result != null) {
					if (result instanceof WifiConfiguration) {
						WifiConfiguration config = (WifiConfiguration) result;
						ssid = config.SSID;
					} else {
						WiFiKeyView.verboseLog(this, "afterHookedMethod(MethodHookParam)", "Result from reflection call object.getConfig() is null!");
					}
				} else {
					WiFiKeyView.verboseLog(this, "afterHookedMethod(MethodHookParam)", "Reflection call object.getConfig() failed!");
				}
			}
			
			// When debugging, show we clicked the item, and the ssid
			if (WiFiKeyView.isDebugging()) {
				WiFiKeyView.verboseLog(this, "afterHookedMethod(MethodHookParam)", "MENU_ID_SHOWPASSWORD clicked!! SSID: " + ssid);
			}
			
			// Execute the task with given ssid, and return to ShowPasswordDialog
			new NetworkParseTask(new ShowPassword(wifiFragment.getActivity())).execute(ssid);
			
			// Set result to true
			param.setResult(true);
		}
	}
	
	private Class<?> getAccessPointClass(MethodHookParam param) {
		// If we already searched before, directly return
		if (accessPointClass == null) {
			// Try to load the class AccessPoint
			try {
				// If the class is not found, an error is thrown
				accessPointClass = XposedHelpers.findClass("AccessPoint", null);
			
				if (WiFiKeyView.isDebugging()) {
					WiFiKeyView.verboseLog(this, "getAccessPointClass(MethodHookParam)", "AccessPoint class was found");
				}
			} catch (ClassNotFoundError cnfe) {
				// Do not do anything, we will not use it if the reference is null
				if (WiFiKeyView.isDebugging()) {
					WiFiKeyView.verboseLog(this, "getAccessPointClass(MethodHookParam)", "AccessPoint class was not found");
				}
			}
		}
		
		return accessPointClass;
	}
	
}