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

import analyzer.controller.Analyzer;
import analyzer.controller.Utils;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * Has information about a node of the simulations.
 *
 * @since 1.0.0
 */
public class Node implements Comparable {

    /**
     * The node number on the trace.
     */
    private int num;
    /**
     * Number of packets sent over the wire.
     */
    private long sentPacksCount = 0;
    /**
     * Number of received packets.
     */
    private long recPacksCount = 0;
    /**
     * Number of dropped packets.
     */
    private long droppedPacksCount = 0;
    /**
     * Sum of size of packets sent over the wire.
     */
    private long sentLengthSum = 0;
    /**
     * Sum of payloads sent over the wire.
     */
    private long sentPayloadSum = 0;
    /**
     * Sum of size of packets received.
     */
    private long recLengthSum = 0;
    /**
     * Sum of size of packets dropped.
     */
    private long droppedLengthSum = 0;
    /**
     * Result of E[N] after the analizys is done.
     */
    private double enResult = 0.0;
    /**
     * Result of E[W] after the analizys is done.
     */
    private double ewResult = 0.0;
    /**
     * Result of Througput after the analizys is done.
     */
    private double throughput = 0.0;
    /**
     * Result of Goodput after the analizys is done.
     */
    private double goodput = 0.0;
    /**
     * The arrival rate. Calculated after the analizys is done.
     */
    private double arrivalRate = 0.0;
    /**
     * The Little's Result (E[N] = E[W] * arrivalRate).
     */
    private double littleResult[] = new double[2];
    /**
     * The IPDV measure. Calculated after the analizys is done.
     */
    private ArrayList<Double[]> ipdv;
    /**
     * The PDV measure. Calculated after the analizys is done.
     */
    private ArrayList<Double[]> pdv;
    /**
     * The E[N] object for Littl's result.
     */
    private EN en;
    /**
     * The E[W] Alpha object for Littl's result.
     */
    private EW ewAlpha;
    /**
     * The E[W] Beta object for Littl's result.
     */
    private EW ewBeta;

    public Node() {

        en = new EN();
        ewAlpha = new EW();
        ewBeta = new EW();

    }

    /**
     * @return the num
     */
    public int getNum() {
        return num;
    }

    /**
     * @return the sentPacksCount
     */
    public long getWiredSentPacksCount() {
        return sentPacksCount;
    }

    /**
     * @return the recPacksCount
     */
    public long getSentPacksCount() {
        return getWiredSentPacksCount();
    }

    /**
     * @return the nDroppedPacks
     */
    public long getDroppedPacksCount() {
        return droppedPacksCount;
    }

    /**
     * @return the enResult
     */
    public double getEnResult() {
        return enResult;
    }

    /**
     * @return the ewResult
     */
    public double getEwResult() {
        return ewResult;
    }

    /**
     * @return the throughput
     */
    public double getThroughput() {
        return throughput;
    }

    /**
     * @return the goodput
     */
    public double getGoodput() {
        return goodput;
    }

    /**
     * @return the arrivalRate
     */
    public double getArrivalRate() {
        return arrivalRate;
    }

    /**
     * @return the littleResult
     */
    public double[] getLittleResult() {
        return littleResult;
    }

    /**
     * @return the sentLengthSum
     */
    public long getSentLengthSum() {
        return getSentWiredLengthSum();
    }

    /**
     * @return the recLengthSum
     */
    public long getRecLengthSum() {
        return recLengthSum;
    }

    /**
     * @return the droppedLengthSum
     */
    public long getDroppedLengthSum() {
        return droppedLengthSum;
    }

    /**
     * @return the sentPayloadSum
     */
    public long getSentPayloadSum() {
        return getSentWiredPayloadSum();
    }

    /**
     *
     * @param index the tcp stream index
     * @return the PDV of the given tcp stream
     */
    public String getPDV(int index) {
        return pdv.get(index).toString();
    }

    /**
     *
     * @param index the tcp stream index
     * @return the IPDV of the given tcp stream
     */
    public String getIPDV(int index) {
        return ipdv.get(index).toString();
    }

    /**
     * @param num the num to set
     */
    public void setNum(int num) {
        this.num = num;
    }

    /**
     * Increments by 1 the number of wired sent packets;
     */
    public void incWiredSentPackCount() {

        this.sentPacksCount++;

    }

    /**
     * Increments by 1 the number of received packets;
     */
    public void incRecPackCount() {

        recPacksCount++;

    }

    /**
     * Increments by 1 the number of dropped packets;
     */
    public void incDroppedPackCount() {

        droppedPacksCount++;

    }

    /**
     * Increments the length sum by the given argument sent over the wire.
     *
     * @param length the payload size
     */
    public void incWiredSentLength(long length) {

        sentLengthSum += length;
        if (sentLengthSum < 0) {
            System.out.println(sentLengthSum);
            System.out.println(sentLengthSum - length);
            System.out.println("Line: " + Analyzer.lineIndex);

            System.out.println(Utils.convertMilisToTime(Calendar.getInstance().getTimeInMillis() - Analyzer.getInstance().start.getTimeInMillis(), false));
            System.exit(1);
        }

    }

