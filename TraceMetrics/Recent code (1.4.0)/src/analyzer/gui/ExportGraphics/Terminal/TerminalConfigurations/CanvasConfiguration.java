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
import analyzer.gui.bug.BugReport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.swing.JOptionPane;

/**
 *
 * Configuration for a canvas (HTML 5) terminal on gnuplot
 *
 * @since 1.2.0
 */
public class CanvasConfiguration extends FileOutputTerminal {
    
    private String jsDirectoryName = "";
    private String title = "";
    private boolean enhancedMouse = false;
    
    @Override
    public CanvasConfiguration setDefault() {
        setDefaultSize(600, 400);
        jsDirectoryName = "plot_scripts";
        title = "Plot using HTML 5";
        setUnit(MeasureUnit.PX);
        setSize(600, 400);
        enhancedMouse = true;
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        if (!getSize().equals(getDefaultSize())) {
            builder.append("size ").append(getSize().toString());
        }
        builder.append(" standalone ").append(enhancedMouse ? "mousing" : "");
        builder.append(" jsdir \"").append(jsDirectoryName).append("\"");
        if (!title.isEmpty()) {
            builder.append(" title \"").append(title).append("\"");
        }
        return builder.toString();
    }

    /**
     * Enable advanced features with mouse.
     *
     * @param enhaced
     */
    public void setEnhacedMouse(boolean enhaced) {
        enhancedMouse = enhaced;
    }

    /**
     * Set the directory that will contain script for html page.
     *
     * @param name Name of directory
     */
    public void setJsDirectoryName(String name) {
        jsDirectoryName = name;
    }

    /**
     * Set the HTML page title.
     *
     * @param title Title for page.
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getJsDirectoryName() {
        return jsDirectoryName;
    }
    
    public String getTitle() {
        return title;
    }

    /**
     * Try to save all needed scripts for html page on especified directory.
     *
     * @param parent Path to directory.
     * @param fileName Name of file that is the output of terminal.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void saveScripts(String parent, String fileName) throws FileNotFoundException, IOException {
        File directory = new File(parent + File.separator + jsDirectoryName);
        ZipFile zip = null;
        File file = null;
        InputStream is = null;
        OutputStream os = null;
        byte[] buffer = new byte[5000];
        try {
            //create the directory if there's no he
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    JOptionPane.showMessageDialog(null, "Some probleam at create directory.");
                }
            }
            zip = new ZipFile(getClass().getResource("scripts.zip").getFile().replaceAll("%20", " "));
            Enumeration e = zip.entries();
            while (e.hasMoreElements()) {
                ZipEntry entrada = (ZipEntry) e.nextElement();
                file = new File(directory, entrada.getName());
                //if a nonexistent directory, create the structure and jump to next entry
                if (entrada.isDirectory() && !file.exists()) {
                    file.mkdirs();
                    continue;
                }
                //if the directories not exist, make he
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                try {
                    //read the zip file and store in disk
                    is = zip.getInputStream(entrada);
                    os = new FileOutputStream(file);
                    int bytesLidos = 0;
                    if (is == null) {
                        new BugReport("Error at read zip file.\nDirectory:\t" + directory.toString()).setVisible(true);
                        throw new ZipException("Error at read zip file");
                    }
                    while ((bytesLidos = is.read(buffer)) > 0) {
                        os.write(buffer, 0, bytesLidos);
                    }
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception ex) {
                        }
                    }
                    if (os != null) {
                        try {
                            os.close();
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (Exception e) {
                }
            }
        }
        
    }
//    /**
//     * Read each script in the jar package and write a copy on a directory, that
//     * have as parent, the directory that contain the html output file.
//     *
//     * @param parent Path to directory.
//     * @param scriptName Name of script in jar package.
//     * @throws FileNotFoundException
//     * @throws IOException
//     */
//    private void copyScript(File parent, String scriptName) throws FileNotFoundException, IOException {
//        File gnuplotScript = new File(getClass().getResource("").getPath().replace("%20", " "), scriptName);
//        Scanner reader = new Scanner(gnuplotScript);
//        StringBuilder builder = new StringBuilder();
//        while (reader.hasNextLine()) {
//            builder.append(reader.nextLine());
//            builder.append("\n");
//        }
//        FileWriter writer = new FileWriter(new File(parent.getPath() + File.separator + scriptName));
//        writer.append(builder.toString());
//        writer.close();
//    }
}
