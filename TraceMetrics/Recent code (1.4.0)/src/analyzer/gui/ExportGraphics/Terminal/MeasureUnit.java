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
package analyzer.gui.ExportGraphics.Terminal;

/**
 * MeasureUnit to be used to measure the terminal size.
 *
 * @since 1.2.0
 */
public enum MeasureUnit {

    INCHES("inches"), CM("cm"), PX("px");
    private String name;

    private MeasureUnit(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return String.format("%s", name);
    }

    /**
     * Try to convert a string to some valid unit type.
     *
     * @param unit String which will converted
     * @return A some valid enum unit, or a NullPointer if String has no
     * regonized.
     * @throws NullPointerException
     * @since 1.2.0
     */
    public static MeasureUnit convert(String unit) throws NullPointerException {
        MeasureUnit returnUnit = null;
        for (MeasureUnit u : MeasureUnit.values()){
            if (unit.toLowerCase().contains(u.toString()) || unit.charAt(0) == u.toString().charAt(0)){
                returnUnit = u;
            }
        }
        return returnUnit;
    }
}
