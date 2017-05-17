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
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/applications-module.h"
#include "ns3/udp-cosem-client-helper.h"
#include "ns3/udp-cosem-server-helper.h"

using namespace ns3;

NS_LOG_COMPONENT_DEFINE ("CosemPointToPointInterface");

int
main (int argc, char *argv[])
{
  // Log
  LogComponentEnable ("CosemApplicationLayerClient", LOG_LEVEL_INFO);
  LogComponentEnable ("CosemApplicationLayerServer", LOG_LEVEL_INFO);
  LogComponentEnable ("CosemApplicationsProcessClient", LOG_LEVEL_INFO);
  LogComponentEnable ("CosemApplicationsProcessServer", LOG_LEVEL_INFO);
  LogComponentEnable ("UdpCosemWrapperLayerClient", LOG_LEVEL_INFO);
  LogComponentEnable ("UdpCosemWrapperLayerServer", LOG_LEVEL_INFO);

  // Nodes
  NodeContainer clientNodes;
  clientNodes.Create (1);
  NodeContainer serverNodes;
  serverNodes.Create (1);

  // Communication Interface
  PointToPointHelper pointToPoint;
  pointToPoint.SetDeviceAttribute ("DataRate", StringValue ("5Mbps"));
  pointToPoint.SetChannelAttribute ("Delay", StringValue ("2ms"));

  // NetDevices
  NetDeviceContainer devices;
  devices = pointToPoint.Install (clientNodes.Get (0), serverNodes.Get (0));

  // Internet Stack
  InternetStackHelper stack;
  stack.Install (clientNodes.Get (0));
  stack.Install (serverNodes.Get (0)); 
 
  Ipv4AddressHelper address;
  address.SetBase ("10.1.1.0", "255.255.255.0");

  Ipv4InterfaceContainer clientInterfaces = address.Assign (devices.Get (0));
  Ipv4InterfaceContainer serverInterfaces = address.Assign (devices.Get (1));


  // Cosem Application

  UdpCosemServerHelper cosemServer (serverInterfaces);

  ApplicationContainer serverApps = cosemServer.Install (serverNodes.Get (0));
  serverApps.Start (Seconds (1.0));
  serverApps.Stop (Seconds (10.0));

  UdpCosemClientHelper cosemClient (serverApps, clientInterfaces, Seconds (5.0));

  ApplicationContainer clientApps = cosemClient.Install (clientNodes.Get (0));
  clientApps.Start (Seconds (2.0));
  clientApps.Stop (Seconds (10.0));

  Simulator::Run ();
  Simulator::Destroy ();
  return 0;
}
