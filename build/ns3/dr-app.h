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

#ifndef DR_APP_H
#define DR_APP_H

#include "ns3/application.h"
#include "ns3/application-container.h"
#include "ns3/address.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"

namespace ns3 {

class Socket;
class Packet;
class MeterDataManagementApplication;


/** 
 * Application for Demand Response (DR): Send/Receive Demand Response messages
 *
 */

class DemandResponseApplication : public Application
{
public:
  static TypeId GetTypeId (void);
  
  DemandResponseApplication ();
 
  virtual ~DemandResponseApplication ();

  // Send demand response/requesting packet to AMI netowrk's Data Concentrator/Meter Data Management Systems
  void Send (Ptr<Packet> packet, Address DcMdmAddress, uint8_t recipientType);
	
  // Receive data from Meter Data Management System
  void Recv (Ptr<Socket> socket);

  // Request data to Meter Data Management System
  void Request ();

  // Build command message to be sent by the Control Center to AMI netowrk's Data Concentrator/Cosem Smart Meter 
  void Command (uint8_t signalType, uint32_t command, Address currentDcAddress, uint32_t customerId);

  // Call Demand Response Mechanism
  void DemandResponseMechanism (uint32_t data, Address currentDcAddres);

  // Set & GET the local Ip address
  void SetLocalAddress (Address localAddress);
  Address GetLocalAddress ();

  // Set & GET the MeterDataManagementApplication pointer
  void SetMdmApp (Ptr<MeterDataManagementApplication> mdmApp);
  Ptr<MeterDataManagementApplication> GetMdmApp ();

  // Type of Demand Response command signal
  enum SignalType { S_CONTROL, S_PRICE }; 

  // Type of Recipient
  enum RecipientType { DC, MDM }; 

protected:

  virtual void DoDispose (void);
 
private:

  virtual void StartApplication (void);
  virtual void StopApplication (void);

  Ptr<Socket> m_socket;
  Address m_localAddress;  // Local Ip address 
  Address m_mdmAddress;  // Meter Data Management System Ip address 
  uint32_t m_meterData; // Meter Data: Input to Demand Response Engine
  Ptr<MeterDataManagementApplication> m_mdmApp;

  // Helpers parameters
  EventId m_sendEvent;
  EventId m_requestEvent;
  EventId m_commandEvent;
  EventId m_demandResponseMechanismEvent;
};

} // namespace ns3

#endif /* DR_APP_H */
