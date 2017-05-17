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
 * Performs tha calculation of the E[W] component of Little's Result (Queueing 
 * Theory)
 * 
 * @since 1.0.0
 */
public strictfp class EW {

    /**
     * The number of packets in the system
     */
    private int reqCount = 0;
    /**
     * The time of the last event
     */
    private double lastTime = 0.0;
    /**
     * The current calculated area
     */
    private double sumArea = 0.0;

    /**
     *
     * @param trace_time the time of the enqueue event if it is EWAlpha or dequeued event if it is EWBeta.
     */
    public void gotPack(double trace_time) {

        sumArea += (trace_time - getLastTime()) * getReqCount();
        lastTime = trace_time;
        reqCount++;

    }
   
    /**
     * Finish the calculations.
     * @param trace_time last time of trace
     */
    public void finalizes(double trace_time) {

        sumArea += (trace_time - getLastTime()) * getReqCount();

    }

    /**
     * @return the somaArea
     */
    public double getSumArea() {
        return sumArea;
    }

    /**
     * @return the reqCount
     */
    public int getReqCount() {
        return reqCount;
    }

    /**
     * @return the lastTime
     */
    public double getLastTime() {
        return lastTime;
    }
}