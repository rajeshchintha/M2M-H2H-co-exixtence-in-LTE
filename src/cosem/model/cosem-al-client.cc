/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/*
 * Copyright (c) 2012 Uniandes (unregistered)
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
 * Author: Juanmalk <jm.aranda121@uniandes.edu.co> 
 */

#include "ns3/log.h"
#include "ns3/packet.h"
#include "ns3/simulator.h"
#include "ns3/nstime.h"
#include "ns3/socket.h"
#include "cosem-header.h"
#include "cosem-al-client.h"
#include "cosem-ap-server.h"
#include "cosem-ap-client.h"
#include "cosem-al-server.h"
#include "udp-cosem-client.h"

namespace ns3 {

NS_LOG_COMPONENT_DEFINE ("CosemApplicationLayerClient");
NS_OBJECT_ENSURE_REGISTERED (CosemAlClient);

TypeId 
CosemAlClient::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::CosemAlClient")
    .SetParent<Object> ()
    .AddConstructor<CosemAlClient> ()
    ;
  return tid;
}

CosemAlClient::CosemAlClient ()
{
  NS_LOG_FUNCTION_NOARGS ();
  m_typeService = REQUEST;
  m_typeGet = GET_NORMAL;
  m_stateCf = CF_INACTIVE;
  m_udpPort = 4056;
  m_changeStateEvent = EventId ();
  m_sendApduEvent = EventId ();
  m_invokeCosemServiceEvent = EventId ();

  // For debugging purposes
  //NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAL created!");
}

CosemAlClient::~CosemAlClient ()
{
  NS_LOG_FUNCTION_NOARGS ();
}

void 
CosemAlClient::CosemAcseOpen (int typeService, Ptr<CosemApServer> cosemApServer, Ptr<Packet> packet)
{
  //.request
  if (typeService == REQUEST)
    {
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP ("<< m_cosemApClient->GetWport () << ":" 
                                                   << Ipv4Address::ConvertFrom (m_cosemApClient->GetLocalAddress ()) << ")" 
                                                   << " --> OPEN.req");
      // Argument Ptr<Packet> packet not used	
      
      // Event: Change the state of CAL to ASSOCIATION_PENDING
      m_changeStateEvent = Simulator::Schedule (Seconds (0.0), &CosemAlClient::SetStateCf, this, CF_ASSOCIATION_PENDING);

      // Event: Request an AA establishment: Construct the AARQ APDU of ACSE service 
      m_invokeCosemServiceEvent = Simulator::Schedule (Seconds (0.0), &CosemAlClient::CosemAcseApdu, this, OPEN, typeService, cosemApServer);


    }
  //.confirm
  if (typeService == CONFIRM)
    {   
      NS_ASSERT (m_invokeCosemServiceEvent.IsExpired ());
      Simulator::Cancel (m_invokeCosemServiceEvent);  // necessary?

      // Event: Change the state of SAL to ASSOCIATED
      m_changeStateEvent = Simulator::Schedule (Seconds (0.0), &CosemAlClient::SetStateCf, this, CF_ASSOCIATED);

      // Event: Inform to the CAP that a remote SAP responses to its request for an establisment of an AA (OPEN.cnf(OK))
      Simulator::Schedule (Seconds (0.0), &CosemApClient::Recv, m_cosemApClient, packet, OPEN, -1, cosemApServer);
    }
}
	
void 
CosemAlClient::CosemAcseRelease (int typeService, Ptr<CosemApServer> cosemApServer, Ptr<Packet> packet)
{ 
  //.request
  if (typeService == REQUEST)
    {
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP ("<< m_cosemApClient->GetWport () << ":" 
                                                   << Ipv4Address::ConvertFrom (m_cosemApClient->GetLocalAddress ()) << ")" 
                                                   << " --> RELEASE.req");	
      // Argument Ptr<Packet> packet not used
      
      // Event: Change the state of CAL to ASSOCIATION_PENDING
      m_changeStateEvent = Simulator::Schedule (Seconds (0.0), &CosemAlClient::SetStateCf, this, CF_ASSOCIATION_PENDING);

      // Event: Release an AA established before: Construct the RLRQ APDU of ACSE service 
      m_invokeCosemServiceEvent = Simulator::Schedule (Seconds (0.0), &CosemAlClient::CosemAcseApdu, this, RELEASE, typeService, cosemApServer);
    }

  //.confirm
  if (typeService == CONFIRM)
    {   
      NS_ASSERT (m_invokeCosemServiceEvent.IsExpired ());
      Simulator::Cancel (m_invokeCosemServiceEvent);  // necessary?

      // Event: Inform to the CAP that an remote SAP responses to the request for a release of an AA
      Simulator::Schedule (Seconds (0.0), &CosemApClient::Recv, m_cosemApClient, packet, RELEASE, -1, cosemApServer);
    }
  
}
	
