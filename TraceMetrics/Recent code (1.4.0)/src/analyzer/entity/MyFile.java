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

package analyzer.entity;

import java.io.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * Abstraction of file operations to make the code a bit cleaner.
 * 
 * @since 1.0.0
 */

public class MyFile {

    /**
     * Object to read the file
     */
    private Scanner input;
    /**
     * The file object
     */
    private File f;
    /**
     * Object to write on the file
     */
    private BufferedWriter out;
    
    /**
     * Open the file to read only.
     * @param name relative or complete path to the file plus the file name
     * @return if the file was openned successfully
     */
    public boolean openFile2Read(String name){
        boolean err = true;
        try {
            input = new Scanner(f = new File(name));
        } catch (FileNotFoundException ex) {
            System.err.println("File doesn't exist!");
            err = false;
        }
        return err;
    }

    /**
     * Open the file to write only.
     * @param name relative or complete path to the file plus the file name
     * @return if the file was openned successfully
     */
    public boolean openFile2Write(String name){

        boolean err = false;
        try {
          out = new BufferedWriter(new FileWriter(name, false));
        } catch (IOException ex) {
            Logger.getLogger(MyFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return !err;
    }

    /**
     * 
     * @param str String to be written on the file
     */
    public void writeLine(String str){

        try {

            out.write(str);
        
        } catch (IOException ex) {
            Logger.getLogger(MyFile.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * 
     * @return the read line. 'null' if there is no lines left to read.
     */
    public String readLine(){
        if(input.hasNext())
            return input.nextLine();
        return null;
    }
    
    /**
     * Closes the open files.
     */
    public void closeFile(){
        if(input!=null)
            input.close();
        if(out!=null)
            try {
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(MyFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @return the size of the file in bytes.
     */
    public long getFileLength(){

        return f.length();

    }

}