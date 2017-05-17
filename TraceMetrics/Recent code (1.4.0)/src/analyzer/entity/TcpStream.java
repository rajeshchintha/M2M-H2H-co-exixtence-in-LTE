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

import java.util.ArrayList;
import java.util.Arrays;


/**
 * 
 * Stores information about all packets belonging to a TCP stream and 
 * information about the source and destination of the stream. Here is the
 * calculation of Delay, PDV and IPDV.
 * 
 * @since 1.0.0
 */
public class TcpStream {

    /**
     * The source node number
     */
    private int senderNumber;
    /**
     * The source port
     */
    private int srcPort;
    /**
     * The destination port
     */
    private int destPort;
    /**
     * Array with te PDV measure
     */
    private double[] pdv;
    /**
     * Array with the IPDV measure
     */
    private double[] ipdv;
    /**
     * The source IP
     */
    private String ipSrc;
    /**
     * The destination IP
     */
    private String ipDest;
    /**
     * List of the transmission times
     */
    public ArrayList<TcpSegment> sendTime;
    /**
     * List of the delay of packets
     */
    public ArrayList<DelayNode> delay;

    public TcpStream(String ipSrc, String ipDest, int senderNumber, int srcPort, int destPort) {

        this.ipSrc = ipSrc;
        this.ipDest = ipDest;
        this.sendTime = new ArrayList<TcpSegment>();
        this.delay = new ArrayList<DelayNode>();
        this.senderNumber = senderNumber;
        this.srcPort = srcPort;
        this.destPort = destPort;

    }

    /**
     * Adds a position in the sendTime array with the packet details.
     *
     * @param sendTime trace time of packet transmission
     * @param packetSecNumber sequence number of the packet
     * @param flags flags of the packet
     * @param payload size of packet payload
     * @param ack number of ack
     * @param signature the packet signature
     */
    public void setSendTime(double sendTime, long packetSecNumber, String[] flags, int payload, long ack, String signature) {

        int i = 0;

        if (this.getSendTime().size() > 0) {



            while ((i < this.getSendTime().size()) && this.getSendTime().get(i).getTime() < sendTime) {
                i++;
            }

            // Put in the last position
            if (i >= this.getSendTime().size()) {
                this.getSendTime().add(new TcpSegment(packetSecNumber, sendTime, flags, payload, ack, signature));
            } else {

                // Packet retransmission, refresh the transmission time
                if (this.getSendTime().get(i).getSignature().equals(signature)) {

                    if ((this.getSendTime().get(i).getTime()) > sendTime) {

                        this.getSendTime().get(i).setTime(sendTime);

                    }

                    // Put packet at position i
                } else {

                    this.getSendTime().add(i, new TcpSegment(packetSecNumber, sendTime, flags, payload, ack, signature));

                }
            }
            // First packet
        } else {

            this.getSendTime().add(new TcpSegment(packetSecNumber, sendTime, flags, payload, ack, signature));

        }

    }

    /**
     *
     * @param packetSecNumber packet sequence number
     * @return time of transmission of the given packet
     */
    public double getSendTime(int packetSecNumber) {

        double result = -1.0;

        for (int i = 0; i < getSendTime().size(); i++) {

            if (this.getSendTime().get(i).getSeqNumber() == packetSecNumber) {
                result = this.getSendTime().get(i).getTime();
            }

        }

        return result;

    }

    /**
     *
     * @param recTime time of the drop event
     * @param packetSecNumber sequence number of the packet
     * @param flags flags of the packet
     * @param payload size of packet payload
     * @param ack number of ack
     * @param signature the packet signature
     */
    public void setDroppedPacket(double recTime, int packetSecNumber, String[] flags, int payload, int ack, String signature) {

        int i = 0;

        while ((i < this.getSendTime().size()) && (packetSecNumber != this.getSendTime().get(i).getSeqNumber())
                && !Arrays.equals(this.sendTime.get(i).getFlags(), flags)
                && this.getSendTime().get(i).getPayload() == payload
                && this.getSendTime().get(i).getAck() == ack
                && this.getSendTime().get(i).getSignature().equals(signature)) {
            i++;
        }

        if ((i == this.getSendTime().size() - 1) && (packetSecNumber == this.getSendTime().get(i).getSeqNumber())
                && Arrays.equals(this.sendTime.get(i).getFlags(), flags)
                && this.getSendTime().get(i).getPayload() == payload
                && this.getSendTime().get(i).getAck() == ack
                && this.getSendTime().get(i).getSignature().equals(signature)) {
            getSendTime().remove(i);
        }
    }

