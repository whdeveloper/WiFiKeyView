/*
 * Copyright Alexandre Possebom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.possebom.openwifipasswordrecover.parser;

import android.os.AsyncTask;

import com.possebom.openwifipasswordrecover.interfaces.NetworkListener;
import com.possebom.openwifipasswordrecover.model.Network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.chainfire.libsuperuser.Shell;


/**
 * Created by alexandre on 17/02/14.
 */
public class NetworkParser extends AsyncTask<Void, Void, List<Network>> {
	
    private final NetworkListener listener;

    public NetworkParser(final NetworkListener listener) {
        this.listener = listener;
    }

    @Override
    protected final void onPostExecute(List<Network> networkList) {
        super.onPostExecute(networkList);
        listener.onParserDone(networkList);
        System.err.println("TASK DONE");
    }

    @Override
    protected final List<Network> doInBackground(Void... params) {
        System.err.println("TASK STARTED");
    	final List<Network> listNetworks = new ArrayList<Network>();
        List<String> suResult;

        if (Shell.SU.available()) {

            suResult = Shell.SU.run("cat /data/misc/wifi/wpa_supplicant.conf");

            StringBuilder sb = new StringBuilder();
            if (suResult != null) for (String line : suResult) {
                sb.append(line);
            }

            final Pattern patternNetworks = Pattern.compile("network=\\{(.*?)\\}", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
            final Pattern patternWpa = Pattern.compile("ssid=\"(.*?)\".*psk=\"(.*?)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
            final Pattern patternWep = Pattern.compile("ssid=\"(.*?)\".*wep_key.=\"(.*?)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
            final Pattern patternOpen = Pattern.compile("ssid=\"(.*?)\".*key_mgmt.=\"NONE\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
            
            final Matcher matcherNetworks = patternNetworks.matcher(sb.toString());

            while (matcherNetworks.find()) {
                final String stringNetwork = matcherNetworks.group(1);
                final Matcher matcherWpa  = patternWpa .matcher(stringNetwork);
                final Matcher matcherWep  = patternWep .matcher(stringNetwork);
                final Matcher matcherOpen = patternOpen.matcher(stringNetwork);

                if (matcherWpa.find() && matcherWpa.groupCount() == 2) {
                    final Network network = new Network(matcherWpa.group(1), matcherWpa.group(2), "wpa");
                    listNetworks.add(network);
                } else if (matcherWep.find() && matcherWep.groupCount() == 2) {
                    final Network network = new Network(matcherWep.group(1), matcherWep.group(2), "wep");
                    listNetworks.add(network);
                } else if (matcherOpen.find() && matcherOpen.groupCount() == 1) {
                	final Network network = new Network(matcherOpen.group(1), "", "open");
                    listNetworks.add(network);
                }
            }

            Collections.sort(listNetworks);
        }
        
        System.err.println("TASK RETURN");
        return listNetworks;
    }

}