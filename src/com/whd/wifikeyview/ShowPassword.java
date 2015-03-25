package com.whd.wifikeyview;

import com.whd.wifikeyview.network.NetworkListener;
import com.whd.wifikeyview.network.NetworkParser.Network;
import com.whd.wifikeyview.network.NetworkParser.SupplicantKey;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
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
	public void onParserDone(final Network network) {
		if (network == null) {
			WiFiKeyView.verboseLog(this, "onParserDone(Network)", "Network was null!!");
			return;
		}
		
		TableLayout showPasswordTable = new TableLayout(mContext);
		showPasswordTable.setStretchAllColumns(true);
		
		int backgroundColor = 0;
		int defValue = isColorDark(backgroundColor) ? Color.WHITE : Color.BLACK;
		int color = WiFiKeyView.getSharedPreferences().getInt("showpassworddialogtextcolor", defValue);
		
		// Read all network entries and add those with data
		for (SupplicantKey key : SupplicantKey.values()) {
			
			// The SSID should be in the title and not in our table
			if (key == SupplicantKey.SSID) {
				continue;
			}
			
			// Get the value and generate an TableRow if needed
			String value = network.get(key);
			if ( (value != null) && (!value.equals("")) ) {
				showPasswordTable.addView(
						generateTableRow(mContext.getString(key.getString()), value, color)
				);
			}
		}
		
		// Get PSK and password
		String psk = network.get(SupplicantKey.PSK);
		String password = network.get(SupplicantKey.PASSWORD);
		
		// Copy PSK to clip board if preference is set
		if (WiFiKeyView.doCopyPSK() && (psk != null) && (!psk.equals("")) ) {
			copyToClipboard(psk);
		}
			
		// Copy password to clip board if preference is set
		if (WiFiKeyView.doCopyPassword() && (password != null) && (!password.equals("")) ) {
			copyToClipboard(password);
		}
		
		// Show results to the user
		AlertDialog dialog = new AlertDialog.Builder(activity, android.R.style.Theme_Holo_Dialog)
				.setTitle(network.get(SupplicantKey.SSID))
				.setView(showPasswordTable)
				.setPositiveButton(mContext.getString(R.string.copy), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						// Get psk and password
						String psk = network.get(SupplicantKey.PSK);
						String password = network.get(SupplicantKey.PASSWORD);
						
						// Copy the right one to the clipboard
						if ( (psk != null) && (!psk.equals("")) ) {
							copyToClipboard(psk);
						} else if ( (password != null) && (!password.equals("")) ) {
							copyToClipboard(password);
						}
					}
				})
				.setNegativeButton(mContext.getString(R.string.cancel), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
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
	
	// TODO
	// Custom strings?
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
	
	/**
	 * Display a row from the supplicant file in a human readable way
	 * 
	 * @param key
	 * 	: The key for the entry
	 * @param value
	 * 	: The value for the entry
	 * @return
	 * 	: A TableRow object to be displayed
	 */
	private TableRow generateTableRow(String key, String value, int color) {
		// Generate row layout
		TableRow row = new TableRow(mContext);
		row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		// Add the key to the layout
		LinearLayout ll_key = new LinearLayout(mContext);
		TextView tv_key = new TextView(mContext);
		tv_key.setText(key);
		tv_key.setPadding(15, 0, 0, 0);
		tv_key.setTextColor(color);
		
		ll_key.addView(tv_key);
		ll_key.setOrientation(LinearLayout.VERTICAL);
		ll_key.setWeightSum(1.0f);
		row.addView(ll_key);
		
		// Add the value to the layout
		LinearLayout ll_value = new LinearLayout(mContext);
		TextView tv_value = new TextView(mContext);
		tv_value.setText(value);
		tv_value.setPadding(0, 0, 15, 0);
		tv_value.setTextColor(color);
		tv_value.setGravity(Gravity.RIGHT);
		
		ll_value.addView(tv_value);
		ll_value.setOrientation(LinearLayout.VERTICAL);
		ll_value.setWeightSum(1.0f);
		row.addView(ll_value);
		
		return row;
	}
	
	/**
	 * http://stackoverflow.com/a/24261119/1470496
	 * @param color
	 * @return
	 */
	public boolean isColorDark(int color){
	    double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
	    if(darkness<0.5){
	        return false; // It's a light color
	    }else{
	        return true; // It's a dark color
	    }
	}
}