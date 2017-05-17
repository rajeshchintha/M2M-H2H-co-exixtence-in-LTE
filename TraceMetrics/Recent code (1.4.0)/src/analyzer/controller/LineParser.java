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

package analyzer.controller;

import analyzer.recoverable_exceptions.NoLengthProtocolException;
import analyzer.unrecoverable_exceptions.InvalidTraceLineException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * This class has methods that takes parts or all the trace line and
 * extracts informations from it.
 *
 * @since 1.0.0
 */
public class LineParser {

    /**
     * Takes the current trace line and verify if it has the correct
     * informations about the operation and time fields.
     *
     * @param fileLine current trace line
     * @return true if the operation and time fields are correct and false
     * otherwise.
     * @throws InvalidTraceLineException
     */
    public boolean validateLine(String fileLine) throws InvalidTraceLineException {

        String[] fields = fileLine.split(" ");
        char operation = fields[0].charAt(0);

        if (operation != '+' && operation != '-' && operation != 'r' && operation != 'd' && operation != 't') {

            String msg = "Invalid operation field: " + operation;

            throw new InvalidTraceLineException(msg, fileLine, Analyzer.lineIndex);

        } else {

            try {

                Double.parseDouble(fields[1]);

            } catch (NumberFormatException nfe) {

                String msg = "Invalid time field: " + nfe.getMessage();

                throw new InvalidTraceLineException(msg, fileLine, Analyzer.lineIndex);

            }

        }

        return true;

    }

    /**
     * Takes a trace line and separate it based on the headers
     *
     * @param line
     * @return index 0 has the list of headers. index 1 has the list of the
     * header's contents
     */
    public ArrayList<String>[] separateHeaders(String line) {

        ArrayList[] ret = new ArrayList[2];
        ArrayList<String> headers = new ArrayList<String>();
        ArrayList<String> headersContent = new ArrayList<String>();
        String auxiliar[];
        auxiliar = line.split("ns3::");

        for (int i = 1; i < auxiliar.length; i++) {       /*
             * i = 1 because 1st isnt a header
             */

            String header = "";
            int j = 0;

            for (; auxiliar[i].charAt(j) != ' ' && auxiliar[i].charAt(j) != '('; j++) {

                header += "" + auxiliar[i].charAt(j);
            }

            headers.add(header);
            String headerCont = "";

            for (; j < auxiliar[i].length(); j++) {

                headerCont += "" + auxiliar[i].charAt(j);

            }

            headersContent.add(headerCont);

        }

        ret[0] = headers;
        ret[1] = headersContent;

        return ret;

    }

    /**
     *
     * @param str trace line splitted on ' ' (space)
     * @return the current operation
     */
    public char getOperation(String[] str) {

        return str[0].charAt(0);

    }

    /**
     *
     * @param str trace line splitted on ' ' (space)
     * @return the current trace time
     */
    public double getTime(String[] str) {

        return Double.parseDouble(str[1]);

    }

    /**
     *
     * @param str trace line splitted on ' ' (space).
     * @return the node number where the current event happened.
     */
    public int getNodeNumber(String[] str) {

        try {

            String A[] = str[2].split("/");
            return Integer.parseInt(A[2]);

        } catch (Exception e) {

            Analyzer.oneNodeTrace = true;
            return 0;

        }

    }

	/**
	 *
	 * @param str
	 * @return
	 */
	public int getDevice(String[] str) {
        try {

            String A[] = str[2].split("/");
            return Integer.parseInt(A[4]);

        } catch (Exception e) {

            return -1;

        }
    }

	/**
	 *
	 * @param headers
	 * @param headersContent
	 * @return
	 */
	public int getIpv4Id(ArrayList<String> headers, ArrayList<String> headersContent) {
        int index;
        int id = -1;

		for (index = 0; index < headers.size() && !headers.get(index).matches("TcpHeader"); index++);

        Pattern p = Pattern.compile(" id ");
        Matcher m = p.matcher(headersContent.get(index));

        if (m.find()) {
            id = Integer.parseInt(headersContent.get(index).substring(m.end()).split(" ")[0]);
        }

        return id;

    }