    /**
     *
     * @param recTime event time
     * @param ts object containing atributes of a tcp segment
     */
    public void setDroppedPacket(double recTime, TcpSegment ts) {

        int i = 0;

        while ((i < this.getSendTime().size()) && (ts.getSeqNumber() != this.getSendTime().get(i).getSeqNumber())
                && !Arrays.equals(this.sendTime.get(i).getFlags(), ts.getFlags())
                && this.getSendTime().get(i).getPayload() == ts.getPayload()
                && this.getSendTime().get(i).getAck() == ts.getAck()
                && this.getSendTime().get(i).getSignature().equals(ts.getSignature())) {
            i++;
        }

        if ((i < this.getSendTime().size() - 1) || ((i == this.getSendTime().size() - 1) && (ts.getSeqNumber() == this.getSendTime().get(i).getSeqNumber())
                && Arrays.equals(this.sendTime.get(i).getFlags(), ts.getFlags())
                && this.getSendTime().get(i).getPayload() == ts.getPayload()
                && this.getSendTime().get(i).getAck() == ts.getAck()
                && this.getSendTime().get(i).getSignature().equals(ts.getSignature()))) {
            getSendTime().remove(i);
        }

    }

    /**
     * Removes the packet from the sendTime list, calculates the delay and puts
     * the delay in the 'delay' array.
     *
     * @param recTime time of the receivement event
     * @param packetSecNumber sequence number of the packet
     * @param flags flags of the packet
     * @param payload size of packet payload
     * @param ack number of ack
     * @param signature the packet signature
     */
    public void setReceiveTime(double recTime, int packetSecNumber, String[] flags, int payload, int ack, String signature) {

        int i = 0;
        TcpSegment sn;

        while ((i < this.getSendTime().size()) && (packetSecNumber != this.getSendTime().get(i).getSeqNumber())
                && !Arrays.equals(this.sendTime.get(i).getFlags(), flags)
                && this.getSendTime().get(i).getPayload() == payload
                && this.getSendTime().get(i).getAck() == ack
                && this.getSendTime().get(i).getSignature().equals(signature)) {
            i++;
        }
		if(i < getSendTime().size()){
			sn = getSendTime().remove(i);
			delay.add(new DelayNode(sn.getTime(), recTime - sn.getTime(), sn.getSeqNumber(), sn.getAck()));
		}

    }

    /**
     * Overloaded method. Removes the packet from the sendTime list, calculates
     * the delay and puts the delay in the 'delay' array.
     *
     * @param recTime time of the receivement event
     * @param streamNode object containing the packet details
     */
    public void setReceivedTime(double recTime, TcpSegment streamNode) {

        int i = 0;
        TcpSegment sn;

        while ((i < this.getSendTime().size()) && (streamNode.getSeqNumber() != this.getSendTime().get(i).getSeqNumber())
                && !Arrays.equals(this.sendTime.get(i).getFlags(), streamNode.getFlags())
                && this.getSendTime().get(i).getPayload() == streamNode.getPayload()
                && this.getSendTime().get(i).getAck() == streamNode.getAck()
                && this.getSendTime().get(i).getSignature().equals(streamNode.getSignature())) {
            i++;
        }
		
		if(i < getSendTime().size()){
			sn = getSendTime().remove(i);
			delay.add(new DelayNode(sn.getTime(), recTime - sn.getTime(), sn.getSeqNumber(), sn.getAck()));
		}

    }

    /**
     * @return the ipSrc
     */
    public String getIpSrc() {
        return ipSrc;
    }

    /**
     * @return the ipDest
     */
    public String getIpDest() {
        return ipDest;
    }

    /**
     * See RFC 5481 4.1
     */
    public void calculateIpdv() {

        ipdv = new double[delay.size()];
        ipdv[0] = Double.MAX_VALUE;

        for (int i = 1; i < getIPDV().length; i++) {

            if (delay.get(i).getDelay() != Double.MAX_VALUE && delay.get(i - 1).getDelay() != Double.MAX_VALUE) {

                ipdv[i] = delay.get(i).getDelay() - delay.get(i - 1).getDelay();

            } else {

                ipdv[i] = Double.MAX_VALUE;

            }

        }

    }

    /**
     * See RFC 5481 4.2
     */
    public void calculatePdv() {

        double minDelay = Double.MAX_VALUE;

        pdv = new double[delay.size()];

        for (int i = 0; i < getPDV().length; i++) {

            if (minDelay > delay.get(i).getDelay()) {

                minDelay = delay.get(i).getDelay();

            }

        }

        for (int i = 0; i < getPDV().length; i++) {

            if (delay.get(i).getDelay() != Double.MAX_VALUE) {

                pdv[i] = delay.get(i).getDelay() - minDelay;

            } else {

                pdv[i] = Double.MAX_VALUE;

            }

        }

    }

