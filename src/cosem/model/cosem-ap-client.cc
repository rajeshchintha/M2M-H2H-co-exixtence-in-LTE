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
#include "ns3/node.h"
#include "ns3/packet.h"
#include "ns3/simulator.h"
#include "ns3/nstime.h"
#include "cosem-header.h"
#include "cosem-al-client.h"
#include "cosem-ap-server.h"
#include "cosem-ap-client.h"

namespace ns3 {

NS_LOG_COMPONENT_DEFINE ("CosemApplicationsProcessClient");
NS_OBJECT_ENSURE_REGISTERED (CosemApClient);

TypeId 
CosemApClient::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::CosemApClient")
    .SetParent<Application> ()
    .AddConstructor<CosemApClient> ()
    ;
  return tid;
}

CosemApClient::CosemApClient ()
{
  NS_LOG_FUNCTION_NOARGS ();
  m_wPort = 0;
  m_udpPort = 4056;
  m_nextTimeRequest = Seconds (0.0);
  m_typeRequesting = false;
  m_reqData = 0; 
  m_sizeReqData = 0;
  m_startRequestEvent = EventId ();
  m_nextRequestEvent = EventId (); 
  m_releaseAAEvent = EventId ();
  m_nSap = 0;
  m_totalNSap = 0; 
  m_enableNewRQ = 0;   

  // For debugging purposes
  // NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP created!");
}

CosemApClient::~CosemApClient ()
{
  NS_LOG_FUNCTION_NOARGS ();
}

