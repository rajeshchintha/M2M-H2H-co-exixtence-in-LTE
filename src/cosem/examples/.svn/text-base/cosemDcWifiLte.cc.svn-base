        /* -*-  Mode: C++; c-file-style: "gnu"; indent-tabs-mode:nil; -*- */
/*
 * Copyright (c) 2011 Centre Tecnologic de Telecomunicacions de Catalunya (CTTC)
 * Copyright (c) 2012 Uniandes 
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Author: Jaume Nin <jaume.nin@cttc.cat>
 *         Juanmalk <jm.aranda121@uniandes.edu.co> 
 */

#include "ns3/lte-helper.h"
#include "ns3/epc-helper.h"
#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/ipv4-global-routing-helper.h"
#include "ns3/internet-module.h"
#include "ns3/mobility-module.h"
#include "ns3/lte-module.h"
#include "ns3/wifi-module.h"
#include "ns3/csma-module.h"
#include "ns3/applications-module.h"
#include "ns3/point-to-point-helper.h"
#include "ns3/config-store.h"
#include "ns3/udp-cosem-client-helper.h"
#include "ns3/udp-cosem-server-helper.h"
#include "ns3/data-concentrator-helper.h"
#include "ns3/demand-response-application-helper.h"
#include "ns3/mdm-application-helper.h"
#include "ns3/flow-monitor-module.h"
#include <iostream>
#include <fstream>
#include <string> 

// Default Network Topology
//                                                                                    (Control Center)  
//      Wifi 10.1.3.0                                                                           DR
//                          AP(Wifi)                                                            |
//  *      *         *       *                                                          =========== LAN 10.1.2.0  
//  |      |         |       |    7.0.0.0                                   1.0.0.0      |   
// SM(1)  SM(0) ... SM(n)   DC ------------- eNB ================= PGW  ============== GW-MDM   
//                           |   LTE channel (1)     PC Network    (1)     Internet               
//  (AMI Network)            x                                                               
//                          UE(LTE)                                                       

using namespace ns3;

/**
 * Sample simulation script for LTE+EPC. It instantiates several eNodeB,
 * attaches one UE per eNodeB starts a flow for each UE to  and from a remote host.
 * It also  starts yet another flow between each UE pair.
 */

NS_LOG_COMPONENT_DEFINE ("CosemDCWifiLteExample");

