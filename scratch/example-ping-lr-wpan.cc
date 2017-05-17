/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/*
 * Copyright (c) 2013 Universita' di Firenze, Italy
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
 * Author: Tommaso Pecorella <tommaso.pecorella@unifi.it>
 */


#include <fstream>
#include "ns3/core-module.h"
#include "ns3/internet-module.h"
#include "ns3/applications-module.h"
#include "ns3/internet-apps-module.h"
#include "ns3/ipv6-static-routing-helper.h"
#include "ns3/mobility-module.h"
#include "ns3/spectrum-module.h"
#include "ns3/propagation-loss-model.h"
#include "ns3/log.h"
#include "ns3/ipv6-routing-table-entry.h"
#include "ns3/sixlowpan-module.h"
#include "ns3/lr-wpan-module.h"

using namespace ns3;



int main (int argc, char** argv)
{

Address serverAddress;//added by me
  CommandLine cmd;
  cmd.Parse (argc, argv);
  
#if 0
  LogComponentEnable ("Ping6Application", LOG_LEVEL_ALL);
  LogComponentEnable ("LrWpanMac",LOG_LEVEL_ALL);
  LogComponentEnable ("LrWpanPhy",LOG_LEVEL_ALL);
  LogComponentEnable ("LrWpanNetDevice", LOG_LEVEL_ALL);
  LogComponentEnable ("SixLowPanNetDevice", LOG_LEVEL_ALL);
#endif

  NodeContainer nodes;
  nodes.Create(2);

  MobilityHelper mobility;
  mobility.SetMobilityModel ("ns3::ConstantPositionMobilityModel");
  mobility.SetPositionAllocator ("ns3::GridPositionAllocator",
                                 "MinX", DoubleValue (0.0),
                                 "MinY", DoubleValue (0.0),
                                 "DeltaX", DoubleValue (20),
                                 "DeltaY", DoubleValue (20),
                                 "GridWidth", UintegerValue (3),
                                 "LayoutType", StringValue ("RowFirst"));
  mobility.SetMobilityModel ("ns3::ConstantPositionMobilityModel");
  mobility.Install (nodes);
  
  LrWpanHelper lrWpanHelper;
  // Add and install the LrWpanNetDevice for each node
  NetDeviceContainer lrwpanDevices = lrWpanHelper.Install(nodes);

  // Fake PAN association and short address assignment.
  lrWpanHelper.AssociateToPan (lrwpanDevices, 0);

  InternetStackHelper internetv6;
  internetv6.Install (nodes);

  SixLowPanHelper sixlowpan;
  NetDeviceContainer devices = sixlowpan.Install (lrwpanDevices); 
 
  Ipv6AddressHelper ipv6;
  ipv6.SetBase (Ipv6Address ("2001:2::"), Ipv6Prefix (64));
  Ipv6InterfaceContainer deviceInterfaces;
  deviceInterfaces = ipv6.Assign (devices);
  // check if adresses are assigned
  //std::cout<< deviceInterfaces.GetAddress(0,1)<<std::endl;
  //std::cout<< deviceInterfaces.GetAddress(1,1)<<std::endl;
  serverAddress = Address(deviceInterfaces.GetAddress (1,1)); //added by me


   
  uint32_t packetSize = 10;
  uint32_t maxPacketCount = 5;
  Time interPacketInterval = Seconds (1.);
  Ping6Helper ping6;

  ping6.SetLocal (deviceInterfaces.GetAddress (0, 1));
  ping6.SetRemote (deviceInterfaces.GetAddress (1, 1));

  ping6.SetAttribute ("MaxPackets", UintegerValue (maxPacketCount));
  ping6.SetAttribute ("Interval", TimeValue (interPacketInterval));
  ping6.SetAttribute ("PacketSize", UintegerValue (packetSize));
  ApplicationContainer apps = ping6.Install (nodes.Get (0));

  apps.Start (Seconds (1.0));
  apps.Stop (Seconds (10.0));

  AsciiTraceHelper ascii;
  lrWpanHelper.EnableAsciiAll (ascii.CreateFileStream ("Ping-6LoW-lr-wpan.tr"));
  lrWpanHelper.EnablePcapAll (std::string ("Ping-6LoW-lr-wpan"), true);

//************Code added by RajeshChintha from examples/udp/udp-echo.cc******************************************************
//NS_LOG_INFO ("Create Applications.");
//
// Create a UdpEchoServer application on node one.
//
  uint16_t port = 9;  // well-known echo port number
  UdpEchoServerHelper server (port);
  ApplicationContainer apps1 = server.Install (nodes.Get (1));
  apps1.Start (Seconds (1.0));
  apps1.Stop (Seconds (10.0));

//
// Create a UdpEchoClient application to send UDP datagrams from node zero to
// node one.
//
//  uint32_t packetSize = 1024;
  //uint32_t maxPacketCount = 1;
 // Time interPacketInterval = Seconds (1.);
  UdpEchoClientHelper client (serverAddress, port);
  client.SetAttribute ("MaxPackets", UintegerValue (maxPacketCount));
  client.SetAttribute ("Interval", TimeValue (interPacketInterval));
  //client.SetAttribute ("PacketSize", UintegerValue (packetSize));//Commented by me
 // apps1 = client.Install (nodes.Get (0));
  //apps1.Start (Seconds (2.0));
  //apps1.Stop (Seconds (10.0));

//#if 0
//
// Users may find it convenient to initialize echo packets with actual data;
// the below lines suggest how to do this
//
  std::ostringstream msg; msg << "Hello World!" << '\0';
  client.SetAttribute ("PacketSize", UintegerValue (msg.str().length()));//moved by me from top and modified from https://groups.google.com/forum/#!msg/ns-3-users/2fUqfp1kYQM/Ouz2ASKUDQAJ
  apps1 = client.Install (nodes.Get (0));//moved by me from top
  apps1.Start (Seconds (2.0));//moved by me from top
  apps1.Stop (Seconds (10.0));//moved by me from top

  //client.SetFill (apps.Get (0), "Hello World");//commented by me and replaced it with below line
    client.SetFill (apps.Get (0), (uint8_t*) msg.str().c_str(), msg.str().length(), msg.str().length());


  //client.SetFill (apps.Get (0), 0xa5, 1024);

  //uint8_t fill[] = { 0, 1, 2, 3, 4, 5, 6};
  //client.SetFill (apps.Get (0), fill, sizeof(fill), 1024);
//#endif

//  AsciiTraceHelper ascii;
  lrWpanHelper.EnableAsciiAll (ascii.CreateFileStream ("udp-echo.tr"));
  lrWpanHelper.EnablePcapAll ("udp-echo", false);

//***************************************************************************************************************
  
  Simulator::Stop (Seconds (10));
  
  Simulator::Run ();
  Simulator::Destroy ();

}

