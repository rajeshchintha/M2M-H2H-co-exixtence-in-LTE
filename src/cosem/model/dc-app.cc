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
#include "ns3/socket.h"
#include "ns3/packet.h"
#include "ns3/ipv4-address.h"
#include "ns3/inet-socket-address.h"
#include "ns3/socket-factory.h"
#include "ns3/simulator.h"
#include "ns3/nstime.h"
#include "dr-header.h"
#include "cosem-ap-client.h"
#include "dc-app.h"

namespace ns3 {

NS_LOG_COMPONENT_DEFINE ("DataConcentratorApplication");
NS_OBJECT_ENSURE_REGISTERED (DataConcentratorApplication);

TypeId 
DataConcentratorApplication::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::DataConcentratorApplication")
    .SetParent<Application> ()
    .AddConstructor<DataConcentratorApplication> ()
    ;
  return tid;
}

DataConcentratorApplication::DataConcentratorApplication ()
{
  // For debugging purposes
  //NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s DcApp created!");
  m_cacheMemory = 0;
  m_numberBlocks = 0;
  m_currentNumberBlock = 0;
  m_sendEvent = EventId ();
}

DataConcentratorApplication::~DataConcentratorApplication ()
{

}

void 
DataConcentratorApplication::Send (Ptr<Packet> packet)
{
  NS_ASSERT (m_sendEvent.IsExpired ());
  Simulator::Cancel (m_sendEvent);

  // Call SEND Udp function (through UdpSocket)
  m_socket->Send (packet); 
  NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s DC (" 
                                               << Ipv4Address::ConvertFrom (m_localAddress) 
                                               << ") sent a reponse (" << packet->GetSize () << "B) to CC (MDM) ("
                                               << Ipv4Address::ConvertFrom (m_sgCenterAddress) << ")");
}	

void 
DataConcentratorApplication::RecvSm (uint32_t sizeData)
{
  Simulator::Schedule (Seconds (0.0), &DcMemory::Access, m_dcMemory, sizeData, STORE); 
}

