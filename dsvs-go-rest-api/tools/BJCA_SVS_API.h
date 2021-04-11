/************************************************************************/
/* SVS C Client Library and COM API                                     */
/************************************************************************/
/* @file: BJCA_SVS_API.h
 * @author: 
 * @date: 2011/11/08
 * @version: 1.0
 * =======================================================
 * change-log:
 *
 */

#ifndef _BJCA_SVS_API_H_
#define _BJCA_SVS_API_H_

#ifdef __cplusplus
extern "C" {
#endif

/**************** const defs *******************************************/
#define MAX_BUF_LEN						4096
#define MAX_LEN							2048


#define MAX_DATA_LEN					(3*1024*1024)
#define DATA_LEN_2M                     (2*1024*1024)
#define MAX_CERT_LEN					(8*1024)
#define MAX_SIGN_LEN					512
#define SHA1_HASH_LEN                   20
#define SM3_HASH_LEN                    32 
#define SHA256_HASH_LEN                 32
#define MAX_NUMBER_32                   32
#define MAX_NUMBER_60                   60
#define MAX_NUMBER_64                   64
#define MAX_NUMBER_128                  128
#define MAX_NUMBER_1024                 1024

#define BJCA_P7_ATTACH					0
#define BJCA_P7_DETACH					1

#define BJCA_ALGO_SM4_CBC                400
#define BJCA_ALGO_3DES_3KEY              102
#define BCA_ALG0_AES_256_CBC			1062


/************************************************************************/
/*  type definitions                                                    */
/************************************************************************/

typedef unsigned int			BJCA_UINT32;
typedef unsigned short			BJCA_UINT16;
typedef unsigned char			BJCA_UCHAR;
typedef char					BJCA_CHAR;
typedef int						BJCA_INT32;
typedef short					BJCA_INT16;
typedef void					BJCA_VOID;
typedef long					BJCA_LONG;
typedef unsigned long			BJCA_ULONG;
typedef int						BJCA_BOOL;

/*typedef clock_t					BJCA_TIME;*/

typedef BJCA_UINT32*			BJCA_UINT32_PTR;
typedef BJCA_UINT16*			BJCA_UINT16_PTR;
typedef BJCA_UCHAR*				BJCA_UCHAR_PTR;
typedef BJCA_CHAR*				BJCA_CHAR_PTR;
typedef BJCA_INT32*				BJCA_INT32_PTR;
typedef BJCA_INT16*				BJCA_INT16_PTR;
typedef BJCA_VOID*				BJCA_VOID_PTR;

/*typedef BJCA_INT32				BJCA_HANDLE;*/
typedef struct bjca_handle_t*	BJCA_HANDLE;

/** types */
struct bjca_handle_t {
	struct scpb_ctx_t *sctx;
	
	/* other data */
	unsigned char *appname;
	unsigned int   applen;
	
	/* file-hash algorithm */
	const struct env_md_st *md_type;
	/* sym-crypt */
	const struct evp_cipher_st *cipher;
};

typedef BJCA_LONG*				BJCA_LONG_PTR;
typedef BJCA_ULONG*				BJCA_ULONG_PTR;
typedef unsigned long			BCA_OBJ;

#define ECC_MAX_MODULUS_BITS_LEN     512    //ECC算法模数的最大长度
#define ECC_SM2_KEY_BYTES   (ECC_MAX_MODULUS_BITS_LEN/8)
#define ECC_MIN_KEY_LEN      65       // min SM2 key length
#define ECC_MAX_KEY_LEN      129      // max (64 * 2 + 1)

typedef struct ECCSignature_st {
	unsigned char   r[ECC_SM2_KEY_BYTES];
	unsigned char   s[ECC_SM2_KEY_BYTES];
}ECCSignature;


#define BJCA_MAX_NAME_LENGTH    256

typedef struct {
    BJCA_CHAR   MultiThreadSynchronizeKey[BJCA_MAX_NAME_LENGTH];
    BJCA_CHAR   ServerNum[BJCA_MAX_NAME_LENGTH];
    BJCA_CHAR   ServerIpAddress[BJCA_MAX_NAME_LENGTH];
    BJCA_CHAR	ServerPort[BJCA_MAX_NAME_LENGTH];
    /*	BJCA_CHAR   SemaphoreTimeOut[BJCA_MAX_NAME_LENGTH];*/
    BJCA_CHAR   SocketTimeOut[BJCA_MAX_NAME_LENGTH];
    /*	BJCA_CHAR   WaitIdleTimeOut[BJCA_MAX_NAME_LENGTH];*/
    BJCA_CHAR   ServerAppName[BJCA_MAX_NAME_LENGTH];
    BJCA_INT32	ServerAppNameLen;
} BJCA_CONFIGURE, *BJCA_CONFIGURE_PTR;

/************************************************************************/
/* error code definition                                                */
/************************************************************************/
#define BJCA_SUCCESS				0
#define BJCA_CONVERCHAR_ERROR					900
#define BJCA_ERROR_OPEN_REGKEY       901


#define BJCA_ERROR					1000
#define BJCA_ERROR_APP				1001
#define BJCA_ERROR_MEMORY			1002
#define BJCA_ERROR_FILE				1003
#define BJCA_ERROR_NULL				1004    //服务端返回NULL

//add
#define BJCA_ERROR_OBJECT			1005
#define BJCA_ERROR_SYMM				1006
#define BJCA_ERROR_UNKOWN	        1007    //服务端返回未知错误
#define BJCA_ERROR_FALSE	        1008    //服务端正常返回false
#define BJCA_ERROR_SERVICE_UNAVAILABLE 1009  //服务不可用

//end

#define BJCA_ERROR_HANDLE			1010
#define BJCA_ERROR_HANDLE_INVALID	1011

#define BJCA_ERROR_PARAM			1020
#define BJCA_ERROR_OUTPUT_PARAM     1021    //add
//#define BJCA_ERROR_PARAM_FIRST		1021
#define BJCA_ERROR_PARAM_SECOND		1022
#define BJCA_ERROR_PARAM_THIRD		1023
#define BJCA_ERROR_PARAM_FOURTH		1024
#define BJCA_ERROR_PARAM_FIFTH		1025
#define BJCA_ERROR_TS_VERIFY		1026
#define BJCA_SERVER_ERROR_PARAM		1027

#define BJCA_ERROR_PACKET			1030
#define BJCA_ERROR_PACKET_PACK		1031
#define BJCA_ERROR_PACKET_UNPACK	1032

#define BJCA_ERROR_SEMAP			1040
#define BJCA_ERROR_SEMAP_CREATE		1041
#define BJCA_ERROR_SEMAP_P			1042
#define BJCA_ERROR_SEMAP_V			1043

#define BJCA_ERROR_SOCKET			1050
#define BJCA_ERROR_SOCKET_INIT		1051
#define BJCA_ERROR_SOCKET_CONNECT	1052
#define BJCA_ERROR_SOCKET_SEND		1053
#define BJCA_ERROR_SOCKET_RECV		1054
#define BJCA_ERROR_SOCKET_GETIP		1055
#define BJCA_ERROR_SOCKET_RENEW		1056
#define BJCA_ERROR_SOCKET_COUNT		1057

//define for updatecert
#define BJCA_ERROR_COM_INSTANCE        1060
#define BJCA_ERROR_EXPORT_CERT         1061
#define BJCA_ERROR_UNNECESSARY_UPDATE  1063
#define BJCA_ERROR_GET_ENVSN           1064
#define BJCA_ERROR_PARSE_CERTPACK      1065
#define BJCA_ERROR_LOGIN               1066
#define BJCA_ERROR_GET_CONNAME         1067
#define BJCA_ERROR_IMPORT_SIGNCERT     1068
#define BJCA_ERROR_IMPORT_ENCCERT      1069

#define BJCA_ERROR_GET_SERVER_CERT     1070
#define BJCA_ERROR_GET_CERT_INFO       1071
#define BJCA_ERROR_CERT_TYPE           1072
#define BJCA_ERROR_SM3                 1073
#define BJCA_ERROR_HASH_FILE           1074
#define BJCA_ERROR_HASH_DATA           1075
#define BJCA_ERROR_GET_PUBKEY          1076
#define BJCA_ERROR_GET_CERT_KEY_TYPE   1077
#define BJCA_ERROR_BASH64_ENCODE	   1078
#define BJCA_ERROR_BASH64_DECODE	   1079
#define BJCA_ERROR_ASN1				   1080
#define BJCA_ERROR_ALG_NO_SUPPORT	   1081
#define BJCA_ERROR_INVALID_ASN1_FORMAT 1082
#define BJCA_ERROR_DEC_SYM_KEY		   1083
#define BJCA_ERROR_GET_HASH_METHOD     1084

/************************************************************************/
/* API declare                                                          */
/************************************************************************/
#if defined(WIN32) || defined(_WINDOWS_)
/* XXX: equals to WINAPI */
#define SVSC_API __stdcall

#else /* !WIN32 */
#define SVSC_API
#endif

#if defined(UNIT_TEST)
#undef SVSC_API
#define SVSC_API
#endif



BJCA_INT32 SVSC_API BJCA_SVS_Init_Test(BJCA_HANDLE *pHandle, BJCA_CHAR_PTR config_file);

BJCA_INT32 SVSC_API BJCA_SVS_Final_Test(BJCA_HANDLE *pHandle);

BJCA_INT32 SVSC_API BJCA_SVS_Init(BJCA_HANDLE *pHandle, BJCA_CONFIGURE_PTR pConfigure);

BJCA_INT32 SVSC_API BJCA_SVS_Init_Default(BJCA_HANDLE *pHandle, BJCA_CHAR_PTR pConfigureFileName);

BJCA_INT32 SVSC_API BJCA_SVS_Init_ByServer(BJCA_HANDLE *pHandle, BJCA_CHAR_PTR pServerIP, BJCA_INT32 nServerPort);

BJCA_INT32 SVSC_API BJCA_SVS_InitConnection(BJCA_HANDLE *pHandle, BJCA_CHAR_PTR pServerIP, BJCA_INT32 nServerPort, BJCA_CHAR_PTR pAppName);
    
BJCA_INT32 SVSC_API BJCA_SVS_Final(BJCA_HANDLE *pHandle);

BJCA_INT32 SVSC_API BJCA_SVS_SignData(BJCA_HANDLE Handle, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszSignData, BJCA_ULONG *ulSignDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifySignData(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszSignData, BJCA_ULONG ulSignDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_SignDataByPKCS7(BJCA_HANDLE Handle, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszP7Data, BJCA_ULONG *ulP7DataLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifySignDataByPKCS7(BJCA_HANDLE Handle, BJCA_UCHAR *pszP7Data, BJCA_ULONG ulP7DataLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_SignXML(BJCA_HANDLE Handle,  BJCA_UCHAR *pszXML, BJCA_ULONG ulXMLLen, BJCA_UCHAR *pszSignDataXML, BJCA_ULONG *ulSignDataXMLLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifySignXML(BJCA_HANDLE Handle, BJCA_UCHAR *pszSignDataXML, BJCA_ULONG ulSignDataXMLLen);

BJCA_INT32 SVSC_API BJCA_SVS_SignFile(BJCA_HANDLE Handle, BJCA_UCHAR *pszFileName, BJCA_ULONG ulFileNameLen, BJCA_UCHAR *pszSignData, BJCA_ULONG *ulSignDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_SignFile_Ex(BJCA_HANDLE Handle, BJCA_UCHAR *pszFileName, BJCA_ULONG ulFileNameLen, BJCA_UCHAR *pszSignData, BJCA_ULONG *ulSignDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifySignFile(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_UCHAR *pszFileName, BJCA_ULONG ulFileNameLen, BJCA_UCHAR *pszSignData, BJCA_ULONG ulSignDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifySignFile_Ex(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_UCHAR *pszFileName, BJCA_ULONG ulFileNameLen, BJCA_UCHAR *pszSignData, BJCA_ULONG ulSignDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_GenRandom(BJCA_HANDLE Handle, BJCA_INT32 nRandomLen, BJCA_UCHAR *pszRandom, BJCA_ULONG *ulRandomLen);

BJCA_INT32 SVSC_API BJCA_SVS_GetServerCertificate(BJCA_HANDLE Handle, BJCA_UCHAR *pszServerCert, BJCA_ULONG *ulServerCertLen);

BJCA_INT32 SVSC_API BJCA_SVS_ValidateCertificate(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen);

BJCA_INT32 SVSC_API BJCA_SVS_GetCertificateInfo(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_INT32 nType, BJCA_UCHAR *pszInfo, BJCA_ULONG *ulInfoLen);

BJCA_INT32 SVSC_API BJCA_SVS_GetCertificateExtInfo(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_UCHAR *pszOID, BJCA_ULONG ulOIDLen, BJCA_UCHAR *pszInfo, BJCA_ULONG *ulInfoLen);

BJCA_INT32 SVSC_API BJCA_SVS_SymmEncrypt(BJCA_HANDLE Handle, BJCA_UCHAR *pszKey, BJCA_ULONG ulKeyLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszEncData, BJCA_ULONG *ulEncDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_SymmDecrypt(BJCA_HANDLE Handle, BJCA_UCHAR *pszKey, BJCA_ULONG ulKeyLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszDecData, BJCA_ULONG *ulDecDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_SymmEncryptFile(BJCA_HANDLE Handle, BJCA_UCHAR *pszKey, BJCA_ULONG ulKeyLen, BJCA_UCHAR *pszInFile, BJCA_ULONG ulInFileLen, BJCA_UCHAR *pszOutFile, BJCA_ULONG ulOutFileLen);

BJCA_INT32 SVSC_API BJCA_SVS_SymmDecryptFile(BJCA_HANDLE Handle, BJCA_UCHAR *pszKey, BJCA_ULONG ulKeyLen, BJCA_UCHAR *pszInFile, BJCA_ULONG ulInFileLen, BJCA_UCHAR *pszOutFile, BJCA_ULONG ulOutFileLen);

BJCA_INT32 SVSC_API BJCA_SVS_GetP7SignDataInfo(BJCA_HANDLE Handle,  BJCA_UCHAR *pszP7Data, BJCA_ULONG ulP7DataLen, BJCA_INT32 nFlag, BJCA_UCHAR *pszInfo, BJCA_ULONG *ulInfoLen);

BJCA_INT32 SVSC_API BJCA_SVS_GetXMLSignDataInfo(BJCA_HANDLE Handle, BJCA_UCHAR *pszSignDataXML, BJCA_ULONG ulSignDataXMLLen, BJCA_INT32 nFlag, BJCA_UCHAR *pszInfo, BJCA_ULONG *ulInfoLen);

BJCA_INT32 SVSC_API BJCA_SVS_PubKeyEncrypt(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszEncData, BJCA_ULONG *ulEncDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_PriKeyDecrypt(BJCA_HANDLE Handle, BJCA_UCHAR *pszEncData, BJCA_ULONG ulEncDataLen, BJCA_UCHAR *pszData, BJCA_ULONG *ulDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_Base64Encode(BJCA_HANDLE Handle, BJCA_UCHAR *pszInData, BJCA_ULONG ulInDataLen, BJCA_UCHAR *pszOutData, BJCA_ULONG *ulOutDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_Base64Decode(BJCA_HANDLE Handle, BJCA_UCHAR *pszInData, BJCA_ULONG ulInDataLen, BJCA_UCHAR *pszOutData, BJCA_ULONG *ulOutDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_SaveCertificate(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen);

BJCA_INT32 SVSC_API BJCA_SVS_QueryCertificate(BJCA_HANDLE Handle, BJCA_UCHAR *pszCertSN, BJCA_ULONG ulCertSNLen, BJCA_UCHAR *pszCert, BJCA_ULONG *ulCertLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifySignDataByCertSN(BJCA_HANDLE Handle, BJCA_UCHAR *pszCertSN, BJCA_ULONG ulCertSNLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszSignData, BJCA_ULONG ulSignDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_CheckCertificate(BJCA_HANDLE Handle, BJCA_UCHAR *pszCertSN, BJCA_ULONG ulCertSNLen);

BJCA_INT32 SVSC_API BJCA_SVS_ValidateAndSaveCertificate(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifySignDataByCertAndSN(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszSignData, BJCA_ULONG ulSignDataLen, BJCA_UCHAR *pszCertSN, BJCA_ULONG ulCertSNLen);

BJCA_INT32 SVSC_API BJCA_SVS_GetUpdateCertPack(BJCA_HANDLE Handle,  BJCA_UCHAR *psEnvsn, BJCA_ULONG ulEnvsnLen, BJCA_UCHAR *psCertAndKeyPairPack, BJCA_ULONG *ulCertAndKeyPairPackLen);

BJCA_INT32 SVSC_API BJCA_SVS_SymmEncryptByType(BJCA_HANDLE Handle, BJCA_UCHAR *pszKey, BJCA_ULONG ulKeyLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_INT32 nType, BJCA_UCHAR *pszEncData, BJCA_ULONG *ulEncDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_SymmDecryptByType(BJCA_HANDLE Handle, BJCA_UCHAR *pszKey, BJCA_ULONG ulKeyLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_INT32 nType, BJCA_UCHAR *pszDecData, BJCA_ULONG *ulDecDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_SignHashedDataByPKCS7Detach(BJCA_HANDLE Handle, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen,BJCA_UCHAR *pszP7Data, BJCA_ULONG *ulP7DataLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifySignHashedDataByPKCS7Detach(BJCA_HANDLE Handle, BJCA_UCHAR *pszP7Data, BJCA_ULONG ulP7DataLen,BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen);

//pszSignPack:signvalue#base64(sn#data)
BJCA_INT32 SVSC_API BJCA_SVS_SignDataReAllInfo(BJCA_HANDLE Handle, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszSignPack, BJCA_ULONG *ulSignPackLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifySignedDataByAllInfo(BJCA_HANDLE Handle, BJCA_UCHAR *pszSignPack, BJCA_ULONG ulSignPackLen);

BJCA_INT32 SVSC_API BJCA_SVS_EncodeEnvelopedData(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_UCHAR *pszData,
										          BJCA_ULONG ulDataLen, BJCA_UCHAR *pszEnvelopedData, BJCA_ULONG *ulEnvelopedDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_DecodeEnvelopedData(BJCA_HANDLE Handle, BJCA_UCHAR *pszEnvelopedData, BJCA_ULONG ulEnvelopedDataLen, BJCA_UCHAR *pszData, BJCA_ULONG *ulDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_GetActiveThreadNum (BJCA_HANDLE Handle);

//以下两个接口直接对hash值进行签名与验证，签名值不进行base64编码
BJCA_INT32 SVSC_API BJCA_SVS_SignHashedData(BJCA_HANDLE Handle, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszSignData, BJCA_ULONG *ulSignDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifySignatureByHashedData(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszSignData, BJCA_ULONG ulSignDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifySignDataAndCert(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszSignData, BJCA_ULONG ulSignDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_SignDataByPKCS7Detach(BJCA_HANDLE Handle, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszP7Data, BJCA_ULONG *ulP7DataLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifySignDataByPKCS7Detach(BJCA_HANDLE Handle, BJCA_UCHAR *pszP7Data, BJCA_ULONG ulP7DataLen,BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_GetPublicKeyByReq(BJCA_HANDLE sh, BJCA_UCHAR *pszReq, BJCA_ULONG ulReqLen, BJCA_UCHAR *pszPubKey, BJCA_ULONG *ulPubKeyLen);
//定制接口，验证不带证书的P7签名，返回签名证书
//BJCA_INT32 SVSC_API BJCA_SVS_VerifySignDataByPKCS7Detach(BJCA_HANDLE Handle,  BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen,  BJCA_UCHAR *pszP7Data, BJCA_ULONG ulP7DataLen, BJCA_UCHAR *pszCert, BJCA_ULONG *ulCertLen);

/* XXX: @deprecated implementation, use none-SHORT one */
BJCA_INT32 SVSC_API BJCA_SVS_SHORT_Init(BJCA_HANDLE *pHandle,BJCA_UCHAR *pszAppName, BJCA_ULONG ulAppNameLen,BJCA_UCHAR *pszServerIP, BJCA_ULONG ulServerIPLen, BJCA_UCHAR *pszServerPort, BJCA_ULONG ulServerPortLen);

BJCA_INT32 SVSC_API BJCA_SVS_SHORT_SignData(BJCA_HANDLE Handle, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszSignData, BJCA_ULONG *ulSignDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_SHORT_VerifySignData(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszSignData, BJCA_ULONG ulSignDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_SHORT_GetServerCertificate(BJCA_HANDLE Handle, BJCA_UCHAR *pszServerCert, BJCA_ULONG *ulServerCertLen);

BJCA_INT32 SVSC_API BJCA_SVS_SHORT_ValidateCertificate(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen);

BJCA_INT32 SVSC_API BJCA_SVS_SHORT_Final(BJCA_HANDLE *pHandle);

//人保行业版新增接口
BJCA_INT32 SVSC_API BJCA_SVS_HashData(BJCA_INT32 nHashAlg, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszHashValue, BJCA_ULONG *ulHashValueLen);

BJCA_INT32 SVSC_API BJCA_SVS_HashDataEx(BJCA_HANDLE Handle, BJCA_INT32 nHashAlg, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_UCHAR *pszHashValue, BJCA_ULONG *ulHashValueLen);
//snmp监控接口
BJCA_INT32 SVSC_API BJCA_SVS_GetServiceMasterProcessStatus(BJCA_HANDLE Handle);

#if !defined(WIN32) && !defined(_WINDOWS_)
BJCA_INT32 SVSC_API BJCA_SVS_GetServerCertificateDate(BJCA_UCHAR *pszCertDate, BJCA_ULONG *ulCertDateLen);
#endif

BJCA_INT32 SVSC_API BJCA_SVS_GetServerTime( BJCA_UCHAR *pszServerTime, BJCA_ULONG *ulServerTimeLen);

//大文件对称加密：对文件分段数据进行加密的接口
BJCA_INT32 SVSC_API BJCA_SVS_SymmEncryptLargeFile(BJCA_HANDLE sh, BJCA_UCHAR *pszKey, BJCA_ULONG ulKeyLen, BJCA_UCHAR *pszInFile, BJCA_ULONG ulInFileLen, BJCA_UCHAR *pszOutFile, BJCA_ULONG ulOutFileLen);

BJCA_INT32 SVSC_API BJCA_SVS_SymmDecryptLargeFile(BJCA_HANDLE sh, BJCA_UCHAR *pszKey, BJCA_ULONG ulKeyLen,BJCA_UCHAR *pszInFile, BJCA_ULONG ulInFileLen,BJCA_UCHAR *pszOutFile, BJCA_ULONG ulOutFileLen);

BJCA_INT32 SVSC_API BJCA_SVS_SymmEncryptForFileBlock(BJCA_HANDLE Handle, int pad ,BJCA_UCHAR *pszKey, BJCA_ULONG ulKeyLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszEncData, BJCA_ULONG *ulEncDataLen);

BJCA_INT32 SVSC_API BJCA_SVS_SymmDecryptForFileBlock(BJCA_HANDLE Handle, int pad ,BJCA_UCHAR *pszKey, BJCA_ULONG ulKeyLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszEncData, BJCA_ULONG *ulEncDataLen);

//华杰招投标项目新增接口
BJCA_INT32 SVSC_API BJCA_SVS_GenerateCert(BJCA_HANDLE Handle, BJCA_UCHAR *pszUid, BJCA_ULONG ulUidLen, BJCA_INT32 nKeyType, BJCA_INT32 nKeyLen, BJCA_UCHAR *pszCert, BJCA_ULONG *ulCertLen);

BJCA_INT32 SVSC_API BJCA_SVS_GetPubKey(BJCA_HANDLE Handle, BJCA_UCHAR *pszUid, BJCA_ULONG ulUidLen, BJCA_UCHAR *pszPubKey, BJCA_ULONG *ulPubKeyLen);

BJCA_INT32 SVSC_API BJCA_SVS_GetGenerateCert(BJCA_HANDLE Handle, BJCA_UCHAR *pszUid, BJCA_ULONG ulUidLen, BJCA_UCHAR *pszCert, BJCA_ULONG *ulCertLen);

BJCA_INT32 SVSC_API BJCA_SVS_DecodeP7Envelope(BJCA_HANDLE Handle, BJCA_UCHAR *pszUid, BJCA_ULONG ulUidLen, BJCA_UCHAR *pszEncIndata, BJCA_ULONG ulEncIndataLen, BJCA_UCHAR *pszDecOutdata, BJCA_ULONG *ulDecOutdataLen);

BJCA_INT32 SVSC_API BJCA_SVS_ASN1_Decode(BJCA_HANDLE Handle, BJCA_UCHAR *sig, BJCA_INT32 sig_len, ECCSignature * pEccSig_St);

BJCA_INT32 SVSC_API BJCA_SVS_ASN1_Encode(BJCA_HANDLE Handle, ECCSignature * pEccSig_St, BJCA_UCHAR *sig, BJCA_ULONG* sig_len);
//dsvs-ng高层控件接口
BJCA_INT32 SVSC_API BJCA_SVS_VerifyAndSaveDataExCToDB(BJCA_HANDLE Handle,
                                                      BJCA_UCHAR *pszRecordNumber, BJCA_ULONG ulRecordNumberLen,
                                                      BJCA_UCHAR *pszRecordRelatedNumber, BJCA_ULONG ulRecordRelatedNumberLen,
                                                      BJCA_UCHAR *pszSrcTitle, BJCA_ULONG ulSrcTitleLen,
                                                      BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, 
                                                      BJCA_UCHAR *pszSrcData, BJCA_ULONG ulSrcDataLen, 
                                                      BJCA_UCHAR *pszSignedData, BJCA_ULONG ulSignedDataLen,
                                                      BJCA_UCHAR *pszSrcHostName, BJCA_ULONG ulSrcHostNameLen,
                                                      BJCA_UCHAR *pszSrcHostId, BJCA_ULONG ulSrcHostIdLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifyAndSaveFileSignedExCToDB(BJCA_HANDLE Handle,
                                                            BJCA_UCHAR *pszRecordNumber, BJCA_ULONG ulRecordNumberLen,
                                                            BJCA_UCHAR *pszRecordRelatedNumber, BJCA_ULONG ulRecordRelatedNumberLen,
                                                            BJCA_UCHAR *pszDocumentId, BJCA_ULONG ulDocumentIdLen,
                                                            BJCA_UCHAR *pszSrcTitle, BJCA_ULONG ulSrcTitleLen,
                                                            BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, 
                                                            BJCA_UCHAR *pszFilePath, BJCA_ULONG ulFilePathLen, 
                                                            BJCA_UCHAR *pszFileSignedData, BJCA_ULONG ulFileSignedDataLen,
                                                            BJCA_UCHAR *pszSrcHostName, BJCA_ULONG ulSrcHostNameLen,
                                                            BJCA_UCHAR *pszSrcHostId, BJCA_ULONG ulSrcHostIdLen);

BJCA_INT32 SVSC_API BJCA_SVS_VerifyAndSaveFileSignedExCToDB_Ex(BJCA_HANDLE Handle,
                                                               BJCA_UCHAR *pszRecordNumber, BJCA_ULONG ulRecordNumberLen,
                                                               BJCA_UCHAR *pszRecordRelatedNumber, BJCA_ULONG ulRecordRelatedNumberLen,
                                                               BJCA_UCHAR *pszDocumentId, BJCA_ULONG ulDocumentIdLen,
                                                               BJCA_UCHAR *pszSrcTitle, BJCA_ULONG ulSrcTitleLen,
                                                               BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, 
                                                               BJCA_UCHAR *pszFilePath, BJCA_ULONG ulFilePathLen, 
                                                               BJCA_UCHAR *pszFileSignedData, BJCA_ULONG ulFileSignedDataLen,
                                                               BJCA_UCHAR *pszSrcHostName, BJCA_ULONG ulSrcHostNameLen,
                                                               BJCA_UCHAR *pszSrcHostId, BJCA_ULONG ulSrcHostIdLen);


BJCA_INT32 SVSC_API BJCA_SVS_GetHashMethod(BJCA_HANDLE Handle, BJCA_UCHAR *pszHashMethod, BJCA_ULONG *ulHashMethodLen);

BJCA_INT32 SVSC_API BJCA_SVS_EncodeEnvelopedFile(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, 
												 BJCA_UCHAR *pszInFilePath, BJCA_ULONG ulInFilePathLen,
												 BJCA_UCHAR *pszOutFilePath, BJCA_ULONG ulOutFilePathLen);

BJCA_INT32 SVSC_API BJCA_SVS_DecodeEnvelopedFile(BJCA_HANDLE Handle,
												 BJCA_UCHAR *pszInFilePath, BJCA_ULONG ulInFilePathLen,
												 BJCA_UCHAR *pszOutFilePath, BJCA_ULONG ulOutFilePathLen);

BJCA_INT32 SVSC_API BJCA_SVS_GetAllCertsInfo(BJCA_HANDLE Handle, BJCA_UCHAR *pszCertsInfo, BJCA_ULONG *ulCertsInfoLen);

#ifdef __cplusplus
}
#endif

#endif /* _BJCA_SVS_API_H_ */
