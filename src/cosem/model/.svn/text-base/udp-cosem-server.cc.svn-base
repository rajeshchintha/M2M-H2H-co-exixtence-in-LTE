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
#include "ns3/ipv4-header.h"
#include "ns3/inet-socket-address.h"
#include "ns3/socket-factory.h"
#include "ns3/simulator.h"
#include "ns3/nstime.h"
#include "cosem-header.h"
#include "cosem-al-server.h"
#include "cosem-ap-server.h"
#include "udp-cosem-server.h"

namespace ns3 {

NS_LOG_COMPONENT_DEFINE ("UdpCosemWrapperLayerServer");
NS_OBJECT_ENSURE_REGISTERED (UdpCosemWrapperServer);

TypeId 
UdpCosemWrapperServer::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::UdpCosemWrapperServer")
    .SetParent<Object> ()
    .AddConstructor<UdpCosemWrapperServer> ()
    ;
  return tid;
}

UdpCosemWrapperServer::UdpCosemWrapperServer ()
{
  NS_LOG_FUNCTION_NOARGS ();
  m_socket = 0;
  m_wPortSap = 0;
  m_wPortCap = 0;
  m_udpPort = 4056;
  m_adaptCosemUdpserviceEvent = EventId ();

 // For debugging purporse
 // NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CWS created!");
}

UdpCosemWrapperServer::~UdpCosemWrapperServer ()
{
  NS_LOG_FUNCTION_NOARGS ();
}

void 
UdpCosemWrapperServer::Init ()
{
  // Create the socket
  if (m_socket == 0)
    {
      TypeId tid = TypeId::LookupByName ("ns3::UdpSocketFactory");
      m_socket = Socket::CreateSocket (m_cosemAlServer->GetCosemApServer ()->GetNode (), tid);
      InetSocketAddress local = InetSocketAddress (Ipv4Address::ConvertFrom(m_localAddress), m_udpPort);
      m_socket->Bind (local);
      // Set the callback method ("Adapt" Recv Udp funtion to UDP-DATA.ind (APDU))
      m_socket->SetRecvCallback (MakeCallback (&UdpCosemWrapperServer::Recv, this));

      // For debugging purporses
      // NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s UDPSocketServer created with Ip " << Ipv4Address::ConvertFrom (m_localAddress));
    }
}

void 
UdpCosemWrapperServer::Recv (Ptr<Socket> socket)
{
  NS_LOG_FUNCTION (this << socket);

  Ptr<Packet> packet;
  Address from;
  CosemWrapperHeader hdr;
  // Retreive the packet sent by the Remote Client and copy the Wrapper Header from the packet
  packet = socket->RecvFrom (from);  
  packet->RemoveHeader (hdr);
  // Retreive the wPort of the Remote Client 
  m_wPortCap = hdr.GetSrcwPort ();
  // Retreive Remote Ip address
  m_remoteAddress = Address (InetSocketAddress::ConvertFrom (from).GetIpv4 ());
  // Adapt RECEIVE UDP function to UDP-DATA.ind (APDU) service
  m_adaptCosemUdpserviceEvent = Simulator::Schedule (Seconds (0.0), &UdpCosemWrapperServer::AdaptCosemUdpServices, this, INDICATION, packet);
}

void
UdpCosemWrapperServer::Send (Ptr<Packet> packet)
{

  // Connect the SAP with the remote CAP 
  if (Ipv4Address::IsMatchingType(m_remoteAddress) == true)
    {
      m_socket->Connect (InetSocketAddress (Ipv4Address::ConvertFrom(m_remoteAddress), m_udpPort)); // m_udpPort same as Client
      // For debugging purporses
      // NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s Server connected with Client " << Ipv4Address::ConvertFrom(m_remoteAddress));
    }

  // Add Wrapper header
  CosemWrapperHeader hdr;
  hdr.SetSrcwPort (m_wPortSap); 
  hdr.SetDstwPort (m_wPortCap); 
  hdr.SetLength (packet->GetSize ()); 
  packet->AddHeader (hdr); 

  // Adapt UDP-DATA.req (APDU) service to SEND UDP function
  m_adaptCosemUdpserviceEvent = Simulator::Schedule (Seconds (0.0), &UdpCosemWrapperServer::AdaptCosemUdpServices, this, REQUEST, packet);

}

void 
UdpCosemWrapperServer::AdaptCosemUdpServices (int type_service, Ptr<Packet> packet)
{
  NS_ASSERT (m_adaptCosemUdpserviceEvent.IsExpired ());
  Simulator::Cancel (m_adaptCosemUdpserviceEvent);

  if (type_service == INDICATION)
    {
      // Pass the information to the SAL ("inoke" UDP-DATA.ind (APDU))
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CW-S (" << Ipv4Address::ConvertFrom (m_localAddress) 
                                                   << ") --> RECEIVE ("
                                                   << packet->GetSize () << "B)" << "--> UDP-DATA.ind (APDU)");  
      m_cosemAlServer->RecvCosemApduUdp (packet);
    }

  if (type_service == REQUEST)
    {
      // Call SEND Udp function (through UdpSocket)
      m_socket->Send (packet);
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CW-S (" << Ipv4Address::ConvertFrom (m_localAddress) 
                                                   << ") --> UDP-DATA.req (APDU) --> SEND ("
                                                   << packet->GetSize () << "B)");
    }
}

void
UdpCosemWrapperServer::SetSocket (Ptr<Socket> socket)
{
  m_socket = socket;
}

Ptr<Socket>
UdpCosemWrapperServer::GetSocket ()
{
  return m_socket;
}

void 
UdpCosemWrapperServer::SetCosemAlServer (Ptr<CosemAlServer> cosemAlServer)
{
  m_cosemAlServer = cosemAlServer;
}

Ptr<CosemAlServer> 
UdpCosemWrapperServer::GetCosemAlServer ()
{
  return m_cosemAlServer;
}

void
UdpCosemWrapperServer::SetwPortServer (Ptr<CosemApServer> cosemApServer)
{
  cosemApServer->SetWport (m_wPortSap++);
}

uint16_t
UdpCosemWrapperServer::GetwPortServer ()
{
 return m_wPortSap;
}

uint16_t
UdpCosemWrapperServer::GetwPortClient ()
{
 return m_wPortCap;
}

void 
UdpCosemWrapperServer::SetUdpport (uint16_t udpPort)
{
  m_udpPort = udpPort ;
}

uint16_t 
UdpCosemWrapperServer::GetUdpport ()
{
  return m_udpPort;
}


void 
UdpCosemWrapperServer::SetLocalAddress (Address ip)
{
  m_localAddress = ip;
}

Address
UdpCosemWrapperServer::GetLocalAddress ()
{
  return m_localAddress;
}

void 
UdpCosemWrapperServer::SetRemoteAddress (Address ip)
{
  m_localAddress = ip;
}

Address
UdpCosemWrapperServer::GetRemoteAddress ()
{
  return m_localAddress;
}

} // namespace ns3
