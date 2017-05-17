//This File Is Made By Combining Backgroudtraffic.Cc & Mmslte.Cc From Src/Mms/Examples By Rajeshchintha On 6jan2017
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

//NS_LOG_COMPONENT_DEFINE ("MmsLteExample");

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
	

	//bool verbose = true;  // commented by me

	//uint16_t x =10;
	uint16_t numberOfNodes = 1;//Number of SMs
	uint16_t numberOfBgNodes = 0;//Number of BG nodes 10:Voice=3;Video=3;Gaming=3;Http=0;Ftp=1;
	double simTime = 10.0;
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
	lteVoiceUeContainer.Create(numberOfBgNodes);	
//*************** Remote hosts *****************
	NodeContainer lteVoiceRemoteContainer;
	lteVoiceRemoteContainer.Create(1);
	
//*************Adding a remote host for SM whick acts MDM and contains MMS client*************
// Create a single RemoteHost
	NodeContainer remoteHostContainer;
	remoteHostContainer.Create (1);//mms client where as ue are mms servers
//***************************************************************
	//Get the Node pointers
	Ptr<Node> lteVoiceRemoteNode = lteVoiceRemoteContainer.Get(0);	
//****************pointer for SM remote host*************************
	Ptr<Node> remoteHost = remoteHostContainer.Get (0);
	InternetStackHelper internet;
	internet.Install (remoteHostContainer);
//******************************************************************
	// Install Internet stack on all NodeContainers

	//InternetStackHelper internet;//Commented by me 
	internet.Install (lteVoiceRemoteContainer);

	// Create Point to Point connections between P-GW and all remote hosts
	PointToPointHelper p2ph;
	p2ph.SetDeviceAttribute ("DataRate", DataRateValue (DataRate ("100Gb/s")));
	p2ph.SetDeviceAttribute ("Mtu", UintegerValue (1500));
	p2ph.SetChannelAttribute ("Delay", TimeValue (Seconds (0.010)));

	NetDeviceContainer lteVoiceRemoteDevice = p2ph.Install (pgw, lteVoiceRemoteNode);

	
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
	Ipv4Address lteVoiceRemoteAddress = lteVoiceRemoteInterface.GetAddress (1); //Commented by me

	

	

	// Install needed routing information from remote hosts to UEs
	Ipv4StaticRoutingHelper ipv4RoutingHelper;

	Ptr<Ipv4StaticRouting> lteVoiceRemoteStaticRouting = ipv4RoutingHelper.GetStaticRouting (lteVoiceRemoteNode->GetObject<Ipv4> ());
	lteVoiceRemoteStaticRouting->AddNetworkRouteTo (Ipv4Address ("7.0.0.0"), Ipv4Mask ("255.0.0.0"), 1);	

	
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
	
//***************Mobily model for SM ues ******************

	uemobility.Install(ueNodes);
//*********************************************************

	// Install LTE Devices to the nodes
	NetDeviceContainer enbLteDevs = lteHelper->InstallEnbDevice (enbNodes);
	NetDeviceContainer lteVoiceUeDevice = lteHelper->InstallUeDevice (lteVoiceUeContainer);
	
//****************Install LTE Devices to SM ue nodes**********
NetDeviceContainer ueLteDevs = lteHelper->InstallUeDevice (ueNodes);
//************************************************************

	// Install the IP stack on the UEs
	internet.Install(lteVoiceUeContainer);
	
	internet.Install (ueNodes);// for SM ue nodes abbded by me


	Ipv4InterfaceContainer lteVoiceUeInterface;
	
	Ipv4InterfaceContainer ueIpIface;//For SM ue nodes added by me
	lteVoiceUeInterface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (lteVoiceUeDevice));
	
	ueIpIface = epcHelper->AssignUeIpv4Address (NetDeviceContainer (ueLteDevs));//For SM ue nodes added by me

	// Assign IP address to UEs, and install applications

	for (uint32_t u = 0; u < lteVoiceUeContainer.GetN (); ++u)
	{
		Ptr<Node> ueNode = lteVoiceUeContainer.Get (u);
		Ptr<MobilityModel> mm = ueNode->GetObject<MobilityModel>();
		//NS_LOG_INFO("Voice UE Node position " << u << " " << mm->GetPosition().x << " " << mm->GetPosition().y);//commented by me

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

	for (uint16_t i = 0; i < lteVoiceUeContainer.GetN(); i++)
	{
		lteHelper->Attach (lteVoiceUeDevice.Get(i), enbLteDevs.Get(0));
	}

	
//**********Attach SM Ues to eNodeB*********************************

for (uint16_t i = 0; i < numberOfNodes; i++)
	{
		lteHelper->Attach (ueLteDevs.Get(i), enbLteDevs.Get(0));
	}
//******************************************************************
//*************************MMS application *************************
//Installing MMS client and server
MmsServerHelper mmsServer (ueIpIface);


	ApplicationContainer serverApps = mmsServer.Install (ueNodes);
	serverApps.Start (Seconds (0.01));
	serverApps.Stop (Seconds (10.0));

	MmsClientHelper mmsClient (serverApps, clientLteInterfaces, Seconds (requestInterval), type, mode, false);//Last two arguments are added by RajeshChintha from mmsp2p.cc



	ApplicationContainer clientApps = mmsClient.Install (remoteHost);
	clientApps.Start (Seconds (0.05));
	clientApps.Stop (Seconds (10.0));

//******************************************************************

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
	//This client is only generating .csv files; added by RajeshChintha
	GeneralUdpClientHelper client (lteVoiceRemoteAddress, lteVoiceRemotePort, 3); //0 = video; 1 = gaming uplink
	lteVoiceRemoteApp = client.Install (lteVoiceUeContainer);
	lteVoiceRemoteApp.Start (Seconds (0.1));
	lteVoiceRemoteApp.Stop (Seconds (100.0));

/*	// DOWNLINK (to UEs) !!!!
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
	
*/
lteHelper->EnablePdcpTraces ();//added by me; copied from just above EpsBearer



	Simulator::Stop(Seconds(simTime));
	Simulator::Run();

	Simulator::Destroy();
	return 0;

}

