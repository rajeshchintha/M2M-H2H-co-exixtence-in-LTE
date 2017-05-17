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

#include "ns3/log.h"
#include "cosem-header.h"
 
namespace ns3 {

NS_LOG_COMPONENT_DEFINE ("TypeAPDU");

/*-----------------------------------------------------------------------------
 *  APDU TYPE HEADER
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (TypeAPDU);

TypeId 
TypeAPDU::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::TypeAPDU")
    .SetParent<Header> ()
    .AddConstructor<TypeAPDU> ()
    ;
  return tid;
}

TypeAPDU::TypeAPDU ()
{
  m_type = AARQ;
}

TypeAPDU::~TypeAPDU()
{

}

TypeId 
TypeAPDU::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
TypeAPDU::GetSerializedSize (void) const
{
  return 1;
}
 
void 
TypeAPDU::Serialize (Buffer::Iterator start) const
{
  start.WriteU8 ((uint8_t) m_type);
}

uint32_t 
TypeAPDU::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  uint8_t type = i.ReadU8 ();
  switch (type)
   {
    case AARQ: 
    case AARE:
    case RLRQ:
    case RLRE: 
    case GETRQ_N: 
    case GETRES_N:
      {
        m_type = (ApduType) type;
        break;
      }
    default:
      NS_LOG_INFO ("ApduType not valid!");  
   }  
  return GetSerializedSize ();
}

void 
TypeAPDU::Print (std::ostream &os) const
{  
 switch (m_type)
   {
    case AARQ:
      {
        os << "AARQ";
        break;
      } 
    case AARE:
      {
        os << "AARE";
        break;
      }
    case RLRQ:
      {
        os << "RLRQ";
        break;
      }
    case RLRE: 
      {
        os << "RLRE";
        break;
      }
    case GETRQ_N: 
      {
        os << "GETRQ_N";
        break;
      }
    case GETRES_N:
      {
        os << "GETRES_N";
        break;
      }
    default:
      os << "UNKNOWN_TYPE";
   }  
}

void 
TypeAPDU::SetApduType (ApduType type)
{
  m_type = type;
}

ApduType
TypeAPDU::GetApduType () const 
{
  return m_type;
}

/*-----------------------------------------------------------------------------
 *  AARQ APDU
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (CosemAarqHeader);

TypeId 
CosemAarqHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::CosemAarqHeader")
    .SetParent<Header> ()
    .AddConstructor<CosemAarqHeader> ()
    ;
  return tid;
}

CosemAarqHeader::CosemAarqHeader ()
{
  m_idApdu = AARQ ;
  m_protocolVersion = 0 ;
  m_applicationContextName = 0;
  m_dedicatedKey = 0;
  m_responseAllowed = true;
  m_proposedQualityOfService = 0;  	
  m_proposedDlmsVersionNumber = 0;
  m_proposedConformance = 0;   	
  m_clientMaxReceivePduSize = 0; 
}

CosemAarqHeader::~CosemAarqHeader ()
{

}

TypeId 
CosemAarqHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
CosemAarqHeader::GetSerializedSize (void) const
{
  return 20;
}
 
void 
CosemAarqHeader::Serialize (Buffer::Iterator start) const
{
  start.WriteU8 (m_idApdu);
  start.WriteU8 (m_protocolVersion);
  start.WriteHtonU64 (m_applicationContextName);
  start.WriteU8 (m_dedicatedKey);
  start.WriteU8 ((uint8_t) m_responseAllowed);
  start.WriteU8 (m_proposedQualityOfService);
  start.WriteU8 (m_proposedDlmsVersionNumber);
  start.WriteHtonU32 (m_proposedConformance);
  start.WriteHtonU16 (m_clientMaxReceivePduSize);
}

uint32_t 
CosemAarqHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  m_idApdu = i.ReadU8 ();
  m_protocolVersion = i.ReadU8 ();
  m_applicationContextName = i.ReadNtohU64 ();
  m_dedicatedKey = i.ReadU8 ();
  m_responseAllowed = (bool)i.ReadU8 ();
  m_proposedQualityOfService = i.ReadU8 ();
  m_proposedDlmsVersionNumber = i.ReadU8 ();
  m_proposedConformance = i.ReadNtohU32 ();
  m_clientMaxReceivePduSize = i.ReadNtohU16 ();
  return GetSerializedSize ();
}

void 
CosemAarqHeader::Print (std::ostream &os) const
{
  os << "id AARQ " << m_idApdu
     << "Protocol Version " << m_protocolVersion
     << "Application Context Name " << m_applicationContextName
     << "Dedicated Key " << m_dedicatedKey
     << "Reponse Allowed " << m_responseAllowed
     << "Proposed Quality Of Service " << m_proposedQualityOfService
     << "Version number " << m_proposedDlmsVersionNumber
     << "Proposed Conformance " << m_proposedConformance
     << "Client Max Receive PDU Size " << m_clientMaxReceivePduSize;   
}

void 
CosemAarqHeader::SetIdApdu (uint8_t idApdu)
{
  m_idApdu = idApdu;
}

uint8_t 
CosemAarqHeader::GetIdApdu (void) const
{
  return m_idApdu;
}

void
CosemAarqHeader::SetProtocolVersion (uint8_t protocolVersion)
{
  m_protocolVersion = protocolVersion;
}

uint8_t 
CosemAarqHeader::GetProtocolVersion (void) const
{
  return m_protocolVersion;
}

void
CosemAarqHeader::SetApplicationContextName (uint64_t applicationContextName)
{
  m_applicationContextName = applicationContextName;
}

uint64_t
CosemAarqHeader::GetApplicationContextName (void) const
{
  return m_applicationContextName;
}  
  
void
CosemAarqHeader::SetDedicatedKey (uint8_t dedicatedKey)
{
  m_dedicatedKey = dedicatedKey;
}

uint8_t
CosemAarqHeader::GetDedicatedKey (void) const
{
  return m_dedicatedKey;
}

void
CosemAarqHeader::SetResponseAllowed (bool responseAllowed)
{
  m_responseAllowed = responseAllowed;
}

bool 
CosemAarqHeader::GetResponseAllowed (void) const
{
  return m_responseAllowed;
}

void 
CosemAarqHeader::SetProposedQualityOfService (uint8_t proposedQualityOfService)
{
  m_proposedQualityOfService = proposedQualityOfService;
}

uint8_t 
CosemAarqHeader::GetProposedQualityOfService (void) const
{
  return m_proposedQualityOfService;
} 

void 
CosemAarqHeader::SetProposedDlmsVersionNumber (uint8_t proposedDlmsVersionNumber)
{
  m_proposedDlmsVersionNumber = proposedDlmsVersionNumber;
} 

uint8_t 
CosemAarqHeader::GetProposedDlmsVersionNumber (void) const
{
  return m_proposedDlmsVersionNumber;
}

void 
CosemAarqHeader::SetProposedConformance (uint32_t proposedConformance)
{
  m_proposedConformance = proposedConformance;
}

uint32_t 
CosemAarqHeader::GetProposedConformance (void) const
{
  return m_proposedConformance;
}

void 
CosemAarqHeader::SetClientMaxReceivePduSize (uint16_t clientMaxReceivePduSize)
{
  m_clientMaxReceivePduSize = clientMaxReceivePduSize;
} 

uint16_t 
CosemAarqHeader::GetClientMaxReceivePduSize (void) const
{
  return m_clientMaxReceivePduSize;
}  

/*-----------------------------------------------------------------------------
 *  AARE APDU
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (CosemAareHeader);

TypeId 
CosemAareHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::CosemAareHeader")
    .SetParent<Header> ()
    .AddConstructor<CosemAareHeader> ()
    ;
  return tid;
}

CosemAareHeader::CosemAareHeader ()
{
  m_idApdu = AARE;
  m_protocolVersion = 0;
  m_applicationContextName = 0;
  m_result = 0; // Result of the request AA, {0, accepted}
  m_resultSourceDiagnostic = 0;  // Result of a rejection of the a request AA, {0, null}
  m_negotiatedQualityOfService = 0;  
  m_negotiatedDlmsVersionNumber = 6; 
  m_negotiatedConformance = 0x001010; // {0x001010}, Based on the example in Annex C IEC 62056-53
  m_serverMaxReceivePduSize = 0x1F4;  // Server_Max_Receive_PDU_Size,{0x1F4}: 500 bytes 
  m_vaaName = 0x0007; // Dummy Value {0x0007}.Taken from page 98 IEC 62056-53
}

CosemAareHeader::~CosemAareHeader ()
{

}

TypeId 
CosemAareHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
CosemAareHeader::GetSerializedSize (void) const
{
  return 22;
}
 
void 
CosemAareHeader::Serialize (Buffer::Iterator start) const
{
  start.WriteU8 (m_idApdu);
  start.WriteU8 (m_protocolVersion);
  start.WriteHtonU64 (m_applicationContextName);
  start.WriteU8 (m_result);
  start.WriteU8 (m_resultSourceDiagnostic);
  start.WriteU8 (m_negotiatedQualityOfService);
  start.WriteU8 (m_negotiatedDlmsVersionNumber);
  start.WriteHtonU32 (m_negotiatedConformance);
  start.WriteHtonU16 (m_serverMaxReceivePduSize);
  start.WriteHtonU16 (m_vaaName);
}

uint32_t 
CosemAareHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  m_idApdu = i.ReadU8 ();
  m_protocolVersion = i.ReadU8 ();
  m_applicationContextName = i.ReadNtohU64 ();
  m_result = i.ReadU8 ();
  m_resultSourceDiagnostic = i.ReadU8 ();
  m_negotiatedQualityOfService = i.ReadU8 ();
  m_negotiatedDlmsVersionNumber = i.ReadU8 ();
  m_negotiatedConformance = i.ReadNtohU32 ();
  m_serverMaxReceivePduSize = i.ReadNtohU16 ();
  m_vaaName = i.ReadNtohU16 ();

  return GetSerializedSize ();
}

void 
CosemAareHeader::Print (std::ostream &os) const
{
  os << "id AARQ " << m_idApdu
     << "Protocol Version " << m_protocolVersion
     << "Application Context Name " << m_applicationContextName
     << "Result of the request AA " << m_result
     << "Result of a rejection of the a request AA " << m_resultSourceDiagnostic
     << "Negotiated Quality Of Service " << m_negotiatedQualityOfService
     << "Negotiated Dlms Version Number " <<  m_negotiatedDlmsVersionNumber
     << "Negotiated Conformance " << m_negotiatedConformance 
     << "Server Max Receive Pdu Size " <<  m_serverMaxReceivePduSize
     << "vaa-name " <<  m_vaaName;
}

void 
CosemAareHeader::SetIdApdu (uint8_t idApdu)
{
  m_idApdu = idApdu;
}

uint8_t
CosemAareHeader::GetIdApdu (void) const
{
  return m_idApdu;
}

void 
CosemAareHeader::SetProtocolVersion (uint8_t protocolVersion)
{
  m_protocolVersion = protocolVersion;
}

uint8_t 
CosemAareHeader::GetProtocolVersion (void) const
{
  return m_protocolVersion;
}

void 
CosemAareHeader::SetApplicationContextName (uint64_t applicationContextName)
{
  m_applicationContextName = applicationContextName;
}

uint64_t 
CosemAareHeader::GetApplicationContextName (void) const
{
  return m_applicationContextName;
}
  
void 
CosemAareHeader::SetResult (uint8_t result)
{
  m_result = result;
}

uint8_t
CosemAareHeader::GetResult (void) const
{
  return m_result;
}
  
void 
CosemAareHeader::SetResultSourceDiagnostic (uint8_t resultSourceDiagnostic)
{
  m_resultSourceDiagnostic = resultSourceDiagnostic;
}

uint8_t 
CosemAareHeader::GetResultSourceDiagnostic (void) const
{
  return m_resultSourceDiagnostic;
}
 
void 
CosemAareHeader::SetNegotiatedQualityOfService (uint8_t negotiatedQualityOfService)
{
  m_negotiatedQualityOfService = negotiatedQualityOfService;
}

uint8_t 
CosemAareHeader::GetNegotiatedQualityOfService (void) const
{
  return m_negotiatedQualityOfService;
}

void 
CosemAareHeader::SetNegotiatedDlmsVersionNumber (uint8_t negotiatedDlmsVersionNumber)
{
  m_negotiatedDlmsVersionNumber = negotiatedDlmsVersionNumber;
}

uint8_t 
CosemAareHeader::GetNegotiatedDlmsVersionNumber (void) const
{
  return m_negotiatedDlmsVersionNumber;
}

void 
CosemAareHeader::SetNegotiatedConformance (uint32_t negotiatedConformance)
{
  m_negotiatedConformance = negotiatedConformance;
}

uint32_t 
CosemAareHeader::GetNegotiatedConformance (void) const
{
  return m_negotiatedConformance;
}

void
CosemAareHeader::SetServerMaxReceivePduSize (uint16_t serverMaxReceivePduSize)
{
  m_serverMaxReceivePduSize = serverMaxReceivePduSize;
}
 
uint16_t 
CosemAareHeader::GetServerMaxReceivePduSize (void) const
{
  return m_serverMaxReceivePduSize;
}
  
void
CosemAareHeader::SetVaaName (uint16_t vaaName)
{
  m_vaaName = vaaName;
}
 
uint16_t 
CosemAareHeader::GetVaaName (void) const
{
  return m_vaaName;
}

/*-----------------------------------------------------------------------------
 *  RLRQ APDU
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (CosemRlrqHeader);

TypeId 
CosemRlrqHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::CosemRlrqHeader")
    .SetParent<Header> ()
    .AddConstructor<CosemRlrqHeader> ()
    ;
  return tid;
}

CosemRlrqHeader::CosemRlrqHeader ()
{
  m_idApdu = RLRQ ;
  m_reason = 0;
}

CosemRlrqHeader::~CosemRlrqHeader ()
{

}

TypeId 
CosemRlrqHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
CosemRlrqHeader::GetSerializedSize (void) const
{
  return 2;
}
 
void 
CosemRlrqHeader::Serialize (Buffer::Iterator start) const
{
  start.WriteU8 (m_idApdu);
  start.WriteU8 (m_reason);
}

uint32_t 
CosemRlrqHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  m_idApdu = i.ReadU8 ();
  m_reason = i.ReadU8 ();
  return GetSerializedSize ();
}

void 
CosemRlrqHeader::Print (std::ostream &os) const
{
  os << "id RLRQ " << m_idApdu
     << "Release request reason " << m_reason;
}

void 
CosemRlrqHeader::SetIdApdu (uint8_t idApdu)
{
  m_idApdu = idApdu;
}

uint8_t 
CosemRlrqHeader::GetIdApdu (void) const
{
  return m_idApdu;
}

void 
CosemRlrqHeader::SetReason (uint8_t reason)
{
  m_reason = reason;
}

uint8_t 
CosemRlrqHeader::GetReason (void) const
{
  return m_reason;
}

/*-----------------------------------------------------------------------------
 *  RLRE APDU
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (CosemRlreHeader);

TypeId 
CosemRlreHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::CosemRlreHeader")
    .SetParent<Header> ()
    .AddConstructor<CosemRlreHeader> ()
    ;
  return tid;
}

CosemRlreHeader::CosemRlreHeader ()
{
  m_idApdu = RLRE;
  m_reason = 0;
}

CosemRlreHeader::~CosemRlreHeader ()
{

}

TypeId 
CosemRlreHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
CosemRlreHeader::GetSerializedSize (void) const
{
  return 2;
}
 
void 
CosemRlreHeader::Serialize (Buffer::Iterator start) const
{
  start.WriteU8 (m_idApdu);
  start.WriteU8 (m_reason);
}

uint32_t 
CosemRlreHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  m_idApdu = i.ReadU8 ();
  m_reason = i.ReadU8 ();
  return GetSerializedSize ();
}

void 
CosemRlreHeader::Print (std::ostream &os) const
{
  os << "id RLRE " << m_idApdu
     << "Release request reason " << m_reason;
}

void 
CosemRlreHeader::SetIdApdu (uint8_t idApdu)
{
  m_idApdu = idApdu;
}

uint8_t
CosemRlreHeader::GetIdApdu (void) const
{
  return m_idApdu;
}

void 
CosemRlreHeader::SetReason (uint8_t reason)
{
  m_reason = reason;
}

uint8_t 
CosemRlreHeader::GetReason (void) const
{
  return m_reason;
}
 
/*-----------------------------------------------------------------------------
 *  GET-Request (Normal) APDU
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (CosemGetRequestNormalHeader);

TypeId 
CosemGetRequestNormalHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::CosemGetRequestNormalHeader")
    .SetParent<Header> ()
    .AddConstructor<CosemGetRequestNormalHeader> ()
    ;
  return tid;
}

CosemGetRequestNormalHeader::CosemGetRequestNormalHeader ()
{
  m_idApdu = GETRQ_N;
  m_typeGetRequest = 1;   // Normal
  m_invokeIdAndPriority = 0x02; // 0000 0010 (invoke_id {0b0000}),service_class= 1 (confirmed) priority level ({normal})) 
  m_classId = 0X03;  // Class Register
  m_instanceId = 0x010100070000;  // OBIS CODE: 1.1.0.7.0.0
  m_attributeId = 0x02; // Second Attribut = Value
}

CosemGetRequestNormalHeader::~CosemGetRequestNormalHeader ()
{

}

TypeId 
CosemGetRequestNormalHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
CosemGetRequestNormalHeader::GetSerializedSize (void) const
{
  return 14;
}
 
void 
CosemGetRequestNormalHeader::Serialize (Buffer::Iterator start) const
{
  start.WriteU8 (m_idApdu);
  start.WriteU8 (m_typeGetRequest);
  start.WriteU8 (m_invokeIdAndPriority);
  start.WriteHtonU16 (m_classId);
  start.WriteHtonU64 (m_instanceId);
  start.WriteU8 (m_attributeId);
}

uint32_t 
CosemGetRequestNormalHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  m_idApdu = i.ReadU8 ();
  m_typeGetRequest = i.ReadU8 ();
  m_invokeIdAndPriority = i.ReadU8 ();
  m_classId = i.ReadNtohU16 ();
  m_instanceId = i.ReadNtohU64 ();
  m_attributeId = i.ReadU8 ();

  return GetSerializedSize ();
}

void 
CosemGetRequestNormalHeader::Print (std::ostream &os) const
{
  os << "id GET-Request (Normal) APDU " << m_idApdu
     << "Type Get-Request " << m_typeGetRequest
     << "Invoke-Id And Priority " << m_invokeIdAndPriority
     << "Cosem Attribute Descriptor: Class-Id " <<  m_classId
     << "Cosem Attribute Descriptor: Instance-Id " << m_instanceId 
     << "Cosem Attribute Descriptor: Attribute-Id " << m_attributeId;
}

void 
CosemGetRequestNormalHeader::SetIdApdu (uint8_t idApdu)
{
  m_idApdu = idApdu;
}

uint8_t 
CosemGetRequestNormalHeader::GetIdApdu (void) const
{
  return m_idApdu;
}

void 
CosemGetRequestNormalHeader::SetTypeGetRequest (uint8_t typeGetRequest)
{
  m_typeGetRequest = typeGetRequest;
}

uint8_t 
CosemGetRequestNormalHeader::GetTypeGetRequest(void) const
{
  return m_typeGetRequest;
}

void 
CosemGetRequestNormalHeader::SetInvokeIdAndPriority (uint8_t invokeIdAndPriority)
{
  m_invokeIdAndPriority = invokeIdAndPriority;
}

uint8_t 
CosemGetRequestNormalHeader::GetInvokeIdAndPriority (void) const
{
  return m_invokeIdAndPriority;
}

void 
CosemGetRequestNormalHeader::SetClassId (uint16_t classId)
{
  m_classId = classId;
}

uint16_t 
CosemGetRequestNormalHeader::GetClassId (void) const
{
  return m_classId;
}
 
void 
CosemGetRequestNormalHeader::SetInstanceId (uint64_t instanceId)
{
  m_instanceId = instanceId;
}

uint64_t 
CosemGetRequestNormalHeader::GetInstanceId (void) const
{
  return m_instanceId;
}
  
void 
CosemGetRequestNormalHeader::SetAttributeId (uint8_t attributeId)
{
  m_attributeId = attributeId;
}

uint8_t 
CosemGetRequestNormalHeader::GetAttributeId (void) const
{
  return m_attributeId;
}

/*-----------------------------------------------------------------------------
 *  GET-Response (Normal) APDU
 *-----------------------------------------------------------------------------
 */
NS_OBJECT_ENSURE_REGISTERED (CosemGetResponseNormalHeader);

TypeId 
CosemGetResponseNormalHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::CosemGetResponseNormalHeader")
    .SetParent<Header> ()
    .AddConstructor<CosemGetResponseNormalHeader> ()
    ;
  return tid;
}