void 
CosemApClient::Recv (Ptr<Packet> packet, int typeAcseService, int typeGet, Ptr<CosemApServer> cosemApServer)
{
  // COSEM ACSE services: COSEM-OPEN.cnf & COSEM-RELEASE.cnf 
  if (typeAcseService == OPEN)
    { 
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP ("<< m_wPort << ":" << Ipv4Address::ConvertFrom (m_localAddress) <<")" 
                                                   << " has established AA with SAP (" << cosemApServer->GetWport () << ":"
                                                   << Ipv4Address::ConvertFrom (cosemApServer->GetLocalAddress ()) << ")");
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAL (" << Ipv4Address::ConvertFrom (m_localAddress) << ") --> OPEN.cnf(Ok)");

      // "Receive" the xDLMS-Initiate.res (information not used at the moment)
      CosemAareHeader hdr;
      packet->RemoveHeader (hdr);

      // Save the AA successfully established: (wPort, Ptr<CosemApServer> cosemApServer)
      SaveActiveAa (cosemApServer);

      // Event: Invoke the COSEM-GET.req (NORMAL) service-Start the phase II: Communication Data
      Ptr<Packet> packet = NULL; // dummy packet
      Simulator::Schedule (Seconds (0.0), &CosemAlClient::CosemXdlmsGet, m_cosemAlClient, GET_NORMAL, REQUEST, cosemApServer, packet);
    }
  
  if (typeAcseService == RELEASE)
    {
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAL (" << Ipv4Address::ConvertFrom (m_localAddress) << ") --> RELEASE.cnf");

      // Extract the Release-Response-Reason 
      CosemRlreHeader hdr;
      packet->RemoveHeader (hdr);
      uint8_t reason = hdr.GetReason ();

      if (reason == 0)
        {
          // Remove the AA successfully established before with this remote SAP
          RemoveActiveAa (cosemApServer);

          NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP ("<< m_wPort << ":" << Ipv4Address::ConvertFrom (m_localAddress) <<")" 
                                                       << " has released the AA with SAP (" << cosemApServer->GetWport () << ":"
                                                       << Ipv4Address::ConvertFrom (cosemApServer->GetLocalAddress ()) << ")");       
        }
      else
        {
          NS_LOG_ERROR ("Release AA action rejected by SAP (" << cosemApServer->GetWport () << ":"
                                                       << Ipv4Address::ConvertFrom (cosemApServer->GetLocalAddress ()) << ")"); 
        }

      if (m_nSap == m_totalNSap)
        {
          NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP ("<< m_wPort << ":" << Ipv4Address::ConvertFrom (m_localAddress) <<")" 
                                                       << " has finished the releasing process of " << m_nSap << " SM(s)!");
          
          // Event: Change the state of CAL to IDLE
          EventId changeStateEvent = Simulator::Schedule (Seconds (0.0), &CosemAlClient::SetStateCf, m_cosemAlClient, CF_IDLE);
          m_cosemAlClient->SetChangeStateEvent (changeStateEvent);
        }
      else
        {
          // Event: Release AA established with the next remote SAP on the list
          m_releaseAAEvent = Simulator::Schedule (Seconds (0.0), &CosemApClient::RequestRelease, this);
        }
    }     

  // COSEM-GET.cnf (NORMAL, Data)
  if (typeGet == GET_NORMAL)
    { 
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAL (" << Ipv4Address::ConvertFrom (m_localAddress) << ") --> Get.cnf(NORMAL, Data)");

      // Extract the requested data
      CosemGetResponseNormalHeader hdr;
      packet->RemoveHeader (hdr);
      m_reqData = hdr.GetData ();
      m_sizeReqData = hdr.GetSerializedSize () - 4 ; // without CosemApp header (4B)

      // callback for DCApp
      if (!m_recvData.IsNull ())
        {
          m_recvData (m_sizeReqData);
        }
     
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP ("<< m_wPort << ":" << Ipv4Address::ConvertFrom (m_localAddress) <<")" 
                                                   << " has received " << m_sizeReqData << "B of data from the SAP (" 
                                                   << cosemApServer->GetWport () << ":"
                                                   << Ipv4Address::ConvertFrom (cosemApServer->GetLocalAddress ()) << ")");

    // Set a timer that permits to request new data to the SMs (SAPs)
    if (m_nSap == m_totalNSap)
      { 
        NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP ("<< m_wPort << ":" << Ipv4Address::ConvertFrom (m_localAddress) <<")" 
                                                     << " has finished the requesting process " << m_nSap << " SM(s)!");
       
        // Initialize "it" parameter at the first entry in the Map that contains the SAPs that successfully established an AA
        m_it = m_activeAa.begin (); 
        m_nSap = 0;
        m_enableNewRQ = 1;

        // Set a delay to request new data to the SMs 
        m_nextRequestEvent = Simulator::Schedule (GetNextTimeRequest (), &CosemApClient::NewRequest, this); 
        	
      }
    else
      {
        if (m_enableNewRQ == 1)
          {
            // Next SAP to request (new request)				
	    m_nextRequestEvent = Simulator::Schedule (Seconds (0.0), &CosemApClient::NewRequest, this); 
          }
        else
          {	
            // Next SAP to request (first request)
            m_startRequestEvent = Simulator::Schedule (Seconds (0.0), &CosemApClient::StartRequest, this);  
          }   
       }
    }
}

void 
CosemApClient::StartRequest ()
{
  NS_LOG_FUNCTION_NOARGS ();
  NS_ASSERT (m_startRequestEvent.IsExpired ());
  Simulator::Cancel (m_startRequestEvent);
 
  if (m_typeRequesting)
    {
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s Multicast Resquesting Mechanism");
      // do nothing
    }
  else
    {
      if (m_itSap == m_containerSap.Begin ())
        {
          NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s Sequential Resquesting Mechanism (polling by CAP ("<< m_wPort << ":" 
                                                       << Ipv4Address::ConvertFrom (m_localAddress) << "))");

          Ptr<Application> app = m_containerSap.Get (m_nSap);
          m_currentCosemApServer = app->GetObject<CosemApServer> ();  // Retrieve the first Saps pointer stored in AppContainer 
          m_nSap ++;   // Increase the value of "m_nSap" by one
          m_itSap ++;  // Increase the value of "it" by one
          /* 
           * Invoke the COSEM-OPEN.req service implemented in CosemClient_AL_CF	
           * in order to establish an AA with a remote server (sap)
           */
          Ptr<Packet> packet = NULL; // dummy packet
          m_cosemAlClient->CosemAcseOpen (REQUEST, m_currentCosemApServer, packet); 

          NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP ("<< m_wPort << ":" << Ipv4Address::ConvertFrom (m_localAddress) <<")" 
                                                       << " has requested an AA establishment to SAP (" 
                                                       << m_currentCosemApServer->GetWport () << ":" 
                                                       << Ipv4Address::ConvertFrom (m_currentCosemApServer->GetLocalAddress ()) << ")"); 
        }
      else 
        {
          if (m_itSap != m_containerSap.End())
            {
              Ptr<Application> app = m_containerSap.Get (m_nSap); 
              m_currentCosemApServer = app->GetObject<CosemApServer> ();  
              m_nSap ++;   
              m_itSap ++;  
              Ptr<Packet> packet = NULL; // dummy packet
              m_cosemAlClient->CosemAcseOpen (REQUEST, m_currentCosemApServer, packet); 
  
              NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP ("<< m_wPort << ":" << Ipv4Address::ConvertFrom (m_localAddress) <<")" 
                                                       << " has requested an AA establishment to SAP (" 
                                                       << m_currentCosemApServer->GetWport () << ":" 
                                                       << Ipv4Address::ConvertFrom (m_currentCosemApServer->GetLocalAddress ()) << ")");      
            }
          else
            {
              m_nSap = 0;  
            }
        }
    }
}

