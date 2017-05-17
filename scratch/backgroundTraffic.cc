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
#include "ns3/mms-client-helper.h"
#include "ns3/mms-server-helper.h"
#include "ns3/lte-ue-rrc.h"
#include "ns3/general-udp-client-server-helper.h"
#include "ns3/packet-sink.h"
#include "ns3/http-module.h"

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
	Config::SetDefault ("ns3::TcpSocket::SegmentSize",UintegerValue(1404));
	Config::SetDefault ("ns3::TcpSocket::ConnTimeout",TimeValue(Seconds (0.5)));
	Config::SetDefault ("ns3::TcpSocket::RcvBufSize", UintegerValue(90000000));
	Config::SetDefault ("ns3::TcpSocket::SndBufSize", UintegerValue(90000000));
	//Config::SetDefault ("ns3::TcpL4Protocol::SocketType", TypeIdValue(TypeId::LookupByName("ns3::TcpTahoe")));//commented by me
	Config::SetDefault ("ns3::TcpL4Protocol::SocketType", TypeIdValue(TypeId::LookupByName("ns3::TcpNewReno")));//added by me, another type of congestion control
	

	bool verbose = true;  // For MMS

	//uint16_t x =10;
	uint16_t numberOfNodes = 2;//Number of SMs
	uint16_t numberOfBgNodes = 10;//Number of BG nodes :Voice=3;Video=2;Gaming=2;Http=2;Ftp=1;
	double simTime = 10.0;//changed to 10 from 100
	double distance = 800.0;
	double requestInterval = 1;
	std::string output = "back-ground";
	int type = 0;
	int mode = 0;
	// Command line arguments
	CommandLine cmd;
	cmd.AddValue("numberOfNodes", "Number of SMs", numberOfNodes);
	cmd.AddValue("numberOfBgNodes", "Number of BG nodes",numberOfBgNodes);
	cmd.AddValue("simTime", "Total duration of the simulation [s])", simTime);
	cmd.AddValue("distance", "Distance between eNBs [m]", distance);
	cmd.AddValue("type", "Type of Requesting", type); // 0 = multicast, 1 = sequential, 2 = reporting
	cmd.AddValue("mode", "Mode 0 = LTE; 1 = Hybrid", mode); //
	cmd.AddValue("requestInterval", "Time between two requests [s]", requestInterval);
	cmd.AddValue("outputfile", "File name of the output trace", output);

	cmd.Parse(argc, argv);
//std::cout << "Upto here it is fine!"<<"\n" ; 

	// For MMS
	if (verbose)
	{
		LogComponentEnable ("MmsAdaptClient", LOG_LEVEL_INFO);
//		LogComponentEnable ("MmsAdaptServer", LOG_LEVEL_INFO);
		LogComponentEnable ("MmsClient", LOG_LEVEL_INFO);
		LogComponentEnable ("MmsServer", LOG_LEVEL_INFO);
//		LogComponentEnable ("CotpClient", LOG_LEVEL_INFO);
//		LogComponentEnable ("CotpServer", LOG_LEVEL_INFO);
//		LogComponentEnable ("MmsServerHelper", LOG_LEVEL_INFO);
//		LogComponentEnable ("TcpTahoe", LOG_LEVEL_LOGIC);
//		LogComponentEnable ("TcpSocketBase", LOG_LEVEL_LOGIC);
//		LogComponentEnable ("RttEstimator", LOG_LEVEL_DEBUG);
//		LogComponentEnable ("LteEnbRrc", LOG_LEVEL_INFO);
//		LogComponentEnable ("LteRlcUm", LOG_LEVEL_INFO);
		LogComponentEnable ("MmsLteExample", LOG_LEVEL_INFO);
//
//		LogComponentEnable ("LteUeMac", LOG_LEVEL_INFO);
//		LogComponentEnable ("LteRlcUm", LOG_LEVEL_LOGIC);
//
//		LogComponentEnable ("RrFfMacScheduler", LOG_LEVEL_INFO);
//		LogComponentEnable ("HttpClient", LOG_LEVEL_ALL);
//		LogComponentEnable ("HttpServer", LOG_LEVEL_ALL);
//		LogComponentEnable ("HttpController", LOG_LEVEL_ALL);
	}

	Ptr<LteHelper> lteHelper = CreateObject<LteHelper> (); 
	//Ptr<EpcHelper> epcHelper = CreateObject<EpcHelper> (); //commented by me
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

	NodeContainer lteVoiceUeContainer;
	lteVoiceUeContainer.Create((float)0.3*numberOfBgNodes);

	NodeContainer lteVideoUeContainer;
	lteVideoUeContainer.Create((float)0.2*numberOfBgNodes);

	NodeContainer lteGamingUeContainer;
	lteGamingUeContainer.Create((float)0.2*numberOfBgNodes);

	NodeContainer lteHttpUeContainer;
	lteHttpUeContainer.Create((float)0.2*numberOfBgNodes);

