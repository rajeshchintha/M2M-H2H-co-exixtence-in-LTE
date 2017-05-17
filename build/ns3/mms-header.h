#ifndef MMS_HEADER_H
#define MMS_HEADER_H

#include "ns3/header.h"

namespace ns3 {

enum CotpType
{ 
  CRE = 0xe0,
  CC = 0xd0,
  DT = 0xf0
};

enum SpduType
{
	CN = 0x0d,
	AC = 0x0e,
	DTD = 0x01
};

enum MmsType
{
	CONFIRMED_REQUEST = 0xa0,
	CONFIRMED_RESPONSE = 0xa1,
	UNCONFIRMED_SRV = 0xa3
};

enum MmsConfirmedType
{
	READ = 0x04,
	WRITE = 0x05
};

/*
 * MMS Type Header  //////////////////////////////////////////////////////////////////////
 */

class MmsTypeHeader : public Header
{
public:
	MmsTypeHeader ();
	virtual ~MmsTypeHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;

	uint8_t GetTypeMms() const;
	uint8_t GetTypeConfirmed() const;
	void SetType(int typeMms, int typeConfirmed, int m_nReq);

	uint8_t GetInvokeID() const;

private:
	uint8_t m_mmsServiceType;
	uint16_t m_MmsConfirmedRequestParam1;
	uint8_t m_MmsConfirmedRequestParam2;
	uint8_t m_invokeId;
	uint8_t m_mmsConfirmedType;
};

/*
 * MMS Unconfirmed Service APDU  //////////////////////////////////////////////////////////////////////
 */

class MmsUnconfirmedHeader : public Header
{
public:
	MmsUnconfirmedHeader ();
	virtual ~MmsUnconfirmedHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;


private:
	uint64_t m_Data1;
	uint64_t m_Data2;
	uint64_t m_Data3;
	uint64_t m_Data4;
	uint64_t m_Data5;
	uint64_t m_Data6;
	uint64_t m_Data7;
	uint64_t m_Data8;
	uint64_t m_Data9;
	uint64_t m_Data10;
	uint64_t m_Data11;
	uint16_t m_Data12;
	uint8_t m_Data13;
};

/*
 * MMS Confirmed Read Response APDU  //////////////////////////////////////////////////////////////////////
 */

class MmsConfirmedReadResponseHeader : public Header
{
public:
	MmsConfirmedReadResponseHeader ();
	virtual ~MmsConfirmedReadResponseHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;


private:
	uint16_t m_Length;
//	uint32_t m_mmsReadResponseParam0;
//	uint32_t m_mmsReadResponseParam1;
//	uint16_t m_mmsReadResponseParam2;
	uint16_t m_mmsReadResponseParam4;
	uint32_t m_mmsReadResponseData;
};

/*
 * MMS Confirmed Read Request APDU  //////////////////////////////////////////////////////////////////////
 */

class MmsConfirmedReadRequestHeader : public Header
{
public:
	MmsConfirmedReadRequestHeader ();
	virtual ~MmsConfirmedReadRequestHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;


private:
	uint64_t m_mmsConfirmedTypeParam;
	uint64_t m_MmsConfirmedRequestParam2;
	uint64_t m_MmsDeviceName;
	uint32_t m_MmsDeviceID;
	uint16_t m_itemID;
	uint64_t m_MmsReportName1;
	uint64_t m_MmsReportName2;
};

/*
 * MMS Confirmed Write Response APDU  //////////////////////////////////////////////////////////////////////
 */

class MmsConfirmedWriteResponseHeader : public Header
{
public:
	MmsConfirmedWriteResponseHeader ();
	virtual ~MmsConfirmedWriteResponseHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;


private:
	uint8_t m_mmsWriteResponseData1;
	uint16_t m_mmsWriteResponseData2;
};

/*
 * MMS Confirmed Write Request APDU  //////////////////////////////////////////////////////////////////////
 */

class MmsConfirmedWriteRequestHeader : public Header
{
public:
	MmsConfirmedWriteRequestHeader ();
	virtual ~MmsConfirmedWriteRequestHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;


private:
	uint8_t m_mmsConfirmedTypeLength;
	uint16_t m_mmsConfirmedTypeParam;
	uint64_t m_MmsConfirmedRequestParam2;
	uint64_t m_MmsDeviceName;
	uint32_t m_MmsDeviceID;
	uint8_t m_itemID;
	uint64_t m_MmsReportName1;
	uint64_t m_MmsReportName2;
	uint64_t m_MmsReportName3;
	uint64_t m_MmsReportName4;
	uint8_t m_listOfData;
	uint32_t m_data;
};


/*
 * MMS Initiate Response APDU  //////////////////////////////////////////////////////////////////////
 */

class MmsInitiateResponseHeader : public Header
{
public:
	MmsInitiateResponseHeader ();
	virtual ~MmsInitiateResponseHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;


private:

