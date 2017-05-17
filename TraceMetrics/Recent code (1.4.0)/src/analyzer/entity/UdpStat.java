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
 * Class created to ease the return of probability calculations on UDP streams 
 * to a GUI class.
 * 
 * @since 1.0.0
 */

public class UdpStat {
    
    private int num;
    private int qtt;
    private double probability;

    /**
     * 
     * @param num dropped packets
     * @param qtt times that 'num' was dropped
     * @param percentage percentage
     */
    public UdpStat(int num, int qtt, double percentage) {
        this.num = num;
        this.qtt = qtt;
        this.probability = percentage;
    }

    /**
     * @return the num
     */
    public int getNum() {
        return num;
    }

    /**
     * @param num the num to set
     */
    public void setNum(int num) {
        this.num = num;
    }

    /**
     * @return the qtt
     */
    public int getQtt() {
        return qtt;
    }

    /**
     * @param qtt the qtt to set
     */
    public void setQtt(int qtt) {
        this.qtt = qtt;
    }

    /**
     * @return the probability
     */
    public double getProbability() {
        return probability;
    }

    /**
     * @param probability the probability to set
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }



}
