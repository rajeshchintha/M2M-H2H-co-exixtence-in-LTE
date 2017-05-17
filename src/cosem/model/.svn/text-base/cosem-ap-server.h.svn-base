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

#ifndef COSEM_AP_SERVER_H
#define COSEM_AL_SERVER_H

#include "ns3/application.h"
#include "ns3/address.h"
#include "ns3/application-container.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"

namespace ns3 {

class Node;
class Packet;
class CosemAlServer;

/** 
 * COSEM Application Process (AP) - Server
 *
 */

class CosemApServer : public Application
{
public:
  static TypeId GetTypeId (void);
  
  CosemApServer ();
 
  virtual ~CosemApServer ();

  // Called when new message received (for COSEM-GET services)
  void Recv (int typeAcseService, int typeGet, Ptr<Packet> packet);

  // Retrieve the node where the CAP is attached
  Ptr<Node> GetNode () const;

  //Set & GET the pointer to a CosemServer_AL_CF object
  void SetCosemAlServer (Ptr<CosemAlServer> cosemAlServer);
  Ptr<CosemAlServer> GetCosemAlServer ();

  // Set & GET the wPort
  void SetWport (uint16_t wPort);
  uint16_t GetWport ();

  // Set & GET the Udp Port listening by the CAL
  void SetUdpport (uint16_t udpPort);
  uint16_t GetUdpport ();

  // Set & GET the local Ip address
  void SetLocalAddress (Address ip);
  Address GetLocalAddress ();
  
  // Type of services
  enum typeService { REQUEST, INDICATION, RESPONSE, CONFIRM };

  // Type of COSEM-GET service
  enum typeGet { GET_NORMAL, GET_WITH_LIST, GET_ONE_BLOCK, GET_LAST_BLOCK }; 
 
  // ACSE services
  enum typeAcseService { OPEN, RELEASE };
	
protected:

  virtual void DoDispose (void);
 
private:

  Ptr<CosemAlServer> m_cosemAlServer;
  uint16_t m_wPort;  // SAP Wrapper Port Number (unique id)
  uint16_t m_udpPort;  // Udp port
  Address m_localAddress;  // Local Ip address
  uint64_t m_reqData [3]; // Data requested by the remote CAP

  virtual void StartApplication (void);
  virtual void StopApplication (void);

  // Helpers parameters
  //EventId m_invokeCosemService; 
};

} // namespace ns3

#endif /* COSEM_AP_SERVER_H */