int
main (int argc, char *argv[])
{

  bool verbose = true;  // For COSEM
  uint16_t nUeNodes = 1;  // UE LTE nodes
  uint16_t nBsNodes = 1;  // BS LTE nodes
  uint32_t nWifi = 150; // Smart Meters
  uint32_t nCcCsma = 1; // Control Center servers (DR)
  double simTime = 300.0;
  double distance = 60.0;
  double interPacketInterval = 100;

  // Command line arguments
  CommandLine cmd;
  cmd.AddValue("nWifi", "Number of Smart Meters", nWifi);
  cmd.AddValue("nUeNodes", "Number of eNodeBs + UE pairs", nUeNodes);
  cmd.AddValue("simTime", "Total duration of the simulation [s])", simTime);
  cmd.AddValue("distance", "Distance between eNBs [m]", distance);
  cmd.AddValue("interPacketInterval", "Inter packet interval [ms])", interPacketInterval);
  cmd.AddValue ("nAmiCsma", "Number of \"extra\" CSMA nodes/devices", nCcCsma);
  cmd.AddValue ("verbose", "Tell echo applications to log if true", verbose);

  cmd.Parse(argc, argv);

  // Wifi
  /*if (nWifi > 18)
    {
      std::cout << "Number of wifi nodes " << nWifi << 
                   " specified exceeds the mobility bounding box" << std::endl;
      exit (1);
    }*/

  // For COSEM
  if (verbose)
    {
      LogComponentEnable ("CosemApplicationLayerClient", LOG_LEVEL_INFO);
      LogComponentEnable ("CosemApplicationLayerServer", LOG_LEVEL_INFO);
      LogComponentEnable ("CosemApplicationsProcessClient", LOG_LEVEL_INFO);
      LogComponentEnable ("CosemApplicationsProcessServer", LOG_LEVEL_INFO);
      LogComponentEnable ("UdpCosemWrapperLayerClient", LOG_LEVEL_INFO);
      LogComponentEnable ("UdpCosemWrapperLayerServer", LOG_LEVEL_INFO);
      LogComponentEnable ("DataConcentratorApplication", LOG_LEVEL_INFO);
      LogComponentEnable ("DemandResponseApplication", LOG_LEVEL_INFO);
      LogComponentEnable ("MeterDataManagementApplication", LOG_LEVEL_INFO);
    }
  // 

  // CSMA  
  nCcCsma = nCcCsma == 0 ? 1 : nCcCsma;

// -------------------------------------------------------------------------------
//  LTE + EPC CONFIGURATION
// -------------------------------------------------------------------------------

  Ptr<LteHelper> lteHelper = CreateObject<LteHelper> ();
  Ptr<EpcHelper>  epcHelper = CreateObject<EpcHelper> ();
  lteHelper->SetEpcHelper (epcHelper);
  lteHelper->SetSchedulerType("ns3::PfFfMacScheduler");

  ConfigStore inputConfig;
  inputConfig.ConfigureDefaults();

  // parse again so you can override default values from the command line
  cmd.Parse(argc, argv);

  Ptr<Node> pgw = epcHelper->GetPgwNode ();

   // Create a single RemoteHost
  NodeContainer remoteHostContainer;
  remoteHostContainer.Create (1);
  Ptr<Node> remoteHost = remoteHostContainer.Get (0);
  InternetStackHelper internet;
  internet.Install (remoteHostContainer);

  // Create the Internet
  PointToPointHelper p2ph;
  p2ph.SetDeviceAttribute ("DataRate", DataRateValue (DataRate ("100Gb/s")));
  p2ph.SetDeviceAttribute ("Mtu", UintegerValue (1500));
  p2ph.SetChannelAttribute ("Delay", TimeValue (Seconds (0.010)));
  NetDeviceContainer internetDevices = p2ph.Install (pgw, remoteHost);
  Ipv4AddressHelper ipv4h;
  ipv4h.SetBase ("1.0.0.0", "255.0.0.0");
  Ipv4InterfaceContainer internetIpIfaces = ipv4h.Assign (internetDevices);
  // interface 0 is localhost, 1 is the p2p device
  // Ipv4Address remoteHostAddr = internetIpIfaces.GetAddress (1);
  
  // For COSEM
  //Ipv4InterfaceContainer controlCenterInterfaces;
  //controlCenterInterfaces.Add (ipv4h.Assign (internetDevices.Get (1)));
  //

  Ipv4StaticRoutingHelper ipv4RoutingHelper;
  Ptr<Ipv4StaticRouting> remoteHostStaticRouting = ipv4RoutingHelper.GetStaticRouting (remoteHost->GetObject<Ipv4> ());
  remoteHostStaticRouting->AddNetworkRouteTo (Ipv4Address ("7.0.0.0"), Ipv4Mask ("255.0.0.0"), 1);

  NodeContainer ueNodes;
  NodeContainer enbNodes;
  enbNodes.Create(nBsNodes);
  ueNodes.Create(nUeNodes);

  // Install Mobility Model
  Ptr<ListPositionAllocator> positionAlloc = CreateObject<ListPositionAllocator> ();
  for (uint16_t i = 0; i < nUeNodes; i++)
    {
      positionAlloc->Add (Vector(distance * i, 0, 0));
    }
  MobilityHelper mobility;
  mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
  mobility.SetPositionAllocator(positionAlloc);
  mobility.Install(enbNodes);
  mobility.Install(ueNodes);

  // Install LTE Devices to the nodes
  NetDeviceContainer enbLteDevs = lteHelper->InstallEnbDevice (enbNodes);
  NetDeviceContainer ueLteDevs = lteHelper->InstallUeDevice (ueNodes);

  // Attach one UE per eNodeB
  for (uint16_t i = 0; i < nUeNodes; i++)
      {
        lteHelper->Attach (ueLteDevs.Get(i), enbLteDevs.Get(i));
      }

  // Install the IP stack on the UEs
  internet.Install (ueNodes);
  Ipv4InterfaceContainer ueIpIface;
  ueIpIface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (ueLteDevs));

  // Assign IP address to UEs, and install applications
  for (uint32_t u = 0; u < ueNodes.GetN (); ++u)
    {
      Ptr<Node> ueNode = ueNodes.Get (u);
      // Set the default gateway for the UE
      Ptr<Ipv4StaticRouting> ueStaticRouting = ipv4RoutingHelper.GetStaticRouting (ueNode->GetObject<Ipv4> ());
      ueStaticRouting->SetDefaultRoute (epcHelper->GetUeDefaultGatewayAddress (), 1);
      //ueStaticRouting->AddNetworkRouteTo (Ipv4Address ("10.1.2.0"), Ipv4Mask ("255.255.255.0"), 1);   
    }
  lteHelper->ActivateEpsBearer (ueLteDevs, EpsBearer (EpsBearer::NGBR_VIDEO_TCP_DEFAULT), EpcTft::Default ());

  lteHelper->EnableTraces ();
  // Uncomment to enable PCAP tracing
  //p2ph.EnablePcapAll("lena-epc-first");