//	NodeContainer lteDcUeContainer;
//	lteDcUeContainer.Create(numberOfDc);

	NodeContainer lteFtpUeContainer;
	lteFtpUeContainer.Create(numberOfBgNodes-lteVoiceUeContainer.GetN()
	-lteVideoUeContainer.GetN()-lteGamingUeContainer.GetN()-lteHttpUeContainer.GetN());

	NodeContainer lteVoiceRemoteContainer;
	lteVoiceRemoteContainer.Create(1);

	NodeContainer lteVideoRemoteContainer;
	lteVideoRemoteContainer.Create(1);

	NodeContainer lteGamingRemoteContainer;
	lteGamingRemoteContainer.Create(1);

	NodeContainer lteFtpRemoteContainer;
	lteFtpRemoteContainer.Create(1);

	NodeContainer lteHttpRemoteContainer;
	lteHttpRemoteContainer.Create(lteHttpUeContainer.GetN());

	//Get the Node pointers
	Ptr<Node> lteVoiceRemoteNode = lteVoiceRemoteContainer.Get(0);
	Ptr<Node> lteVideoRemoteNode = lteVideoRemoteContainer.Get(0);
	Ptr<Node> lteGamingRemoteNode = lteGamingRemoteContainer.Get(0);
	Ptr<Node> lteFtpRemoteNode = lteFtpRemoteContainer.Get(0);


	// Install Internet stack on all NodeContainers

	InternetStackHelper internet;
	internet.Install (lteVoiceRemoteContainer);
	internet.Install (lteVideoRemoteContainer);
	internet.Install (lteGamingRemoteContainer);
	internet.Install (lteFtpRemoteContainer);
	internet.Install (lteHttpRemoteContainer);


	// Create Point to Point connections between P-GW and all remote hosts
	PointToPointHelper p2ph;
	p2ph.SetDeviceAttribute ("DataRate", DataRateValue (DataRate ("100Gb/s")));
	p2ph.SetDeviceAttribute ("Mtu", UintegerValue (1500));
	p2ph.SetChannelAttribute ("Delay", TimeValue (Seconds (0.010)));

	NetDeviceContainer lteVoiceRemoteDevice = p2ph.Install (pgw, lteVoiceRemoteNode);
	NetDeviceContainer lteVideoRemoteDevice = p2ph.Install (pgw, lteVideoRemoteNode);
	NetDeviceContainer lteGamingRemoteDevice = p2ph.Install (pgw, lteGamingRemoteNode);
	NetDeviceContainer lteFtpRemoteDevice = p2ph.Install (pgw, lteFtpRemoteNode);

	NetDeviceContainer lteHttpRemoteDevice;
	NetDeviceContainer lteHttpPgwDevice;

	for (uint32_t i = 0; i < lteHttpRemoteContainer.GetN(); i++)
	{
		NetDeviceContainer nd = p2ph.Install (pgw, lteHttpRemoteContainer.Get(i));
		lteHttpPgwDevice.Add(nd.Get(0));
		lteHttpRemoteDevice.Add(nd.Get(1));
	}
 
	Ipv4AddressHelper ipv4hVoice;
	ipv4hVoice.SetBase ("2.0.0.0", "255.0.0.0");
	Ipv4InterfaceContainer lteVoiceRemoteInterface = ipv4hVoice.Assign (lteVoiceRemoteDevice);
	Ipv4Address lteVoiceRemoteAddress = lteVoiceRemoteInterface.GetAddress (1);

	Ipv4AddressHelper ipv4hVideo;
	ipv4hVideo.SetBase ("3.0.0.0", "255.0.0.0");
	Ipv4InterfaceContainer lteVideoRemoteInterface = ipv4hVideo.Assign (lteVideoRemoteDevice);
	Ipv4Address lteVideoRemoteAddress = lteVideoRemoteInterface.GetAddress (1);

	Ipv4AddressHelper ipv4hGaming;
	ipv4hGaming.SetBase ("4.0.0.0", "255.0.0.0");
	Ipv4InterfaceContainer lteGamingRemoteInterface = ipv4hGaming.Assign (lteGamingRemoteDevice);
	Ipv4Address lteGamingRemoteAddress = lteGamingRemoteInterface.GetAddress (1);

	Ipv4AddressHelper ipv4hFtp;
	ipv4hFtp.SetBase ("5.0.0.0", "255.0.0.0");
	Ipv4InterfaceContainer lteFtpRemoteInterface = ipv4hFtp.Assign (lteFtpRemoteDevice);