void 
CosemAlClient::CosemXdlmsGet (int typeGet, int typeService, Ptr<CosemApServer> cosemApServer, Ptr<Packet> packet)
{
  //.request
  if (typeService == REQUEST)
    {  
      if (typeGet == GET_NORMAL)
        { 
          NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP ("<< m_cosemApClient->GetWport () << ":" 
                                                       << Ipv4Address::ConvertFrom (m_cosemApClient->GetLocalAddress ()) << ")" 
                                                       << " --> GET.req (NORMAL)");	

          // Event: Construct the GET-REQUEST APDU of xDLMS_ASE service
          m_invokeCosemServiceEvent = Simulator::Schedule (Seconds (0.0), &CosemAlClient::CosemXdlmsApdu, this, typeGet, typeService, cosemApServer);
        }
      else
        {
          NS_LOG_INFO ("Error: Undefined Cosem Get-request type!");     
        }
    } 
  
  //.confirm
  if (typeService == CONFIRM)
    {   
      NS_ASSERT (m_invokeCosemServiceEvent.IsExpired ());
      Simulator::Cancel (m_invokeCosemServiceEvent);  // necessary?

      // Event: Inform to the CAP that the remote SAP sent the data requested (GET.cnf)
      Simulator::Schedule (Seconds (0.0), &CosemApClient::Recv, m_cosemApClient, packet, -1, typeGet, cosemApServer);
    }
}

void 
CosemAlClient::CosemAcseApdu (int typeAcseService, int typeService, Ptr<CosemApServer> cosemApServer)
{
  NS_ASSERT (m_invokeCosemServiceEvent.IsExpired ());
  Simulator::Cancel (m_invokeCosemServiceEvent);  // necessary?

  if (typeService == REQUEST)
    { 
      if (typeAcseService == OPEN)
        { 
          // Build an xDLMS-Initiate.req PDU and an AARQ APDU
          CosemAarqHeader hdr;
          hdr.SetApplicationContextName (0);  // {N/A}Application Context Name (rules that govern th exchange-parameters)
          hdr.SetDedicatedKey (0);  // Dedicated Key, {0,N/A}        
          hdr.SetResponseAllowed (true);  // Reponse Allowed (AA is confirmed), {TRUE}	
          hdr.SetProposedQualityOfService (0); // Not used, {O, N/A} 
          hdr.SetProposedDlmsVersionNumber (6);  // Version number, {6}
          hdr.SetProposedConformance (0x001010);   // {0x001010}, Based on the example in Annex C IEC 62056-53	
          hdr.SetClientMaxReceivePduSize (0x4B0);  // Client_Max_Receive_PDU_Size,{0x4B0}:1200 bytes
	     
          Ptr<Packet> packet = Create<Packet> (); // Create the AARQ APDU packet
          packet->AddHeader (hdr); // Copy the header into the packet

          TypeAPDU typeHdr;
          typeHdr.SetApduType ((ApduType)hdr.GetIdApdu()); // Define the type of APDU
          packet->AddHeader (typeHdr); // Copy the header into the packet
    
          NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAL (" << Ipv4Address::ConvertFrom (m_cosemWrapperClient->GetLocalAddress ()) 
                                                       << ") --> AARQ APDU ("
                                                       << packet->GetSize () << "B)");

          // Event: Send the APDU to the sub-layer Wrapper (Invoke UDP-DATA.req (APDU))
          double t = (8*hdr.GetSerializedSize ())/(500000) + 2.235e-3; // assuming an ideal PLC channel
          m_sendApduEvent = Simulator::Schedule (Seconds (t), &CosemAlClient::sendApdu, this, packet, cosemApServer);
        }
      else if (typeAcseService == RELEASE)
        {
          // Build an RLRQ APDU
          CosemRlrqHeader hdr;
          hdr.SetReason (0);  // Release request reason, {0, normal}  

          Ptr<Packet> packet = Create<Packet> (); // Create the RLRQ APDU packet
          packet->AddHeader (hdr); // Copy the header into the packet

          TypeAPDU typeHdr;
          typeHdr.SetApduType ((ApduType)hdr.GetIdApdu()); // Define the type of APDU
          packet->AddHeader (typeHdr); // Copy the header into the packet
    
          NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAL (" << Ipv4Address::ConvertFrom (m_cosemWrapperClient->GetLocalAddress ()) 
                                                       << ") --> RLRQ APDU ("
                                                       << packet->GetSize () << "B)");

          // Event: Send the APDU to the sub-layer Wrapper (Invoke UDP-DATA.req (APDU))
          double t = (8*hdr.GetSerializedSize ())/(500000) + 2.235e-3; // assuming an ideal PLC channel
          m_sendApduEvent = Simulator::Schedule (Seconds (t), &CosemAlClient::sendApdu, this, packet, cosemApServer);   
        }
      else
        {
          NS_LOG_INFO ("Error: Undefined Cosem ACSE service (CAL)");     
        }
    }
  else
    {
      NS_LOG_INFO ("Error: Undefined request Cosem service (CAL)");     
    }
}
	
