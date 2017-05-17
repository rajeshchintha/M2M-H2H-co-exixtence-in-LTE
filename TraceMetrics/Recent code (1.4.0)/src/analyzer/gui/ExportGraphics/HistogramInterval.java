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
package analyzer.gui.ExportGraphics;

import analyzer.recoverable_exceptions.hitogramIntervals.DecreasingIntervalNotAllowed;
import analyzer.recoverable_exceptions.hitogramIntervals.InconsistentInterval;
import analyzer.recoverable_exceptions.hitogramIntervals.IntervalTypeNotDefined;
import analyzer.recoverable_exceptions.hitogramIntervals.NotInitializedStreamInterval;
import java.util.HashMap;

/**
 * Object that store one interval to be used for histograms plots. Implements
 * useful methods for simplified analysis of a interval.
 *
 * @since 1.2.0
 */
public class HistogramInterval implements Comparable<HistogramInterval> {

    private double begin = 0.0D;
    private double end = 0.0D;
    private boolean beginInitialized = false;
    private boolean endInitialized = false;
    private boolean allowDecreasingInterval = false;
    private HashMap<Integer, Integer> occurrences = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> streamTotalPkgs = new HashMap<Integer, Integer>();
    private HashMap<Integer, Double> txsOccurrences = new HashMap<Integer, Double>();
    private StreamMetric type = null;

    /**
     * Create a histogram object. if a decreasing interval is allowed, then the
     * begin and the end of interval can be configured in any time, in any
     * order; If not allowed, an exception can be throwed if you not set the
     * begin and end accordingly.
     *
     * @param allowDecreasingInterval
     */
    public HistogramInterval(boolean allowDecreasingInterval) {
        this.allowDecreasingInterval = allowDecreasingInterval;

    }

    /**
     * Creates a histogram object which not allow decreasing intervals.
     */
    public HistogramInterval() {
        this(false);
    }

    /**
     * Each interval holds the metrics for each stream as needed, so, is
     * necessary initialize each stream.
     *
     * @param stream A unique number that identify a stream.
     */
    public void initializeStream(int stream) {
        streamTotalPkgs.put(stream, 0);
        occurrences.put(stream, 0);
        txsOccurrences.put(stream, 0.0D);
    }

    /**
     * Gets the number of configured packages of a stream. If stream is not
     * initialized, an exception is thrown.
     *
     * @param stream A unique number that identify a stream.
     * @return The total packages count.
     * @throws NotInitializedStreamInterval
     */
    public long getStreamPkgsCount(int stream) throws NotInitializedStreamInterval {
        Integer returnValue;
        if (streamTotalPkgs.containsKey(stream)) {
            returnValue = streamTotalPkgs.get(stream);
        } else {
            throw new NotInitializedStreamInterval(stream);
        }
        return returnValue;
    }

    /**
     *
     * @param stream
     * @param pkgsCount
     * @throws NotInitializedStreamInterval
     */
    public void setStreamPkgsCount(int stream, int pkgsCount) throws NotInitializedStreamInterval {
        if (streamTotalPkgs.containsKey(stream)) {
            streamTotalPkgs.put(stream, pkgsCount);
        } else {
            throw new NotInitializedStreamInterval(stream);
        }
    }