    /**
     * Increments the length sum by the given argument received.
     *
     * @param length the payload size
     */
    public void incRecLength(long length) {

        recLengthSum += length;

    }

    /**
     * Increments the length sum by the given argument dropped.
     *
     * @param length the payload size
     */
    public void incDroppedLength(long length) {

        droppedLengthSum += length;

    }

    /**
     * Increments the payload sum by the given argument sent over the wire.
     *
     * @param payloadLength the payload size
     */
    public void incWiredSentPayloadSum(long payloadLength) {

        sentPayloadSum += payloadLength;

    }

    /**
     *
     * @param time enqueue time
     */
    public void gotPackEN(double time) {

        en.gotPack(time);

    }

    /**
     *
     * @param time dequeue time
     */
    public void sentPackEN(double time) {

        en.sentPack(time);

    }

    /**
     *
     * @param time enqueue time
     */
    public void gotPackEWAlfa(double time) {

        ewAlpha.gotPack(time);

    }

    /**
     *
     * @param time deuqeue time
     */
    public void gotPackEWBeta(double time) {

        ewBeta.gotPack(time);

    }

    /**
     * Finish all the calculations based on the last trace time.
     *
     * @param time last trace time
     */
    public void finishAndCalculate(double time) {

        en.finalizes(time);
        ewAlpha.finalizes(time);
        ewBeta.finalizes(time);

        throughput = getSentLengthSum() / time;
        goodput = getSentPayloadSum() / time;
        enResult = en.getSumArea() / time;
        if (getSentPacksCount() == 0) {
            ewResult = 0.0;
        } else {
            ewResult = (ewAlpha.getSumArea() - ewBeta.getSumArea()) / getWiredSentPacksCount();
        }
        arrivalRate = getSentPacksCount() / time;
        littleResult[0] = enResult;
        littleResult[1] = ewResult * arrivalRate;

    }

    @Override
    public int compareTo(Object o) {

        if (((Node) o).getNum() > this.getNum()) {
            return -1;
        } else if (((Node) o).getNum() < this.getNum()) {
            return 1;
        } else {
            return 0;
        }

    }

    /**
     * @return the sentWiredLengthSum
     */
    public long getSentWiredLengthSum() {
        return sentLengthSum;
    }

    /**
     * @return the sentWiredPayloadSum
     */
    public long getSentWiredPayloadSum() {
        return sentPayloadSum;
    }

    /**
     *
     * @return the EN object
     */
    public EN getEN() {
        return en;
    }

    /**
     *
     * @return the EWAlpha object
     */
    public EW getEWA() {
        return ewAlpha;
    }

    /**
     *
     * @return the EWBeta object
     */
    public EW getEWB() {
        return ewBeta;
    }

    /**
     *
     * @param printNode Flag used to indicate if the String returned will
     * contain the node number.
     */
    public String getDetails(boolean printNode) {

        StringBuilder result = new StringBuilder();

        double averageSizeWiredSent;
        double averageSizeRec;
        String sentData;
        String receivedData;
        String droppedData;

        if (getWiredSentPacksCount() == 0) {
            averageSizeWiredSent = 0.0;
        } else {
            averageSizeWiredSent = getSentLengthSum() / getWiredSentPacksCount();
        }


        if (getRecPacksCount() == 0) {
            averageSizeRec = 0.0;
        } else {
            averageSizeRec = getRecLengthSum() / getRecPacksCount();
        }

        sentData = Utils.convertData((double) getSentWiredLengthSum());
        receivedData = Utils.convertData(getRecLengthSum());
        droppedData = Utils.convertData(getDroppedLengthSum());

        if (printNode) {
            result.append("Node:\t\t ").append(getNum()).append("\n");
        }
        result.append("Sent packets: \t").append(getWiredSentPacksCount()).append("\n");
        result.append("Received packets: \t").append(getRecPacksCount()).append("\n");
        result.append("Dropped packets: \t").append(getDroppedPacksCount()).append("\n");
        result.append("Data sent: \t\t").append(sentData).append("\n");
        result.append("Data received: \t").append(receivedData).append("\n");
        result.append("Data dropped: \t").append(droppedData).append("\n");
        result.append("Throughput: \t\t").append(getThroughput()).append(" B").append("\n");
        result.append("Goodput: \t\t").append(getGoodput()).append(" B").append("\n");
        result.append("Lambda: \t\t").append(getArrivalRate()).append("\n");
        result.append("EN: \t\t").append(getEnResult()).append("\n");
        result.append("EW: \t\t").append(getEwResult()).append("\n");
        result.append("Little's result:\n");
        result.append("  -> EN:\t\t").append(enResult);
        result.append("\n  -> EW*lambda:\t").append(ewResult * arrivalRate).append("\n");
        result.append("Average length of:");
        result.append("\n  -> Sent packets: \t").append(averageSizeWiredSent).append(" B").append("\n");
        result.append("  -> Received packets: \t").append(averageSizeRec).append(" B").append("\n");


        return result.toString();
    }

    /**
     * @return the recPacksCount
     */
    public long getRecPacksCount() {
        return recPacksCount;
    }
}
