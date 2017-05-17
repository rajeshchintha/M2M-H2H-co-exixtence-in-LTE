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
 * Has informations about UDP packets and data about drop sequences.
 * 
 * @since 1.0.0
 */
public class DropCount {

    /**
     * Index of the current drop sequence
     */
    private int index = 0;
    /**
     * List to store the number of drops for each drop sequence
     */
    private ArrayList<Integer> droppedSequence = new ArrayList<Integer>();

    /**
     * @param newSequence indicates if it is a new drop sequence
     */
    public void setDrop(boolean newSequence) {

        if (newSequence) {
            getDroppedSequence().add(1);
            setIndex(getDroppedSequence().size() - 1);
        } else {
            getDroppedSequence().set(getIndex(), getDroppedSequence().get(getIndex()) + 1);
        }

    }

    @Override
    public String toString() {
        return "DropCount{" + "index=" + index + ", droppedStream=" + droppedSequence + '}';
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return the droppedStream
     */
    public ArrayList<Integer> getDroppedSequence() {
        return droppedSequence;
    }

    /**
     * @param droppedStream the droppedStream to set
     */
    public void setDroppedStream(ArrayList<Integer> droppedStream) {
        this.droppedSequence = droppedStream;
    }

    /**
     *
     * @return number of drop sequences
     */
    public int getSize() {
        return this.droppedSequence.size();
    }

    /**
     * @return the total of dropped packets.
     */
    public int getCountDroppedPackets() {
        int sum = 0;
        for (int i = 0; i < droppedSequence.size(); i++) {
            sum += droppedSequence.get(i);
        }
        return sum;
    }

    /**
     * 
     * @return the minimun of dropped packets
     */
    public int getMin() {

        int min = Integer.MAX_VALUE;

        for (int i = 0; i < droppedSequence.size(); i++) {

            if (droppedSequence.get(i) < min) {
                min = droppedSequence.get(i);
            }

        }

        return min;

    }

    /**
     * 
     * @return the maximun of dropped packets
     */
    public int getMax() {

        int max = 0;

        for (int i = 0; i < droppedSequence.size(); i++) {

            if (droppedSequence.get(i) > max) {
                max = droppedSequence.get(i);
            }

        }

        return max;

    }
}
