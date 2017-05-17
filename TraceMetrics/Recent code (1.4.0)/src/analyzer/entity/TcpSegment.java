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

/**
 * Represents a segment using TCP and has informations belonging to TCP header.
 * 
 * @since 1.0.0
 */
public class TcpSegment {

    /**
     * The packet sequence number
     */
    private long seqNumber;
    /**
     * The time of send event
     */
    private double time;
    /**
     * The flags
     */
    private String[] flags;
    /**
     * The payload value
     */
    private int payload;
    /**
     * The ack value
     */
    private long ack;
    
    /**
     * The packet signature
     */
    private String signature;

    /**
     * 
     * @param seqNumber the sequence number
     * @param time the time of send event
     * @param flags the flags
     * @param payload the payload value
     * @param ack the ack number
     */
    public TcpSegment(long seqNumber, double time, String[] flags, int payload, long ack, String signature) {

        this.seqNumber = seqNumber;
        this.time = time;
        this.flags = flags;
        this.payload = payload;
        this.ack = ack;
        this.signature = signature;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TcpSegment other = (TcpSegment) obj;

        if (Double.doubleToLongBits(this.time) != Double.doubleToLongBits(other.time)) {
            return false;
        }

        if ((this.signature == null) ? (other.signature != null) : !this.signature.equals(other.signature)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.time) ^ (Double.doubleToLongBits(this.time) >>> 32));
        hash = 13 * hash + (this.signature != null ? this.signature.hashCode() : 0);
        return hash;
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
     * @return the time
     */
    public double getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(double time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "TcpStreamNode{" + "seqNumber=" + seqNumber + ", time=" + time + ", flags=" + flags + ", payload=" + payload + ", ack=" + ack + ", signature=" + signature + "\n}";
    }



    /**
     * @return the flags
     */
    public String[] getFlags() {
        return flags;
    }

    /**
     * @return the payload
     */
    public int getPayload() {
        return payload;
    }

    /**
     * @return the ack
     */
    public long getAck() {
        return ack;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    
    


}