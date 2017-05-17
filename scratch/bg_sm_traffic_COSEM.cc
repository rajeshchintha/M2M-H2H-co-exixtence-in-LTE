/*
 * This file is made by combining bg_sm_traffic_.cc & cosemLte.cc from src/cosem/examples by RajeshChintha on 17Jan2017
 * Objective is to get KPI's for SM & Background traffic comination 
 * Here SM is installed with LTE and COSEM stacks; Bg's are left unchanged as in bg_sm_traffic.cc
 * As in bg_sm_traffic.cc, this also won't have DC; MDM is installed with COSEM instead of MMS
 */

//If you want to have .csv files for background nodes go to src/gen-udp/model/general-udp-client.cc & src/gen-udp/model/general-udp-server.cc and uncoment the corresponding block comments in GeneralUdpClient::StartApplication (void) & GeneralUdpServer::StartApplication (void) respectively

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

#include "ns3/lte-ue-rrc.h"
#include "ns3/general-udp-client-server-helper.h"
#include "ns3/packet-sink.h"
#include "ns3/http-module.h"
//For COSEM ; added by me
#include "ns3/udp-cosem-client-helper.h"
#include "ns3/udp-cosem-server-helper.h"

using namespace ns3;

/**
 * Sample simulation script for LTE+EPC. It instantiates several eNodeB,
 * attaches one UE per eNodeB starts a flow for each UE to  and from a remote host.
 * It also  starts yet another flow between each UE pair.
 */

NS_LOG_COMPONENT_DEFINE ("CosemLteTrafficExample");

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
//**************************************************************************************************
       //These no's are taken from tgpham 80/20 traffic mix experiment pg:82
	uint16_t numberOfNodes = 76;//Number of SMs 
	uint16_t numberOfBgNodes = 20;//Number of BG nodes 10:Voice=3;Video=3;Gaming=3;Http=0;Ftp=1;
//*************************************************************************************************
	double simTime = 100.0;
	double distance = 800.0;
	double requestInterval = 1;
	std::string output = "bg_sm_traffic";
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
//Use this if u want more info:
/*
       $ export 'NS_LOG=CosemApplicationLayerClient=level_all|prefix_func:CosemApplicationLayerServer=level_all|        
        prefix_func:CosemApplicationsProcessClient=level_all|prefix_func:CosemApplicationsProcessServer=level_all|      
        prefix_func:UdpCosemWrapperLayerClient=level_all|prefix_func:UdpCosemWrapperLayerServer=level_all|
        prefix_func:UdpCosemServerHelper=level_all|prefix_func'

*/
      LogComponentEnable ("CosemApplicationLayerClient", LOG_LEVEL_INFO);
      LogComponentEnable ("CosemApplicationLayerServer", LOG_LEVEL_INFO);
      LogComponentEnable ("CosemApplicationsProcessClient", LOG_LEVEL_INFO);
      LogComponentEnable ("CosemApplicationsProcessServer", LOG_LEVEL_INFO);
      LogComponentEnable ("UdpCosemWrapperLayerClient", LOG_LEVEL_INFO);
      LogComponentEnable ("UdpCosemWrapperLayerServer", LOG_LEVEL_INFO);
      LogComponentEnable ("UdpCosemServerHelper", LOG_LEVEL_INFO);
		
		LogComponentEnable ("CosemLteTrafficExample", LOG_LEVEL_INFO);

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
	lteVideoUeContainer.Create((float)0.3*numberOfBgNodes);

	NodeContainer lteGamingUeContainer;
	lteGamingUeContainer.Create((float)0.3*numberOfBgNodes);

	NodeContainer lteHttpUeContainer;
	lteHttpUeContainer.Create((float)0*numberOfBgNodes);//removed httpUe because not compiling, instead increased the other (3+3+3+1)

//	NodeContainer lteDcUeContainer;
//	lteDcUeContainer.Create(numberOfDc);

	NodeContainer lteFtpUeContainer;
	lteFtpUeContainer.Create(numberOfBgNodes-lteVoiceUeContainer.GetN()
	-lteVideoUeContainer.GetN()-lteGamingUeContainer.GetN()-lteHttpUeContainer.GetN());
//*************** Remote hosts *****************
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
//*************Adding a remote host for SM whick acts MDM and contains COSEM client*************
// Create a single RemoteHost
	NodeContainer remoteHostContainer;
	remoteHostContainer.Create (1);//COSEM client where as ue are COSEM servers
