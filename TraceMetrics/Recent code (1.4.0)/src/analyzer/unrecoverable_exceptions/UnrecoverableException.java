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
 * General exception that makes the execution to abort. This kind of exception
 * can not be recovered, so there is nothing to be done.
 * 
 * @since 1.0.0
 */
public class UnrecoverableException extends Exception{

    /**
     * The message of a child exception
     */
    String childMessage;

    /**
     * 
     * @param childMessage the message of a child exception
     */
    public UnrecoverableException(String childMessage) {
        super("An unrecoverable exception was found and the analysis was aborted!\n"+childMessage);
        this.childMessage = childMessage;
    }
    
    

}