void 
CosemAlClient::CosemXdlmsApdu (int typeGet, int typeService, Ptr<CosemApServer> cosemApServer)
{
  NS_ASSERT (m_invokeCosemServiceEvent.IsExpired ());
  Simulator::Cancel (m_invokeCosemServiceEvent);  // necessary?

  if (typeService == REQUEST)
    {  
      if (typeGet == GET_NORMAL)
        { 
          // Build an GET-Request (Normal) APDU
          CosemGetRequestNormalHeader hdr;
          hdr.SetInvokeIdAndPriority (0x02);  // 0000 0010 (invoke_id {0b0000}),service_class= 1 (confirmed) priority level ({normal}))
          hdr.SetClassId (0X03);  // Class Register
          hdr.SetInstanceId (0x010100070000);  // OBIS CODE: 1.1.0.7.0.0
          hdr.SetAttributeId (0x02);  // Second Attribut = Value

          Ptr<Packet> packet = Create<Packet> (); // Create the GET-Request (Normal) APDU packet
          packet->AddHeader (hdr); // Copy the header into the packet

          TypeAPDU typeHdr;
          typeHdr.SetApduType ((ApduType)hdr.GetIdApdu()); // Define the type of APDU
          packet->AddHeader (typeHdr); // Copy the header into the packet
    
          NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAL (" << Ipv4Address::ConvertFrom (m_cosemWrapperClient->GetLocalAddress ()) 
                                                       << ") --> GET-RQ-NOR APDU ("
                                                       << packet->GetSize () << "B)");

          // Event: Send the APDU to the sub-layer Wrapper (Invoke UDP-DATA.req (APDU))
          double t = (8*hdr.GetSerializedSize ())/(500000) + 2.235e-3; // assuming an ideal PLC channel
          m_sendApduEvent = Simulator::Schedule (Seconds (t), &CosemAlClient::sendApdu, this, packet, cosemApServer);
        }
      else
        {
          NS_LOG_INFO ("Error: Undefined Get-Request Type");     
        }    
    }
  else
    {
      NS_LOG_INFO ("Error: Undefined Cosem-Get-Request service");     
    }
}
	
void 
CosemAlClient::RecvCosemApduTcp (int tcpsService, Ptr<Packet> packet)
{
  // do nothing
}