//	Ipv4Address lteFtpRemoteAddress = lteFtpRemoteInterface.GetAddress (1);

	Ipv4AddressHelper ipv4hHttp;
	ipv4hHttp.SetBase ("10.1.1.0", "255.255.255.0");
	Ipv4InterfaceContainer lteHttpRemoteInterface;
	Ipv4InterfaceContainer lteHttpPgwInterface;
	for (uint32_t i = 0; i < lteHttpRemoteDevice.GetN(); i++)
	{
		lteHttpPgwInterface.Add(ipv4hHttp.Assign(lteHttpPgwDevice.Get(i)));
		lteHttpRemoteInterface.Add(ipv4hHttp.Assign(lteHttpRemoteDevice.Get(i)));
		ipv4hHttp.NewNetwork();
	}

	// Install needed routing information from remote hosts to UEs
	Ipv4StaticRoutingHelper ipv4RoutingHelper;

	Ptr<Ipv4StaticRouting> lteVoiceRemoteStaticRouting = ipv4RoutingHelper.GetStaticRouting (lteVoiceRemoteNode->GetObject<Ipv4> ());
	lteVoiceRemoteStaticRouting->AddNetworkRouteTo (Ipv4Address ("7.0.0.0"), Ipv4Mask ("255.0.0.0"), 1);

	Ptr<Ipv4StaticRouting> lteVideoRemoteStaticRouting = ipv4RoutingHelper.GetStaticRouting (lteVideoRemoteNode->GetObject<Ipv4> ());
	lteVideoRemoteStaticRouting->AddNetworkRouteTo (Ipv4Address ("7.0.0.0"), Ipv4Mask ("255.0.0.0"), 1);

	Ptr<Ipv4StaticRouting> lteGamingRemoteStaticRouting = ipv4RoutingHelper.GetStaticRouting (lteGamingRemoteNode->GetObject<Ipv4> ());
	lteGamingRemoteStaticRouting->AddNetworkRouteTo (Ipv4Address ("7.0.0.0"), Ipv4Mask ("255.0.0.0"), 1);

	Ptr<Ipv4StaticRouting> lteFtpRemoteStaticRouting = ipv4RoutingHelper.GetStaticRouting (lteFtpRemoteNode->GetObject<Ipv4> ());
	lteFtpRemoteStaticRouting->AddNetworkRouteTo (Ipv4Address ("7.0.0.0"), Ipv4Mask ("255.0.0.0"), 1);

	for (uint32_t i = 0; i < lteHttpRemoteContainer.GetN(); i++)
	{
		Ptr<Ipv4StaticRouting> lteHttpRemoteStaticRouting = ipv4RoutingHelper.GetStaticRouting (lteHttpRemoteContainer.Get(i)->GetObject<Ipv4> ());
		lteHttpRemoteStaticRouting->AddNetworkRouteTo (Ipv4Address ("7.0.0.0"), Ipv4Mask ("255.0.0.0"), 1);
	}

	NodeContainer enbNodes;
	enbNodes.Create(1);

	// Install Mobility Model
	// Position of eNodeB
	Ptr<ListPositionAllocator> positionAlloc = CreateObject<ListPositionAllocator> ();
	positionAlloc->Add (Vector (distance, distance, 0.0));
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


	//	uemobility.SetPositionAllocator ("ns3::RandomRectanglePositionAllocator",
	//	                                 "X", StringValue ("ns3::UniformRandomVariable[Min=0.0|Max=200.0]"),"Y", StringValue ("ns3::UniformRandomVariable[Min=0.0|Max=200.0]"));


	uemobility.SetMobilityModel ("ns3::ConstantPositionMobilityModel");
	uemobility.Install(lteVoiceUeContainer);
	uemobility.Install(lteVideoUeContainer);
	uemobility.Install(lteGamingUeContainer);
	uemobility.Install(lteHttpUeContainer);
	uemobility.Install(lteFtpUeContainer);

	// Install LTE Devices to the nodes
	NetDeviceContainer enbLteDevs = lteHelper->InstallEnbDevice (enbNodes);
	NetDeviceContainer lteVoiceUeDevice = lteHelper->InstallUeDevice (lteVoiceUeContainer);
	NetDeviceContainer lteVideoUeDevice = lteHelper->InstallUeDevice (lteVideoUeContainer);
	NetDeviceContainer lteGamingUeDevice = lteHelper->InstallUeDevice (lteGamingUeContainer);
	NetDeviceContainer lteHttpUeDevice = lteHelper->InstallUeDevice (lteHttpUeContainer);
	NetDeviceContainer lteFtpUeDevice = lteHelper->InstallUeDevice (lteFtpUeContainer);

	// Install the IP stack on the UEs
	internet.Install(lteVoiceUeContainer);
	internet.Install(lteVideoUeContainer);
	internet.Install(lteGamingUeContainer);
	internet.Install(lteHttpUeContainer);
	internet.Install(lteFtpUeContainer);


	Ipv4InterfaceContainer lteVoiceUeInterface;
	Ipv4InterfaceContainer lteVideoUeInterface;
	Ipv4InterfaceContainer lteGamingUeInterface;
	Ipv4InterfaceContainer lteHttpUeInterface;
	Ipv4InterfaceContainer lteFtpUeInterface;
	lteVoiceUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteVoiceUeDevice));
	lteVideoUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteVideoUeDevice));
	lteGamingUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteGamingUeDevice));
	lteHttpUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteHttpUeDevice));
