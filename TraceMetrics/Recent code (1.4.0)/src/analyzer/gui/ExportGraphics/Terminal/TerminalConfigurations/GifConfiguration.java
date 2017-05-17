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
public class GifConfiguration extends ImageOutputTerminal {

    private boolean transparent;
    private boolean rounded;
    private boolean enhanced;
    private double lineWidth;
    private double dashLength;

    @Override
    public TerminalConfiguration setDefault() {
        setDefaultSize(640, 480);
        rounded = true;
        transparent = false;
        enhanced = false;
        lineWidth = 1;
        dashLength = 1;
        getSize().setUnit(MeasureUnit.PX);
        setSize(640, 480);
        return this;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public void setLineWidth(double lineWidth) {
        this.lineWidth = lineWidth;
    }

    public void setRounded(boolean rounded) {
        this.rounded = rounded;
    }

    public void setEnhaced(boolean enhaced) {
        this.enhanced = enhaced;
    }

    public void setDashLenght(double dashLenght) {
        this.dashLength = dashLenght;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(enhanced ? " enhanced" : " ");
        builder.append(transparent ? " transparent" : " ");
        builder.append(rounded ? " rounded" : " butt");
        builder.append(lineWidth != 1.0D ? " linewidth " + lineWidth : " ");
        builder.append(dashLength != 1.0D ? " dashlength " + dashLength : " ");
        if (!getSize().equals(getDefaultSize())) {
            builder.append(" size ").append(getSize().toString());
        }
        return String.format(builder.toString().replaceAll(" +", " ").replaceFirst("^ +", ""));
    }
}
