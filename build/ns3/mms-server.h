#ifndef MMS_SERVER_H
#define MMS_SERVER_H

#include "ns3/application.h"
#include "ns3/address.h"
#include "ns3/application-container.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"
#include "ns3/socket.h"

namespace ns3 {

class Node;
class Socket;
class Packet;
class MmsAdaptServer;
class MmsClient;

/** 
 * COSEM Application Process (AP) - Server
 *
 */

class MmsServer : public Application
{
public:
  static TypeId GetTypeId (void);
  
  MmsServer ();
 
  virtual ~MmsServer ();

  void SendReport(Time interval);

  void Trigger(Ptr<MmsClient> from);

  // Called when new message received (for COSEM-GET services)
  void Recv (Ptr<Socket> socket, int typeSpdu, int typeService, Ptr<Packet> packet);

  // Retrieve the node where the CAP is attached
  Ptr<Node> GetNode () const;

  //Set & GET the pointer to a CosemServer_AL_CF object
  void SetMmsAdaptServer (Ptr<MmsAdaptServer> cosemAlServer);
  Ptr<MmsAdaptServer> GetMmsAdaptServer ();

  // Set & GET the srcRef
  void SetSrcRef (uint16_t srcRef);
  uint16_t GetSrcRef ();

  // Set & GET the Udp Port listening by the CAL
  void SetTcpport (uint16_t tcpPort);
  uint16_t GetTcpport ();

  // Set & GET the local Ip address
  void SetLocalAddress (Address ip);
  Address GetLocalAddress ();
  
  // Type of services
  enum typeService { REQUEST, INDICATION, RESPONSE, CONFIRM };

  enum mmsService { INITIATE, CONFIRMED, CONCLUDE };

  enum spdutype { CN = 0x0d, AC = 0x0e, DTD = 0x01};

protected:

  virtual void DoDispose (void);
 
private:

  Ptr<MmsAdaptServer> m_mmsAdaptServer;
  Ptr<Socket> m_clientSocket;
  uint16_t m_srcRef;  // SAP Wrapper Port Number (unique id)
  uint16_t m_tcpPort;  // Tcp port
  Address m_localAddress;  // Local Ip address
  uint64_t m_reqData [3]; // Data requested by the remote CAP

  virtual void StartApplication (void);
  virtual void StopApplication (void);

  // Helpers parameters
  //EventId m_invokeCosemService; 
};

} // namespace ns3

#endif /* MMS_SERVER_H */
