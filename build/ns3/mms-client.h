#ifndef MMS_CLIENT_H
#define MMS_CLIENT_H

#include <map>
#include <iostream>
#include <fstream>
#include "ns3/callback.h"
#include "ns3/ptr.h"
#include "ns3/application.h"
#include "ns3/address.h"
#include "ns3/application-container.h"
#include "ns3/event-id.h"
#include <stdint.h>
#include "ns3/random-variable-stream.h"

using namespace std;

namespace ns3 {

class Node;
class Packet;
class Time;
class MmsAdaptClient;
class MmsServer;

/** 
 * COSEM Application Process (AP) - Client
 *
 */

class MmsClient : public Application
{
public:
  static TypeId GetTypeId (void);
  
  MmsClient ();
 
  virtual ~MmsClient ();

  // Called when new packet received
  void Recv (Ptr<Socket> socket, int typeSpdu, Ptr<Packet> packet, Ptr<MmsServer> mmsServer, Address from);

  // Start the request of data to the SAP by the CAP
  void StartRequest ();

  // New request of data to the SAP by the CAP
  void NewRequest (Ptr<Socket> socket, Ptr<MmsServer> mmsServer);

  //Request the Release of AAs
  void RequestRelease ();

  // Store the AAs succesfully established
  void SaveActiveAa (Ptr<MmsServer> mmsServer, Ptr<Socket> socket);
	
  // Remove the AAs succesfully established before
  void RemoveActiveAa (Ptr<MmsServer> mmsServer);

  // Notify Data Concentrator Application when new data is available to be read. 
  void SetRecvCallback (Callback<void, uint32_t> recvData);

  // Set & GET the pointer to a MmsAdaptClient object
  void SetMmsAdaptClient (Ptr<MmsAdaptClient> mmsAdaptClient);
  Ptr<MmsAdaptClient> GetMmsAdaptClient ();

  // Set & GET the SrcRef
  void SetSrcRef (uint16_t SrcRef);
  uint16_t GetSrcRef ();

    // Set & GET the Tcp Port listening by the CAL
  void SetTcpport (uint16_t tcpPort);
  uint16_t GetTcpport ();

  // Set & GET the local Ip address (Cap)
  void SetLocalAddress (Address ip);
  Address GetLocalAddress ();

  void StartInitiate(Ptr<MmsServer> mmsServer);
  // Retrieve a reference of ApplicationContainer object (Sap)
  void SetApplicationContainerSap (ApplicationContainer containerSap);

  // Set & Get the pointer of the Current MmsServer
  void SetCurrentMmsServer (Ptr<MmsServer> CurrentMmsServer);
  Ptr<MmsServer> GetCurrentMmsServer();

  // Set & Get the type of requesting mechanism
  void SetTypeRequesting (int typeRequesting);
  bool GetTypeRequesting ();
 
  void SetSm (uint8_t nSm);

  // Set & Get the type of mode (LTE/Hybrid)
  void SetMode (int mode);
  int GetMode ();

  // Set & Get the next time that the Client(DC) requests to the Servers (SmartMeter,SM)
  void SetNextTimeRequest (Time nextTimeRequest);
  Time GetNextTimeRequest ();

  // Retrieve the node where the CAP is attached
  Ptr<Node> GetNode () const;

  // Retrieve the size in Bytes of the requested Data sent by the remote SAP
  uint32_t GetSizeReqData (); 
  
  // Type of services
  enum typeService { REQUEST, INDICATION, RESPONSE, CONFIRM };

  enum spdutype { CN = 0x0d, AC = 0x0e, DTD = 0x01};

  enum mmsServiceType { READ = 0x04, WRITE = 0x05 };

  enum mmsService { INITIATE, CONFIRMED, UNCONFIRMED, CONCLUDE };

  enum stateCf { CF_INACTIVE, CF_IDLE, CF_ASSOCIATION_PENDING, CF_ASSOCIATED, CF_ASSOCIATION_RELEASE_PENDING };
	
protected:

  virtual void DoDispose (void);

private:

  Ptr<MmsAdaptClient> m_mmsAdaptClient;
  uint16_t m_SrcRef;  // CAP Wrapper Port Number (unique id)
  uint16_t m_tcpPort;  // Tcp port
  uint8_t m_nReq;
  uint32_t m_count;
  uint32_t m_count1;
  Address m_localAddress;  // Local Ip address 
  ApplicationContainer m_containerSap; // Container of Sap in the scenario
  Ptr<MmsServer> m_currentMmsServer;  // Pointer of the current remote SAP
  Time m_nextTimeRequest; // Next time request of data to SAPs
  int m_typeRequesting;  // Type Requesting mechanism: 1 = MULTICASTING (simultaneous); 2 = SEQUENCIAL (Round Robin style); 3 = REPORTING
  uint32_t m_reqData; // The requested Data sent by the remote SAP
  uint32_t m_sizeReqData; // Size in Bytes of the requested Data sent by the remote SAP
  bool m_enableRpt;
  uint8_t m_nSm;
  uint32_t m_mode; //0 = LTE; 1 = Hybrid
  // Callback for DataConcentrator only
  Callback<void, uint32_t> m_recvData; 

  // Map container to store the AAs succesfully established 
  std::map<uint16_t, Ptr<MmsServer> > m_activeAa;
  std::map<uint16_t, Ptr<Socket> > m_activeSocket;
  std::map<uint16_t, Ptr<MmsServer> >::iterator m_it;
  std::map<uint16_t, Ptr<Socket> >::iterator m_it_socket;

  virtual void StartApplication (void);

  virtual void StopApplication (void);

  // Helpers parameters
  EventId m_startRequestEvent;
  EventId m_nextRequestEvent;
  EventId m_releaseAAEvent;
  std::vector<Ptr<Application> >::const_iterator m_itSap; // Iterator AppContainer
  uint16_t m_nSap;  // Number of Saps 
  uint32_t m_totalNSap;  // Total Number of Saps   
  uint8_t m_enableNewRQ;
  ostringstream ClientFile1;
};

} // namespace ns3

#endif /* MMS_CLIENT_H */
