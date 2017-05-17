/* -*-  Mode: C++; c-file-style: "gnu"; indent-tabs-mode:nil; -*- */
/*
 * Copyright (c) 2011 Centre Tecnologic de Telecomunicacions de Catalunya (CTTC)
 * Copyright (c) 2013 IEE - Uniandes 
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

// Default AMI Network Topology (Only tested with one DC node)
//                                                                                       
//                                                                                     MDMS    DR-Engine
//                          AP(Wifi)                                                     |      |
//  *      *         *       *                                                         =========== 
//  |      |         |       |    7.0.0.0            10.0.0.0            1.0.0.0         |   
// SM(1)  SM(0) ... SM(n)   DC ------------- eNB ::::::::::::::: PGW ::::::::::::::: GW-Router  
//                           |   LTE channel (1)    EPC Network  (1)     Internet               
//   Wifi 10.1.3.0           x                                                          LAN 10.1.2.0         
//   (AMI Network)          UE(LTE)                                                   (Control Center) 
//
// Note:
//       * Wifi connection
//    x/-- LTE connection
//       = Ethernet (CSMA) connection
//      :: Fiber optic connection
//      n < 253

using namespace ns3;

NS_LOG_COMPONENT_DEFINE ("CosemDCWifiLteExample");

int
main (int argc, char *argv[])
{

  bool verbose = true;      // For COSEM
  uint16_t nDcUeNodes = 1;  // DC UE LTE nodes
  uint16_t nBsNodes = 1;    // BS LTE nodes
  uint32_t nWifi = 252;       // Smart Meters
  uint32_t nCcCsma = 2;     // Control Center servers (DR-Engine + MDMS)
  uint64_t rngRun = 1;     // run number
  double simTime =  370.0;
  double reqSmTime = 60.0;
  double reqDcTime = 180.0;
  double distance = 60.0;
  double interPacketInterval = 100;
 
  // Command line arguments
  CommandLine cmd;
  cmd.AddValue("nWifi", "Number of Smart Meters", nWifi);
  cmd.AddValue("rngRun", "Number of independent runs", rngRun);
  cmd.AddValue("nDcUeNodes", "DC UEs", nDcUeNodes);
  cmd.AddValue("nBsNodes", "Number of eNodeBs", nBsNodes);
  cmd.AddValue("simTime", "Total duration of the simulation [s])", simTime);
  cmd.AddValue("distance", "Distance between eNBs [m]", distance);
  cmd.AddValue("interPacketInterval", "Inter packet interval [ms])", interPacketInterval);
  cmd.AddValue ("nCcCsma", "Number of \"extra\" CSMA nodes/devices", nCcCsma);
  cmd.AddValue ("verbose", "Tell echo applications to log if true", verbose);
  cmd.Parse(argc, argv);

  // For COSEM
  if (verbose)
    {
      LogComponentEnable ("CosemApplicationLayerClient", LOG_LEVEL_INFO);
      LogComponentEnable ("CosemApplicationLayerServer", LOG_LEVEL_INFO);
      LogComponentEnable ("CosemApplicationsProcessClient", LOG_LEVEL_INFO);
      LogComponentEnable ("UdpCosemWrapperLayerClient", LOG_LEVEL_INFO);
      LogComponentEnable ("CosemApplicationsProcessServer", LOG_LEVEL_INFO);
      LogComponentEnable ("UdpCosemWrapperLayerServer", LOG_LEVEL_INFO);
      LogComponentEnable ("DataConcentratorApplication", LOG_LEVEL_INFO);
      LogComponentEnable ("DemandResponseApplication", LOG_LEVEL_INFO);
      LogComponentEnable ("MeterDataManagementApplication", LOG_LEVEL_INFO);
      //LogComponentEnable ("Node", LOG_LEVEL_INFO);
      //LogComponentEnable ("EpcHelper", LOG_LEVEL_INFO);
      //LogComponentEnable ("PointToPointNetDevice", LOG_LEVEL_INFO);
      //LogComponentEnable ("LteEnbNetDevice", LOG_LEVEL_INFO);
      //LogComponentEnable ("LteNetDevice", LOG_LEVEL_INFO);
    }

  // CSMA  
  nCcCsma = nCcCsma == 0 ? 1 : nCcCsma;

  /* Produces multiple independent runs of the same simulation advancing the substream state
     (i.e. varying "g_rngRun" parameter (run number) and fixed seed)
     Source: http://www.nsnam.org/docs/manual/html/random-variables.html#seeding-and-independent-replications
  */
  RngSeedManager::SetRun (rngRun); 
  std::cout << "RUN NUMBER #" << RngSeedManager::GetRun () << "\n";

