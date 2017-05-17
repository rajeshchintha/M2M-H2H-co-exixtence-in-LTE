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
 * Configuration for a jpg terminal on gnuplot.
 *
 * @since 1.2.0
 */
public class JpegConfiguration extends ImageOutputTerminal {

    private boolean useInterlace = false;

    public void setUseInterlace(boolean value) {
        useInterlace = value;
    }

    @Override
    public JpegConfiguration setDefault() {
        setDefaultSize(640, 400);
        setUseInterlace(false);
        setSize(640, 400);
        setUnit(MeasureUnit.PX);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (!getSize().equals(getDefaultSize())) {
            builder.append(" size ").append(getSize().toString());
        }
        builder.append(" ");
        builder.append(useInterlace ? " interlace" : " ");
        return String.format(builder.toString().replaceAll(" +", " ").replaceFirst("^ +", ""));
    }
}
