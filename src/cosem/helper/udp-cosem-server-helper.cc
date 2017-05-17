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

#include "ns3/application-container.h"
#include "ns3/ipv4-interface-container.h"
#include "ns3/object-factory.h"
#include "ns3/node-container.h"
#include "ns3/log.h"
#include "ns3/uinteger.h"
#include "ns3/string.h"
#include "ns3/cosem-al-server.h"
#include "udp-cosem-server-helper.h"
#include "ns3/cosem-ap-server.h"
#include "ns3/udp-cosem-server.h"

NS_LOG_COMPONENT_DEFINE ("UdpCosemServerHelper");

namespace ns3 {

UdpCosemServerHelper::UdpCosemServerHelper ()
{
}

UdpCosemServerHelper::UdpCosemServerHelper (Ipv4InterfaceContainer interface)
{
  m_interface = interface;
  m_index = 0;
}

void
UdpCosemServerHelper::SetAttribute (std::string name, const AttributeValue &value)
{
  m_factory.Set (name, value);
}

ApplicationContainer
UdpCosemServerHelper::Install (NodeContainer c)
{
  ApplicationContainer apps;
  
  for (NodeContainer::Iterator i = c.Begin (); i != c.End (); ++i)
    {
      // Retreive the pointer of the i-node storaged in the NodeContainer
      Ptr<Node> node = *i;  
      // Add the CosemServerStack to the Node (i.e. UdpCosemWrapperServer & CosemAlServer)
      AddCosemServerStack (node);

      // Create a CosemApServerObject
      Ptr<CosemApServer> cosemApServer = CreateObject<CosemApServer> ();
      // Retrieve the pointer of the CosemAlServer that has previously aggregated to the node
      Ptr<CosemAlServer> cosemAlServer = node->GetObject<CosemAlServer> ();
      // Retrieve the pointer of the UdpCosemWrapperServer that has previously aggregated to the node
      Ptr<UdpCosemWrapperServer> udpCosemWrapperServer = node->GetObject<UdpCosemWrapperServer> ();
      // Add the CosemApServer created to the Node
      node->AddApplication (cosemApServer);
      // Set the pointer to the CosemAlServer object attached at the node
      cosemApServer->SetCosemAlServer (cosemAlServer);
      // Set the wPort
      udpCosemWrapperServer->SetwPortServer (cosemApServer);
      // Set the Udp Port listening by the CAL
      cosemApServer->SetUdpport (4056);
      // Set the Ip address assigned to the node 
      cosemApServer->SetLocalAddress (m_interface.GetAddress (m_index)); 
      // Add the CosemApServer created to the ApplicationContainer
      apps.Add (cosemApServer);   
      // Connect CosemAlServer and cosemApServer to each other
      cosemAlServer->SetCosemApServer (cosemApServer);
      cosemApServer->SetCosemAlServer (cosemAlServer);
     
      m_index++;  
    }
  return apps;
}

void 
UdpCosemServerHelper::AddCosemServerStack (Ptr<Node> node)
{
  // Create a UdpCosemWrapperServer
  Ptr<UdpCosemWrapperServer> udpCosemWrapperServer = CreateObject<UdpCosemWrapperServer> ();
  // Create a CosemAlServer Object 
  Ptr<CosemAlServer> cosemAlServer = CreateObject<CosemAlServer> ();
  // Connect UdpCosemWrapperServer and CosemAlServer to each other
  udpCosemWrapperServer->SetCosemAlServer (cosemAlServer);      
  cosemAlServer->SetCosemWrapperServer (udpCosemWrapperServer);
  // Aggregate the UdpCosemWrapperServer to the node and set Ip Address and Udp Port number 
  node->AggregateObject (udpCosemWrapperServer);
  udpCosemWrapperServer->SetUdpport (4056);
  udpCosemWrapperServer->SetLocalAddress (m_interface.GetAddress(m_index)); 
  // Aggregate the CosemAlServer to the node and set its state to CF_IDLE and Udp Port number 
  node->AggregateObject (cosemAlServer);
  cosemAlServer->SetStateCf (1);
  cosemAlServer->SetUdpport (4056);
}

} // namespace ns3
