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
#include "ns3/object-factory.h"
#include "ns3/node-container.h"
#include "ns3/log.h"
#include "ns3/uinteger.h"
#include "ns3/string.h"
#include "ns3/data-concentrator-helper.h"
#include "ns3/cosem-ap-client.h"
#include "ns3/dc-app.h"

NS_LOG_COMPONENT_DEFINE ("DataConcentratorApplicationHelper");

namespace ns3 {

DataConcentratorApplicationHelper::DataConcentratorApplicationHelper ()
{
}

DataConcentratorApplicationHelper::DataConcentratorApplicationHelper (ApplicationContainer clientApps, Address centerIp, Address dcIpAddress)
{
  m_centerIpAddress = centerIp;
  m_dcIpAddress = dcIpAddress;
  m_clientApps = clientApps;
}

void
DataConcentratorApplicationHelper::SetAttribute (std::string name, const AttributeValue &value)
{
  m_factory.Set (name, value);
}

ApplicationContainer
DataConcentratorApplicationHelper::Install (NodeContainer c)
{
  ApplicationContainer apps;
  
  for (NodeContainer::Iterator i = c.Begin (); i != c.End (); ++i)
    {
      // Retreive the pointer of the i-node storaged in the NodeContainer
      Ptr<Node> node = *i;  
      // Create DcMemory Object
      Ptr<DcMemory> dcMemory = CreateObject<DcMemory> ();
      // Aggregate the DcMemory to the node
      node->AggregateObject (dcMemory);
      // Create a DataConcentratorApplication Object
      Ptr<DataConcentratorApplication> dataConcentratorApplication = CreateObject<DataConcentratorApplication> ();
      // Set the SmatGridCenter Address & local Adress
      dataConcentratorApplication->SetSgCenterAddress (m_centerIpAddress);
      dataConcentratorApplication->SetLocalAddress (m_dcIpAddress);
      // Set the pointer to the CosemApClient object attached at the node
      Ptr<Application> app = m_clientApps.Get (0); // assuming only one clientApp per node
      Ptr<CosemApClient> cosemApClient = app->GetObject<CosemApClient> ();  
      dataConcentratorApplication->SetCosemApClient (cosemApClient);
      // Set the pointer to the DcMemory object attached at the node
      dataConcentratorApplication->SetDcMemory (dcMemory);
      // Add the DataConcentratorApplication created to the Node
      node->AddApplication (dataConcentratorApplication);
      // Add the DataConcentratorApplication created to the ApplicationContainer
      apps.Add (dataConcentratorApplication);   
    }
  return apps;
}

} // namespace ns3