// -------------------------------------------------------------------------------
// NODES CREATION
// -------------------------------------------------------------------------------
  // GW-Router (RemoteHost) Nodes creation
  NodeContainer remoteHostContainer;
  remoteHostContainer.Create (1);
  Ptr<Node> remoteHost = remoteHostContainer.Get (0); // GW

  // CC Nodes creation: MDMS, DR-Engine
  NodeContainer CcCsmaNodes; 
  CcCsmaNodes.Add (remoteHost); // Add the remote Host (GW) node created to the CSMA network
  CcCsmaNodes.Create (nCcCsma);

  // Create DC-UEs and ENBs nodes
  NodeContainer dcUeNodes;
  NodeContainer enbNodes;
  dcUeNodes.Create(nDcUeNodes);
  enbNodes.Create(nBsNodes);
  // Install the IP stack on the UEs
  InternetStackHelper internet;
  internet.Install (dcUeNodes);

  // Wifi Nodes creation - Wifi Smart Meters
  NodeContainer wifiStaNodes;
  wifiStaNodes.Create (nWifi);
  NodeContainer wifiApNode = dcUeNodes.Get (0); // DC-UE Node

// -------------------------------------------------------------------------------
//  CSMA CONTROL CENTER NETWORK CONFIGURATION
// -------------------------------------------------------------------------------

  // Control Center Links configuration & NetDevice instalation
  CsmaHelper CcCsma;
  CcCsma.SetChannelAttribute ("DataRate", StringValue ("100Mbps"));
  CcCsma.SetChannelAttribute ("Delay", TimeValue (NanoSeconds (6560)));

  NetDeviceContainer CcCsmaDevices;
  CcCsmaDevices = CcCsma.Install (CcCsmaNodes);
  
  // Internet Stack instalation (with the remoteHost)
  for (uint32_t i = 0; i < CcCsmaNodes.GetN (); ++i)
     {
        internet.Install (CcCsmaNodes.Get (i));
     }

  // Assign Ip Address
  Ipv4AddressHelper addressCC;
  addressCC.SetBase ("10.1.2.0", "255.255.255.0");
  Ipv4InterfaceContainer CcCsmaInterfaces;
  for (uint32_t i = 0; i < (CcCsmaNodes.GetN ()); ++i)
     {
        CcCsmaInterfaces.Add (addressCC.Assign (CcCsmaDevices.Get (i)));
     }
 
// -------------------------------------------------------------------------------
//  WIFI AMI NETWORK CONFIGURATION
// -------------------------------------------------------------------------------
  
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

  // Internet Stack instalation 
  internet.Install (wifiStaNodes);

  // Assign Ip Address
  Ipv4AddressHelper address;
  Ipv4InterfaceContainer serverInterfaces;
  address.SetBase ("10.1.3.0", "255.255.255.0");
  Ipv4InterfaceContainer dcWifiInterfaces;
  dcWifiInterfaces.Add (address.Assign (apDevices));
  for (uint32_t i = 0; i < wifiStaNodes.GetN (); ++i)
     {
        serverInterfaces.Add (address.Assign (staDevices.Get (i)));
     }

