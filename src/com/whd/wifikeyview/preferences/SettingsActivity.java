package com.whd.wifikeyview.preferences;

import java.util.List;

import com.whd.wifikeyview.R;
import com.whd.wifikeyview.preferences.fragments.DebugPreferences;
import com.whd.wifikeyview.preferences.fragments.ShowPasswordPreferences;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}
	
	protected boolean isValidFragment(String fragmentName) {
		return (
				DebugPreferences.class.getName().equals(fragmentName) 			||
				ShowPasswordPreferences.class.getName().equals(fragmentName)
		);
	}
}