CosemGetResponseNormalHeader::CosemGetResponseNormalHeader ()
{
  m_idApdu = GETRES_N;
  m_typeGetResponse = 1;   // Normal
  m_invokeIdAndPriority = 0x02; // 0000 0010 (invoke_id {0b0000}),service_class= 1 (confirmed) priority level ({normal})) 
  m_data = 0; 
  m_dataAccessResult = 0; 
}

CosemGetResponseNormalHeader::~CosemGetResponseNormalHeader ()
{

}

TypeId 
CosemGetResponseNormalHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
CosemGetResponseNormalHeader::GetSerializedSize (void) const
{
  return 8;
}
 
void 
CosemGetResponseNormalHeader::Serialize (Buffer::Iterator start) const
{
  start.WriteU8 (m_idApdu);
  start.WriteU8 (m_typeGetResponse);
  start.WriteU8 (m_invokeIdAndPriority);
  start.WriteHtonU32 (m_data);
  start.WriteU8 (m_dataAccessResult);
}

uint32_t 
CosemGetResponseNormalHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start;
  m_idApdu = i.ReadU8 ();
  m_typeGetResponse = i.ReadU8 ();
  m_invokeIdAndPriority = i.ReadU8 ();
  m_data = i.ReadNtohU32 (); 
  m_dataAccessResult = i.ReadU8 ();

  return GetSerializedSize ();
}