    /**
     *
     * @param headers the list with the header's names.
     * @param headersContent the list with the header's content.
     * @return the packet signature.
     */
    public String getPacketSignature(ArrayList<String> headers, ArrayList<String> headersContent) {

        String result = "";
        int transportIndex;
        for (transportIndex = 0; transportIndex < headers.size()
                && !headers.get(transportIndex).matches("Ipv4Header|Ipv6Header|ArpHeader"); transportIndex++);

        for (int i = transportIndex; i < headers.size(); i++) {

            result += headers.get(i) + " ";
            result += headersContent.get(i) + " ";

        }
        if (!result.isEmpty()) {
            result = result.substring(0, result.length() - 1);
        }

        Pattern p = Pattern.compile("ttl [0-9]* ");
        Matcher m = p.matcher(result);
        if (m.find()) {
            String aux = result.substring(0, m.start());
            aux += result.substring(m.end());
            result = aux;
        }

        return result;

    }

    /**
     *
     * @param headers the list with the header's names.
     * @param headersContent the list with the header's content.
     * @return 2 position array with src port and dest port.
     */
    public int[] getStreamPorts(ArrayList<String> headers, ArrayList<String> headersContent) {

        int ports[] = new int[2];
        String splitted[];
        String aux;
        String regexUdp = "[0-9]{1,5} > [0-9]{1,5}";
        Pattern p;
        Matcher m;

        for (int i = 0; i < headers.size(); i++) {

            if (headers.get(i).matches("TcpHeader|UdpHeader")) {

                p = Pattern.compile(regexUdp);
                m = p.matcher(headersContent.get(i));

                if (m.find()) {

                    aux = headersContent.get(i).substring(m.start(), m.end());
                    splitted = aux.split(" > ");
                    ports[0] = Integer.parseInt(splitted[0]);
                    ports[1] = Integer.parseInt(splitted[1]);
                }

                break;

            }

        }

        return ports;

    }

    /**
     * @param headers the list with the header's names.
     * @param headersContent the list with the header's content.
     * @return the payload value. 0 if there is no payload.
     */
    public int getPayload(ArrayList<String> headers, ArrayList<String> headersContent) {


        int payload;

        if (headers.contains("Ipv6Header")) {

            payload = getPayloadIpv6(headersContent.get(headers.indexOf("Ipv6Header")));

        } else {

            payload = getPayloadIpv4(headers, headersContent);

        }
        return payload;
    }
	
	/**
	 *
	 * @param headers
	 * @return
	 */
	public int getTCPOptions(ArrayList<String> headers) {

		double sum = 0;
		for(String s: headers){
			if(s.contains("TcpOption")){
				sum += Analyzer.noLengthHeaders.headers.get(s);
			}
		}
		double frac = sum % 4;
		
		//including padding
		if(frac != 0.0){
			sum += 4-frac;
		}
		
		return (int)sum;
    }

