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

#ifndef COSEM_AL_CLIENT_H
#define COSEM_AL_CLIENT_H

#include <map> 
#include "ns3/object.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"

namespace ns3 {

class Packet;
class CosemApServer;
class CosemApClient;
class UdpCosemWrapperClient;

/** 
 * COSEM Application Layer (AL) Control Function - Client
 *
 */

class CosemAlClient : public Object
{
public:
  static TypeId GetTypeId (void);
  
  CosemAlClient ();
 
  virtual ~CosemAlClient();

  // COSEM-OPEN service of ACSE, implemented by the derived class
  void CosemAcseOpen (int typeService, Ptr<CosemApServer> cosemApServer, Ptr<Packet> packet);
	
  // COSEM-RELEASE service of ACSE, implemented by the derived class
  void CosemAcseRelease (int typeService, Ptr<CosemApServer> cosemApServer, Ptr<Packet> packet);
	
  // COSEM-GET service of xDLMS_ASE, implemented by the derived class
  void CosemXdlmsGet (int typeGet, int typeService, Ptr<CosemApServer> cosemApServer, Ptr<Packet> packet);

  // Construct the APDUs of ACSE services (AARQ, RLRQ)
  void CosemAcseApdu (int typeAcseService, int typeService, Ptr<CosemApServer> cosemApServer);
	
  // Construct the APDUs of xDLMS_ASE services(GET-REQUEST)
  void CosemXdlmsApdu (int typeGet, int typeService, Ptr<CosemApServer> cosemApServer);
	
  // Received the indication/confirmation of a TCP-DATA resquest
  void RecvCosemApduTcp (int tcpsService, Ptr<Packet> packet);

  // Received the indication/confirmation of a UDP-DATA resquest
  void RecvCosemApduUdp (Ptr<Packet> packet);

  // Send the APDU to the sub-layer Wrapper
  void sendApdu (Ptr<Packet> packet, Ptr<CosemApServer> cosemApServer);

  // Set & Get the pointer to a CosemAlServer object
  void SetCosemApClient (Ptr<CosemApClient> cosemApClient);
  Ptr<CosemApClient> GetCosemApClient ();
 
  // Set & Get the pointer to a CosemWrapperClient object
  void SetCosemWrapperClient (Ptr<UdpCosemWrapperClient> cosemWrapperClient);
  Ptr<UdpCosemWrapperClient> GetCosemWrapperClient ();

  // Set & Get the state of CF
  void SetStateCf (int state);
  int GetStateCf ();
	
  // Set & Get the type of service
  void SetTypeService (int typeService);
  int GetTypeService ();

  // Set & Get the type of COSEM-GET service
  void SetTypeGet (int typeGet); 
  int GetTypeGet ();

  // Set & GET the Udp Port listening by the CAL
  void SetUdpport (uint16_t udpPort);
  uint16_t GetUdpport ();

  // Set the m_changeStateEvent pointer
  void SetChangeStateEvent (EventId changeStateEvent);

  // States machine of the Control Function
  enum stateCf { CF_INACTIVE, CF_IDLE, CF_ASSOCIATION_PENDING, CF_ASSOCIATED, CF_ASSOCIATION_RELEASE_PENDING };

  // Type of services
  enum typeService { REQUEST, INDICATION, RESPONSE, CONFIRM };
	
  // Type of COSEM-GET service
  enum typeGet { GET_NORMAL, GET_WITH_LIST, GET_ONE_BLOCK, GET_LAST_BLOCK };

  // TCP service
  enum tcpService { CONNECT, DATA, DISCONNECT, ABORT };	

  // ACSE services
  enum typeAcseService { OPEN, RELEASE };

protected:

  // A pointer to a CosemServer_AP object (cross-reference) 
  Ptr<CosemApClient> m_cosemApClient; 

  // A pointer to a Wrapper Client object
  Ptr<UdpCosemWrapperClient> m_cosemWrapperClient;

  int m_typeService;  // Type of service
  int m_typeGet;   // Type of COSEM-GET service
  int m_stateCf;  // State of CF
  uint16_t m_udpPort;  // Udp port
 
  // Helpers parameters
  EventId m_changeStateEvent;
  EventId m_sendApduEvent;
  EventId m_invokeCosemServiceEvent;

};

} // namespace ns3

#endif /* COSEM_AL_CLIENT_H */
