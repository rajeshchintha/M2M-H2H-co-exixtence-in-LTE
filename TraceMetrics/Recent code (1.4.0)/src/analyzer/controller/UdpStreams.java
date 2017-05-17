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

package analyzer.controller;

import analyzer.entity.DropCount;
import analyzer.entity.UdpStat;
import java.util.ArrayList;

/**
 * 
 * Performs statistical calculations on a set of UdpStreams.
 * 
 * @since 1.0.0
 */
public class UdpStreams {

    public static ArrayList<UdpStat> getStatistics(ArrayList<DropCount> data) {

        int min = Integer.MAX_VALUE;
        int max = 0;
        int packetSum = 0;
        ArrayList<UdpStat> stat = new ArrayList<UdpStat>();

        for (int i = 0; i < data.size(); i++) {

            if (data.get(i).getMin() < min) {
                min = data.get(i).getMin();
            }
            if (data.get(i).getMax() > max) {
                max = data.get(i).getMax();
            }

            packetSum += data.get(i).getCountDroppedPackets();

        }
        if (max != 0) {
            int[] sum = new int[max - min + 1];

            for (int i = 0; i < data.size(); i++) {

                for (int j = 0; j < data.get(i).getCountDroppedPackets(); j++) {

                    sum[data.get(i).getDroppedSequence().get(j) - min]++;

                }

            }

            for (int i = 0; i < sum.length; i++) {

                if (sum[i] > 0) {
                    stat.add(new UdpStat(i + min, sum[i], sum[i] / packetSum));
                }

            }
        }else{
            stat.add(new UdpStat(0, 0 , 1.0));
        }

        return stat;

    }
}
