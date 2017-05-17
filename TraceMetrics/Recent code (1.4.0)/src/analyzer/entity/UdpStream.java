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

/**
 * 
 * Stores the information about drops of a UDP stream and the information about
 * the source and destination of the stream.
 * 
 * @since 1.0.0
 */
public class UdpStream {

    /**
     * Indicates if it's a new drop sequence
     */
    private boolean newDrop;
    /**
     * The source node number
     */
    private int senderNumber;
    /**
     * The source Ip
     */
    private String ipSrc;
    /**
     * The destination Ip
     */
    private String ipDest;
    /**
     * The object to count the drops
     */
    private DropCount dc;
    /**
     * List of the delay of packets
     */
    private int sentPackets;
    /**
     * List of the transmission times
     */
    private int receivedPackets;
    /**
     * The source port
     */
    private int srcPort;
    /**
     * The destination port
     */
    private int destPort;

    /**
     *
     * @param ipSrc source Ip
     * @param ipDest destination Ip
     */
    public UdpStream(String ipSrc, String ipDest, int srcPort, int destPort) {

        newDrop = true;
        this.ipSrc = ipSrc;
        this.ipDest = ipDest;
        this.srcPort = srcPort;
        this.destPort = destPort;
        this.dc = new DropCount();

    }

    /**
     * Sets a new drop sequence
     */
    public void setDrop() {

        getDc().setDrop(newDrop);

    }

    /**
     * @return the newDrop
     */
    public boolean isNewDrop() {
        return newDrop;
    }

    /**
     * @param newDrop the newDrop to set
     */
    public void setNewDrop(boolean newDrop) {
        this.newDrop = newDrop;
    }

    /**
     * @return the dc
     */
    public DropCount getDc() {
        return dc;
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
     * @return the senderNumber
     */
    public int getSenderNumber() {
        return senderNumber;
    }

    /**
     *
     * @param dropCount the drop counter object
     * @return the average dropped packets per sequence
     */
    private double calculateAverage(DropCount dropCount) {

        double sum = 0.0;
        for (int i = 0; i < dropCount.getSize(); i++) {
            sum += dropCount.getDroppedSequence().get(i);
        }
        double size = dropCount.getSize();
        return sum / size;

    }

    /**
     * Calls the calculateAverage method
     *
     * @return the average value
     */
    public double getAverage() {

        return calculateAverage(dc);

    }

    /**
     * Calculates the drop variance.
     *
     * @return the variance value
     */
    public double getDropVariance() {

        double variance = 0.0;
        double average = calculateAverage(dc);
        int length = dc.getSize();
        int dropped = 0;

        for (int i = 0; i < length; i++) {
            variance += Math.pow(dc.getDroppedSequence().get(i) - average, 2);
        }

        double dLength = length;

        return variance / dLength;

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

    public void setSent() {

        sentPackets++;

    }

    public void setReceived() {

        receivedPackets++;

    }

    public String getDetails() {

        StringBuilder result = new StringBuilder();
        double average;
        double variance;

        if (dc.getSize() == 0) {
            average = 0.0;
            variance = 0.0;
        } else {
            average = getAverage();
            variance = getDropVariance();
        }

        result.append("Ips: \t\t").append(ipSrc).append(" --> ").append(ipDest);
        result.append("\nPorts: \t\t").append(getSrcPort()).append(" --> ").append(getDestPort());
        result.append("\nNumber of:\n");
        result.append("  -> Sent packets: \t").append(getSentPackets());
        result.append("\n  -> Received packets: \t").append(getReceivedPackets());
        result.append("\n  -> Dropped packets: \t").append(dc.getCountDroppedPackets());
        result.append("\n  -> Drop sequences: \t").append(dc.getDroppedSequence().size());
        result.append("\nAverage drop: \t").append(average);
        result.append("\nDrop Variance: \t").append(variance);

        ArrayList<DropCount> a = new ArrayList<DropCount>();
        a.add(dc);

        return result.toString();
    }

    /**
     * @return the sentPackets
     */
    public int getSentPackets() {
        return sentPackets;
    }

    /**
     * @return the receivedPackets
     */
    public int getReceivedPackets() {
        return receivedPackets;
    }

    /**
     * @return the srcPort
     */
    public int getSrcPort() {
        return srcPort;
    }

    /**
     * @return the destPort
     */
    public int getDestPort() {
        return destPort;
    }
}
