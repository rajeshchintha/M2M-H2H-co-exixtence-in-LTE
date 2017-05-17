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
 * That class provide a object that hold the dimension configuration of a
 * gnuplot terminal, such width, height and unit of meansure. Support a
 * flag that say if the meansure unit will be shown on
 * toString method.
 *
 * @since 1.2.0
 */
public class TerminalDimension {

    private double width, height;
    private MeasureUnit widthUnit = MeasureUnit.INCHES;
    private MeasureUnit heightUnit = MeasureUnit.INCHES;
    private boolean useExplicitUnit = false;

    public TerminalDimension(boolean useUnit) {
        useExplicitUnit = useUnit;
    }

    /**
     * Convert object to a string that represent a terminal dimension on gnuplot
     * terminal settings, correctly.
     *
     * @return A string in some these formats:
     *
     * (width) (width unit), (height) (height unit) - if
     * explicit unit flag is on, or
	 * (width), (unit) - if
     * explicit unit flag is off.
     *
     */
    @Override
    public String toString() {
        if (useExplicitUnit) {
            return String.format("%s %s, %s %s", width, widthUnit, height, heightUnit);
        } else {
            return String.format("%s, %s", width, height);

        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TerminalDimension) {
            TerminalDimension objToCmp = (TerminalDimension) obj;
            if (objToCmp.getHeight() == this.height && objToCmp.getWidth() == this.width) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void useExplictunit(boolean useUnit) {
        useExplicitUnit = useUnit;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public void setUnit(MeasureUnit unit) {
        widthUnit = unit;
        heightUnit = unit;
    }
}