//***************************************************************
	//Get the Node pointers
	Ptr<Node> lteVoiceRemoteNode = lteVoiceRemoteContainer.Get(0);
	Ptr<Node> lteVideoRemoteNode = lteVideoRemoteContainer.Get(0);
	Ptr<Node> lteGamingRemoteNode = lteGamingRemoteContainer.Get(0);
	Ptr<Node> lteFtpRemoteNode = lteFtpRemoteContainer.Get(0);
//****************pointer for SM remote host*************************
	Ptr<Node> remoteHost = remoteHostContainer.Get (0);
	InternetStackHelper internet;
	internet.Install (remoteHostContainer);
//******************************************************************
	// Install Internet stack on all NodeContainers

	//InternetStackHelper internet;//Commented by me 
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
//****************Internet for sm remote host******************
	NetDeviceContainer internetDevices = p2ph.Install (pgw, remoteHost);
	Ipv4AddressHelper ipv4h;
	ipv4h.SetBase ("1.0.0.0", "255.0.0.0");
	Ipv4InterfaceContainer internetIpIfaces = ipv4h.Assign (internetDevices);
	// For COSEM
	Ipv4InterfaceContainer clientLteInterfaces;
	clientLteInterfaces.Add (ipv4h.Assign (internetDevices.Get (1)));
	
//***********************************************************
 
	Ipv4AddressHelper ipv4hVoice;
	ipv4hVoice.SetBase ("2.0.0.0", "255.0.0.0");
	Ipv4InterfaceContainer lteVoiceRemoteInterface = ipv4hVoice.Assign (lteVoiceRemoteDevice);
	Ipv4Address lteVoiceRemoteAddress = lteVoiceRemoteInterface.GetAddress (1);//commented by me

	Ipv4AddressHelper ipv4hVideo;
	ipv4hVideo.SetBase ("3.0.0.0", "255.0.0.0");
	Ipv4InterfaceContainer lteVideoRemoteInterface = ipv4hVideo.Assign (lteVideoRemoteDevice);
//	Ipv4Address lteVideoRemoteAddress = lteVideoRemoteInterface.GetAddress (1);//commented by me

	Ipv4AddressHelper ipv4hGaming;
	ipv4hGaming.SetBase ("4.0.0.0", "255.0.0.0");
	Ipv4InterfaceContainer lteGamingRemoteInterface = ipv4hGaming.Assign (lteGamingRemoteDevice);
//	Ipv4Address lteGamingRemoteAddress = lteGamingRemoteInterface.GetAddress (1);//commented by me

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
//****************Routing for SM remote host********************
	//Ipv4StaticRoutingHelper ipv4RoutingHelper;
	Ptr<Ipv4StaticRouting> remoteHostStaticRouting = ipv4RoutingHelper.GetStaticRouting (remoteHost->GetObject<Ipv4> ());
	remoteHostStaticRouting->AddNetworkRouteTo (Ipv4Address ("7.0.0.0"), Ipv4Mask ("255.0.0.0"), 1);
//**************************************************************

//*******************************Upto here we have created remote hosts and backgroud Ues and installed Ipv4 and made p2p links between pgw and remote hosts**************************************************************************************************************************
	NodeContainer enbNodes;
	NodeContainer ueNodes;//for SM nodes added by me
	enbNodes.Create(1);
	ueNodes.Create(numberOfNodes);//for SM nodes added by me

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
//***************Mobily model for SM ues ******************

	uemobility.Install(ueNodes);
//*********************************************************

	// Install LTE Devices to the nodes
	NetDeviceContainer enbLteDevs = lteHelper->InstallEnbDevice (enbNodes);
	NetDeviceContainer lteVoiceUeDevice = lteHelper->InstallUeDevice (lteVoiceUeContainer);
	NetDeviceContainer lteVideoUeDevice = lteHelper->InstallUeDevice (lteVideoUeContainer);
	NetDeviceContainer lteGamingUeDevice = lteHelper->InstallUeDevice (lteGamingUeContainer);
	NetDeviceContainer lteHttpUeDevice = lteHelper->InstallUeDevice (lteHttpUeContainer);
	NetDeviceContainer lteFtpUeDevice = lteHelper->InstallUeDevice (lteFtpUeContainer);