	uint8_t m_mmsServiceType;
	uint8_t m_mmsLengh;
	uint64_t m_MmsInitResponseParam1;
	uint64_t m_MmsInitResponseParam2;
	uint64_t m_MmsInitResponseParam3;
	uint64_t m_MmsInitResponseParam4;
	uint32_t m_MmsInitResponseParam5;
	uint8_t m_MmsInitResponseParam6;
};

/*
 * MMS Initiate Request Header  //////////////////////////////////////////////////////////////////////
 */

class MmsInitiateRequestHeader : public Header
{
public:
	MmsInitiateRequestHeader ();
	virtual ~MmsInitiateRequestHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;


private:

	uint8_t m_mmsServiceType;
	uint8_t m_mmsLengh;
	uint64_t m_MmsInitRequestParam1;
	uint64_t m_MmsInitRequestParam2;
	uint64_t m_MmsInitRequestParam3;
	uint64_t m_MmsInitRequestParam4;
	uint32_t m_MmsInitRequestParam5;
	uint16_t m_MmsInitRequestParam6;
};

/*
 * MMS ACSE AARE Header  //////////////////////////////////////////////////////////////////////
 */

class MmsAcseAareHeader : public Header
{
public:
	MmsAcseAareHeader ();
	virtual ~MmsAcseAareHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;


private:

	uint8_t m_idAcse;
	uint8_t m_length;
	uint64_t m_PresentationInitParam1;
	uint64_t m_PresentationInitParam2;
	uint64_t m_PresentationInitParam3;
	uint64_t m_PresentationInitParam4;
	uint64_t m_PresentationInitParam5;
	uint64_t m_PresentationInitParam6;
};

/*
 * MMS ACSE Header //////////////////////////////////////////////////////////////////////
 */

class MmsAcseAarqHeader : public Header
{
public:
	MmsAcseAarqHeader ();
	virtual ~MmsAcseAarqHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;


private:

	uint8_t m_idAcse;
	uint8_t m_length;
	uint64_t m_PresentationInitParam1;
	uint64_t m_PresentationInitParam2;
	uint64_t m_PresentationInitParam3;
	uint64_t m_PresentationInitParam4;
	uint64_t m_PresentationInitParam5;
	uint64_t m_PresentationInitParam6;
	uint8_t m_MMS_length;

};

/*
 * MMS Presentation Header  //////////////////////////////////////////////////////////////////////
 */

class MmsPresentationHeader : public Header
{
public:
	MmsPresentationHeader ();
	virtual ~MmsPresentationHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;

	void SetLength(uint16_t length);

private:

	uint8_t m_idPpdu;  // PPDU identifier (2B) = 0x3181
	uint16_t m_length1;  // SPDU Length (1B) = 0x88
	uint8_t m_param1;
	uint16_t m_length2;
	uint32_t m_param2;
	uint16_t m_length3;
};

/*
 * MMS Presentation Initiate Response Header  //////////////////////////////////////////////////////////////////////
 */

class MmsPresentationInitResponseHeader : public Header
{
public:
	MmsPresentationInitResponseHeader ();
	virtual ~MmsPresentationInitResponseHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;


private:

	uint16_t m_idPpdu;  // PPDU identifier (2B) = 0x3181
	uint8_t m_length;  // SPDU Length (1B) = 0x88
	uint8_t m_modeSelector; // =
	uint32_t m_modeSelectorParam;
	uint64_t m_modeParam1;
	uint64_t m_modeParam2;
	uint64_t m_modeParam3;
	uint64_t m_modeParam4;
	uint64_t m_modeParam5;
	uint8_t m_presentationDataValue;
	uint8_t m_presentationDataValueLength;

};

/*
 * MMS Presentation Initiate Request Header  //////////////////////////////////////////////////////////////////////
 */

class MmsPresentationInitRequestHeader : public Header
{
public:
	MmsPresentationInitRequestHeader ();
	virtual ~MmsPresentationInitRequestHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;


private:

	uint16_t m_idPpdu;  // PPDU identifier (2B)
	uint8_t m_length;  // SPDU Length (1B)
	uint8_t m_modeSelector;
	uint32_t m_modeSelectorParam;
	uint16_t m_normalModeSelector;
	uint8_t m_lengthMode;
	uint64_t m_modeParam1;
	uint32_t m_modeParam2;
	uint8_t m_contextList;
	uint8_t m_contextListLength;
	uint64_t m_contextListParam1;
	uint64_t m_contextListParam2;
	uint64_t m_contextListParam3;
	uint64_t m_contextListParam4;
	uint32_t m_contextListParam5;
	uint8_t m_userDataLength;
	uint8_t m_pdvListIndication;
	uint8_t m_pdvListLength;
	uint32_t m_presentationDataValue;
	uint8_t m_presentationDataValueLength;

};


/*
 * MMS Session Header  //////////////////////////////////////////////////////////////////////
 */

class MmsSessionTypeHeader : public Header
{
public:
	MmsSessionTypeHeader ();
	virtual ~MmsSessionTypeHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;

	// Allow protocol-specific access to the header
	void SetIdSpdu (uint8_t idSpdu);
	uint8_t GetIdSpdu (void) const;
	void SetLength (uint8_t length);
	uint8_t GetLength (void) const;


private:

	uint8_t m_idSpdu;  // SPDU identifier (1B)
	uint8_t m_length;  // SPDU Length (1B)
};

////////////////////////////////////////////////////////////////

class MmsSessionHeader : public Header
{
public:
	MmsSessionHeader ();
	virtual ~MmsSessionHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;

	// Allow protocol-specific access to the header
	void SetConnectAcceptItem (uint64_t connectAcceptItem);
	uint64_t GetConnectAcceptItem (void) const;
	void SetSessionRequirement (uint32_t sessionRequirement);
	uint32_t GetSessionRequirement (void) const;
	void SetCallingSessionSelector (uint32_t callingSessionSelector);
	uint32_t GetCallingSessionSelector (void) const;
	void SetCalledSessionSelector (uint32_t calledSessionSelector);
	uint32_t GetCalledSessionSelector (void) const;
	void SetSessionUserDataIndication (uint8_t sessionUserDataIndication);
	uint8_t GetSessionUserDataIndication (void) const;
	void SetSessionUserDataLength (uint8_t sessionUserDataLength);
	uint8_t GetSessionUserDataLength (void) const;



private:

	uint64_t m_connectAcceptItem;
	uint32_t m_sessionRequirement;
	uint32_t m_callingSessionSelector;
	uint32_t m_calledSessionSelector;
	uint8_t m_sessionUserDataIndication;
	uint8_t m_sessionUserDataLength;
  
};

class MmsSessionInitResponseHeader : public Header
{
public:
	MmsSessionInitResponseHeader ();
	virtual ~MmsSessionInitResponseHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;