void 
DataConcentratorApplication::RecvSg (Ptr<Socket> socket)
{
  Ptr<Packet> pkt;
  Address from;
  TypeMessage typeHdr;
  // Retreive the packet sent by the Smart Grid Center
  pkt = socket->RecvFrom (from);  

  if (InetSocketAddress::IsMatchingType (from))
    {
     // Remove and copy the Demand Reponse Message Header from the packet
     pkt->RemoveHeader (typeHdr);
     MessageType typeMessage = typeHdr.GetMessageType (); 
 
     if (typeMessage == METER_POLL_REQ)
       { 
         NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s DC (" 
                                                 << Ipv4Address::ConvertFrom (m_localAddress) 
                                                 << ") received a request of data (" << pkt->GetSize () << "B) from CC (MDM) ("
                                                 << InetSocketAddress::ConvertFrom (from).GetIpv4 () << ")");

         // Record the received information
         MeterPollRequestMessageHeader hdr;
         pkt->RemoveHeader (hdr);

         // Update the time that Data Concentrator must requests to Smart Meters
         m_cosemApClient->SetNextTimeRequest (Seconds(hdr.GetReadingTime ()));

         // Check for data in memory
         uint32_t data = m_dcMemory->Access (0, RETRIEVE); 
         if (data == 0)
           {
             NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s Empty MEMORY! Requesting data to Smart Meters!");
                                                   
             // Doesn't exist data in the memory, request to the SMs
             Simulator::Schedule (Seconds (0.0), &CosemApClient::NewRequest, m_cosemApClient); 
           } 
         else
           {
             // Stores the data temporarily at "cache Memory"
             m_cacheMemory = data;

             // Exist data in the memory, encapsulate them and send a packet to the SG Center 
             TypeMessage typeHdr; 
             m_numberBlocks = (m_cacheMemory/1000 + (m_cacheMemory%1000 ? 1 : 0)); // Maximum packet size to send: 1000 Bytes
             NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s Number of blocks to sent " << m_numberBlocks);
             
             if ((m_numberBlocks > 2) || (m_numberBlocks == 2) )
               {
         
                 // Create a Demand Response packet 
                 Ptr<Packet> packet = Create<Packet> (1000); 

                 // Build the Demand Response Message and add header into the packet
                 MeterPollResponseBlockMessageHeader hdr;
                 hdr.SetMeterData (1000); // the first 1000 bytes
                 hdr.SetBlockNumber (++m_currentNumberBlock); // first block
                 hdr.SetLength (m_cacheMemory);
                     
                 packet->AddHeader (hdr); // Add the Demand Response Message header into the packet 

                 typeHdr.SetMessageType (METER_POLL_RES_BLOCK); // Define the Message type 
                 packet->AddHeader (typeHdr);
               
                 m_sendEvent = Simulator::Schedule (Seconds (0.0), &DataConcentratorApplication::Send, this, packet);

                 // Reset Memory
                 Simulator::Schedule (Seconds (0.0), &DcMemory::Reset, m_dcMemory);
               }
             else
               {
                 // Create a Demand Response packet 
                 Ptr<Packet> packet = Create<Packet> ();

                 // Build the Demand Response Message and add header into the packet
                 MeterPollResponseNormalMessageHeader hdr;
                 hdr.SetMeterData (data);
                 hdr.SetLength (data);
                    
                 packet->AddHeader (hdr); // Add the Demand Response Message header into the packet 

                 typeHdr.SetMessageType (METER_POLL_RES_NORMAL); // Define the Message type 
                 packet->AddHeader (typeHdr);
           
                 m_sendEvent = Simulator::Schedule (Seconds (0.0), &DataConcentratorApplication::Send, this, packet);

                 // Reset Memory
                 Simulator::Schedule (Seconds (0.0), &DcMemory::Reset, m_dcMemory);
               }
           }
       }
     else if (typeMessage == METER_POLL_REQ_NEXT)
       { 
         // Create a Demand Response packet 
         Ptr<Packet> packet = Create<Packet> (); 

         // Build the Demand Response Message and add header into the packet
         MeterPollResponseBlockMessageHeader hdr;
         hdr.SetMeterData (1000); 
         hdr.SetLength (m_cacheMemory);
         hdr.SetBlockNumber (++m_currentNumberBlock);
        
         if (m_currentNumberBlock == m_numberBlocks)
           {
             hdr.SetLastBlock (true);
             uint32_t lastBytesData = m_cacheMemory - 1000*(m_currentNumberBlock-1);

             if (lastBytesData == 0)
               {
                 hdr.SetMeterData (1000); 
               }
             else
               {
                 hdr.SetMeterData (lastBytesData); 
               }
            
             m_currentNumberBlock = 0;
             m_cacheMemory = 0;
           }
          
         packet->AddHeader (hdr); // Add the Demand Response Message header into the packet 

         typeHdr.SetMessageType (METER_POLL_RES_BLOCK); // Define the Message type 
         packet->AddHeader (typeHdr);
               
         m_sendEvent = Simulator::Schedule (Seconds (0.0), &DataConcentratorApplication::Send, this, packet);                 
       }
   
     if (typeMessage == PRICE)
       { 
         NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s DC (" 
                                                 << Ipv4Address::ConvertFrom (m_localAddress) 
                                                 << ") received an udpated rates command (" << pkt->GetSize () << "B) from CC (DR) ("
                                                 << InetSocketAddress::ConvertFrom (from).GetIpv4 () << ")");
         // Record the received information
         PriceMessageHeader hdr;
         pkt->RemoveHeader (hdr);
         // XX-HERE: Call the DLMS/COSEM service associated with update rates
       }
   
     if (typeMessage == CONTROL)
       { 
         NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s DC (" 
                                                 << Ipv4Address::ConvertFrom (m_localAddress) 
                                                 << ") received a control command (" << pkt->GetSize () << "B) from CC ("
                                                 << InetSocketAddress::ConvertFrom (from).GetIpv4 () << ")");
         // Record the received information
         ControlMessageHeader hdr;
         pkt->RemoveHeader (hdr);
         // XX-HERE: Call the DLMS/COSEM service associated with Disconnect command
       }
   }
}

