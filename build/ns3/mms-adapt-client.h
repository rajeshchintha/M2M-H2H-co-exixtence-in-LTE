#ifndef MMS_ADAPT_CLIENT_H
#define MMS_ADAPT_CLIENT_H

#include <iostream>
#include <fstream>
#include <map> 
#include "ns3/object.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"

using namespace std;

namespace ns3 {

class Packet;
class Address;
class Socket;
class MmsServer;
class MmsClient;
class CotpClient;

/** 
 * MMS Adapt Client
 *
 */

class MmsAdaptClient : public Object
{
public:
  static TypeId GetTypeId (void);
  
  MmsAdaptClient ();
 
  virtual ~MmsAdaptClient();

  void MmsAdapt(Ptr<Socket> socket, int mmsService, int typeService, Address from, Ptr<Packet> packet);

  void ConfirmedRequest (Ptr<Socket> socket, int mmsService, Address from, Ptr<Packet> packet, uint8_t m_nReq);

  void RecvSpdu (Ptr<Socket> socket, Ptr<Packet> packet, Address from);

  void sendApdu (Ptr<Socket> socket, Ptr<Packet> packet,  Address from);

  void sendConfirmedRequestApdu (Ptr<Socket> socket, Ptr<Packet> packet,  Address from);

  void SetMmsClient (Ptr<MmsClient> mmsClient);

  Ptr<MmsClient> GetMmsClient ();
 
  void SetCotpClient (Ptr<CotpClient> cotpClient);

  Ptr<CotpClient> GetCotpClient ();

  void SetStateCf (int state);
  int GetStateCf ();
	
  void SetNode (int node);

  void SetTypeService (int typeService);
  int GetTypeService ();

  void SetTypeGet (int typeGet); 
  int GetTypeGet ();

  void SetTcpport (uint16_t tcpPort);
  uint16_t GetTcpport ();

  void SetChangeStateEvent (EventId changeStateEvent);

  enum stateCf { CF_INACTIVE, CF_IDLE, CF_ASSOCIATION_PENDING, CF_ASSOCIATED, CF_ASSOCIATION_RELEASE_PENDING };

  enum typeService { REQUEST, INDICATION, RESPONSE, CONFIRM };

  enum spdutype { CN = 0x0d , AC = 0x0e , DT };

  enum mmsService { INITIATE, CONFIRMED, CONCLUDE };

  enum mmsServiceType { READ = 0x04, WRITE = 0x05 };

protected:

  Ptr<MmsClient> m_mmsClient;

  Ptr<CotpClient> m_cotpClient;

  int m_typeService;  // Type of service
  int m_typeGet;
  int m_stateCf;  // State of CF
  uint16_t m_tcpPort;  // Tcp port
  int m_node;
  // Helpers parameters
  EventId m_changeStateEvent;
  EventId m_sendApduEvent;
  EventId m_invokeMmsAdaptService;
  ostringstream ClientFile1;
};

} // namespace ns3

#endif /* MMS_ADAPT_CLIENT_H */
