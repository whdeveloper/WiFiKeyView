package com.whd.wifikeyview.network;

import com.whd.wifikeyview.network.NetworkParser.Network;

public interface NetworkListener {
	/**
	 * This method will return after the NetworkParseTask has finished
	 * 
	 * @param network
	 * 		The {@link Network} searched for or <b>null</b> if not found
	 */
	public void onParserDone(Network network);
}