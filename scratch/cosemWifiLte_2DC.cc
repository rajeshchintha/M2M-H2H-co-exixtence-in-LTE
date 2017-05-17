       
/* This made by changing bg_sm_traffic_COSEM_DC.cc  with the help of cosemDcwifiLte.cc by RajeshChintha on 7th April 2017
 * Objective is to have more than one DC (May be 2) with set of WiFi SMs attached to each of them.
 * Hurray successful!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * Check wifi2dc.out for output

 * Author: Jaume Nin <jaume.nin@cttc.cat>
 *         Juanmalk <jm.aranda121@uniandes.edu.co>      &          rajeshchintha@gmail.com
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
#include "ns3/csma-module.h"
#include "ns3/lte-ue-rrc.h"
#include "ns3/general-udp-client-server-helper.h"
#include "ns3/packet-sink.h"
#include "ns3/http-module.h"
//For COSEM ; added by me
#include "ns3/udp-cosem-client-helper.h"
#include "ns3/udp-cosem-server-helper.h"
#include "ns3/data-concentrator-helper.h"
#include "ns3/demand-response-application-helper.h"
#include "ns3/mdm-application-helper.h"
#include "ns3/flow-monitor-module.h"
#include "ns3/wifi-module.h"
using namespace ns3;

/**
 * Sample simulation script for LTE+EPC. It instantiates several eNodeB,
 * attaches one UE per eNodeB starts a flow for each UE to  and from a remote host.
 * It also  starts yet another flow between each UE pair.
 */
