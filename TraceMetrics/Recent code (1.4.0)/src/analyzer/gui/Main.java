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
package analyzer.gui;

import analyzer.controller.Analyzer;
import analyzer.controller.Utils;
import analyzer.entity.Node;
import analyzer.entity.TcpStream;
import analyzer.entity.UdpStream;
import analyzer.gui.ExportGraphics.ExportTCPGraphic;
import analyzer.gui.bug.BugReport;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * The main class that will contain the menus and the other tabs.
 *
 * @version 1.1.0-b
 *
 */
public class Main extends javax.swing.JFrame implements Observer {

    /**
     * Variable used in all environment to know the program version, avoiding
     * inconsistent versions of the program.
     */
    public static final String VERSION = "1.4.0";
    /*
     * Actual file reference
     */
    private File openedFile;
    /*
     * Last directory opened, used as a convenience to the user on each open file action.
     * The program "remember" the last open directory.
     */
    private static String lastOpenedDirectory = System.getProperty("user.home");
    /*
     * Similar at up, but for know the last directory used to save files; for the user's convenience too.
     */
    public static String lastSavedDirectory = null;
    private ProgressBar progressBar;
    private static Main instance = null;
    private HashMap<String, JPanel> hashMapTabs = new HashMap<String, JPanel>();
    public Font defaultTraceFont;

    public final void setUIFont(javax.swing.plaf.FontUIResource f) {

        java.util.Enumeration keys = UIManager.getDefaults().keys();

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    private Main() {
        try {
            defaultTraceFont = Font.createFont(Font.ROMAN_BASELINE, getClass().getResourceAsStream("/analyzer/resources/Goulong.ttf")).deriveFont(13F);
            setUIFont(new javax.swing.plaf.FontUIResource(defaultTraceFont));
        } catch (FontFormatException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException nullPointerException) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, nullPointerException);
        }
        initComponents();
        setLocation(Utils.getCentralCoords(Toolkit.getDefaultToolkit().getScreenSize(), getBounds()));


        //Hide the two panels on the main window
        jB_BackAnalysis.setVisible(false);
        jTP_Principal.setVisible(false);
        jP_FileDetails.setVisible(false);
        //Center the program at the center of window
        Point location = Utils.getCentralCoords(Toolkit.getDefaultToolkit().getScreenSize(), this.getBounds());

        this.setLocation(location);