void 
CosemAlClient::RecvCosemApduUdp (Ptr<Packet> packet)
{
  TypeAPDU typeHdr;

  // Copy the Cosem APDU Header from the packet
  packet->RemoveHeader (typeHdr);
  ApduType typeAPDU = typeHdr.GetApduType (); 
  Ptr<CosemApServer> curretSap =  m_cosemApClient->GetCurrentCosemApServer ();
 
  if (typeAPDU == AARE)
    {
      // Event: Invoke COSEM-OPEN.cnf(OK) service    
      m_invokeCosemServiceEvent = Simulator::Schedule (Seconds (0.0), &CosemAlClient::CosemAcseOpen, this, CONFIRM, curretSap, packet);
    }

  if (typeAPDU == GETRES_N)
    { 
      // Event: Invoke the COSEM-GET.cnf (NORMAL, Data) service in order to indicate to he CAP that SAP sent the requested data 
      m_invokeCosemServiceEvent = Simulator::Schedule (Seconds (0.0), &CosemAlClient::CosemXdlmsGet, this, GET_NORMAL, CONFIRM, curretSap, packet);
    }

  if (typeAPDU == RLRE)
    {    
      // Event: Invoke COSEM-RELEASE.cnf service 
      m_invokeCosemServiceEvent = Simulator::Schedule (Seconds (0.0), &CosemAlClient::CosemAcseRelease, this, CONFIRM, curretSap, packet);     
    }
}

void 
CosemAlClient::sendApdu (Ptr<Packet> packet,  Ptr<CosemApServer> cosemApServer)
{
  NS_ASSERT (m_sendApduEvent.IsExpired ());
  Simulator::Cancel (m_sendApduEvent);
  m_cosemWrapperClient->Send (packet, cosemApServer);
}

void 
CosemAlClient::SetCosemApClient (Ptr<CosemApClient> cosemApClient)
{
  m_cosemApClient = cosemApClient;
}
	
Ptr<CosemApClient> 
CosemAlClient::GetCosemApClient ()
{
  return m_cosemApClient;
}

void 
CosemAlClient::SetCosemWrapperClient (Ptr<UdpCosemWrapperClient> cosemWrapperClient)
{
  m_cosemWrapperClient = cosemWrapperClient;
}
	
Ptr<UdpCosemWrapperClient> 
CosemAlClient::GetCosemWrapperClient ()
{
  return m_cosemWrapperClient;
}

void 
CosemAlClient::SetStateCf (int state)
{
  NS_ASSERT (m_changeStateEvent.IsExpired ());
  Simulator::Cancel (m_changeStateEvent);

  if (state == CF_IDLE)
    {
      m_stateCf = state;
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAL (" << Ipv4Address::ConvertFrom (m_cosemWrapperClient->GetLocalAddress ()) 
                                                   << ") --> IDLE");
      /*Ptr<Socket> skt = m_cosemWrapperClient->GetSocket ();
      if (skt != 0) 
        {
          skt->Close (); // Close a socket
          NS_LOG_INFO ("Socket closed!!");
        }*/
    } 
  else if (state == CF_ASSOCIATION_PENDING)    
      {
        m_stateCf = state;
        NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAL (" << Ipv4Address::ConvertFrom (m_cosemWrapperClient->GetLocalAddress ()) 
                                                     << ") --> ASSOCIATION_PENDING");
      }
  else if (state == CF_ASSOCIATED)    
      {
        m_stateCf = state;
        NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAL (" << Ipv4Address::ConvertFrom (m_cosemWrapperClient->GetLocalAddress ()) 
                                                     << ") --> ASSOCIATED");
      } 
  else if (state == CF_ASSOCIATION_RELEASE_PENDING)    
      {
        m_stateCf = state;
        NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAL (" << Ipv4Address::ConvertFrom (m_cosemWrapperClient->GetLocalAddress ()) 
                                                     << ") --> ASSOCIATION_RELEASE_PENDING");
      } 
  else
    {
      NS_LOG_INFO ("Error: No change of stare AL");
    }
}

int 
CosemAlClient::GetStateCf ()
{
  return m_stateCf;
}
	
int 
CosemAlClient::GetTypeService ()
{
  return m_typeService;
}

void 
CosemAlClient::SetTypeService (int typeService)
{
  m_typeService = typeService;
}
	
int 
CosemAlClient::GetTypeGet ()
{
  return m_typeGet;
}

void 
CosemAlClient::SetTypeGet (int typeGet)
{
  m_typeGet = typeGet;
}
 
void 
CosemAlClient::SetUdpport (uint16_t udpPort)
{
  m_udpPort = udpPort ;
}

uint16_t 
CosemAlClient::GetUdpport ()
{
  return m_udpPort;
}

void
CosemAlClient::SetChangeStateEvent (EventId changeStateEvent)
{
  m_changeStateEvent = changeStateEvent;
}

} // namespace ns3