/*  If you want to have 
 


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
	uint16_t nSmNodesDC1 = 10;//Number of SMs 
    uint16_t nSmNodesDC2 = 10;//Number of SMs 
	uint16_t numberOfBgNodes = 10;//Number of BG nodes 10:Voice=3;Video=3;Gaming=3;Http=0;Ftp=1;
    uint16_t nDcNodes = 2;  //  LTE DC nodes
    uint32_t nCcCsma = 4; // Control Center servers (DR)
//*************************************************************************************************
	double simTime = 20.0;
    double reqSmTime = 1.0;
    double reqDcTime = 5.0;
	double distance = 800.0;
	double requestInterval = 1;
	std::string output = "bg_sm_traffic";
	int type = 0;
	int mode = 0;
	// Command line arguments
	CommandLine cmd;
	cmd.AddValue("numberOfSmNodes", "Number of SMs", nSmNodesDC1);
	cmd.AddValue("numberOfBgNodes", "Number of BG nodes",numberOfBgNodes);
    cmd.AddValue("nDcNodes", "Number of dc nodes", nDcNodes);
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
        prefix_func:UdpCosemServerHelper=level_all|prefix_func:DataConcentratorApplication=level_all|prefix_func:MeterDataManagementApplication=level_all|prefix_func:DemandResponseApplication=level_all|prefix_func'

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
//	Ipv4Address lteVoiceRemoteAddress = lteVoiceRemoteInterface.GetAddress (1);//commented by me for debugging on 6th April 12 PM

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
 Ptr<Ipv4StaticRouting> pgwStaticRouting = ipv4RoutingHelper.GetStaticRouting (pgw->GetObject<Ipv4> ());
  pgwStaticRouting->AddNetworkRouteTo (Ipv4Address ("10.1.2.0"), Ipv4Mask ("255.255.255.0"), 2); // Working!!!
//****************Routing for SM remote host********************
	//Ipv4StaticRoutingHelper ipv4RoutingHelper;
	Ptr<Ipv4StaticRouting> remoteHostStaticRouting = ipv4RoutingHelper.GetStaticRouting (remoteHost->GetObject<Ipv4> ());
	remoteHostStaticRouting->AddNetworkRouteTo (Ipv4Address ("7.0.0.0"), Ipv4Mask ("255.0.0.0"), 1);
//**************************************************************

//*******************************Upto here we have created remote hosts and backgroud Ues and installed Ipv4 and made p2p links between pgw and remote hosts**************************************************************************************************************************
	NodeContainer enbNodes;
//	NodeContainer ueNodes;//for SM nodes added by me
    NodeContainer dcNodes;
	enbNodes.Create(1);
//	ueNodes.Create(numberOfSmNodes);//for SM nodes added by me
    dcNodes.Create(nDcNodes);

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

//	uemobility.Install(ueNodes);
    uemobility.Install(dcNodes);
//*********************************************************

	// Install LTE Devices to the nodes
	NetDeviceContainer enbLteDevs = lteHelper->InstallEnbDevice (enbNodes);
	NetDeviceContainer lteVoiceUeDevice = lteHelper->InstallUeDevice (lteVoiceUeContainer);
	NetDeviceContainer lteVideoUeDevice = lteHelper->InstallUeDevice (lteVideoUeContainer);
	NetDeviceContainer lteGamingUeDevice = lteHelper->InstallUeDevice (lteGamingUeContainer);
	NetDeviceContainer lteHttpUeDevice = lteHelper->InstallUeDevice (lteHttpUeContainer);
	NetDeviceContainer lteFtpUeDevice = lteHelper->InstallUeDevice (lteFtpUeContainer);
//****************Install LTE Devices to DC nodes**********
//NetDeviceContainer ueLteDevs = lteHelper->InstallUeDevice (ueNodes);
NetDeviceContainer dcLteDevs = lteHelper->InstallUeDevice (dcNodes);
//************************************************************

	// Install the IP stack on the UEs
	internet.Install(lteVoiceUeContainer);
	internet.Install(lteVideoUeContainer);
	internet.Install(lteGamingUeContainer);
	internet.Install(lteHttpUeContainer);
	internet.Install(lteFtpUeContainer);
//	internet.Install (ueNodes);// for SM ue nodes abbded by me
    internet.Install (dcNodes);// for dc nodes abbded by me

	Ipv4InterfaceContainer lteVoiceUeInterface;
	Ipv4InterfaceContainer lteVideoUeInterface;
	Ipv4InterfaceContainer lteGamingUeInterface;
	Ipv4InterfaceContainer lteHttpUeInterface;
	Ipv4InterfaceContainer lteFtpUeInterface;
//	Ipv4InterfaceContainer ueIpIface;//For SM ue nodes added by me
	Ipv4InterfaceContainer dcIpIface;//For DC nodes added by me

	lteVoiceUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteVoiceUeDevice));
	lteVideoUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteVideoUeDevice));
	lteGamingUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteGamingUeDevice));
	lteHttpUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteHttpUeDevice));
//	lteDcUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteDcUeDevice));
	lteFtpUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteFtpUeDevice));
//	ueIpIface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (ueLteDevs));//For SM ue nodes added by me
    dcIpIface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (dcLteDevs));//For DC nodes added by me

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


//*******************************************************************
//************For DC nodes ***************************************
for (uint32_t u = 0; u < dcNodes.GetN (); ++u)
	{
		Ptr<Node> ueNode = dcNodes.Get (u);
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

//**********Attach DC to eNodeB*********************************

for (uint16_t i = 0; i < nDcNodes; i++)
	{
		lteHelper->Attach (dcLteDevs.Get(i), enbLteDevs.Get(0));
                
	}


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
  Ipv4InterfaceContainer CcCsmaInterfaces;
 for (uint32_t i = 0; i < (CcCsmaNodes.GetN ()); ++i)
     {
        CcCsmaInterfaces.Add (addressCC.Assign (CcCsmaDevices.Get (i)));
     }

   // Set the default gateway (Router) for CC server
  for (uint32_t i = 1; i < CcCsmaNodes.GetN (); ++i)
     {
       Ptr<Ipv4StaticRouting> ccStaticRouting = ipv4RoutingHelper.GetStaticRouting (CcCsmaNodes.Get (i)->GetObject<Ipv4> ());
       ccStaticRouting->SetDefaultRoute (CcCsmaInterfaces.GetAddress (0), 1); // Working!!!
    }
// -------------------------------------------------------------------------------
//  WIFI AMI NETWORK CONFIGURATION
// -------------------------------------------------------------------------------
  NodeContainer wifiStaNodesDC1,wifiStaNodesDC2;
  wifiStaNodesDC1.Create (nSmNodesDC1);
 wifiStaNodesDC2.Create (nSmNodesDC2);
  NodeContainer wifiApNodeDC1 = dcNodes.Get (0); // DC also acts s AP
  NodeContainer wifiApNodeDC2 = dcNodes.Get (1);

  YansWifiChannelHelper channel = YansWifiChannelHelper::Default ();
  YansWifiPhyHelper phyDC1 = YansWifiPhyHelper::Default ();
YansWifiPhyHelper phyDC2 = YansWifiPhyHelper::Default ();
  phyDC1.SetChannel (channel.Create ());
phyDC2.SetChannel (channel.Create ());

  WifiHelper wifiDC1 = WifiHelper ();
  wifiDC1.SetRemoteStationManager ("ns3::AarfWifiManager");
  WifiHelper wifiDC2 = WifiHelper ();
  wifiDC2.SetRemoteStationManager ("ns3::AarfWifiManager");

  NqosWifiMacHelper macDC1 = NqosWifiMacHelper::Default ();
  NqosWifiMacHelper macDC2 = NqosWifiMacHelper::Default ();

  Ssid ssidDC1 = Ssid ("ns-3-ssid_DC1");
   Ssid ssidDC2 = Ssid ("ns-3-ssid_DC2");
  macDC1.SetType ("ns3::StaWifiMac",
               "Ssid", SsidValue (ssidDC1),
               "ActiveProbing", BooleanValue (false));
  macDC2.SetType ("ns3::StaWifiMac",
               "Ssid", SsidValue (ssidDC2),
               "ActiveProbing", BooleanValue (false));

  NetDeviceContainer staDevicesDC1,staDevicesDC2;
  staDevicesDC1 = wifiDC1.Install (phyDC1, macDC1, wifiStaNodesDC1);
  staDevicesDC2 = wifiDC2.Install (phyDC2, macDC2, wifiStaNodesDC2);

  macDC1.SetType ("ns3::ApWifiMac",
               "Ssid", SsidValue (ssidDC1));
 macDC2.SetType ("ns3::ApWifiMac",
               "Ssid", SsidValue (ssidDC2));

  NetDeviceContainer apDevicesDC1, apDevicesDC2;
  apDevicesDC1 = wifiDC1.Install (phyDC1, macDC1, wifiApNodeDC1);
  apDevicesDC2 = wifiDC2.Install (phyDC2, macDC2, wifiApNodeDC2);

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
  mobilityWifi.Install (wifiStaNodesDC1);
  mobilityWifi.Install (wifiStaNodesDC2);

  mobilityWifi.SetMobilityModel ("ns3::ConstantPositionMobilityModel");
  mobilityWifi.Install (wifiApNodeDC1);
  mobilityWifi.Install (wifiApNodeDC2);
  //InternetStackHelper stack;
  //internet.Install (wifiApNode);
  internet.Install (wifiStaNodesDC1);
  internet.Install (wifiStaNodesDC2);

  Ipv4AddressHelper address;
  Ipv4InterfaceContainer serverInterfacesDC1, serverInterfacesDC2;

  address.SetBase ("10.1.3.0", "255.255.255.0");
  // Ipv4InterfaceContainer wifiInterfaces;
  // address.Assign (staDevices);
 
  Ipv4InterfaceContainer dcWifiInterfacesDC1,dcWifiInterfacesDC2;
  dcWifiInterfacesDC1.Add (address.Assign (apDevicesDC1));
  dcWifiInterfacesDC2.Add (address.Assign (apDevicesDC2));

  for (uint32_t i = 0; i < wifiStaNodesDC1.GetN (); ++i)
     {
        serverInterfacesDC1.Add (address.Assign (staDevicesDC1.Get (i)));
     }
for (uint32_t i = 0; i < wifiStaNodesDC2.GetN (); ++i)
     {
        serverInterfacesDC2.Add (address.Assign (staDevicesDC2.Get (i)));
     }
  //Ipv4InterfaceContainer apInterfaces;
  //apInterfaces.Add (address.Assign (apDevices));

//

// -------------------------------------------------------------------------------
//  COSEM & DATA CONCENTRATOR APPLICATIONS CONFIGURATION
// -------------------------------------------------------------------------------

 // Cosem Application

//COSEM Servers for DC1
  UdpCosemServerHelper cosemServerDC1 (serverInterfacesDC1);
  ApplicationContainer serverAppsDC1 = cosemServerDC1.Install (wifiStaNodesDC1);
  serverAppsDC1.Start (Seconds (0.1));
  serverAppsDC1.Stop (Seconds (simTime));//10.0));

//COSEM Servers for DC2
  UdpCosemServerHelper cosemServerDC2 (serverInterfacesDC2);
  ApplicationContainer serverAppsDC2 = cosemServerDC2.Install (wifiStaNodesDC2);
  serverAppsDC2.Start (Seconds (0.1));
  serverAppsDC2.Stop (Seconds (simTime));//10.0));

//COSEM Clients (DCs)

 //DC1
  UdpCosemClientHelper cosemClient (serverAppsDC1, dcWifiInterfacesDC1, Seconds (reqSmTime));//requestInterval));//5.0));//modified by me
  ApplicationContainer clientApps = cosemClient.Install (dcNodes.Get (0));
  clientApps.Start (Seconds (0.2));
  clientApps.Stop (Seconds (simTime));

//DC2
  UdpCosemClientHelper cosemClient2 (serverAppsDC2, dcWifiInterfacesDC2, Seconds (reqSmTime));//requestInterval));//5.0));//modified by me
  ApplicationContainer clientApps2 = cosemClient2.Install (dcNodes.Get (1));
  clientApps2.Start (Seconds (0.3));
  clientApps2.Stop (Seconds (simTime));

// Data Concentrator Application
//DC1
  DataConcentratorApplicationHelper dc (clientApps, CcCsmaInterfaces.GetAddress (1), dcIpIface.GetAddress (0)); //ApplicationContainer clientApps, Address centerIp, Address dcIpAddress
  ApplicationContainer dcApps = dc.Install (dcNodes.Get (0));
  dcApps.Start (Seconds (0.0001));
  dcApps.Stop (Seconds (simTime));
//DC2
  DataConcentratorApplicationHelper dc2 (clientApps2, CcCsmaInterfaces.GetAddress (1), dcIpIface.GetAddress (1)); //ApplicationContainer clientApps, Address centerIp, Address dcIpAddress
  ApplicationContainer dcApps2 = dc2.Install (dcNodes.Get (1));
  dcApps2.Start (Seconds (0.01));
  dcApps2.Stop (Seconds (simTime));

// -------------------------------------------------------------------------------
//  METER DATA MANAGEMENT & DEMAND RESPONSE APPLICATIONS CONFIGURATION 
// -------------------------------------------------------------------------------
 
  // Meter Data Management Application on Control Center
//MD1

  MeterDataManagementApplicationHelper mdmHelper (dcApps, CcCsmaInterfaces.GetAddress (1), Seconds (reqDcTime), reqSmTime);//ApplicationContainer dataConcentratorAppContainer, Address mdmIp, Time interval, uint32_t readingTime
  ApplicationContainer mdmApps = mdmHelper.Install (CcCsmaNodes.Get (1)); 
  mdmApps.Start (Seconds (1.0));
  mdmApps.Stop (Seconds (simTime));
//MD2
  MeterDataManagementApplicationHelper mdmHelper2 (dcApps2, CcCsmaInterfaces.GetAddress (2), Seconds (reqDcTime), reqSmTime);
  ApplicationContainer mdmApps2 = mdmHelper2.Install (CcCsmaNodes.Get (2)); 
  mdmApps2.Start (Seconds (2.0));
  mdmApps2.Stop (Seconds (simTime));

  // Demand Response Application on Control Center (Send demand response signals to Data Cocentrator)
//DR1
  DemandResponseApplicationHelper drHelper (CcCsmaInterfaces.GetAddress (3), mdmApps);
  ApplicationContainer drApps = drHelper.Install (CcCsmaNodes.Get (3));
  drApps.Start (Seconds (1.0));
  drApps.Stop (Seconds (simTime));

//DR2
  DemandResponseApplicationHelper drHelper2 (CcCsmaInterfaces.GetAddress (4), mdmApps2);
  ApplicationContainer drApps2 = drHelper2.Install (CcCsmaNodes.Get (4));
  drApps2.Start (Seconds (2.0));
  drApps2.Stop (Seconds (simTime));


//***********************************This bllock is commented for debug by me*************************************
/* commented by me

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