//	lteDcUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteDcUeDevice));
	lteFtpUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteFtpUeDevice));

	// Assign IP address to UEs, and install applications

	for (uint32_t u = 0; u < lteVoiceUeContainer.GetN (); ++u)
	{
		Ptr<Node> ueNode = lteVoiceUeContainer.Get (u);
		Ptr<MobilityModel> mm = ueNode->GetObject<MobilityModel>();
		NS_LOG_INFO("Voice UE Node position " << u << " " << mm->GetPosition().x << " " << mm->GetPosition().y);

		// Set the default gateway for the UE
		Ptr<Ipv4StaticRouting> ueStaticRouting = ipv4RoutingHelper.GetStaticRouting (ueNode->GetObject<Ipv4> ());
		ueStaticRouting->SetDefaultRoute (epcHelper->GetUeDefaultGatewayAddress (), 1);
	}

	for (uint32_t u = 0; u < lteVideoUeContainer.GetN (); ++u)
	{
		Ptr<Node> ueNode = lteVideoUeContainer.Get (u);
		Ptr<MobilityModel> mm = ueNode->GetObject<MobilityModel>();
		NS_LOG_INFO("Video UE Node position " << u << " " << mm->GetPosition().x << " " << mm->GetPosition().y);

		// Set the default gateway for the UE
		Ptr<Ipv4StaticRouting> ueStaticRouting = ipv4RoutingHelper.GetStaticRouting (ueNode->GetObject<Ipv4> ());
		ueStaticRouting->SetDefaultRoute (epcHelper->GetUeDefaultGatewayAddress (), 1);
	}
	for (uint32_t u = 0; u < lteGamingUeContainer.GetN (); ++u)
	{
		Ptr<Node> ueNode = lteGamingUeContainer.Get (u);
		Ptr<MobilityModel> mm = ueNode->GetObject<MobilityModel>();
		NS_LOG_INFO("Gaming UE Node position " << u << " " << mm->GetPosition().x << " " << mm->GetPosition().y);

		// Set the default gateway for the UE
		Ptr<Ipv4StaticRouting> ueStaticRouting = ipv4RoutingHelper.GetStaticRouting (ueNode->GetObject<Ipv4> ());
		ueStaticRouting->SetDefaultRoute (epcHelper->GetUeDefaultGatewayAddress (), 1);
	}
	for (uint32_t u = 0; u < lteFtpUeContainer.GetN (); ++u)
	{
		Ptr<Node> ueNode = lteFtpUeContainer.Get (u);
		Ptr<MobilityModel> mm = ueNode->GetObject<MobilityModel>();
		NS_LOG_INFO("FTP UE Node position " << u << " " << mm->GetPosition().x << " " << mm->GetPosition().y);

		// Set the default gateway for the UE
		Ptr<Ipv4StaticRouting> ueStaticRouting = ipv4RoutingHelper.GetStaticRouting (ueNode->GetObject<Ipv4> ());
		ueStaticRouting->SetDefaultRoute (epcHelper->GetUeDefaultGatewayAddress (), 1);
	}
	for (uint32_t u = 0; u < lteHttpUeContainer.GetN (); ++u)
	{
		Ptr<Node> ueNode = lteHttpUeContainer.Get (u);
		Ptr<MobilityModel> mm = ueNode->GetObject<MobilityModel>();
		NS_LOG_INFO("HTTP UE Node position " << u << " " << mm->GetPosition().x << " " << mm->GetPosition().y);

		// Set the default gateway for the UE
		Ptr<Ipv4StaticRouting> ueStaticRouting = ipv4RoutingHelper.GetStaticRouting (ueNode->GetObject<Ipv4> ());
		ueStaticRouting->SetDefaultRoute (epcHelper->GetUeDefaultGatewayAddress (), 1);
	}

	// Attach UEs to eNodeB

