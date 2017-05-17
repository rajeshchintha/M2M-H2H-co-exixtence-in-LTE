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

#ifndef UDP_COSEM_SERVER_H
#define UDP_COSEM_SERVER_H

#include "ns3/object.h"
#include "ns3/event-id.h"
#include "ns3/address.h"
#include "ns3/ptr.h"

namespace ns3 {

class Socket;
class Packet;
class CosemAlServer;
class CosemApServer;

/** 
 * UDP COSEM Wrapper Transport sub-layer - Server
 *
 */

class UdpCosemWrapperServer : public Object
{
public:
  static TypeId GetTypeId (void);
  
  UdpCosemWrapperServer ();
 
  virtual ~UdpCosemWrapperServer ();

  // First Process
  void Init ();

  // Recive ("read") the data from the socket and pass to the Cosem Application Layer
  void Recv (Ptr<Socket> socket);
	
  // Called when new packet is ready to be send (assuming only one SAP attached to the physical device)
  void Send (Ptr<Packet> packet);


  // Call UDP services
  void AdaptCosemUdpServices (int type_service, Ptr<Packet> packet);

  // Set & GET the pointer to a CosemAlServer object
  void SetCosemAlServer (Ptr<CosemAlServer> cosemAlServer);
  Ptr<CosemAlServer> GetCosemAlServer ();

  // Set & GET the pointer to a Socket object
  void SetSocket (Ptr<Socket> socket);
  Ptr<Socket> GetSocket ();

  // Assign a wPort number to the SAP created and attached to the SAL
  void SetwPortServer (Ptr<CosemApServer> cosemApServer);

  // Return the wPort number of the SAP connected to the SAL
  uint16_t GetwPortServer ();

  // Return the wPort number of the remote CAP
  uint16_t GetwPortClient ();

  // Set & GET the Udp Port listening by the SAL
  void SetUdpport (uint16_t udpPort);
  uint16_t GetUdpport ();

  // Set & GET the local Ip address
  void SetLocalAddress (Address ip);
  Address GetLocalAddress ();

  // Set & GET the remote Ip address
  void SetRemoteAddress (Address ip);
  Address GetRemoteAddress ();

  // Type of services
  enum typeService { REQUEST, INDICATION, CONFIRM };
  
private:

  Ptr<Socket> m_socket;

  Ptr<CosemAlServer> m_cosemAlServer; 

  uint16_t m_wPortSap;  // Wrapper Port Number assigned to the SAP (Counter)
  uint16_t m_wPortCap;  // Wrapper Port Number assigned to the remote CAP
  uint16_t m_udpPort;  // Udp port
  Address m_localAddress;  // Local Ip address
  Address m_remoteAddress;  // Remote Ip address

  // Helpers parameters
  EventId m_adaptCosemUdpserviceEvent;

};

} // namespace ns3

#endif /* UDP_COSEM_SERVER_H */
