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
#include "ns3/socket.h"
#include "ns3/packet.h"
#include "ns3/ipv4-address.h"
#include "ns3/inet-socket-address.h"
#include "ns3/socket-factory.h"
#include "ns3/simulator.h"
#include "ns3/nstime.h"
#include "cosem-header.h"
#include "cosem-ap-server.h"
#include "cosem-al-client.h"
#include "cosem-ap-client.h"
#include "udp-cosem-client.h"

namespace ns3 {

NS_LOG_COMPONENT_DEFINE ("UdpCosemWrapperLayerClient");
NS_OBJECT_ENSURE_REGISTERED (UdpCosemWrapperClient);

TypeId 
UdpCosemWrapperClient::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::UdpCosemWrapperClient")
    .SetParent<Object> ()
    .AddConstructor<UdpCosemWrapperClient> ()
    ;
  return tid;
}

UdpCosemWrapperClient::UdpCosemWrapperClient ()
{
  NS_LOG_FUNCTION_NOARGS ();
  m_socket = 0;
  m_wPortCap = 0;
  m_udpPort = 4056;
  EventId m_adaptCosemUdpserviceEvent = EventId ();

  // For debugging purposes
  // NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CWC created!");
}

UdpCosemWrapperClient::~UdpCosemWrapperClient ()
{
  NS_LOG_FUNCTION_NOARGS ();
}

void 
UdpCosemWrapperClient::Recv (Ptr<Socket> socket)
{
  NS_LOG_FUNCTION (this << socket);

  //NS_LOG_INFO ("CallBack Recv () funtion in CW-C " << Ipv4Address::ConvertFrom (m_localAddress));

  Ptr<Packet> packet;
  Address from;
  CosemWrapperHeader hdr;
  // Retreive the packet sent by the Remote Server and copy the Wrapper Header from the packet
  packet = socket->RecvFrom (from);  // Check while???
  packet->RemoveHeader (hdr);
  // Retreive the wPort of the Remote Server 
  m_wPortSap = hdr.GetSrcwPort ();
  // Adapt RECEIVE UDP function to UDP-DATA.ind (APDU) service
  m_adaptCosemUdpserviceEvent = Simulator::Schedule (Seconds (0.0), &UdpCosemWrapperClient::AdaptCosemUdpServices, this, INDICATION, packet);
}

void
UdpCosemWrapperClient::Send (Ptr<Packet> packet, Ptr<CosemApServer> cosemApServer)
{ 
  // Retrieve the peer address
  m_remoteAddress = cosemApServer->GetLocalAddress ();
  uint16_t dstwPort = cosemApServer->GetWport ();
  uint16_t srcwPort = m_cosemAlClient->GetCosemApClient ()->GetWport ();

  // Create the socket if not already
  if (m_socket == 0)
    {
      TypeId tid = TypeId::LookupByName ("ns3::UdpSocketFactory");
      m_socket = Socket::CreateSocket (m_cosemAlClient->GetCosemApClient ()->GetNode (), tid);  
      InetSocketAddress local = InetSocketAddress (Ipv4Address::GetAny (), m_udpPort);
      m_socket->Bind (local);
     
      // For debugging purposes
      // NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s UDPSocketClient created with Ip " << Ipv4Address::ConvertFrom(m_localAddress));

      // Set the callback method ("Adapt" Recv Udp funtion to UDP-DATA.ind (APDU))
      m_socket->SetRecvCallback (MakeCallback (&UdpCosemWrapperClient::Recv, this));
    }

  // Connect the CAP with the remote SAP
  if (Ipv4Address::IsMatchingType(m_remoteAddress) == true)
    {
      m_socket->Connect (InetSocketAddress (Ipv4Address::ConvertFrom(m_remoteAddress), m_udpPort)); // m_udpPort same as Client
      // For debugging purposes
      // NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s Client connected with Server " << Ipv4Address::ConvertFrom(m_remoteAddress));
    }

  // Add Wrapper header
  CosemWrapperHeader hdr;
  hdr.SetSrcwPort (srcwPort); 
  hdr.SetDstwPort (dstwPort); 
  hdr.SetLength (packet->GetSize ()); 
  packet->AddHeader (hdr); 

  // Adapt UDP-DATA.req (APDU) service to SEND UDP function
  m_adaptCosemUdpserviceEvent = Simulator::Schedule (Seconds (0.0), &UdpCosemWrapperClient::AdaptCosemUdpServices, this, REQUEST, packet);
}


void 
UdpCosemWrapperClient::AdaptCosemUdpServices (int type_service, Ptr<Packet> packet)
{
  NS_ASSERT (m_adaptCosemUdpserviceEvent.IsExpired ());
  Simulator::Cancel (m_adaptCosemUdpserviceEvent);

  if (type_service == REQUEST)
    {

      // Call SEND Udp function (through UdpSocket)
      m_socket->Send (packet); 
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CW-C (" << Ipv4Address::ConvertFrom (m_localAddress)    
                                                   << ") --> UDP-DATA.req (APDU) --> SEND ("
                                                   << packet->GetSize () << "B)");
    }

  if (type_service == INDICATION)
    {
      // Pass the information to the CAL ("inoke" UDP-DATA.ind (APDU))
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CW-C (" << Ipv4Address::ConvertFrom (m_localAddress) 
                                                   << ") --> RECEIVE ("
                                                   << packet->GetSize () << "B)" << "--> UDP-DATA.ind (APDU)");
      m_cosemAlClient->RecvCosemApduUdp (packet);
    }
}

void
UdpCosemWrapperClient::SetSocket (Ptr<Socket> socket)
{
  // Send the packet to the lower layers
  m_socket = socket;
}

Ptr<Socket>
UdpCosemWrapperClient::GetSocket ()
{
  return m_socket;
}

void 
UdpCosemWrapperClient::SetCosemAlClient (Ptr<CosemAlClient> cosemAlClient)
{
  m_cosemAlClient = cosemAlClient;
}

Ptr<CosemAlClient> 
UdpCosemWrapperClient::GetCosemAlClient ()
{
  return m_cosemAlClient;
}

void
UdpCosemWrapperClient::SetwPortClient (Ptr<CosemApClient> cosemApClient)
{
  cosemApClient->SetWport (m_wPortCap++);
}

uint16_t
UdpCosemWrapperClient::GetwPortClient ()
{
 return m_wPortCap;
}

uint16_t
UdpCosemWrapperClient::GetwPortServer ()
{
 return m_wPortSap;
}

void 
UdpCosemWrapperClient::SetUdpport (uint16_t udpPort)
{
  m_udpPort = udpPort ;
}

uint16_t 
UdpCosemWrapperClient::GetUdpport ()
{
  return m_udpPort;
}

void 
UdpCosemWrapperClient::SetLocalAddress (Address ip)
{
  m_localAddress = ip;
}

Address
UdpCosemWrapperClient::GetLocalAddress ()
{
  return m_localAddress;
}

void 
UdpCosemWrapperClient::SetRemoteAddress (Address ip)
{
  m_remoteAddress = ip;
}

Address
UdpCosemWrapperClient::GetRemoteAddress ()
{
  return m_remoteAddress;
}

} // namespace ns3
