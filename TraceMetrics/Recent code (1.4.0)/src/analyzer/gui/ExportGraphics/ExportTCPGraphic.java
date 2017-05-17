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

import analyzer.controller.Analyzer;
import analyzer.controller.Utils;
import analyzer.entity.TcpStream;
import analyzer.gui.ExportGraphics.ExportWindowState.TypeOfGraphic;
import analyzer.gui.ExportGraphics.Terminal.TerminalConfigurations.CanvasConfiguration;
import analyzer.gui.ExportGraphics.Terminal.TerminalConfigurator;
import analyzer.gui.ExportGraphics.Terminal.TerminalTypes;
import analyzer.gui.Main;
import analyzer.recoverable_exceptions.hitogramIntervals.InconsistentInterval;
import analyzer.recoverable_exceptions.hitogramIntervals.NotInitializedStreamInterval;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Window that facilite the generation of script to plot the result of
 * tracemetrics analysis on GNU-Plot. After generated, you can save and edit as
 * your convenience.
 *
 * @since 1.2.0
 * @version 1.0
 */
public final class ExportTCPGraphic extends javax.swing.JFrame {

    //<editor-fold defaultstate="collapsed" desc="Variables and object's members">
    //------------------------------------------------------------------------//
    //------- Defaults values to fields of ExportGraphic window --------------//
    //------------------------------------------------------------------------//
    private String lineTitle = "Stream <n>";
    private String lineXlabelTitle = "Elapsed time";
    private String lineYlabelTitle = "Delay variation";
    private String histogramTitle = "Stream <n>";
    private String histogramXlabelTitle = "Stream(s)";
    private String histogramYlabelTitle = "Percents of use for each interval (%)";
    private JPanel zoomImagePanel = new JPanel();
    private JFrame zoomImage = new JFrame("Plot view");
    private JLabel zoomImageLabel = new JLabel();
    private Plotter plotter = new Plotter();
    /**
     *
     * The program allows you to open multiples instances on ExportTCPGraphic
     * window, for variated number os streams, but, when you load and analyze a
     * new trace file, if some previous analysis EG window remain opened, they
     * become inconsistent and is necessary to close all ExportTCPGraphic
     * windows. This variable it's only used by the
     * <code>public static void invalidateInstances()</code> method.
     */
    private static HashMap<String, ExportTCPGraphic> instances = new HashMap<String, ExportTCPGraphic>(Analyzer.getInstance().getTamArrayStreams() + 1);
    /**
     *
     * Stores selected intervals per user
     */
    private SortedSet<HistogramInterval> histogramIntervals = new TreeSet<HistogramInterval>();
    /**
     *
     * All the occured times on simulation has stored here
     */
    private static SortedSet<Object> allTimes = null;
    /**
     *
     * All current stream occured times on simulation has stored here
     */
    private SortedSet<Object> times = null;
    //------------------------------------------------------------------------//
    //------- StringBuilders that make scripts  ------------------------------//
    //------------------------------------------------------------------------//
    /**
     *
     * Stores all configurations of environment to be loaded on gnuplot.
     */
    private StringBuilder environmentSettings;
    /**
     *
     * Stores the line that is responsible to make a line plot on GNU-plot.
     */
    private StringBuilder linePlotCommand;
    /**
     *
     * Stores the line that is responsible by make a histogram plot on GNU-plot.
     */
    private StringBuilder histogramPlotCommand;
    /**
     *
     * The name of file that will contain a data to be plotted. They will be
     * saved on same directory of script
     */
    String outputData_FileName;
    /**
     *
     * Used to know which is the current stream that will be treated; If
     * actualStream equal -1, then represent all streams.
     */
    private int actualStream;
    /**
     *
     * streamToPlot indicator of which stream-calcs will be plotted (Delay, PDV
     * or IPDV) Initially "Delay" because this is the default when window is
     * created.
     */
    private StreamMetric metricToPlot = StreamMetric.DELAY;
    /**
     *
     * The name of file that will contain the script
     */
    private File scriptFile;
    /**
     * The file stored on tmp dir, used to store all data necessary to plot a
     * preview of lines.
     */
    private File previewLinesOutputData_File = null;
    /**
     * The file stored on tmp dir, used to store all data necessary to plot a
     * preview of lines.
     */
    private File previewHistogramOutputData_File = null;
    /**
     *
     * Stores the last used directory to save file
     */
    private static String lastOpenedDirectory = System.getProperty("user.home");
    /**
     *
     * "File Chooser" for select a directory and file to store script.
     */
    private JFileChooser fileChooser = new JFileChooser(lastOpenedDirectory);
    /**
     * A window to manage output configurations
     */
    private TerminalConfigurator configurator;// = new TerminalConfigurator(new File( lineTitle.replace(<n>, lineTitle) + "_" + metricToPlot + ".txt"));
    /**
     * The selected output terminal
     */
    private TerminalTypes output;
    //Writers for file that will contain the script and a file that will contain
    //the data to be ploted.
    private FileWriter scriptWriter, dataWriter;
    /**
     * Used to know the number of events occurred on stream. It's useful for
     * example, to know if is possible apply smooth on data plot (at least three
     * points)
     */
    private int singleStreamSize;
    //Help to estimate a correct y-range for line plot, case autoscale is not activated.
    private double minDelay = 0,
            maxDelay = 0,
            minPDV = 0,
            maxPDV = 0,
            minIPDV = 0,
            maxIPDV = 0;
    /**
     * Variables used to know the state of data for plot, to know if a new set
     * of data is requested or not.
     */
    private ExportWindowState oldValues, newValues;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructors, factories and other related methods">
    /**
     * Factory method that create a unique instance of class for each stream.
     *
     * @param streamToMakePlotScript stream that will be ploted. A unique number
     * or word "ALL". It's used as key to verify if a window for current stream
     * is already built; if not, a new instance of this class is created and
     * returned by factory method, otherwise a existing instance is recovered
     * and shown again.
     * 
     * @return An unique configured window for selected stream
     */
    public static ExportTCPGraphic windowFactory( String streamToMakePlotScript ) {

        if ( instances.containsKey(streamToMakePlotScript) ) {
            return instances.get(streamToMakePlotScript);
        }
        else {
            return new ExportTCPGraphic(streamToMakePlotScript);
        }
    }

    /**
     * @param node Based on the found devices on the analysis, this constructor
     * receive the number of the desired node to generate a window for configure
     * a script for GNU-plot.
     */
    private ExportTCPGraphic( String streamInformation ) {
        initComponents();
        this.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
        Point location = Utils.getCentralCoords(Toolkit.getDefaultToolkit().getScreenSize(), this.getBounds());
        this.setLocation(location);

        //Configure frame that will show the large image of 'preview' plot.
        zoomImage.setResizable(true);
        zoomImage.setMinimumSize(new Dimension(820, 640));
        zoomImage.setSize(820, 640);
        zoomImageLabel.setMinimumSize(zoomImage.getMinimumSize());
        zoomImage.setDefaultCloseOperation(HIDE_ON_CLOSE);
        zoomImagePanel.add(zoomImageLabel);
        zoomImagePanel.setMinimumSize(zoomImage.getMinimumSize());
        zoomImagePanel.setBackground(Color.WHITE);
        zoomImage.add(zoomImagePanel);
        location = Utils.getCentralCoords(Toolkit.getDefaultToolkit().getScreenSize(), zoomImage.getBounds());
        zoomImage.setLocation(location);
        zoomImage.pack();
        //End of configuration of frame

        //Make the zoom cursor
        try {
            Point hotSpot = new Point(20, 20);
            Cursor zoomCursom = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(getClass().getResource("/analyzer/gui/images/zoom_in.png")).getImage(), hotSpot, "Zoom");
            jL_preview.setCursor(zoomCursom);
        }
        catch ( NullPointerException npe ) {
        }
        //end cursor configs

        //Ensure the button size
        jB_makePreview.setBounds(15, 15, jB_makePreview.getSize().width, jB_makePreview.getSize().height);
        //Configure cursor, for when passing in label, appear like a magnifying glass
        jL_outputHelpIcon.setToolTipText(TerminalTypes.getDefaultTerminal().getDescription());
        jCoB_output.setToolTipText(TerminalTypes.getDefaultTerminal().getDescription());

        //Add this form in a hashmap, for future recovery, if necessary.
        addInstance(streamInformation);

        //Take the name of opened file, the stream that will be plotted and a title of graph to ensure a unique name file
        configureOutputDataFileName();

        int node;
        if ( streamInformation.toLowerCase().equals("all") ) {
            actualStream = -1;
            lineTitle = "All streams comparation";
            histogramTitle = "All streams comparation";
            histogramXlabelTitle = "Streams";
            jTF_TitleInput.setText(lineTitle);
            this.setTitle("Export graphic for all streams (" + Main.getInstance().getOpenedFileName() + " )");

            singleStreamSize = 0;
            for ( TcpStream i : Analyzer.getInstance().getTcpStreams().values() ) {
                if ( i.delay.size() > singleStreamSize ) {
                    singleStreamSize = i.delay.size();
                }
            }
            if ( singleStreamSize <= 3 ) {
                jCB_autoRangeX.setSelected(true);
                jCB_autoRangeX.setEnabled(false);
            }


            //<editor-fold defaultstate="collapsed" desc="Configure the ComboBoxes of XRange with all possible values">

            if ( allTimes == null ) {
                allTimes = new TreeSet<Object>();
                for ( int j = 0 ; j < Analyzer.getInstance().getTamArrayStreams() ; j++ ) {
                    for ( int i = 0 ; i < Analyzer.getInstance().getStream(j).delay.size() ; i++ ) {
                        allTimes.add(Analyzer.getInstance().getStream(j).delay.get(i).getTxTime());
                    }
                }
            }

            Object[] streamsTimesVector = allTimes.toArray();
            //Configure for line plot
            jCoB_minimumX.setModel(new javax.swing.DefaultComboBoxModel<Object>(streamsTimesVector));
            jCoB_maximumX.setModel(new javax.swing.DefaultComboBoxModel<Object>(streamsTimesVector));
            jCoB_maximumX.setSelectedIndex(jCoB_maximumX.getItemCount() - 1);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Find the max and min values of  all Delays, PDVs and IPDVs for YRange">
            double tmp;
            for ( int j = 0 ; j < Analyzer.getInstance().getTamArrayStreams() ; j++ ) {
                for ( int i = 0 ; i < Analyzer.getInstance().getStream(j).delay.size() ; i++ ) {
                    tmp = Analyzer.getInstance().getStream(j).delay.get(i).getDelay();
                    if ( tmp != Double.MAX_VALUE ) {
                        if ( tmp < minDelay ) {
                            minDelay = tmp;
                        }
                        else if ( tmp > maxDelay ) {
                            maxDelay = tmp;
                        }
                        tmp = Analyzer.getInstance().getStream(j).getPDV()[i];
                        if ( tmp < minPDV ) {
                            minPDV = tmp;
                        }
                        else if ( tmp > maxPDV ) {
                            maxPDV = tmp;
                        }
                        tmp = Analyzer.getInstance().getStream(j).getIPDV()[i];
                        if ( tmp < minIPDV ) {
                            minIPDV = tmp;
                        }
                        else if ( tmp > maxIPDV ) {
                            maxIPDV = tmp;
                        }
                    }
                }
            }
            jCoB_minimumY.setSelectedItem(new Double(minDelay));
            jCoB_maximumY.setSelectedItem(new Double(maxDelay));
            //</editor-fold>


        }
        else {//If its only for one stream
            actualStream = Integer.parseInt(streamInformation);
            node = actualStream;
            singleStreamSize = Analyzer.getInstance().getStream(node).delay.size();
            lineTitle = "Stream " + actualStream;
            histogramTitle = "Stream " + actualStream;
            histogramXlabelTitle = "Selected stream";
            jTF_TitleInput.setText(lineTitle);
            this.setTitle("Export graphic for stream " + actualStream + " ( " + Main.getInstance().getOpenedFileName() + " )");

            //<editor-fold defaultstate="collapsed" desc="Configure the ComboBoxes of XRange">
            //@SuppressWarnings("UseOfObsoleteCollectionType")
            times = new TreeSet<Object>();
            for ( int i = 0 ; i < Analyzer.getInstance().getStream(node).delay.size() ; i++ ) {
                times.add(Analyzer.getInstance().getStream(node).delay.get(i).getTxTime());
            }

            //Configure for line plot
            jCoB_minimumX.setModel(new javax.swing.DefaultComboBoxModel<Object>(times.toArray()));
            jCoB_maximumX.setModel(new javax.swing.DefaultComboBoxModel<Object>(times.toArray()));
            jCoB_maximumX.setSelectedIndex(jCoB_maximumX.getItemCount() - 1);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Find the max and min values of Delay, PDV and IPDV for YRange">
            double tmp;
            for ( int i = 0 ; i < Analyzer.getInstance().getStream(node).delay.size() ; i++ ) {
                tmp = Analyzer.getInstance().getStream(node).delay.get(i).getDelay();
                if ( tmp != Double.MAX_VALUE ) {
                    if ( tmp < minDelay ) {
                        minDelay = tmp;
                    }
                    else if ( tmp > maxDelay ) {
                        maxDelay = tmp;
                    }
                    tmp = Analyzer.getInstance().getStream(node).getPDV()[i];
                    if ( tmp < minPDV ) {
                        minPDV = tmp;
                    }
                    else if ( tmp > maxPDV ) {
                        maxPDV = tmp;
                    }
                    tmp = Analyzer.getInstance().getStream(node).getIPDV()[i];
                    if ( tmp < minIPDV ) {
                        minIPDV = tmp;
                    }
                    else if ( tmp > maxIPDV ) {
                        maxIPDV = tmp;
                    }
                }
            }
            jCoB_minimumY.setSelectedItem(new Double(minDelay));
            jCoB_maximumY.setSelectedItem(new Double(maxDelay));
            //</editor-fold>

        }

        //Take a 'state' of data, used to only generate a new file for preview if necessary.
        oldValues = new ExportWindowState();
        newValues = new ExportWindowState();
        if ( actualStream == -1 ) {
            oldValues.updateValues(Double.parseDouble(allTimes.first().toString()), Double.parseDouble(allTimes.last().toString()), histogramIntervals, metricToPlot, ExportWindowState.TypeOfGraphic.LINES);
        }
        else {

            oldValues.updateValues(Double.parseDouble(times.first().toString()), Double.parseDouble(times.last().toString()), histogramIntervals, metricToPlot, ExportWindowState.TypeOfGraphic.LINES);
        }

        if ( singleStreamSize <= 3 ) {
            jCB_autoRangeX.setSelected(true);
            jCB_autoRangeX.setEnabled(false);
        }