void 
CosemGetResponseNormalHeader::Print (std::ostream &os) const
{
  os << "id GET-Response (Normal) APDU " << m_idApdu
     << "Type Get-Request " << m_typeGetResponse
     << "Invoke-Id And Priority " << m_invokeIdAndPriority
     << "Result: Data " <<  m_data
     << "Result: Data-Access-Result " << m_dataAccessResult; 
}

void 
CosemGetResponseNormalHeader::SetIdApdu (uint8_t idApdu)
{
  m_idApdu = idApdu;
}

uint8_t 
CosemGetResponseNormalHeader::GetIdApdu (void) const
{
  return m_idApdu;
}

void 
CosemGetResponseNormalHeader::SetTypeGetResponse (uint8_t typeGetResponse)
{
  m_typeGetResponse = typeGetResponse;
}

uint8_t 
CosemGetResponseNormalHeader::GetTypeGetResponse(void) const
{
  return m_typeGetResponse;
}

void 
CosemGetResponseNormalHeader::SetInvokeIdAndPriority (uint8_t invokeIdAndPriority)
{
  m_invokeIdAndPriority = invokeIdAndPriority;
}

uint8_t 
CosemGetResponseNormalHeader::GetInvokeIdAndPriority (void) const
{
  return m_invokeIdAndPriority;
}