//****************Install LTE Devices to SM ue nodes**********
NetDeviceContainer ueLteDevs = lteHelper->InstallUeDevice (ueNodes);
//************************************************************

	// Install the IP stack on the UEs
	internet.Install(lteVoiceUeContainer);
	internet.Install(lteVideoUeContainer);
	internet.Install(lteGamingUeContainer);
	internet.Install(lteHttpUeContainer);
	internet.Install(lteFtpUeContainer);
	internet.Install (ueNodes);// for SM ue nodes abbded by me


	Ipv4InterfaceContainer lteVoiceUeInterface;
	Ipv4InterfaceContainer lteVideoUeInterface;
	Ipv4InterfaceContainer lteGamingUeInterface;
	Ipv4InterfaceContainer lteHttpUeInterface;
	Ipv4InterfaceContainer lteFtpUeInterface;
	Ipv4InterfaceContainer ueIpIface;//For SM ue nodes added by me
	lteVoiceUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteVoiceUeDevice));
	lteVideoUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteVideoUeDevice));
	lteGamingUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteGamingUeDevice));
	lteHttpUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteHttpUeDevice));
//	lteDcUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteDcUeDevice));
	lteFtpUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteFtpUeDevice));
	ueIpIface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (ueLteDevs));//For SM ue nodes added by me

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

//************Assign IP address to UEs, and install applications*****
//************For SM Ue nodes ***************************************
for (uint32_t u = 0; u < ueNodes.GetN (); ++u)
	{
		Ptr<Node> ueNode = ueNodes.Get (u);
		// Set the default gateway for the UE
		Ptr<Ipv4StaticRouting> ueStaticRouting = ipv4RoutingHelper.GetStaticRouting (ueNode->GetObject<Ipv4> ());
		ueStaticRouting->SetDefaultRoute (epcHelper->GetUeDefaultGatewayAddress (), 1);
	}
//*******************************************************************
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
//**********Attach SM Ues to eNodeB*********************************

for (uint16_t i = 0; i < numberOfNodes; i++)
	{
		lteHelper->Attach (ueLteDevs.Get(i), enbLteDevs.Get(0));
                
	}
//*************************Replaced MMS with COSEM*****************************************

 // Cosem Application

  UdpCosemServerHelper cosemServer (ueIpIface);

  ApplicationContainer serverApps = cosemServer.Install (ueNodes);
  serverApps.Start (Seconds (0.1));
  serverApps.Stop (Seconds (10.0));//10.0));

  UdpCosemClientHelper cosemClient (serverApps, clientLteInterfaces, Seconds (5.0));//requestInterval));//5.0));//modified by me

  ApplicationContainer clientApps = cosemClient.Install (remoteHost);
  clientApps.Start (Seconds (0.2));
  clientApps.Stop (Seconds (10.0));
//***********************************This bllock is commented for debug by me*************************************


//  ------------------------------------------------------------------------------------------------------------


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
	lteVoiceRemoteApp.Start (Seconds (11.0));//0.1));
	lteVoiceRemoteApp.Stop (Seconds (100.0));
/* commented by me
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

*///commented by me

/*	// ------------------------------------------------------------------------------------------------
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
std::cout << "Upto here it is fine!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "<<"\n" ;
		ApplicationContainer clientApps = httpClient.Install (lteHttpUeContainer.Get(i));

		clientApps.Start (Seconds (0.2));
		clientApps.Stop (Seconds (1000));
	}

*/
/*  commented by me

//	lteHelper->EnableTraces ();//For PDCP stats refer to lena-simple-epc.cc by RajeshChintha 2Jan2017

	//LTE QoS bearer            //Added by RajeshChintha from scrach/lte-example-sim.cc
//       	EpsBearer bearer (EpsBearer::NGBR_VOICE_VIDEO_GAMING);//Added by RajeshChintha from scrach/lte-example-sim.cc

//for (uint16_t i = 0; i < lteVoiceUeContainer.GetN(); i++)
//	{
//		lteHelper->ActivateDedicatedEpsBearer (lteVoiceUeDevice.Get(i), bearer, EpcTft::Default ()); 

		//		if (ueLteDevs.Get(i)->GetObject<LteUeNetDevice> ()->GetRrc()->GetState() == LteUeRrc::CONNECTED_NORMALLY)
		//		{
		//
		//		}
//	}
    	 
*///Up to here commented by me


//lteHelper->EnableTraces ();//added by me; copied from just above EpsBearer
lteHelper->EnablePdcpTraces ();//Copied from https://www.nsnam.org/docs/models/html/lte-user.html#basic-simulation-program   by me

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

