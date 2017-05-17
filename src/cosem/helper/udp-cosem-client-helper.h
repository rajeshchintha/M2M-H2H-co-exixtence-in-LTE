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
#ifndef UDP_COSEM_CLIENT_HELPER_H
#define UDP_COSEM_CLIENT_HELPER_H

/**
 * NOte: Code based on UdpClientServerHelper, 2008 INRIA
 * Author: Mohamed Amine Ismail <amine.ismail@sophia.inria.fr>
 */

namespace ns3 {

class ApplicationContainer;
class Ipv4InterfaceContainer;
class ObjectFactory;
class NodeContainer;
class Node;
class Time;

class UdpCosemClientHelper
{
public:

  UdpCosemClientHelper ();

  UdpCosemClientHelper (ApplicationContainer CosemApServerContainer, Ipv4InterfaceContainer interface, Time interval);

  // Record an attribute to be set in each Application after it is is created
  void SetAttribute (std::string name, const AttributeValue &value);

  /** 
   * Create one udp cosem client application on each of the input nodes and 
   * returns the applications created, one application per input node  
   */
  ApplicationContainer Install (NodeContainer c);
  
  // Create and Aggregate the CosemClient Stack to the node ("physical device")
  void AddCosemClientStack (Ptr<Node> node);

private:
      
  ApplicationContainer m_cosemApServerContainer;
  Ipv4InterfaceContainer m_interface;
  Time m_interval;
  ObjectFactory m_factory;
  uint32_t m_index;
};

} // namespace ns3

#endif /* UDP_COSEM_CLIENT_HELPER_H */
