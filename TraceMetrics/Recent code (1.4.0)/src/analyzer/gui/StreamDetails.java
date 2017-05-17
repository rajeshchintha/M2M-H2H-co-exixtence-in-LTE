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

import analyzer.controller.UdpStreams;
import analyzer.controller.Utils;
import analyzer.entity.*;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import javax.swing.JFileChooser;


/**
 *
 * Class to show details of a TCP or UDP stream.
 *
 * @since 1.0.0
 */
public class StreamDetails extends javax.swing.JFrame {

    /**
     * Creates new form StreamDetails
     */
    TcpStream tcp;
    UdpStream udp;

    public StreamDetails() {
        initComponents();
        Point location = Utils.getCentralCoords(Toolkit.getDefaultToolkit().getScreenSize(), this.getBounds());
        this.setLocation(location);
    }

    public StreamDetails(TcpStream ts) {
        this();
        this.tcp = ts;
        fillTableTcp(ts);

    }

    public StreamDetails(UdpStream us) {
        this();
        this.udp = us;
        fillTableUdp(us);

    }

    public StreamDetails(ArrayList<UdpStream> udps) {
        this();
        fillTableUdpAll(udps);

    }

    private void fillTableUdpAll(ArrayList<UdpStream> udps) {

        NoEditTableModel dtm = new NoEditTableModel();
        dtm.addColumn("Nº");
        dtm.addColumn("Dropped packets");
        dtm.addColumn("Number of sequences");
        dtm.addColumn("Probability");

        ArrayList<DropCount> dc = new ArrayList<DropCount>();

        for (int i = 0; i < udps.size(); i++) {

            dc.add(udps.get(i).getDc());

        }

        ArrayList<UdpStat> stat = UdpStreams.getStatistics(dc);
        Object[] o = new Object[4];

        if (stat.get(0).getNum() == 0) {

            int sum = 0;
            for (int i = 0; i < udps.size(); i++) {

                sum += udps.get(i).getReceivedPackets();

            }

            stat.get(0).setQtt(sum);
        }
        int cont = 0;
        for (int i = 0, column = 0; i < stat.size(); i++,column = 0) {

            o[column++] = ++cont;
            o[column++] = stat.get(i).getNum();
            o[column++] = stat.get(i).getQtt();
            o[column++] = stat.get(i).getProbability();
            dtm.addRow(o);

        }

        jTableStream.setModel(dtm);

    }

    private void fillTableUdp(UdpStream udp) {

        NoEditTableModel dtm = new NoEditTableModel();
        dtm.addColumn("Nº");
        dtm.addColumn("Dropped packets");
        dtm.addColumn("Number of sequences");
        dtm.addColumn("Probability");

        ArrayList<DropCount> dc = new ArrayList<DropCount>();
        dc.add(udp.getDc());
        ArrayList<UdpStat> stat = UdpStreams.getStatistics(dc);
        Object[] o = new Object[4];

        if (stat.get(0).getNum() == 0) {
            stat.get(0).setQtt(udp.getReceivedPackets());
        }

        int cont = 0;
        for (int i = 0, column = 0; i < stat.size(); i++, column = 0) {

            o[column++] = ++cont;
            o[column++] = stat.get(i).getNum();
            o[column++] = stat.get(i).getQtt();
            o[column++] = stat.get(i).getProbability();
            dtm.addRow(o);

        }

        jTableStream.setModel(dtm);

    }

    private void fillTableTcp(TcpStream tcp) {

        NoEditTableModel dtm = new NoEditTableModel();
        dtm.addColumn("Nº");
        dtm.addColumn("Time");
        dtm.addColumn("Sequence number");
        dtm.addColumn("ACK number");
        dtm.addColumn("Delay");
        dtm.addColumn("PDV");
        dtm.addColumn("IPDV");

        jTableStream.setModel(dtm);
        jTableStream.getColumnModel().getColumn(0).setPreferredWidth(40);
        jTableStream.getColumnModel().getColumn(1).setPreferredWidth(40);
        
        int cont = 0;

        Object o[];
        for (int i = 0, column = 0; i < tcp.delay.size(); i++, column = 0) {

            o = new Object[7];
            o[column++] = ++cont;
            o[column++] = tcp.delay.get(i).getTxTime();
            o[column++] = tcp.delay.get(i).getSeqNumber();
            o[column++] = tcp.delay.get(i).getAck();
            o[column++] = tcp.delay.get(i).getDelay();

            if (tcp.getPDV()[i] == Double.MAX_VALUE) {
                o[column++] = "NaN";
            } else {
                o[column++] = tcp.getPDV()[i];
            }
            if (tcp.getIPDV()[i] == Double.MAX_VALUE) {
                o[column++] = "NaN";
            } else {
                o[column++] = tcp.getIPDV()[i];
            }
            dtm.addRow(o);

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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTableStream = new javax.swing.JTable();
        jButtonExport = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Stream details");
        setBackground(new java.awt.Color(248, 248, 248));

        jTableStream.setAutoCreateRowSorter(true);
        jScrollPane1.setViewportView(jTableStream);

        jButtonExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/document_export.png"))); // NOI18N
        jButtonExport.setText("Export");
        jButtonExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jButtonExportMouseReleased(evt);
            }
        });

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/application_exit.png"))); // NOI18N
        jButton1.setText("Close");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jButton1MouseReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 809, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(339, Short.MAX_VALUE)
                        .addComponent(jButtonExport)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1)
                        .addGap(0, 298, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonExport)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseReleased
        dispose();
    }//GEN-LAST:event_jButton1MouseReleased

    private void jButtonExportMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonExportMouseReleased

        String path;
        String result = "";
        MyFile file = new MyFile();
        JFileChooser fc = new JFileChooser();

        int status = fc.showDialog(null, "Save");
        if (status == JFileChooser.APPROVE_OPTION) {

            path = fc.getSelectedFile().getAbsolutePath();
            NoEditTableModel dtm = (NoEditTableModel) jTableStream.getModel();

            if (file.openFile2Write(path)) {

                for (int i = 0; i < dtm.getColumnCount(); i++) {

                    result += dtm.getColumnName(i) + "\t| ";

                }
                file.writeLine(result + "\n");

                for (int i = 0; i < dtm.getRowCount(); i++) {

                    result = "";
                    for (int j = 0; j < dtm.getColumnCount(); j++) {

                        result += "" + dtm.getValueAt(i, j) + "\t";

                    }
                    file.writeLine(result + "\n");

                }

                file.closeFile();

            }
        }
    }//GEN-LAST:event_jButtonExportMouseReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonExport;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableStream;
    // End of variables declaration//GEN-END:variables
}