void 
DataConcentratorApplication::SetSgCenterAddress (Address ip)
{
  m_sgCenterAddress = ip;
}

Address 
DataConcentratorApplication::GetSgCenterAddress ()
{
  return m_sgCenterAddress;
}

void 
DataConcentratorApplication::SetLocalAddress (Address ip)
{
  m_localAddress = ip;
}

Address
DataConcentratorApplication::GetLocalAddress ()
{
  return m_localAddress;
}

void 
DataConcentratorApplication::SetCosemApClient (Ptr<CosemApClient> cosemApClient)
{
  m_cosemApClient = cosemApClient;
}

Ptr<CosemApClient> 
DataConcentratorApplication::GetCosemApClient ()
{
  return m_cosemApClient;
}

void 
DataConcentratorApplication::SetDcMemory (Ptr<DcMemory> dcMemory)
{
  m_dcMemory = dcMemory;
}

Ptr<DcMemory> 
DataConcentratorApplication::GetDcMemory ()
{
  return m_dcMemory;
}

void
DataConcentratorApplication::DoDispose (void)
{
  NS_LOG_FUNCTION (this);
  Application::DoDispose ();
}

void 
DataConcentratorApplication::StartApplication (void)
{
  NS_LOG_FUNCTION_NOARGS ();

 // Create the socket
  if (m_socket == 0)
    {
      TypeId tid = TypeId::LookupByName ("ns3::UdpSocketFactory");
      m_socket = Socket::CreateSocket (GetNode (), tid);
      InetSocketAddress local = InetSocketAddress (Ipv4Address::GetAny (), 5050);
      m_socket->Bind (local);
      m_socket->Connect (InetSocketAddress (Ipv4Address::ConvertFrom (m_sgCenterAddress), 5050));
    }

   m_socket->SetRecvCallback (MakeCallback (&DataConcentratorApplication::RecvSg, this));

   // build callback instance which points to DataConcentratorApplication::RecvSm function
   m_cosemApClient->SetRecvCallback (MakeCallback (&DataConcentratorApplication::RecvSm, this)); 
}

void 
DataConcentratorApplication::StopApplication (void)
{

}

/*-----------------------------------------------------------------------------
 *  Data Concentrator's Memory
 *-----------------------------------------------------------------------------
 */

NS_OBJECT_ENSURE_REGISTERED (DcMemory);

TypeId 
DcMemory::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::DcMemory")
    .SetParent<Object> ()
    .AddConstructor<DcMemory> ()
    ;
  return tid;
}

DcMemory::DcMemory ()
{
 NS_LOG_FUNCTION_NOARGS ();
 m_memory = 0;
}

DcMemory::~DcMemory ()
{
  NS_LOG_FUNCTION_NOARGS ();
}

uint32_t
DcMemory::Access (uint32_t data, uint8_t memoryProcess)
{
  if (memoryProcess == STORE)
    {	
      // Store the data in the memory
      Store (data);   
    }

  if (memoryProcess == RETRIEVE)
    {
      // Retreive the data in the memory
      uint32_t ndata = Retrieve ();
      return ndata;
    } 

 return -1;
}

void 
DcMemory::Store (uint32_t data)
{
  // Store data (bytes)
  m_memory += data;
}

uint32_t 
DcMemory::Retrieve ()
{
  // Retrun data (bytes)
  return m_memory;
}

void 
DcMemory::Reset ()
{
  m_memory = 0;
  NS_LOG_INFO (Simulator::Now ().GetSeconds () << " DC's memory reset");
}
	

} // namespace ns3
