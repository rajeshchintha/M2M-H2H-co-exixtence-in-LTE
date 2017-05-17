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

#include "ns3/cosem-al-client.h"
#include "ns3/application-container.h"
#include "ns3/ipv4-interface-container.h"
#include "ns3/object-factory.h"
#include "ns3/node-container.h"
#include "ns3/log.h"
#include "ns3/nstime.h"
#include "ns3/uinteger.h"
#include "ns3/string.h"
#include "udp-cosem-client-helper.h"
#include "ns3/cosem-ap-client.h"
#include "ns3/udp-cosem-client.h"

NS_LOG_COMPONENT_DEFINE ("UdpCosemClientHelper");

namespace ns3 {

UdpCosemClientHelper::UdpCosemClientHelper ()
{
}

UdpCosemClientHelper::UdpCosemClientHelper (ApplicationContainer CosemApServerContainer, Ipv4InterfaceContainer interface, Time interval)
{
  m_cosemApServerContainer = CosemApServerContainer;
  m_interface = interface;
  m_interval = interval;
  m_index = 0;
}

void
UdpCosemClientHelper::SetAttribute (std::string name, const AttributeValue &value)
{
  m_factory.Set (name, value);
}

ApplicationContainer
UdpCosemClientHelper::Install (NodeContainer c)
{
  ApplicationContainer apps;
  for (NodeContainer::Iterator i = c.Begin (); i != c.End (); ++i)
    {
      // Retreive the pointer of the i-node storaged in the NodeContainer
      Ptr<Node> node = *i;  
      // Add the CosemClientStack to the Node (i.e. UdpCosemWrapperClient & CosemAlClient)
      AddCosemClientStack (node);

      // Create a CosemApClientObject
      //Ptr<CosemApClient> cosemApClient =  m_factory.Create<CosemApClient> ();
      Ptr<CosemApClient> cosemApClient = CreateObject<CosemApClient> ();
      // Retrieve the pointer of the CosemAlClient that has previously aggregated to the node
      Ptr<CosemAlClient> cosemAlClient = node->GetObject<CosemAlClient> ();
      // Retrieve the pointer of the UdpCosemWrapperClient that has previously aggregated to the node
      Ptr<UdpCosemWrapperClient> udpCosemWrapperClient = node->GetObject<UdpCosemWrapperClient> ();
      // Add the CosemApClient created to the Node
      node->AddApplication (cosemApClient);
      // Set the pointer to the CosemAlClient object attached at the node
      cosemApClient->SetCosemAlClient (cosemAlClient);
      // Set the wPort
      udpCosemWrapperClient->SetwPortClient (cosemApClient);
      // Set the Udp Port listening by the CAL
      cosemApClient->SetUdpport (4056);
      // Set the Ip address assigned to the node 
      cosemApClient->SetLocalAddress (m_interface.GetAddress (m_index)); 
      // Retreive the pointer of ApplicationContainer (CosemApServers)
      cosemApClient->SetApplicationContainerSap (m_cosemApServerContainer);
      // Add the CosemApClient created to the ApplicationContainer
      apps.Add (cosemApClient);

      // Set interval for requesting data to remote SAPs
      cosemApClient->SetNextTimeRequest (m_interval);
      // Connect CosemAlClient and cosemApClient to each other
      cosemAlClient->SetCosemApClient (cosemApClient);
      cosemApClient->SetCosemAlClient (cosemAlClient);
     
      m_index++;  
    }
  return apps;
}

void 
UdpCosemClientHelper::AddCosemClientStack (Ptr<Node> node)
{
  // Create a UdpCosemWrapperClient
  Ptr<UdpCosemWrapperClient> udpCosemWrapperClient = CreateObject<UdpCosemWrapperClient> ();
  // Create a CosemAlClient Object 
  Ptr<CosemAlClient> cosemAlClient = CreateObject<CosemAlClient> ();
  // Connect UdpCosemWrapperClient and CosemAlClient to each other
  udpCosemWrapperClient->SetCosemAlClient (cosemAlClient);      
  cosemAlClient->SetCosemWrapperClient (udpCosemWrapperClient);
  // Aggregate the UdpCosemWrapperClient to the node and set Ip Address and Udp Port number 
  node->AggregateObject (udpCosemWrapperClient);
  udpCosemWrapperClient->SetUdpport (4056);
  udpCosemWrapperClient->SetLocalAddress (m_interface.GetAddress(m_index)); 
  // Aggregate the CosemAlClient to the node and set its state to CF_IDLE and Udp Port number 
  node->AggregateObject (cosemAlClient);
  cosemAlClient->SetStateCf (1);
  cosemAlClient->SetUdpport (4056);
}

} // namespace ns3
