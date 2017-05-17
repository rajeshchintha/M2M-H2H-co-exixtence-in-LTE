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

package analyzer.recoverable_exceptions;


/**
 * 
 * Exception to handle the case that a header that does not have the 'length' 
 * field was found and it is not on the Headers class. Ignoring this 
 * exception will cause the Throughput, Average data of Sent and received 
 * packets to be wrong.
 * 
 * @since 1.0.0
 */
public class NoLengthProtocolException extends RecoverableException {

    /**
     * The protocol name
     */
    private String protocol;
    /**
     * The protocol content
     */
    private String content;

    /**
     * 
     * @param protocol protocol name
     */
    public NoLengthProtocolException(String protocol) {
        super("A protocol with no length field was found! Measures that uses this field will not be reliable!\nProtocol: "+protocol);
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return "NoLengthProtocolException: " + getProtocol() + " " + getContent() + "\n";
    }

    /**
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }



}