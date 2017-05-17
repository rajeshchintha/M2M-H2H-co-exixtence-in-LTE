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
package analyzer.recoverable_exceptions.hitogramIntervals;

/**
 * This exception happen when you try to get some information of interval when
 * this is not initialized yet.
 * 
 * @since 1.2.0
 */
public class NotInitializedStreamInterval extends InconsistentInterval {

    public NotInitializedStreamInterval() {
        super("You tried to manipulate a stream that not has been initialized.");
    }

    public NotInitializedStreamInterval(int stream) {
        super("You tried to manipulate a stream that not has been initialized.\nStream: " + stream);
    }
}