    /**
     * Method to get the payload from Ipv4 header.
     *
     * @param headers the list with the header's names.
     * @param headersContent the list with the header's content.
     * @return the payload value. 0 if there is no payload.
     */
    public int getPayloadIpv4(ArrayList<String> headers, ArrayList<String> headersContent) {

        Pattern pattern = Pattern.compile("Payload Fragment ");
        Pattern pattern2 = Pattern.compile("Payload ");
        Matcher m1;
        Matcher m2;
        String partial = "";
        String total = "";
        String simplePayload = "";
        int payloadFrag = 0;
        int payload = 0;
        int index = -1;
        int deslocamento = 0;
        boolean foundFragPayload = false;
        boolean foundSimplePayload = false;
        boolean foundPayload = false;

        if (headers.get(headers.size() - 1).matches("EthernetTrailer|WifiMacTrailer")) {
            deslocamento = 1;
        }

        for (int i = headersContent.size() - 1 - deslocamento; i >= 0 && !foundPayload; i--) {

            m1 = pattern.matcher(headersContent.get(i));

            while (foundFragPayload = m1.find()) {
                // has 'Payload Fragment'

                partial = "";
                total = "";
                for (index = m1.end() + 1; headersContent.get(i).charAt(index) != ':'; index++) {

                    partial += headersContent.get(i).charAt(index);

                }

                for (index++; headersContent.get(i).charAt(index) != ']'; index++) {

                    total += headersContent.get(i).charAt(index);
                }


                payloadFrag += Integer.parseInt(total) - Integer.parseInt(partial);

            }

            m2 = pattern2.matcher(headersContent.get(i));

            while ((foundSimplePayload = m2.find()) && (m2.start() > index)) {
                // has 'Payload'

                /*
                 * Payload (size=512) regex cant have '(', so +6
                 */
                index = m2.end() + 6;

                simplePayload = "";
                for (; headersContent.get(i).charAt(index) != ')'; index++) {

                    simplePayload += headersContent.get(i).charAt(index);

                }

                payload += Integer.parseInt(simplePayload);

            }

            foundPayload = (!foundFragPayload || !foundSimplePayload);

        }

        return payload + payloadFrag;

    }

    /**
     *
     * @param str the fields of Ipv6 header.
     * @return the payload value. 0 if there is no payload.
     */
    private int getPayloadIpv6(String str) {

        int payload;
        StringBuilder strPayload = new StringBuilder();
        Pattern pattern = Pattern.compile("Payload Length ");
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {

            for (int i = matcher.end(); i
                    < str.length() && str.charAt(i) != ' '; i++) {

                strPayload.append(str.charAt(i));

            }

        }

        payload = Integer.parseInt(new String(strPayload));

        return payload;

    }

    /**
     *
     * @param headers the list with the header's names.
     * @param headersContent the list with the header's content.
     * @return 2 position array with src ip and dest ip.
     */
    public String[] getIps(ArrayList<String> headers, ArrayList<String> headersContent) {

        int index;
        String[] result = null;

        headers.indexOf(this);
        if ((index = headers.indexOf("Ipv4Header")) != -1) {
            result = getIpv4(headersContent.get(index));
        } else if ((index = headers.indexOf("Ipv6Header")) != -1) {
            result = getIpv6(headersContent.get(index));
        }

        return result;

    }

    /**
     *
     * @param content ipv4 content
     * @return the 2 ip addresses
     */
    private String[] getIpv4(String content) {

        String[] ips = new String[2];
        String ipv4Regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        boolean found;
        Pattern pattern = Pattern.compile(ipv4Regex);
        Matcher ma = pattern.matcher(content);
        found = ma.find();

        if (found) {
            ips[0] = content.substring(ma.start(), ma.end());
            ma.find();
            ips[1] = content.substring(ma.start(), ma.end());
        }

        return ips;
    }

    /**
     *
     * @param content the ipv6 content
     * @return the 2 ip addresses
     */
    private String[] getIpv6(String content) {

        String ips[] = new String[2];
        String ipv6Regex = "([((A-F)|(a-f))0-9]{4}:?){8}";
        boolean found;

		Pattern pattern = Pattern.compile(ipv6Regex);
        Matcher ma = pattern.matcher(content);
		found = ma.find();

        if (found) {
			ips[0] = content.substring(ma.start(), ma.end());
			ma.find();
			ips[1] = content.substring(ma.start(), ma.end());
        }

        return ips;
    }