//	for (uint16_t i = 0; i < lteDcUeContainer.GetN(); i++)
//	{
//		lteHelper->Attach (lteDcUeDevice.Get(i), enbLteDevs.Get(0));
//		//		if (ueLteDevs.Get(i)->GetObject<LteUeNetDevice> ()->GetRrc()->GetState() == LteUeRrc::CONNECTED_NORMALLY)
//		//		{
//		//
//	}

	for (uint16_t i = 0; i < lteVoiceUeContainer.GetN(); i++)
	{
		lteHelper->Attach (lteVoiceUeDevice.Get(i), enbLteDevs.Get(0));
		//		if (ueLteDevs.Get(i)->GetObject<LteUeNetDevice> ()->GetRrc()->GetState() == LteUeRrc::CONNECTED_NORMALLY)
		//		{
		//
		//		}
	}

	for (uint16_t i = 0; i < lteVideoUeContainer.GetN(); i++)
	{
		lteHelper->Attach (lteVideoUeDevice.Get(i), enbLteDevs.Get(0));
		//		if (ueLteDevs.Get(i)->GetObject<LteUeNetDevice> ()->GetRrc()->GetState() == LteUeRrc::CONNECTED_NORMALLY)
		//		{
		//
		//		}
	}

	for (uint16_t i = 0; i < lteGamingUeContainer.GetN(); i++)
	{
		lteHelper->Attach (lteGamingUeDevice.Get(i), enbLteDevs.Get(0));
		//		if (ueLteDevs.Get(i)->GetObject<LteUeNetDevice> ()->GetRrc()->GetState() == LteUeRrc::CONNECTED_NORMALLY)
		//		{
		//
		//		}
	}

	for (uint16_t i = 0; i < lteHttpUeContainer.GetN(); i++)
	{
		lteHelper->Attach (lteHttpUeDevice.Get(i), enbLteDevs.Get(0));
		//		if (ueLteDevs.Get(i)->GetObject<LteUeNetDevice> ()->GetRrc()->GetState() == LteUeRrc::CONNECTED_NORMALLY)
		//		{
		//
		//		}
	}

	for (uint16_t i = 0; i < lteFtpUeContainer.GetN(); i++)
	{
		lteHelper->Attach (lteFtpUeDevice.Get(i), enbLteDevs.Get(0));
		//		if (ueLteDevs.Get(i)->GetObject<LteUeNetDevice> ()->GetRrc()->GetState() == LteUeRrc::CONNECTED_NORMALLY)
		//		{
		//
		//		}
	}


	// ------------------------------------------------------------------------------------------------

	// VoIP Application, both UL and DL

	// UPLINK (from UEs) !!!!
	//
	// Create one udpServer applications on node one.
	//
	uint16_t lteVoiceRemotePort = 4000;
	GeneralUdpServerHelper lteVoiceRemoteServer (lteVoiceRemotePort, 3);//VoIP=3
	ApplicationContainer lteVoiceRemoteApp = lteVoiceRemoteServer.Install (lteVoiceRemoteNode);
	lteVoiceRemoteApp.Start (Seconds (0.0));
	lteVoiceRemoteApp.Stop (Seconds (1000.0));

	//
	// Create one VoIPClient application to send UDP datagrams from UE nodes to
	// Remote VoIP host.
	//
	GeneralUdpClientHelper client (lteVoiceRemoteAddress, lteVoiceRemotePort, 3); //0 = video; 1 = gaming uplink
	lteVoiceRemoteApp = client.Install (lteVoiceUeContainer);
	lteVoiceRemoteApp.Start (Seconds (0.1));
	lteVoiceRemoteApp.Stop (Seconds (100.0));

	// DOWNLINK (to UEs) !!!!
	//
	// Create udpServer applications on UE nodes.
	//
	uint16_t lteVoiceUePort = 4000;
	GeneralUdpServerHelper lteVoiceUeServer (lteVoiceUePort, 3);
	ApplicationContainer lteVoiceUeApp = lteVoiceUeServer.Install (lteVoiceUeContainer);
	lteVoiceUeApp.Start (Seconds (0.0));
	lteVoiceUeApp.Stop (Seconds (1000.0));

	//
	// Create one VoIPClient application to send UDP datagrams from Remote Host to
	// VoIP UEs.
	//
	for (uint32_t i = 0; i < lteVoiceUeInterface.GetN(); i++)
	{
		GeneralUdpClientHelper remoteClient (lteVoiceUeInterface.GetAddress(i), lteVoiceUePort, 3); //0 = video; 1 = gaming uplink
		lteVoiceRemoteApp = remoteClient.Install (lteVoiceRemoteNode);
		lteVoiceRemoteApp.Start (Seconds (0.1));
		lteVoiceRemoteApp.Stop (Seconds (100.0));
	}


	// ------------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------------

	// Gaming Application, both UL and DL

	// UPLINK (from UEs) !!!!
	//
	// Create one udpServer applications on node one.
	//
	uint16_t lteGamingRemotePort = 5000;
	GeneralUdpServerHelper lteGamingRemoteServer (lteGamingRemotePort, 1);//Gaming uplink=1; Gaming downlink=2
	ApplicationContainer lteGamingUeApp = lteGamingRemoteServer.Install (lteGamingRemoteNode);
	lteGamingUeApp.Start (Seconds (0.0));
	lteGamingUeApp.Stop (Seconds (1000.0));

	//
	// Create one VoIPClient application to send UDP datagrams from UE nodes to
	// Remote VoIP host.
	//
	GeneralUdpClientHelper gamingClientUe (lteGamingRemoteAddress, lteGamingRemotePort, 1); //0 = video; 1 = gaming uplink
	lteGamingUeApp = gamingClientUe.Install (lteGamingUeContainer);
	lteGamingUeApp.Start (Seconds (0.1));
	lteGamingUeApp.Stop (Seconds (100.0));

	// DOWNLINK (to UEs) !!!!
	//
	// Create udpServer applications on UE nodes.
	//
	uint16_t lteGamingUePort = 5000;
	GeneralUdpServerHelper lteGamingUeServer (lteGamingUePort, 2);
	ApplicationContainer lteGamingRemoteApp = lteGamingUeServer.Install (lteGamingUeContainer);
	lteGamingRemoteApp.Start (Seconds (0.0));
	lteGamingRemoteApp.Stop (Seconds (1000.0));

	//
	// Create one VoIPClient application to send UDP datagrams from Remote Host to
	// VoIP UEs.
	//
	for (uint32_t i = 0; i < lteGamingUeInterface.GetN(); i++)
	{
		GeneralUdpClientHelper gamingClientRemote (lteGamingUeInterface.GetAddress(i), lteGamingUePort, 2); //0 = video; 1 = gaming uplink
		lteGamingRemoteApp = gamingClientRemote.Install (lteGamingRemoteNode);
		lteGamingRemoteApp.Start (Seconds (0.1));
		lteGamingRemoteApp.Stop (Seconds (100.0));
	}


	// ------------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------------

	// Video Application, both UL and DL

	// UPLINK (from UEs) !!!!
	//
	// Create one udpServer applications on node one.
	//
	uint16_t lteVideoRemotePort = 5000;
	GeneralUdpServerHelper lteVideoRemoteServer (lteVideoRemotePort, 0);//Video=0
	ApplicationContainer lteVideoUeApp = lteVideoRemoteServer.Install (lteVideoRemoteNode);
	lteVideoUeApp.Start (Seconds (0.0));
	lteVideoUeApp.Stop (Seconds (1000.0));

	//
	// Create one VoIPClient application to send UDP datagrams from UE nodes to
	// Remote VoIP host.
	//
	GeneralUdpClientHelper VideoClientUe (lteVideoRemoteAddress, lteVideoRemotePort, 0); //0 = video;
	lteVideoUeApp = VideoClientUe.Install (lteVideoUeContainer);
	lteVideoUeApp.Start (Seconds (0.1));
	lteVideoUeApp.Stop (Seconds (100.0));

	// DOWNLINK (to UEs) !!!!
	//
	// Create udpServer applications on UE nodes.
	//
	uint16_t lteVideoUePort = 5000;
	GeneralUdpServerHelper lteVideoUeServer (lteVideoUePort, 0);
	ApplicationContainer lteVideoRemoteApp = lteVideoUeServer.Install (lteVideoUeContainer);
	lteVideoRemoteApp.Start (Seconds (0.0));
	lteVideoRemoteApp.Stop (Seconds (1000.0));

	//
	// Create one VoIPClient application to send UDP datagrams from Remote Host to
	// VoIP UEs.
	//
	for (uint32_t i = 0; i < lteVideoUeInterface.GetN(); i++)
	{
		GeneralUdpClientHelper VideoClientRemote (lteVideoUeInterface.GetAddress(i), lteVideoUePort, 0); //0 = video; 1 = Video uplink
		lteVideoRemoteApp = VideoClientRemote.Install (lteVideoRemoteNode);
		lteVideoRemoteApp.Start (Seconds (0.1));
		lteVideoRemoteApp.Stop (Seconds (100.0));
	}


	// ------------------------------------------------------------------------------------------------
	// FTP DOWNLINK

	uint16_t port = 9;  // well-known echo port number
	//
	// Create a PacketSinkApplication and install it on node 1
	//
	PacketSinkHelper sink ("ns3::TcpSocketFactory",
			InetSocketAddress (Ipv4Address::GetAny (), port));
	ApplicationContainer sinkApps = sink.Install (lteFtpUeContainer);
	sinkApps.Start (Seconds (0.0));
	sinkApps.Stop (Seconds (1000.0));

	//
	// Create a BulkSendApplication and install it on remote FTP hosts
	//

	for (uint32_t i = 0; i < lteFtpUeInterface.GetN(); i++)
	{
		BulkSendHelper source ("ns3::TcpSocketFactory", InetSocketAddress (lteFtpUeInterface.GetAddress(i), port));
		ApplicationContainer sourceApps = source.Install (lteFtpRemoteNode);
		sourceApps.Start (Seconds (0.1));
		sourceApps.Stop (Seconds (100.0));
	}


