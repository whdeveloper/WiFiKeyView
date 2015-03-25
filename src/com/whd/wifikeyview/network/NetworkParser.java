package com.whd.wifikeyview.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.whd.wifikeyview.R;
import com.whd.wifikeyview.WiFiKeyView;


import android.annotation.SuppressLint;
import eu.chainfire.libsuperuser.Shell;

public class NetworkParser {
	// The data blocks
	private static final String WIFI_BLOCK_START = "network={";
	private static final String WIFI_BLOCK_END 	= "}";
	
	// Supplicant files
	private static String[] passwordFiles = new String[] {
			"/data/misc/wifi/wpa_supplicant.conf",
			"/data/wifi/bcm_supp.conf",
			"/data/misc/wifi/wpa.conf"
	};
	
	// An list with all networks
	private static List<Network> networks;
	
	/**
	 * This will parse the 'wpa_supplicant.conf' file, construct in a separate thread and parse it.
	 * After parsing you can use it without threads.
	 */
	public static List<Network> getNetworks() {
		networks = new ArrayList<Network>();
		
		// Do we have access to the root shell?
		if (Shell.SU.available()) {
			
			// Try to read the file, if it returns empty, try another
			List<String> suResult = null;
			for (String passwordFile : passwordFiles) {
				suResult = Shell.SU.run("cat " + passwordFile);
				
				if (!suResult.isEmpty()) {
					break;
				}
			}
			
			if ( (suResult != null) && (!suResult.isEmpty()) ) {
				parse(suResult);
			} else {
				WiFiKeyView.verboseLog(NetworkParser.class, "getNetworks()", "Could not open a supplicant file");
			}
		} else {
			WiFiKeyView.verboseLog(NetworkParser.class, "getNetworks()", "Root shell was not available!");
		}
		
		return networks;
	}
	
	/**
	 * This method parses an String containing network={} from 'wpa_supplicant.conf'
	 */
	@SuppressLint("DefaultLocale")
	private static void parse(List<String> configurationFile) {
		Network network = null;
		
		for (String string : configurationFile) {
			// If we have no network object, read to first opening
			if (network == null) {
				if (string.startsWith(WIFI_BLOCK_START)) {
					network = new Network();
				} else {
					// Ignore this
					// Empty lines will come up here (and the variables from the start of the supplicant)
				}
			} else {
				// Are we at the end?
				if (string.startsWith(WIFI_BLOCK_END)) {
					networks.add(network);
					network = null;
				} else {
					// Find the equal sign for split of key and value
					int equalsChar = string.indexOf('=');
					
					// Find key and value
					String key = new String(string.substring(0, equalsChar).toUpperCase().trim());
					String value = new String(string.substring(equalsChar+1).replace("\"", "").trim());
					
					try {
						// Put the current key-value in the network holder,
						// or throw IllegalArgumentException if key does not exist
						network.put(SupplicantKey.valueOf(key), value);
					} catch (IllegalArgumentException iae) {
						WiFiKeyView.verboseLog(NetworkParser.class, "parse(List<String>)", "Unknown key in supplicant file: " + key);
					}
				}
			}
		}
	}
	
	/**
	 * @author WHD
	 *
	 * This class makes the rest of the source easier to read
	 */
	public static class Network extends HashMap<SupplicantKey, String> {
		private static final long serialVersionUID = 0L;
		
		public boolean equals(Object object) {
			return ((object instanceof String) && (equals((String) object)));
		}
		
		public boolean equals(String ssid) {
			return (get(SupplicantKey.SSID).equals(ssid));
		}
	}
	
	/**
	 * @author WHD
	 *
	 * This class contains the keys found in the supplicant files
	 */
	public enum SupplicantKey {
		SSID					(R.string.supplicant_key_ssid),
		BSSID					(R.string.supplicant_key_bssid),
		WEP_KEY0				(R.string.supplicant_key_wep_key0),
		WEP_KEY1				(R.string.supplicant_key_wep_key1),
		WEP_KEY2				(R.string.supplicant_key_wep_key2),
		WEP_KEY3				(R.string.supplicant_key_wep_key3),
		PSK						(R.string.supplicant_key_psk),
		PASSWORD				(R.string.supplicant_key_password),
		KEY_MGMT				(R.string.supplicant_key_key_mgmt),
		GROUP					(R.string.supplicant_key_group),
		AUTH_ALG				(R.string.supplicant_key_auth_alg),
		EAP						(R.string.supplicant_key_eap),
		IDENTITY				(R.string.supplicant_key_identity),
		ANOMYMOUS_IDENTITY		(R.string.supplicant_key_anonymous_identity),
		PHASE1					(R.string.supplicant_key_phase1),
		PHASE2					(R.string.supplicant_key_phase2),
		PRIORITY				(R.string.supplicant_key_priority),
		PAC_FILE				(R.string.supplicant_key_pac_file),
		CA_CERT					(R.string.supplicant_key_ca_cert),
		CLIENT_CERT				(R.string.supplicant_key_client_cert),
		PRIVATE_KEY				(R.string.supplicant_key_private_key),
		PRIVATE_KEY_PASSWORD	(R.string.supplicant_key_private_key_password);
		
		private int stringRes;
		
		private SupplicantKey(int stringRes) {
			this.stringRes = stringRes;
		}
		
		public int getString() {
			return stringRes;
		}
	}
	
}