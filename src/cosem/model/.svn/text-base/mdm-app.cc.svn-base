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
#include "ns3/mdm-app.h"
#include "dr-header.h"
#include "dc-app.h"


namespace ns3 {

NS_LOG_COMPONENT_DEFINE ("MeterDataManagementApplication");
NS_OBJECT_ENSURE_REGISTERED (MeterDataManagementApplication);

TypeId 
MeterDataManagementApplication::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::MeterDataManagementApplication")
    .SetParent<Application> ()
    .AddConstructor<MeterDataManagementApplication> ()
    ;
  return tid;
}

MeterDataManagementApplication::MeterDataManagementApplication ()
{
  // For debugging purposes
  //NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s DrApp created!");
  m_nextTimeRequest = Seconds (0.0);
  m_meterData = 0;
  m_partialMeterData = 0;
  m_length = 0;
  m_sendEvent = EventId ();
  m_requestEvent = EventId ();
  m_demandResponseMechanismEvent = EventId ();
  m_accessDataBaseServerEvent = EventId ();
}

MeterDataManagementApplication::~MeterDataManagementApplication ()
{
  
}

void 
MeterDataManagementApplication::Send (Ptr<Packet> packet, Address dcDrAddress, uint8_t recipientType)
{
  NS_ASSERT (m_sendEvent.IsExpired ());
  Simulator::Cancel (m_sendEvent);

  if (Ipv4Address::IsMatchingType(dcDrAddress) == true)
    {
      if (recipientType == DC)
        {
          // Connect Control Center to the current remote Data Concentrator
          m_socket->Connect (InetSocketAddress (Ipv4Address::ConvertFrom (dcDrAddress), 5050));

          // Call SEND Udp function (through UdpSocket)
          m_socket->Send (packet); 
          NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CC (MDM) (" 
                                               << Ipv4Address::ConvertFrom (m_localAddress) 
                                               << ") sent a request (" << packet->GetSize () << "B) to DC ("
                                               << Ipv4Address::ConvertFrom (dcDrAddress) << ")");
        }

        if (recipientType == DR)
        {
          // Connect Control Center to the current remote Data Response System
          m_socket->Connect (InetSocketAddress (Ipv4Address::ConvertFrom (dcDrAddress), 5050));

          // Call SEND Udp function (through UdpSocket)
          m_socket->Send (packet); 
          NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CC (MDM) (" 
                                               << Ipv4Address::ConvertFrom (m_localAddress) 
                                               << ") sent data (" << packet->GetSize () << "B) to DR System ("
                                               << Ipv4Address::ConvertFrom (dcDrAddress) << ")");
        }
    } 
}	

void 
MeterDataManagementApplication::Recv (Ptr<Socket> socket)
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
   
     if (typeMessage == METER_POLL_RES_NORMAL)
       { 
         // Record the received information
         MeterPollResponseNormalMessageHeader hdr;
         pkt->RemoveHeader (hdr);
         m_meterData = hdr.GetMeterData (); //---> check it when the CC is attending more than one DC: can m_meterData being overwritten?
         NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CC (MDM) (" 
                                                   << Ipv4Address::ConvertFrom (m_localAddress) 
                                                   << ") received " << hdr.GetLength () << "B of data from DC ("
                                                   << InetSocketAddress::ConvertFrom (from).GetIpv4 () << ")");

         //Event: Save the received data into the Data Base Server
         Address currentDcAddress = Address (InetSocketAddress::ConvertFrom (from).GetIpv4 ());
         m_accessDataBaseServerEvent = Simulator::Schedule (Seconds (0.0), &MeterDataManagementApplication::AccessDataBaseServer, this, currentDcAddress, m_meterData, STORE);
       }

     if (typeMessage == METER_POLL_RES_BLOCK)
       { 
         // Record the received information
         MeterPollResponseBlockMessageHeader hdr;
         pkt->RemoveHeader (hdr);

         if (hdr.GetLastBlock () == 0)
           {
             m_partialMeterData += hdr.GetMeterData (); //---> check it when the CC is attending more than one DC
              
             if (hdr.GetBlockNumber () == 1)
                m_length = hdr.GetLength ();

             NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CC (MDM) (" << Ipv4Address::ConvertFrom (m_localAddress) 
                                                          << ") received block (n = " << hdr.GetBlockNumber ()
                                                          << ") " << m_partialMeterData << "B of " <<  m_length << "B from DC ("
                                                          << InetSocketAddress::ConvertFrom (from).GetIpv4 () << ")");
             
             // Event: Request the next block of data
             m_requestNextBlockEvent = Simulator::Schedule (Seconds (0.0), &MeterDataManagementApplication::RequestNextBlock, this, hdr.GetBlockNumber ());

           }
         else
           {
             m_partialMeterData += hdr.GetMeterData ();  //---> check it when the CC is attending more than one DC
             m_meterData = m_partialMeterData;
             m_partialMeterData = 0;

             NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CC (MDM) (" 
                                                   << Ipv4Address::ConvertFrom (m_localAddress) 
                                                   << ") received block (LAST) " <<  m_meterData << "B of " 
                                                   <<  m_length << "B from DC ("
                                                   << InetSocketAddress::ConvertFrom (from).GetIpv4 () << ")");
              
            //Event: Save the received data into the Data Base Server
            Address currentDcAddress = Address (InetSocketAddress::ConvertFrom (from).GetIpv4 ());
            m_accessDataBaseServerEvent = Simulator::Schedule (Seconds (0.0), &MeterDataManagementApplication::AccessDataBaseServer, this, currentDcAddress, m_meterData, STORE);
            
            } 
         }
      }
}