    /**
     *
     * @param headers the list with the header's names.
     * @param headersContent the list with the header's content.
     * @return the packet length
     */
    public int getPackLength(ArrayList<String> headers, ArrayList<String> headersContent) {

        Pattern pattern = Pattern.compile("length: |Length ");
        Matcher matcher;
        int index;
        int length = 0;
        int sumNoLength = 0;
        boolean found = false;
        boolean foundWithLength = false;

        for (index = 0; index < headersContent.size() && !found; index++) {

            matcher = pattern.matcher(headersContent.get(index));

            if (matcher.find()) {

                length = getLength(headersContent.get(index), matcher.end());
                found = true;
                foundWithLength = true;

            } else {

                if(headers.get(index).matches("Device|WimaxNetDevice")){
                    try {

                        sumNoLength += compProtocol(headers.get(index));

                    } catch (NoLengthProtocolException nlex) {

                        nlex.setContent(headersContent.get(index));
                        Analyzer.recoverableExceptions.add(nlex);

                        if (Analyzer.isShowExceptionMessages()) {

                            if (javax.swing.JOptionPane.showConfirmDialog(null, nlex.getMessage() + "\n" + nlex.getContent() + "\n"
                                    + "If you click 'OK', we will not show any more alerts for this problem.",
                                    "How should we proceed?", 2) == 0) {
                                Analyzer.setStopBecauseOfNoLengthProto(false);
                            } else {
                                System.exit(1);
                            }

                        }

                        if (Analyzer.isStopBecauseOfNoLengthProto()) {

                            System.exit(1);

                        }

                    }
                }
            }
        }

		//get payload sizes for IPv4 and IPv6
        if (!foundWithLength) {
            sumNoLength += getPayload(headers, headersContent);
            sumNoLength += getTCPOptions(headers);
        }

        if (headers.get(0).contains("PointToPoint")) {
            if (Analyzer.noLengthHeaders.headers.get("PppHeader") != null) {
                length += Analyzer.noLengthHeaders.headers.get("PppHeader");
            }
        } else if (headers.get(0).contains("Wifi")) {

            length += getWifiMacLength(headers, headersContent);

        }

        return length + sumNoLength;

    }

    /**
     *
     * @param str a header's content
     * @param pos starting position of the length field
     * @return the length
     */
    private int getLength(String str, int pos) {

        String tamanho = "";

        for (; str.charAt(pos) != ' '; pos++) {

            tamanho += str.charAt(pos);

        }

        return Integer.parseInt(tamanho);

    }

    /**
     * Used to get the lengths of protocols without length field.
     *
     * @param protocol the protocol name
     * @return the protocol length
     * @throws NoLengthProtocolException
     */
    public int compProtocol(String protocol) throws NoLengthProtocolException {

        Integer result = Analyzer.noLengthHeaders.headers.get(protocol);

        if (result == null) {

            if (protocol.equals("MgtProbeResponseHeader")) {
                return getMgtHeaderLength(protocol);
            } else if (protocol.equals("MgtAssocRequestHeader")) {
                return getMgtHeaderLength(protocol);
            } else if (protocol.equals("MgtProbeRequestHeader")) {
                return getMgtHeaderLength(protocol);
            } else if (protocol.equals("MgtAssocResponseHeader")) {
                return getMgtHeaderLength(protocol);
            } else if (!protocol.matches("Ipv4L3Protocol.*")) {

                if (!Analyzer.hasNoLengthProtErr) {

                    Analyzer.hasNoLengthProtErr = true;

                    throw new NoLengthProtocolException(protocol);

                }
            }
            return 0;
        } else {
            return result;
        }

    }

    /**
     *
     * @param headers the list with the header's names.
     * @param headersContent the list with the header's content.
     * @return the position right before the IcmpDestinationUnreachableHeader
     */
    public int existsIcmpDestinationUnreachableHeader(ArrayList<String> headers, ArrayList<String> headersContent) {

        Pattern icmpHeader = Pattern.compile("DestinationUnreachable");

        for (int i = 0; i < headers.size(); i++) {

            Matcher m = icmpHeader.matcher(headers.get(i));

            if (m.find()) {

                return i - 1;

            }
        }

        return headersContent.size() - 1;

    }

