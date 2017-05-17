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

/**
 * 
 * Holds information of a TCP packet that arrived on the destination.
 * 
 * @since 1.0.0
 */

public class DelayNode {

    /**
     * Holds the transmission time of a given packet.
     */
    private double txTime;
    
    /**
     * Holds the delay between the transmission and the receivement.
     */
    private double delay;
    
    /**
     * Holds the sequence number.
     */
    private long seqNumber;
    
    /**
     * Holds the ack number
     */
    private long ack;
 

    /**
     * @param txTime time of transmission
     * @param delay delay of the given packet
     * @param seqNumber sequence number
     * @param ack ack number
     */
    public DelayNode(double txTime, double delay, long seqNumber, long ack) {
        this.txTime = txTime;
        this.delay = delay;
        this.seqNumber = seqNumber;
        this.ack = ack;
    }

    @Override
    public String toString() {
        return Analyzer.lineIndex+ " DelayNode{" + "txTime=" + txTime + ", delay=" + delay + ", seqNumber=" + seqNumber + ", ack=" + ack + '}';
    }

    /**
     * @return the txTime
     */
    public double getTxTime() {
        return txTime;
    }

    /**
     * @param txTime the txTime to set
     */
    public void setTxTime(double txTime) {
        this.txTime = txTime;
    }

    /**
     * @return the delay
     */
    public double getDelay() {
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(double delay) {
        this.delay = delay;
    }

    /**
     * @return the seqNumber
     */
    public long getSeqNumber() {
        return seqNumber;
    }

    /**
     * @param seqNumber the seqNumber to set
     */
    public void setSeqNumber(long seqNumber) {
        this.seqNumber = seqNumber;
    }

    /**
     * @return the ack
     */
    public long getAck() {
        return ack;
    }



}