    /**
     * @return the delay array
     */
    public double[] getDelay() {

        double[] arrayD = new double[delay.size()];

        for (int i = 0; i < arrayD.length; i++) {

            arrayD[i] = delay.get(i).getDelay();

        }

        return arrayD;

    }

    /**
     * @return the senderNumber
     */
    public int getSenderNumber() {
        return senderNumber;
    }

    /**
     * Calculates the Delay variance.
     *
     * @return the calculated variance
     */
    public double getDelayVariance() {

        double variance = 0.0;
        double average = getAverage(delay);
        int length = delay.size();
        int dropped = 0;

        for (int i = 0; i < length; i++) {
            if (delay.get(i).getDelay() != Double.MAX_VALUE) {
                variance += Math.pow(delay.get(i).getDelay() - average, 2);
            } else {
                dropped++;
            }

        }

        if ((length - dropped - 1) > 0) {

            return variance / (length - dropped - 1);

        } else {
            return 0;
        }

    }

    /**
     * Calculates the PDV variance.
     *
     * @return the calculated variance
     */
    public double getPdvVariance() {

        double variance = 0.0;
        double average = getAverage(getPDV());
        int length = getPDV().length;
        int dropped = 0;

        for (int i = 0; i < length; i++) {
            if (getPDV()[i] != Double.MAX_VALUE) {
                variance += Math.pow(getPDV()[i] - average, 2);
            } else {
                dropped++;
            }

        }

        if ((length - dropped) > 0) {

            return variance / (length - dropped);

        } else {
            return 0;
        }

    }

    /**
     * Calculates the IPDV variance.
     *
     * @return the calculated variance
     */
    public double getIpdvVariance() {

        double variance = 0.0;
        double average = getAverage(getIPDV());
        int length = getIPDV().length;
        int dropped = 0;

        for (int i = 1; i < length; i++) {

            if (getIPDV()[i] != Double.MAX_VALUE) {
                variance += Math.pow(getIPDV()[i] - average, 2);
            } else {
                dropped++;
            }

        }

        if ((length - dropped - 1) > 0) {

            return variance / (length - dropped - 1);

        } else {
            return 0;
        }

    }

    /**
     * @return the average delay value
     */
    public double getAverageDelay() {
        return getAverage(delay);
    }

    /**
     * @return the average PDV value
     */
    public double getAveragePdv() {
        return getAverage(getPDV());
    }

    /**
     * @return the average IPDV value
     */
    public double getAverageIpdv() {
        return getAverage(getIPDV());
    }

    /**
     * @param array array to calculate the average value
     * @return the average value
     */
    public double getAverage(double[] array) {

        double average = 0.0;
        int length = array.length;
        int dropped = 0;

        for (int i = 0; i < length; i++) {

            if (array[i] != Double.MAX_VALUE) {
                average += array[i];
            } else {
                dropped++;
            }

        }

        average /= (length - dropped);

        return average;

    }

    /**
     * Overloaded method.
     *
     * @param array ArrayList to calculate the average value
     * @return the average value
     */
    public double getAverage(ArrayList<DelayNode> array) {

        double average = 0.0;
        int length = array.size();
        int dropped = 0;

        for (int i = 0; i < length; i++) {

            if (array.get(i).getDelay() != Double.MAX_VALUE) {
                average += array.get(i).getDelay();
            } else {
                dropped++;
            }

        }

        average /= (length - dropped);

        return average;

    }

    /**
     * Remove all the elements of the sendTime list.
     */
    public void finish() {

        while (!sendTime.isEmpty()) {
            getSendTime().remove(0);
        }

    }

    /**
     * @return the sendTime
     */
    public ArrayList<TcpSegment> getSendTime() {
        return sendTime;
    }

    /**
     * @return the pdv
     */
    public double[] getPDV() {
        return pdv;
    }

    /**
     * @return the ipdv
     */
    public double[] getIPDV() {
        return ipdv;
    }

    public String getDetails() {

        StringBuilder result = new StringBuilder();
        result.append("IPs: \t\t").append(ipSrc).append(" --> ").append(ipDest);
        result.append("\nPorts: \t\t").append(srcPort).append(" --> ").append(destPort);
        result.append("\nNumber of packets: \t").append(delay.size());
        result.append("\nAverage Delay: \t").append(getAverageDelay());
        result.append("\nDelay variance: \t").append(getDelayVariance());
        result.append("\nAverage PDV: \t").append(getAveragePdv());
        result.append("\nPDV variance: \t").append(getPdvVariance());
        result.append("\nAverage IPDV: \t").append(getAverageIpdv());
        result.append("\nIPDV variance: \t").append(getIpdvVariance());

        return result.toString();

    }
}
