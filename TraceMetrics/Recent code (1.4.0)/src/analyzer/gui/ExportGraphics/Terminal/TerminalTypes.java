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
 * List the types of terminals that TraceMetrics provide support for
 * script.
 * Each enum object of that class holds some informations how,
 * friendly object's name, especific terminal name, extension of file if the
 * terminal generate a output file, a entension of file if the terminal generate
 * a output file and a flag indicating if terminal generate a file output.
 *
 * @since 1.2.0
 */
public enum TerminalTypes {

    //------------------------------------------------------------------------//
    //---   For add a new terminal type of gnuplot on tracemetrics, follow the
    //---       below model correctly.
    //------------------------------------------------------------------------//
    //Model: NAME(Friendly name, gnuplot name, extension of file, description, generate a file output, generate a image as output)
    X11("x11", "x11", null, "Gnuplot provides the x11 terminal type for use with X servers."),
    WXT("Wxt", "wxt", null, "The wxt terminal device generates output in a separate window.\n The window is created by the wxWidgets library, where the 'wxt' comes from.\n The actual drawing is done via cairo, a 2D graphics library, and pango, a library for laying out and rendering text."),
    POSTSCRIPT("Postscript", "postscript", ".eps", "Encapsulated PostScript File - EPS files are supported by several different drawing programs and vector graphic editing applications.", true),
    PNG("Png", "png", ".png", "A PNG image that support transparency and interlaced.", true, true),
    JPEG("Jpeg", "jpeg", ".jpeg", "A JPG/JPEG image that support interlace.", true, true),
    GIF("Gif", "gif", ".gif", "GIF images are created using the external library libgd.", true, true),
    CANVAS("HTML 5", "canvas", ".html", "To view the results you need a browser that supports the HTML 5 'canvas' element.", true);
    private String name = "";
    private String gnuplotName = "";
    private String description = "Without description";
    private String extensionFile = "";
    private boolean isFileOutput = false;
    private boolean isImageOutput = false;

    private TerminalTypes(String name) {
        this.name = name;
    }

    private TerminalTypes(String name, String gnuplotName) {
        this(name);
        this.gnuplotName = gnuplotName;
    }

    private TerminalTypes(String name, String gnuplotName, String extensionFile) {
        this(name, gnuplotName);
        this.extensionFile = extensionFile;
    }

    private TerminalTypes(String name, String gnuplotName, String extensionFile, String description) {
        this(name, gnuplotName, extensionFile);
        this.description = description;
    }

    private TerminalTypes(String name, String gnuplotName, String extensionFile, String description, boolean isFileOutput) {
        this(name, gnuplotName, extensionFile, description);
        this.isFileOutput = isFileOutput;
    }

    private TerminalTypes(String name, String gnuplotName, String extensionFile, String description, boolean isFileOutput, boolean isImageOutput) {
        this(name, gnuplotName, extensionFile, description, isFileOutput);
        this.isImageOutput = isImageOutput;
    }

    /**
     * Return a extension of file used as output from terminal. It includes the
     * dot at begin of string.
	 * Example: For png terminal, the return
     * would be as * ".png";
     *
     * @return String with extension of file generated from specific terminal.
     */
    public String getExtension() {
        return extensionFile;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("%s", name);
    }

    public String getGnuplotName() {
        return gnuplotName;
    }

    public boolean isFileoutput() {
        return isFileOutput;
    }

    public static TerminalTypes getDefaultTerminal() {
        return TerminalTypes.values()[0];
    }

    public boolean isImageOutput() {
        return isImageOutput;
    }
}