void 
CosemGetResponseNormalHeader::SetData (uint16_t data)
{
  m_data = data;
}
 
uint32_t 
CosemGetResponseNormalHeader::GetData (void) const
{
  return m_data;
}
  
void 
CosemGetResponseNormalHeader::SetDataAccessResult (uint8_t dataAccessResult)
{
  m_dataAccessResult = dataAccessResult;
}

uint8_t 
CosemGetResponseNormalHeader::GetDataAccessResult (void) const
{
  return m_dataAccessResult;
}

/*-----------------------------------------------------------------------------
 *  WRAPPER PDU
 *-----------------------------------------------------------------------------
 */

NS_OBJECT_ENSURE_REGISTERED (CosemWrapperHeader);

TypeId 
CosemWrapperHeader::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::CosemWrapperHeader")
    .SetParent<Header> ()
    .AddConstructor<CosemWrapperHeader> ()
    ;
  return tid;
}

CosemWrapperHeader::CosemWrapperHeader ()
{
  m_version = 0x0001;  
  m_srcwPort = 0;
  m_dstwPort = 0; 
  m_length = 0;  
}

CosemWrapperHeader::~CosemWrapperHeader ()
{

}

TypeId 
CosemWrapperHeader::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}

uint32_t 
CosemWrapperHeader::GetSerializedSize (void) const
{
  return 8;
}
 
