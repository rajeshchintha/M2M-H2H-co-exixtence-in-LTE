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
package analyzer.gui.ExportGraphics;

import analyzer.gui.ExportGraphics.Terminal.TerminalDimension;
import analyzer.gui.ExportGraphics.Terminal.TerminalTypes;
import analyzer.gui.bug.BugReport;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 * That class try to call gnuplot by a system process, passing a file to be
 * loaded and interpreted by program. Provide some facilities such tell if a try
 * to plot was successfull or not.
 *
 * @since 1.2.0
 */
public class Plotter {

    private File plotedImageFile = null;
    private String plotedFileName = "";
    private boolean successState = false;
    private Image preview = null;
    private Image largePreview = null;
    /**
     * Variable globalNumberID is a fix for make files with different names.
     */
    private static int globalNumberID = 0;

    /**
     * This method build a process on target system and try to invoke gnuplot,
     * passing "File" argument on line command, to auto plot the file. NOTE The
     * file is the script that automatize all work.
     *
     *
     * @param fileToPlot File that will be loaded on gnuplot
     * @return True if the file was successful plotted, or false cause some
     * problem occurred.
     */
    public boolean tryPlot( File fileToPlot ) {
        if ( fileToPlot != null ) {
            ProcessBuilder builder = System.getProperty("os.name").toLowerCase().contains("window")
                                     ? new ProcessBuilder("cmd", "/C", "gnuplot", fileToPlot.getName()).directory(fileToPlot.getParentFile())
                                     : new ProcessBuilder("gnuplot", fileToPlot.getName()).directory(fileToPlot.getParentFile());
            String in = null;
            try {
                Process proc = builder.redirectErrorStream(true).start();
                InputStream is = proc.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                while ( ( in = br.readLine() ) != null ) {
                    System.out.println(in);
                    if ( in.matches("\\\".*\\\", line [0-9]*") || in.contains("erro") );
                    successState = false;
                    return successState;
                }

            }
            catch ( Exception e ) {
                successState = false;
                return successState;
            }

            //  Read the script file and verify if the output was ploted on directory
            //to know if a plot was successfull or not.
            try {
                if ( fileToPlot.exists() ) {
                    Scanner scanner = new Scanner(fileToPlot);
                    String line;

                    while ( scanner.hasNextLine() ) {
                        line = scanner.nextLine();
                        if ( line.contains("output") ) {
                            int s, e;
                            s = line.indexOf("\"");
                            e = line.lastIndexOf("\"");
                            plotedFileName = line.substring(s + 1, e);
                            plotedImageFile = new File(fileToPlot.getParent() + File.separator + plotedFileName);
                            break;
                        }
                    }

                    successState = plotedImageFile.exists();
                }
                else {
                    throw new IOException("File not found");
                }
            }
            catch ( FileNotFoundException ex ) {
                Logger.getLogger(Plotter.class.getName()).log(Level.SEVERE, null, ex);
                successState = false;
            }
            catch ( IOException iOException ) {
                successState = false;
            }
            finally {
                return successState;
            }
        }
        else {
            successState = false;
            return successState;
        }
    }

