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
import analyzer.gui.ExportGraphics.Terminal.TerminalDimension;

/**
 * Interface to padronize all types of terminal configurations.
 *
 * @since 1.2.0
 */
public interface TerminalConfiguration {

    /**
     * Make and return an instance of configuration of terminal, that are set to
     * default gnuplot values.
     *
     * @return The configuration of terminal.
     */
    public TerminalConfiguration setDefault();

    /**
     * Configure the default size for terminal;
     *
     * @param width Width of terminal.
     * @param height Height of terminal.
     */
    public void setDefaultSize(double width, double height);

    /**
     * Return the default size of terminal.
     *
     * @return A default dimension of terminal.
     */
    public TerminalDimension getDefaultSize();

    /**
     * Configure the size of terminal. The size may or may not be associated
     * with a specific unit of measure. If not, by default, this unit is in
     * pixels.
     *
     * @param width Width of terminal.
     * @param height Height of terminal.
     */
    public void setSize(double width, double height);

    /**
     * Set the unit that are associated with size of terminal.
     *
     * @param unit Measure unit, which can be PX, INCHES, and others units
     * defined on gnuplot.
     */
    /**
     * Get the size of terminal.
     *
     * @return An TerminalDimension object with the setted dimension.
     */
    public TerminalDimension getSize();

    public void setUnit(MeasureUnit unit);

    /**
     * For use with
     * <code>OutputTerminalConfigurator</code> object; It assists instances of
     * object, that contain jSpinners, for lock the proportion between width and
     * height. When user select to lock the proportion, the width and height at
     * moment are used to know the proportion between these, that can be
     * restored later with the method
     * <code>getProportion()</code>.
     *
     * @param width Actual width of terminal
     * @param height Actual height of terminal
     */
    public void setProportion(double width, double height);

    /**
     * Retrieve the proportion at lock moment. Only for assist the
     * <code>OutputTerminalConfigurator</code> objects.
     *
     * @return the proportion between width and height terminal.
     */
    public double getProportion();

    /**
     * Make a line that contain all configurations of terminal.
     *
     * @return All parameters of terminal.
     */
    @Override
    public String toString();
}