void 
CosemWrapperHeader::Serialize (Buffer::Iterator start) const
{
  start.WriteHtonU16 (m_version);
  start.WriteHtonU16 (m_srcwPort);
  start.WriteHtonU16 (m_dstwPort);
  start.WriteHtonU16 (m_length);
}

uint32_t 
CosemWrapperHeader::Deserialize (Buffer::Iterator start)
{
  Buffer::Iterator i = start; 
  m_version = i.ReadNtohU16 (); 
  m_srcwPort = i.ReadNtohU16 (); 
  m_dstwPort = i.ReadNtohU16 (); 
  m_length = i.ReadNtohU16 ();  

  return GetSerializedSize ();
}

void 
CosemWrapperHeader::Print (std::ostream &os) const
{
  os << "Version of Wrapper Sub-layer " << m_version
     << "Source wrapper port number " << m_srcwPort
     << "Destination wrapper port number " << m_dstwPort
     << "Length of the APDU transported " << m_length; 
}

void 
CosemWrapperHeader::SetVersion (uint16_t version)
{
  m_version = version;
}

uint16_t
CosemWrapperHeader::GetVersion (void) const
{
  return m_version;
}

void
CosemWrapperHeader::SetSrcwPort (uint16_t srcwPort)
{
  m_srcwPort = srcwPort;
}

uint16_t 
CosemWrapperHeader::GetSrcwPort (void) const
{
  return m_srcwPort;
}

void
CosemWrapperHeader::SetDstwPort (uint16_t dstwPort)
{
  m_dstwPort = dstwPort;
}

uint16_t 
CosemWrapperHeader::GetDstwPort (void) const
{
  return m_dstwPort;
}

void
CosemWrapperHeader::SetLength (uint16_t length)
{
  m_length = length;
}
 
uint16_t
CosemWrapperHeader::GetLength (void) const
{
  return m_length;
}

} // namespace ns3
