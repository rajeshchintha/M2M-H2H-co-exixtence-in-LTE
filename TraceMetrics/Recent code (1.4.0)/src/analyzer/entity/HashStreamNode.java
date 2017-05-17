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
 * 
 * Abstraction of information of TCP and UDP streams. Used as key to retrieve a
 * stream from the HashMap object that stores the streams.
 * 
 * @since 1.0.0
 */

public class HashStreamNode {

    /**
     * The source port.
     */
    private int portSrc;
    /**
     * The destination port.
     */
    private int portDest;
    /**
     * The source number,
     */
    private int nodeSrc;
    /**
     * The destination number,
     */
    private int nodeDest;
    /**
     * The number of sender node.
     */
    private int srcNumber;
    /**
     * The origin IP.
     */
    private String ipSrc;
    /**
     * The destination IP.
     */
    private String ipDest;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HashStreamNode other = (HashStreamNode) obj;
        if ((this.ipSrc == null) ? (other.ipSrc != null) : !this.ipSrc.equals(other.ipSrc)) {
            return false;
        }
        if ((this.ipDest == null) ? (other.ipDest != null) : !this.ipDest.equals(other.ipDest)) {
            return false;
        }
        if (this.portSrc != other.portSrc) {
            return false;
        }
        if (this.portDest != other.portDest) {
            return false;
        }
        if (this.getSrcNumber() != other.getSrcNumber()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.ipSrc != null ? this.ipSrc.hashCode() : 0);
        hash = 47 * hash + (this.ipDest != null ? this.ipDest.hashCode() : 0);
        hash = 47 * hash + this.portSrc;
        hash = 47 * hash + this.portDest;
        hash = 47 * hash + this.getSrcNumber();
        return hash;
    }
    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * @param ports 2 position array with the port values
     */
    public void setPorts(int[] ports) {
        this.portSrc = ports[0];
        this.portDest = ports[1];
    }
    
    /**
     * @param ips 2 position array with the ip values
     */
    public void setIps(String[] ips) {
        this.ipSrc = ips[0];
        this.ipDest = ips[1];
    }
    
    /**
     * @return the ipSrc
     */
    public String getIpSrc() {
        return ipSrc;
    }
    
    /**
     * @param ipSrc the ipSrc to set3
     */
    public void setIpSrc(String ipSrc) {
        this.ipSrc = ipSrc;
    }
    
    /**
     * @return the ipDest
     */
    public String getIpDest() {
        return ipDest;
    }
    
    /**
     * @param ipDest the ipDest to set
     */
    public void setIpDest(String ipDest) {
        this.ipDest = ipDest;
    }
    
    /**3
     * @return the portSrc
     */
    public int getPortSrc() {
        return portSrc;
    }
    
    /**
     * @param portSrc the portSrc to set
     */
    public void setPortSrc(int portSrc) {
        this.portSrc = portSrc;
    }
    
    /**
     * @return the portDest
     */
    public int getPortDest() {
        return portDest;
    }
    
    /**
     * @param portDest the portDest to set3
     */
    public void setPortDest(int portDest) {
        this.portDest = portDest;
    }
    
    /**
     * @return the nodeSrc
     */
    public int getNodeSrc() {
        return nodeSrc;
    }
    
    /**
     * @param nodeSrc the nodeSrc to set
     */
    public void setNodeSrc(int nodeSrc) {
        this.nodeSrc = nodeSrc;
    }
    
    /**
     * @return the nodeDest
     */
    public int getNodeDest() {
        return nodeDest;
    }
    
    /**
     * @param nodeDest the nodeDest to set
     */
    public void setNodeDest(int nodeDest) {
        this.nodeDest = nodeDest;
    }
    
    /**
     * @return the srcNumber
     */
    public int getSrcNumber() {
        return srcNumber;
    }
    
    /**
     * @param srcNumber the srcNumber to set
     */
    public void setSrcNumber(int srcNumber) {
        this.srcNumber = srcNumber;
    }
}
//</editor-fold>
