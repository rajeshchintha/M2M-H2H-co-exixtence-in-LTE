/* This file is part of TraceMetrics
 * 
 * TraceMetrics is a trace file analyzer for Network Simulator 3 (www.nsnam.org).
 * The goal is to calculate useful metrics for research and performance measurement.
 * URL: www.tracemetrics.net
 * 
 * Copyright (C) 2012  Luiz Felipe Zafra Saggioro
 * Copyright (C) 2012  Flavio Barbieri Gonzaga
 * Copyright (C) 2012  Reuel Ramos Ribeiro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package analyzer.entity;

import java.util.HashMap;

/**
 * 
 * Class to store information about headers that does not have the 'length'
 * field in its header. Its stored in a HashMap in the format:
 * (protocol_name, length)
 * 
 * @since 1.0.0
 */

public class Headers {

    /**
     * The structure to store the Header and it's size.
     */
    public HashMap<String, Integer> headers;

    public Headers() {

        headers = new HashMap();
        inicializeHashMap();

    }

    /**
     * Puts the headers and it's sizes on the structure.
     */
    private void inicializeHashMap() {

        headers.put("ArpHeader", 28); // see rfc 826
        headers.put("PppHeader", 2); //see rfc 1661
        headers.put("EthernetHeader", 10); 
        headers.put("TcpHeader", 20); // see rfc 793
        
		headers.put("TcpOptionEnd", 0); // see rfc 793
        headers.put("TcpOptionNOP", 0); // see rfc 793
        headers.put("TcpOptionMSS", 4); // see rfc 793
        headers.put("TcpOptionWinScale", 4); // see rfc 7323
        headers.put("TcpOptionTS", 10); // see rfc 7323 TimeStamps
        
		headers.put("Icmpv6RS", 4); // see rfc 2461 4.1
        headers.put("Icmpv6RA", 16); // see rfc 2461 4.2
        headers.put("Icmpv6NS", 0);  // see rfc 2461 4.3
        headers.put("Icmpv6NA", 24); // see rfc 2461 4.4
        headers.put("Icmpv6OptionLinkLayerAddress", 0); // see rfc 2461 4.6.1
        headers.put("Ipv6ExtensionLooseRoutingHeader", 4); // see rfc 2460 4.4
        headers.put("Icmpv6Echo", 0); // see rfc 2463 2.1 - This header's size is already a separated payload with 4 bytes
        headers.put("Ipv6Header", 40); // see rfc 2460 3
        headers.put("Ipv6ExtensionFragmentHeader", 8); // see rfc 2460 4.5
        headers.put("LlcSnapHeader", 3); // see rfc 1483 4.1
        headers.put("Icmpv4Header", 16); // see rfc 792 page 4
        headers.put("Icmpv4DestinationUnreachable", 36);
        headers.put("WifiMacTrailer", 0);
        headers.put("AmsduSubframeHeader", 14); // see http://standards.ieee.org/getieee802/download/802.11n-2009.pdf
        headers.put("EthernetTrailer", 0);

        /* got from the file src/device/wifi/wifi-mac-header.c */
        headers.put("WifiMacHeader", 0);
        headers.put("WifiMacHeaderMGT", 24);
        headers.put("WifiMacHeaderCTL_RTS", 16);
        headers.put("WifiMacHeaderCTL_CTS", 10);
        headers.put("WifiMacHeaderCTL_ACK", 10);
        headers.put("WifiMacHeaderCTL_BACKREQ", 16);
        headers.put("WifiMacHeaderCTL_BACKRESP", 16);
        headers.put("WifiMacHeaderDATA", 30);
        headers.put("WifiMacHeaderQOSDATA", 30);

        
        headers.put("WifiActionHeader", 0);
        // src/wifi/model/wifi-header.cc
        headers.put("MgtAddBaRequestHeader", 7);
        headers.put("MgtAddBaResponseHeader", 7);
        headers.put("CtrlBAckRequestHeader", 4);
        headers.put("CtrlBAckResponseHeader", 4);

        // src/wimax/model/wimax-mac-header.cc
        headers.put("MacHeaderType", 0);
        
        // included in the following fields
        headers.put("ManagementMessageType", 0);
        
        // only the trace fields were counted
        headers.put("Dcd", 3);
        headers.put("Ucd", 6);
        headers.put("DlMap", 9);
        headers.put("UlMap", 8);
//        headers.put("MacHeaderType", 0);
//        headers.put("MacHeaderType", 0);
        
        
        headers.put("GenericMacHeader", 6); //IEEE 802.16-2009

//        /* special way to get the size. not here */
//        headers.put("MgtProbeResponseHeader", 0);
//        headers.put("MgtAssocRequestHeader", 0);
//        headers.put("MgtAssocResponseHeader", 0);
//        headers.put("MgtProbeRequestHeader", 0);




    }
}