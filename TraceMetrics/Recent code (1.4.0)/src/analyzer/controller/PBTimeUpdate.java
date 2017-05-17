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
package analyzer.controller;

import analyzer.gui.ProgressBar;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 
 * This class estimates the remaining time to finish the analysis. The
 * calculation is done every 1 second and it's displayed on the Progress Bar.
 * 
 * @since 1.0.0
 */
public class PBTimeUpdate extends Thread {

    private boolean stop = false;
    private final int DELAY = 1000; // 1 second

    public void stopThread(){
        stop = true;
    }

    @Override
    public void run() {
        double totalLenght = Analyzer.getInstance().getFileLength();
        double partialLenght;
        double partialTime;

        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException ex) {
            Logger.getLogger(PBTimeUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (!stop) {

            partialTime = Calendar.getInstance().getTimeInMillis() - Analyzer.getInstance().start.getTimeInMillis();
            partialLenght = Analyzer.getInstance().getSizeAnalyzed();

            ProgressBar.setEstimatedTime(Utils.convertMilisToTime(new Double(partialTime).longValue(), true) + " / "
                    +Utils.convertMilisToTime(new Double(((partialTime * totalLenght) / partialLenght) - partialTime).longValue(), false));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PBTimeUpdate.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
}