// -------------------------------------------------------------------------------
//  LTE + EPC CONFIGURATION
// -------------------------------------------------------------------------------

  Ptr<LteHelper> lteHelper = CreateObject<LteHelper> ();
  Ptr<EpcHelper>  epcHelper = CreateObject<EpcHelper> ();
  lteHelper->SetEpcHelper (epcHelper);
  lteHelper->SetSchedulerType ("ns3::PfFfMacScheduler");
  lteHelper->SetAttribute ("PathlossModel",
                               StringValue ("ns3::FriisPropagationLossModel"));
  ConfigStore inputConfig;
  inputConfig.ConfigureDefaults();

  Ptr<Node> pgw = epcHelper->GetPgwNode ();

  // Create the Internet
  PointToPointHelper p2ph;
  p2ph.SetDeviceAttribute ("DataRate", DataRateValue (DataRate ("100Gb/s")));
  p2ph.SetDeviceAttribute ("Mtu", UintegerValue (1500));
  p2ph.SetChannelAttribute ("Delay", TimeValue (Seconds (0.010)));
  NetDeviceContainer internetDevices = p2ph.Install (pgw, remoteHost);
  Ipv4AddressHelper ipv4h;
  ipv4h.SetBase ("1.0.0.0", "255.0.0.0");
  Ipv4InterfaceContainer internetIpIfaces = ipv4h.Assign (internetDevices);

  // Install Mobility Model for UTRAN
  MobilityHelper mobility;
  Ptr<ListPositionAllocator> positionAlloc = CreateObject<ListPositionAllocator> ();
  positionAlloc->Add (Vector(50.0,50.0, 0)); // Initial coordinates for all nodes 
  mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
  mobility.SetPositionAllocator(positionAlloc);
  mobility.Install(dcUeNodes);  // DC-UE mobility instalation
  mobility.Install(enbNodes); // Enb mobility instalation
  mobility.Install(pgw); // pgw mobility instalation
  mobility.Install(CcCsmaNodes); // CC's server mobility instalation
  mobility.Install(wifiStaNodes); // SM-Wifi mobility instalation

  //--------------------Cell #1 Enb-1 for the AMI Network---------------------------//
  // Configure Tun devices for enb-1
  NetDeviceContainer enbDcLteDevs = lteHelper->InstallEnbDevice (enbNodes.Get (0));
  NetDeviceContainer dcUeLteDevs = lteHelper->InstallUeDevice (dcUeNodes);
  // Attach one UE per eNodeB  
  for (uint16_t i = 0; i < dcUeNodes.GetN (); i++)   // several UEs to one ENB
     {
       lteHelper->Attach (dcUeLteDevs.Get(i), enbDcLteDevs.Get (0));
     }
  // Assign IP address to UEs
  Ipv4InterfaceContainer ueDcIpIface;
  ueDcIpIface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (dcUeLteDevs));

  // Set the default gateway for the UE
  Ipv4StaticRoutingHelper ipv4RoutingHelper;
  for (uint32_t u = 0; u < dcUeNodes.GetN (); ++u)
    {
      Ptr<Node> ueNode = dcUeNodes.Get (u);
      Ptr<Ipv4StaticRouting> ueStaticRouting = ipv4RoutingHelper.GetStaticRouting (ueNode->GetObject<Ipv4> ());
      ueStaticRouting->SetDefaultRoute (epcHelper->GetUeDefaultGatewayAddress (), 2); // Working!!!
    }
   lteHelper->ActivateEpsBearer (dcUeLteDevs, EpsBearer (EpsBearer::NGBR_VIDEO_TCP_DEFAULT), EpcTft::Default ()); // Why does this line permit to send traffic from PGW to enB to UE?

  // Set the default gateway (Router) for CC server
  for (uint32_t i = 1; i < CcCsmaNodes.GetN (); ++i)
     {
       Ptr<Ipv4StaticRouting> ccStaticRouting = ipv4RoutingHelper.GetStaticRouting (CcCsmaNodes.Get (i)->GetObject<Ipv4> ());
       ccStaticRouting->SetDefaultRoute (CcCsmaInterfaces.GetAddress (0), 1); // Working!!!
    }
 
  Ptr<Ipv4StaticRouting> remoteHostStaticRouting = ipv4RoutingHelper.GetStaticRouting (remoteHost->GetObject<Ipv4> ());
  remoteHostStaticRouting->AddNetworkRouteTo (Ipv4Address ("7.0.0.0"), Ipv4Mask ("255.0.0.0"), 2); // Working!!!
  
  Ptr<Ipv4StaticRouting> pgwStaticRouting = ipv4RoutingHelper.GetStaticRouting (pgw->GetObject<Ipv4> ());
  pgwStaticRouting->AddNetworkRouteTo (Ipv4Address ("10.1.2.0"), Ipv4Mask ("255.255.255.0"), 2); // Working!!!

  lteHelper->EnableTraces ();
  // Uncomment to enable PCAP tracing
  //p2ph.EnablePcapAll("lena-epc-first");

