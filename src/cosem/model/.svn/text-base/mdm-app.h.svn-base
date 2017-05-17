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

#ifndef MDM_APP_H
#define MDM_APP_H

#include <map>
#include <stdint.h>
#include "ns3/application.h"
#include "ns3/application-container.h"
#include "ns3/address.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"

namespace ns3 {

class Socket;
class Packet;
class DataBaseServer;

/** 
 * Application for Meter Data Management (MDM): Send/Receive Requesting/Consumption messages
 *
 */

class MeterDataManagementApplication : public Application
{
public:

  static TypeId GetTypeId (void);
  
  MeterDataManagementApplication ();
 
  virtual ~MeterDataManagementApplication ();

  typedef std::map<Address, uint32_t>::iterator Iterator;

  // Send requesting packet (Data) to AMI netowrk's Data Concentrator/ response to Demand Response System
  void Send (Ptr<Packet> packet, Address dcDrAddress, uint8_t recipientType);
	
  // Receive data (consumption signal) from AMI netowrk's Data Concentrator 
  void Recv (Ptr<Socket> socket);

  // Request data to AMI network's Data Concentrator 
  void Request ();

  // Request the next block of data to AMI netowrk's Data Concentrator 
  void RequestNextBlock (uint16_t blockNumber);

  // Retrieve a reference of Application Container (DataConcentratorApplication) object
  void SetApplicationContainerDcApp (ApplicationContainer containerDcApp);

  // Set & GET the local Ip address
  void SetLocalAddress (Address ip);
  Address GetLocalAddress ();

  // Set & Get Next Time Request AMI netowrk's Data Concentrator  
  void SetNextTimeRequest (Time nextTimeRequest);
  Time GetNextTimeRequest ();

  // Set & Get Reading time to poll the meter data 
  void SetReadingTime (uint32_t readingTime);
  uint32_t GetReadingTime ();
  
  // Set & GET the DataBaseServer pointer
  void SetDataBaseServer (Ptr<DataBaseServer> dataBaseServer);
  Ptr<DataBaseServer> GetDataBaseServer ();

  // Access to Data Base
  void AccessDataBaseServer (Address DcMeterAddress, uint32_t data, uint8_t dataBaseProcess);

  // Retreive Demand Response System Address
  void SetDrAddressCallback (Callback<Address> drCb);

  // Data Base processes 
  enum DataBaseProcess { STORE, RETRIEVE };

  // Type of Recipient
  enum RecipientType { DC, DR }; 

protected:

  virtual void DoDispose (void);
 
private:

  virtual void StartApplication (void);
  virtual void StopApplication (void);

  Ptr<Socket> m_socket;
  ApplicationContainer m_containerDcApp; // Container of DcApplication in the scenario
  Address m_localAddress;  // Local Ip address 
  Address m_drAddress;  // Demand Response System address 
  Time m_nextTimeRequest; // Next time request of data to AMI netowrk's Data Concentrator 
  uint32_t m_readingTime; // Reading time to poll the meter data 
  uint32_t m_meterData; // Meter Data: Input to Demand Response Engine
  uint32_t m_partialMeterData; // Parcial Meter Data (block)
  uint16_t m_length;   // Length of the complete meter data block received by the Control Center
  Ptr<DataBaseServer> m_dataBaseServer;

  // Callback for Demand Response System only
  Callback<Address> m_drCb; 

  // Helpers parameters
  EventId m_sendEvent;
  EventId m_requestEvent;
  EventId m_requestNextBlockEvent;
  EventId m_demandResponseMechanismEvent;
  EventId m_accessDataBaseServerEvent;
};

/** 
 * Data Base Server associated with Meter Data Management Application
 *
 */
class DataBaseServer : public Object
{
public:
  static TypeId GetTypeId (void);
  
  DataBaseServer ();
 
  virtual ~DataBaseServer ();

  typedef std::map<Address, uint32_t>::iterator Iterator;

  // Access to Data Base
  Iterator Access (Address DcMeterAddress, uint32_t data, uint8_t memoryProcess);

  // Store data into the Data Base
  void Store (uint32_t data, Address DcMeterAddress);

  // Retrieve data into the Data Base
  Iterator Retrieve ();

  // Data Base processes 
  enum DataBaseProcess { STORE, RETRIEVE };

private:
  
  // Map container to store the address and data associated with Data Concentrator/Cosem Meter
  std::map<Address, uint32_t> m_AddressData; 
  Iterator m_it; 
};

} // namespace ns3

#endif /* MDM_APP_H */
