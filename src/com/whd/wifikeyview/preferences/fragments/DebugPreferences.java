package com.whd.wifikeyview.preferences.fragments;

import com.whd.wifikeyview.R;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class DebugPreferences extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences_debug);
		
		getPreferenceManager()
			.getSharedPreferences()
			.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("hide_launcher_icon")) {
			int newState = (sharedPreferences.getBoolean(key, false)) ? 
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED : 
						PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
			
			PackageManager packageManager = 
					getActivity().getPackageManager();
			ComponentName componentName = 
					new ComponentName(getActivity(), "com.whd.wifikeyview.preferences.SettingsActivity-Alias");
			
			int oldState = packageManager.getComponentEnabledSetting(componentName);
			
			if (newState != oldState) {
				packageManager.setComponentEnabledSetting(componentName, newState, PackageManager.DONT_KILL_APP);
			}
		}
	}
}