//	// FTP UPLINK
//
//	//	// UPLINK (from UEs) !!!!
//	//
//	// Create a PacketSinkApplication and install it on Remote FTP Host
//	//
//	PacketSinkHelper sinkRemote ("ns3::TcpSocketFactory",
//			InetSocketAddress (Ipv4Address::GetAny (), port));
//	ApplicationContainer sinkAppsRemote = sinkRemote.Install (lteFtpRemoteNode);
//	sinkAppsRemote.Start (Seconds (0.0));
//	sinkAppsRemote.Stop (Seconds (1000.0));
//
//	//
//	// Create a BulkSendApplication and install it on UEs
//	//
//
//	BulkSendHelper sourceUe ("ns3::TcpSocketFactory", InetSocketAddress (lteFtpRemoteAddress, port));
//	ApplicationContainer sourceAppsUe = sourceUe.Install (lteFtpUeContainer);
//	sourceAppsUe.Start (Seconds (0.1));
//	sourceAppsUe.Stop (Seconds (100.0));

///*
	//--------------------------------------------------------------------------------------------
	// HTTP Client and Server

	port = 80;		

	for (uint32_t i = 0; i < lteHttpRemoteContainer.GetN(); i++)
	{
		
		HttpHelper httpHelper;

		HttpServerHelper httpServer;
		httpServer.SetAttribute ("Local", AddressValue (InetSocketAddress (Ipv4Address::GetAny (), port)));
		httpServer.SetAttribute ("HttpController", PointerValue (httpHelper.GetController ()));
		ApplicationContainer serverApps = httpServer.Install (lteHttpRemoteContainer.Get (i));

		serverApps.Start (Seconds (0.0));
		serverApps.Stop (Seconds (1000));

		HttpClientHelper httpClient;
		httpClient.SetAttribute ("Peer", AddressValue (InetSocketAddress (lteHttpRemoteInterface.GetAddress (i), port)));
		httpClient.SetAttribute ("HttpController", PointerValue (httpHelper.GetController ()));
std::cout <<"\n" << "Upto here it is fine!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "<<"\n" ;
		ApplicationContainer clientApps = httpClient.Install (lteHttpUeContainer.Get(i));

		clientApps.Start (Seconds (0.2));
		clientApps.Stop (Seconds (1000));
	}

