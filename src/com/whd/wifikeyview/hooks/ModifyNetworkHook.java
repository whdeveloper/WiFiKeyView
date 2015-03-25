package com.whd.wifikeyview.hooks;

import java.lang.reflect.Field;

import com.whd.wifikeyview.WiFiKeyView;
import com.whd.wifikeyview.network.NetworkListener;
import com.whd.wifikeyview.network.NetworkParseTask;
import com.whd.wifikeyview.network.NetworkParser.Network;
import com.whd.wifikeyview.network.NetworkParser.SupplicantKey;

import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class ModifyNetworkHook extends XC_MethodHook implements NetworkListener {
	
	private TextView mPasswordView;
	
	@Override
	public void afterHookedMethod(MethodHookParam param) {
		// Logging string
		String methodAndParams = "afterHookedMethod(MethodHookParam)";
		
		// Log
		WiFiKeyView.verboseLog(this, methodAndParams, "ModifyNetworkHook started");
		
		// The WifiConfigController class reference
		Class<?> wifiConfigControllerClass  = param.thisObject.getClass();
		
		for (Field f : wifiConfigControllerClass.getFields()) {
			WiFiKeyView.verboseLog(this, methodAndParams, "" + f.getName());
		}
		
		// Get the password field
		Field mPasswordViewField = null;
		try {
			mPasswordViewField = XposedHelpers.findField(wifiConfigControllerClass, "mPasswordView");
			//mPasswordViewField = wifiConfigControllerClass.getField("mPasswordView");
		} catch (NoSuchFieldError e) {
			// Print stack trace
			e.printStackTrace();
			
			// Report message in Xposed log
			WiFiKeyView.verboseLog(this, methodAndParams, "mPasswordView is not found: " + e.getMessage());
			
			// Don't continue, this will produce more exceptions
			return;
		}
		
		try {
			mPasswordView = (TextView) mPasswordViewField.get(param.thisObject);
		} catch (IllegalAccessException e) {
			// Print stack trace
			e.printStackTrace();
			
			// Report message in Xposed log
			WiFiKeyView.verboseLog(this, methodAndParams, "Failed to get value from mPasswordView field: " + e.getMessage());
			
			// Don't continue, this will produce more exceptions
			return;
			
		} catch (IllegalArgumentException e) {
			// Print stack trace
			e.printStackTrace();
			
			// Report message in Xposed log
			WiFiKeyView.verboseLog(this, methodAndParams, "Failed to get value from mPasswordView field: " + e.getMessage());
			
			// Don't continue, this will produce more exceptions
			return;
		}
		
		Field fAccessPoint = null;
		try {
			fAccessPoint = XposedHelpers.findField(wifiConfigControllerClass, "mAccessPoint");
		} catch(NoSuchFieldError e) {
			// Print stack trace
			e.printStackTrace();
			
			// Report message in Xposed log
			WiFiKeyView.verboseLog(this, methodAndParams, "mAccessPoint is not found: " + e.getMessage());
			
			// Don't continue, this will produce more exceptions
			return;
		}
		
		Class<?> accessPointClass = null;
		try {
			accessPointClass = fAccessPoint.get(param.thisObject).getClass();
		} catch (IllegalAccessException e) {
			// Print stack trace
			e.printStackTrace();
			
			// Report message in Xposed log
			WiFiKeyView.verboseLog(this, methodAndParams, "Failed to get value from mAccessPoint field: " + e.getMessage());
			
			// Don't continue, this will produce more exceptions
			return;
			
		} catch (IllegalArgumentException e) {
			// Print stack trace
			e.printStackTrace();
			
			// Report message in Xposed log
			WiFiKeyView.verboseLog(this, methodAndParams, "Failed to get value from mAccessPoint field: " + e.getMessage());
			
			// Don't continue, this will produce more exceptions
			return;
		}
		
		Field fssid = null;
		try {
			fssid = XposedHelpers.findField(accessPointClass, "ssid");
		} catch (NoSuchFieldError e) {
			// Print stack trace
			e.printStackTrace();
			
			// Report message in Xposed log
			WiFiKeyView.verboseLog(this, methodAndParams, "Failed to get value from mPasswordView field: " + e.getMessage());
			
			// Don't continue, this will produce more exceptions
			return;
			
		}
		
		String ssid = null;
		try {
			ssid = (String) fssid.get(fAccessPoint.get(param.thisObject));
		} catch (IllegalAccessException e) {
			// Print stack trace
			e.printStackTrace();
			
			// Report message in Xposed log
			WiFiKeyView.verboseLog(this, methodAndParams, "Failed to get value from ssid field: " + e.getMessage());
			
			// Don't continue, this will produce more exceptions
			return;
			
		} catch (IllegalArgumentException e) {
			// Print stack trace
			e.printStackTrace();
			
			// Report message in Xposed log
			WiFiKeyView.verboseLog(this, methodAndParams, "Failed to get value from ssid field: " + e.getMessage());
			
			// Don't continue, this will produce more exceptions
			return;
		}
		
		WiFiKeyView.verboseLog(this, "afterHookedMethod(MethodHookParam);", "After hooked method finished with ssid: " + ssid);
		
		new NetworkParseTask(this).execute(ssid);
	}

	@Override
	public void onParserDone(Network network) {
		
		String psk = network.get(SupplicantKey.PSK);
		String password = network.get(SupplicantKey.PASSWORD);
		
		String value = null;
		if ((psk != null) && (!psk.equals(""))) {
			value = psk;
		} else if ((password != null) && (!password.equals(""))) {
			value = password;
		} else {
			// TODO
		}
		
		WiFiKeyView.verboseLog(this, "onParserDone(Network);", "Parser finished, ssid: " + value);
		
		mPasswordView.setText(value);
	}
	
}