// -------------------------------------------------------------------------------
// Building the AMI Topology 
// -------------------------------------------------------------------------------

  // Establishing nodes' coordinates of the AMI System 
  // For EPC network & DC-UE & SM-WIFI
  uint32_t a = 0;
  //std::cout << "a = " << a << "\n";
  uint32_t b = nDcUeNodes + nBsNodes + nWifi + 1; // +1 (PGW node)
  //std::cout << "b = " << b << "\n";
  double x, y;
  std::ifstream in; // read coordinates from a topology file
  in.open ("coordNodesAMI.txt");
  if(in.fail())
    { 
      std::cout << "ERROR: Can't open the input file!\n" << std::endl;
      return 1;
    } 

  while (!in.eof() & (a < b))
       {
         in >> x;
         in >> y;
         if (a == 0)
           (pgw -> GetObject<ConstantPositionMobilityModel>()) -> SetPosition(Vector(x, y, 0.0));  // PWG

         if (a == 1)
           (enbNodes.Get(0) -> GetObject<ConstantPositionMobilityModel>()) -> SetPosition(Vector(x, y, 0.0)); // ENB
         
         if (a == 2)
           (dcUeNodes.Get(0) -> GetObject<ConstantPositionMobilityModel>()) -> SetPosition(Vector(x, y, 0.0)); // DC

         if (a > 2)
           (wifiStaNodes.Get((a-3)) -> GetObject<ConstantPositionMobilityModel>()) -> SetPosition(Vector(x, y, 0.0)); // SM

         a ++;
       }
  in.close ();

  // CC's servers  
  for (uint32_t i = 0; i < CcCsmaNodes.GetN (); ++i)
     {
        (CcCsmaNodes.Get(i) -> GetObject<ConstantPositionMobilityModel>()) -> SetPosition(Vector(0.0, 0.0, 0.0)); // CC's server 
     }
 
 /* // FOR SMs-Wifi (must to be generated outside this script, because every that we run a independent simulation, the coordinates of SMs   nodes change (X,Y are random variable)
  ObjectFactory pos;  // Based on manet-routing-compare.cc
  pos.SetTypeId ("ns3::RandomRectanglePositionAllocator");  // Rectangle Area 120mx120m
  std::ostringstream XxStream("");
  XxStream << "ns3::UniformRandomVariable[Min=" << (x*1000+10) << "|Max=" << (x*1000+120) << "]";
  std::string Xx = XxStream .str();
  std::ostringstream YyStream("");
  YyStream << "ns3::UniformRandomVariable[Min=" << (y*1000+10) << "|Max=" << (y*1000+120) << "]";
  std::string Yy  = YyStream .str();
  pos.Set ("X", StringValue (Xx.c_str()));
  pos.Set ("Y", StringValue (Yy.c_str()));

  for (uint16_t i = 0; i < wifiStaNodes.GetN (); i++)
     {
       Ptr<PositionAllocator> taPositionAlloc = pos.Create ()->GetObject<PositionAllocator> ();
       mobility.SetPositionAllocator(taPositionAlloc);
     }
*/


  // Source: https://bitbucket.org/domargan/ns-3-waf/src/5768685f9fdb/samples/main-grid-topology.cc?at=ns-3.1
  // iterate our nodes and print their position.
  Ptr<MobilityModel> position_pgw = pgw->GetObject<MobilityModel> ();
  NS_ASSERT (position_pgw != 0);
  Vector pos_pgw = position_pgw->GetPosition ();
  std::cout << "PGW (id = " << pgw->GetId () << "):"
            << " x=" << pos_pgw.x << ", y=" << pos_pgw.y << ", z=" << pos_pgw.z << std::endl;

  for (NodeContainer::Iterator j = enbNodes.Begin ();
       j != enbNodes.End (); ++j)
    {
      Ptr<Node> object = *j;
      Ptr<MobilityModel> position = object->GetObject<MobilityModel> ();
      NS_ASSERT (position != 0);
      Vector pos = position->GetPosition ();
      std::cout << "eNB (id = " << object->GetId () << "):"
                << " x=" << pos.x << ", y=" << pos.y << ", z=" << pos.z << std::endl;
    }

  for (NodeContainer::Iterator j = dcUeNodes.Begin ();
       j != dcUeNodes.End (); ++j)
    {
      Ptr<Node> object = *j;
      Ptr<MobilityModel> position = object->GetObject<MobilityModel> ();
      NS_ASSERT (position != 0);
      Vector pos = position->GetPosition ();
      std::cout << "DC (id = " << object->GetId () << "): "
                << " x=" << pos.x << ", y=" << pos.y << ", z=" << pos.z << std::endl;
    }

  for (NodeContainer::Iterator j = wifiStaNodes.Begin ();
       j != wifiStaNodes.End (); ++j)
    {
      Ptr<Node> object = *j;
      Ptr<MobilityModel> position = object->GetObject<MobilityModel> ();
      NS_ASSERT (position != 0);
      Vector pos = position->GetPosition ();
      std::cout << "SM (id = " << object->GetId () << "):"
                << " x=" << pos.x << ", y=" << pos.y << ", z=" << pos.z << std::endl;
    }

  for (NodeContainer::Iterator j = CcCsmaNodes.Begin ();
       j != CcCsmaNodes.End (); ++j)
    {
      Ptr<Node> object = *j;
      Ptr<MobilityModel> position = object->GetObject<MobilityModel> ();
      NS_ASSERT (position != 0);
      Vector pos = position->GetPosition ();
      std::cout << "CC (id = " << object->GetId () << "):"
                << " x=" << pos.x << ", y=" << pos.y << ", z=" << pos.z << std::endl;
    }

