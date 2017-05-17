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
#include "ns3/mdm-application-helper.h"
#include "ns3/mdm-app.h"

NS_LOG_COMPONENT_DEFINE ("MeterDataManagementApplicationHelper");

namespace ns3 {

MeterDataManagementApplicationHelper::MeterDataManagementApplicationHelper ()
{
}

MeterDataManagementApplicationHelper::MeterDataManagementApplicationHelper (ApplicationContainer dataConcentratorAppContainer, Address mdmIp, Time interval, uint32_t readingTime)
{
  m_dataConcentratorApplicationContainer = dataConcentratorAppContainer;
  m_mdmAddress = Address (mdmIp);
  m_interval = interval;
  m_readingTime = readingTime;
}

void
MeterDataManagementApplicationHelper::SetAttribute (std::string name, const AttributeValue &value)
{
  m_factory.Set (name, value);
}

ApplicationContainer
MeterDataManagementApplicationHelper::Install (NodeContainer c)
{
  ApplicationContainer apps;
  
  for (NodeContainer::Iterator i = c.Begin (); i != c.End (); ++i)
    {
      // Retreive the pointer of the i-node storaged in the NodeContainer
      Ptr<Node> node = *i;  
     
      // Create DataBaseServer Object
      Ptr<DataBaseServer> dataBaseServer = CreateObject<DataBaseServer> ();
      // Aggregate the DataBaseServer to the node
      node->AggregateObject (dataBaseServer);

      // Create a MeterDataManagementApplication Object
      Ptr<MeterDataManagementApplication> meterDataManagementApplication = CreateObject<MeterDataManagementApplication> ();
      // Set the SmatGrid Control Center Address
      meterDataManagementApplication->SetLocalAddress (m_mdmAddress);
      // Retreive the pointer of ApplicationContainer
      meterDataManagementApplication->SetApplicationContainerDcApp (m_dataConcentratorApplicationContainer);
      // Set interval for requesting data to remote Data Doncentrator and the Reading time to poll the meter data 
      meterDataManagementApplication->SetNextTimeRequest (m_interval);
      meterDataManagementApplication->SetReadingTime (m_readingTime);

      // Set the pointer to the DcMemory object attached at the node
      meterDataManagementApplication->SetDataBaseServer (dataBaseServer);

      // Add the MeterDataManagementApplication created to the Node
      node->AddApplication (meterDataManagementApplication);
      // Add the MeterDataManagementApplication created to the ApplicationContainer
      apps.Add (meterDataManagementApplication);   
    }
  return apps;
}

} // namespace ns3