// -------------------------------------------------------------------------------
//  CSMA CONTROL CENTER NETWORK CONFIGURATION
// -------------------------------------------------------------------------------

  // CC Nodes creation
  NodeContainer CcCsmaNodes; 
  CcCsmaNodes.Add (remoteHost); // Add the remote Host (GW) node created to the CSMA network
  CcCsmaNodes.Create (nCcCsma);

  // Control Center Links configuration & NetDevice instalation
  CsmaHelper CcCsma;
  CcCsma.SetChannelAttribute ("DataRate", StringValue ("100Mbps"));
  CcCsma.SetChannelAttribute ("Delay", TimeValue (NanoSeconds (6560)));

  NetDeviceContainer CcCsmaDevices;
  CcCsmaDevices = CcCsma.Install (CcCsmaNodes);
  
  // Internet Stack instalation (without the remoteHost)
  for (uint32_t i = 1; i < CcCsmaNodes.GetN (); ++i)
     {
        internet.Install (CcCsmaNodes.Get (i));
     }

  // Assign Ip Address
  Ipv4AddressHelper addressCC;
  addressCC.SetBase ("10.1.2.0", "255.255.255.0");
  Ipv4InterfaceContainer CcCsmaInterfaces = addressCC.Assign (CcCsmaDevices);

  // Set the default gateway (Router) for CC server
  //for (uint32_t i = 1; i < CcCsmaNodes.GetN (); ++i)
    // {
       Ptr<Ipv4StaticRouting> ccStaticRouting = ipv4RoutingHelper.GetStaticRouting (CcCsmaNodes.Get (1)->GetObject<Ipv4> ());
       ccStaticRouting->SetDefaultRoute (CcCsmaInterfaces.GetAddress (0), 1);
       //ccStaticRouting->AddNetworkRouteTo (Ipv4Address ("10.1.2.0"), Ipv4Mask ("255.255.255.0"), Ipv4Address ("1.0.0.0"), 1);   
   //  }

