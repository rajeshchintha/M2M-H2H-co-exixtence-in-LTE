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
#include "ns3/mms-client-helper.h"
#include "ns3/mms-server-helper.h"

using namespace ns3;

NS_LOG_COMPONENT_DEFINE ("MmsPointToPointInterface");

int
main (int argc, char *argv[])
{
	// Log
	Config::SetDefault ("ns3::TcpSocket::DelAckTimeout",TimeValue(Seconds (0)));
	Config::SetDefault ("ns3::TcpSocket::ConnTimeout",TimeValue(Seconds (0.5)));

	uint16_t numberOfNodes = 1;
	double simTime = 20.0;
	double distance = 1000.0;
	double interPacketInterval = 100;
	int type = 0;
	int mode = 1;
	// Command line arguments
	CommandLine cmd;
	cmd.AddValue("numberOfNodes", "Number of UEs", numberOfNodes);
	cmd.AddValue("simTime", "Total duration of the simulation [s])", simTime);
	cmd.AddValue("distance", "Distance between eNBs [m]", distance);
	cmd.AddValue("type", "Type of Requesting", type); // 0 = multicast, 1 = sequential, 2 = reporting
	cmd.AddValue("mode", "Mode 0 = LTE; 1 = Hybrid", type); //
	cmd.AddValue("interPacketInterval", "Inter packet interval [ms])", interPacketInterval);
	cmd.Parse(argc, argv);

	LogComponentEnable ("MmsAdaptClient", LOG_LEVEL_INFO);
	LogComponentEnable ("MmsAdaptServer", LOG_LEVEL_INFO);
	LogComponentEnable ("MmsClient", LOG_LEVEL_INFO);
	LogComponentEnable ("MmsServer", LOG_LEVEL_INFO);
	LogComponentEnable ("CotpClient", LOG_LEVEL_INFO);
	LogComponentEnable ("CotpServer", LOG_LEVEL_INFO);
	LogComponentEnable ("TcpSocket", LOG_LEVEL_INFO);


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

	pointToPoint.EnablePcapAll("mms-p2p-");
	// MMS Application

	MmsServerHelper mmsServer (serverInterfaces);

	ApplicationContainer serverApps = mmsServer.Install (serverNodes.Get (0));
	serverApps.Start (Seconds (1.0));
	serverApps.Stop (Seconds (20.0));

	MmsClientHelper mmsClient (serverApps, clientInterfaces, MilliSeconds (0), type, mode, false);

	ApplicationContainer clientApps = mmsClient.Install (clientNodes.Get (0));
	clientApps.Start (Seconds (2.0));
	clientApps.Stop (Seconds (20.0));

	Simulator::Stop(Seconds(simTime));

	Simulator::Run ();

	Simulator::Destroy ();
	return 0;
}