        //Associate a configurator for this window
        configurator = new TerminalConfigurator(new File(jTF_TitleInput.getText() + "_(" + Main.getInstance().getOpenedFileName().replace(".tr", "") + ")_" + metricToPlot));


    }

    /**
     *
     * Adds a instance on a arrayList windows of ExportTCPGraphic instances.
     *
     * @param e Window that will be added.
     */
    private void addInstance( String key ) {
        instances.put(key, this);
    }

    /**
     *
     * Disposes all Export Graphic opens windows when a new analysis is
     * performed. That is necessary because the window's instances gets the data
     * directly from
     * <code>Analyzer</code> instance, causing a inconsistency of data.
     */
    public static void invalidateInstances() {
        for ( ExportTCPGraphic eg : instances.values() ) {
            eg.dispose();
        }
        instances.clear();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Others methods">
    /**
     * Find for each defined interval, the occurrences of all values of metric
     * selected (Delay, PDV or IPDV) on the stream selected (single or all).
     */
    private void findIntervalsOccurrences() {
        //Number of all packages on the stream
        int totalPkgCount;

        double[] values = null;

        if ( actualStream != -1 ) {
            totalPkgCount = Analyzer.getInstance().getStream(actualStream).delay.size();
            switch ( metricToPlot ) {
                case DELAY:
                    values = Analyzer.getInstance().getStream(actualStream).getDelay();
                    break;
                case PDV:
                    values = Analyzer.getInstance().getStream(actualStream).getPDV();
                    break;
                case IPDV:
                    values = Analyzer.getInstance().getStream(actualStream).getIPDV();
                    break;
            }
            try {
                for ( HistogramInterval interval : histogramIntervals ) {
                    interval.initializeStream(actualStream);
                    interval.setStreamPkgsCount(actualStream, totalPkgCount);
                    for ( Double d : values ) {
                        if ( interval.contains(d) ) {
                            interval.increaseOccurrences(actualStream);
                        }
                    }
                    interval.setTxOccurrence(totalPkgCount, actualStream);
                }
            }
            catch ( NotInitializedStreamInterval notInitializedStreamInterval ) {
                //TODO implementar bugreport aqui
            }
            catch ( InconsistentInterval inconsistentInterval ) {
                //TODO
            }
        }
        else {
            for ( int currentStream = 0 ; currentStream < Analyzer.getInstance().getTamArrayStreams() ; currentStream++ ) {
                totalPkgCount = Analyzer.getInstance().getStream(currentStream).delay.size();
                switch ( metricToPlot ) {
                    case DELAY:
                        values = Analyzer.getInstance().getStream(currentStream).getDelay();
                        break;
                    case PDV:
                        values = Analyzer.getInstance().getStream(currentStream).getPDV();
                        break;
                    case IPDV:
                        values = Analyzer.getInstance().getStream(currentStream).getIPDV();
                        break;
                }
                try {
                    for ( HistogramInterval interval : histogramIntervals ) {
                        interval.initializeStream(currentStream);
                        interval.setStreamPkgsCount(actualStream, totalPkgCount);
                        for ( Double d : values ) {
                            if ( interval.contains(d) ) {
                                interval.increaseOccurrences(currentStream);
                            }
                        }
                        interval.setTxOccurrence(totalPkgCount, currentStream);
                    }
                }
                catch ( NotInitializedStreamInterval notInitializedStreamInterval ) {
                    //TODO implementar BugReport aqui
                }
                catch ( InconsistentInterval inconsistentInterval ) {
                    //TODO
                }
            }
        }

    }

    /**
     *
     * outputData_FileName is a variable that store the name of the file that
     * will contain all data to generate a plot. The only function of method is
     * update the name, in these some situations:<br/> <li>The construction of
     * class</li> <li>At click on some button plot (line or histogram)</li>
     * <li>At open the output setup window</li>
     */
    private void configureOutputDataFileName() {
        //Refresh variable
        outputData_FileName = jTF_TitleInput.getText() + "_(" + Main.getInstance().getOpenedFileName().replace(".tr", "") + ")_" + metricToPlot + ".dat";
    }

    private void tryPlot() {

        plotter.tryPlot(fileChooser.getSelectedFile());

        if ( plotter.isSuccessfull() && output.isImageOutput() ) {
        }

    }

    private int showFileChooserDialog() {
        fileChooser = new JFileChooser(lastOpenedDirectory);
        fileChooser.setDialogTitle("Save GNUPlot script and data file");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.updateUI();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setSelectedFile(new File(jTF_TitleInput.getText() + "_(" + Main.getInstance().getOpenedFileName().replace(".tr", "") + ")_" + metricToPlot + ".txt"));

        int returnValue = fileChooser.showSaveDialog(null);
        if ( fileChooser.getSelectedFile().exists() ) {
            if ( JOptionPane.showConfirmDialog(null, "The file <file> already exist. Overwrite?".replace("<file>", fileChooser.getSelectedFile().toString()), "Overwrite file?", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION ) {
                returnValue = JFileChooser.CANCEL_OPTION;
            }

        }

        lastOpenedDirectory = fileChooser.getCurrentDirectory().toString();

        return returnValue;
    }

    /**
     * When a range of values on lines, or the interval set is updated, or the
     * data to analyze is changed (PDV,IPDV,DELAY), a change on data file is
     * necessary. It's used for to know when is necessary update the data file
     * for plot preview.
     *
     * @return True if necessary, else if not.
     *
     * @since 1.2.0
     */
    public boolean dataFileNeedUpdate() {
        @SuppressWarnings("UnusedAssignment")
        TypeOfGraphic lastTab = null;
        if ( jB_GenerateLineScript.isShowing() ) {
            lastTab = TypeOfGraphic.LINES;
        }
        else {
            lastTab = TypeOfGraphic.HISTOGRAM;
        }
        newValues.updateValues(Double.parseDouble(jCoB_minimumX.getSelectedItem().toString()), Double.parseDouble(jCoB_maximumX.getSelectedItem().toString()), histogramIntervals, metricToPlot, lastTab);
        if ( newValues.equals(oldValues) ) {
            return false;
        }
        else {
            oldValues = newValues;
            newValues = new ExportWindowState();
            return true;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        bg_outputFormat = new javax.swing.ButtonGroup();
        bg_streamToPlot = new javax.swing.ButtonGroup();
        bg_histogramStyle = new javax.swing.ButtonGroup();
        bg_histogramFill = new javax.swing.ButtonGroup();
        bg_legendPosition = new javax.swing.ButtonGroup();
        jSP_generalOptions = new javax.swing.JScrollPane();
        jP_generalOptions = new javax.swing.JPanel();
        jL_titleLabel = new javax.swing.JLabel();
        jL_xAxisLabel = new javax.swing.JLabel();
        jL_yAxisLabel = new javax.swing.JLabel();
        jL_OutputFormat = new javax.swing.JLabel();
        jTF_TitleInput = new javax.swing.JTextField();
        jTF_Xaxis = new javax.swing.JTextField();
        jTF_Yaxis = new javax.swing.JTextField();
        jL_StreamsToPlot = new javax.swing.JLabel();
        jRB_IPDV = new javax.swing.JRadioButton();
        jRB_Delay = new javax.swing.JRadioButton();
        jRB_PDV = new javax.swing.JRadioButton();
        jP_gridConfigs = new javax.swing.JPanel();
        jCB_enableGrid = new javax.swing.JCheckBox();
        jCB_gridOnX = new javax.swing.JCheckBox();
        jCB_gridOnY = new javax.swing.JCheckBox();
        jL_GridLineWidth = new javax.swing.JLabel();
        jSpinner_gridLineWidth = new javax.swing.JSpinner();
        jB_outputSetupOptions = new javax.swing.JButton();
        jCB_autoGenerateOutput = new javax.swing.JCheckBox();
        jCoB_output = new javax.swing.JComboBox();
        jL_outputHelpIcon = new javax.swing.JLabel();
        jSP_plotsTypes = new javax.swing.JScrollPane();
        jTP_plotsTypes = new javax.swing.JTabbedPane();
        jP_Lines = new javax.swing.JPanel();
        jB_GenerateLineScript = new javax.swing.JButton();
        jP_XRange = new javax.swing.JPanel();
        jCoB_maximumX = new javax.swing.JComboBox();
        jL_MinimumX = new javax.swing.JLabel();
        jCoB_minimumX = new javax.swing.JComboBox();
        jL_maximumX = new javax.swing.JLabel();
        jCB_Steps_X_Tics = new javax.swing.JCheckBox();
        jL_xAxes = new javax.swing.JLabel();
        jSpinner_customXtics = new javax.swing.JSpinner();
        jCB_autoRangeX = new javax.swing.JCheckBox();
        jP_YRange = new javax.swing.JPanel();
        jL_yAxes = new javax.swing.JLabel();
        jL_minimumY = new javax.swing.JLabel();
        jCoB_minimumY = new javax.swing.JComboBox();
        jL_maximumY = new javax.swing.JLabel();
        jCoB_maximumY = new javax.swing.JComboBox();
        jCB_Steps_Y_Tics = new javax.swing.JCheckBox();
        jSpinner_customYtics = new javax.swing.JSpinner();
        jCB_autoRangeY = new javax.swing.JCheckBox();
        jP_Logscale = new javax.swing.JPanel();
        jCB_EnableLogScale = new javax.swing.JCheckBox();
        jCB_YlogBase = new javax.swing.JCheckBox();
        jCB_XlogBase = new javax.swing.JCheckBox();
        jL_logScaleOnAxis = new javax.swing.JLabel();
        jCoB_YlogBase = new javax.swing.JComboBox();
        jCoB_XlogBase = new javax.swing.JComboBox();
        jL_warningIPDV = new javax.swing.JLabel();
        jL_warningIPDV.setVisible(false);
        jP_Histogram = new javax.swing.JPanel();
        jB_GenerateHistogramScript = new javax.swing.JButton();
        jP_histogramIntervals = new javax.swing.JPanel();
        jB_delInterval = new javax.swing.JButton();
        jScrollPane_intervalsList = new javax.swing.JScrollPane();
        jList_intervals = new javax.swing.JList();
        jL_beginInterval = new javax.swing.JLabel();
        jL_intervalsHistogram = new javax.swing.JLabel();
        jB_addInterval = new javax.swing.JButton();
        jL_endInterval = new javax.swing.JLabel();
        jTF_beginHistogramInverval = new javax.swing.JTextField();
        jTF_endHistogramInverval = new javax.swing.JTextField();
        jB_clearAllIntervals = new javax.swing.JButton();
        jP_histogramLogscale = new javax.swing.JPanel();
        jCB_histogramEnableLogScale = new javax.swing.JCheckBox();
        jL_histogramLogScale = new javax.swing.JLabel();
        jCoB_histogramYlogBase = new javax.swing.JComboBox();
        jCB_histogramSteps_Y_Tics = new javax.swing.JCheckBox();
        jSpinner_histogramCustomYtics = new javax.swing.JSpinner();
        jSP_smoothAndStyles = new javax.swing.JScrollPane();
        JTP_smoothAndStyles = new javax.swing.JTabbedPane();
        jP_smoothAndStyleforLines = new javax.swing.JPanel();
        jCoB_smoothTypes = new javax.swing.JComboBox();
        jCoB_simpleStylesType = new javax.swing.JComboBox();
        jCB_rotateXlabel = new javax.swing.JCheckBox();
        jL_smoothToUse = new javax.swing.JLabel();
        jL_plotStyle = new javax.swing.JLabel();
        jCoB_decimalSign = new javax.swing.JComboBox();
        jL_decimalSignToShow = new javax.swing.JLabel();
        jCoB_timeUnit = new javax.swing.JComboBox();
        jSpinner_rotateXLabel = new javax.swing.JSpinner();
        jP_widths = new javax.swing.JPanel();
        jL_lineWidth = new javax.swing.JLabel();
        jL_boxWidth = new javax.swing.JLabel();
        jL_pointWidth = new javax.swing.JLabel();
        jSpinner_pointWidth = new javax.swing.JSpinner();
        jSpinner_boxWidth = new javax.swing.JSpinner();
        jSpinner_lineWidth = new javax.swing.JSpinner();
        jCB_timeOnXaxis = new javax.swing.JCheckBox();
        jP_smoothAndStyleforHistogram = new javax.swing.JPanel();
        jRB_clusteredHistogram = new javax.swing.JRadioButton();
        jRB_stackedHistogram = new javax.swing.JRadioButton();
        jL_histogramStyle = new javax.swing.JLabel();
        jSpinner_histogramGapSize = new javax.swing.JSpinner();
        jL_histogramGapSize = new javax.swing.JLabel();
        jL_fillUsing = new javax.swing.JLabel();
        jRB_histogramFillPatterns = new javax.swing.JRadioButton();
        jRB_histogramFillSolid = new javax.swing.JRadioButton();
        jL_histogramBoxWidth = new javax.swing.JLabel();
        jSpinner_histogramBoxWidth = new javax.swing.JSpinner();
        jCB_histogramRotateXlabel = new javax.swing.JCheckBox();
        jSpinner_histogramRotateXLabel = new javax.swing.JSpinner();
        jCoB_histogramDecimalSign = new javax.swing.JComboBox();
        jL_histogramDecimalSignToShow = new javax.swing.JLabel();
        jCB_boxesWithoutBorder = new javax.swing.JCheckBox();
        jP_legends = new javax.swing.JPanel();
        jCB_legendsOutsideLegends = new javax.swing.JCheckBox();
        jCB_legendsFillWithBorder = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jTB_legendLeftBottom = new javax.swing.JToggleButton();
        jTB_legendRightBottom = new javax.swing.JToggleButton();
        jTB_legendRightTop = new javax.swing.JToggleButton();
        jTB_legendCenterBottom = new javax.swing.JToggleButton();
        jTB_legendCenterTop = new javax.swing.JToggleButton();
        jTB_legendLeftTop = new javax.swing.JToggleButton();
        jTB_legendLeftCenter = new javax.swing.JToggleButton();
        jTB_legendCenterCenter = new javax.swing.JToggleButton();
        jTB_legendRightCenter = new javax.swing.JToggleButton();
        jL_legendsLegendPosition = new javax.swing.JLabel();
        jCB_legendsHorizontalAlignment = new javax.swing.JCheckBox();
        jCB_legendsOpaque = new javax.swing.JCheckBox();
        jP_graphicView = new javax.swing.JPanel();
        jLP_graphicView = new javax.swing.JLayeredPane();
        jL_preview = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(930, 660));
        setPreferredSize(new java.awt.Dimension(930, 660));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jSP_generalOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "General options"));
        jSP_generalOptions.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jSP_generalOptions.setToolTipText("");
        jSP_generalOptions.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jP_generalOptions.setMaximumSize(new java.awt.Dimension(428, 248));

        jL_titleLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_titleLabel.setText("Title");
        jL_titleLabel.setToolTipText("<html>\nThe text that will be shown as a title of image at the top.\n</html>");

        jL_xAxisLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_xAxisLabel.setText("X-axis label");
        jL_xAxisLabel.setToolTipText("<html>\nThe text that will be shown as label of the X axis\n</html>");

        jL_yAxisLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_yAxisLabel.setText("Y-axis label");
        jL_yAxisLabel.setToolTipText("<html>\nThe text that will be shown as label of the Y axis\n</html>");

        jL_OutputFormat.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_OutputFormat.setText("Output format");
        jL_OutputFormat.setToolTipText("<html>\nIndicates what will be output, but not how will be saved by TraceMetrics<b>!</b><br>\n</html>");

        jTF_TitleInput.setText("Stream <n>");
        jTF_TitleInput.setToolTipText("<html>\nThe text that will be shown as a title of image at the top.\n</html>");

        jTF_Xaxis.setText("Elapsed time");
        jTF_Xaxis.setToolTipText("<html>\nThe text that will be shown as label of the X axis\n</html>");

        jTF_Yaxis.setText("Delay variation");
        jTF_Yaxis.setToolTipText("<html>\nThe text that will be shown as label of the Y axis\n</html>");

        jL_StreamsToPlot.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_StreamsToPlot.setText("Data to plot");

        bg_streamToPlot.add(jRB_IPDV);
        jRB_IPDV.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jRB_IPDV.setText("IPDV");
        jRB_IPDV.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRB_IPDVItemStateChanged(evt);
            }
        });

        bg_streamToPlot.add(jRB_Delay);
        jRB_Delay.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jRB_Delay.setSelected(true);
        jRB_Delay.setText("Delay");
        jRB_Delay.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRB_DelayItemStateChanged(evt);
            }
        });

        bg_streamToPlot.add(jRB_PDV);
        jRB_PDV.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jRB_PDV.setText("PDV");
        jRB_PDV.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRB_PDVItemStateChanged(evt);
            }
        });

        jP_gridConfigs.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));

        jCB_enableGrid.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_enableGrid.setText("Enable grid");
        jCB_enableGrid.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_enableGridItemStateChanged(evt);
            }
        });

        jCB_gridOnX.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_gridOnX.setText("X grid");
        jCB_gridOnX.setEnabled(false);
        jCB_gridOnX.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_gridOnXItemStateChanged(evt);
            }
        });

        jCB_gridOnY.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_gridOnY.setText("Y grid");
        jCB_gridOnY.setEnabled(false);
        jCB_gridOnY.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_gridOnYItemStateChanged(evt);
            }
        });

        jL_GridLineWidth.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_GridLineWidth.setText("grid line width");
        jL_GridLineWidth.setEnabled(false);

        jSpinner_gridLineWidth.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jSpinner_gridLineWidth.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.2f), Float.valueOf(5.0f), Float.valueOf(0.2f)));
        jSpinner_gridLineWidth.setEnabled(false);

        javax.swing.GroupLayout jP_gridConfigsLayout = new javax.swing.GroupLayout(jP_gridConfigs);
        jP_gridConfigs.setLayout(jP_gridConfigsLayout);
        jP_gridConfigsLayout.setHorizontalGroup(
            jP_gridConfigsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_gridConfigsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCB_enableGrid)
                .addGap(20, 20, 20)
                .addComponent(jCB_gridOnX)
                .addGap(18, 18, 18)
                .addComponent(jCB_gridOnY)
                .addGap(14, 14, 14)
                .addComponent(jL_GridLineWidth)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSpinner_gridLineWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jP_gridConfigsLayout.setVerticalGroup(
            jP_gridConfigsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_gridConfigsLayout.createSequentialGroup()
                .addGap(0, 7, Short.MAX_VALUE)
                .addGroup(jP_gridConfigsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCB_enableGrid, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCB_gridOnX)
                    .addComponent(jCB_gridOnY)
                    .addComponent(jL_GridLineWidth)
                    .addComponent(jSpinner_gridLineWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jB_outputSetupOptions.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jB_outputSetupOptions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/gear.png"))); // NOI18N
        jB_outputSetupOptions.setToolTipText("Configure output settings");
        jB_outputSetupOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_outputSetupOptionsActionPerformed(evt);
            }
        });

        jCB_autoGenerateOutput.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_autoGenerateOutput.setText("Auto generate output");
        jCB_autoGenerateOutput.setToolTipText("<html>\nIf gnuplot are installed on your computer and selected terminal output is supported,<br/>\ntracemetrics will try to  plot your script and save on the same folder of script.\n</html>");
        jCB_autoGenerateOutput.setEnabled(false);

        jCoB_output.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_output.setModel(new javax.swing.DefaultComboBoxModel(TerminalTypes.values()));
        jCoB_output.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jCoB_outputMouseEntered(evt);
            }
        });
        jCoB_output.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCoB_outputItemStateChanged(evt);
            }
        });

        jL_outputHelpIcon.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_outputHelpIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/system_help.png"))); // NOI18N
        jL_outputHelpIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jL_outputHelpIconMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jP_generalOptionsLayout = new javax.swing.GroupLayout(jP_generalOptions);
        jP_generalOptions.setLayout(jP_generalOptionsLayout);
        jP_generalOptionsLayout.setHorizontalGroup(
            jP_generalOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_generalOptionsLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jP_generalOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jL_xAxisLabel)
                    .addComponent(jL_yAxisLabel)
                    .addComponent(jL_titleLabel)
                    .addGroup(jP_generalOptionsLayout.createSequentialGroup()
                        .addComponent(jL_OutputFormat)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jP_generalOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jP_generalOptionsLayout.createSequentialGroup()
                                .addComponent(jCoB_output, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(jL_outputHelpIcon)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCB_autoGenerateOutput)
                                .addGap(18, 18, 18)
                                .addComponent(jB_outputSetupOptions, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jTF_Xaxis, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTF_Yaxis, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTF_TitleInput, javax.swing.GroupLayout.Alignment.LEADING)))
                    .addGroup(jP_generalOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jP_generalOptionsLayout.createSequentialGroup()
                            .addComponent(jL_StreamsToPlot)
                            .addGap(45, 45, 45)
                            .addComponent(jRB_Delay)
                            .addGap(63, 63, 63)
                            .addComponent(jRB_PDV, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jRB_IPDV))
                        .addComponent(jP_gridConfigs, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        jP_generalOptionsLayout.setVerticalGroup(
            jP_generalOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_generalOptionsLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jP_generalOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jL_titleLabel)
                    .addComponent(jTF_TitleInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jP_generalOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jL_xAxisLabel)
                    .addComponent(jTF_Xaxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jP_generalOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jL_yAxisLabel)
                    .addComponent(jTF_Yaxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jP_generalOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jCB_autoGenerateOutput)
                    .addComponent(jB_outputSetupOptions)
                    .addComponent(jCoB_output, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jL_OutputFormat)
                    .addComponent(jL_outputHelpIcon))
                .addGap(10, 10, 10)
                .addComponent(jP_gridConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jP_generalOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRB_IPDV)
                    .addComponent(jRB_PDV)
                    .addComponent(jRB_Delay)
                    .addComponent(jL_StreamsToPlot, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jSP_generalOptions.setViewportView(jP_generalOptions);

        jSP_plotsTypes.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Type of graphic"));
        jSP_plotsTypes.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jSP_plotsTypes.setToolTipText("");

        jTP_plotsTypes.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTP_plotsTypesStateChanged(evt);
            }
        });

        jP_Lines.setMaximumSize(new java.awt.Dimension(456, 345));

        jB_GenerateLineScript.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jB_GenerateLineScript.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/document_save.png"))); // NOI18N
        jB_GenerateLineScript.setText("Generate GNU-Plot Script");
        jB_GenerateLineScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_GenerateLineScriptActionPerformed(evt);
            }
        });

        jP_XRange.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jCoB_maximumX.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_maximumX.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCoB_maximumXItemStateChanged(evt);
            }
        });

        jL_MinimumX.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_MinimumX.setText("Minimum x");

        jCoB_minimumX.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_minimumX.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCoB_minimumXItemStateChanged(evt);
            }
        });

        jL_maximumX.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_maximumX.setText("Maximum x");

        jCB_Steps_X_Tics.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_Steps_X_Tics.setText("Custom steps between x tics");
        jCB_Steps_X_Tics.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_Steps_X_TicsItemStateChanged(evt);
            }
        });

        jL_xAxes.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_xAxes.setText("X Range");

        jSpinner_customXtics.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jSpinner_customXtics.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), null, Float.valueOf(0.1f)));
        jSpinner_customXtics.setEnabled(false);

        jCB_autoRangeX.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_autoRangeX.setSelected(true);
        jCB_autoRangeX.setText("Autorange");
        jCB_autoRangeX.setToolTipText("<html>\nGives a larger space in the vicinity of the terminal,<br>\nto better accommodate the points in left and right.\n</html>");

        javax.swing.GroupLayout jP_XRangeLayout = new javax.swing.GroupLayout(jP_XRange);
        jP_XRange.setLayout(jP_XRangeLayout);
        jP_XRangeLayout.setHorizontalGroup(
            jP_XRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_XRangeLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jP_XRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jP_XRangeLayout.createSequentialGroup()
                        .addComponent(jL_MinimumX)
                        .addGap(18, 18, 18)
                        .addComponent(jCoB_minimumX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jL_maximumX)
                        .addGap(18, 18, 18)
                        .addComponent(jCoB_maximumX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jP_XRangeLayout.createSequentialGroup()
                        .addComponent(jCB_Steps_X_Tics)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner_customXtics, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jP_XRangeLayout.createSequentialGroup()
                        .addComponent(jL_xAxes)
                        .addGap(18, 18, 18)
                        .addComponent(jCB_autoRangeX)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jP_XRangeLayout.setVerticalGroup(
            jP_XRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_XRangeLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jP_XRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jL_xAxes)
                    .addComponent(jCB_autoRangeX))
                .addGap(3, 3, 3)
                .addGroup(jP_XRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jL_MinimumX)
                    .addComponent(jCoB_minimumX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCoB_maximumX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jL_maximumX))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jP_XRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCB_Steps_X_Tics)
                    .addComponent(jSpinner_customXtics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 8, Short.MAX_VALUE))
        );

        jP_YRange.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jL_yAxes.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_yAxes.setText("Y Range");

        jL_minimumY.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_minimumY.setText("Minimum y");
        jL_minimumY.setToolTipText("<html>\nTraceMetrics will try to select the correct value<br>\nthat be abrange all your data.<br>\n<i>If you know the correctly range, deselect the <b>autorange</b> checkbox</i><br>\n</html>");
        jL_minimumY.setEnabled(false);

        jCoB_minimumY.setEditable(true);
        jCoB_minimumY.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_minimumY.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0" }));
        jCoB_minimumY.setToolTipText("<html>\nTraceMetrics will try to select the correct value<br>\nthat be abrange all your data.<br>\n<i>If you know the correctly range, deselect the <b>autorange</b> checkbox</i><br>\n</html>");
        jCoB_minimumY.setEnabled(false);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jCoB_minimumX, org.jdesktop.beansbinding.ELProperty.create("${preferredSize}"), jCoB_minimumY, org.jdesktop.beansbinding.BeanProperty.create("preferredSize"));
        bindingGroup.addBinding(binding);

        jL_maximumY.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_maximumY.setText("Maximum y");
        jL_maximumY.setToolTipText("<html>\nTraceMetrics will try to select the correct value<br>\nthat be abrange all your data.<br>\n<i>If you know the correctly range, deselect the <b>autorange</b> checkbox</i><br>\n</html>");
        jL_maximumY.setEnabled(false);

        jCoB_maximumY.setEditable(true);
        jCoB_maximumY.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_maximumY.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "100" }));
        jCoB_maximumY.setToolTipText("<html>\nTraceMetrics will try to select the correct value<br>\nthat be abrange all your data.<br>\n<i>If you know the correctly range, deselect the <b>autorange</b> checkbox</i><br>\n</html>");
        jCoB_maximumY.setEnabled(false);

        jCB_Steps_Y_Tics.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_Steps_Y_Tics.setText("Custom steps between y tics");
        jCB_Steps_Y_Tics.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_Steps_Y_TicsItemStateChanged(evt);
            }
        });

        jSpinner_customYtics.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jSpinner_customYtics.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), null, Float.valueOf(0.1f)));
        jSpinner_customYtics.setEnabled(false);

        jCB_autoRangeY.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_autoRangeY.setSelected(true);
        jCB_autoRangeY.setText("Autorange");
        jCB_autoRangeY.setToolTipText("<html>\nDelegate to gnuplot the function of adjust your graph.<br>\nThe border's data will be ploted on the margin of output image.<br>\n<b>Is recomendade use this option</b><br>\n</html>");
        jCB_autoRangeY.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_autoRangeYItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jP_YRangeLayout = new javax.swing.GroupLayout(jP_YRange);
        jP_YRange.setLayout(jP_YRangeLayout);
        jP_YRangeLayout.setHorizontalGroup(
            jP_YRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_YRangeLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jP_YRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jP_YRangeLayout.createSequentialGroup()
                        .addComponent(jL_minimumY)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCoB_minimumY, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jL_maximumY)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCoB_maximumY, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jP_YRangeLayout.createSequentialGroup()
                        .addComponent(jL_yAxes)
                        .addGap(18, 18, 18)
                        .addComponent(jCB_autoRangeY))
                    .addGroup(jP_YRangeLayout.createSequentialGroup()
                        .addComponent(jCB_Steps_Y_Tics)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner_customYtics, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jP_YRangeLayout.setVerticalGroup(
            jP_YRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jP_YRangeLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(jP_YRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jL_yAxes)
                    .addComponent(jCB_autoRangeY))
                .addGap(5, 5, 5)
                .addGroup(jP_YRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jL_maximumY)
                    .addComponent(jCoB_minimumY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jL_minimumY)
                    .addComponent(jCoB_maximumY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jP_YRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCB_Steps_Y_Tics)
                    .addComponent(jSpinner_customYtics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jP_Logscale.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jCB_EnableLogScale.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_EnableLogScale.setText("Enable logaritmic scale using custom base");
        jCB_EnableLogScale.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_EnableLogScaleItemStateChanged(evt);
            }
        });

        jCB_YlogBase.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_YlogBase.setText("Y log scale base");
        jCB_YlogBase.setEnabled(false);
        jCB_YlogBase.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_YlogBaseItemStateChanged(evt);
            }
        });

        jCB_XlogBase.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_XlogBase.setText("X log scale base");
        jCB_XlogBase.setEnabled(false);
        jCB_XlogBase.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_XlogBaseItemStateChanged(evt);
            }
        });

        jL_logScaleOnAxis.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_logScaleOnAxis.setText("Log scale on axis");

        jCoB_YlogBase.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_YlogBase.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "e", "2", "10" }));
        jCoB_YlogBase.setEnabled(false);

        jCoB_XlogBase.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_XlogBase.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "e", "2", "10" }));
        jCoB_XlogBase.setEnabled(false);

        jL_warningIPDV.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jL_warningIPDV.setForeground(new java.awt.Color(255, 0, 51));
        jL_warningIPDV.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jL_warningIPDV.setText("Log scale won't accept negatives values. IPDV it has.");
        jL_warningIPDV.setToolTipText("IPDV has negatives values, and a log scale on Y won't accept negative values!");

        javax.swing.GroupLayout jP_LogscaleLayout = new javax.swing.GroupLayout(jP_Logscale);
        jP_Logscale.setLayout(jP_LogscaleLayout);
        jP_LogscaleLayout.setHorizontalGroup(
            jP_LogscaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_LogscaleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jP_LogscaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jP_LogscaleLayout.createSequentialGroup()
                        .addComponent(jCB_EnableLogScale)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jP_LogscaleLayout.createSequentialGroup()
                        .addComponent(jCB_XlogBase)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCoB_XlogBase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCB_YlogBase)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCoB_YlogBase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jP_LogscaleLayout.createSequentialGroup()
                        .addComponent(jL_logScaleOnAxis)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                        .addComponent(jL_warningIPDV)))
                .addContainerGap())
        );
        jP_LogscaleLayout.setVerticalGroup(
            jP_LogscaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jP_LogscaleLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(jP_LogscaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jL_logScaleOnAxis, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jL_warningIPDV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(5, 5, 5)
                .addComponent(jCB_EnableLogScale, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jP_LogscaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCB_YlogBase)
                    .addComponent(jCB_XlogBase)
                    .addComponent(jCoB_YlogBase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCoB_XlogBase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5))
        );

        javax.swing.GroupLayout jP_LinesLayout = new javax.swing.GroupLayout(jP_Lines);
        jP_Lines.setLayout(jP_LinesLayout);
        jP_LinesLayout.setHorizontalGroup(
            jP_LinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_LinesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jP_LinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jP_Logscale, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jP_XRange, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jP_YRange, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jB_GenerateLineScript, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jP_LinesLayout.setVerticalGroup(
            jP_LinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_LinesLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jP_Logscale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(jP_XRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(jP_YRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jB_GenerateLineScript)
                .addContainerGap())
        );

        jTP_plotsTypes.addTab("Lines", jP_Lines);

        jB_GenerateHistogramScript.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jB_GenerateHistogramScript.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/document_save.png"))); // NOI18N
        jB_GenerateHistogramScript.setText("Generate GNU-Plot Script");
        jB_GenerateHistogramScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_GenerateHistogramScriptActionPerformed(evt);
            }
        });

        jB_delInterval.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jB_delInterval.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/list_remove.png"))); // NOI18N
        jB_delInterval.setText("Remove");
        jB_delInterval.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_delIntervalActionPerformed(evt);
            }
        });

        jList_intervals.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jScrollPane_intervalsList.setViewportView(jList_intervals);

        jL_beginInterval.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_beginInterval.setText("Begin");

        jL_intervalsHistogram.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_intervalsHistogram.setText("Intervals (seconds)");

        jB_addInterval.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jB_addInterval.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/list_add.png"))); // NOI18N
        jB_addInterval.setText("Add");
        jB_addInterval.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_addIntervalActionPerformed(evt);
            }
        });

        jL_endInterval.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_endInterval.setText("End");

        jTF_beginHistogramInverval.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTF_beginHistogramInvervalKeyTyped(evt);
            }
        });

        jTF_endHistogramInverval.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTF_endHistogramInvervalKeyTyped(evt);
            }
        });

        jB_clearAllIntervals.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jB_clearAllIntervals.setText("Remove All");
        jB_clearAllIntervals.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_clearAllIntervalsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jP_histogramIntervalsLayout = new javax.swing.GroupLayout(jP_histogramIntervals);
        jP_histogramIntervals.setLayout(jP_histogramIntervalsLayout);
        jP_histogramIntervalsLayout.setHorizontalGroup(
            jP_histogramIntervalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jP_histogramIntervalsLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jP_histogramIntervalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jP_histogramIntervalsLayout.createSequentialGroup()
                        .addComponent(jL_intervalsHistogram)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jP_histogramIntervalsLayout.createSequentialGroup()
                        .addGroup(jP_histogramIntervalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jP_histogramIntervalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jB_addInterval, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jB_clearAllIntervals, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jB_delInterval, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jL_beginInterval))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jP_histogramIntervalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jP_histogramIntervalsLayout.createSequentialGroup()
                                .addComponent(jTF_beginHistogramInverval, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jL_endInterval)
                                .addGap(18, 18, 18)
                                .addComponent(jTF_endHistogramInverval, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane_intervalsList, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jP_histogramIntervalsLayout.setVerticalGroup(
            jP_histogramIntervalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_histogramIntervalsLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jL_intervalsHistogram)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jP_histogramIntervalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jL_beginInterval)
                    .addComponent(jL_endInterval)
                    .addComponent(jTF_beginHistogramInverval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTF_endHistogramInverval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jP_histogramIntervalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jP_histogramIntervalsLayout.createSequentialGroup()
                        .addComponent(jB_addInterval)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jB_delInterval)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jB_clearAllIntervals))
                    .addComponent(jScrollPane_intervalsList, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );

        jP_histogramLogscale.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jCB_histogramEnableLogScale.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_histogramEnableLogScale.setText("Enable logaritmic scale using custom base");
        jCB_histogramEnableLogScale.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_histogramEnableLogScaleItemStateChanged(evt);
            }
        });

        jL_histogramLogScale.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_histogramLogScale.setText("Log scale on y axis");

        jCoB_histogramYlogBase.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_histogramYlogBase.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "e", "2", "10" }));
        jCoB_histogramYlogBase.setEnabled(false);

        javax.swing.GroupLayout jP_histogramLogscaleLayout = new javax.swing.GroupLayout(jP_histogramLogscale);
        jP_histogramLogscale.setLayout(jP_histogramLogscaleLayout);
        jP_histogramLogscaleLayout.setHorizontalGroup(
            jP_histogramLogscaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_histogramLogscaleLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jP_histogramLogscaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jP_histogramLogscaleLayout.createSequentialGroup()
                        .addComponent(jCB_histogramEnableLogScale)
                        .addGap(18, 18, 18)
                        .addComponent(jCoB_histogramYlogBase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jL_histogramLogScale))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jP_histogramLogscaleLayout.setVerticalGroup(
            jP_histogramLogscaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jP_histogramLogscaleLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jL_histogramLogScale, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(jP_histogramLogscaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCB_histogramEnableLogScale, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCoB_histogramYlogBase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jCB_histogramSteps_Y_Tics.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_histogramSteps_Y_Tics.setText("Custom steps between y tics");
        jCB_histogramSteps_Y_Tics.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_histogramSteps_Y_TicsItemStateChanged(evt);
            }
        });

        jSpinner_histogramCustomYtics.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jSpinner_histogramCustomYtics.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), null, Float.valueOf(0.1f)));
        jSpinner_histogramCustomYtics.setEnabled(false);

        javax.swing.GroupLayout jP_HistogramLayout = new javax.swing.GroupLayout(jP_Histogram);
        jP_Histogram.setLayout(jP_HistogramLayout);
        jP_HistogramLayout.setHorizontalGroup(
            jP_HistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_HistogramLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jP_HistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jP_histogramIntervals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jP_HistogramLayout.createSequentialGroup()
                        .addComponent(jCB_histogramSteps_Y_Tics)
                        .addGap(30, 30, 30)
                        .addComponent(jSpinner_histogramCustomYtics, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jP_histogramLogscale, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jB_GenerateHistogramScript, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jP_HistogramLayout.setVerticalGroup(
            jP_HistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jP_HistogramLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jP_histogramIntervals, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jP_histogramLogscale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jP_HistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner_histogramCustomYtics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCB_histogramSteps_Y_Tics))
                .addGap(28, 28, 28)
                .addComponent(jB_GenerateHistogramScript)
                .addContainerGap())
        );

        jTP_plotsTypes.addTab("Histogram", jP_Histogram);

        jSP_plotsTypes.setViewportView(jTP_plotsTypes);

        jSP_smoothAndStyles.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Smooth, styles and other options"));
        jSP_smoothAndStyles.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jSP_smoothAndStyles.setPreferredSize(new java.awt.Dimension(462, 209));

        JTP_smoothAndStyles.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                JTP_smoothAndStylesStateChanged(evt);
            }
        });

        jP_smoothAndStyleforLines.setPreferredSize(new java.awt.Dimension(440, 209));

        jCoB_smoothTypes.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_smoothTypes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<nothing>", "Cubic splines", "Belzier curve's" }));
        jCoB_smoothTypes.setToolTipText("<html>\n<b>Bezier</b> - Calculates and plots a Bézier approximation of order n (where n is the number of data points).<br>\nThe curve is guaranteed to pass through the first and last data point exactly,<br>\nbut in general won’t pass through any of the internal points. The resulting curve usually appears particularly smooth.<br>\n<br>\n<b>Cubicsplines</b> - Calculates natural cubic splines to the result and plots these.<br>\nThe resulting curve passes exactly through all data points.<br>\nAs opposed to the Bézier curve (which is defined for the entire data set), splines are defined locally<br>\n (between any two consecutive data points) and joined smoothly at the points.<br>\nThey’re therefore most suited for data that’s already quite smooth.\n</html>");

        jCoB_simpleStylesType.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_simpleStylesType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "points", "lines", "linespoints", "dots", "impulses", "boxes" }));
        jCoB_simpleStylesType.setToolTipText("<html>\n<b>Points</b> The points style displays a small symbol at each point.<br>\n<b>Lines</b> The lines style connects adjacent points with straight line segments.<br>\n<b>Lines and Points</b> The linespoints style connects adjacent points with straight line segments and then goes back to draw a small symbol at each point.<br>\n<b>Dots</b> The dots style plots a tiny dot at each point; this is useful for scatter plots with <i>many</i> points.<br>\n<b>Impulses</b> The impulses style displays a vertical line from y=0 to the y value of each point.Note that the y values may be negative.<br>\n<b>Boxes</b> It draws a box centered about the given x coordinate that extends from the x axis (not from the graph border) to the given y coordinate.<br>\n</html>");
        jCoB_simpleStylesType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCoB_simpleStylesTypeItemStateChanged(evt);
            }
        });

        jCB_rotateXlabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_rotateXlabel.setText("Rotate x-tics label (1º ~ 90º)");
        jCB_rotateXlabel.setToolTipText("<html>\nWhen the xtics is too near, is recommended rotate by some angle to not pollute the graph<br>\n</html>");
        jCB_rotateXlabel.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_rotateXlabelItemStateChanged(evt);
            }
        });

        jL_smoothToUse.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_smoothToUse.setText("Smooth to use");

        jL_plotStyle.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_plotStyle.setText("Plot style");

        jCoB_decimalSign.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_decimalSign.setModel(new javax.swing.DefaultComboBoxModel(new String[] { ".", ",", "<other>" }));
        jCoB_decimalSign.setToolTipText("<html>\nTell to GNUpot which char is used as decimal sign to plot image.<br>\n<b><h4>This not affect the decimal sign of data file!</h4></b>\n</html>");
        jCoB_decimalSign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCoB_decimalSignActionPerformed(evt);
            }
        });

        jL_decimalSignToShow.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_decimalSignToShow.setText("Decimal sign to be show on plot");
        jL_decimalSignToShow.setToolTipText("<html>\nTell to GNUpot which char is used as decimal sign to plot image.<br>\n<b><h4>This not affect the decimal sign of data file!</h4></b>\n</html>");

        jCoB_timeUnit.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_timeUnit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Seconds", "Minutes", "Hours" }));
        jCoB_timeUnit.setToolTipText("<html>\nSelect a most suitable unit to represent your data  which belong to the x-axis<br>\n</html>");
        jCoB_timeUnit.setEnabled(false);

        jSpinner_rotateXLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jSpinner_rotateXLabel.setModel(new javax.swing.SpinnerNumberModel(45, 1, 90, 1));
        jSpinner_rotateXLabel.setToolTipText("<html>\nWhen the xtics is too near, is recommended rotate by some angle to not pollute the graph<br>\n</html>");
        jSpinner_rotateXLabel.setEnabled(false);

        jP_widths.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true), "Widths"));

        jL_lineWidth.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_lineWidth.setText("Line");
        jL_lineWidth.setEnabled(false);

        jL_boxWidth.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_boxWidth.setText("Box");
        jL_boxWidth.setEnabled(false);

        jL_pointWidth.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_pointWidth.setText("Point");

        jSpinner_pointWidth.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jSpinner_pointWidth.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.2f), null, Float.valueOf(0.2f)));

        jSpinner_boxWidth.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jSpinner_boxWidth.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), null, Float.valueOf(0.1f)));
        jSpinner_boxWidth.setEnabled(false);

        jSpinner_lineWidth.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jSpinner_lineWidth.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.2f), null, Float.valueOf(0.2f)));
        jSpinner_lineWidth.setEnabled(false);

        javax.swing.GroupLayout jP_widthsLayout = new javax.swing.GroupLayout(jP_widths);
        jP_widths.setLayout(jP_widthsLayout);
        jP_widthsLayout.setHorizontalGroup(
            jP_widthsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_widthsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jL_lineWidth)
                .addGap(1, 1, 1)
                .addComponent(jSpinner_lineWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86)
                .addComponent(jL_boxWidth)
                .addGap(5, 5, 5)
                .addComponent(jSpinner_boxWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(jL_pointWidth)
                .addGap(5, 5, 5)
                .addComponent(jSpinner_pointWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );
        jP_widthsLayout.setVerticalGroup(
            jP_widthsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jP_widthsLayout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jP_widthsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jL_lineWidth)
                    .addComponent(jL_boxWidth)
                    .addComponent(jL_pointWidth)
                    .addComponent(jSpinner_pointWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinner_boxWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinner_lineWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jCB_timeOnXaxis.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_timeOnXaxis.setText("Time unit on x-axis");
        jCB_timeOnXaxis.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_timeOnXaxisItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jP_smoothAndStyleforLinesLayout = new javax.swing.GroupLayout(jP_smoothAndStyleforLines);
        jP_smoothAndStyleforLines.setLayout(jP_smoothAndStyleforLinesLayout);
        jP_smoothAndStyleforLinesLayout.setHorizontalGroup(
            jP_smoothAndStyleforLinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_smoothAndStyleforLinesLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(jP_smoothAndStyleforLinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jP_smoothAndStyleforLinesLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jP_smoothAndStyleforLinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jCB_rotateXlabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jCB_timeOnXaxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jL_decimalSignToShow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jP_smoothAndStyleforLinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSpinner_rotateXLabel)
                            .addComponent(jCoB_timeUnit, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jCoB_decimalSign, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jP_smoothAndStyleforLinesLayout.createSequentialGroup()
                        .addComponent(jL_plotStyle)
                        .addGap(18, 18, 18)
                        .addComponent(jCoB_simpleStylesType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 75, Short.MAX_VALUE)
                        .addComponent(jL_smoothToUse)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCoB_smoothTypes, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addGroup(jP_smoothAndStyleforLinesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jP_widths, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jP_smoothAndStyleforLinesLayout.setVerticalGroup(
            jP_smoothAndStyleforLinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_smoothAndStyleforLinesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jP_smoothAndStyleforLinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCoB_smoothTypes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCoB_simpleStylesType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jL_smoothToUse)
                    .addComponent(jL_plotStyle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jP_smoothAndStyleforLinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jCB_rotateXlabel)
                    .addComponent(jSpinner_rotateXLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jP_smoothAndStyleforLinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jL_decimalSignToShow)
                    .addComponent(jCoB_decimalSign, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jP_smoothAndStyleforLinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCoB_timeUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCB_timeOnXaxis))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jP_widths, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        JTP_smoothAndStyles.addTab("Lines", jP_smoothAndStyleforLines);

        jP_smoothAndStyleforHistogram.setPreferredSize(new java.awt.Dimension(440, 209));

        bg_histogramStyle.add(jRB_clusteredHistogram);
        jRB_clusteredHistogram.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jRB_clusteredHistogram.setSelected(true);
        jRB_clusteredHistogram.setText("Clustered");

        bg_histogramStyle.add(jRB_stackedHistogram);
        jRB_stackedHistogram.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jRB_stackedHistogram.setText("Stacked");
        jRB_stackedHistogram.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRB_stackedHistogramItemStateChanged(evt);
            }
        });

        jL_histogramStyle.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_histogramStyle.setText("Histogram Style");

        jSpinner_histogramGapSize.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jSpinner_histogramGapSize.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(2.0d), Double.valueOf(0.2d), null, Double.valueOf(0.2d)));

        jL_histogramGapSize.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_histogramGapSize.setText("Gap size");

        jL_fillUsing.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_fillUsing.setText("Fill boxes using");

        bg_histogramFill.add(jRB_histogramFillPatterns);
        jRB_histogramFillPatterns.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jRB_histogramFillPatterns.setSelected(true);
        jRB_histogramFillPatterns.setText("Patterns");

        bg_histogramFill.add(jRB_histogramFillSolid);
        jRB_histogramFillSolid.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jRB_histogramFillSolid.setText("Solid colors");

        jL_histogramBoxWidth.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_histogramBoxWidth.setText("Box width");

        jSpinner_histogramBoxWidth.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jSpinner_histogramBoxWidth.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.1d, 1.0d, 0.1d));

        jCB_histogramRotateXlabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_histogramRotateXlabel.setText("Rotate x-tics label (1º ~ 90º)");
        jCB_histogramRotateXlabel.setToolTipText("<html>\nWhen the xtics is too near, is recommended rotate by some angle to not pollute the graph<br>\n</html>");
        jCB_histogramRotateXlabel.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_histogramRotateXlabelItemStateChanged(evt);
            }
        });

        jSpinner_histogramRotateXLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jSpinner_histogramRotateXLabel.setModel(new javax.swing.SpinnerNumberModel(45, 1, 90, 1));
        jSpinner_histogramRotateXLabel.setToolTipText("<html>\nWhen the xtics is too near, is recommended rotate by some angle to not pollute the graph<br>\n</html>");
        jSpinner_histogramRotateXLabel.setEnabled(false);

        jCoB_histogramDecimalSign.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCoB_histogramDecimalSign.setModel(new javax.swing.DefaultComboBoxModel(new String[] { ".", ",", "<other>" }));
        jCoB_histogramDecimalSign.setToolTipText("<html>\nTell to GNUpot which char is used as decimal sign to plot image.<br>\n<b><h4>This not affect the decimal sign of data file!</h4></b>\n</html>");
        jCoB_histogramDecimalSign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCoB_histogramDecimalSignActionPerformed(evt);
            }
        });

        jL_histogramDecimalSignToShow.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_histogramDecimalSignToShow.setText("Decimal sign to be show on plot");
        jL_histogramDecimalSignToShow.setToolTipText("<html>\nTell to GNUpot which char is used as decimal sign to plot image.<br>\n<b><h4>This not affect the decimal sign of data file!</h4></b>\n</html>");

        jCB_boxesWithoutBorder.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_boxesWithoutBorder.setText("Boxes without borders");

        javax.swing.GroupLayout jP_smoothAndStyleforHistogramLayout = new javax.swing.GroupLayout(jP_smoothAndStyleforHistogram);
        jP_smoothAndStyleforHistogram.setLayout(jP_smoothAndStyleforHistogramLayout);
        jP_smoothAndStyleforHistogramLayout.setHorizontalGroup(
            jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_smoothAndStyleforHistogramLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jP_smoothAndStyleforHistogramLayout.createSequentialGroup()
                        .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jP_smoothAndStyleforHistogramLayout.createSequentialGroup()
                                .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jL_histogramStyle)
                                    .addComponent(jL_fillUsing))
                                .addGap(14, 14, 14)
                                .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jRB_histogramFillPatterns)
                                    .addComponent(jRB_clusteredHistogram)))
                            .addComponent(jCB_boxesWithoutBorder))
                        .addGap(12, 12, 12)
                        .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRB_histogramFillSolid)
                            .addComponent(jRB_stackedHistogram))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jL_histogramGapSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jL_histogramBoxWidth, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSpinner_histogramBoxWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpinner_histogramGapSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jP_smoothAndStyleforHistogramLayout.createSequentialGroup()
                        .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCB_histogramRotateXlabel)
                            .addComponent(jL_histogramDecimalSignToShow))
                        .addGap(18, 18, 18)
                        .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSpinner_histogramRotateXLabel)
                            .addComponent(jCoB_histogramDecimalSign, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jP_smoothAndStyleforHistogramLayout.setVerticalGroup(
            jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_smoothAndStyleforHistogramLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRB_clusteredHistogram)
                    .addComponent(jRB_stackedHistogram)
                    .addComponent(jL_histogramStyle)
                    .addComponent(jSpinner_histogramGapSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jL_histogramGapSize))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRB_histogramFillPatterns)
                    .addComponent(jRB_histogramFillSolid)
                    .addComponent(jL_histogramBoxWidth)
                    .addComponent(jSpinner_histogramBoxWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jL_fillUsing))
                .addGap(8, 8, 8)
                .addComponent(jCB_boxesWithoutBorder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCB_histogramRotateXlabel)
                    .addComponent(jSpinner_histogramRotateXLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jP_smoothAndStyleforHistogramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCoB_histogramDecimalSign, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jL_histogramDecimalSignToShow))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        JTP_smoothAndStyles.addTab("Histogram", jP_smoothAndStyleforHistogram);

        jP_legends.setPreferredSize(new java.awt.Dimension(440, 209));

        jCB_legendsOutsideLegends.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_legendsOutsideLegends.setText("Outside legends");
        jCB_legendsOutsideLegends.setToolTipText("<html>\nPut all legends outside from plot area.<br>\nOtherwise, the legends are put inside area.<br>\n</html>");

        jCB_legendsFillWithBorder.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_legendsFillWithBorder.setText("Filled with border");
        jCB_legendsFillWithBorder.setToolTipText("<html>\nFill the legends texts with borders.\n</html>");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        bg_legendPosition.add(jTB_legendLeftBottom);
        jTB_legendLeftBottom.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jTB_legendLeftBottom.setText(" ");

        bg_legendPosition.add(jTB_legendRightBottom);
        jTB_legendRightBottom.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jTB_legendRightBottom.setText(" ");

        bg_legendPosition.add(jTB_legendRightTop);
        jTB_legendRightTop.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jTB_legendRightTop.setSelected(true);
        jTB_legendRightTop.setText(" ");

        bg_legendPosition.add(jTB_legendCenterBottom);
        jTB_legendCenterBottom.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jTB_legendCenterBottom.setText(" ");

        bg_legendPosition.add(jTB_legendCenterTop);
        jTB_legendCenterTop.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jTB_legendCenterTop.setText(" ");

        bg_legendPosition.add(jTB_legendLeftTop);
        jTB_legendLeftTop.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jTB_legendLeftTop.setText(" ");

        bg_legendPosition.add(jTB_legendLeftCenter);
        jTB_legendLeftCenter.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jTB_legendLeftCenter.setText(" ");

        bg_legendPosition.add(jTB_legendCenterCenter);
        jTB_legendCenterCenter.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jTB_legendCenterCenter.setText(" ");

        bg_legendPosition.add(jTB_legendRightCenter);
        jTB_legendRightCenter.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jTB_legendRightCenter.setText(" ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTB_legendLeftCenter)
                        .addGap(12, 12, 12)
                        .addComponent(jTB_legendCenterCenter)
                        .addGap(12, 12, 12)
                        .addComponent(jTB_legendRightCenter))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTB_legendLeftTop)
                        .addGap(12, 12, 12)
                        .addComponent(jTB_legendCenterTop)
                        .addGap(12, 12, 12)
                        .addComponent(jTB_legendRightTop))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTB_legendLeftBottom)
                        .addGap(12, 12, 12)
                        .addComponent(jTB_legendCenterBottom)
                        .addGap(12, 12, 12)
                        .addComponent(jTB_legendRightBottom)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTB_legendLeftTop)
                    .addComponent(jTB_legendCenterTop)
                    .addComponent(jTB_legendRightTop))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTB_legendLeftCenter)
                    .addComponent(jTB_legendCenterCenter)
                    .addComponent(jTB_legendRightCenter))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTB_legendLeftBottom)
                    .addComponent(jTB_legendCenterBottom)
                    .addComponent(jTB_legendRightBottom))
                .addContainerGap())
        );

        jL_legendsLegendPosition.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jL_legendsLegendPosition.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jL_legendsLegendPosition.setText("Legend position");

        jCB_legendsHorizontalAlignment.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_legendsHorizontalAlignment.setText("Horizontal alignment");
        jCB_legendsHorizontalAlignment.setToolTipText("<html>\nBy default, vertical alignment are setted, this means that the legends<br>\n and the square area of plot are placed like columns.<br>\n If horizontal alignment are setted, these are placed like lines. <br>\nCase left-center, right-center or center-center position are selected, differences are not perceived.<br>\n</html>");

        jCB_legendsOpaque.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jCB_legendsOpaque.setText("Opaque");
        jCB_legendsOpaque.setToolTipText("<html>\nCauses the legend area box to be generated after all the plots, forcing him to stay on top.\n</html>");

        javax.swing.GroupLayout jP_legendsLayout = new javax.swing.GroupLayout(jP_legends);
        jP_legends.setLayout(jP_legendsLayout);
        jP_legendsLayout.setHorizontalGroup(
            jP_legendsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_legendsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jP_legendsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jL_legendsLegendPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jP_legendsLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jP_legendsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCB_legendsOpaque)
                            .addComponent(jCB_legendsOutsideLegends)
                            .addComponent(jCB_legendsFillWithBorder)
                            .addComponent(jCB_legendsHorizontalAlignment))))
                .addContainerGap(161, Short.MAX_VALUE))
        );
        jP_legendsLayout.setVerticalGroup(
            jP_legendsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_legendsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jL_legendsLegendPosition)
                .addGap(10, 10, 10)
                .addGroup(jP_legendsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jP_legendsLayout.createSequentialGroup()
                        .addComponent(jCB_legendsOutsideLegends)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCB_legendsFillWithBorder)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCB_legendsHorizontalAlignment)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCB_legendsOpaque)))
                .addContainerGap(46, Short.MAX_VALUE))
        );

        JTP_smoothAndStyles.addTab("Legends", jP_legends);

        jSP_smoothAndStyles.setViewportView(JTP_smoothAndStyles);

        jP_graphicView.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Graphic view (require GNUPlot installed)"));

        jB_makePreview.setText("Make a view");
        jB_makePreview.setToolTipText("<html>\nMake a view based on configures choosen,<br>\nexcept on output terminals settings.<br>\n<i>It's just a view</i>\n</html>");
        jB_makePreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_makePreviewActionPerformed(evt);
            }
        });
        jB_makePreview.setBounds(20, 20, 150, 29);
        jLP_graphicView.add(jB_makePreview, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jL_preview.setBackground(new java.awt.Color(254, 254, 254));
        jL_preview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jL_preview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/bar_chart.png"))); // NOI18N
        jL_preview.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jL_preview.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jL_preview.setMaximumSize(new java.awt.Dimension(2000, 2000));
        jL_preview.setMinimumSize(new java.awt.Dimension(300, 300));
        jL_preview.setOpaque(true);
        jL_preview.setPreferredSize(new java.awt.Dimension(580, 360));
        jL_preview.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jL_previewMouseClicked(evt);
            }
        });
        jL_preview.setBounds(0, -1, 520, 350);
        jLP_graphicView.add(jL_preview, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jP_graphicViewLayout = new javax.swing.GroupLayout(jP_graphicView);
        jP_graphicView.setLayout(jP_graphicViewLayout);
        jP_graphicViewLayout.setHorizontalGroup(
            jP_graphicViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_graphicViewLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLP_graphicView)
                .addContainerGap())
        );
        jP_graphicViewLayout.setVerticalGroup(
            jP_graphicViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_graphicViewLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLP_graphicView)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSP_plotsTypes, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                    .addComponent(jSP_generalOptions, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSP_smoothAndStyles, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                    .addComponent(jP_graphicView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSP_generalOptions)
                    .addComponent(jSP_smoothAndStyles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jP_graphicView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jSP_plotsTypes, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE))
                .addGap(5, 5, 5))
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Methods that will configure environment">
    /**
     *
     * Configures all set of options that are common for line and histogram
     * graphics.
     */
    private void configureGeneralEnvironmentSettings() {
        environmentSettings = new StringBuilder();

        environmentSettings.append("#This part configure the GNUplot environment to be adjusted conform your configuration.\r\n"
                + "#Feel free to alter it.\r\n\r\n");
        environmentSettings.append("#As there may be incompatibilities with older versions of the script GNUPlot,\r\n#which might not accept some of the explicit settings, just comment out the line by placing a # at the beginning.\r\n\r\n");
        environmentSettings.append("reset;\r\n");
        environmentSettings.append("e = 2.7182818284590452354;\r\n");
        environmentSettings.append("set datafile missing \"NaN\";\r\n");
        //Output
        String terminalOutput;
        String fileOutput;
        output = (TerminalTypes) jCoB_output.getModel().getSelectedItem();

        terminalOutput = output.getGnuplotName() + ( output.isFileoutput() ? " " : "" ) + configurator.getTerminalConfiguration(output).toString();
        environmentSettings.append("set terminal ").append(terminalOutput).append(";\r\n");

        if ( output.isFileoutput() ) {
            fileOutput = "\"" + jTF_TitleInput.getText() + output.getExtension() + "\"";
            environmentSettings.append("set output ").append(fileOutput).append(";\r\n");
        }
        else {
            environmentSettings.append("set output;\r\n");
        }

        //Grid
        if ( jCB_enableGrid.isSelected() ) {
            environmentSettings.append("set grid");
            if ( new Double(jSpinner_gridLineWidth.getValue().toString()) != 1.0D ) {
                environmentSettings.append(" linewidth ").append(jSpinner_gridLineWidth.getValue());
            }
            environmentSettings.append(";\r\n");
            if ( jCB_gridOnX.isSelected() ) {
                environmentSettings.append("set grid xtics;\r\n");
            }
            else {
                environmentSettings.append("set grid noxtics;\r\n");
            }

            if ( jCB_gridOnY.isSelected() ) {
                environmentSettings.append("set grid ytics;\r\n");
            }
            else {
                environmentSettings.append("set grid noytics;\r\n");
            }
        }

        //Legends
        if ( jCB_legendsFillWithBorder.isSelected() || jCB_legendsHorizontalAlignment.isSelected() || jCB_legendsOpaque.isSelected() || jCB_legendsOutsideLegends.isSelected() || !jTB_legendRightTop.isSelected() ) {
            environmentSettings.append("set key");
        }
        if ( jCB_legendsOutsideLegends.isSelected() ) {
            environmentSettings.append(" outside ");
        }
        if ( jTB_legendRightTop.isSelected() ) {
        }
        else if ( jTB_legendRightCenter.isSelected() ) {
            environmentSettings.append(" right center ");
        }
        else if ( jTB_legendRightBottom.isSelected() ) {
            environmentSettings.append(" right bottom ");
        }
        else if ( jTB_legendCenterTop.isSelected() ) {
            environmentSettings.append(" center top ");
        }
        else if ( jTB_legendCenterCenter.isSelected() ) {
            environmentSettings.append(" center center ");
        }
        else if ( jTB_legendCenterBottom.isSelected() ) {
            environmentSettings.append(" center bottom ");
        }
        else if ( jTB_legendLeftTop.isSelected() ) {
            environmentSettings.append(" left top ");
        }
        else if ( jTB_legendLeftCenter.isSelected() ) {
            environmentSettings.append(" left center ");
        }
        else if ( jTB_legendLeftBottom.isSelected() ) {
            environmentSettings.append(" left bottom ");
        }
        if ( jCB_legendsHorizontalAlignment.isSelected() ) {
            environmentSettings.append(" horizontal ");
        }
        if ( jCB_legendsOpaque.isSelected() ) {
            environmentSettings.append(" opaque ");
        }
        if ( jCB_legendsFillWithBorder.isSelected() ) {
            environmentSettings.append(" box ");
        }
        if ( jCB_legendsFillWithBorder.isSelected() || jCB_legendsHorizontalAlignment.isSelected() || jCB_legendsOpaque.isSelected() || jCB_legendsOutsideLegends.isSelected() || !jTB_legendRightTop.isSelected() ) {
            environmentSettings.append(";\r\n");
        }
        //environmentSettings.append("set key outside right top vertical Left reverse noenhanced box linetype -1 linewidth 1.000;\r\n");

    }

    /**
     *
     * This method generates a script containing the configuration for gnu-plot
     * environment, based on the the choices of user on ExportGraphics's window
     * screen.
     */
    private void configureLineEnvironment() {
        //environmentSettings = new StringBuilder();
        configureGeneralEnvironmentSettings();

        //<editor-fold defaultstate="collapsed" desc="environment configs">
        if ( jCoB_timeUnit.isEnabled() ) {
            environmentSettings.append("set xdata time;\r\nset timefmt \"%").append(( (String) jCoB_timeUnit.getItemAt(jCoB_timeUnit.getSelectedIndex()) ).charAt(0)).append("\";\r\n");
        }
        //Decimal sign
        if ( jCoB_decimalSign.getSelectedIndex() != 0 ) {
            environmentSettings.append("set decimalsign \"").append((String) jCoB_decimalSign.getItemAt(jCoB_decimalSign.getSelectedIndex())).append("\";\r\n");
        }
        //Title and each respective axis-label
        //environmentSettings.append("set key autotitle columnhead;\r\n");
        environmentSettings.append("set title  \"").append(jTF_TitleInput.getText()).append(( jCoB_smoothTypes.getSelectedIndex() == 0 ) ? "\";\r\n" : " (using " + jCoB_smoothTypes.getSelectedItem() + " to smooth data)\";\r\n");
        environmentSettings.append("set xlabel \"").append(jTF_Xaxis.getText()).append(" (").append(jCoB_timeUnit.getSelectedItem().toString()).append(")").append("\";\r\n");
        environmentSettings.append("set ylabel \"").append(jTF_Yaxis.getText()).append("\";\r\n");
        if ( !jCB_autoRangeX.isSelected() ) {
            environmentSettings.append("set xrange [").append(jCoB_minimumX.getSelectedItem().toString().replaceAll(",", ".")).append(":").append(jCoB_maximumX.getSelectedItem().toString().replaceAll(",", ".")).append("];\r\n");
        }
        //Define manual y range
        if ( !jCB_autoRangeY.isSelected() ) {
            environmentSettings.append("set yrange [").append(jCoB_minimumY.getEditor().getItem().toString().replaceAll(",", ".")).append(":").append(jCoB_maximumY.getEditor().getItem().toString().replaceAll(",", ".")).append("];\r\n ");
        }
//        else {
//            environmentSettings.append("set autoscale keepfix;\r\n");
//        }
        //Logarithmic escale
        if ( jCB_EnableLogScale.isSelected() ) {
            if ( jCB_XlogBase.isSelected() ) {
                environmentSettings.append("set log x ").append(jCoB_XlogBase.getSelectedItem());
                environmentSettings.append(";\r\n");
            }
            if ( jCB_YlogBase.isSelected() ) {
                environmentSettings.append("set log y ").append(jCoB_YlogBase.getSelectedItem());
                environmentSettings.append(";\r\n");
            }
        }
        //X tics and Y tics
        if ( jCB_Steps_X_Tics.isSelected() ) {
            environmentSettings.append("set xtics ").append(jSpinner_customXtics.getValue()).append(";\r\n");
        }
        //Rotate x label
        if ( jCB_rotateXlabel.isSelected() ) {
            environmentSettings.append("set xtics nomirror rotate by -").append(jSpinner_rotateXLabel.getValue()).append(";\r\n");
        }
        if ( jCB_Steps_Y_Tics.isSelected() ) {
            environmentSettings.append("set ytics ").append(jSpinner_customYtics.getValue()).append(";\r\n");
        }
        //</editor-fold>

    }

    /**
     *
     * This method generates a script containing the configuration for gnu-plot
     * environment, based on the the choices of user on ExportGraphics's window
     * screen.
     */
    private void configureHistogramEnvironment() {
        //environmentSettings = new StringBuilder();
        configureGeneralEnvironmentSettings();

        //Styles
        environmentSettings.append("set style data histogram;\r\n");
        environmentSettings.append("set style histogram ").append(bg_histogramStyle.isSelected(jRB_clusteredHistogram.getModel()) ? "clustered" : "rowstacked").append(" gap ").append(jSpinner_histogramGapSize.getValue().toString()).append(";\r\n");
        environmentSettings.append("set style fill ").append(bg_histogramFill.isSelected(jRB_histogramFillPatterns.getModel()) ? "pattern" : "solid").append(jCB_boxesWithoutBorder.isSelected() ? " noborder" : " 1 border lt -1").append(";\r\n");
        //Titles
        environmentSettings.append("set title  \"").append(jTF_TitleInput.getText()).append("\";\r\n");
        environmentSettings.append("set xlabel \"").append(jTF_Xaxis.getText()).append("\";\r\n");
        environmentSettings.append("set ylabel \"").append(jTF_Yaxis.getText()).append("\";\r\n");

        //Logarithmic escale
        if ( jCB_histogramEnableLogScale.isSelected() ) {
            environmentSettings.append("set log y ").append(jCoB_histogramYlogBase.getSelectedItem());
            environmentSettings.append(";\r\n");
        }
        //Tics
        //environmentSettings.append("set xtics border out scale 1,0.5 offset character 0, 0, 0 autojustify;\r\n");
        //environmentSettings.append("set xtics  norangelimit;\r\n");
        //environmentSettings.append("set xtics  ();\r\n");
        //environmentSettings.append("set ytics border in scale 0,0 mirror norotate  offset character 0, 0, 100 autojustify rangelimited;\r\n");
        //if (jCB_histogramSteps_Y_Tics.isSelected()) {
        //    environmentSettings.append("set ytics ").append(jSpinner_histogramCustomYtics.getValue().toString()).append(";\r\n");
        //}
        //Rotate x label
        if ( jCB_histogramRotateXlabel.isSelected() ) {
            environmentSettings.append("set xtics nomirror rotate by -").append(jSpinner_histogramRotateXLabel.getValue()).append(";\r\n");
        }
        //Others
        if ( Double.parseDouble(jSpinner_boxWidth.getValue().toString()) != 1.0D ) {
            environmentSettings.append("set boxwidth ").append(jSpinner_histogramBoxWidth.getValue().toString()).append(";\r\n");
        }
        //environmentSettings.append("set autoscale x;\r\n");
        //environmentSettings.append("set yrange [:100<*];\r\n");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Methods that will make a plot command">
    /**
     * Constructs a line with necessary commands to plot a single histogram
     * graphic.
     */
    private void makeHistogramPlotCommand() {
        int cont = 1;
        histogramPlotCommand = new StringBuilder("plot \"<file>\"");
        for ( HistogramInterval hi : histogramIntervals ) {
            histogramPlotCommand.append(" using ").append(++cont).append(" title \"").append(hi.toString()).append("\",  \"\"");
        }
//        histogramPlotCommand.replace(histogramPlotCommand.length() - 5, histogramPlotCommand.length(), "");
//        histogramPlotCommand.replace(histogramPlotCommand.lastIndexOf("using")+7, histogramPlotCommand.lastIndexOf("using")+7, ":xtic(1)");
        histogramPlotCommand.append(" using ").append(++cont).append(":xtic(1) title \"Remainder\"");
//        histogramPlotCommand.append(" \"<file>\" ");
//        histogramPlotCommand.append("using i:xtic(1) ti col");
    }

    /**
     * Constructs a line with all necessary commands to plot a single line
     * graphic.
     */
    private void makeSingleLinePlotCommand() {

        linePlotCommand = new StringBuilder();

        linePlotCommand.append("plot \"\" ");
        /* The name of file will be added later */
        linePlotCommand.append("every 1:1:").append(jCoB_minimumX.getSelectedIndex()).append(":0:").append(jCoB_minimumX.getSelectedIndex() + jCoB_maximumX.getSelectedIndex() + 1).append(":0 ");
        linePlotCommand.append("using 1:2 ");
        //Define the title of data plot
        linePlotCommand.append("title ").append("\"").append(metricToPlot).append(" of stream ").append(actualStream).append("\" ");
        //Type of line
        linePlotCommand.append("with ").append((String) jCoB_simpleStylesType.getSelectedItem()).append(" ");
        if ( jSpinner_boxWidth.isEnabled() && new Double(jSpinner_boxWidth.getValue().toString()) != 1.0D ) {
            environmentSettings.append("set boxwidth ").append(jSpinner_boxWidth.getValue()).append(";\r\n");
        }
        //Line width
        if ( jSpinner_lineWidth.isEnabled() && new Double(jSpinner_lineWidth.getValue().toString()) != 1.0D ) {
            linePlotCommand.append("linewidth ").append(jSpinner_lineWidth.getValue()).append(" ");
        }
        //Point width
        if ( jSpinner_pointWidth.isEnabled() && new Double(jSpinner_pointWidth.getValue().toString()) != 1.0D ) {
            linePlotCommand.append("pointsize ").append(jSpinner_pointWidth.getValue()).append(" ");
        }
        //Type of smooth
        if ( jCoB_smoothTypes.getSelectedIndex() != 0 && singleStreamSize >= 3 ) {
            linePlotCommand.append("smooth ");
            switch ( jCoB_smoothTypes.getSelectedIndex() ) {
                case 1:
                    linePlotCommand.append("csplines ");
                    break;
                case 2:
                    linePlotCommand.append("bezier ");
                    break;
            }
        }


    }

    /**
     * Builds a line with the necessary commands to plot all streams variations
     * for comparation between they. It's similar to makeSingleLinePlotCommand,
     * except because of loop inside the function, necessary to generate a
     * unique line to each diferrent stream; it's necessary for GNU-plot auto
     * set a appropriate style for each plot (color and point's style) for
     * diferenciation between them.
     */
    private void makeMultiLinePlotCommand() {
        linePlotCommand = new StringBuilder();


        linePlotCommand.append("plot \"\" ");
        for ( int i = 0 ; i < Analyzer.getInstance().getTamArrayStreams() ; i++ ) {
            /* The name of file will be added later */
            linePlotCommand.append("index ").append(i).append(" ");
            linePlotCommand.append("every 1:1:").append(jCoB_minimumX.getSelectedIndex()).append(":0:").append(jCoB_minimumX.getSelectedIndex() + jCoB_maximumX.getSelectedIndex() + 1).append(":0 ");
            linePlotCommand.append("using 1:2 ");
            //Define the title of data plot
            linePlotCommand.append("title ").append("\"").append(metricToPlot).append(" of stream ").append(i).append("\" ");
            //Type of line
            linePlotCommand.append("with ").append((String) jCoB_simpleStylesType.getSelectedItem()).append(" ");
            if ( jSpinner_boxWidth.isEnabled() && new Double(jSpinner_boxWidth.getValue().toString()) != 1.0D ) {
                environmentSettings.append("set boxwidth ").append(jSpinner_boxWidth.getValue()).append(";\r\n");
            }
            //Line width
            if ( jSpinner_lineWidth.isEnabled() && new Double(jSpinner_lineWidth.getValue().toString()) != 1.0D ) {
                linePlotCommand.append("linewidth ").append(jSpinner_lineWidth.getValue()).append(" ");
            }
            //Point width
            if ( jSpinner_pointWidth.isEnabled() && new Double(jSpinner_pointWidth.getValue().toString()) != 1.0D ) {
                linePlotCommand.append("pointsize ").append(jSpinner_pointWidth.getValue()).append(" ");
            }
            //Type of smooth
            if ( jCoB_smoothTypes.getSelectedIndex() != 0 && Analyzer.getInstance().getStream(i).delay.size() >= 3 ) {
                linePlotCommand.append("smooth ");
                switch ( jCoB_smoothTypes.getSelectedIndex() ) {
                    case 1:
                        linePlotCommand.append("csplines ");
                        break;
                    case 2:
                        linePlotCommand.append("bezier ");
                        break;
                }
            }
            //Add a ',' to write a next plot command.
            linePlotCommand.append(",\\\r\n\"\" ");
        } //For end
        for ( int i = 0 ; i < 8 ; i++ ) {
            linePlotCommand.deleteCharAt(linePlotCommand.length() - 1);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Methods that will make data files">
    /**
     *
     * Generates a file that contains the percent of use of a determined stream
     * on a selected interval.
     *
     * @param file
     * @throws IOException
     */
    private void makeHistogramDataFile( File file ) throws IOException {
        dataWriter = new FileWriter(file);

        //Make data header
        dataWriter.append("#\"Stream\"\t");
        for ( HistogramInterval interval : histogramIntervals ) {
            dataWriter.append("\"");
            dataWriter.append(interval.toString());
            dataWriter.append("\"\t");
        }
        dataWriter.append("\"Remainder\"\r\n");
        //End make data header

        //Make line with data
        if ( actualStream != -1 ) {
            //Write the number of stream
            dataWriter.append(Integer.toString(actualStream) + "\t");

            double fullRate = 100.0D;
            double intervalRate;
            for ( HistogramInterval hi : histogramIntervals ) {
                intervalRate = hi.getTxOccurrence(actualStream);
                fullRate -= intervalRate;
                dataWriter.append(Double.toString(intervalRate)).append("\t");
            }
            if ( fullRate < 0 ) {
                fullRate = 0D;
            }
            dataWriter.append(Double.toString(fullRate)).append("\r\n");

        }
        else {

            double fullRate = 100.0D;
            double intervalRate;
            for ( int currentStream = 0 ; currentStream < Analyzer.getInstance().getTamArrayStreams() ; currentStream++ ) {
                //Write the number of stream
                dataWriter.append(Integer.toString(currentStream) + "\t");

                for ( HistogramInterval hi : histogramIntervals ) {
                    intervalRate = hi.getTxOccurrence(currentStream);
                    fullRate -= intervalRate;
                    dataWriter.append(Double.toString(intervalRate)).append("\t");
                }
                if ( fullRate < 0 ) {
                    fullRate = 0D;
                }
                dataWriter.append(Double.toString(fullRate)).append("\r\n");
                dataWriter.append("\r\n");
                fullRate = 100.0D;

            }
        }
    }

    /**
     *
     * Generates a file to be used as repository of data that expect to be
     * ploted.
     *
     * @param file The name of file to be createad for save data set.
     * @throws IOException
     */
    private void makeSingleLineDataFile( File file ) throws IOException {

        dataWriter = new FileWriter(file);

        int qtd_lines;
        qtd_lines = Analyzer.getInstance().getStream(actualStream).delay.size();
        if ( metricToPlot.equals(StreamMetric.DELAY) ) {
            dataWriter.append("Time \"Delay of stream ").append(new Integer(actualStream).toString()).append("\"\r\n");
            for ( int i = 0 ; i < qtd_lines ; i++ ) {
                dataWriter.append(Double.toString(Analyzer.getInstance().getStream(actualStream).delay.get(i).getTxTime()));
                dataWriter.append(" ");
                if ( Analyzer.getInstance().getStream(actualStream).getDelay()[i] != Double.MAX_VALUE ) {
                    dataWriter.append(Double.toString(Analyzer.getInstance().getStream(actualStream).getDelay()[i]));
                }
                else {
                    dataWriter.append("NaN");
                }
                dataWriter.append("\r\n");
            }
        }
        else if ( metricToPlot.equals(StreamMetric.PDV) ) {
            dataWriter.append("Time \"PDV of stream ").append(new Integer(actualStream).toString()).append("\"\r\n");
            for ( int i = 0 ; i < qtd_lines ; i++ ) {
                dataWriter.append(Double.toString(Analyzer.getInstance().getStream(actualStream).delay.get(i).getTxTime()));
                dataWriter.append(" ");
                if ( Analyzer.getInstance().getStream(actualStream).getPDV()[i] != Double.MAX_VALUE ) {
                    dataWriter.append(Double.toString(Analyzer.getInstance().getStream(actualStream).getPDV()[i]));
                }
                else {
                    dataWriter.append("NaN");
                }
                dataWriter.append("\r\n");
            }
        }
        else if ( metricToPlot.equals(StreamMetric.IPDV) ) {
            dataWriter.append("Time \"IPDV of stream ").append(new Integer(actualStream).toString()).append("\"\r\n");
            for ( int i = 0 ; i < qtd_lines ; i++ ) {
                dataWriter.append(Double.toString(Analyzer.getInstance().getStream(actualStream).delay.get(i).getTxTime()));
                dataWriter.append(" ");
                if ( Analyzer.getInstance().getStream(actualStream).getIPDV()[i] != Double.MAX_VALUE ) {
                    dataWriter.append(Double.toString(Analyzer.getInstance().getStream(actualStream).getIPDV()[i]));
                }
                else {
                    dataWriter.append("NaN");
                }
                dataWriter.append("\r\n");
            }
        }

    }

    /**
     *
     * Generates a file with all data set of determinated streams information
     * (Delay, PDV or IPDV variations)
     *
     * @param file The name of file to be createad for save data set.
     * @throws IOException
     */
    private void makeMultiDataFile( File file ) throws IOException {
        dataWriter = new FileWriter(file);

        int qtd_lines;
        switch ( metricToPlot ) {
            case DELAY:
                for ( int j = 0 ; j < Analyzer.getInstance().getTamArrayStreams() ; j++ ) {
                    qtd_lines = Analyzer.getInstance().getStream(j).delay.size();
                    dataWriter.append("Time \"Delay of stream ").append(new Integer(j).toString()).append("\"\r\n");
                    for ( int i = 0 ; i < qtd_lines ; i++ ) {
                        dataWriter.append(Double.toString(Analyzer.getInstance().getStream(j).delay.get(i).getTxTime()));
                        dataWriter.append(" ");
                        dataWriter.append(Double.toString(Analyzer.getInstance().getStream(j).getDelay()[i]));
                        dataWriter.append("\r\n");
                    }
                    dataWriter.append("\r\n\r\n");
                }
                break;
            case PDV:
                for ( int j = 0 ; j < Analyzer.getInstance().getTamArrayStreams() ; j++ ) {
                    qtd_lines = Analyzer.getInstance().getStream(j).delay.size();
                    dataWriter.append("Time \"PDV of stream ").append(new Integer(j).toString()).append("\"\r\n");
                    for ( int i = 0 ; i < qtd_lines ; i++ ) {
                        dataWriter.append(Double.toString(Analyzer.getInstance().getStream(j).delay.get(i).getTxTime()));
                        dataWriter.append(" ");
                        dataWriter.append(Double.toString(Analyzer.getInstance().getStream(j).getPDV()[i]));
                        dataWriter.append("\r\n");
                    }
                    dataWriter.append("\r\n\r\n");
                }
                break;
            case IPDV:
                for ( int j = 0 ; j < Analyzer.getInstance().getTamArrayStreams() ; j++ ) {
                    qtd_lines = Analyzer.getInstance().getStream(j).delay.size();
                    dataWriter.append("Time \"IPDV of stream ").append(new Integer(j).toString()).append("\"\r\n");
                    for ( int i = 0 ; i < qtd_lines ; i++ ) {
                        dataWriter.append(Double.toString(Analyzer.getInstance().getStream(j).delay.get(i).getTxTime()));
                        dataWriter.append(" ");
                        dataWriter.append(Double.toString(Analyzer.getInstance().getStream(j).getIPDV()[i]));
                        dataWriter.append("\r\n");
                    }
                    dataWriter.append("\r\n\r\n");
                }
                break;

        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Swing buttons and other components actions ">
    private void jCB_gridOnYItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_gridOnYItemStateChanged
        if ( evt.getStateChange() == ItemEvent.DESELECTED ) {
            if ( !jCB_gridOnX.isSelected() ) {
                jCB_enableGrid.setSelected(false);
            }
        }
    }//GEN-LAST:event_jCB_gridOnYItemStateChanged

    private void jCB_gridOnXItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_gridOnXItemStateChanged
        if ( evt.getStateChange() == ItemEvent.DESELECTED ) {
            if ( !jCB_gridOnY.isSelected() ) {
                jCB_enableGrid.setSelected(false);
            }
        }
    }//GEN-LAST:event_jCB_gridOnXItemStateChanged

    private void jCB_enableGridItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_enableGridItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            jCB_gridOnX.setEnabled(true);
            jCB_gridOnX.setSelected(true);
            jCB_gridOnY.setEnabled(true);
            jCB_gridOnY.setSelected(true);
            jL_GridLineWidth.setEnabled(true);
            jSpinner_gridLineWidth.setEnabled(true);
        }
        else {
            jCB_gridOnX.setEnabled(false);
            jCB_gridOnX.setSelected(false);
            jCB_gridOnY.setEnabled(false);
            jCB_gridOnY.setSelected(false);
            jL_GridLineWidth.setEnabled(false);
            jSpinner_gridLineWidth.setEnabled(false);
        }
    }//GEN-LAST:event_jCB_enableGridItemStateChanged

    private void jRB_PDVItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRB_PDVItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            metricToPlot = StreamMetric.PDV;
            if ( jTF_Yaxis.getText().matches("(IPDV|Delay) variation") || jTF_Yaxis.getText().isEmpty() ) {
                jTF_Yaxis.setText("PDV variation");
            }
            jCoB_minimumY.setSelectedItem(minPDV);
            jCoB_maximumY.setSelectedItem(maxPDV);
        }
    }//GEN-LAST:event_jRB_PDVItemStateChanged

    private void jRB_DelayItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRB_DelayItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            metricToPlot = StreamMetric.DELAY;
            if ( jTF_Yaxis.getText().matches("(I)?PDV variation") || jTF_Yaxis.getText().isEmpty() ) {
                jTF_Yaxis.setText("Delay variation");
            }
            jCoB_minimumY.setSelectedItem(minDelay);
            jCoB_maximumY.setSelectedItem(maxDelay);
        }
    }//GEN-LAST:event_jRB_DelayItemStateChanged

    private void jRB_IPDVItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRB_IPDVItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            if ( jCB_YlogBase.isSelected() ) {
                jL_warningIPDV.setVisible(true);
                jL_warningIPDV.setForeground(new Color(250, 0, 0));
            }
            jCB_YlogBase.setEnabled(false);
            jCB_YlogBase.setSelected(false);
            jCoB_minimumY.setSelectedItem(minIPDV);
            jCoB_maximumY.setSelectedItem(maxIPDV);
        }
        else {
            jL_warningIPDV.setVisible(false);
            jL_warningIPDV.setForeground(new Color(240, 240, 240));
            if ( jCB_EnableLogScale.isSelected() ) {
                jCB_YlogBase.setEnabled(true);
            }
        }
        //Redundancy for clear read of code
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            metricToPlot = StreamMetric.IPDV;
            if ( jTF_Yaxis.getText().matches("(PDV|Delay) variation") || jTF_Yaxis.getText().isEmpty() ) {
                jTF_Yaxis.setText("IPDV variation");
            }
        }
    }//GEN-LAST:event_jRB_IPDVItemStateChanged

    private void jCB_XlogBaseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_XlogBaseItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            jCoB_XlogBase.setEnabled(true);
            jCoB_XlogBase.setFocusable(true);
        }
        else {
            jCoB_XlogBase.setEnabled(false);
            if ( !jCB_YlogBase.isSelected() ) {
                jCB_EnableLogScale.setSelected(false);
            }
        }
    }//GEN-LAST:event_jCB_XlogBaseItemStateChanged

    private void jCB_YlogBaseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_YlogBaseItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            jCoB_YlogBase.setEnabled(true);
        }
        else {
            jCoB_YlogBase.setEnabled(false);
            if ( !jCB_XlogBase.isSelected() ) {
                jCB_EnableLogScale.setSelected(false);
            }
        }
    }//GEN-LAST:event_jCB_YlogBaseItemStateChanged

    private void jCB_EnableLogScaleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_EnableLogScaleItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            jCB_XlogBase.setEnabled(true);
            jCB_XlogBase.setSelected(true);
            if ( !jRB_IPDV.isSelected() ) {
                jCB_YlogBase.setEnabled(true);
                jCB_YlogBase.setSelected(true);
            }
            else {
                jCB_YlogBase.setEnabled(false);
                jCB_YlogBase.setSelected(false);
            }
        }
        else {
            jCB_XlogBase.setEnabled(false);
            jCB_XlogBase.setSelected(false);
            jCB_YlogBase.setEnabled(false);
            jCB_YlogBase.setSelected(false);
        }
    }//GEN-LAST:event_jCB_EnableLogScaleItemStateChanged

    private void jCB_autoRangeYItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_autoRangeYItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            jL_minimumY.setEnabled(false);
            jL_maximumY.setEnabled(false);
            jCoB_minimumY.setEnabled(false);
            jCoB_maximumY.setEnabled(false);
        }
        else {
            jL_minimumY.setEnabled(true);
            jL_maximumY.setEnabled(true);
            jCoB_minimumY.setEnabled(true);
            jCoB_maximumY.setEnabled(true);
        }
    }//GEN-LAST:event_jCB_autoRangeYItemStateChanged

    private void jCB_Steps_Y_TicsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_Steps_Y_TicsItemStateChanged
        if ( jCB_Steps_Y_Tics.isSelected() ) {
            jSpinner_customYtics.setEnabled(true);
        }
        else {
            jSpinner_customYtics.setEnabled(false);
        }
    }//GEN-LAST:event_jCB_Steps_Y_TicsItemStateChanged

    private void jCB_Steps_X_TicsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_Steps_X_TicsItemStateChanged
        if ( jCB_Steps_X_Tics.isSelected() ) {
            jSpinner_customXtics.setEnabled(true);
        }
        else {
            jSpinner_customXtics.setEnabled(false);
        }
    }//GEN-LAST:event_jCB_Steps_X_TicsItemStateChanged

    private void jCoB_minimumXItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCoB_minimumXItemStateChanged
        //if (evt.getStateChange() == ItemEvent.SELECTED) {
        double maximumTmp = Double.parseDouble(jCoB_maximumX.getSelectedItem().toString());
        @SuppressWarnings("UseOfObsoleteCollectionType")
        Vector<Object> o = new Vector<Object>(jCoB_minimumX.getModel().getSize() - jCoB_minimumX.getSelectedIndex() + 1);
        for ( int i = jCoB_minimumX.getSelectedIndex() ; i < jCoB_minimumX.getModel().getSize() ; i++ ) {
            o.add(jCoB_minimumX.getModel().getElementAt(i));
        }
        jCoB_maximumX.setModel(new javax.swing.DefaultComboBoxModel<Object>(o));
        double newmaximumTmp = Double.parseDouble(jCoB_maximumX.getItemAt(0).toString());
        if ( newmaximumTmp < maximumTmp ) {
            jCoB_maximumX.setSelectedItem(new Double(maximumTmp));
        }

        System.out.println(jCoB_minimumX.getModel().getSize() - jCoB_minimumX.getSelectedIndex());
        if ( jCoB_minimumX.getModel().getSize() - jCoB_minimumX.getSelectedIndex() < 4 ) {
            jCB_autoRangeX.setSelected(true);
            jCB_autoRangeX.setEnabled(false);
        }
        else {
            jCB_autoRangeX.setSelected(true);
            jCB_autoRangeX.setEnabled(true);
        }
        //}
    }//GEN-LAST:event_jCoB_minimumXItemStateChanged

    private void jB_GenerateLineScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_GenerateLineScriptActionPerformed

        //Generate a set of gnuplot environment configurations
        configureLineEnvironment();

        if ( actualStream != -1 ) {
            makeSingleLinePlotCommand();
        }
        else {
            makeMultiLinePlotCommand();
        }

        try {

            configureOutputDataFileName();

            int status = showFileChooserDialog();
            if ( status == JFileChooser.APPROVE_OPTION ) {

                //File that will contain tha necessary data to plot
                File outputData_File = new File(fileChooser.getCurrentDirectory().toURI().getRawPath().replaceAll("%20", " ") + outputData_FileName);

                if ( actualStream != -1 ) {
                    makeSingleLineDataFile(outputData_File);
                }
                else {
                    makeMultiDataFile(outputData_File);
                }
                //Insert the name of file after 'plot' name on the StringBuilder
                linePlotCommand.insert(6, outputData_FileName);

                scriptFile = new File(fileChooser.getSelectedFile().toString());
                scriptWriter = new FileWriter(scriptFile);

                //Append instruction and setting of environment
                scriptWriter.append("#Instruction: to plot the graphic, type load \"" + fileChooser.getSelectedFile().getName().replaceAll("%20", " ") + "\" in the same directory.\r\n");
                scriptWriter.append("#You can type 'cd \"<dir>\"' on GNU-plot to reach it.\r\n".replace("<dir>", fileChooser.getCurrentDirectory().toString()));
                scriptWriter.append("#Make sure that the file \"" + outputData_FileName + "\" is in the same directory.\r\n\r\n");
                scriptWriter.append(environmentSettings.toString());
                //Append the plot command
                scriptWriter.append("#The inline command to be used on GNUPlot.\r\n");
                scriptWriter.append(linePlotCommand.toString());

                scriptWriter.close();
                dataWriter.close();

                javax.swing.JOptionPane.showMessageDialog(null, "The file " + fileChooser.getSelectedFile().getName() + " was sucessful saved.\n"
                        + "type \"load <file>\" on gnuplot to make the graphic.".replace("<file>", fileChooser.getSelectedFile().getName()) + ( ( actualStream == -1 ) ? "\nYou can customize each different line plot on file." : "" ));

                if ( output == TerminalTypes.CANVAS ) {
                    CanvasConfiguration configuration = (CanvasConfiguration) configurator.getTerminalConfiguration(output);
                    configuration.saveScripts(outputData_File.getParent(), configuration.getJsDirectoryName());
                }

                if ( jCB_autoGenerateOutput.isSelected() ) {
                    tryPlot();
                }
            }
            else {
                if ( status != JFileChooser.CANCEL_OPTION ) {
                    throw new IOException("Fail at save the file on" + fileChooser.getCurrentDirectory());
                }
            }

        }
        catch ( IOException ex ) {
            Logger.getLogger(ExportTCPGraphic.class.getName()).log(Level.SEVERE, null, ex);
            javax.swing.JOptionPane.showMessageDialog(null, "Some problem occured. Verify if you have permission on the selected directory.\n" + ex);
        }
    }//GEN-LAST:event_jB_GenerateLineScriptActionPerformed

    private void jB_GenerateHistogramScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_GenerateHistogramScriptActionPerformed
        if ( jList_intervals.getModel().getSize() > 0 ) {
            configureHistogramEnvironment();
            makeHistogramPlotCommand();
            findIntervalsOccurrences();

            try {
                configureOutputDataFileName();

                int status = showFileChooserDialog();

                if ( status == JFileChooser.APPROVE_OPTION ) {

                    //File that will contain tha necessary data to plot
                    File outputData_File = new File(fileChooser.getCurrentDirectory().toURI().getRawPath().replaceAll("%20", " ") + outputData_FileName);

                    makeHistogramDataFile(outputData_File);

                    scriptFile = new File(fileChooser.getSelectedFile().toString());
                    scriptWriter = new FileWriter(scriptFile);

                    //Append instruction and setting of environment
                    scriptWriter.append("#Instruction: to plot the graphic, type load \"" + fileChooser.getSelectedFile().getName() + "\" in the same directory.\r\n");
                    scriptWriter.append("#You can type 'cd \"<dir>\"' on GNU-plot to reach it.\r\n".replace("<dir>", fileChooser.getCurrentDirectory().toString()));
                    scriptWriter.append("#Make sure that the file \"" + outputData_FileName + "\" is in the same directory.\r\n\r\n");
                    scriptWriter.append(environmentSettings.toString());
                    //Append the plot command
                    scriptWriter.append("#The inline command to be used on GNUPlot.\r\n");
                    scriptWriter.append(histogramPlotCommand.toString().replaceAll("<file>", outputData_FileName));

                    scriptWriter.close();
                    dataWriter.close();

                    javax.swing.JOptionPane.showMessageDialog(null, "The file " + fileChooser.getSelectedFile().getName() + " was sucessful saved.\n"
                            + "type \"load <file>\" on gnuplot to make the graphic.".replace("<file>", fileChooser.getSelectedFile().getName()) + ( ( actualStream == -1 ) ? "\nYou can customize each different line plot on file." : "" ));

                    if ( output == TerminalTypes.CANVAS ) {
                        CanvasConfiguration configuration = (CanvasConfiguration) configurator.getTerminalConfiguration(output);
                        configuration.saveScripts(outputData_File.getParent(), fileChooser.getSelectedFile().getName());
                    }
                    if ( jCB_autoGenerateOutput.isSelected() ) {
                        tryPlot();
                    }


                }
                else {
                    if ( status != JFileChooser.CANCEL_OPTION ) {
                        throw new IOException("Fail at save the file on" + fileChooser.getCurrentDirectory());
                    }
                }
            }
            catch ( IOException ex ) {
                Logger.getLogger(ExportTCPGraphic.class.getName()).log(Level.SEVERE, null, ex);
                javax.swing.JOptionPane.showMessageDialog(null, "Some problem occured. Verify if you have permission on the selected directory.\n" + ex);
            }
        }
        else {
            JOptionPane.showMessageDialog(this, "Please, give at least one interval!");
        }
    }//GEN-LAST:event_jB_GenerateHistogramScriptActionPerformed

    private void jTP_plotsTypesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTP_plotsTypesStateChanged
        if ( JTP_smoothAndStyles.getComponents().length > 0 ) {
            if ( jTP_plotsTypes.getSelectedComponent() == jP_Lines ) {
                if ( JTP_smoothAndStyles.getSelectedComponent() != jP_legends ) {
                    JTP_smoothAndStyles.setSelectedComponent(jP_smoothAndStyleforLines);
                }
                //Set legends accordingly
                jCB_legendsOutsideLegends.setSelected(false);
                jCB_legendsFillWithBorder.setSelected(false);
                //End legends
                histogramTitle = jTF_TitleInput.getText();
                histogramXlabelTitle = jTF_Xaxis.getText();
                histogramYlabelTitle = jTF_Yaxis.getText();
                jTF_TitleInput.setText(lineTitle);
                jTF_Xaxis.setText(lineXlabelTitle);
                jTF_Yaxis.setText(lineYlabelTitle);
            }
            else if ( jTP_plotsTypes.getSelectedComponent() == jP_Histogram ) {
                if ( JTP_smoothAndStyles.getSelectedComponent() != jP_legends ) {
                    JTP_smoothAndStyles.setSelectedComponent(jP_smoothAndStyleforHistogram);
                }
                //Set legends accordingly
                jCB_legendsOutsideLegends.setSelected(true);
                jCB_legendsFillWithBorder.setSelected(true);
                //End legends
                lineTitle = jTF_TitleInput.getText();
                lineXlabelTitle = jTF_Xaxis.getText();
                lineYlabelTitle = jTF_Yaxis.getText();
                jTF_TitleInput.setText(histogramTitle);
                jTF_Xaxis.setText(histogramXlabelTitle);
                jTF_Yaxis.setText(histogramYlabelTitle);
            }
        }

    }//GEN-LAST:event_jTP_plotsTypesStateChanged

    private void jB_addIntervalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_addIntervalActionPerformed
        HistogramInterval interval = new HistogramInterval();
        interval.setType(metricToPlot);
        try {

            interval.setBegin(new Double(jTF_beginHistogramInverval.getText().replaceAll(",", ".")));
            //Only accept crescent intervals
            interval.setEnd(new Double(jTF_endHistogramInverval.getText().replaceAll(",", ".")));
            histogramIntervals.add(interval);

            jList_intervals.setModel(new javax.swing.AbstractListModel() {
                Object[] strings = histogramIntervals.toArray();

                @Override
                public int getSize() {
                    return strings.length;
                }

                @Override
                public Object getElementAt( int i ) {
                    return strings[i];
                }
            });

        }
        catch ( InconsistentInterval inconsistentInterval ) {
            javax.swing.JOptionPane.showMessageDialog(this, "Invalid range");
        }
        catch ( NumberFormatException numberFormatException ) {
        }
        jTF_beginHistogramInverval.setFocusable(true);
    }//GEN-LAST:event_jB_addIntervalActionPerformed

    private void jB_delIntervalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_delIntervalActionPerformed
        histogramIntervals.removeAll(jList_intervals.getSelectedValuesList());
        jList_intervals.setModel(new javax.swing.AbstractListModel() {
            Object[] strings = histogramIntervals.toArray();

            @Override
            public int getSize() {
                return strings.length;
            }

            @Override
            public Object getElementAt( int i ) {
                return strings[i];
            }
        });
    }//GEN-LAST:event_jB_delIntervalActionPerformed

    private void jCB_histogramEnableLogScaleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_histogramEnableLogScaleItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            jCoB_histogramYlogBase.setEnabled(true);
        }
        else {
            jCoB_histogramYlogBase.setEnabled(false);
        }
    }//GEN-LAST:event_jCB_histogramEnableLogScaleItemStateChanged

    private void jCoB_decimalSignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCoB_decimalSignActionPerformed
        if ( jCoB_decimalSign.getSelectedIndex() == ( jCoB_decimalSign.getItemCount() - 1 ) ) {
            jCoB_decimalSign.setEditable(true);

        }
        else {
            jCoB_decimalSign.setEditable(false);
        }
    }//GEN-LAST:event_jCoB_decimalSignActionPerformed

    private void jCB_rotateXlabelItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_rotateXlabelItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            jSpinner_rotateXLabel.setEnabled(true);
        }
        else {
            jSpinner_rotateXLabel.setEnabled(false);
        }
    }//GEN-LAST:event_jCB_rotateXlabelItemStateChanged

    private void jCoB_simpleStylesTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCoB_simpleStylesTypeItemStateChanged
        String select = (String) jCoB_simpleStylesType.getSelectedItem();
        select = select.toLowerCase();
        if ( select.contains("points") ) {
            jL_pointWidth.setEnabled(true);
            jSpinner_pointWidth.setEnabled(true);
        }
        else {
            jL_pointWidth.setEnabled(false);
            jSpinner_pointWidth.setEnabled(false);
        }
        if ( select.contains("line") || select.contains("impulse") ) {
            jL_lineWidth.setEnabled(true);
            jSpinner_lineWidth.setEnabled(true);
        }
        else {
            jL_lineWidth.setEnabled(false);
            jSpinner_lineWidth.setEnabled(false);
            if ( select.contains("box") ) {
                jL_boxWidth.setEnabled(true);
                jSpinner_boxWidth.setEnabled(true);
                jL_lineWidth.setEnabled(true);
                jSpinner_lineWidth.setEnabled(true);
            }
            else {
                jL_boxWidth.setEnabled(false);
                jSpinner_boxWidth.setEnabled(false);
                jL_lineWidth.setEnabled(false);
                jSpinner_lineWidth.setEnabled(false);
            }
        }
    }//GEN-LAST:event_jCoB_simpleStylesTypeItemStateChanged

    private void JTP_smoothAndStylesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_JTP_smoothAndStylesStateChanged
        if ( jTP_plotsTypes.getComponents().length > 0 ) {
            if ( JTP_smoothAndStyles.getSelectedComponent() == jP_smoothAndStyleforLines ) {
                jTP_plotsTypes.setSelectedComponent(jP_Lines);
            }
            else if ( JTP_smoothAndStyles.getSelectedComponent() == jP_smoothAndStyleforHistogram ) {
                jTP_plotsTypes.setSelectedComponent(jP_Histogram);
            }
        }
    }//GEN-LAST:event_JTP_smoothAndStylesStateChanged

    private void jCB_histogramRotateXlabelItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_histogramRotateXlabelItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            jSpinner_histogramRotateXLabel.setEnabled(true);
        }
        else {
            jSpinner_histogramRotateXLabel.setEnabled(false);
        }
    }//GEN-LAST:event_jCB_histogramRotateXlabelItemStateChanged

    private void jCoB_histogramDecimalSignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCoB_histogramDecimalSignActionPerformed
        if ( jCoB_histogramDecimalSign.getSelectedIndex() == ( jCoB_decimalSign.getItemCount() - 1 ) ) {
            jCoB_histogramDecimalSign.setEditable(true);

        }
        else {
            jCoB_histogramDecimalSign.setEditable(false);
        }
    }//GEN-LAST:event_jCoB_histogramDecimalSignActionPerformed

    private void jCB_histogramSteps_Y_TicsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_histogramSteps_Y_TicsItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jCB_histogramSteps_Y_TicsItemStateChanged

    private void jRB_stackedHistogramItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRB_stackedHistogramItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            jCB_histogramEnableLogScale.setSelected(false);
            jCB_histogramEnableLogScale.setEnabled(false);
        }
        else {
            jCB_histogramEnableLogScale.setEnabled(true);
        }
    }//GEN-LAST:event_jRB_stackedHistogramItemStateChanged

    private void jB_outputSetupOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_outputSetupOptionsActionPerformed
        configureOutputDataFileName();
        configurator.setVisible(true);
    }//GEN-LAST:event_jB_outputSetupOptionsActionPerformed

    private void jTF_endHistogramInvervalKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTF_endHistogramInvervalKeyTyped
        if ( evt.paramString().contains("primaryLevelUnicode=10") ) {
            jB_addIntervalActionPerformed(null);
        }

    }//GEN-LAST:event_jTF_endHistogramInvervalKeyTyped

    private void jB_clearAllIntervalsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_clearAllIntervalsActionPerformed
        if ( JOptionPane.showConfirmDialog(this, "Are you shure?", "Remove all intervals?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ) {
            histogramIntervals.clear();
            jList_intervals.setModel(new javax.swing.AbstractListModel() {
                Object[] strings = histogramIntervals.toArray();

                @Override
                public int getSize() {
                    return strings.length;
                }

                @Override
                public Object getElementAt( int i ) {
                    return strings[i];
                }
            });
        }
    }//GEN-LAST:event_jB_clearAllIntervalsActionPerformed

    private void jTF_beginHistogramInvervalKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTF_beginHistogramInvervalKeyTyped
        if ( evt.paramString().contains("primaryLevelUnicode=10") ) {
            jB_addIntervalActionPerformed(null);
        }
    }//GEN-LAST:event_jTF_beginHistogramInvervalKeyTyped

    private void jCoB_outputItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCoB_outputItemStateChanged
        TerminalTypes item = (TerminalTypes) evt.getItem();
        if ( item.isFileoutput() ) {
            jCB_autoGenerateOutput.setEnabled(true);
        }
        else {
            jCB_autoGenerateOutput.setEnabled(false);
        }
    }//GEN-LAST:event_jCoB_outputItemStateChanged

    private void jCoB_outputMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCoB_outputMouseEntered
        TerminalTypes configuration = (TerminalTypes) jCoB_output.getSelectedItem();
        jCoB_output.setToolTipText(configuration.getDescription());
    }//GEN-LAST:event_jCoB_outputMouseEntered

    private void jL_outputHelpIconMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jL_outputHelpIconMouseEntered
        TerminalTypes configuration = (TerminalTypes) jCoB_output.getSelectedItem();
        jL_outputHelpIcon.setToolTipText(configuration.getDescription());
    }//GEN-LAST:event_jL_outputHelpIconMouseEntered

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        jL_preview.setBounds(0, 0, jLP_graphicView.getSize().width, jLP_graphicView.getSize().height);
//        if ( jL_preview.getIcon() != null ) {
//            jB_makePreviewActionPerformed(null);
//        }
    }//GEN-LAST:event_formComponentResized

    private void jB_makePreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_makePreviewActionPerformed
        if ( jB_makePreview.getText() != null ) {
            jB_makePreview.setText(null);
            jB_makePreview.setToolTipText("Reload view");
            jB_makePreview.setIcon(new ImageIcon(getClass().getResource("/analyzer/gui/images/reload.png")));
            jB_makePreview.setBounds(15, 15, 32, 32);
        }

        jL_preview.setIcon(null);

        //File scriptFile = null;
        if ( jB_GenerateLineScript.isShowing() ) {
            configureLineEnvironment();

            if ( actualStream != -1 ) {
                makeSingleLinePlotCommand();
            }
            else {
                makeMultiLinePlotCommand();
            }
            try {
                if ( previewLinesOutputData_File == null ) {
                    dataWriter = null;
                    newValues.updateValues(oldValues.getXminValue(), oldValues.getXmaxValue(), oldValues.getOldIntervals(), metricToPlot, ExportWindowState.TypeOfGraphic.LINES);
                    previewLinesOutputData_File = File.createTempFile("linepreview_" + actualStream + "_", ".dat");
                }
                if ( dataFileNeedUpdate() || dataWriter == null ) {
                    if ( actualStream != -1 ) {
                        makeSingleLineDataFile(previewLinesOutputData_File);
                    }
                    else {
                        makeMultiDataFile(previewLinesOutputData_File);
                    }
                    dataWriter.close();
                }
                //Insert the name of file after 'plot' name on the StringBuilder
                linePlotCommand.insert(6, previewLinesOutputData_File.getName());

                scriptFile = File.createTempFile("preview", ".txt");
                scriptWriter = new FileWriter(scriptFile);

                scriptWriter.append(environmentSettings.toString());
                scriptWriter.append(linePlotCommand.toString());

                scriptWriter.close();
                jL_preview.setIcon(plotter.makePreview(scriptFile, jL_preview.getSize()));
            }
            catch ( IOException ex ) {
                Logger.getLogger(ExportTCPGraphic.class.getName()).log(Level.SEVERE, null, ex);
                jL_preview.setIcon(plotter.makePreview(null, jL_preview.getSize()));
            }
            finally {
                if ( previewLinesOutputData_File != null ) {
                    previewLinesOutputData_File.deleteOnExit();
                }
                if ( scriptFile != null ) {
                    scriptFile.delete();
                }

            }
        }
        else if ( jB_GenerateHistogramScript.isShowing() ) {

            if ( jList_intervals.getModel().getSize() > 0 ) {

                try {
                    configureHistogramEnvironment();
                    makeHistogramPlotCommand();
                    findIntervalsOccurrences();
                    if ( previewHistogramOutputData_File == null ) {
                        dataWriter = null;
                        newValues.updateValues(oldValues.getXminValue(), oldValues.getXmaxValue(), oldValues.getOldIntervals(), metricToPlot, ExportWindowState.TypeOfGraphic.HISTOGRAM);
                        previewHistogramOutputData_File = File.createTempFile("histopreview_" + actualStream + "_", ".dat");
                    }
                    if ( dataFileNeedUpdate() || dataWriter == null ) {
                        makeHistogramDataFile(previewHistogramOutputData_File);
                        dataWriter.close();
                    }
                    scriptFile = File.createTempFile("preview", ".txt", previewHistogramOutputData_File.getParentFile());
                    scriptWriter = new FileWriter(scriptFile);

                    scriptWriter.append(environmentSettings.toString());

                    scriptWriter.append(histogramPlotCommand.toString().replaceAll("<file>", previewHistogramOutputData_File.getName()));

                    scriptWriter.close();

                    jL_preview.setIcon(plotter.makePreview(scriptFile, jL_preview.getSize()));

                }
                catch ( IOException ex ) {
                    Logger.getLogger(ExportTCPGraphic.class.getName()).log(Level.SEVERE, null, ex);
                    jL_preview.setIcon(plotter.makePreview(null, jL_preview.getSize()));
                }
                finally {
                    if ( previewHistogramOutputData_File != null ) {
                        previewHistogramOutputData_File.deleteOnExit();
                    }
                    if ( scriptFile != null ) {
                        scriptFile.delete();
                    }

                }

            }
            else {
                JOptionPane.showMessageDialog(null, "Give at least one interval!");
                jL_preview.setIcon(plotter.makePreview(null, jL_preview.getSize()));
                zoomImageLabel.setIcon(plotter.makePreview(null, jL_preview.getSize()));
            }


        }
    }//GEN-LAST:event_jB_makePreviewActionPerformed

    private void jL_previewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jL_previewMouseClicked
        try {
            zoomImageLabel.setIcon(new ImageIcon(plotter.getLargePreview()));
            zoomImage.setVisible(true);
        }
        catch ( NullPointerException npe ) {
        }
    }//GEN-LAST:event_jL_previewMouseClicked

    private void jCB_timeOnXaxisItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_timeOnXaxisItemStateChanged
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            jCoB_timeUnit.setEnabled(true);
        }
        else {
            jCoB_timeUnit.setEnabled(false);
        }
    }//GEN-LAST:event_jCB_timeOnXaxisItemStateChanged

    private void jCoB_maximumXItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCoB_maximumXItemStateChanged
        if ( jCoB_maximumX.getSelectedIndex() < 3 ) {
            jCB_autoRangeX.setSelected(true);
            jCB_autoRangeX.setEnabled(false);
        }
        else {
            jCB_autoRangeX.setSelected(true);
            jCB_autoRangeX.setEnabled(true);
        }
    }//GEN-LAST:event_jCoB_maximumXItemStateChanged
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc=" Swing variables declaration ">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane JTP_smoothAndStyles;
    private javax.swing.ButtonGroup bg_histogramFill;
    private javax.swing.ButtonGroup bg_histogramStyle;
    private javax.swing.ButtonGroup bg_legendPosition;
    private javax.swing.ButtonGroup bg_outputFormat;
    private javax.swing.ButtonGroup bg_streamToPlot;
    private javax.swing.JButton jB_GenerateHistogramScript;
    private javax.swing.JButton jB_GenerateLineScript;
    private javax.swing.JButton jB_addInterval;
    private javax.swing.JButton jB_clearAllIntervals;
    private javax.swing.JButton jB_delInterval;
    private final javax.swing.JButton jB_makePreview = new javax.swing.JButton();
    private javax.swing.JButton jB_outputSetupOptions;
    private javax.swing.JCheckBox jCB_EnableLogScale;
    private javax.swing.JCheckBox jCB_Steps_X_Tics;
    private javax.swing.JCheckBox jCB_Steps_Y_Tics;
    private javax.swing.JCheckBox jCB_XlogBase;
    private javax.swing.JCheckBox jCB_YlogBase;
    private javax.swing.JCheckBox jCB_autoGenerateOutput;
    private javax.swing.JCheckBox jCB_autoRangeX;
    private javax.swing.JCheckBox jCB_autoRangeY;
    private javax.swing.JCheckBox jCB_boxesWithoutBorder;
    private javax.swing.JCheckBox jCB_enableGrid;
    private javax.swing.JCheckBox jCB_gridOnX;
    private javax.swing.JCheckBox jCB_gridOnY;
    private javax.swing.JCheckBox jCB_histogramEnableLogScale;
    private javax.swing.JCheckBox jCB_histogramRotateXlabel;
    private javax.swing.JCheckBox jCB_histogramSteps_Y_Tics;
    private javax.swing.JCheckBox jCB_legendsFillWithBorder;
    private javax.swing.JCheckBox jCB_legendsHorizontalAlignment;
    private javax.swing.JCheckBox jCB_legendsOpaque;
    private javax.swing.JCheckBox jCB_legendsOutsideLegends;
    private javax.swing.JCheckBox jCB_rotateXlabel;
    private javax.swing.JCheckBox jCB_timeOnXaxis;
    private javax.swing.JComboBox jCoB_XlogBase;
    private javax.swing.JComboBox jCoB_YlogBase;
    private javax.swing.JComboBox jCoB_decimalSign;
    private javax.swing.JComboBox jCoB_histogramDecimalSign;
    private javax.swing.JComboBox jCoB_histogramYlogBase;
    private javax.swing.JComboBox jCoB_maximumX;
    private javax.swing.JComboBox jCoB_maximumY;
    private javax.swing.JComboBox jCoB_minimumX;
    private javax.swing.JComboBox jCoB_minimumY;
    private javax.swing.JComboBox jCoB_output;
    private javax.swing.JComboBox jCoB_simpleStylesType;
    private javax.swing.JComboBox jCoB_smoothTypes;
    private javax.swing.JComboBox jCoB_timeUnit;
    private javax.swing.JLayeredPane jLP_graphicView;
    private javax.swing.JLabel jL_GridLineWidth;
    private javax.swing.JLabel jL_MinimumX;
    private javax.swing.JLabel jL_OutputFormat;
    private javax.swing.JLabel jL_StreamsToPlot;
    private javax.swing.JLabel jL_beginInterval;
    private javax.swing.JLabel jL_boxWidth;
    private javax.swing.JLabel jL_decimalSignToShow;
    private javax.swing.JLabel jL_endInterval;
    private javax.swing.JLabel jL_fillUsing;
    private javax.swing.JLabel jL_histogramBoxWidth;
    private javax.swing.JLabel jL_histogramDecimalSignToShow;
    private javax.swing.JLabel jL_histogramGapSize;
    private javax.swing.JLabel jL_histogramLogScale;
    private javax.swing.JLabel jL_histogramStyle;
    private javax.swing.JLabel jL_intervalsHistogram;
    private javax.swing.JLabel jL_legendsLegendPosition;
    private javax.swing.JLabel jL_lineWidth;
    private javax.swing.JLabel jL_logScaleOnAxis;
    private javax.swing.JLabel jL_maximumX;
    private javax.swing.JLabel jL_maximumY;
    private javax.swing.JLabel jL_minimumY;
    private javax.swing.JLabel jL_outputHelpIcon;
    private javax.swing.JLabel jL_plotStyle;
    private javax.swing.JLabel jL_pointWidth;
    private javax.swing.JLabel jL_preview;
    private javax.swing.JLabel jL_smoothToUse;
    private javax.swing.JLabel jL_titleLabel;
    private javax.swing.JLabel jL_warningIPDV;
    private javax.swing.JLabel jL_xAxes;
    private javax.swing.JLabel jL_xAxisLabel;
    private javax.swing.JLabel jL_yAxes;
    private javax.swing.JLabel jL_yAxisLabel;
    private javax.swing.JList jList_intervals;
    private javax.swing.JPanel jP_Histogram;
    private javax.swing.JPanel jP_Lines;
    private javax.swing.JPanel jP_Logscale;
    private javax.swing.JPanel jP_XRange;
    private javax.swing.JPanel jP_YRange;
    private javax.swing.JPanel jP_generalOptions;
    private javax.swing.JPanel jP_graphicView;
    private javax.swing.JPanel jP_gridConfigs;
    private javax.swing.JPanel jP_histogramIntervals;
    private javax.swing.JPanel jP_histogramLogscale;
    private javax.swing.JPanel jP_legends;
    private javax.swing.JPanel jP_smoothAndStyleforHistogram;
    private javax.swing.JPanel jP_smoothAndStyleforLines;
    private javax.swing.JPanel jP_widths;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRB_Delay;
    private javax.swing.JRadioButton jRB_IPDV;
    private javax.swing.JRadioButton jRB_PDV;
    private javax.swing.JRadioButton jRB_clusteredHistogram;
    private javax.swing.JRadioButton jRB_histogramFillPatterns;
    private javax.swing.JRadioButton jRB_histogramFillSolid;
    private javax.swing.JRadioButton jRB_stackedHistogram;
    private javax.swing.JScrollPane jSP_generalOptions;
    private javax.swing.JScrollPane jSP_plotsTypes;
    private javax.swing.JScrollPane jSP_smoothAndStyles;
    private javax.swing.JScrollPane jScrollPane_intervalsList;
    private javax.swing.JSpinner jSpinner_boxWidth;
    private javax.swing.JSpinner jSpinner_customXtics;
    private javax.swing.JSpinner jSpinner_customYtics;
    private javax.swing.JSpinner jSpinner_gridLineWidth;
    private javax.swing.JSpinner jSpinner_histogramBoxWidth;
    private javax.swing.JSpinner jSpinner_histogramCustomYtics;
    private javax.swing.JSpinner jSpinner_histogramGapSize;
    private javax.swing.JSpinner jSpinner_histogramRotateXLabel;
    private javax.swing.JSpinner jSpinner_lineWidth;
    private javax.swing.JSpinner jSpinner_pointWidth;
    private javax.swing.JSpinner jSpinner_rotateXLabel;
    private javax.swing.JToggleButton jTB_legendCenterBottom;
    private javax.swing.JToggleButton jTB_legendCenterCenter;
    private javax.swing.JToggleButton jTB_legendCenterTop;
    private javax.swing.JToggleButton jTB_legendLeftBottom;
    private javax.swing.JToggleButton jTB_legendLeftCenter;
    private javax.swing.JToggleButton jTB_legendLeftTop;
    private javax.swing.JToggleButton jTB_legendRightBottom;
    private javax.swing.JToggleButton jTB_legendRightCenter;
    private javax.swing.JToggleButton jTB_legendRightTop;
    private javax.swing.JTextField jTF_TitleInput;
    private javax.swing.JTextField jTF_Xaxis;
    private javax.swing.JTextField jTF_Yaxis;
    private javax.swing.JTextField jTF_beginHistogramInverval;
    private javax.swing.JTextField jTF_endHistogramInverval;
    private javax.swing.JTabbedPane jTP_plotsTypes;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
