#ifndef MMS_SERVER_HELPER_H
#define MMS_SERVER_HELPER_H

/**
 * NOte: Code based on UdpClientServerHelper, 2008 INRIA
 * Author: Mohamed Amine Ismail <amine.ismail@sophia.inria.fr>
 */

namespace ns3 {

class Ipv4InterfaceContainer;
class ObjectFactory;
class NodeContainer;
class Node;

class MmsServerHelper
{
public:

  MmsServerHelper ();

  MmsServerHelper (Ipv4InterfaceContainer interface);

  // Record an attribute to be set in each Application after it is is created
  void SetAttribute (std::string name, const AttributeValue &value);

  /** 
   * Create one MMS server application on each of the input nodes and
   * returns the applications created, one application per input node  
   */
  ApplicationContainer Install (NodeContainer c);
  
  // Create and Aggregate the MmsServer Stack to the node ("physical device")
  void AddMmsServerStack (Ptr<Node> node);

private:
      
  ApplicationContainer m_mmsServerContainer;
  Ipv4InterfaceContainer m_interface;
  ObjectFactory m_factory;
  uint32_t m_index;
};

} // namespace ns3

#endif /* MMS_SERVER_HELPER_H*/
