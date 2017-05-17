#ifndef MMS_ADAPT_SERVER_H
#define MMS_ADAPT_SERVER_H

#include "ns3/object.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"

namespace ns3 {

class Packet;
class Socket;
class MmsServer;
class CotpServer;

/** 
 * MMS Adapt Layer - Server
 *
 */

class MmsAdaptServer : public Object
{
public:
  static TypeId GetTypeId (void);
  
  MmsAdaptServer ();
 
  virtual ~MmsAdaptServer ();

  // Received the indication/confirmation of a UDP-DATA request
  void RecvSpdu (Ptr<Socket> socket, Ptr<Packet> packet);

  void MmsAdapt(Ptr<Socket> socket, int mmsService, int typeService, Ptr<Packet> packet);
  // Send the APDU to the sub-layer Wrapper
  void sendApdu (Ptr<Socket> socket, Ptr<Packet> packet);

  void ConfirmedResponse(Ptr<Socket> socket, int typeConfirmed, Ptr<Packet> packet);

  void UnconfirmedService (Ptr<Socket> socket, Ptr<Packet> packet);

  // Set & GET the pointer to a MmsAdaptServer object
  void SetMmsServer (Ptr<MmsServer> mmsServer);
  Ptr<MmsServer> GetMmsServer ();

  // Set & GET the pointer to a CotpServer object
  void SetCotpServer (Ptr<CotpServer> cotpServer);
  Ptr<CotpServer> GetCotpServer ();

  // Set & Get the state of CF
  void SetStateCf (int state);
  int GetStateCf ();
	
  // Set & Get the type of service
  void SetTypeService (int typeService);
  int GetTypeService ();

  void SetTypeGet (int typeGet); 
  int GetTypeGet ();

  // Set & GET the Tcp Port of the client
  void SetTcpport (uint16_t tcpPort);
  uint16_t GetTcpport ();

  // Initial Process at the node (physical device)
  void Init ();
 
  // States machine of the Control Function
  enum stateCf { CF_INACTIVE, CF_IDLE, CF_ASSOCIATION_PENDING, CF_ASSOCIATED, CF_ASSOCIATION_RELEASE_PENDING };

  // Type of services
  enum typeService { REQUEST, INDICATION, RESPONSE, CONFIRM };

  enum mmsService { INITIATE, CONFIRMED, CONCLUDE };

  enum spdutype { CN = 0x0d, AC = 0x0e, DTD = 0x01 };

protected:

  // A pointer to a MmsServer object (cross-reference)
  Ptr<MmsServer> m_mmsServer;

  // A pointer to a CotpServer object
  Ptr<CotpServer> m_cotpServer;
  
  int m_typeService;  // Type of service
  int m_typeGet;
  int m_stateCf;  // State of CF
  uint16_t m_tcpPort;  // Tcp port
 
  // Helpers parameters
  EventId m_changeStateEvent;
  EventId m_invokeMmsAdaptService;
  EventId m_sendApduEvent;


};

} // namespace ns3

#endif /* MMS_ADAPT_SERVER_H */
