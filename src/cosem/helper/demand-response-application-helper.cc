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
#include "ns3/demand-response-application-helper.h"
#include "ns3/dr-app.h"
#include "ns3/mdm-app.h"

NS_LOG_COMPONENT_DEFINE ("DemandResponseApplicationHelper");

namespace ns3 {

DemandResponseApplicationHelper::DemandResponseApplicationHelper ()
{
}

DemandResponseApplicationHelper::DemandResponseApplicationHelper (Address drSystemIp, ApplicationContainer mdmAppContainer)
{
  m_drSystemIpAddress = Address (drSystemIp);
  m_mdmAppContainer =  mdmAppContainer;
}

void
DemandResponseApplicationHelper::SetAttribute (std::string name, const AttributeValue &value)
{
  m_factory.Set (name, value);
}

ApplicationContainer
DemandResponseApplicationHelper::Install (NodeContainer c)
{
  ApplicationContainer apps;
  
  for (NodeContainer::Iterator i = c.Begin (); i != c.End (); ++i)
    {
      // Retreive the pointer of the i-node storaged in the NodeContainer
      Ptr<Node> node = *i;  
     
      // Create a DemandResponseApplication Object
      Ptr<DemandResponseApplication> demandResponseApplication = CreateObject<DemandResponseApplication> ();
      // Set the SmatGrid Control Center Address
      demandResponseApplication->SetLocalAddress (m_drSystemIpAddress);
      // Set the pointer to the Meter Data Management object attached at the node
      Ptr<Application> app = m_mdmAppContainer.Get (0); // assuming only one Meter Data Management Application per node
      Ptr<MeterDataManagementApplication> mdmApp = app->GetObject<MeterDataManagementApplication> ();  
      demandResponseApplication->SetMdmApp (mdmApp);
      // Add the DemandResponseApplication created to the Node
      node->AddApplication (demandResponseApplication);
      // Add the DemandResponseApplication created to the ApplicationContainer
      apps.Add (demandResponseApplication);   
    }
  return apps;
}

} // namespace ns3
