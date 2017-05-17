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

import analyzer.controller.Utils;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JOptionPane;

/**
 *
 * This class shows a progress bar of the analysis, the elapsed time and the
 * estimated remaining time to finish the analysis.
 *
 * @since 1.0.0
 */
public class ProgressBar extends javax.swing.JFrame implements Observer {

    /**
     * Creates new form ProgressBar
     */
    long fileLength = 0;
    double step;
    boolean normalize;

    public ProgressBar(long tamanho) {

        initComponents();

        Point location = Utils.getCentralCoords(Toolkit.getDefaultToolkit().getScreenSize(), this.getBounds());
        this.setLocation(location);

        fileLength = tamanho;
        if (fileLength > Integer.MAX_VALUE) {

            normalize = true;
            step = fileLength / Integer.MAX_VALUE;
            jProgressBar1.setMaximum(Integer.MAX_VALUE);

        } else {

            normalize = false;
            jProgressBar1.setMaximum((int) fileLength);

        }

        jProgressBar1.setStringPainted(true);


    }

    public int getValor() {

        return jProgressBar1.getValue();

    }

    public void setValor(int valor) {

        jProgressBar1.setValue(valor);

    }

    public void setMax(int valor) {

        jProgressBar1.setMaximum(valor);

    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof Integer) {

            int size = (Integer) arg;
            if (normalize) {

                jProgressBar1.setValue(new Double(jProgressBar1.getValue() + (size / step)).intValue());

            } else {

                jProgressBar1.setValue((int) (jProgressBar1.getValue() + size));

            }

        } else if (arg instanceof Boolean) {
            dispose();

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

        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel1 = new javax.swing.JLabel();
        jLabelEstimatedTime = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Analyzing... ");
        setResizable(false);

        jLabel1.setText("Analyzing... Please wait");

        jLabelEstimatedTime.setText("Estimating...");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/process_stop.png"))); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabelEstimatedTime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(46, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(jLabelEstimatedTime, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (JOptionPane.showConfirmDialog(
                this, "Are you sure you want to stop the analysis?\nThe partial results are shown!",
                "want to stop the analysis?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            analyzer.controller.Analyzer.getInstance().setBreakAnalyzer(true);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private static javax.swing.JLabel jLabelEstimatedTime;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables

    public static void setEstimatedTime(String time) {

        jLabelEstimatedTime.setText(time);

    }
}