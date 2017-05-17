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
#include "dr-header.h"
 
namespace ns3 {

NS_LOG_COMPONENT_DEFINE ("TypeMessage");

/*-----------------------------------------------------------------------------
 *  DEMAND RESPONSE MESSAGE TYPE HEADER
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (TypeMessage);

TypeId 
TypeMessage::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::TypeMessage")
    .SetParent<Header> ()
    .AddConstructor<TypeMessage> ()
    ;
  return tid;
}

TypeMessage::TypeMessage  ()
{
  m_type = METER_POLL_REQ;
}

TypeMessage::~TypeMessage ()
{

}

TypeId 
TypeMessage::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
TypeMessage::GetSerializedSize (void) const
{
  return 1;
}
 
void 
TypeMessage::Serialize (Buffer::Iterator start) const
{
  start.WriteU8 ((uint8_t) m_type);
}

uint32_t 
TypeMessage::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  uint8_t type = i.ReadU8 ();
  switch (type)
   {
     case PRICE: 
     case CONTROL:
     case METER_POLL_REQ:
     case METER_POLL_REQ_NEXT:
     case METER_POLL_RES_NORMAL:
     case METER_POLL_RES_BLOCK:
      {
        m_type = (MessageType) type;
        break;
      }
    default:
      NS_LOG_INFO ("MessageType not valid!");  
   }  
  return GetSerializedSize ();
}

void 
TypeMessage::Print (std::ostream &os) const
{  
 switch (m_type)
   {
    case PRICE:
      {
        os << "PRICE";
        break;
      } 
    case CONTROL:
      {
        os << "CONTROL";
        break;
      }
    case METER_POLL_REQ:
      {
        os << "METER_POLL_REQ";
        break;
      }
    case METER_POLL_REQ_NEXT: 
      {
        os << "METER_POLL_REQ_NEXT";
        break;
      }
    case METER_POLL_RES_NORMAL: 
      {
        os << "METER_POLL_RES_NORMAL";
        break;
      }
    case METER_POLL_RES_BLOCK:
      {
        os << "METER_POLL_RES_BLOCK";
        break;
      }
    default:
      os << "UNKNOWN_TYPE";
   }  
}

void 
TypeMessage::SetMessageType (MessageType type)
{
  m_type = type;
}

MessageType
TypeMessage::GetMessageType () const 
{
  return m_type;
}

/*-----------------------------------------------------------------------------
 *  PRICE MESSAGE
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (PriceMessageHeader);

TypeId 
PriceMessageHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::PriceMessageHeader")
    .SetParent<Header> ()
    .AddConstructor<PriceMessageHeader> ()
    ;
  return tid;
}

PriceMessageHeader::PriceMessageHeader ()
{
  m_price = 0;  
}

PriceMessageHeader::~PriceMessageHeader ()
{

}

TypeId 
PriceMessageHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
PriceMessageHeader::GetSerializedSize (void) const
{
  return 6;
}
 
void 
PriceMessageHeader::Serialize (Buffer::Iterator start) const
{
  start.WriteHtonU16 (m_price);
  start.WriteHtonU32 (m_customerId);
}

uint32_t 
PriceMessageHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  m_price = i.ReadNtohU16 ();
  m_customerId = i.ReadNtohU32 ();
  return GetSerializedSize ();
}

void 
PriceMessageHeader::Print (std::ostream &os) const
{
  os << "Price " << m_price
     << "Customer Id - Meter Address " << m_customerId;
}

void 
PriceMessageHeader::SetPrice (uint16_t price)
{
  m_price = price;
}
uint16_t 
PriceMessageHeader::GetPrice (void) const
{
  return m_price;
}
void 
PriceMessageHeader::SetCustomerId (uint32_t customerId)
{
  m_customerId = customerId;
}

uint32_t 
PriceMessageHeader::GetCustomerId ()
{
 return m_customerId;
}
 	


/*-----------------------------------------------------------------------------
 *  CONTROL MESSAGE
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (ControlMessageHeader);

TypeId 
ControlMessageHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::ControlMessageHeader")
    .SetParent<Header> ()
    .AddConstructor<ControlMessageHeader> ()
    ;
  return tid;
}

ControlMessageHeader::ControlMessageHeader ()
{
  m_command = 0; // Disconnect Meter
}

ControlMessageHeader::~ControlMessageHeader ()
{

}

TypeId 
ControlMessageHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
ControlMessageHeader::GetSerializedSize (void) const
{
  return 8;
}
 
void 
ControlMessageHeader::Serialize (Buffer::Iterator start) const
{
  start.WriteHtonU32 (m_command);
  start.WriteHtonU32 (m_customerId);
}

uint32_t 
ControlMessageHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  m_command = i.ReadNtohU32 ();
  m_customerId = i.ReadNtohU32 ();
  return GetSerializedSize ();
}

void 
ControlMessageHeader::Print (std::ostream &os) const
{
  os << "Command " << m_command
     << "Customer Id - Meter Address " << m_customerId;
}

void 
ControlMessageHeader::SetCommand (uint32_t command)
{
  m_command = command;
}

uint32_t 
ControlMessageHeader::GetCommand (void) const
{
  return m_command;
}

void 
ControlMessageHeader::SetCustomerId (uint32_t  customerId)
{
  m_customerId = customerId;
}

uint32_t 
ControlMessageHeader::GetCustomerId ()
{
  return m_customerId;
}

/*-----------------------------------------------------------------------------
 *  METER POLL REQUEST MESSAGE
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (MeterPollRequestMessageHeader);

TypeId 
MeterPollRequestMessageHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::MeterPollRequestMessageHeader")
    .SetParent<Header> ()
    .AddConstructor<MeterPollRequestMessageHeader> ()
    ;
  return tid;
}

MeterPollRequestMessageHeader::MeterPollRequestMessageHeader ()
{
  m_readingType = 0;
  m_readingTime = 0.0; 
}

MeterPollRequestMessageHeader::~MeterPollRequestMessageHeader ()
{

}

TypeId 
MeterPollRequestMessageHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
MeterPollRequestMessageHeader::GetSerializedSize (void) const
{
  return 5;
}
 
void 
MeterPollRequestMessageHeader::Serialize (Buffer::Iterator start) const
{
  start.WriteU8 (m_readingType);
  start.WriteHtonU32 (m_readingTime);
}

uint32_t 
MeterPollRequestMessageHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  m_readingType = i.ReadU8 ();
  m_readingTime = i.ReadNtohU32 ();
  return GetSerializedSize ();
}

void 
MeterPollRequestMessageHeader::Print (std::ostream &os) const
{
  os << "Electric parameter " <<  m_readingType
     << "Reading time to poll the meter data " << m_readingTime;
}

void 
MeterPollRequestMessageHeader::SetReadingType (uint8_t readingType)
{
  m_readingType = readingType;
}

uint8_t 
MeterPollRequestMessageHeader::GetReadingType (void) const
{
  return m_readingType;
}

void 
MeterPollRequestMessageHeader::SetReadingTime (uint32_t readingTime)
{
  m_readingTime = readingTime;
}

uint32_t
MeterPollRequestMessageHeader::GetReadingTime (void) const
{
  return m_readingTime;
}

/*-----------------------------------------------------------------------------
 *  METER POLL REQUEST NEXT MESSAGE
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (MeterPollRequestNextMessageHeader);

TypeId 
MeterPollRequestNextMessageHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::MeterPollRequestNextMessageHeader")
    .SetParent<Header> ()
    .AddConstructor<MeterPollRequestNextMessageHeader> ()
    ;
  return tid;
}

MeterPollRequestNextMessageHeader::MeterPollRequestNextMessageHeader ()
{
  m_blockNumber = 0;
}

MeterPollRequestNextMessageHeader::~MeterPollRequestNextMessageHeader ()
{

}

TypeId 
MeterPollRequestNextMessageHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
MeterPollRequestNextMessageHeader::GetSerializedSize (void) const
{
  return 2;
}
 
void 
MeterPollRequestNextMessageHeader::Serialize (Buffer::Iterator start) const
{
  start.WriteHtonU16 (m_blockNumber);
}

uint32_t 
MeterPollRequestNextMessageHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  m_blockNumber = i.ReadNtohU16 ();
  return GetSerializedSize ();
}

void 
MeterPollRequestNextMessageHeader::Print (std::ostream &os) const
{
  os << "Block number " << m_blockNumber;
}

void 
MeterPollRequestNextMessageHeader::SetBlockNumber (uint16_t blockNumber)
{
  m_blockNumber = blockNumber;
}

uint16_t 
MeterPollRequestNextMessageHeader::GetBlockNumber (void) const
{
  return m_blockNumber;
}

/*-----------------------------------------------------------------------------
 *  METER POLL RESPONSE NORMAL MESSAGE
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (MeterPollResponseNormalMessageHeader);

TypeId 
MeterPollResponseNormalMessageHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::MeterPollResponseNormalMessageHeader")
    .SetParent<Header> ()
    .AddConstructor<MeterPollResponseNormalMessageHeader> ()
    ;
  return tid;
}

MeterPollResponseNormalMessageHeader::MeterPollResponseNormalMessageHeader ()
{
  m_meterData = 0;
  m_length = 0;
}

MeterPollResponseNormalMessageHeader::~MeterPollResponseNormalMessageHeader ()
{

}

TypeId 
MeterPollResponseNormalMessageHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
MeterPollResponseNormalMessageHeader::GetSerializedSize (void) const
{
  return 10;
}
 
void 
MeterPollResponseNormalMessageHeader::Serialize (Buffer::Iterator start) const
{
  WriteTo (start, m_remoteAddress);
  start.WriteHtonU32 (m_meterData);
  start.WriteHtonU16 (m_length);
}

uint32_t 
MeterPollResponseNormalMessageHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  ReadFrom (i, m_remoteAddress);
  m_meterData = i.ReadNtohU32 ();
  m_length = i.ReadNtohU16 ();
  return GetSerializedSize ();
}

void 
MeterPollResponseNormalMessageHeader::Print (std::ostream &os) const
{
  os << "Remote Data Concentrator/Smart Meter ip address " << m_remoteAddress
     << "Meter Data " << m_meterData
     << "Length of complete meter data " << m_length;
}

void 
MeterPollResponseNormalMessageHeader::SetMeterData (uint32_t meterData)
{
  m_meterData = meterData;
}
uint32_t 
MeterPollResponseNormalMessageHeader::GetMeterData (void) const
{
  return m_meterData;
}
void 
MeterPollResponseNormalMessageHeader::SetLength (uint16_t length)
{
  m_length = length;
}

uint16_t 
MeterPollResponseNormalMessageHeader::GetLength (void) const
{
  return m_length;
}

void 
MeterPollResponseNormalMessageHeader::SetRemoteAddress (Ipv4Address remoteAddress)
{
 m_remoteAddress = remoteAddress;
}

Ipv4Address
MeterPollResponseNormalMessageHeader::GetRemoteAddress (void) const
{
  return m_remoteAddress;
}

/*-----------------------------------------------------------------------------
 *  METER POLL RESPONSE BLOCK MESSAGE
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (MeterPollResponseBlockMessageHeader);

TypeId 
MeterPollResponseBlockMessageHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::MeterPollResponseBlockMessageHeader")
    .SetParent<Header> ()
    .AddConstructor<MeterPollResponseBlockMessageHeader> ()
    ;
  return tid;
}

MeterPollResponseBlockMessageHeader::MeterPollResponseBlockMessageHeader ()
{
  m_meterData = 0;
  m_blockNumber = 0;
  m_length = 0;   
  m_lastBlock = false;
}

MeterPollResponseBlockMessageHeader::~MeterPollResponseBlockMessageHeader ()
{

}

TypeId 
MeterPollResponseBlockMessageHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
MeterPollResponseBlockMessageHeader::GetSerializedSize (void) const
{
  return 9;
}
 
void 
MeterPollResponseBlockMessageHeader::Serialize (Buffer::Iterator start) const
{
  start.WriteHtonU32 (m_meterData);
  start.WriteHtonU16 (m_blockNumber);
  start.WriteHtonU16 (m_length);
  start.WriteU8 ((uint8_t) m_lastBlock);
}

uint32_t 
MeterPollResponseBlockMessageHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  m_meterData = i.ReadNtohU32 (); 
  m_blockNumber = i.ReadNtohU16 (); 
  m_length = i.ReadNtohU16 (); 
  m_lastBlock = (bool)i.ReadU8 ();
  return GetSerializedSize ();
}

void 
MeterPollResponseBlockMessageHeader::Print (std::ostream &os) const
{
  os << "Meter Data " << m_meterData
     << "Block number " << m_blockNumber
     << "Length of complete meter data " << m_length
     << "Last block " <<  m_lastBlock; 
}

void 
MeterPollResponseBlockMessageHeader::SetMeterData (uint32_t meterData)
{
  m_meterData = meterData;
}

uint32_t 
MeterPollResponseBlockMessageHeader::GetMeterData (void) const
{
  return m_meterData;
}

void 
MeterPollResponseBlockMessageHeader::SetBlockNumber (uint16_t blockNumber)
{
  m_blockNumber = blockNumber;
}

uint16_t 
MeterPollResponseBlockMessageHeader::GetBlockNumber (void) const
{
  return m_blockNumber;
}

void 
MeterPollResponseBlockMessageHeader::SetLength (uint16_t length)
{
  m_length = length;
}

uint16_t 
MeterPollResponseBlockMessageHeader::GetLength (void) const 
{
  return m_length;
}

void 
MeterPollResponseBlockMessageHeader::SetLastBlock (bool lastBlock)
{
  m_lastBlock = lastBlock;
}

bool 
MeterPollResponseBlockMessageHeader::GetLastBlock (void) const
{
  return m_lastBlock;
}

} // namespace ns3