// -------------------------------------------------------------------------------
//  COSEM & DATA CONCENTRATOR APPLICATIONS CONFIGURATION
// -------------------------------------------------------------------------------
 
  // Cosem Applications Servers 
  UdpCosemServerHelper cosemServer (serverInterfaces);
  ApplicationContainer serverApps = cosemServer.Install (wifiStaNodes);
  serverApps.Start (Seconds (0.0001));
  serverApps.Stop (Seconds (simTime));

  // Cosem Application Clients 
  UdpCosemClientHelper cosemClient (serverApps, dcWifiInterfaces, Seconds (reqSmTime)); 
  ApplicationContainer clientApps = cosemClient.Install (dcUeNodes.Get (0));
  clientApps.Start (Seconds (0.0001));
  clientApps.Stop (Seconds (simTime-2));

  // Data Concentrator Application
  DataConcentratorApplicationHelper dc (clientApps, CcCsmaInterfaces.GetAddress (1), ueDcIpIface.GetAddress (0));  
  ApplicationContainer dcApps = dc.Install (dcUeNodes.Get (0));
  dcApps.Start (Seconds (0.0001));
  dcApps.Stop (Seconds (simTime));

// -------------------------------------------------------------------------------
//  METER DATA MANAGEMENT & DEMAND RESPONSE APPLICATIONS CONFIGURATION 
// -------------------------------------------------------------------------------
 
  // Meter Data Management Application on Control Center
  MeterDataManagementApplicationHelper mdmHelper (dcApps, CcCsmaInterfaces.GetAddress (1), Seconds (reqDcTime), reqSmTime);
  ApplicationContainer mdmApps = mdmHelper.Install (CcCsmaNodes.Get (1)); 
  mdmApps.Start (Seconds (1.0));
  mdmApps.Stop (Seconds (simTime));

  // Demand Response Application on Control Center (Send demand response signals to Data Cocentrator)
  DemandResponseApplicationHelper drHelper (CcCsmaInterfaces.GetAddress (2), mdmApps);
  ApplicationContainer drApps = drHelper.Install (CcCsmaNodes.Get (2));
  drApps.Start (Seconds (1.0));
  drApps.Stop (Seconds (simTime));

  //Ipv4GlobalRoutingHelper::PopulateRoutingTables ();
 
  Simulator::Stop(Seconds(simTime));

