package com.whd.wifikeyview.network;

import java.util.List;

import com.whd.wifikeyview.WiFiKeyView;
import com.whd.wifikeyview.network.NetworkParser.Network;
import com.whd.wifikeyview.network.NetworkParser.SupplicantKey;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author WHD
 *
 * This class will run the {@link NetworkParser} in the background and will call 
 * a {@link NetworkListener} when it is finished
 */
public class NetworkParseTask extends AsyncTask<String, Void, Network> {
	
	private NetworkListener mListener;
	
	/**
	 * Constructor
	 * 
	 * @param listener
	 * 		The {@link NetworkListener} that will be called when the task is finished
	 */
	public NetworkParseTask(NetworkListener listener) {
		if (listener == null) {
			throw (new NullPointerException("NetworkListener must not be null!"));
		}
		
		mListener = listener;
	}

	@Override
	protected Network doInBackground(String... params) {
		// We need only 1 argument because we will search for it in the supplicant files
		if (params.length < 1) {
			throw (new IllegalArgumentException("NetworkParseTask#doInBackground(String...) needs at least one argument!"));
		}
		
		Network ret = null;
		
		List<Network> networks = NetworkParser.getNetworks();
		
		int index = networks.indexOf(params[0]);
		if (index == -1) {
			Log.e("TAG", "Network " + params[0] + " could not be found!");
		} else {
			String pass = networks.get(index).get(SupplicantKey.PASSWORD);
			Log.e("TAG", "Network " + params[0] + " found! Password: " + pass);
		}
		
		for (Network network : networks) {
			if (WiFiKeyView.isDebugging()) {
				WiFiKeyView.verboseLog(this, "doInBackground(String...)", "" + network.get(SupplicantKey.SSID) + " =?= " + params[0]);
			}
			
			if (network.equals(params[0])) {
				ret = network;
				break;
			}
		}
		
		if ( (ret == null) && WiFiKeyView.isDebugging()) {
			WiFiKeyView.verboseLog(this, "doInBackground(String...)", "The network '" + params[0] + "' could not be found");
		}
		
		return ret;
	}
	
	@Override
	protected void onPostExecute(Network network) {
		mListener.onParserDone(network);
	}
	
}