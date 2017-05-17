#ifndef COTP_CLIENT_H
#define COTP_CLIENT_H

#include "ns3/object.h"
#include "ns3/address.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"

namespace ns3 {

class Socket;
class Packet;
class MmsServer;
class MmsAdaptClient;
class MmsClient;

/** 
 * COTP Layer - Client
 *
 */

class CotpClient : public Object
{
public:
  static TypeId GetTypeId (void);
  
  CotpClient ();
 
  virtual ~CotpClient ();

  // Receive ("read") the data from the socket
  void Recv (Ptr<Socket> socket);
	
  // Called when new packet is ready to be send
  void Send (Ptr<Socket> socket, Ptr<Packet> packet, int type, Address from);

  // Call COTP adapt services
  void CotpAdaptServices (Ptr<Socket> socket, int cotptype, int type_service, Ptr<Packet> packet, Address from);

  // Set & GET the pointer to a MmsAdaptClient object
  void SetMmsAdaptClient (Ptr<MmsAdaptClient> mmsAdaptClient);
  Ptr<MmsAdaptClient> GetMmsAdaptClient ();

  // Set & GET the pointer to a Socket object
  void SetSocket (Ptr<Socket> socket);
  Ptr<Socket> GetSocket ();

  // Assign a SRC reference number to the client created and attached to the adapt client
  void SetSrcRefClient (Ptr<MmsClient> mmsClient);

  // Return the Src reference number of the server
  uint16_t GetSrcRefClient ();

  // Return the wPort number of the remote server
  uint16_t GetSrcRefServer ();

  // Set & GET the TCP Port listening by the client
  void SetTcpport (uint16_t tcpPort);
  uint16_t GetTcpport ();

  // Set & GET the local Ip address (Client)
  void SetLocalAddress (Address ip);
  Address GetLocalAddress ();

  // Set the remote ip address (Server)
  void SetRemoteAddress (Address ip); 
  Address GetRemoteAddress ();

  // Type of services
  enum CotpType
  {
    CRE = 0xe0,
    CC = 0xd0,
    DT = 0xf0
  };

  enum spdutype { CN = 0x0d, AC = 0x0e, DTD = 0x01};

  enum typeservice { REQUEST, INDICATION };
  
private:

  Ptr<Socket> m_socket;

  Ptr<MmsAdaptClient> m_mmsAdaptClient;

  uint16_t m_srcRefClient;  // Reference Number assigned to the Client (Counter)
  uint16_t m_srcRefServer;  // Reference Number assigned to the remote Server currently being accessed
  uint16_t m_tcpPort;  // Tcp port
  Address m_localAddress;  // Local Ip address 
  Address m_remoteAddress;  // Ip Address of the remote SAP

  // Helpers parameters
  EventId m_adaptCotpserviceEvent;

};

} // namespace ns3

#endif /* COTP_CLIENT_H */
