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

package analyzer.unrecoverable_exceptions;


/**
 * 
 * Exception to handle the case when an invalid line is found on the trace file.
 * Ignoring an invalid trace line can make the sequence of events to be 
 * inconsistent, so the analysis must be aborted.
 * 
 * @since 1.0.0
 */
public class InvalidTraceLineException extends UnrecoverableException {

    /**
     * The line number
     */
    private long lineNumber;
    /**
     * The trace line
     */
    private String line;
    /**
     * The exception message
     */
    private String message;

    /**
     *
     * @param message the exception message
     * @param line the trace line
     */
    public InvalidTraceLineException(String message, String line, long lineNumber) {
        super("Malformed line! " + message + "\nLine number: "+lineNumber+"\nLine: "+ line);
        this.line = line;
        this.message = message;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return "InvalidTraceLineException - " + message + "\n" + "Line number: "+ lineNumber + "\n" + "Line: " + line;
    }
}