//</editor-fold>
}

/**
 * Used to help recognize when a change is needed in the data.
 *
 * @since 1.2.0
 * @version 1.0
 */
class ExportWindowState {

    public enum TypeOfGraphic {

        LINES, HISTOGRAM;
    }
    private StreamMetric metric = StreamMetric.DELAY;
    private double XminValue = 0.0D;
    private double XmaxValue = 0.0D;
    private LinkedList<HistogramInterval> intervals = new LinkedList<HistogramInterval>();
    private TypeOfGraphic lastTab = null;

    public void updateValues( Double xMin, Double xMax, Collection<HistogramInterval> intervals, StreamMetric metric, TypeOfGraphic lastTab ) {
        XmaxValue = xMax;
        XminValue = xMin;
        this.intervals.clear();
        this.intervals.addAll(intervals);
        this.metric = metric;
        this.lastTab = lastTab;
    }

    @Override
    public boolean equals( Object obj ) {
        ExportWindowState object;
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }

        if ( !( obj instanceof ExportWindowState ) ) {
            return false;
        }
        else {
            object = (ExportWindowState) obj;
            if ( object.getLastTab().equals(this.lastTab) ) {
                if ( object.getMetric() == this.metric ) {
                    if ( ( object.getXmaxValue() == this.XmaxValue ) && ( object.getXminValue() == this.XminValue ) ) {
                        if ( object.intervals.equals(this.intervals) ) {
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                }
            }
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + ( this.metric != null ? this.metric.hashCode() : 0 );
        hash = 83 * hash + (int) ( Double.doubleToLongBits(this.XminValue) ^ ( Double.doubleToLongBits(this.XminValue) >>> 32 ) );
        hash = 83 * hash + (int) ( Double.doubleToLongBits(this.XmaxValue) ^ ( Double.doubleToLongBits(this.XmaxValue) >>> 32 ) );
        hash = 83 * hash + ( this.intervals != null ? this.intervals.hashCode() : 0 );
        return hash;
    }

    public Double getXminValue() {
        return XminValue;
    }

    public void setXminValue( Double XminValue ) {
        this.XminValue = XminValue;
    }

    public Double getXmaxValue() {
        return XmaxValue;
    }

    public void setXmaxValue( Double XmaxValue ) {
        this.XmaxValue = XmaxValue;
    }

    public LinkedList<HistogramInterval> getOldIntervals() {
        return intervals;
    }

    public void setOldIntervals( LinkedList<HistogramInterval> oldIntervals ) {
        this.intervals = oldIntervals;
    }

    public StreamMetric getMetric() {
        return metric;
    }

    public TypeOfGraphic getLastTab() {
        return lastTab;
    }

    public void setLastTab( TypeOfGraphic lastTab ) {
        this.lastTab = lastTab;
    }
}