// -------------------------------------------------------------------------------
//  WIFI AMI NETWORK CONFIGURATION
// -------------------------------------------------------------------------------
  NodeContainer wifiStaNodes;
  wifiStaNodes.Create (nWifi);
  NodeContainer wifiApNode = ueNodes.Get (0); // UE Node

  YansWifiChannelHelper channel = YansWifiChannelHelper::Default ();
  YansWifiPhyHelper phy = YansWifiPhyHelper::Default ();
  phy.SetChannel (channel.Create ());

  WifiHelper wifi = WifiHelper::Default ();
  wifi.SetRemoteStationManager ("ns3::AarfWifiManager");

  NqosWifiMacHelper mac = NqosWifiMacHelper::Default ();

  Ssid ssid = Ssid ("ns-3-ssid");
  mac.SetType ("ns3::StaWifiMac",
               "Ssid", SsidValue (ssid),
               "ActiveProbing", BooleanValue (false));

  NetDeviceContainer staDevices;
  staDevices = wifi.Install (phy, mac, wifiStaNodes);

  mac.SetType ("ns3::ApWifiMac",
               "Ssid", SsidValue (ssid));

  NetDeviceContainer apDevices;
  apDevices = wifi.Install (phy, mac, wifiApNode);

  MobilityHelper mobilityWifi;

  /*mobilityWifi.SetPositionAllocator ("ns3::GridPositionAllocator",
                                 "MinX", DoubleValue (0.0),
                                 "MinY", DoubleValue (0.0),
                                 "DeltaX", DoubleValue (5.0),
                                 "DeltaY", DoubleValue (10.0),
                                 //"GridWidth", UintegerValue (3),
                                 "GridWidth", UintegerValue (100), // number of objects layed out on a line.
                                 "LayoutType", StringValue ("RowFirst"));*/

  //mobilityWifi.SetMobilityModel ("ns3::RandomWalk2dMobilityModel",
  //                           "Bounds", RectangleValue (Rectangle (-50, 50, -50, 50)));
  mobilityWifi.SetMobilityModel ("ns3::ConstantPositionMobilityModel");
  mobilityWifi.Install (wifiStaNodes);

  mobilityWifi.SetMobilityModel ("ns3::ConstantPositionMobilityModel");
  mobilityWifi.Install (wifiApNode);

  //InternetStackHelper stack;
  //internet.Install (wifiApNode);
  internet.Install (wifiStaNodes);

  Ipv4AddressHelper address;
  Ipv4InterfaceContainer serverInterfaces;

  address.SetBase ("10.1.3.0", "255.255.255.0");
  // Ipv4InterfaceContainer wifiInterfaces;
  // address.Assign (staDevices);
 
  Ipv4InterfaceContainer dcWifiInterfaces;
  dcWifiInterfaces.Add (address.Assign (apDevices));

  for (uint32_t i = 0; i < wifiStaNodes.GetN (); ++i)
     {
        serverInterfaces.Add (address.Assign (staDevices.Get (i)));
     }

  //Ipv4InterfaceContainer apInterfaces;
  //apInterfaces.Add (address.Assign (apDevices));

// -------------------------------------------------------------------------------
//  COSEM & DATA CONCENTRATOR CONFIGURATION
// -------------------------------------------------------------------------------
 
  // Cosem Applications Servers 
  UdpCosemServerHelper cosemServer (serverInterfaces);
  ApplicationContainer serverApps = cosemServer.Install (wifiStaNodes);
  serverApps.Start (Seconds (0.0001));
  serverApps.Stop (Seconds (simTime));

  // Cosem Application Clients 
  UdpCosemClientHelper cosemClient (serverApps, dcWifiInterfaces, Seconds (5.0)); 
  ApplicationContainer clientApps = cosemClient.Install (ueNodes.Get (0));
  clientApps.Start (Seconds (0.0001));
  clientApps.Stop (Seconds (simTime-10));

  // Data Concentrator Application
  DataConcentratorApplicationHelper dc (clientApps, internetIpIfaces.GetAddress (1), ueIpIface.GetAddress (0)); 
  ApplicationContainer dcApps = dc.Install (ueNodes.Get (0));
  dcApps.Start (Seconds (0.0001));
  dcApps.Stop (Seconds (simTime));

// -------------------------------------------------------------------------------
//  METER DATA MANAGEMENT & DEMAND RESPONSE APPLICATIONS CONFIGURATION 
// -------------------------------------------------------------------------------
 
  // Meter Data Management Application on Control Center
  MeterDataManagementApplicationHelper mdmHelper (dcApps, CcCsmaInterfaces.GetAddress (0), Seconds (20.0), 5.0);
  ApplicationContainer mdmApps = mdmHelper.Install (CcCsmaNodes.Get (0)); 
  mdmApps.Start (Seconds (1.0));
  mdmApps.Stop (Seconds (simTime));

  // Demand Response Application on Control Center (Send demand response signals to Data Cocentrator)
  DemandResponseApplicationHelper drHelper (CcCsmaInterfaces.GetAddress (1), mdmApps);
  ApplicationContainer drApps = drHelper.Install (CcCsmaNodes.Get (1));
  drApps.Start (Seconds (1.0));
  drApps.Stop (Seconds (simTime));

  //Ipv4GlobalRoutingHelper::PopulateRoutingTables ();
 
  Simulator::Stop(Seconds(simTime));

  phy.EnablePcap ("third", apDevices.Get (0));
  CcCsma.EnablePcap ("second", CcCsmaDevices.Get (1), true);

  /*Ipv4GlobalRoutingHelper g;
  Ptr<OutputStreamWrapper> routingStream= Create<OutputStreamWrapper>("dynamicDC.routes",std::ios::out);
  g.PrintRoutingTableAllAt(Seconds(0),routingStream);*/