void 
MeterDataManagementApplication::Request ()
{
  NS_ASSERT (m_requestEvent.IsExpired ());
  Simulator::Cancel (m_requestEvent);

  TypeMessage typeHdr; 
  // Create a Demand Response packet 
  Ptr<Packet> packet = Create<Packet> (); 

  // Build the Demand Response Message and add header into the packet
  MeterPollRequestMessageHeader hdr;
  hdr.SetReadingType (0); // Request the Total Average Active energy (kWh)
  hdr.SetReadingTime (m_readingTime); // Time to read the smart Meter by Data Concentrator 
  packet->AddHeader (hdr); // Add the Demand Response Message header into the packet 

  typeHdr.SetMessageType (METER_POLL_REQ); // Define the Message type 
  packet->AddHeader (typeHdr); 

  // Request of data to Data Concentrators at AMI network
  std::vector<Ptr<Application> >::const_iterator i;
  Time t = m_nextTimeRequest; // Next YY secs (established at script) 
  uint32_t j = 0;
  for (i = m_containerDcApp.Begin (); i != m_containerDcApp.End (); ++i)
    { 
      Ptr<Application> app = m_containerDcApp.Get (j++); 
      Ptr<DataConcentratorApplication> dcApp = app->GetObject<DataConcentratorApplication> (); 
      Address currentDcAddress = dcApp->GetLocalAddress ();
      // Event: Send the PDU to the lower layers ---> each event has independent argument (i.e. currentDcAddress)???
      m_sendEvent = Simulator::Schedule (t, &MeterDataManagementApplication::Send, this, packet, currentDcAddress, DC);
      t += Seconds (0.001); // increase by 1ms 
    }
 
  // Event: Set a timer that permits to request new data to the AMI's Data Concentrators
  m_requestEvent = Simulator::Schedule (m_nextTimeRequest, &MeterDataManagementApplication::Request, this);
}

void 
MeterDataManagementApplication::RequestNextBlock (uint16_t blockNumber)
{
  NS_ASSERT (m_requestNextBlockEvent.IsExpired ());
  Simulator::Cancel (m_requestNextBlockEvent);

  TypeMessage typeHdr; 
  // Create a Demand Response packet 
  Ptr<Packet> packet = Create<Packet> (); 

  // Build the Demand Response Message and add header into the packet
  MeterPollRequestNextMessageHeader hdr;
  hdr.SetBlockNumber (blockNumber); // Number of the meter data block received before
  packet->AddHeader (hdr); // Add the Demand Response Message header into the packet 

  typeHdr.SetMessageType (METER_POLL_REQ_NEXT); // Define the Message type 
  packet->AddHeader (typeHdr); 

  // Request the next block of data to Data Concentrators at AMI network
  std::vector<Ptr<Application> >::const_iterator i;
  Time t = Seconds(0.0); // Next YY secs (established at script) 
  uint32_t j = 0;
  for (i = m_containerDcApp.Begin (); i != m_containerDcApp.End (); ++i)
    { 
      Ptr<Application> app = m_containerDcApp.Get (j++); 
      Ptr<DataConcentratorApplication> dcApp = app->GetObject<DataConcentratorApplication> (); 
      Address currentDcAddress = dcApp->GetLocalAddress ();
      // Event: Send the PDU to the lower layers ---> each event has independent argument (i.e. currentDcAddress)???
      m_sendEvent = Simulator::Schedule (t, &MeterDataManagementApplication::Send, this, packet, currentDcAddress, DC);
      t += Seconds (0.001); // increase by 1ms 
    }
}

void 
MeterDataManagementApplication::SetApplicationContainerDcApp (ApplicationContainer containerDcApp)
{
 m_containerDcApp = containerDcApp;
}

void 
MeterDataManagementApplication::SetLocalAddress (Address ip)
{
  m_localAddress = ip;
}

Address
MeterDataManagementApplication::GetLocalAddress ()
{
  return m_localAddress;
}

void 
MeterDataManagementApplication::SetNextTimeRequest (Time nextTimeRequest)
{
  m_nextTimeRequest = nextTimeRequest;
}

Time 
MeterDataManagementApplication::GetNextTimeRequest ()
{
  return m_nextTimeRequest;
}

