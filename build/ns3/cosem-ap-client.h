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

#ifndef COSEM_AP_CLIENT_H
#define COSEM_AL_CLIENT_H

#include <map>
#include "ns3/callback.h"
#include "ns3/ptr.h"
#include "ns3/application.h"
#include "ns3/address.h"
#include "ns3/application-container.h"
#include "ns3/event-id.h"
#include <stdint.h>

namespace ns3 {

class Node;
class Packet;
class Time;
class CosemAlClient;
class CosemApServer;

/** 
 * COSEM Application Process (AP) - Client
 *
 */

class CosemApClient : public Application
{
public:
  static TypeId GetTypeId (void);
  
  CosemApClient ();
 
  virtual ~CosemApClient ();

  // Called when new packet received
  void Recv (Ptr<Packet> packet, int typeAcseService, int typeGet, Ptr<CosemApServer> cosemApServer);

  // Start the request of data to the SAP by the CAP
  void StartRequest ();

  // New request of data to the SAP by the CAP
  void NewRequest ();

  //Request the Release of AAs
  void RequestRelease ();

  // Store the AAs succesfully established
  void SaveActiveAa (Ptr<CosemApServer> cosemApServer);
	
  // Remove the AAs succesfully established before
  void RemoveActiveAa (Ptr<CosemApServer> cosemApServer);

  // Notify Data Concentrator Application when new data is available to be read. 
  void SetRecvCallback (Callback<void, uint32_t> recvData);

  // Set & GET the pointer to a CosemAlClient object
  void SetCosemAlClient (Ptr<CosemAlClient> cosemAlClient);
  Ptr<CosemAlClient> GetCosemAlClient ();

  // Set & GET the wPort
  void SetWport (uint16_t wPort);
  uint16_t GetWport ();

  // Set & GET the Udp Port listening by the CAL
  void SetUdpport (uint16_t udpPort);
  uint16_t GetUdpport ();

  // Set & GET the local Ip address (Cap)
  void SetLocalAddress (Address ip);
  Address GetLocalAddress ();

  // Retrieve a reference of ApplicationContainer object (Sap)
  void SetApplicationContainerSap (ApplicationContainer containerSap);

  // Set & Get the pointer of the Current CosemApServer
  void SetCurrentCosemApServer (Ptr<CosemApServer> CurrentCosemApServer);
  Ptr<CosemApServer> GetCurrentCosemApServer();

  // Set & Get the type of requesting mechanism
  void SetTypeRequesting (bool typeRequesting);
  bool GetTypeRequesting ();
 
  // Set & Get the next time that the Client(DC) requests to the Servers (SmartMeter,SM)
  void SetNextTimeRequest (Time nextTimeRequest);
  Time GetNextTimeRequest ();

  // Retrieve the node where the CAP is attached
  Ptr<Node> GetNode () const;

  // Retrieve the size in Bytes of the requested Data sent by the remote SAP
  uint32_t GetSizeReqData (); 
  
  // Type of services
  enum typeService { REQUEST, INDICATION, RESPONSE, CONFIRM };

  // Type of COSEM-GET service
  enum typeGet { GET_NORMAL, GET_WITH_LIST, GET_ONE_BLOCK, GET_LAST_BLOCK };

  // ACSE services
  enum typeAcseService { OPEN, RELEASE }; 

  // States machine of the Control Function
  enum stateCf { CF_INACTIVE, CF_IDLE, CF_ASSOCIATION_PENDING, CF_ASSOCIATED, CF_ASSOCIATION_RELEASE_PENDING };
	
protected:

  virtual void DoDispose (void);

private:

  Ptr<CosemAlClient> m_cosemAlClient;
  uint16_t m_wPort;  // CAP Wrapper Port Number (unique id)
  uint16_t m_udpPort;  // Udp port
  Address m_localAddress;  // Local Ip address 
  ApplicationContainer m_containerSap; // Container of Sap in the scenario
  Ptr<CosemApServer> m_currentCosemApServer;  // Pointer of the current remote SAP 
  Time m_nextTimeRequest; // Next time request of data to SAPs
  bool m_typeRequesting;  // Type Requesting mechanism: TRUE = MULTICASTING (simultaneous); FALSE = SEQUENCIAL (Round Robin style)
  uint32_t m_reqData; // The requested Data sent by the remote SAP
  uint32_t m_sizeReqData; // Size in Bytes of the requested Data sent by the remote SAP
  
  // Callback for DataConcentrator only
  Callback<void, uint32_t> m_recvData; 

  // Map container to store the AAs succesfully established 
  std::map<uint16_t, Ptr<CosemApServer> > m_activeAa;	
  std::map<uint16_t, Ptr<CosemApServer> >::iterator m_it;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             virtual void StartApplication (void);

  virtual void StopApplication (void);

  // Helpers parameters
  EventId m_startRequestEvent;
  EventId m_nextRequestEvent;
  EventId m_releaseAAEvent;
  std::vector<Ptr<Application> >::const_iterator m_itSap; // Iterator AppContainer
  uint16_t m_nSap;  // Number of Saps 
  uint32_t m_totalNSap;  // Total Number of Saps   
  uint8_t m_enableNewRQ;         
};

} // namespace ns3

#endif /* COSEM_AP_CLIENT_H */
