/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/*
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
 */

#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/csma-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/applications-module.h"
#include "ns3/ipv4-global-routing-helper.h"
#include "ns3/udp-cosem-client-helper.h"
#include "ns3/udp-cosem-server-helper.h"
#include "ns3/data-concentrator-helper.h"
#include "ns3/demand-response-application-helper.h"
#include "ns3/mdm-application-helper.h"

// Default Network Topology
// 
// (CC-DR)     (CC-MDM)   10.1.1.0     (DC) (SM1) (SM2) ... (SM(i))
//    n0          n1------------------- n2   n3   n4    ...  n(i)
//    |           |     fiber (p2p)     |    |    |          |
//    =============                     ====================== (CSMA Channel)
//    LAN 10.1.3.0                           LAN 10.1.2.0


using namespace ns3;

NS_LOG_COMPONENT_DEFINE ("CosemCsmadcExample");

int 
main (int argc, char *argv[])
{
  bool verbose = true;
  uint32_t nAmiCsma = 3;
  uint32_t nCcCsma = 1;

  CommandLine cmd;
  cmd.AddValue ("nAmiCsma", "Number of \"extra\" CSMA nodes/devices", nAmiCsma);
  cmd.AddValue ("verbose", "Tell echo applications to log if true", verbose);

  cmd.Parse (argc,argv);

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

  nAmiCsma = nAmiCsma == 0 ? 1 : nAmiCsma;
  nCcCsma = nCcCsma == 0 ? 1 : nCcCsma;

// -------------------------------------------------------------------------------
//  TOPOLOGY CONSTRUCTION & CONFIGURATION
// -------------------------------------------------------------------------------

  // Backhaul Nodes creation
  NodeContainer p2pNodes;
  p2pNodes.Create (2);

  // AMI Nodes creation
  NodeContainer AmiCsmaNodes;  // AMI network
  AmiCsmaNodes.Add (p2pNodes.Get (1));
  AmiCsmaNodes.Create (nAmiCsma);
 
  // CC Nodes creation
  NodeContainer CcCsmaNodes; // CC network
  CcCsmaNodes.Add (p2pNodes.Get (0));
  CcCsmaNodes.Create (nCcCsma);

  // Backhaul Link configuration & NetDevice instalation
  PointToPointHelper pointToPoint; // fiber
  pointToPoint.SetDeviceAttribute ("DataRate", StringValue ("622Mbps"));
  pointToPoint.SetChannelAttribute ("Delay", StringValue ("1ms"));

  NetDeviceContainer p2pDevices;
  p2pDevices = pointToPoint.Install (p2pNodes);

  // AMI Links configuration & NetDevice instalation
  CsmaHelper AmiCsma; 
  AmiCsma.SetChannelAttribute ("DataRate", StringValue ("100Mbps"));
  AmiCsma.SetChannelAttribute ("Delay", TimeValue (NanoSeconds (6560)));

  NetDeviceContainer AmiCsmaDevices;
  AmiCsmaDevices = AmiCsma.Install (AmiCsmaNodes);

  // Control Center Links configuration & NetDevice instalation
  CsmaHelper CcCsma;
  CcCsma.SetChannelAttribute ("DataRate", StringValue ("100Mbps"));
  CcCsma.SetChannelAttribute ("Delay", TimeValue (NanoSeconds (6560)));

  NetDeviceContainer CcCsmaDevices;
  CcCsmaDevices = CcCsma.Install (CcCsmaNodes);

  // Internet Stack instalation 
  InternetStackHelper stack;
  stack.Install (AmiCsmaNodes);
  stack.Install (CcCsmaNodes);

// -------------------------------------------------------------------------------
//  BACKHAUL CONFIGURATION
// -------------------------------------------------------------------------------

  Ipv4AddressHelper address;
  address.SetBase ("10.1.1.0", "255.255.255.0");
  Ipv4InterfaceContainer p2pInterfaces;
  p2pInterfaces = address.Assign (p2pDevices);

// -------------------------------------------------------------------------------
//  AMI NETWORK CONFIGURATION
// -------------------------------------------------------------------------------

  address.SetBase ("10.1.2.0", "255.255.255.0");
  Ipv4InterfaceContainer AmiCsmaInterfaces;
  Ipv4InterfaceContainer dcCsmaInterfaces;
  dcCsmaInterfaces.Add (address.Assign (AmiCsmaDevices.Get (0)));

  for (uint32_t i = 1; i < AmiCsmaNodes.GetN (); ++i)
     {
        AmiCsmaInterfaces.Add (address.Assign (AmiCsmaDevices.Get (i)));
     }

// -------------------------------------------------------------------------------
//  CONTROL CENTER NETWORK CONFIGURATION
// -------------------------------------------------------------------------------
 
  address.SetBase ("10.1.3.0", "255.255.255.0");
  Ipv4InterfaceContainer CcCsmaInterfaces = address.Assign (CcCsmaDevices);

// -------------------------------------------------------------------------------
//  COSEM & DATA CONCENTRATOR CONFIGURATION
// -------------------------------------------------------------------------------
 
  // Cosem Applications Servers 
  UdpCosemServerHelper cosemServer (AmiCsmaInterfaces);
  ApplicationContainer serverApps;
  for (uint32_t i = 1; i < AmiCsmaNodes.GetN (); ++i)
    {
       serverApps.Add (cosemServer.Install (AmiCsmaNodes.Get (i)));
    }
  serverApps.Start (Seconds (1.0));
  serverApps.Stop (Seconds (900.0));

  // Cosem Application Clients  
  // (request data to Smart Meters every 1 min + delay of polling all smart meters)
  UdpCosemClientHelper cosemClient (serverApps, dcCsmaInterfaces, Seconds (60.0)); 
  ApplicationContainer clientApps = cosemClient.Install (p2pNodes.Get (1));
  clientApps.Start (Seconds (1.0));
  clientApps.Stop (Seconds (900.0));

  // Data Concentrator Application
  DataConcentratorApplicationHelper dc (clientApps, p2pInterfaces.GetAddress (0), p2pInterfaces.GetAddress (1)); 
  ApplicationContainer dcApps = dc.Install (p2pNodes.Get (1));
  dcApps.Start (Seconds (1.001));
  dcApps.Stop (Seconds (900.0));

// -------------------------------------------------------------------------------
//  METER DATA MANAGEMENT & DEMAND RESPONSE APPLICATIONS CONFIGURATION 
// -------------------------------------------------------------------------------
 
  
  // Meter Data Management Application on Control Center (request data to Data Concentrator every 3 min)
  MeterDataManagementApplicationHelper mdmHelper (dcApps, p2pInterfaces.GetAddress (0), Seconds (180.0), 60.0);
  ApplicationContainer mdmApps = mdmHelper.Install (p2pNodes.Get (0));
  mdmApps.Start (Seconds (0.0));
  mdmApps.Stop (Seconds (900.0));

  // Demand Response Application on Control Center (Send demand response signals to Data Cocentrator)
  DemandResponseApplicationHelper drHelper (CcCsmaInterfaces.GetAddress (1), mdmApps);
  ApplicationContainer drApps = drHelper.Install (CcCsmaNodes.Get(1));
  drApps.Start (Seconds (0.0));
  drApps.Stop (Seconds (900.0));

  Ipv4GlobalRoutingHelper::PopulateRoutingTables ();
  
  Simulator::Stop (Seconds (950.0));
  
  pointToPoint.EnablePcapAll ("second");
  AmiCsma.EnablePcap ("second", AmiCsmaDevices.Get (1), true);
  CcCsma.EnablePcap ("second", CcCsmaDevices.Get (1), true);

  Simulator::Run ();
  Simulator::Destroy ();
  return 0;
}