void
CosemApClient::NewRequest ()
{
  // Only when Data Concentratror Application is present at the node
  if (!m_recvData.IsNull ())
    {
      NS_LOG_FUNCTION_NOARGS ();
      Simulator::Cancel (m_nextRequestEvent);    
    }
  else
    {
      NS_LOG_FUNCTION_NOARGS ();
      NS_ASSERT (m_nextRequestEvent.IsExpired ()); 
      Simulator::Cancel (m_nextRequestEvent);
    }
  
  if (m_typeRequesting)
    {
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s Multicast Resquesting Mechanism");
      // do nothing
    }
  else
    {
      //  Request new data only to the SAP, which the CAP has established a successfully AA
      if (m_it != m_activeAa.end())
        {  
          // Event: Invoke the COSEM-GET.req (NORMAL) service-Start the phase II: Communication Data
          Ptr<Packet> packet = NULL; // dummy packet
          m_currentCosemApServer = m_it->second;
          Simulator::Schedule (Seconds (0.0), &CosemAlClient::CosemXdlmsGet, m_cosemAlClient, GET_NORMAL, REQUEST, m_currentCosemApServer, packet);
          m_it ++;
          m_nSap ++;

          NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP ("<< m_wPort << ":" << Ipv4Address::ConvertFrom (m_localAddress) <<")" 
                                                       << " has requested new data to the SAP (" 
                                                       << m_currentCosemApServer->GetWport () << ":" 
                                                       << Ipv4Address::ConvertFrom (m_currentCosemApServer->GetLocalAddress ()) << ")"); 
        }
      else 
        {
          m_nSap = 0; 
          m_enableNewRQ = 0; 
        }
    }
}

void 
CosemApClient::RequestRelease ()
{
  NS_LOG_FUNCTION_NOARGS ();
  NS_ASSERT (m_releaseAAEvent.IsExpired ());
  Simulator::Cancel (m_releaseAAEvent); 

 if (m_typeRequesting)
    {
      NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s Multicast Resquesting Mechanism");
      // do nothing
    }
  else
    {
      //  Release one by one the AAs established 
      if (m_it != m_activeAa.end())
        {  
          // Event: Invoke the COSEM-RELEASE.req (NORMAL) service-Start the phase III: Releasing AAs
          Ptr<Packet> packet = NULL; // dummy packet
          m_currentCosemApServer = m_it->second;
          Simulator::Schedule (Seconds (0.0), &CosemAlClient::CosemAcseRelease, m_cosemAlClient, REQUEST, m_currentCosemApServer, packet);
          m_it ++;
          m_nSap ++;

          NS_LOG_INFO (Simulator::Now ().GetSeconds () << "s CAP ("<< m_wPort << ":" << Ipv4Address::ConvertFrom (m_localAddress) <<")" 
                                                       << " has requested an AA release to the SAP (" 
                                                       << m_currentCosemApServer->GetWport () << ":" 
                                                       << Ipv4Address::ConvertFrom (m_currentCosemApServer->GetLocalAddress ()) << ")");  
        }
      else 
        { 
          m_nSap = 0; 
        }
    }
}

