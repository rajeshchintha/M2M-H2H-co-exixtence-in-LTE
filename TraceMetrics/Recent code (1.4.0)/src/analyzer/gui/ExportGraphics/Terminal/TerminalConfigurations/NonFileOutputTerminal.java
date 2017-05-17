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
 * Abstract class for all terminals that not generate a file as output.
 * 
 * @since 1.2.0
 */
public abstract class NonFileOutputTerminal implements TerminalConfiguration {

    private final TerminalDimension defaultSize = new TerminalDimension(false);
    private TerminalDimension size = new TerminalDimension(true);
    private double proportion = 0.0D;

    @Override
    public void setDefaultSize(double width, double height) {
        defaultSize.setSize(width, height);
    }

    @Override
    public TerminalDimension getDefaultSize() {
        return defaultSize;
    }
    
    @Override
    public void setSize(double width, double height) {
        size.setSize(width, height);
    }

    @Override
    public void setUnit(MeasureUnit unit) {
        size.setUnit(unit);
    }

    @Override
    public void setProportion(double width, double height) {
        proportion = width / height;
    }

    @Override
    public double getProportion() {
        return proportion;
    }
}