    //TODO Revisar essa documentação
    /**
     * Make a image preview of plot, independently of terminal chosen on file.
     * That method <i>will try</i> to plot image with common images terminals of
     * gnuplot, such jpg, png, gif and etc. The image is loaded in memory on
     * program and immediately deleted from temp folder on disk. Accept
     * <code>null</code> parameter, which return a default ImageIcon indicating
     * error.
     *
     * @param fileToPlot File that contain script sequence to be load on
     * gnuplot. If <code>null</code>, a default error image is retorned.
     * @return A ImageIcon containing the image built by gnuplot if successful,
     * otherwise, a default image indicating failure is returned.
     */
    public ImageIcon makePreview( File fileToPlot, Dimension dimension ) {
        TerminalDimension terminalDimensio = new TerminalDimension(false);
        terminalDimensio.setSize(dimension.width, dimension.height);

        Stack<TerminalTypes> terminalsTrys = new Stack<TerminalTypes>();
        terminalsTrys.push(TerminalTypes.GIF);
        terminalsTrys.push(TerminalTypes.JPEG);
        terminalsTrys.push(TerminalTypes.PNG);

        ImageIcon returnValue = new ImageIcon(getClass().getResource("/analyzer/gui/images/previewFail.jpg"));

        File previewToPlot = null;
        File largePreviewToPlot = null;

        Stack<File> imagesToDelete = new Stack<File>();

        if ( fileToPlot != null && fileToPlot.exists() ) {
            preview = null;
            largePreview = null;
            globalNumberID++;
            try {
                String readedLine;
                Scanner scanner;
                while ( !terminalsTrys.empty() ) {
                    scanner = new Scanner(fileToPlot);
                    StringBuilder normalPreviewCommands = new StringBuilder();
                    StringBuilder largePreviewCommands = new StringBuilder();

                    while ( scanner.hasNextLine() ) {
                        readedLine = scanner.nextLine();

                        if ( readedLine.contains("terminal") ) {
                            readedLine = readedLine.replace(readedLine, "set terminal " + terminalsTrys.peek().getExtension().replaceAll("\\.", "") + " size " + terminalDimensio.toString() + ";");
                            largePreviewCommands.append(readedLine.replace(readedLine, "set terminal " + terminalsTrys.peek().getExtension().replaceAll("\\.", "") + " size 800,600;\n"));
                        }
                        else if ( readedLine.contains("output") ) {
                            readedLine = readedLine.replaceAll("\\..*\"", ".preview" + globalNumberID + terminalsTrys.peek().getExtension() + "\"");
                            if ( readedLine.matches("set output;") ) {
                                readedLine = "set output \"out.preview" + globalNumberID + terminalsTrys.peek().getExtension() + "\"";
                            }
                            largePreviewCommands.append(readedLine.replace("preview", "largePreview")).append("\n");
                        }
                        else {
                            largePreviewCommands.append(readedLine).append("\n");
                        }
                        normalPreviewCommands.append(readedLine).append("\n");
                    }

                    previewToPlot = new File(fileToPlot.getParent(), "preview");
                    largePreviewToPlot = new File(fileToPlot.getParent(), "largePreview");

                    FileWriter nfw = new FileWriter(previewToPlot);
                    nfw.append(normalPreviewCommands.toString());
                    nfw.close();

                    FileWriter lfw = new FileWriter(largePreviewToPlot);
                    lfw.append(largePreviewCommands.toString());
                    lfw.close();

                    boolean previousState = successState;
                    tryPlot(previewToPlot);
                    if ( isSuccessfull() ) {
                        preview = Toolkit.getDefaultToolkit().createImage(plotedImageFile.getPath());
                        imagesToDelete.add(plotedImageFile.getCanonicalFile());
                        tryPlot(largePreviewToPlot);
                        if ( isSuccessfull() ) {
                            largePreview = Toolkit.getDefaultToolkit().createImage(fileToPlot.getParent() + File.separator + plotedFileName);
                            imagesToDelete.add(plotedImageFile.getCanonicalFile());
                        }
                        terminalsTrys.clear();
                    }
                    else {
                        terminalsTrys.pop();
                    }
                    successState = previousState;

                    largePreviewToPlot.delete();
                    previewToPlot.delete();
                }

                if ( plotedImageFile.exists() ) {
                    returnValue = new ImageIcon(getPreview());
                }

            }
            catch ( FileNotFoundException ex ) {
                Logger.getLogger(Plotter.class.getName()).log(Level.SEVERE, null, ex);
                new BugReport("Fail at create a preview" + ex).setVisible(true);
            }
            catch ( IOException ex ) {
                Logger.getLogger(Plotter.class.getName()).log(Level.SEVERE, null, ex);
                new BugReport("Fail at create a preview" + ex).setVisible(true);
            }
            finally {
                if ( previewToPlot != null ) {
                    previewToPlot.delete();
                }
                if ( largePreviewToPlot != null ) {
                    largePreviewToPlot.delete();
                }
                for ( File f : imagesToDelete ) {
                    f.deleteOnExit();
                }
                if ( preview == null ) {
                    preview = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/analyzer/gui/images/previewFail.jpg"));
                }
                if ( largePreview == null ) {
                    largePreview = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/analyzer/gui/images/previewFail.jpg"));
                }

                return returnValue;
            }
        }
        else {
            preview = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/analyzer/gui/images/previewFail.jpg"));
            largePreview = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/analyzer/gui/images/previewFail.jpg"));
            return returnValue;
        }
    }

    /**
     * Indicate if the last tryed plot has successfull or not.
     *
     * @return True if successfull, else, false.
     */
    public boolean isSuccessfull() {
        return successState;
    }

    public Image getPreview() {
        return preview;
    }

    public Image getLargePreview() {
        return largePreview;
    }
}
