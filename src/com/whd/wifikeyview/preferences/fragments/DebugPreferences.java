package com.whd.wifikeyview.preferences.fragments;

import com.whd.wifikeyview.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class DebugPreferences extends PreferenceFragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences_debug);
	}
}