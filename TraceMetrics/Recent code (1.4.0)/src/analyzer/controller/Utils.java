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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * Class with general purpose methods.
 *
 * @since 1.0.0
 */
public class Utils {

    /**
     * Convert the data quantity from 'byte' to the most suitable unit. E.g. KB,
     * MB, GB.
     *
     * @param data amount of data in bytes
     * @return converted data plus most suitable unit e.g. 1024KB
     * @since 1.0
     *
     */
    public static String convertData( double data ) {

        String[] units = { "B", "KB", "MB", "GB", "TB", "PB", "ZB" };
        int cont = -1;
        if ( data > 1023 ) {
            do {
                cont++;
                data /= 1024.0;
            } while ( data >= 1.0 );
            data *= 1024.0;
        }
        else {
            cont++;
        }

        return new Double(data).toString() + " " + units[cont];

    }

    /**
     * Convert the data quantity from 'byte' to the most suitable unit. E.g. KB,
     * MB, GB. The return number is truncated in decimal places by truncDecimal.
     *
     * @param truncDecimal The decimal places quantity. Truncate number if
     * necessary.
     * @param data amount of data in bytes
     * @return converted data plus most suitable unit e.g. 1024KB
     * @since 1.0
     *
     */
    public static String convertData( double data, int truncDecimal ) {
        String[] units = { "B", "KB", "MB", "GB", "TB", "PB", "ZB" };
        int cont = -1;
        if ( data > 1023 ) {
            do {
                cont++;
                data /= 1024.0;
            } while ( data >= 1.0 );
            data *= 1024.0;
        }
        else {
            cont++;
        }

        return Double.toString(Utils.truncDouble(data, 2)) + " " + units[cont];
    }

    public static String convertMilisToTime( long time, boolean formatOutput ) {

        String result;
        long milis;
        long seconds;
        long minutes;
        long hours;
        long days;

        milis = time % 1000;
        time = time / 1000;

        seconds = time % 60;
        time = time / 60;

        minutes = time % 60;
        time = time / 60;

        hours = time % 24;
        if ( formatOutput ) {
            result = ( ( hours > 0 ) ? hours + "h " : "   " ) + ( ( minutes > 0 ) ? ( minutes < 10 ? " " + minutes + "m " : minutes + "m " ) : "   " ) + seconds + "s ";
        }
        else {
            result = ( ( hours > 0 ) ? hours + "h " : "" ) + ( ( minutes > 0 ) ? minutes + "m " : "" ) + seconds + "s ";
        }

        return result;

    }

    public static Point getCentralCoords( Dimension screenSize, Rectangle windowSize ) {

        Point centralCoords = new Point();

        centralCoords.x = ( screenSize.width / 2 ) - ( windowSize.width / 2 );
        centralCoords.y = ( screenSize.height / 2 ) - ( windowSize.height / 2 );

        return centralCoords;

    }

    /**
     * Receive a double value and trunc decimals places
     *
     * @param value Number to truncate.
     * @param decimals Numbers of decimal places to keep. If a negative number
     * or number zero is request, nothing will happen with number. * *      * If <code>decimals</code> are bigger that the decimals places of
     * number, then, the original number will be return.
     * @return A truncated double value
     * @since 1.2.0
     */
    public static double truncDouble( Double value, int decimals ) {

        char[] charValue = value.toString().toCharArray();

        if ( value.toString().contains("E") ) {
            return value;
        }

        StringBuilder buffer = new StringBuilder(charValue.length);

        int position = 0;
        while ( charValue[position] != '.' ) {
            buffer.append(charValue[position]);
            position++;
        }
        if ( position == 0 ) {
            buffer.append("0");
        }
        position++;

        if ( ( charValue.length - position ) < decimals ) {
            //throw new Exception("Insulficient decimals points.");
            buffer.append(".");

            while ( decimals > 0 && position < charValue.length ) {
                buffer.append(charValue[position]);
                position++;
                decimals--;
            }
            //If a future implementation request one String as return
            while ( decimals > 0 ) {
                buffer.append('0');
                decimals--;
            }

        }
        else {

            buffer.append(".");

            while ( decimals > 0 ) {
                buffer.append(charValue[position]);
                position++;
                decimals--;
            }
        }
        return Double.parseDouble(buffer.toString());
    }

    public static void main( String[] args ) {
        System.out.println(truncDouble(987.4, 25));
    }
}
