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
package analyzer.gui.ExportGraphics.Terminal.TerminalConfigurations;

import analyzer.gui.ExportGraphics.Terminal.TerminalDimension;
import analyzer.gui.ExportGraphics.Terminal.MeasureUnit;

/**
 * A empty gnuplot terminal configuration. For unknowns terminals.
 *
 * @since 1.2.0
 */
public final class EmptyConfiguration extends NonFileOutputTerminal {

    private String name;

    public EmptyConfiguration() {
        name = "";
    }

    @Override
    public EmptyConfiguration setDefault() {
        name = "";
        return this;
    }

    @Override
    public void setUnit(MeasureUnit unit) {
        MeasureUnit u = unit;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void setSize(double width, double height) {
        double w = width, h = height;
    }

    @Override
    public void setDefaultSize(double width, double height) {
        double w = width, h = height;
    }

    /**
     * Return an TerminalDimension object setted to 0x0 units;
     *
     * @return Return a TerminalDimension with 0x0 dimension.
     */
    @Override
    public TerminalDimension getDefaultSize() {
        TerminalDimension returnValue = new TerminalDimension(false);
        returnValue.setSize(0, 0);
        return returnValue;
    }

    @Override
    public TerminalDimension getSize() {
        TerminalDimension returnValue = new TerminalDimension(false);
        returnValue.setSize(0, 0);
        return returnValue;
    }
}