// -------------------------------------------------------------------------------
//  FlowMonitor CONFIGURATION
// -------------------------------------------------------------------------------

  //Create a new FlowMonitorHelper object
  FlowMonitorHelper flowmon;
  Ptr<FlowMonitor> monitor;
  monitor = flowmon.Install(CcCsmaNodes.Get (0));
  monitor = flowmon.Install(CcCsmaNodes.Get (1));
  monitor = flowmon.Install(ueNodes.Get (0));
 
  for (uint32_t i = 0; i < wifiStaNodes.GetN (); ++i)
     {
        monitor = flowmon.Install(wifiStaNodes.Get (i));
     }

  // Conﬁgure some histogram attributes
  monitor->SetAttribute ("DelayBinWidth",
                           DoubleValue(0.01));
  monitor->SetAttribute ("JitterBinWidth",
                           DoubleValue(0.001));
  monitor->SetAttribute ("PacketSizeBinWidth",
                           DoubleValue(20));

  // Run simulation 
  Simulator::Run ();

  // Print per flow statistics
  monitor->CheckForLostPackets ();
  Ptr<Ipv4FlowClassifier> classifier = DynamicCast<Ipv4FlowClassifier> (flowmon.GetClassifier ());
  std::map<FlowId, FlowMonitor::FlowStats> stats = monitor->GetFlowStats ();

  // Open a file an recod the metrics
  std::ofstream myfile;
  std::ostringstream fileNameStream("");
  fileNameStream << "metrics-" << nWifi << ".txt";
  std::string fileName = fileNameStream.str();
 // std::cout << fileName << "\n"; 
  myfile.open(fileName.c_str());
 
  myfile << "Flow_id  scr_Address   dst_Address  Mean{Throughput}  Mean{Delay}  Mean{Jitter}  PLR\n";

  for (std::map<FlowId, FlowMonitor::FlowStats>::const_iterator i = stats.begin (); i != stats.end (); ++i)
    {     
      Ipv4FlowClassifier::FiveTuple t = classifier->FindFlow (i->first);
    /*myfile << "\nFlow " << i->first << " (" << t.sourceAddress << " -> " << t.destinationAddress << ")\n";           
      myfile << "  Tx Bytes:   " << i->second.txBytes << "\n";
      myfile << "  Rx Bytes:   " << i->second.rxBytes << "\n";
      myfile << "  Tx Packets: " << i->second.txPackets << "\n";
      myfile << "  Rx Packets: " << i->second.rxPackets << "\n";
      myfile << "  Lost Packets: " << i->second.lostPackets << "\n";
      myfile << "  Mean{Throughput}: " << (i->second.rxBytes * 8.0 / simTime)/ 1024 << " kbps\n"; 

      if (i->second.rxPackets > 0)
        {
          myfile << "  Mean{Delay}: " << (i->second.delaySum.GetSeconds() / i->second.rxPackets) * 1000 << " ms\n";
	  myfile << "  Mean{Jitter}: " << (i->second.jitterSum.GetSeconds() / (i->second.rxPackets-1)) * 1000 << " ms\n"; 
        }*/

     // Print Format: Flow_id scr_Address dst_Address  Mean{Throughput} Mean{Delay} Mean{Jitter} PLR
     myfile << i->first << " "                      
            << t.sourceAddress << " "   
            << t.destinationAddress << " "  
            << (i->second.rxBytes * 8.0 / simTime) << " "  //bps
            << (i->second.delaySum.GetSeconds() / i->second.rxPackets) << " "  //secs
            << (i->second.jitterSum.GetSeconds() / (i->second.rxPackets-1)) << " " //secs  
            << (i->second.lostPackets / (i->second.rxPackets + i->second.lostPackets)) << "\n";  
                   
    }
  // Close file
  myfile.close();

  // Write the ﬂow monitored results to "resultsWifiLteDc.xml" ﬁle
  monitor->SerializeToXmlFile("resultsWifiLteDc.xml",true,true);

  Simulator::Destroy();
  return 0;

}