	// Allow protocol-specific access to the header
	void SetConnectAcceptItem (uint64_t connectAcceptItem);
	uint64_t GetConnectAcceptItem (void) const;
	void SetSessionRequirement (uint32_t sessionRequirement);
	uint32_t GetSessionRequirement (void) const;
	void SetCallingSessionSelector (uint32_t callingSessionSelector);
	uint32_t GetCallingSessionSelector (void) const;
	void SetCalledSessionSelector (uint32_t calledSessionSelector);
	uint32_t GetCalledSessionSelector (void) const;
	void SetSessionUserDataIndication (uint8_t sessionUserDataIndication);
	uint8_t GetSessionUserDataIndication (void) const;
	void SetSessionUserDataLength (uint8_t sessionUserDataLength);
	uint8_t GetSessionUserDataLength (void) const;



private:

	uint64_t m_connectAcceptItem;
	uint32_t m_sessionRequirement;
	uint8_t m_sessionUserDataIndication;
	uint8_t m_sessionUserDataLength;

};

/**
 * COTP sub-layer header
 */

class CotpTypeHeader: public Header
{
public:
	CotpTypeHeader ();
	virtual ~CotpTypeHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;

	// Allow protocol-specific access to the header
	void SetTptkVersion (uint8_t version);
	uint8_t GetTptkVersion (void) const;
	void SetTptkReserved (uint8_t reserved);
	uint8_t GetTptkReserved (void) const;
	void SetTptkLength (uint16_t length);
	uint16_t GetTptkLength (void) const;
	void SetCotpLength (uint8_t length);
	uint8_t GetCotpLength (void) const;
	void SetCotpPduType (uint8_t type);
	uint8_t GetCotpPduType (void) const;

private:

	uint8_t m_TptkVersion;  // Version of TPTK (3)
	uint8_t m_TptkReserved; // TPTK reserved (0)
	uint16_t m_TptkLength;   // Length of the higher protocol packet transported (2B)
	uint8_t m_CotpLength;
	uint8_t m_CotpPduType;
};

class CotpHeader: public Header
{
public:
	CotpHeader ();
	virtual ~CotpHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;

	// Allow protocol-specific access to the header
	void SetDstRef (uint16_t ref);
	uint16_t GetDstRef (void) const;
	void SetSrcRef (uint16_t ref);
	uint16_t GetSrcRef (void) const;
	void SetCotpParamField1 (uint32_t param);
	uint32_t GetCotpParamField1 (void) const;
	void SetCotpParamField2 (uint16_t param);
	uint16_t GetCotpParamField2 (void) const;
	void SetDstTsap (uint16_t dstTsap);
	uint16_t GetDstTsap (void) const;
	void SetCotpParamField3 (uint16_t param);
	uint16_t GetCotpParamField3 (void) const;
	void SetSrcTsap (uint16_t srcTsap);
	uint16_t GetSrcTsap (void) const;

private:

	uint16_t m_DstRef;
	uint16_t m_SrcRef;
	uint32_t m_CotpParamField1;
	uint16_t m_CotpParamField2;
	uint16_t m_DstTsap;
	uint16_t m_CotpParamField3;
	uint16_t m_SrcTsap;
};

class CotpDtHeader: public Header
{
public:
	CotpDtHeader ();
	virtual ~CotpDtHeader ();

	static TypeId GetTypeId (void);
	virtual TypeId GetInstanceTypeId (void) const;
	virtual uint32_t GetSerializedSize (void) const;
	virtual void Serialize (Buffer::Iterator start) const;
	virtual uint32_t Deserialize (Buffer::Iterator start);
	virtual void Print (std::ostream &os) const;

	// Allow protocol-specific access to the header
	void SetParam (uint8_t param);
	uint8_t GetParam (void) const;

private:

	uint8_t m_Param;
};

} // namespace ns3

#endif /* MMS_HEADER_H */
