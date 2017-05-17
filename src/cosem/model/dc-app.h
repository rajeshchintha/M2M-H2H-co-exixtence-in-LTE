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

#ifndef DC_APP_H
#define DC_APP_H

#include "ns3/object.h"
#include "ns3/application.h"
#include "ns3/address.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"

namespace ns3 {

class Socket;
class Packet;
class CosemApClient;
class DcMemory;

/** 
 * Application for managing Data Concentrator's receiving/sending processes
 *
 */

class DataConcentratorApplication : public Application
{
public:
  static TypeId GetTypeId (void);
  
  DataConcentratorApplication ();
 
  virtual ~DataConcentratorApplication ();

  // Send data to the Smart Grid Center
  void Send (Ptr<Packet> packet);
	
  // Receive data from Smart Meter Network (CosemApplication) or requests from Smart Grid Center
  void RecvSm (uint32_t sizeData);
  void RecvSg (Ptr<Socket> socket);

  // Set & GET the SmatGridCenterAddress
  void SetSgCenterAddress (Address ip);
  Address GetSgCenterAddress ();

  // Set & GET the local Ip address (Cap)
  void SetLocalAddress (Address ip);
  Address GetLocalAddress ();

  // Set & GET the cosemApClient pointer
  void SetCosemApClient (Ptr<CosemApClient> cosemApClient);
  Ptr<CosemApClient> GetCosemApClient ();
  
  // Set & GET the DcMemory pointer
  void SetDcMemory (Ptr<DcMemory> dcMemory);
  Ptr<DcMemory> GetDcMemory ();

  // Memory processes 
  enum MemoryProcess { STORE, RETRIEVE }; 

protected:

  virtual void DoDispose (void);
 
private:

  virtual void StartApplication (void);
  virtual void StopApplication (void);

  Ptr<Socket> m_socket;
  Ptr<CosemApClient> m_cosemApClient; 
  Ptr<DcMemory> m_dcMemory;
  Address m_sgCenterAddress;
  Address m_localAddress;  // Local Ip address 
  uint32_t m_cacheMemory; // "cache Memory"

  // Helpers parameters
  uint32_t m_numberBlocks;
  uint32_t m_currentNumberBlock;
  EventId m_sendEvent;
};


/** 
 * Data Concentrator's Memory
 *
 */
class DcMemory : public Object
{
public:
  static TypeId GetTypeId (void);
  
  DcMemory ();
 
  virtual ~DcMemory ();

  // Access to DC's memory
  uint32_t Access (uint32_t data, uint8_t memoryProcess);

  // Store data to  DC's memory
  void Store (uint32_t data);

  // Retrieve data from DC's memory
  uint32_t Retrieve ();

  // Reset DC's memory
  void Reset ();
	
  // Memory processes 
  enum MemoryProcess { STORE, RETRIEVE };  

private:
  uint32_t m_memory; // stores 4Bytes of information (32 bits)
};

} // namespace ns3

#endif /* DC_APP_H */
