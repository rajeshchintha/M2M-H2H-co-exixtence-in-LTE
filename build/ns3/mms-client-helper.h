#ifndef MMS_CLIENT_HELPER_H
#define MMS_CLIENT_HELPER_H

namespace ns3 {

class ApplicationContainer;
class Ipv4InterfaceContainer;
class ObjectFactory;
class NodeContainer;
class Node;
class Time;

class MmsClientHelper
{
public:

  MmsClientHelper ();

  MmsClientHelper (ApplicationContainer MmsServerContainer, Ipv4InterfaceContainer interface, Time interval, int type, int mode, uint8_t nSm);

  // Record an attribute to be set in each Application after it is is created
  void SetAttribute (std::string name, const AttributeValue &value);

  /** 
   * Create one udp cosem client application on each of the input nodes and 
   * returns the applications created, one application per input node  
   */
  ApplicationContainer Install (NodeContainer c);
  
  // Create and Aggregate the CosemClient Stack to the node ("physical device")
  void AddMmsClientStack (Ptr<Node> node);

private:
      
  ApplicationContainer m_mmsServerContainer;
  Ipv4InterfaceContainer m_interface;
  Time m_interval;
  ObjectFactory m_factory;
  uint32_t m_index;
  int m_type;
  int m_mode;
  uint8_t m_nSm;
};

} // namespace ns3

#endif /* MMS_CLIENT_HELPER_H */
