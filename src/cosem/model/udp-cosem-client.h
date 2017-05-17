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

#ifndef UDP_COSEM_CLIENT_H
#define UDP_COSEM_CLIENT_H

#include "ns3/object.h"
#include "ns3/address.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"

namespace ns3 {

class Socket;
class Packet;
class CosemApServer;
class CosemAlClient;
class CosemApClient;

/** 
 * UDP COSEM Wrapper Transport sub-layer - Client
 *
 */

class UdpCosemWrapperClient : public Object
{
public:
  static TypeId GetTypeId (void);
  
  UdpCosemWrapperClient ();
 
  virtual ~UdpCosemWrapperClient ();

  // Recive ("read") the data from the socket and pass to the Cosem Application Layer
  void Recv (Ptr<Socket> socket);
	
  // Called when new packet is ready to be send
  void Send (Ptr<Packet> packet, Ptr<CosemApServer> cosemApServer);

  // Call UDP services
  void AdaptCosemUdpServices (int type_service, Ptr<Packet> packet);

  // Set & GET the pointer to a CosemAlClient object
  void SetCosemAlClient (Ptr<CosemAlClient> cosemAlClient);
  Ptr<CosemAlClient> GetCosemAlClient ();

  // Set & GET the pointer to a Socket object
  void SetSocket (Ptr<Socket> socket);
  Ptr<Socket> GetSocket ();

  // Assign a wPort number to the CAP created and attached to the CAL
  void SetwPortClient (Ptr<CosemApClient> cosemApClient);

  // Return the wPort number of the CAP connected to the CAL
  uint16_t GetwPortClient ();

  // Return the wPort number of the remote SAP
  uint16_t GetwPortServer ();

  // Set & GET the Udp Port listening by the CAL
  void SetUdpport (uint16_t udpPort);
  uint16_t GetUdpport ();

  // Set & GET the local Ip address (Cap)
  void SetLocalAddress (Address ip);
  Address GetLocalAddress ();

  // Set the remote ip address (Sap)
  void SetRemoteAddress (Address ip); 
  Address GetRemoteAddress ();

  // Type of services
  enum typeService { REQUEST, INDICATION, CONFIRM };
  
private:

  Ptr<Socket> m_socket;

  Ptr<CosemAlClient> m_cosemAlClient; 

  uint16_t m_wPortCap;  // Wrapper Port Number assigned to the CAP (Counter)
  uint16_t m_wPortSap;  // Wrapper Port Number assigned to the remote SAP currently being attended
  uint16_t m_udpPort;  // Udp port
  Address m_localAddress;  // Local Ip address 
  Address m_remoteAddress;  // Ip Address of the remote SAP

  // Helpers parameters
  EventId m_adaptCosemUdpserviceEvent;

};

} // namespace ns3

#endif /* UDP_COSEM_CLIENT_H */