void 
CosemApClient::SaveActiveAa (Ptr<CosemApServer> cosemApServer)
{
  // uint16_t dstWport = cosemApServer->GetWport ();
  m_activeAa[m_nSap] = cosemApServer;      
}
	
void 
CosemApClient::RemoveActiveAa (Ptr<CosemApServer> cosemApServer)
{
  // Find the wPort of the current SAP
  std::map<uint16_t, Ptr<CosemApServer> >::iterator it = m_activeAa.find(m_nSap);

  if (m_activeAa.end () != it)
    {
       m_activeAa.erase (it);	// Exists the connection, so erase it
    } 
  else 
    {
      NS_LOG_INFO ("Error: Doesn't exist the AA requested to release!");
      return;			
    }
}

void 
CosemApClient::SetRecvCallback (Callback<void, uint32_t> recvData)
{
  m_recvData = recvData;
}

void 
CosemApClient::SetCosemAlClient (Ptr<CosemAlClient> cosemAlClient)
{
  m_cosemAlClient = cosemAlClient;
}

Ptr<CosemAlClient> 
CosemApClient::GetCosemAlClient ()
{
  return m_cosemAlClient;
}

void 
CosemApClient::SetWport (uint16_t wPort)
{
  m_wPort = wPort;
}

uint16_t 
CosemApClient::GetWport ()
{
  return m_wPort;
}

void 
CosemApClient::SetUdpport (uint16_t udpPort)
{
  m_udpPort = udpPort ;
}

uint16_t 
CosemApClient::GetUdpport ()
{
  return m_udpPort;
}

void 
CosemApClient::SetLocalAddress (Address ip)
{
  m_localAddress = ip;
}

Address
CosemApClient::GetLocalAddress ()
{
  return m_localAddress;
}

void 
CosemApClient::SetApplicationContainerSap (ApplicationContainer containerSap)
{
  m_containerSap = containerSap;
}

void 
CosemApClient::SetCurrentCosemApServer (Ptr<CosemApServer> currentCosemApServer)
{
  m_currentCosemApServer = currentCosemApServer;
}

Ptr<CosemApServer> 
CosemApClient::GetCurrentCosemApServer ()
{
  return m_currentCosemApServer;
}

void 
CosemApClient::SetTypeRequesting (bool typeRequesting)
{
  m_typeRequesting = typeRequesting;
}

bool
CosemApClient::GetTypeRequesting ()
{
  return m_typeRequesting;
}

void 
CosemApClient::SetNextTimeRequest (Time nextTimeRequest)
{
  m_nextTimeRequest = nextTimeRequest;
}

Time 
CosemApClient::GetNextTimeRequest ()
{
  return m_nextTimeRequest;
}

Ptr<Node>
CosemApClient::GetNode () const
{
  Ptr<Node> node = Application::GetNode ();
  return node;
}

uint32_t 
CosemApClient::GetSizeReqData ()
{
  return m_sizeReqData;
}

void
CosemApClient::DoDispose (void)
{
  NS_LOG_FUNCTION (this);
  Application::DoDispose ();
}

void 
CosemApClient::StartApplication (void)
{ 
  // Set the iterator at the begining of the container
  m_itSap = m_containerSap.Begin ();   
  // Retreive the total number of SAP that this Cap could resquet
  m_totalNSap = m_containerSap.GetN ();
  // Event: Create the StartRequest Event
  m_startRequestEvent = Simulator::Schedule (Seconds (0.0), &CosemApClient::StartRequest, this);
}

void 
CosemApClient::StopApplication (void)
{
   // Cancel Events
   Simulator::Cancel (m_nextRequestEvent);
   Simulator::Cancel (m_startRequestEvent); 
   // Initialize "it" parameter at the first entry in the Map that contains the SAPs that successfully established an AA
   m_it = m_activeAa.begin(); 	
   m_nSap = 0;
   // Event: Release AA established with remote SAPs
   m_releaseAAEvent = Simulator::Schedule (Seconds (0.0), &CosemApClient::RequestRelease, this);
}

} // namespace ns3

