///* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
//Added by Rajesh Chintha on 21.12.2016; copied from /media/raj-vaio/Ext-Data1/CPRI-Stuff/Thrust/OPNET/NS3/MMS and LTE_NS3.pdf

//
#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/applications-module.h"
#include "ns3/general-udp-server.h"
#include "ns3/general-udp-client.h"
#include "ns3/general-udp-client-server-helper.h"
#include "ns3/uinteger.h"
#include "ns3/string.h"

using namespace ns3;


int 
main (int argc, char *argv[])
{
  bool verbose = true;
  uint32_t numberOfBgNodes = 100;
//  uint16_t lteVideoRemotePort = 5000;
  CommandLine cmd;
  cmd.AddValue ("verbose", "Tell application to log if true", verbose);

  cmd.Parse (argc,argv);

  /* ... */
  // Create Voice UEs (30% of the nodes)
NodeContainer lteVoiceUeContainer;
lteVoiceUeContainer.Create((float)0.3*numberOfBgNodes);
// Create Video UEs (20% of the nodes)
NodeContainer lteVideoUeContainer;
lteVideoUeContainer.Create((float)0.2*numberOfBgNodes);
// Create Gaming UEs (20% of the nodes)
NodeContainer lteGamingUeContainer;
lteGamingUeContainer.Create((float)0.2*numberOfBgNodes);
// Create HTTP UEs (20% of the nodes)
NodeContainer lteHttpUeContainer;
lteHttpUeContainer.Create((float)0.2*numberOfBgNodes);
// Create FTP UEs (the rest)
NodeContainer lteFtpUeContainer;
lteFtpUeContainer.Create(numberOfBgNodes-lteVoiceUeContainer.GetN()-lteVideoUeContainer.GetN()-lteGamingUeContainer.GetN()-lteHttpUeContainer.GetN());
// Create Voice Remote host to send/receive Voice traffic to/from Voice UEs
NodeContainer lteVoiceRemoteContainer;
lteVoiceRemoteContainer.Create(1);
// Create Video Remote host to send/receive Video traffic to/from Video UEs
NodeContainer lteVideoRemoteContainer;
lteVideoRemoteContainer.Create(1);
// Create Gaming Remote host to send/receive Gaming traffic to/from Gaming UEs
NodeContainer lteGamingRemoteContainer;
lteGamingRemoteContainer.Create(1);
// Create FTP Remote host to send/receive FTP traffic to/from FTP UEs
NodeContainer lteFtpRemoteContainer;
lteFtpRemoteContainer.Create(1);
// Create a number of HTTP Server, one for each UEs (currently server does not support multiple clients)
NodeContainer lteHttpRemoteContainer;
lteHttpRemoteContainer.Create(lteHttpUeContainer.GetN());

/*
// -------------------------------------------------------------------
// Video Application, both UL and DL
// UPLINK (from UEs)
//
// Create one Video applications on remote host.
//

uint16_t lteVideoRemotePort = 5000;

GeneralUdpServerHelper lteVideoRemoteServer (lteVideoRemotePort, 0);
ApplicationContainer lteVideoUeApp = lteVideoRemoteServer.Install(lteVideoRemoteNode);//commented by me and added next line
//ApplicationContainer lteVideoUeApp = lteVideoRemoteServer.Install(lteVideoRemoteContainer);
lteVideoUeApp.Start (Seconds (0.0));
lteVideoUeApp.Stop (Seconds (1000.0));
//
// Create one Video application to send UDP datagrams from UE nodes to
// Remote Video host.
//
GeneralUdpClientHelper VideoClientUe (lteVideoRemoteAddress,lteVideoRemotePort, 0); //0 = video; 1 = Video uplink
lteVideoUeApp = VideoClientUe.Install (lteVideoUeContainer);
lteVideoUeApp.Start (Seconds (0.1));
lteVideoUeApp.Stop (Seconds (100.0));
// DOWNLINK (to UEs)
//
// Create Video applications on UE nodes.
//
uint16_t lteVideoUePort = 5000;
GeneralUdpServerHelper lteVideoUeServer (lteVideoUePort, 0);
ApplicationContainer lteVideoRemoteApp = lteVideoUeServer.Install
(lteVideoUeContainer);
lteVideoRemoteApp.Start (Seconds (0.0));
lteVideoRemoteApp.Stop (Seconds (1000.0));
//
// Create one Video application to send UDP datagrams from Remote Host to
// VoIP UEs.
//
for (uint32_t i = 0; i < lteVideoUeInterface.GetN(); i++)
{
GeneralUdpClientHelper VideoClientRemote(lteVideoUeInterface.GetAddress(i), lteVideoUePort, 0); //0 = video; 1= Video uplink
lteVideoRemoteApp = VideoClientRemote.Install (lteVideoRemoteContainer);
lteVideoRemoteApp.Start (Seconds (0.1));
lteVideoRemoteApp.Stop (Seconds (100.0));
}
*/
  Simulator::Run ();
  Simulator::Destroy ();
  return 0;
}


