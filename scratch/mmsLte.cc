//This file is copied from Dung-nguyan (https://github.com/anhdung0111/myrepository) and made changes by RajeshChintha on 28-12-2016
#include "ns3/lte-helper.h"
#include "ns3/epc-helper.h"
#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/ipv4-global-routing-helper.h"
#include "ns3/internet-module.h"
#include "ns3/mobility-module.h"
#include "ns3/lte-module.h"
#include "ns3/applications-module.h"
#include "ns3/point-to-point-helper.h"
#include "ns3/config-store.h"
//#include "ns3/gtk-config-store.h"
#include "ns3/mms-client-helper.h"
#include "ns3/mms-server-helper.h"
#include "ns3/lte-ue-rrc.h"

using namespace ns3;

/**
 * Sample simulation script for LTE+EPC. It instantiates several eNodeB,
 * attaches one UE per eNodeB starts a flow for each UE to  and from a remote host.
 * It also  starts yet another flow between each UE pair.
 */

NS_LOG_COMPONENT_DEFINE ("MmsLteExample");

int
main (int argc, char *argv[])
{
	Config::SetDefault ("ns3::TcpSocket::DelAckTimeout",TimeValue(Seconds (0)));
	Config::SetDefault ("ns3::TcpSocket::ConnTimeout",TimeValue(Seconds (0.5)));
	//Config::SetDefault ("ns3::TcpL4Protocol::SocketType", TypeIdValue(TypeId::LookupByName ("ns3::TcpTahoe")));//replaced with next line
	Config::SetDefault ("ns3::TcpL4Protocol::SocketType", TypeIdValue(TypeId::LookupByName("ns3::TcpNewReno")));//added by me, another type of congestion control

	bool verbose = true;  // For MMS
	uint16_t numberOfNodes = 38;//changed from 100 by me
	double simTime = 10.0;
	int mode = 1;//added by me from mmsp2p.cc
	double distance = 600.0;
	double requestInterval = 0.1;
	std::string output = "mms-lte";
	int type = 0;
	// Command line arguments
	CommandLine cmd;
	cmd.AddValue("numberOfNodes", "Number of UEs", numberOfNodes);
	cmd.AddValue("simTime", "Total duration of the simulation [s])", simTime);
	cmd.AddValue("distance", "Distance between eNBs [m]", distance);
	cmd.AddValue("type", "Type of Requesting", type); // 0 = multicast, 1 = sequential, 2 = reporting
	cmd.AddValue("mode", "Mode 0 = LTE; 1 = Hybrid", type); // added by me from mmsp2p.cc
	cmd.AddValue("requestInterval", "Time between two requests [s])", requestInterval);
	cmd.AddValue("outputfile", "File name of the output trace", output);
	cmd.Parse(argc, argv);

	// For MMS
	if (verbose)
	{
//		LogComponentEnable ("MmsAdaptClient", LOG_LEVEL_INFO);
//		LogComponentEnable ("MmsAdaptServer", LOG_LEVEL_INFO);
		LogComponentEnable ("MmsClient", LOG_LEVEL_INFO);
		LogComponentEnable ("MmsServer", LOG_LEVEL_INFO);
//		LogComponentEnable ("CotpClient", LOG_LEVEL_INFO);
//		LogComponentEnable ("CotpServer", LOG_LEVEL_INFO);
//		LogComponentEnable ("MmsServerHelper", LOG_LEVEL_INFO);
//		LogComponentEnable ("TcpTahoe", LOG_LEVEL_LOGIC);
		LogComponentEnable ("TcpSocketBase", LOG_LEVEL_LOGIC);
//		LogComponentEnable ("RttEstimator", LOG_LEVEL_DEBUG);
//		LogComponentEnable ("LteEnbRrc", LOG_LEVEL_INFO);
		LogComponentEnable ("LteUeRrc", LOG_LEVEL_INFO);
//		LogComponentEnable ("LteUeMac", LOG_LEVEL_INFO);
//		LogComponentEnable ("LteRlcUm", LOG_LEVEL_LOGIC);

//		LogComponentEnable ("RrFfMacScheduler", LOG_LEVEL_INFO);



	}
	//

	Ptr<LteHelper> lteHelper = CreateObject<LteHelper> ();
//	Ptr<EpcHelper>  epcHelper = CreateObject<EpcHelper> ();
	Ptr<PointToPointEpcHelper>  epcHelper = CreateObject<PointToPointEpcHelper> ();   //added by me from lena-simple-epc.cc  
	lteHelper->SetEpcHelper (epcHelper);
	lteHelper->SetSchedulerType("ns3::RrFfMacScheduler");
	Config::SetDefault ("ns3::ConfigStore::Filename", StringValue ("input-defaults.txt"));
	Config::SetDefault ("ns3::ConfigStore::Mode", StringValue ("Load"));
	Config::SetDefault ("ns3::ConfigStore::FileFormat", StringValue ("RawText"));
	ConfigStore inputConfig;
	inputConfig.ConfigureDefaults();

	// parse again so you can override default values from the command line
	cmd.Parse(argc, argv);

	Ptr<Node> pgw = epcHelper->GetPgwNode ();

	// Create a single RemoteHost
	NodeContainer remoteHostContainer;
	remoteHostContainer.Create (1);//mms client where as ue are mms servers
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
	Ipv4InterfaceContainer clientLteInterfaces;
	clientLteInterfaces.Add (ipv4h.Assign (internetDevices.Get (1)));
	//Ipv4Address remoteHostAddr = clientLteInterfaces.GetAddress (0);
	//

	Ipv4StaticRoutingHelper ipv4RoutingHelper;
	Ptr<Ipv4StaticRouting> remoteHostStaticRouting = ipv4RoutingHelper.GetStaticRouting (remoteHost->GetObject<Ipv4> ());
	remoteHostStaticRouting->AddNetworkRouteTo (Ipv4Address ("7.0.0.0"), Ipv4Mask ("255.0.0.0"), 1);

	NodeContainer ueNodes;
	NodeContainer enbNodes;
	enbNodes.Create(1);
	ueNodes.Create(numberOfNodes);

	// Install Mobility Model
	// Position of eNodeB
	Ptr<ListPositionAllocator> positionAlloc = CreateObject<ListPositionAllocator> ();
	positionAlloc->Add (Vector (0.0, 0.0, 0.0));
	MobilityHelper enbMobility;
	enbMobility.SetMobilityModel ("ns3::ConstantPositionMobilityModel");
	enbMobility.SetPositionAllocator (positionAlloc);
	enbMobility.Install (enbNodes);

	// Position of UEs attached to eNodeB
	MobilityHelper uemobility;
	uemobility.SetPositionAllocator ("ns3::UniformDiscPositionAllocator",
			"X", DoubleValue (0.0),
			"Y", DoubleValue (0.0),
			"rho", DoubleValue (distance));
	uemobility.SetMobilityModel ("ns3::ConstantPositionMobilityModel");
	uemobility.Install(ueNodes);

	// Install LTE Devices to the nodes
	NetDeviceContainer enbLteDevs = lteHelper->InstallEnbDevice (enbNodes);
	NetDeviceContainer ueLteDevs = lteHelper->InstallUeDevice (ueNodes);

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
	}

	// Attach UEs to eNodeB
	for (uint16_t i = 0; i < numberOfNodes; i++)
	{
		lteHelper->Attach (ueLteDevs.Get(i), enbLteDevs.Get(0));
//		if (ueLteDevs.Get(i)->GetObject<LteUeNetDevice> ()->GetRrc()->GetState() == LteUeRrc::CONNECTED_NORMALLY)
//		{
//
//		}



	}
	//lteHelper->ActivateDedicatedEpsBearer (ueLteDevs, EpsBearer (EpsBearer::NGBR_VIDEO_TCP_DEFAULT), EpcTft::Default ());

	// MMS Application

	MmsServerHelper mmsServer (ueIpIface);


	ApplicationContainer serverApps = mmsServer.Install (ueNodes);
	serverApps.Start (Seconds (0.01));
	serverApps.Stop (Seconds (10.0));

	MmsClientHelper mmsClient (serverApps, clientLteInterfaces, Seconds (requestInterval), type, mode, false);//Last tow arguments are added by RajeshChintha from mmsp2p.cc



	ApplicationContainer clientApps = mmsClient.Install (remoteHost);
	clientApps.Start (Seconds (0.05));
	clientApps.Stop (Seconds (10.0));

	//  serverApps.Start (Seconds (0.01));
	//  clientApps.Start (Seconds (0.01));
	lteHelper->EnableTraces ();
	// Uncomment to enable PCAP tracing
	//p2ph.EnablePcapAll(output); 


	Simulator::Stop(Seconds(simTime));
	Simulator::Run();


	/*GtkConfigStore config;
  config.ConfigureAttriutes();*/

	Simulator::Destroy();
	return 0;

}

