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

#ifndef DR_HEADER_H
#define DR_HEADER_H

#include "ns3/header.h"
#include "ns3/nstime.h"
#include "ns3/address.h"
#include "ns3/address-utils.h"

namespace ns3 {

enum MessageType 
{ 
  PRICE = 1, 
  CONTROL = 2,
  METER_POLL_REQ = 3, 
  METER_POLL_REQ_NEXT = 4,
  METER_POLL_RES_NORMAL = 5,
  METER_POLL_RES_BLOCK = 6, 
};

class TypeMessage : public Header
{
public:
  TypeMessage  ();
  virtual ~TypeMessage  ();

  static TypeId GetTypeId (void);
  virtual TypeId GetInstanceTypeId (void) const;
  virtual uint32_t GetSerializedSize (void) const; 
  virtual void Serialize (Buffer::Iterator start) const;
  virtual uint32_t Deserialize (Buffer::Iterator start);
  virtual void Print (std::ostream &os) const;

  // Allow protocol-specific access to the header
  void SetMessageType (MessageType type);
  MessageType GetMessageType () const; 

private:

  MessageType m_type;
};

/*
 * Price Message Header: PRICE
 */

class PriceMessageHeader : public Header
{
public:
  PriceMessageHeader ();
  virtual ~PriceMessageHeader ();

  static TypeId GetTypeId (void);
  virtual TypeId GetInstanceTypeId (void) const;
  virtual uint32_t GetSerializedSize (void) const; 
  virtual void Serialize (Buffer::Iterator start) const;
  virtual uint32_t Deserialize (Buffer::Iterator start);
  virtual void Print (std::ostream &os) const;

  // Allow protocol-specific access to the header 
  void SetPrice (uint16_t price);
  uint16_t GetPrice (void) const; 
  void SetCustomerId (uint32_t customerId);
  uint32_t GetCustomerId ();
 	
private:

  uint16_t m_price;  // Price of kWh (2B)
  uint32_t m_customerId; // Customer Id (4B)
};

/*
 *  Control Message Header: CONTROL
 */

class ControlMessageHeader : public Header
{
public:
  ControlMessageHeader ();
  virtual ~ControlMessageHeader ();

  static TypeId GetTypeId (void);
  virtual TypeId GetInstanceTypeId (void) const;
  virtual uint32_t GetSerializedSize (void) const; 
  virtual void Serialize (Buffer::Iterator start) const;
  virtual uint32_t Deserialize (Buffer::Iterator start);
  virtual void Print (std::ostream &os) const;

  // Allow protocol-specific access to the header  
  void SetCommand (uint32_t command);
  uint32_t GetCommand (void) const;
  void SetCustomerId (uint32_t customerId);
  uint32_t GetCustomerId ();

  	
private:

  uint32_t m_command;  // Command: (1) For direct controllable loads; (2) For demand reducction signals (4B)
  uint32_t m_customerId; // Customer Id (4B)
};

/*
 * METER POLL REQUEST MESSAGE: Request meter data to the data concentrator
 */

class MeterPollRequestMessageHeader : public Header
{
public:
  MeterPollRequestMessageHeader ();
  virtual ~MeterPollRequestMessageHeader ();

  static TypeId GetTypeId (void);
  virtual TypeId GetInstanceTypeId (void) const;
  virtual uint32_t GetSerializedSize (void) const; 
  virtual void Serialize (Buffer::Iterator start) const;
  virtual uint32_t Deserialize (Buffer::Iterator start);
  virtual void Print (std::ostream &os) const;

  // Allow protocol-specific access to the header  
  void SetReadingType (uint8_t readingType);
  uint8_t GetReadingType (void) const;
  void SetReadingTime (uint32_t readingTime);
  uint32_t GetReadingTime (void) const;

private:

  uint8_t m_readingType; // Electric parameter. Ex. Total Average Active Power (1B)
  uint32_t m_readingTime; // Reading time to poll the meter data (4B)
};

/*
 * METER POLL REQUEST NEXT MESSAGE : Request next meter data block to the data concentrator
 */

class MeterPollRequestNextMessageHeader : public Header
{
public:
  MeterPollRequestNextMessageHeader ();
  virtual ~MeterPollRequestNextMessageHeader ();

  static TypeId GetTypeId (void);
  virtual TypeId GetInstanceTypeId (void) const;
  virtual uint32_t GetSerializedSize (void) const; 
  virtual void Serialize (Buffer::Iterator start) const;
  virtual uint32_t Deserialize (Buffer::Iterator start);
  virtual void Print (std::ostream &os) const;

  // Allow protocol-specific access to the header  
  void SetBlockNumber (uint16_t blockNumber);
  uint16_t GetBlockNumber (void) const;

private:

  uint16_t m_blockNumber; // Number of the meter data block received before (2B)
};

/*
 * METER POLL RESPONSE NORMAL MESSAGE : Response of data to the data concentrator to Control Center (NORMAL)
 */

class MeterPollResponseNormalMessageHeader : public Header
{
public:
  MeterPollResponseNormalMessageHeader ();
  virtual ~MeterPollResponseNormalMessageHeader ();

  static TypeId GetTypeId (void);
  virtual TypeId GetInstanceTypeId (void) const;
  virtual uint32_t GetSerializedSize (void) const; 
  virtual void Serialize (Buffer::Iterator start) const;
  virtual uint32_t Deserialize (Buffer::Iterator start);
  virtual void Print (std::ostream &os) const;

  // Allow protocol-specific access to the header  
  void SetMeterData (uint32_t meterData);
  uint32_t GetMeterData (void) const;
  void SetLength (uint16_t length); 
  uint16_t GetLength (void) const;  
  void SetRemoteAddress (Ipv4Address remoteAddress); 
  Ipv4Address GetRemoteAddress (void) const;  


private:

  uint32_t m_meterData; // Meter Data (4B)
  uint16_t m_length;   // Length of the complete meter data block stored by the Data Concentrator (2B)
  Ipv4Address m_remoteAddress; // Remote Address (DataConcentrator/SmartMeter) (4B)
};

/*
 * METER POLL RESPONSE BLOCK MESSAGE : Response of data to the data concentrator to Control Center (BLOCKS)
 */

class MeterPollResponseBlockMessageHeader : public Header
{
public:
  MeterPollResponseBlockMessageHeader ();
  virtual ~MeterPollResponseBlockMessageHeader ();

  static TypeId GetTypeId (void);
  virtual TypeId GetInstanceTypeId (void) const;
  virtual uint32_t GetSerializedSize (void) const; 
  virtual void Serialize (Buffer::Iterator start) const;
  virtual uint32_t Deserialize (Buffer::Iterator start);
  virtual void Print (std::ostream &os) const;

  // Allow protocol-specific access to the header  
  void SetMeterData (uint32_t meterData);
  uint32_t GetMeterData (void) const;
  void SetBlockNumber (uint16_t blockNumber);
  uint16_t GetBlockNumber (void) const;
  void SetLength (uint16_t length); 
  uint16_t GetLength (void) const;  
  void SetLastBlock (bool lastBlock);
  bool GetLastBlock (void) const;

private:

  uint32_t m_meterData; // Meter Data (4B)
  uint16_t m_blockNumber; // Number of the current block sent by the Data Concetrator (2B)
  uint16_t m_length;   // Length of the complete meter data block stored by the Data Concentrator (2B)
  bool m_lastBlock; // Flag to indicate that the current block is the last one (1B)
};

} // namespace ns3

#endif /* DR_HEADER_H */
