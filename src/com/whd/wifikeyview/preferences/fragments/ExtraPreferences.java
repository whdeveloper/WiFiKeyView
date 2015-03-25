package com.whd.wifikeyview.preferences.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.whd.wifikeyview.R;

public class ExtraPreferences extends PreferenceFragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences_extra);
	}
}