//*/ This HTTP block is uncommented by me on 7th April 2017

//	lteHelper->EnableTraces ();//For PDCP stats refer to lena-simple-epc.cc by RajeshChintha 2Jan2017

	//LTE QoS bearer            //Added by RajeshChintha from scrach/lte-example.sim.cc
       	EpsBearer bearer (EpsBearer::NGBR_VOICE_VIDEO_GAMING);//Added by RajeshChintha from scrach/lte-example.sim.cc

for (uint16_t i = 0; i < lteVoiceUeContainer.GetN(); i++)
	{
		lteHelper->ActivateDedicatedEpsBearer (lteVoiceUeDevice.Get(i), bearer, EpcTft::Default ()); 

		//		if (ueLteDevs.Get(i)->GetObject<LteUeNetDevice> ()->GetRrc()->GetState() == LteUeRrc::CONNECTED_NORMALLY)
		//		{
		//
		//		}
	}
    	 
lteHelper->EnablePdcpTraces ();//added by me; copied from just above EpsBearer

/* *****************************************Post-processing on the produced simulation output ****************************************
	//This section is added by RajeshChintha on 2Jan2017 
                              
		-------------------Troughput calculation in GNU Octave:  http://lena.cttc.es/manual/lte-user.html
       
	% RxBytes is the 10th column
	DlRxBytes = load ("no-op-DlRlcStats.txt") (:,10);
	DlAverageThroughputKbps = sum (DlRxBytes) * 8 / 1000 / 50;// 8 is for bits conversion; 1000 is Kilo; 50 is for average calculation

                                
                 ----------------------SINR calculation:----------------------------------
	% Sinr is the 6th column
	DlSinr = load ("no-op-DlRsrpSinrStats.txt") (:,6);
	% eliminate NaN values
	idx = isnan (DlSinr);
	DlSinr (idx) = 0;
	DlAverageSinrDb = 10 * log10 (mean (DlSinr)) % convert to dB

***************************************************************************************************************************************/


//	Ipv4GlobalRoutingHelper::PopulateRoutingTables ();

	// Uncomment to enable PCAP tracing
//	p2ph.EnablePcapAll(output);// commented by me *************************************

	Simulator::Stop(Seconds(simTime));
	Simulator::Run();

	Simulator::Destroy();
	return 0;

}

