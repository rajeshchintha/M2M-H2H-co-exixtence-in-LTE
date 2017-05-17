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

import analyzer.gui.ExportGraphics.Terminal.MeasureUnit;

/**
 * Configuration for a eps terminal on gnuplot.
 *
 * @since 1.2.0
 */
public class EpsConfiguration extends FileOutputTerminal {

    private boolean coloured = false;
    private Orientation orientation = Orientation.LANDSCAPE;
    private MeasureUnit unit = MeasureUnit.INCHES;

    public MeasureUnit getUnit() {
        return unit;
    }

    @Override
    public EpsConfiguration setDefault() {
        setDefaultSize(10, 7);
        setColoured(true);
        setOrientation(Orientation.LANDSCAPE);
        setSize(10, 7);
        setUnit(MeasureUnit.INCHES);
        getSize().useExplictunit(true);
        super.proportion = 10.0 / 7.0;
        return this;
    }

    /**
     * Orientation for eps plot; Landscape or Portrait.
     *
     * @since 1.2.0
     */
    public enum Orientation {

        LANDSCAPE("landscape"), PORTRAIT("portrait");
        private String name;

        private Orientation(String s) {
            name = s;
        }

        @Override
        public String toString() {
            return String.format("%s", name);
        }
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public void setColoured(boolean coloured) {
        this.coloured = coloured;
    }

    @Override
    public void setUnit(MeasureUnit unit) {
        this.unit = unit;
        getSize().setUnit(this.unit);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(orientation);
        builder.append(coloured ? " color" : " monochrome");
        if (!getSize().equals(getDefaultSize())) {
            builder.append(" size ").append(getSize().toString());
        }
        return builder.toString().replaceAll(" +", " ").replaceFirst("^ +", "");
    }
}