        //Manualy create the four tabs in the main's panel
        this.createTablePanels();
    }

    public static Main getInstance() {
        if (instance == null) {
            instance = new Main();
        }
        return instance;
    }

    public String getOpenedFileName() {
        return openedFile.getName();
    }

    private void createTablePanels() {

        CommonTablePanel commonTablePanel;
        NoEditTableModel noEditTableModel;
        SimulationDetails simulationDetails;
        GeneralNodeData generalNodeData;
        GeneralStreamData generalStreamData;

        //Create the "simulation" tab and add to jTP_Principal panel
        simulationDetails = new SimulationDetails("Simulation");
        hashMapTabs.put(simulationDetails.getName(), simulationDetails);
        this.jTP_Principal.add(simulationDetails);

        //Create the "nodes" tab and add to jTP_Principal panel
        generalNodeData = new GeneralNodeData("Nodes");
        hashMapTabs.put(generalNodeData.getName(), generalNodeData);
        this.jTP_Principal.add(generalNodeData);
        //The created table in tab node's can't be edited
        noEditTableModel = new NoEditTableModel();
        noEditTableModel.addColumn("Node");
        generalNodeData.getTableNode().setModel(noEditTableModel);

        //Create the "Throughput / Goodput" tab and add to jTP_Principal panel
        commonTablePanel = new CommonTablePanel("Throughput / Goodput");
        hashMapTabs.put(commonTablePanel.getName(), commonTablePanel);
        //Create the non editable table with three columns
        noEditTableModel = new NoEditTableModel();
        noEditTableModel.addColumn("Node");
        noEditTableModel.addColumn("Throughput");
        noEditTableModel.addColumn("Goodput");
        commonTablePanel.getTable().setModel(noEditTableModel);
        //Configure the 'node' columns width
        commonTablePanel.getTable().getColumnModel().getColumn(0).setMaxWidth(70);
        commonTablePanel.getTable().getColumnModel().getColumn(0).setMinWidth(30);
        commonTablePanel.getTable().getColumnModel().getColumn(0).setPreferredWidth(43);
        this.jTP_Principal.add(commonTablePanel);

        //Create the "Little's Result" tab and add to jTP_Principal panel
        commonTablePanel = new CommonTablePanel("Little's Result");
        hashMapTabs.put(commonTablePanel.getName(), commonTablePanel);
        //Create the non editable table with five columns
        noEditTableModel = new NoEditTableModel();
        noEditTableModel.addColumn("Node");
        noEditTableModel.addColumn("Lambda");
        noEditTableModel.addColumn("E[W]");
        noEditTableModel.addColumn("E[N]");
        noEditTableModel.addColumn("E[W] * Lambda");
        commonTablePanel.getTable().setModel(noEditTableModel);
        //Configure the 'node' columns width
        commonTablePanel.getTable().getColumnModel().getColumn(0).setMaxWidth(70);
        commonTablePanel.getTable().getColumnModel().getColumn(0).setMinWidth(30);
        commonTablePanel.getTable().getColumnModel().getColumn(0).setPreferredWidth(43);
        this.jTP_Principal.add(commonTablePanel);

        //Create the "Streams" tab and add to jTP_Principal panel
        generalStreamData = new GeneralStreamData("Streams");
        hashMapTabs.put(generalStreamData.getName(), generalStreamData);
        //The created table in tab node's can't be edited too like the others
        noEditTableModel = new NoEditTableModel();
        noEditTableModel.addColumn("Streams");
        generalStreamData.getTable().setModel(noEditTableModel);
        this.jTP_Principal.add(generalStreamData);

    }

    public void eraseTable(JTable table) {

        DefaultTableModel dtm = (DefaultTableModel) table.getModel();

        for (int i = dtm.getRowCount() - 1; i >= 0; i--) {

            dtm.removeRow(i);

        }
    }

    public void fillData() {

        fillSimulationDetails();
        fillNodes();
        fillThroughputGoodput();
        fillLittleResult();
        fillStreams();

    }

    private void emptyForms() {
        jTP_Principal.removeAll();
        hashMapTabs.clear();
        createTablePanels();
        System.gc();

    }

    private void fillThroughputGoodput() {

        CommonTablePanel commonTablePanel = (CommonTablePanel) hashMapTabs.get("Throughput / Goodput");
        ArrayList<Node> nodes = new ArrayList<Node>(Analyzer.getInstance().HashNodes().values());
        Collections.sort(nodes);
        Object[] obj = new Object[3];
        eraseTable(commonTablePanel.getTable());
        DefaultTableModel dtm = (DefaultTableModel) commonTablePanel.getTable().getModel();

        for (int i = 0; i < nodes.size(); i++) {

            obj[0] = nodes.get(i).getNum();
            obj[1] = nodes.get(i).getThroughput();
            obj[2] = nodes.get(i).getGoodput();
            dtm.addRow(obj);

        }

    }

    private void fillLittleResult() {

        CommonTablePanel commonTablePanel = (CommonTablePanel) hashMapTabs.get("Little's Result");
        ArrayList<Node> nodes = new ArrayList<Node>(Analyzer.getInstance().HashNodes().values());
        Collections.sort(nodes);
        Object[] obj = new Object[5];
        eraseTable(commonTablePanel.getTable());
        DefaultTableModel dtm = (DefaultTableModel) commonTablePanel.getTable().getModel();

        for (int i = 0; i < nodes.size(); i++) {

            obj[0] = nodes.get(i).getNum();
            obj[1] = nodes.get(i).getArrivalRate();
            obj[2] = nodes.get(i).getEwResult();
            obj[3] = nodes.get(i).getEnResult();
            obj[4] = nodes.get(i).getEwResult() * nodes.get(i).getArrivalRate();
            dtm.addRow(obj);

        }

    }

    private void fillSimulationDetails() {

        String text;
        SimulationDetails simulationDetails = (SimulationDetails) hashMapTabs.get("Simulation");
        text = Analyzer.getInstance().getSimulationInfo();
        simulationDetails.getTextPanel().setText(text);

    }

    private void fillNodes() {

        GeneralNodeData generalNodeData = (GeneralNodeData) hashMapTabs.get("Nodes");
        ArrayList<Node> nodes = new ArrayList<Node>(Analyzer.getInstance().HashNodes().values());
        Collections.sort(nodes);
        Object[] obj = new Object[1];
        eraseTable(generalNodeData.getTableNode());
        DefaultTableModel defaultTableModel = (DefaultTableModel) generalNodeData.getTableNode().getModel();
        generalNodeData.getTableNode().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        for (int i = 0; i < nodes.size(); i++) {

            obj[0] = nodes.get(i).getNum();
            defaultTableModel.addRow(obj);

        }

    }

    private void fillStreams() {


        GeneralStreamData generalStreamData = (GeneralStreamData) hashMapTabs.get("Streams");
        ArrayList<Node> nodes = new ArrayList<Node>(Analyzer.getInstance().HashNodes().values());
        Collections.sort(nodes);
        Object[] obj = new Object[1];
        eraseTable(generalStreamData.getTable());
        DefaultTableModel dtm = (DefaultTableModel) generalStreamData.getTable().getModel();
        generalStreamData.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ArrayList<TcpStream> tcpStream = new ArrayList<TcpStream>(Analyzer.getInstance().getTcpStreams().values());

        if (tcpStream.size() > 0) {

            obj[0] = "TCP ALL";
            dtm.addRow(obj);

            for (int i = 0; i < tcpStream.size(); i++) {

                obj[0] = "TCP " + i;
                dtm.addRow(obj);

            }
        }

        ArrayList<UdpStream> udpStream = new ArrayList<UdpStream>(Analyzer.getInstance().getUdpStreams().values());

        if (udpStream.size() > 0) {

            obj[0] = "UDP ALL";
            dtm.addRow(obj);

            for (int i = 0; i < udpStream.size(); i++) {

                obj[0] = "UDP " + i;
                dtm.addRow(obj);

            }
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

        jLayeredPane1 = new javax.swing.JLayeredPane();
        jTP_Principal = new javax.swing.JTabbedPane();
        jP_FileDetails = new javax.swing.JPanel();
        jL_Icon = new javax.swing.JLabel();
        jL_Name = new javax.swing.JLabel();
        jL_Size = new javax.swing.JLabel();
        jL_Path = new javax.swing.JLabel();
        jL_FileName = new javax.swing.JLabel();
        jL_FileSize = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTA_FilePath = new javax.swing.JTextArea();
        JL_Date = new javax.swing.JLabel();
        jL_FileDate = new javax.swing.JLabel();
        jL_Attribs = new javax.swing.JLabel();
        jL_Read = new javax.swing.JLabel();
        jL_Write = new javax.swing.JLabel();
        jL_Execute = new javax.swing.JLabel();
        jL_ReadPermission = new javax.swing.JLabel();
        jL_WritePermission = new javax.swing.JLabel();
        jL_ExecutePermision = new javax.swing.JLabel();
        jB_ExecuteAnalysis = new javax.swing.JButton();
        jB_BackAnalysis = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jM_File = new javax.swing.JMenu();
        jMI_ChooseFile = new javax.swing.JMenuItem();
        jMI_ExecuteAnalysis = new javax.swing.JMenuItem();
        jMI_Exit = new javax.swing.JMenuItem();
        jM_Tools = new javax.swing.JMenu();
        jMI_Options = new javax.swing.JMenuItem();
        jM_Help = new javax.swing.JMenu();
        jMI_Site = new javax.swing.JMenuItem();
        jMI_BUG = new javax.swing.JMenuItem();
        jMI_VerifyUpdate = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMI_About = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TraceMetrics - a trace analyzer for Network Simulator 3");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jTP_Principal.setBackground(new java.awt.Color(248, 248, 248));
        jTP_Principal.setPreferredSize(new java.awt.Dimension(740, 456));
        jTP_Principal.setBounds(0, 0, 740, 420);
        jLayeredPane1.add(jTP_Principal, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jP_FileDetails.setBackground(new java.awt.Color(248, 248, 248));
        jP_FileDetails.setMinimumSize(new java.awt.Dimension(740, 456));
        jP_FileDetails.setPreferredSize(new java.awt.Dimension(740, 457));
        jP_FileDetails.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jP_FileDetailsComponentHidden(evt);
            }
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jP_FileDetailsComponentShown(evt);
            }
        });

        jL_Icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/documentinfo_koffice.png"))); // NOI18N

        jL_Name.setFont(new java.awt.Font("Bitstream Vera Sans", 1, 13)); // NOI18N
        jL_Name.setText("Name:");

        jL_Size.setFont(new java.awt.Font("Bitstream Vera Sans", 1, 13)); // NOI18N
        jL_Size.setText("Size:");

        jL_Path.setFont(new java.awt.Font("Bitstream Vera Sans", 1, 13)); // NOI18N
        jL_Path.setText("Path:");

        jL_FileName.setText("<fileName>");

        jL_FileSize.setText("<fileSize>");

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setEnabled(false);

        jTA_FilePath.setBackground(new java.awt.Color(248, 248, 248));
        jTA_FilePath.setColumns(20);
        jTA_FilePath.setEditable(false);
        jTA_FilePath.setLineWrap(true);
        jTA_FilePath.setRows(5);
        jTA_FilePath.setWrapStyleWord(true);
        jTA_FilePath.setDisabledTextColor(new java.awt.Color(248, 248, 248));
        jScrollPane1.setViewportView(jTA_FilePath);

        JL_Date.setFont(new java.awt.Font("Bitstream Vera Sans", 1, 13)); // NOI18N
        JL_Date.setText("Modified:");

        jL_FileDate.setText("<modificationDate>");

        jL_Attribs.setFont(new java.awt.Font("Bitstream Vera Sans", 1, 13)); // NOI18N
        jL_Attribs.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jL_Attribs.setText("File's attributes:");

        jL_Read.setText("Read");

        jL_Write.setText("Write");

        jL_Execute.setText("Execute");

        jL_ReadPermission.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/dialog_ok_apply.png"))); // NOI18N

        jL_WritePermission.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/dialog_ok_apply.png"))); // NOI18N

        jL_ExecutePermision.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/dialog_ok_apply.png"))); // NOI18N

        jB_ExecuteAnalysis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/document_export.png"))); // NOI18N
        jB_ExecuteAnalysis.setText("Execute analysis");
        jB_ExecuteAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_ExecuteAnalysisActionPerformed(evt);
            }
        });

        jB_BackAnalysis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/back.png"))); // NOI18N
        jB_BackAnalysis.setText("Previous analysis");
        jB_BackAnalysis.setDefaultCapable(false);
        jB_BackAnalysis.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/go_next_rtl.png"))); // NOI18N
        jB_BackAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_BackAnalysisActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jP_FileDetailsLayout = new javax.swing.GroupLayout(jP_FileDetails);
        jP_FileDetails.setLayout(jP_FileDetailsLayout);
        jP_FileDetailsLayout.setHorizontalGroup(
            jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_FileDetailsLayout.createSequentialGroup()
                .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jP_FileDetailsLayout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jP_FileDetailsLayout.createSequentialGroup()
                                .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jL_Write)
                                    .addComponent(jL_Read)
                                    .addComponent(jL_Execute))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jL_ReadPermission, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jL_WritePermission, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jL_ExecutePermision, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addComponent(jL_Attribs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jL_Icon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jP_FileDetailsLayout.createSequentialGroup()
                                    .addGap(63, 63, 63)
                                    .addComponent(jL_Name))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jP_FileDetailsLayout.createSequentialGroup()
                                    .addGap(37, 37, 37)
                                    .addComponent(jL_Path)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jP_FileDetailsLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(JL_Date, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jL_Size, javax.swing.GroupLayout.Alignment.TRAILING))))
                        .addGap(18, 18, 18)
                        .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
                            .addGroup(jP_FileDetailsLayout.createSequentialGroup()
                                .addGap(0, 256, Short.MAX_VALUE)
                                .addComponent(jB_BackAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jP_FileDetailsLayout.createSequentialGroup()
                                .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jL_FileName)
                                    .addComponent(jL_FileSize)
                                    .addComponent(jL_FileDate))
                                .addGap(0, 344, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jP_FileDetailsLayout.createSequentialGroup()
                        .addContainerGap(546, Short.MAX_VALUE)
                        .addComponent(jB_ExecuteAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jP_FileDetailsLayout.setVerticalGroup(
            jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jP_FileDetailsLayout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jL_Name)
                    .addComponent(jL_FileName))
                .addGap(26, 26, 26)
                .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jL_Size)
                    .addComponent(jL_FileSize))
                .addGap(28, 28, 28)
                .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(JL_Date)
                    .addComponent(jL_FileDate))
                .addGap(34, 34, 34)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jB_BackAnalysis)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jB_ExecuteAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
            .addGroup(jP_FileDetailsLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jL_Icon, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jL_Attribs)
                    .addComponent(jL_Path))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jL_ReadPermission, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jL_Read, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jL_WritePermission, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jL_Write, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jP_FileDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jL_ExecutePermision, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jL_Execute, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 110, Short.MAX_VALUE))
        );

        jP_FileDetails.setBounds(0, 0, 740, 420);
        jLayeredPane1.add(jP_FileDetails, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jMenuBar1.setBackground(new java.awt.Color(248, 248, 248));
        jMenuBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jM_File.setText("File");

        jMI_ChooseFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        jMI_ChooseFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/empty.png"))); // NOI18N
        jMI_ChooseFile.setMnemonic('C');
        jMI_ChooseFile.setText("Choose file");
        jMI_ChooseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMI_ChooseFileActionPerformed(evt);
            }
        });
        jM_File.add(jMI_ChooseFile);

        jMI_ExecuteAnalysis.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        jMI_ExecuteAnalysis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/document_preview.png"))); // NOI18N
        jMI_ExecuteAnalysis.setMnemonic('x');
        jMI_ExecuteAnalysis.setText("Execute analysis");
        jMI_ExecuteAnalysis.setEnabled(false);
        jMI_ExecuteAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMI_ExecuteAnalysisActionPerformed(evt);
            }
        });
        jM_File.add(jMI_ExecuteAnalysis);

        jMI_Exit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        jMI_Exit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/application_exit.png"))); // NOI18N
        jMI_Exit.setText("Exit");
        jMI_Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMI_ExitActionPerformed(evt);
            }
        });
        jM_File.add(jMI_Exit);

        jMenuBar1.add(jM_File);

        jM_Tools.setText("Tools");

        jMI_Options.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMI_Options.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/configure.png"))); // NOI18N
        jMI_Options.setText("Options");
        jMI_Options.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMI_OptionsActionPerformed(evt);
            }
        });
        jM_Tools.add(jMI_Options);

        jMenuBar1.add(jM_Tools);

        jM_Help.setText("Help");

        jMI_Site.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/internet_web_browser.png"))); // NOI18N
        jMI_Site.setText("Visit the website");
        jMI_Site.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMI_SiteActionPerformed(evt);
            }
        });
        jM_Help.add(jMI_Site);

        jMI_BUG.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/tools_report_bug.png"))); // NOI18N
        jMI_BUG.setText("Report BUG");
        jMI_BUG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMI_BUGActionPerformed(evt);
            }
        });
        jM_Help.add(jMI_BUG);

        jMI_VerifyUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/system_software_update.png"))); // NOI18N
        jMI_VerifyUpdate.setText("Verify Updates");
        jMI_VerifyUpdate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jMI_VerifyUpdateMouseReleased(evt);
            }
        });
        jM_Help.add(jMI_VerifyUpdate);
        jM_Help.add(jSeparator1);

        jMI_About.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        jMI_About.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/help_about.png"))); // NOI18N
        jMI_About.setText("About");
        jMI_About.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMI_AboutActionPerformed(evt);
            }
        });
        jM_Help.add(jMI_About);

        jMenuBar1.add(jM_Help);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 739, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMI_ChooseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMI_ChooseFileActionPerformed



        javax.swing.filechooser.FileFilter traceFiles = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String extension = f.getName().toLowerCase();
                return (extension.endsWith(".tr"));

            }

            @Override
            public String getDescription() {
                return "TRACE files";
            }
        };

        JFileChooser fc = new JFileChooser(lastOpenedDirectory);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(traceFiles);

        if (!Analyzer.getInstance().isAnalysing()) {
            int status = fc.showOpenDialog(null);
            if (status == JFileChooser.APPROVE_OPTION) {
                openedFile = fc.getSelectedFile();


                jTP_Principal.setVisible(false);
                if (openedFile != null) {
                    jP_FileDetails.setVisible(false);
                }
                jP_FileDetails.setVisible(true);

                jMI_ExecuteAnalysis.setEnabled(true);

            }
            lastOpenedDirectory = fc.getCurrentDirectory().toString();
        } else {
            javax.swing.JOptionPane.showMessageDialog(null, "The analyzing isn't finished yet.");
        }
    }//GEN-LAST:event_jMI_ChooseFileActionPerformed

    private void jMI_ExecuteAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMI_ExecuteAnalysisActionPerformed
        ExportTCPGraphic.invalidateInstances();
        this.jMI_ExecuteAnalysis.setEnabled(false);
        if (!Analyzer.getInstance().isAnalysing()) {
            if (!openedFile.getName().isEmpty()) {
                emptyForms();
                jMI_ChooseFile.setEnabled(false);
                Analyzer.getInstance().setAnalysing(true);
                Analyzer.getInstance().openTrace(openedFile.getAbsolutePath());
                progressBar = new ProgressBar(Analyzer.getInstance().tamFile);
                progressBar.setVisible(true);
                progressBar.setAlwaysOnTop(true);
                Analyzer.getInstance().addObserver(progressBar);
                Analyzer.getInstance().addObserver(this);
                jMI_ChooseFile.setEnabled(true);
                jB_BackAnalysis.setVisible(true);
                new Thread(Analyzer.getInstance()).start();
            } else {

                javax.swing.JOptionPane.showMessageDialog(null, "No file selected!");
            }
            jTP_Principal.setVisible(true);
            jP_FileDetails.setVisible(false);
        } else {
            javax.swing.JOptionPane.showMessageDialog(null, "Are have a analyzing in progress!");
        }


    }//GEN-LAST:event_jMI_ExecuteAnalysisActionPerformed

    private void jMI_ExitActionPerformed( java.awt.event.ActionEvent evt ) {//GEN-FIRST:event_jMI_ExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMI_ExitActionPerformed

    private void jMI_OptionsActionPerformed( java.awt.event.ActionEvent evt ) {//GEN-FIRST:event_jMI_OptionsActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Options.getInstance().setVisible(true);
            }
        });
    }//GEN-LAST:event_jMI_OptionsActionPerformed

    private void jMI_AboutActionPerformed( java.awt.event.ActionEvent evt ) {//GEN-FIRST:event_jMI_AboutActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                About.getInstance().setVisible(true);
            }
        });
    }//GEN-LAST:event_jMI_AboutActionPerformed

    private void jMI_SiteActionPerformed( java.awt.event.ActionEvent evt ) {//GEN-FIRST:event_jMI_SiteActionPerformed
        Desktop d = Desktop.getDesktop();
        try {
            d.browse(new URI("http://www.tracemetrics.net"));
        } catch (URISyntaxException uRISyntaxException) {
        } catch (IOException iOException) {
        }

    }//GEN-LAST:event_jMI_SiteActionPerformed

    private void formWindowClosed( java.awt.event.WindowEvent evt ) {//GEN-FIRST:event_formWindowClosed
        instance = null;
        dispose();
    }//GEN-LAST:event_formWindowClosed

    private void jP_FileDetailsComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jP_FileDetailsComponentHidden


        jL_FileName.setText("");
        jL_FileSize.setText("");
        jTA_FilePath.setText("");
        jL_FileDate.setText("");

    }//GEN-LAST:event_jP_FileDetailsComponentHidden

    private void jP_FileDetailsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jP_FileDetailsComponentShown

        jL_FileName.setText(openedFile.getName());
        jL_FileSize.setText(new Long(openedFile.length()).toString() + " bytes. (" + Utils.convertData((double) openedFile.length(), 2) + ")");
        jTA_FilePath.setText(openedFile.getParent());
        jL_FileDate.setText(new Date(openedFile.lastModified()).toString());


        //Icons
        jL_ReadPermission.setIcon(new ImageIcon(getClass().getResource(openedFile.canRead() ? "/analyzer/gui/images/dialog_ok_apply.png" : "/analyzer/gui/images/dialog_close.png")));
        jL_WritePermission.setIcon(new ImageIcon(getClass().getResource(openedFile.canWrite() ? "/analyzer/gui/images/dialog_ok_apply.png" : "/analyzer/gui/images/dialog_close.png")));
        jL_ExecutePermision.setIcon(new ImageIcon(getClass().getResource(openedFile.canExecute() ? "/analyzer/gui/images/dialog_ok_apply.png" : "/analyzer/gui/images/dialog_close.png")));


    }//GEN-LAST:event_jP_FileDetailsComponentShown

    private void jB_ExecuteAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_ExecuteAnalysisActionPerformed
        this.jMI_ExecuteAnalysisActionPerformed(evt);
    }//GEN-LAST:event_jB_ExecuteAnalysisActionPerformed

    private void jB_BackAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_BackAnalysisActionPerformed
        this.jMI_ExecuteAnalysis.setEnabled(false);
        this.openedFile = null;
        this.jP_FileDetails.setVisible(false);
        this.jTP_Principal.setVisible(true);
    }//GEN-LAST:event_jB_BackAnalysisActionPerformed

    private void jMI_BUGActionPerformed( java.awt.event.ActionEvent evt ) {//GEN-FIRST:event_jMI_BUGActionPerformed
        BugReport rb = new BugReport();
        rb.setVisible(true);

    }//GEN-LAST:event_jMI_BUGActionPerformed

    private void jMI_VerifyUpdateMouseReleased( java.awt.event.MouseEvent evt ) {//GEN-FIRST:event_jMI_VerifyUpdateMouseReleased
        try {
            URL url = new URL("http://www.tracemetrics.net/version.txt");
            InputStream in = url.openStream();
            Scanner leitor = new Scanner(in);

            String version = leitor.nextLine();
            if (version.equals(Main.VERSION)) {
                javax.swing.JOptionPane.showMessageDialog(null, "TraceMetrics is updated.");
            } else {
                javax.swing.JOptionPane.showMessageDialog(null, "Tracemetrics has a new version.\nPlease visit the website.", "Update found!", javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException iOException) {
            javax.swing.JOptionPane.showMessageDialog(null, "A error ocourred. Please, verify your conection and try later.", "Fail", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (NoSuchElementException noSuchElementException) {
            BugReport bg = new BugReport(noSuchElementException.toString());
        }
    }//GEN-LAST:event_jMI_VerifyUpdateMouseReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel JL_Date;
    private javax.swing.JButton jB_BackAnalysis;
    private javax.swing.JButton jB_ExecuteAnalysis;
    private javax.swing.JLabel jL_Attribs;
    private javax.swing.JLabel jL_Execute;
    private javax.swing.JLabel jL_ExecutePermision;
    private javax.swing.JLabel jL_FileDate;
    private javax.swing.JLabel jL_FileName;
    private javax.swing.JLabel jL_FileSize;
    private javax.swing.JLabel jL_Icon;
    private javax.swing.JLabel jL_Name;
    private javax.swing.JLabel jL_Path;
    private javax.swing.JLabel jL_Read;
    private javax.swing.JLabel jL_ReadPermission;
    private javax.swing.JLabel jL_Size;
    private javax.swing.JLabel jL_Write;
    private javax.swing.JLabel jL_WritePermission;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JMenuItem jMI_About;
    private javax.swing.JMenuItem jMI_BUG;
    private javax.swing.JMenuItem jMI_ChooseFile;
    private javax.swing.JMenuItem jMI_ExecuteAnalysis;
    private javax.swing.JMenuItem jMI_Exit;
    private javax.swing.JMenuItem jMI_Options;
    private javax.swing.JMenuItem jMI_Site;
    private javax.swing.JMenuItem jMI_VerifyUpdate;
    private javax.swing.JMenu jM_File;
    private javax.swing.JMenu jM_Help;
    private javax.swing.JMenu jM_Tools;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jP_FileDetails;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTextArea jTA_FilePath;
    private javax.swing.JTabbedPane jTP_Principal;
    // End of variables declaration//GEN-END:variables

    public JTabbedPane getjTP_Principal() {
        return jTP_Principal;
    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof Boolean) {

            fillData();

        }

    }
}
