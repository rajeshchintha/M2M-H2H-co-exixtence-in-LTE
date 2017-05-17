#ifndef COTP_SERVER_H
#define COTP_SERVER_H

#include "ns3/object.h"
#include "ns3/event-id.h"
#include "ns3/address.h"
#include "ns3/ptr.h"

namespace ns3 {

class Socket;
class Packet;
class MmsAdaptServer;
class MmsServer;

/** 
 * COTP layer - Server
 *
 */

class CotpServer : public Object
{
public:
  static TypeId GetTypeId (void);
  
  CotpServer ();
 
  virtual ~CotpServer ();

  // First Process
  void Init ();

  // Receive ("read") the data from the socket and pass to the MMS Adaptation Layer
  void Recv (Ptr<Socket> socket);
	
  // Called when new packet is ready to be send (assuming only one Server attached to the physical device)
  void Send (Ptr<Socket> socket, Ptr<Packet> packet);

  void HandleAccept (Ptr<Socket> s, const Address& from);

  // Call UDP services
  void CotpAdaptServices (Ptr<Socket> socket, int cotptype, int type_service, Ptr<Packet> packet);

  // Set & GET the pointer to a MmsAdaptServer object
  void SetMmsAdaptServer (Ptr<MmsAdaptServer> mmsAdaptServer);
  Ptr<MmsAdaptServer> GetMmsAdaptServer ();

  // Set & GET the pointer to a Socket object
  void SetSocket (Ptr<Socket> socket);
  Ptr<Socket> GetSocket ();

  void SetSrcRefServer (Ptr<MmsServer> mmsServer);

  uint16_t GetSrcRefServer ();

  // Return the SrcRef number of the remote client
  uint16_t GetSrcRefClient ();

  // Set & GET the Tcp Port listening by the server
  void SetTcpport (uint16_t tcpPort);
  uint16_t GetTcpport ();

  // Set & GET the local Ip address
  void SetLocalAddress (Address ip);
  Address GetLocalAddress ();

  // Set & GET the remote Ip address
  void SetRemoteAddress (Address ip);
  Address GetRemoteAddress ();

  // Type of services
  enum CotpType
  {
    CRE = 0xe0,
    CC = 0xd0,
    DT = 0xf0
  };

  enum spdutype { CN, AC , DTD };

  enum typeservice { REQUEST, INDICATION };
  
private:

  Ptr<Socket> m_socket;

  Ptr<MmsAdaptServer> m_mmsAdaptServer;

  uint16_t m_SrcRefServer;
  uint16_t m_SrcRefClient;
  uint16_t m_tcpPort;
  Address m_localAddress;
  Address m_remoteAddress;

  // Helpers parameters
  EventId m_adaptCotpserviceEvent;

};

} // namespace ns3

#endif /* COTP_SERVER_H */
