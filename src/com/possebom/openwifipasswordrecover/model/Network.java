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

package com.possebom.openwifipasswordrecover.model;

/**
 * Created by alexandre on 17/02/14.
 */

public class Network implements Comparable<Network> {
    private String ssid = "";
    private String password = "";
    private String type = "";
    private boolean connected = false;

    public final void setConnected(final boolean connected) {
        this.connected = connected;
    }

    public final boolean isConnected() {
        return connected;
    }

    public final String getSsid() {
        return ssid;
    }

    public final void setSsid(final String ssid) {
        this.ssid = ssid;
    }

    public final String getPassword() {
        return password;
    }

    public final void setPassword(final String password) {
        this.password = password;
    }

    public final String getType() {
        return type;
    }

    public final void setType(final String type) {
        this.type = type;
    }

    public Network(final String ssid, final String password, final String type) {
        this.ssid = ssid;
        this.password = password;
        this.type = type;
    }

    @Override
    public final int compareTo(final Network o) {
        if (o == null) {
            return -1;
        }
        return ssid.compareToIgnoreCase(o.ssid);
    }

    @Override
    public final String toString() {
        return String.format("ssid : %s\npass : %s\ntype : %s", ssid, password, type);
    }

    @Override
    public final boolean equals(final Object o) {
        boolean ret = false;
        if (o != null) {
            ret = o.toString().equals(toString());
        }
        return ret;
    }
}