package com.whd.wifikeyview;

import com.whd.wifikeyview.network.NetworkListener;
import com.whd.wifikeyview.network.NetworkParser.Network;
import com.whd.wifikeyview.network.NetworkParser.SupplicantKey;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

//TODO https://github.com/culmor30/android-fragments-example/blob/master/src/com/culmor30/fragmentsTesting/FragmentsTestingActivity.java
public class ShowPassword implements NetworkListener {
	
	private Context mContext;
	private Context activity;
	
	public ShowPassword(Context context) {
		activity = context;
		
		try {
			mContext = context.createPackageContext
				("com.whd.wifikeyview", Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException nnfe) {
			nnfe.printStackTrace();
			throw (new RuntimeException("Package name not found"));
		}
	}

	@Override
	public void onParserDone(Network network) {
		if (network == null) {
			WiFiKeyView.log("ShowPassword#onParserDone(Network); Network was null!!");
			return;
		}
		
		TableLayout showPasswordTable = new TableLayout(mContext);
		showPasswordTable.setStretchAllColumns(true);
		
		for (SupplicantKey key : SupplicantKey.values()) {
			String value = network.get(key);
			if ( (value != null) && (!value.equals("")) ) {
				showPasswordTable.addView(
						generateTableRow(mContext.getString(key.getString()), value)
				);
			}
		}
		
		// Toast/Dialog/CopyPass/SharePass
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		if (sharedPreferences.getBoolean("directcopypsk", false)) {
			String psk = network.get(SupplicantKey.PSK);
			if ( (psk != null) && (!psk.equals("")) ) {
				copyToClipboard(psk);
			}
		} else if (sharedPreferences.getBoolean("directcopypassword", false)) {
			String password = network.get(SupplicantKey.PASSWORD);
			if ( (password != null) && (!password.equals("")) ) {
				copyToClipboard(password);
			}
		}
		
		AlertDialog dialog = new AlertDialog.Builder(activity)
				.setTitle(network.get(SupplicantKey.SSID))
				.setView(showPasswordTable)
				.create();
		dialog.show();
	}
	
	@SuppressWarnings("deprecation")
	private void copyToClipboard(String text) {
		// Get reference to the clipboardmanager, we do not know what version we have
		Object clipboardManager = mContext.getSystemService(Context.CLIPBOARD_SERVICE);
					
		// If it is an instance of the ClipboardManager in content package, cast it
		if (clipboardManager instanceof android.content.ClipboardManager) {
			((android.content.ClipboardManager) clipboardManager).setPrimaryClip(ClipData.newPlainText("", text));
					
			// Otherwise it is the one in text package
		} else {
			((android.text.ClipboardManager) clipboardManager).setText(text);
		}
					
		// Visual feedback
		Toast.makeText(mContext, mContext.getString(R.string.password_copied), Toast.LENGTH_SHORT).show();
	}
	
	//TODO
	@SuppressWarnings("unused")
	private void share() {
		// Create intent for sharing and set data
		Intent share = new Intent();
		share.setAction(Intent.ACTION_SEND);
		share.putExtra(Intent.EXTRA_TEXT, "SSID: " /*+ ssid + "\n"*/ +  mContext.getString(R.string.password) + " "/* + password*/);
		share.setType("text/plain");
					
		// Send the intent
		mContext.startActivity(Intent.createChooser(share, mContext.getString(R.string.share_password_to)));
	}
	
	private TableRow generateTableRow(String key, String value) {
		TableRow row = new TableRow(mContext);
		row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		TextView tv_key = new TextView(mContext);
		tv_key.setText(key);
		tv_key.setPadding(15, 0, 0, 0);
		row.addView(tv_key);
		
		TextView value_key = new TextView(mContext);
		value_key.setText(value);
		value_key.setPadding(0, 0, 15, 0);
		value_key.setGravity(Gravity.RIGHT);
		row.addView(value_key);
		
		return row;
	}
}