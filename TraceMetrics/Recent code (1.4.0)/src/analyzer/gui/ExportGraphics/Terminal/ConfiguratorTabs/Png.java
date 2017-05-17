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
package analyzer.gui.ExportGraphics.Terminal.ConfiguratorTabs;

import analyzer.controller.Utils;
import analyzer.gui.ExportGraphics.Terminal.TerminalConfigurations.PngConfiguration;
import analyzer.gui.ExportGraphics.Terminal.TerminalConfigurator;
import analyzer.gui.ExportGraphics.Terminal.TerminalTypes;
import analyzer.gui.ExportGraphics.Terminal.MeasureUnit;

/**
 * A panel that will be used as tab on
 * <code>TerminalConfigurator</code> objects.
 *
 * @since 1.2.0
 */
public class Png extends javax.swing.JPanel implements TabConfiguration {

    TerminalConfigurator parent;

    public Png(TerminalConfigurator instance) {
        parent = instance;
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jL_pngWdth = new javax.swing.JLabel();
        jCB_pngInterlace = new javax.swing.JCheckBox();
        jCoB_pngUnits = new javax.swing.JComboBox();
        jL_pngUnit = new javax.swing.JLabel();
        jTB_pngLockProportion = new javax.swing.JToggleButton();
        jSpinner_pngWidth = new javax.swing.JSpinner();
        jCB_pngTransparency = new javax.swing.JCheckBox();
        jL_pngHeight = new javax.swing.JLabel();
        jCB_pngTrueColor = new javax.swing.JCheckBox();
        jSpinner_pngHeight = new javax.swing.JSpinner();

        jL_pngWdth.setText("Width");

        jCB_pngInterlace.setText("Use interlace");

        jCoB_pngUnits.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "px" }));

        jL_pngUnit.setText("unit");

        jTB_pngLockProportion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/unlock-icon.png"))); // NOI18N
        jTB_pngLockProportion.setRolloverEnabled(true);
        jTB_pngLockProportion.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/unlock-icon.png"))); // NOI18N
        jTB_pngLockProportion.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/analyzer/gui/images/lock-icon.png"))); // NOI18N
        jTB_pngLockProportion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jTB_pngLockProportionItemStateChanged(evt);
            }
        });

        jSpinner_pngWidth.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jSpinner_pngWidth.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(640.0d), Double.valueOf(10.0d), null, Double.valueOf(1.0d)));
        jSpinner_pngWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner_pngWidthStateChanged(evt);
            }
        });

        jCB_pngTransparency.setText("Use transparency");

        jL_pngHeight.setText("Height");

        jCB_pngTrueColor.setText("Use trueColor");

        jSpinner_pngHeight.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jSpinner_pngHeight.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(400.0d), Double.valueOf(10.0d), null, Double.valueOf(1.0d)));
        jSpinner_pngHeight.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner_pngHeightStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCB_pngInterlace)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCB_pngTransparency)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCB_pngTrueColor))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jL_pngWdth)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner_pngWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jL_pngHeight)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner_pngHeight, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jTB_pngLockProportion, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jL_pngUnit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCoB_pngUnits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(8, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCB_pngTransparency)
                    .addComponent(jCB_pngTrueColor)
                    .addComponent(jCB_pngInterlace))
                .addGap(41, 41, 41)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jL_pngWdth)
                    .addComponent(jSpinner_pngWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jL_pngHeight)
                    .addComponent(jSpinner_pngHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTB_pngLockProportion, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jL_pngUnit)
                    .addComponent(jCoB_pngUnits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTB_pngLockProportionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jTB_pngLockProportionItemStateChanged
        if (jTB_pngLockProportion.isSelected()) {
            parent.getTerminalConfiguration(TerminalTypes.PNG).setProportion(new Double(jSpinner_pngWidth.getValue().toString()), new Double(jSpinner_pngHeight.getValue().toString()));
        }
    }//GEN-LAST:event_jTB_pngLockProportionItemStateChanged

    private void jSpinner_pngWidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner_pngWidthStateChanged
        if (jTB_pngLockProportion.isSelected()) {
            jSpinner_pngHeight.setValue(new Double(jSpinner_pngWidth.getValue().toString()) / parent.getTerminalConfiguration(TerminalTypes.PNG).getProportion());
        }
    }//GEN-LAST:event_jSpinner_pngWidthStateChanged

    private void jSpinner_pngHeightStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner_pngHeightStateChanged
        if (jTB_pngLockProportion.isSelected()) {
            jSpinner_pngWidth.setValue(new Double(jSpinner_pngHeight.getValue().toString()) * parent.getTerminalConfiguration(TerminalTypes.PNG).getProportion());
        }
    }//GEN-LAST:event_jSpinner_pngHeightStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCB_pngInterlace;
    private javax.swing.JCheckBox jCB_pngTransparency;
    private javax.swing.JCheckBox jCB_pngTrueColor;
    private javax.swing.JComboBox jCoB_pngUnits;
    private javax.swing.JLabel jL_pngHeight;
    private javax.swing.JLabel jL_pngUnit;
    private javax.swing.JLabel jL_pngWdth;
    private javax.swing.JSpinner jSpinner_pngHeight;
    private javax.swing.JSpinner jSpinner_pngWidth;
    private javax.swing.JToggleButton jTB_pngLockProportion;
    // End of variables declaration//GEN-END:variables

    @Override
    public void configureTerminal() {
        PngConfiguration pngConfiguration = (PngConfiguration) parent.getTerminalConfiguration(TerminalTypes.PNG);
        //Configure size
        pngConfiguration.setSize(Utils.truncDouble(new Double(jSpinner_pngWidth.getValue().toString()),2), Utils.truncDouble(new Double(jSpinner_pngHeight.getValue().toString()),2));
        //Configure unit
        pngConfiguration.setUnit(MeasureUnit.convert(jCoB_pngUnits.getSelectedItem().toString()));
        //Configure image option
        pngConfiguration.setUseInterlace(jCB_pngInterlace.isSelected());
        pngConfiguration.setUseTransparency(jCB_pngTransparency.isSelected());
        pngConfiguration.setUseTrueColor(jCB_pngTrueColor.isSelected());
    }
}