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

#ifndef METER_DATA_MANAGEMENT_APPLICATION_HELPER_H
#define METER_DATA_MANAGEMENT_APPLICATION_HELPER_H

#include "ns3/ipv4-address.h"
#include "ns3/address.h"

namespace ns3 {

class ApplicationContainer;
class ObjectFactory;
class NodeContainer;
class Node;
class Time;

class MeterDataManagementApplicationHelper
{
public:

  MeterDataManagementApplicationHelper ();

  MeterDataManagementApplicationHelper (ApplicationContainer dataConcentratorAppContainer, Address mdmIp, Time interval, uint32_t readingTime);


  // Record an attribute to be set in each Application after it is is created
  void SetAttribute (std::string name, const AttributeValue &value);

  /** 
   * Create one MeterDataManagementApplication application on each of the input nodes and 
   * returns the applications created, one application per input node  
   */
  ApplicationContainer Install (NodeContainer c);
  
private:
  ApplicationContainer m_dataConcentratorApplicationContainer;
  Address m_mdmAddress;
  Time m_interval;
  uint32_t m_readingTime;
  ObjectFactory m_factory;
};

} // namespace ns3

#endif /* DMETER_DATA_MANAGEMENT_APPLICATION_HELPER_H*/