    // trows seqnumber exception
    /**
     *
     * @param headers the list with the header's names.
     * @param headersContent the list with the header's content.
     * @return the packet sequence number
     */
    public long getPackSeqNumber(ArrayList<String> headers, ArrayList<String> headersContent) {

        long seqNumber;
        String seqNumberStr = "";
        String regex = " Seq=";
        boolean found = false;
        Pattern pattern = Pattern.compile(regex);
        Matcher m = null;

        for (int i = 0; i < headers.size() && !found; i++) {

            if (headers.get(i).equals("TcpHeader")) {

                m = pattern.matcher(headersContent.get(i));

                if (m.find()) {

                    for (int j = m.end(); headersContent.get(i).charAt(j) != ' '; j++) {

                        seqNumberStr += headersContent.get(i).charAt(j);

                    }

                    found = true;

                }

            }

        }

        seqNumber = Long.parseLong(seqNumberStr);

        return seqNumber;

    }

    /**
     *
     * @param headers the list with the header's names.
     * @param headersContent the list with the header's content.
     * @return the packet ack number
     */
    public long getPackAckNumber(ArrayList<String> headers, ArrayList<String> headersContent) {

        int index = headers.indexOf("TcpHeader");
        int i = 0;
        long ack = -1;
        String regex = " Ack=";

        if (index != -1) {
            Pattern pattern = Pattern.compile(regex);
            Matcher m = pattern.matcher(headersContent.get(index));

            if (m.find()) {
                for (i = m.end(); i < headersContent.get(index).length() && headersContent.get(index).charAt(i) != ' '; i++) {
                }
                ack = Long.parseLong(headersContent.get(index).substring(m.end(), i));
            }
        }
        return ack;

    }

    /**
     *
     * @param line trace line
     * @return boolean value
     */
    public boolean isPpp(String line) {

        return line.contains("PointToPointNetDevice");

    }

    /**
     *
     * @param line trace line
     * @return boolean value
     */
    public boolean isWifi(String line) {

        return line.contains("Wifi");

    }

    /**
     *
     * @param headers the list with the header's names.
     * @param headersContent the list with the header's content.
     * @return the length
     */
    public double getWifiMacLength(ArrayList<String> headers, ArrayList<String> headersContent) {

        double result = 0;
        int i;
        for (i = 0; i < headers.size() && !headers.contains("WifiMacHeader"); i++);

        String splitted[] = headersContent.get(i).split(" ");
        if (splitted.length > 1) {
            String aux = splitted[1].substring(1);

            if (aux.contains("MGT")) {
                aux = "MGT";
            }

            result = Analyzer.noLengthHeaders.headers.get("WifiMacHeader" + aux);
        }
        return result;

    }

    /**
     *
     * @param headers the list with the header's names.
     * @param headersContent the list with the header's content.
     * @return an array with the Tcp flags
     */
    public String[] getFlags(ArrayList<String> headers, ArrayList<String> headersContent) {

        String flags;
        String flagsSplitted[] = null;

        Pattern patternStart = Pattern.compile("\\[");
        Pattern patternEnd = Pattern.compile("\\]");

        int i = headers.indexOf("TcpHeader");

        Matcher ma1 = patternStart.matcher(headersContent.get(i));
        Matcher ma2 = patternEnd.matcher(headersContent.get(i));

        if (ma1.find() && ma2.find()) {

            flags = headersContent.get(i).substring(ma1.start(), ma2.end());
            flagsSplitted = flags.split("[ |  | ]");

        }

        return flagsSplitted;

    }

    /**
     *
     * @param content the MgtHeader content
     * @return the length
     */
    public int getMgtHeaderLength(String content) {

        int size = 0;
        String split[] = content.split(", ");
        for (int i = 0; i < split.length; i++) {
            if (split[i].contains("ssid")) {
                String ssid = split[i].split("=")[1];
                size += ssid.length(); // see http://standards.ieee.org/getieee802/download/802.11-2007.pdf - 7.3.2.1
            } else if (split[i].contains("rates")) {
                String rates[] = split[1].split("\\[| |\\]");
                size += rates.length - 2; // see http://standards.ieee.org/getieee802/download/802.11-2007.pdf - 7.3.2.2
            } else if (split[i].contains("status code")) {
                size += 2;  // see http://standards.ieee.org/getieee802/download/802.11-2007.pdf - 7.3.1.9
            }

        }

        return size;

    }
}
