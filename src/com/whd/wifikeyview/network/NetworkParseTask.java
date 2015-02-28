package com.whd.wifikeyview.network;

import java.util.List;

import com.whd.wifikeyview.WiFiKeyView;
import com.whd.wifikeyview.network.NetworkParser.Network;
import com.whd.wifikeyview.network.NetworkParser.SupplicantKey;

import android.os.AsyncTask;

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
		for (Network network : networks) {
			if (WiFiKeyView.isDebugging()) {
				WiFiKeyView.log("" + network.get(SupplicantKey.SSID) + " =?= " + params[0]);
			}
			
			if (network.get(SupplicantKey.SSID).equals(params[0])) {
				ret = network;
				break;
			}
		}
		
		return ret;
	}
	
	@Override
	protected void onPostExecute(Network network) {
		mListener.onParserDone(network);
	}
	
}