    /**
     * Verify if such
     * <code>number</code> is contained on interval. Only verify for one
     * extremity;
	 * If decreasing interval is not allowed (by default),
     * return true if
     * begin {@code <= number < end}
	 * If decreasing interval is
     * allowed, first, verify if
     * begin {@code <= number < end},
	 * if false, then verify if
     * begin {@code > number >= end}
     *
     *
     *
     * @param number A value to verify if is contained on interval.
     * @return True if has contained, else, false.
     * @throws InconsistentInterval If interval isn't initialized yet.
     */
    public boolean contains(double number) throws InconsistentInterval {
        if (!beginInitialized || !endInitialized) {
            throw new InconsistentInterval((!beginInitialized && !endInitialized) ? "Begin and end not initialized" : (!beginInitialized) ? "Begin not initialized" : "End not initialized");
        }
        if (!allowDecreasingInterval) {
            if (begin <= number && number < end) {
                return true;
            } else {
                return false;
            }
        } else {
            if (begin <= number && number < end) {
                return true;
            } else {
                if (begin > number && number >= end) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    /**
     * Sets the occurrences tax of the interval on stream.
     *
     * @param totalPkgs The total number of packages on stream
     * @param stream Stream to be analyzed
     * @return True if successfully {@code (totalPkgs >= 0)}, else, false.
     */
    public boolean setTxOccurrence(int totalPkgs, int stream) {
        if (totalPkgs > 0 && stream >= 0) {
            double txOccurrence;
            txOccurrence = ((double) occurrences.get(stream)) * 100.0 / ((double) totalPkgs);
            txsOccurrences.put(stream, txOccurrence);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the occurrences tax of this interval on stream values.
     *
     * @param stream The number of the stream.
     * @return //TODO complete documentation
     */
    public double getTxOccurrence(int stream) {
        Double returnValue = txsOccurrences.get(stream);
        if (returnValue != null) {
            return returnValue;
        } else {
            return 0.0D;
        }
    }

    /**
     * Sets the type of metric.
     *
     * @see StreamMetric
     * @param type
     */
    public void setType(StreamMetric type) {
        this.type = type;
    }

    /**
     * Returns the metric that is performed by object.
     *
     * @return Return the type of metric analyzed
     *
     * @throws IntervalTypeNotDefined If type isn't defined yet.
     */
    public StreamMetric getType() throws IntervalTypeNotDefined {
        if (type != null) {
            return type;
        } else {
            throw new IntervalTypeNotDefined();
        }
    }

    /**
     * Sets the begin of interval.
     *
     * @param begin A double value for begin.
     * @throws InconsistentInterval If the begin is greater than end, and
     * decreasing interval is not allowed.
     */
    public void setBegin(double begin) throws InconsistentInterval {
        if (allowDecreasingInterval) {
            this.begin = begin;
            beginInitialized = true;
        } else {
            if (endInitialized && (end < begin)) {
                throw new DecreasingIntervalNotAllowed("You tried to set interval as: [Begin: " + begin + "\tEnd: " + end + "], but decreasing interval is not allowed.");
            } else {
                this.begin = begin;
                beginInitialized = true;
            }
        }

    }

    /**
     * Gets the begin of interval.
     *
     * @return A double value for begin.
     * @throws InconsistentInterval If the begin has not been initialized.
     */
    public double getBegin() throws InconsistentInterval {
        if (beginInitialized) {
            return begin;
        } else {
            throw new InconsistentInterval("Begin not initialized.");
        }
    }

    /**
     * Sets the end of interval.
     *
     * @param end Value of end of interval.
     * @throws DecreasingIntervalNotAllowed
	 * If a decreasing interval isn't allowed, and a
     * attempt to configure decreasing interval is made.
     */
    public void setEnd(double end) throws DecreasingIntervalNotAllowed {
        if (!allowDecreasingInterval) {
            if (beginInitialized) {
                if (end >= begin) {
                    this.end = end;
                    endInitialized = true;
                } else {
                    throw new DecreasingIntervalNotAllowed("[Begin: " + begin + "\tEnd: " + end + "]");
                }
            } else {
                this.end = end;
                endInitialized = true;
            }
        } else {
            this.end = end;
            endInitialized = true;
        }
    }

    /**
     * Gets the end value of interval.
     *
     * @return A double value for end.
     * @throws InconsistentInterval If the end has not been initialized.
     */
    public double getEnd() throws InconsistentInterval {
        if (endInitialized) {
            return end;
        } else {
            throw new InconsistentInterval("The end has not been initialized.");
        }
    }

    public int getOccurrences(int stream) {
        Integer returnValue;
        returnValue = occurrences.get(stream);
        if (returnValue != null) {
            return returnValue.intValue();
        } else {
            return 0;
        }
    }

    public void increaseOccurrences(int stream) {
        if (occurrences.containsKey(stream)) {
            occurrences.put(stream, occurrences.get(stream) + 1);
        } else {
            occurrences.put(stream, 1);
        }
    }

    public int getRemainderPackages(int stream) {
        Integer returnValue;
        if (occurrences.containsKey(stream)) {
            returnValue = new Integer(streamTotalPkgs.get(stream) - occurrences.get(stream));
        } else {
            return -1;
        }
        return returnValue;
    }

    @Override
    public String toString() {
        String beginTmp = Double.toString(this.begin).replaceFirst("(0*)$", "").replaceFirst("\\.$", ".0");
        String endTmp = Double.toString(this.end).replaceFirst("(0*)$", "").replaceFirst("\\.$", ".0");
        return String.format("[%s : %s]", beginTmp, endTmp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (this == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        HistogramInterval aux = (HistogramInterval) obj;

        try {
            return (this.begin == aux.getBegin() && this.end == aux.getEnd());
        } catch (InconsistentInterval inconsistentInterval) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.begin) ^ (Double.doubleToLongBits(this.begin) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.end) ^ (Double.doubleToLongBits(this.end) >>> 32));
        return hash;
    }

    @Override
    public int compareTo(HistogramInterval o) {
        if (this.equals(o)) {
            return 0;
        }
        if (this.hashCode() < o.hashCode()) {
            return -1;
        }

        return 1;

    }
}