void
MeterDataManagementApplication::SetReadingTime (uint32_t readingTime)
{
  m_readingTime = readingTime;
}

uint32_t
MeterDataManagementApplication::GetReadingTime ()
{ 
  return m_readingTime;
}

void 
MeterDataManagementApplication::SetDataBaseServer (Ptr<DataBaseServer> dataBaseServer)
{
  m_dataBaseServer = dataBaseServer;
}

Ptr<DataBaseServer> 
MeterDataManagementApplication::GetDataBaseServer ()
{
  return m_dataBaseServer;
}

void
MeterDataManagementApplication::AccessDataBaseServer (Address DcMeterAddress, uint32_t data, uint8_t DataBaseProcess)
{
  NS_ASSERT (m_accessDataBaseServerEvent.IsExpired ());
  Simulator::Cancel (m_accessDataBaseServerEvent);

  if (DataBaseProcess == STORE)
    {	
      m_dataBaseServer->Access (DcMeterAddress, m_meterData, STORE);

      // callback for DrApp
      if (!m_drCb.IsNull ())
        {
          m_drAddress = m_drCb ();
          TypeMessage typeHdr; 
          // Create a Demand Response packet 
          Ptr<Packet> packet = Create<Packet> ();

          // Build the Demand Response Message and add header into the packet
          MeterPollResponseNormalMessageHeader hdr;
          hdr.SetMeterData (data);
          hdr.SetLength (data);
          hdr.SetRemoteAddress (Ipv4Address::ConvertFrom(DcMeterAddress));          
          packet->AddHeader (hdr); // Add the Demand Response Message header into the packet 

          typeHdr.SetMessageType (METER_POLL_RES_NORMAL); // Define the Message type 
          packet->AddHeader (typeHdr);

          // Event: Send the Meter Data requested to the Demand Response System to be analyzed
          m_sendEvent = Simulator::Schedule (Seconds(0.0), &MeterDataManagementApplication::Send, this, packet, m_drAddress, DR);
        }   
    }

  if (DataBaseProcess == RETRIEVE)
    {
     //MeterDataManagementApplication::Iterator itr = m_dbServer->Access (DcMeterAddress, m_meterData, STORE);
    } 
}

void 
MeterDataManagementApplication::SetDrAddressCallback (Callback<Address> drCb)
{
  m_drCb = drCb;
}

void
MeterDataManagementApplication::DoDispose (void)
{
  NS_LOG_FUNCTION (this);
  Application::DoDispose ();
}

void 
MeterDataManagementApplication::StartApplication (void)
{
  NS_LOG_FUNCTION_NOARGS ();

  // Create the socket
  if (m_socket == 0)
    {
      TypeId tid = TypeId::LookupByName ("ns3::UdpSocketFactory");
      m_socket = Socket::CreateSocket (GetNode (), tid);
      InetSocketAddress local = InetSocketAddress (Ipv4Address::GetAny (), 5050);
      m_socket->Bind (local);
    }

  m_socket->SetRecvCallback (MakeCallback (&MeterDataManagementApplication::Recv, this));

  // Event: Request data to AMI's Data Concentrator for the first time
  m_requestEvent = Simulator::Schedule (Seconds(0.0), &MeterDataManagementApplication::Request, this);
}

void 
MeterDataManagementApplication::StopApplication (void)
{
  // Cancel Events
  Simulator::Cancel (m_sendEvent);
  Simulator::Cancel (m_requestEvent);
  Simulator::Cancel (m_requestNextBlockEvent);
  Simulator::Cancel (m_demandResponseMechanismEvent);
}

/*-----------------------------------------------------------------------------
 *  Data Base Server
 *-----------------------------------------------------------------------------
 */

NS_OBJECT_ENSURE_REGISTERED (DataBaseServer);

TypeId 
DataBaseServer::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::DataBaseServer")
    .SetParent<Object> ()
    .AddConstructor<DataBaseServer> ()
    ;
  return tid;
}

DataBaseServer::DataBaseServer ()
{
 NS_LOG_FUNCTION_NOARGS ();
}

DataBaseServer::~DataBaseServer ()
{
  NS_LOG_FUNCTION_NOARGS ();
}

DataBaseServer::Iterator
DataBaseServer::Access (Address DcMeterAddress, uint32_t data, uint8_t DataBaseProcess)
{
  if (DataBaseProcess == STORE)
    {	
      // Store the data in the data base
      Store (data, DcMeterAddress);   
    }

  if (DataBaseProcess == RETRIEVE)
    {
      // Retreive the iterator of the map container
      Iterator itr = Retrieve ();
      return itr;
    } 
  return DataBaseServer::Iterator (NULL);
}

void 
DataBaseServer::Store (uint32_t data, Address DcMeterAddress)
{
  m_AddressData[DcMeterAddress] = data;
}

DataBaseServer::Iterator
DataBaseServer::Retrieve ()
{
  return m_it;
}	

} // namespace ns3
