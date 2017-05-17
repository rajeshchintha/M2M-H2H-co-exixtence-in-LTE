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

import analyzer.entity.*;
import analyzer.gui.Main;
import analyzer.recoverable_exceptions.RecoverableException;
import analyzer.unrecoverable_exceptions.InvalidTraceLineException;
import analyzer.unrecoverable_exceptions.UnrecoverableException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Observable;

/**
 * This class is responsible to analyze the trace file. It iterates on all lines
 * of the trace file, interpreting it and executing the appropriate operations
 * to get the metrics calculated.
 *
 * @since 1.0.0
 */
public strictfp class Analyzer extends Observable implements Runnable {

    private boolean breakAnalyzer = false;
    /**
     * Singleton object
     */
    private static Analyzer instance = null;

    /**
     * Singleton method
     *
     * @return the Analyzer object
     */
    public static Analyzer getInstance() {
        if (instance == null) {
            instance = new Analyzer();
        }
        return instance;
    }
    //<editor-fold defaultstate="collapsed" desc="Variables">
    /**
     * Modifier of trace analyzis. If the trace contains only one node on the
     * network, this variable is set to 'false'. If the trace contains all the
     * nodes on the network, this variable is set to 'true'.
     */
    public static boolean oneNodeTrace = false;
    /**
     * This variable indicates if a protocol without the 'length' field was
     * found.
     */
    public static boolean hasNoLengthProtErr;
    /**
     * This variable is set to 'true' if the analysis must stop in case of a
     * protocol without the 'length' field is found and it's 'false' if this
     * issue should not stop de analysis.
     */
    private static boolean stopBecauseOfNoLengthProto = true;
    /**
     * This variable is set to 'true' if any of the TraceMetrics' custom
     * expceptions happen and it's desired to show the expception's message and
     * it's set to 'false' not to show the exceptions messages. Used for
     * debugging.
     */
    private static boolean showExceptionMessages = true;
    /**
     * Holds the current trace line index.
     */
    public static long lineIndex;
    /**
     * Object to check protocol lengths
     */
    public static Headers noLengthHeaders = new Headers();
    /**
     * Object to hold the UnrecoverableException that happened during the
     * analysis.
     */
    public static UnrecoverableException unrecoverableException;
    /**
     * List to hold the various RecoverableException that may happen during the
     * analysis.
     */
    public static ArrayList<RecoverableException> recoverableExceptions;
    /**
     * Indicates if the trace file is open.
     */
    public boolean fileIsOpen;
    /**
     * Indicates if the analysis is over.
     */
    public boolean finished;
    /**
     * Indicates if the current dropped packet belongs to an existing drop
     * sequence or if it belongs to a new drop sequence. Used in Stream
     * analysis.
     */
    public boolean newDrop;
    /**
     * Indicates if there is an unfinished receivement task on Streams.
     */
    private boolean remainingRecStreamTasks;
    /**
     * Indicates if there is an unfinished drop task on Streams.
     */
    private boolean remainingDropStreamTask;
    /**
     * Holds the type of the remaining task. True is for TcpStream and False if
     * for UdpStream.
     */
    private boolean typeOfRemainingTask;
    /**
     * Indicates if the analysis is being done.
     */
    private boolean analysing;
    /**
     * Counter of the number of received packets during the simulation.
     */
    private int nRecPacks;
    /**
     * Counter of the number of enqueued packets during the simulation.
     */
    private int nEnqPacks;
    /**
     * Counter of the number of dequeued/sent packets during the simulation.
     */
    private int nDeqPacks;
    /**
     * Counter of the number of dropped packets during the simulation.
     */
    private int nDropPacks;
    /**
     * Counter of the number of wifi transmitted packets during the simulation.
     */
    private int nTransmittedPacks;
    /**
     * Holds the index of the last device.
     */
    private int lastDevice;
    /**
     * Holds the size in bytes of the trace file; Used by the ProgressBar class.
     */
    public long tamFile;
    /**
     * Holds the size already analyzed.
     */
    private long sizeAnalyzed;
    /**
     * Holds the last time read from the trace file.
     */
    private double lastTime;
    /**
     * Current trace time.
     */
    private double currentTime;
    /**
     * Holds the name of the trace file.
     */
    private String fileName;
    /**
     * Holds the signature of the last analyzed line.
     */
    private String lastSignature;
    /**
     * Holds the current trace line after split() method.
     */
    private String[] splittedLine;
    /**
     * Trace file object
     */
    private MyFile traceFile;
    /**
     * Holds the object to the LineParser.
     */
    private LineParser parser;
    /**
     * Holds the last used TcpStream object.
     */
    private TcpStream lastTcpStream;
    /**
     * Holds the last used TcpSegment object.
     */
    private TcpSegment lastTcpStreamNode;
    /**
     * Holds the last used UdpStream object.
     */
    private UdpStream lastUdpStream;
    /**
     * Holds the last node used.
     */
    private Node lastNode;
    /**
     * Objects used to calculate time.
     */
    public Calendar start, end;
    /**
     * HashMap of Node objects. (traceNumber, respectiveNodeObject)
     */
    private HashMap<Integer, Node> nodes;
    /**
     * HashMap of TcpStream objects. (HashStreamNode, respectiveTcpStream)
     */
    private HashMap<HashStreamNode, TcpStream> tcpStreams;
    /**
     * HashMap of UdpStream objects. (HashStreamNode, respectiveUdpStream)
     */
    private HashMap<HashStreamNode, UdpStream> udpStreams;
    /**
     * Stores the Protocol's names returned by the parser of the current packet
     * being analysed.
     */
    private ArrayList<String> headers;
    /**
     * Stores the Protocol's content returned by the parser of the current
     * packet being analysed.
     */
    private ArrayList<String> headersContent;
    //</editor-fold>

    /**
     * Constructor of the Analyzer Class.
     */
    private Analyzer() {

        //HashMap of Node objects. (traceNumber, respectiveNodeObject)
        nodes = new HashMap<Integer, Node>();
        //HashMap of TcpStream objects. (HashStreamNode, respectiveTcpStream)
        tcpStreams = new HashMap<HashStreamNode, TcpStream>();
        //HashMap of UdpStream objects. (HashStreamNode, respectiveUdpStream)
        udpStreams = new HashMap<HashStreamNode, UdpStream>();
        //Holds the last time read from the trace file.
        lastTime = -1.0;
        remainingRecStreamTasks = false;
        remainingDropStreamTask = false;

        recoverableExceptions = new ArrayList<RecoverableException>();
        parser = new LineParser(); //Passa a linha do trace e retorna informações da linha

    }

    /**
     *
     * @param traceFile the relative or absolute path to trace file.
     */
    public void openTrace(String traceFile) {

        this.traceFile = new MyFile();
        fileName = traceFile;

        if (this.traceFile.openFile2Read(traceFile)) {

            tamFile = this.traceFile.getFileLength();
            fileIsOpen = true;

        }
    }

    /**
     * This method contains the main instructions to do the trace analysis.
     */
    @Override
    public void run() {

        Main.getInstance().getjTP_Principal().setEnabled(false);
        try {
            PBTimeUpdate pbtTimeUpdate = new PBTimeUpdate();
            start = Calendar.getInstance();

            init();

            String fileLine;
            char currentOperation;
            int currentNodeNumber;
            int currentDevice;
            int lineLength;
            Node currentNode;
            ArrayList<String>[] parseHeaderReturn;

            if (fileIsOpen) {

                pbtTimeUpdate.start();

                while ((fileLine = traceFile.readLine()) != null && !breakAnalyzer) {

                    lineIndex++;
                    /*
                     * notify the observers that the size changed
                     */
                    lineLength = fileLine.length();
                    setChanged();
                    notifyObservers(lineLength);

                    sizeAnalyzed += lineLength;

                    //This can generate a exception
                    parser.validateLine(fileLine);

                    splittedLine = fileLine.split(" ");

                    currentOperation = parser.getOperation(splittedLine);
                    currentTime = parser.getTime(splittedLine);
                    currentNodeNumber = parser.getNodeNumber(splittedLine);
                    currentDevice = parser.getDevice(splittedLine);

                    // temporary fix for wimax awkward line
                    //t|r <time> to|from: <mac address> /NodeList/<num>/DeviceList/<num>/$ns3::WimaxNetDevice/Tx
                    if (!fileLine.contains("from:") && !fileLine.contains("to:")) {

                        parseHeaderReturn = parser.separateHeaders(fileLine);
                        headers = parseHeaderReturn[0];
                        headersContent = parseHeaderReturn[1];
                        currentNode = getNode(currentNodeNumber);

                        executeOperations(currentNode, currentDevice, currentTime, currentOperation);
                        lastTime = currentTime;

                    } else {

                        if (currentOperation == 't') {
                            getNode(currentNodeNumber).incWiredSentPackCount();
                            getNode(currentNodeNumber).incWiredSentLength(6);
                        } else if (currentOperation == 'r') {
                            getNode(currentNodeNumber).incRecPackCount();
                            getNode(currentNodeNumber).incRecLength(6);
                        }

                        remainingDropStreamTask = false;
                        remainingRecStreamTasks = false;

                    }

                } //While end

                if (tcpStreams != null) {
                    int cont = 0;
                    System.out.println("TCP size: " + tcpStreams.size());
                    for (TcpStream ts : tcpStreams.values()) {
                        System.out.println("Stream " + ++cont);
                        System.out.println(ts.sendTime);
                    }
                }
                finalizeAnalyzis(currentTime);

                end = Calendar.getInstance();

                finished = true;
                setAnalysing(false);
                setChanged();

                notifyObservers(finished);
                pbtTimeUpdate.stopThread();
                pbtTimeUpdate = null;

            }

        } catch (InvalidTraceLineException itle) {
            javax.swing.JOptionPane.showMessageDialog(null, itle.getMessage().replace("ns3::", "\n"));
            System.exit(1);
        } finally {
            Main.getInstance().getjTP_Principal().setEnabled(true);
        }

    } //End of method run

    /**
     * Method to finalize the E[N] and E[W] measures with the last trace time.
     *
     * @param time last time read from trace
     */
    public void finalizeAnalyzis(double time) {

        ArrayList<Node> listNodes = new ArrayList<Node>(HashNodes().values());
        for (int j = 0; j < listNodes.size(); j++) {
            listNodes.get(j).finishAndCalculate(time);

        }

        ArrayList<TcpStream> tcpStreamArray = new ArrayList<TcpStream>(tcpStreams.values());

        for (int i = 0; i < tcpStreamArray.size(); i++) {

            tcpStreamArray.get(i).finish();
            tcpStreamArray.get(i).calculatePdv();
            tcpStreamArray.get(i).calculateIpdv();

        }

    }

    private void init() {

        lastDevice = 0;
        oneNodeTrace = false;
        hasNoLengthProtErr = false;
        unrecoverableException = null;
        recoverableExceptions = new ArrayList<RecoverableException>();
        lineIndex = 0;
        splittedLine = null;
        finished = false;
        newDrop = true;
        tamFile = 0;
        nRecPacks = 0;
        nEnqPacks = 0;
        nDeqPacks = 0;
        nDropPacks = 0;
        nTransmittedPacks = 0;
        nodes = new HashMap<Integer, Node>();
        tcpStreams = new HashMap<HashStreamNode, TcpStream>();
        udpStreams = new HashMap<HashStreamNode, UdpStream>();
        headers = new ArrayList<String>();
        headersContent = new ArrayList<String>();
        lastTcpStream = null;
        lastTcpStreamNode = null;
        lastUdpStream = null;
        lastTime = 0.0;
        remainingRecStreamTasks = false;
        remainingDropStreamTask = false;
        lastSignature = null;
        lastNode = null;
        sizeAnalyzed = 0;
        breakAnalyzer = false;

    }

    private void doRemainingDropStreamTask(Node lastNode, Node currentNode, int device, double time, char op, boolean b) {

        String signature = parser.getPacketSignature(headers, headersContent);

        if (!signature.equals(lastSignature)) {

            lastTcpStream.setDroppedPacket(time, lastTcpStreamNode);

        }
        remainingDropStreamTask = false;

    }

    private void doRemainingRecStreamTasks(Node lastNode, Node currentNode, int device, double time, char op, boolean finish) {

        String signature = parser.getPacketSignature(headers, headersContent);

        if (!signature.equals(lastSignature)) {

            lastTcpStream.setReceivedTime(lastTime, lastTcpStreamNode);


        }

        remainingRecStreamTasks = false;

    }

    /**
     * Executes the calculations for each type of event found in the trace file.
     *
     * @param currentNode the current node object
     * @param time trace time of the current event
     * @param op event of the current trace line
     */
    private void executeOperations(Node currentNode, int device, double time, char op) {

        int payload;
        int length;

        if (remainingDropStreamTask) {
            doRemainingDropStreamTask(lastNode, currentNode, device, time, op, false);
        }

        if (remainingRecStreamTasks) {
            doRemainingRecStreamTasks(lastNode, currentNode, device, time, op, false);
        }

        switch (op) {
            case '+':

                nEnqPacks++;
                currentNode.gotPackEN(time);
                currentNode.gotPackEWAlfa(time);
                break;

            case '-':

                nDeqPacks++;
                currentNode.incWiredSentPackCount();
                currentNode.sentPackEN(time);
                currentNode.gotPackEWBeta(time);

                length = parser.getPackLength(headers, headersContent);
                currentNode.incWiredSentLength(length);
                payload = parser.getPayload(headers, headersContent);
                currentNode.incWiredSentPayloadSum(payload);

                if (!oneNodeTrace && headers.indexOf("TcpHeader") != -1) {

                    TcpStream ts;

                    long seqNumber;
                    long ack;

                    String signature;
                    String[] flags;
                    String[] ips = parser.getIps(headers, headersContent);
                    int[] ports = parser.getStreamPorts(headers, headersContent);

                    HashStreamNode hsn = new HashStreamNode();
                    hsn.setIps(ips);
                    hsn.setPorts(ports);

                    seqNumber = parser.getPackSeqNumber(headers, headersContent);
                    flags = parser.getFlags(headers, headersContent);
                    ack = parser.getPackAckNumber(headers, headersContent);
                    signature = parser.getPacketSignature(headers, headersContent);

                    ts = getTcpStream(hsn, currentNode.getNum());

                    if (ts.getSenderNumber() == currentNode.getNum()) {

                        ts.setSendTime(time, seqNumber, flags, payload, ack, signature);

                    }

                }

                if (!oneNodeTrace && headers.indexOf("UdpHeader") != -1) {

                    UdpStream us;

                    String[] ips = parser.getIps(headers, headersContent);
                    int[] ports = parser.getStreamPorts(headers, headersContent);

                    HashStreamNode hsn = new HashStreamNode();
                    hsn.setIps(ips);
                    hsn.setPorts(ports);
                    us = getUdpStream(hsn);
                    us.setSent();

                }
                break;

            case 'd':

                nDropPacks++;
                currentNode.incDroppedPackCount();
                currentNode.incDroppedLength(parser.getPackLength(headers, headersContent));

                if (!oneNodeTrace && headers.indexOf("TcpHeader") != -1) {

                    TcpStream ts;

                    long seqNumber;
                    long ack;


                    String signature;
                    String[] flags;
                    String[] ips = parser.getIps(headers, headersContent);

                    int[] ports = parser.getStreamPorts(headers, headersContent);

                    HashStreamNode hsn = new HashStreamNode();
                    hsn.setIps(ips);
                    hsn.setPorts(ports);

                    seqNumber = parser.getPackSeqNumber(headers, headersContent);
                    flags = parser.getFlags(headers, headersContent);
                    ack = parser.getPackAckNumber(headers, headersContent);
                    payload = parser.getPayload(headers, headersContent);
                    signature = parser.getPacketSignature(headers, headersContent);

                    lastTcpStreamNode = new TcpSegment(seqNumber, time, flags, payload, ack, signature);

                    ts = getTcpStream(hsn, currentNode.getNum());
                    lastTcpStream = ts;
                    lastNode = currentNode;
                    remainingDropStreamTask = true;

                } else if (!oneNodeTrace && headers.indexOf("UdpHeader") != -1) {

                    String[] ips = parser.getIps(headers, headersContent);
                    int[] ports = parser.getStreamPorts(headers, headersContent);

                    HashStreamNode hsn = new HashStreamNode();
                    hsn.setIps(ips);
                    hsn.setPorts(ports);

                    getUdpStream(hsn).setDrop();
                    getUdpStream(hsn).setNewDrop(false);

                }

                newDrop = false;
                break;

            case 'r':
                nRecPacks++;
                currentNode.incRecPackCount();
                length = parser.getPackLength(headers, headersContent);
                currentNode.incRecLength(length);
                payload = parser.getPayload(headers, headersContent);


                if (!oneNodeTrace && headers.indexOf("TcpHeader") != -1) {

                    long seqNumber;
                    long ack;

                    String signature;
                    String[] flags;
                    String[] ips = parser.getIps(headers, headersContent);
                    int[] ports = parser.getStreamPorts(headers, headersContent);

                    HashStreamNode hsn = new HashStreamNode();
                    hsn.setIps(ips);
                    hsn.setPorts(ports);

                    seqNumber = parser.getPackSeqNumber(headers, headersContent);
                    flags = parser.getFlags(headers, headersContent);
                    ack = parser.getPackAckNumber(headers, headersContent);
                    signature = parser.getPacketSignature(headers, headersContent);
                    lastSignature = signature;

                    lastTcpStream = getTcpStream(hsn, currentNode.getNum());
                    lastTcpStreamNode = new TcpSegment(seqNumber, time, flags, payload, ack, signature);

                    remainingRecStreamTasks = true;
                    lastNode = currentNode;
                    lastDevice = device;
                    typeOfRemainingTask = true;

                } else if (!oneNodeTrace && headers.indexOf("UdpHeader") != -1) {


                    String[] ips = parser.getIps(headers, headersContent);
                    int[] ports = parser.getStreamPorts(headers, headersContent);
                    HashStreamNode hsn = new HashStreamNode();
                    hsn.setIps(ips);
                    hsn.setPorts(ports);

                    UdpStream us = getUdpStream(hsn);
                    us.setNewDrop(true);
                    us.setReceived();

                }
                break;

            case 't':

                nTransmittedPacks++;

                length = parser.getPackLength(headers, headersContent);
                currentNode.incWiredSentLength(length);
                payload = parser.getPayload(headers, headersContent);
                currentNode.incWiredSentPayloadSum(payload);
                currentNode.incWiredSentPackCount();

                if (!oneNodeTrace && headers.indexOf("TcpHeader") != -1) {

                    TcpStream ts;

                    long seqNumber;
                    long ack;

                    String signature;
                    String[] flags;
                    String[] ips = parser.getIps(headers, headersContent);
                    int[] ports = parser.getStreamPorts(headers, headersContent);

                    HashStreamNode hsn = new HashStreamNode();
                    hsn.setIps(ips);
                    hsn.setPorts(ports);

                    seqNumber = parser.getPackSeqNumber(headers, headersContent);
                    flags = parser.getFlags(headers, headersContent);
                    ack = parser.getPackAckNumber(headers, headersContent);
                    signature = parser.getPacketSignature(headers, headersContent);

                    ts = getTcpStream(hsn, currentNode.getNum());

                    if (ts.getSenderNumber() == currentNode.getNum()) {

                        ts.setSendTime(time, seqNumber, flags, payload, ack, signature);

                    }

                }

                if (!oneNodeTrace && headers.indexOf("UdpHeader") != -1) {

                    UdpStream us;

                    String[] ips = parser.getIps(headers, headersContent);
                    int[] ports = parser.getStreamPorts(headers, headersContent);

                    HashStreamNode hsn = new HashStreamNode();
                    hsn.setIps(ips);
                    hsn.setPorts(ports);
                    us = getUdpStream(hsn);
                    us.setSent();

                }
                break;
        }

    }

    /**
     * @return the stopBecauseOfNoLengthProto
     */
    public static boolean isStopBecauseOfNoLengthProto() {
        return stopBecauseOfNoLengthProto;
    }

    /**
     * @return the showExceptionMessages
     */
    public static boolean isShowExceptionMessages() {
        return showExceptionMessages;
    }

    /**
     * @return the nodes
     */
    public HashMap<Integer, Node> HashNodes() {
        return nodes;
    }

    /**
     * @return the analysing
     */
    public synchronized boolean isAnalysing() {
        return analysing;
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public boolean isInterrupted() {
        return breakAnalyzer;
    }

    public void setBreakAnalyzer(boolean breakAnalyzer) {
        this.breakAnalyzer = breakAnalyzer;
    }

    /**
     * Checks if a node is already on the node simulation's list.
     *
     * @param num trace node number
     * @return the given Node
     */
    public Node getNode(int num) {

        Node result;

        result = HashNodes().get(num);

        if (result == null) {

            result = new Node();
            result.setNum(num);
            HashNodes().put(num, result);

        }

        return result;
    }

    /**
     *
     * @param hashStreamNode details of the current stream
     * @param srcNum number of sender node
     * @return respective TcpStream object
     */
    public TcpStream getTcpStream(HashStreamNode hashStreamNode, int srcNum) {

        TcpStream result;

        result = tcpStreams.get(hashStreamNode);

        if (result == null) {

            result = new TcpStream(hashStreamNode.getIpSrc(), hashStreamNode.getIpDest(), srcNum,
                    hashStreamNode.getPortSrc(), hashStreamNode.getPortDest());
            tcpStreams.put(hashStreamNode, result);

        }

        return result;
    }

    /**
     *
     * @param hashStreamNode details of the current stream
     * @return respective UdpStream object
     */
    public UdpStream getUdpStream(HashStreamNode hashStreamNode) {

        UdpStream result;

        result = udpStreams.get(hashStreamNode);

        if (result == null) {

            result = new UdpStream(hashStreamNode.getIpSrc(), hashStreamNode.getIpDest(),
                    hashStreamNode.getPortSrc(), hashStreamNode.getPortDest());
            udpStreams.put(hashStreamNode, result);

        }

        return result;
    }

    /**
     * Returns the Throughput of the given node number.
     *
     * @param num trace index of the node
     * @return the respective value of throughput
     */
    public double getThroughtputOf(int num) {

        return (nodes.get(num).getThroughput());

    }

    /**
     * Returns the Goodput of the given node number.
     *
     * @param num trace index of the node
     * @return the respective value of goodput
     */
    public double getGoodputOf(int num) {

        return (nodes.get(num).getGoodput());

    }

    /**
     * Returns the Average number of packet (aka E[N]) of the given node number.
     *
     * @param num trace index of the node
     * @return the respective value of average number of packets
     */
    public double getAverageNumberOfPacketsOf(int num) {

        return (nodes.get(num).getEnResult());

    }

    /**
     * Returns the Average packet delay (aka E[W]) of the given node number.
     *
     * @param num trace index of the node
     * @return the respective value of average packet delay
     */
    public double getAveragePacketDelayOf(int num) {

        return (nodes.get(num).getEwResult());

    }

    /**
     * Returns the Arrival rate (aka lambda) of the given node number.
     *
     * @param num trace index of the node
     * @return the respective value of lambda
     */
    public double getArrivalRateOf(int num) {

        return (nodes.get(num).getArrivalRate());

    }

    /**
     * Returns the Little's Result ( E[N] = E[W] * lambda) of the given node
     * number.
     *
     * @param num trace index of the node
     * @return the respective value of Little's result e.g.E[N]_value =
     * E[W]_value * lambda_value
     */
    public String getLittleResultOf(int num) {

        String result;
        result = getAverageNumberOfPacketsOf(num) + " = " + (getAveragePacketDelayOf(num) * getArrivalRateOf(num));
        return result;

    }

    /**
     * @param i index of the node's list
     * @return respective Node object
     */
    public Node getNodes(int i) {
        return HashNodes().get(i);
    }

    /**
     * @return the size of the Node's list
     */
    public int getNumberOfNodes() {
        return HashNodes().size();
    }

    /**
     * @return size of the TcpStream's list
     */
    public int getTamArrayStreams() {

        return tcpStreams.size();

    }

    /**
     * @return the sizeAnalyzed
     */
    public long getSizeAnalyzed() {
        return sizeAnalyzed;
    }

    /**
     * @param index of a TcpStream
     * @return respective TcpStream object
     */
    public TcpStream getStream(int index) {
        ArrayList<TcpStream> tcpStreamArray = new ArrayList<TcpStream>(tcpStreams.values());
        return tcpStreamArray.get(index);
    }

    /**
     * @return the size of the trace file in bytes
     */
    public long getFileLength() {
        return traceFile.getFileLength();
    }

    /**
     * Return the length of the current line being analysed. Used to update the
     * Progress Bar.
     *
     * @param fileLine the current trace line
     * @return the length of the given trace line
     */
    private static int getLineLength(String fileLine) {
        if (fileLine != null) {

            return fileLine.length();

        } else {
            return 0;
        }
    }

    /**
     *
     * @return the simulation info.
     */
    public String getSimulationInfo() {
        StringBuilder result = new StringBuilder();
        result.append("File: \t\t").append(fileName);
        result.append("\nLines on file: \t\t").append(lineIndex);
        result.append("\nTotal enqueued packets: \t").append(nEnqPacks);
        result.append("\nTotal sent packets: \t").append(nDeqPacks + nTransmittedPacks);
        result.append("\nTotal received packets: \t").append(nRecPacks);
        result.append("\nTotal dropped packets: \t").append(nDropPacks);
        result.append("\nTotal simulation time: \t").append(currentTime).append(" seconds");
        result.append("\nTime of analisys: \t").append(Utils.convertMilisToTime(end.getTimeInMillis() - start.getTimeInMillis(), false));
        return new String(result);

    }

    /**
     * @return the tcpStreams.
     */
    public HashMap<HashStreamNode, TcpStream> getTcpStreams() {
        return tcpStreams;
    }

    /**
     * @return the udpStreams.
     */
    public HashMap<HashStreamNode, UdpStream> getUdpStreams() {
        return udpStreams;
    }

    /**
     * @param analysing the analysing to set
     */
    public synchronized void setAnalysing(boolean analysing) {
        this.analysing = analysing;
    }

    /**
     * @param aStopBecauseOfNoLengthProto the stopBecauseOfNoLengthProto to set
     */
    public static void setStopBecauseOfNoLengthProto(boolean aStopBecauseOfNoLengthProto) {
        stopBecauseOfNoLengthProto = aStopBecauseOfNoLengthProto;
    }

    /**
     * @param aShowExceptionMessages the showExceptionMessages to set
     */
    public static void setShowExceptionMessages(boolean aShowExceptionMessages) {
        showExceptionMessages = aShowExceptionMessages;
    }
    //</editor-fold>
}