/*  phy.EnablePcap ("third", apDevices.Get (0));
  CcCsma.EnablePcap ("second", CcCsmaDevices.Get (1), true);*/

/*  Ipv4GlobalRoutingHelper g;
  Ptr<OutputStreamWrapper> routingStream= Create<OutputStreamWrapper>("AmiDynamicRoutes.routes",std::ios::out);
  g.PrintRoutingTableAllAt(Seconds(0),routingStream);*/


// -------------------------------------------------------------------------------
//  FlowMonitor CONFIGURATION
// -------------------------------------------------------------------------------

  //Create a new FlowMonitorHelper object
  FlowMonitorHelper flowmon;
  Ptr<FlowMonitor> monitor;

  for (uint32_t i = 0; i < CcCsmaNodes.GetN (); ++i)
     {
        flowmon.Install(CcCsmaNodes.Get (i));
     }

  for (uint32_t u = 0; u < dcUeNodes.GetN (); ++u)
     {
       monitor = flowmon.Install(dcUeNodes.Get (u));   
     }

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

  // Open a file an record the metrics
  std::ofstream myfile;
  std::ostringstream fileNameStream("");
  fileNameStream << "metrics-" << nWifi << "-" << rngRun << ".txt";
  std::string fileName = fileNameStream.str();
 // std::cout << fileName << "\n"; 
  myfile.open(fileName.c_str());
 
 // myfile << "Flow_id  scr_Address   dst_Address  Mean{Throughput}  Mean{Delay}  Mean{Jitter}  PLR Mean{TxBitRate} Mean{RxBitRate} \n";
  myfile << "Flow_id  scr_Address   dst_Address  Mean{Throughput}  Mean{Delay}  Mean{Jitter}  PLR \n";
  
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

     // Print Format: Flow_id scr_Address dst_Address  Mean{Throughput} Mean{Delay} Mean{Jitter} PLR Mean{TxBitRate} Mean{RxBitRate}
     myfile << i->first << " "                      
            << t.sourceAddress << " "   
            << t.destinationAddress << " "  
            << (i->second.rxBytes * 8.0 / simTime) << " "  //bps
            << (i->second.delaySum.GetSeconds() / i->second.rxPackets) << " "  //secs
            << (i->second.jitterSum.GetSeconds() / (i->second.rxPackets-1)) << " " //secs  
            << (i->second.lostPackets / (i->second.rxPackets + i->second.lostPackets)) << "\n";
            //<< ((8.0*i->second.txBytes) / (i->second.timeLastTxPacket - i->second.timeFirstTxPacket)) << " " //bit/s
            //<< ((8.0*i->second.rxBytes) / (i->second.timeLastRxPacket - i->second.timeFirstRxPacket)) << "\n"; //bit/s
                   
    }
  // Close file
  myfile.close();

  // Write the ﬂow monitored results to "resultsWifiLteDc.xml" ﬁle
  //monitor->SerializeToXmlFile("resultsWifiLteDc.xml",true,true);

  Simulator::Destroy();
  return 0;

}

