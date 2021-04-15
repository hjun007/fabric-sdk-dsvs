package tools

//
// 引用的C头文件需要在注释中声明，紧接着注释需要有import "C"，且这一行和注释之间不能有空格
// libsvscc.so must in /usr/lib/ directory
//

/*
//包含header的目录
#cgo CFLAGS: -I .
//动态库编译方式
//-L/dsvs/  指定库目录
//-lsvscc	指定库名称libsvscc.so
#cgo LDFLAGS: -lsvscc -ldl
//#include "dsvs.h"
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <dlfcn.h>
#include <unistd.h>
#include <string.h>
#include "BJCA_SVS_API.h"


int get_server_cert(
	unsigned char* config_file,
	unsigned char* server_cert,
	unsigned long* server_cert_len) {

	typedef BJCA_INT32(SVSC_API* pBJCA_SVS_Init_Default)(BJCA_HANDLE* pHandle, BJCA_CHAR_PTR pConfigureFileName);
	typedef BJCA_INT32(SVSC_API* pBJCA_SVS_GetServerCertificate)(BJCA_HANDLE Handle, BJCA_UCHAR* pszServerCert, BJCA_ULONG* ulServerCertLen);
	typedef BJCA_INT32(SVSC_API* pBJCA_SVS_Final)(BJCA_HANDLE* pHandle);

	char *dsvs_lib_file = getenv("DSVS_LIB_FILE");
	printf("DSVS_LIB_FILE=%s\n", dsvs_lib_file);

	void *module;
	module = dlopen(dsvs_lib_file, RTLD_LAZY);

	pBJCA_SVS_Init_Default BJCA_SVS_Init_Default = dlsym(module, "BJCA_SVS_Init_Default");
	pBJCA_SVS_GetServerCertificate BJCA_SVS_GetServerCertificate = dlsym(module, "BJCA_SVS_GetServerCertificate");
	pBJCA_SVS_Final BJCA_SVS_Final = dlsym(module, "BJCA_SVS_Final");

	BJCA_INT32 rv;
	BJCA_HANDLE sh;
	int fd;
	BJCA_UCHAR serv_cert[4096];
	BJCA_ULONG serv_cert_len;

	if (fd = access(config_file, F_OK) == 0) {
		rv = BJCA_SVS_Init_Default(&sh, config_file);
	}
	else {
		printf("无法找到配置文件");
		return -1;
	}
	if (rv != 0) {
		printf("初始化失败");
		return -1;
	}

	//调用应用接口
	serv_cert_len = sizeof(serv_cert);
	rv = BJCA_SVS_GetServerCertificate(sh, serv_cert, &serv_cert_len);
	if (rv != 0) {
		printf("获取服务器证书失败!\n");
		BJCA_SVS_Final(&sh);
		return -1;
	}
	//printf("获取服务器证书成功!\n");
	for (BJCA_ULONG i = 0; i < serv_cert_len; i++) {
		server_cert[i] = serv_cert[i];
		//printf("%c", serv_cert[i]);
	}
	*server_cert_len = serv_cert_len;

	return 0;
}

int sign_hashed_data(
	unsigned char* 	config_file,
	unsigned char* 	data,
	unsigned long 	data_len,
	unsigned char* 	sign_data,
	unsigned long* 	sign_data_len) {

	typedef BJCA_INT32(SVSC_API* pBJCA_SVS_Init_Default)(BJCA_HANDLE* pHandle, BJCA_CHAR_PTR pConfigureFileName);
	typedef BJCA_INT32(SVSC_API* pBJCA_SVS_SignHashedData)(BJCA_HANDLE Handle, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszSignData, BJCA_ULONG *ulSignDataLen);
	typedef BJCA_INT32(SVSC_API* pBJCA_SVS_Final)(BJCA_HANDLE* pHandle);

	void *module;
	char *dsvs_lib_file = getenv("DSVS_LIB_FILE");
	module = dlopen(dsvs_lib_file, RTLD_LAZY);

	pBJCA_SVS_Init_Default BJCA_SVS_Init_Default = dlsym(module, "BJCA_SVS_Init_Default");
	pBJCA_SVS_SignHashedData BJCA_SVS_SignHashedData = dlsym(module, "BJCA_SVS_SignHashedData");
	pBJCA_SVS_Final BJCA_SVS_Final = dlsym(module, "BJCA_SVS_Final");

	BJCA_INT32 rv;
	BJCA_HANDLE sh;
	int fd;
	BJCA_UCHAR signed_data[4096] = { 0 };
	BJCA_ULONG signed_data_len;
	signed_data_len = sizeof(signed_data);

	if (fd = access(config_file, F_OK) == 0) {
		rv = BJCA_SVS_Init_Default(&sh, config_file);
	}
	else {
		printf("无法找到配置文件");
		return -1;
	}
	if (rv != 0) {
		printf("初始化失败");
		return -1;
	}

	rv = BJCA_SVS_SignHashedData(sh, data, data_len, signed_data, &signed_data_len);
	if (rv != 0) {
		printf("签名失败!\n");
		BJCA_SVS_Final(&sh);
		return -1;
	}
	//printf("签名成功!\n");
	for (BJCA_ULONG i = 0; i < signed_data_len; i++) {
		sign_data[i] = signed_data[i];
		//printf("%c", signed_data[i]);
	}
	*sign_data_len = signed_data_len;

	return 0;
}

int verify_by_hashed_data(
	unsigned char* 	config_file,
	unsigned char* 	cert,
	unsigned long 	cert_len,
	unsigned char* 	data,
	unsigned long 	data_len,
	unsigned char* 	sign_data,
	unsigned long 	sign_data_len) {

	typedef BJCA_INT32(SVSC_API* pBJCA_SVS_Init_Default)(BJCA_HANDLE* pHandle, BJCA_CHAR_PTR pConfigureFileName);
	typedef BJCA_INT32(SVSC_API* pBJCA_SVS_VerifySignatureByHashedData)(BJCA_HANDLE Handle, BJCA_UCHAR *pszCert, BJCA_ULONG ulCertLen, BJCA_UCHAR *pszData, BJCA_ULONG ulDataLen, BJCA_UCHAR *pszSignData, BJCA_ULONG ulSignDataLen);
	typedef BJCA_INT32(SVSC_API* pBJCA_SVS_Final)(BJCA_HANDLE* pHandle);

	void *module;
	char *dsvs_lib_file = getenv("DSVS_LIB_FILE");
	module = dlopen(dsvs_lib_file, RTLD_LAZY);

	pBJCA_SVS_Init_Default BJCA_SVS_Init_Default = dlsym(module, "BJCA_SVS_Init_Default");
	pBJCA_SVS_VerifySignatureByHashedData BJCA_SVS_VerifySignatureByHashedData = dlsym(module, "BJCA_SVS_VerifySignatureByHashedData");
	pBJCA_SVS_Final BJCA_SVS_Final = dlsym(module, "BJCA_SVS_Final");

	BJCA_INT32 rv;
	BJCA_HANDLE sh;
	int fd;

	if (fd = access(config_file, F_OK) == 0) {
		rv = BJCA_SVS_Init_Default(&sh, config_file);
	}
	else {
		printf("无法找到配置文件");
		return -1;
	}
	if (rv != 0) {
		printf("初始化失败");
		return -1;
	}

	rv = BJCA_SVS_VerifySignatureByHashedData(sh, cert, cert_len, data, data_len, sign_data, sign_data_len);
	if (rv == 0) {
		//printf("签名验证成功!\n");
		BJCA_SVS_Final(&sh);
		return 0;
	}
	else {
		printf("签名验证失败!\n");
		BJCA_SVS_Final(&sh);
		return  -1;
	}

	return 0;

}
*/
import "C"
import (
	"encoding/asn1"
	"encoding/hex"
	"encoding/pem"
	"errors"
	"fmt"
	"github.com/cetcxinlian/cryptogm/sm2"
	"github.com/cetcxinlian/cryptogm/sm3"
	"github.com/cetcxinlian/cryptogm/x509"
	"log"
	"math/big"
	"unsafe"
)

func GetServerCert(configFile []byte) ([]byte, error) {

	fmt.Printf("GetServerCert.go: %s\n", configFile)
	var rv = C.int(1)
	var serverCert = make([]byte, 4096)
	var serverCertLen C.ulong
	var pServerCert = (*C.uchar)(&serverCert[0])
	var pConfigFile = (*C.uchar)(&configFile[0])
	rv = C.get_server_cert(pConfigFile, pServerCert, &serverCertLen)
	if rv != 0 {
		return nil, errors.New("get server cert failed")
	}
	return C.GoBytes(unsafe.Pointer(pServerCert), C.int(serverCertLen)), nil

}

func SignHashedData(configFile, data []byte) ([]byte, error) {

	if data == nil {
		return nil, errors.New("data cannot be nil")
	}

	if len(data) == 0 {
		return nil, errors.New("data cannot be empty")
	}

	var rv = C.int(1)
	var pData = (*C.uchar)(&data[0])
	var dataLen C.ulong
	var signData = make([]byte, 4096)
	var pSignData = (*C.uchar)(&signData[0])
	var pConfigFile = (*C.uchar)(&configFile[0])
	var signDataLen C.ulong

	dataLen = C.ulong(len(data))
	signDataLen = C.ulong(len(signData))

	rv = C.sign_hashed_data(pConfigFile, pData, dataLen, pSignData, &signDataLen)
	if rv != 0 {
		return nil, errors.New("sign hashed data failed")
	}
	return C.GoBytes(unsafe.Pointer(pSignData), C.int(signDataLen)), nil

}

func VerifyByHashedData(configFile, serverCert, data, signData []byte) (bool, error) {

	if serverCert == nil || data == nil || signData == nil {
		return false, errors.New("parameters cannot be nil")
	}

	if len(serverCert) == 0 || len(data) == 0 || len(signData) == 0 {
		return false, errors.New("parameters cannot be empty")
	}

	var rv = C.int(1)
	var pServerCert = (*C.uchar)(&serverCert[0])
	var pConfigFile = (*C.uchar)(&configFile[0])
	var pData = (*C.uchar)(&data[0])
	var pSignData = (*C.uchar)(&signData[0])
	rv = C.verify_by_hashed_data(pConfigFile, pServerCert, C.ulong(len(serverCert)), pData, C.ulong(len(data)), pSignData, C.ulong(len(signData)))
	if rv != 0 {
		return false, errors.New("verify by hashed data failed")
	}
	return true, nil
}

func GetPubKeyFromX509CertPEM(certPEM []byte) ([]byte, []byte, error) {
	tmpCert := []byte("-----BEGIN CERTIFICATE-----\n")
	tmpCert = append(tmpCert, certPEM...)
	tmpCert = append(tmpCert, []byte("\n-----END CERTIFICATE-----")...)

	block, _ := pem.Decode(tmpCert)
	if block == nil {
		fmt.Println("pem.Decode err")
		return nil, nil, errors.New("pem.Decode err")
	}

	cert, err := x509.ParseCertificate(block.Bytes)
	if err != nil {
		return nil, nil, errors.New("x509 parse cert bytes failed")
	}
	sm2PK, ok := cert.PublicKey.(*sm2.PublicKey)
	if !ok {
		return nil, nil, errors.New("cert.PublicKey transfer to sm2.PublicKey failed")
	}

	return sm2PK.X.Bytes(), sm2PK.Y.Bytes(), nil
}

type SM2Signature struct {
	R, S *big.Int
}

func MarshalSM2Signature(r, s *big.Int) ([]byte, error) {
	return asn1.Marshal(SM2Signature{r, s})
}

func UnmarshalSM2Signature(raw []byte) (*big.Int, *big.Int, error) {
	// Unmarshal
	sig := new(SM2Signature)
	_, err := asn1.Unmarshal(raw, sig)
	if err != nil {
		return nil, nil, fmt.Errorf("Failed unmashalling signature [%s]", err)
	}

	// Validate sig
	if sig.R == nil {
		return nil, nil, errors.New("Invalid signature. R must be different from nil.")
	}
	if sig.S == nil {
		return nil, nil, errors.New("Invalid signature. S must be different from nil.")
	}

	if sig.R.Sign() != 1 {
		return nil, nil, errors.New("Invalid signature. R must be larger than zero")
	}
	if sig.S.Sign() != 1 {
		return nil, nil, errors.New("Invalid signature. S must be larger than zero")
	}

	return sig.R, sig.S, nil
}

func zeroByteSlice() []byte{
	return []byte{0,0,0,0,
		0,0,0,0,
		0,0,0,0,
		0,0,0,0,
		0,0,0,0,
		0,0,0,0,
		0,0,0,0,
		0,0,0,0,
	}
}

func getZById(pub *sm2.PublicKey, id []byte) []byte{
	var lena = uint16(len(id) * 8) //bit len of IDA
	var ENTLa = []byte{byte(lena>>8), byte(lena)}
	var z = make([]byte, 0, 1024)

	//判断公钥x,y坐标长度是否小于32字节，若小于则在前面补0
	xBuf := pub.X.Bytes()
	yBuf := pub.Y.Bytes()
	if n := len(xBuf); n < 32 {
		xBuf = append(zeroByteSlice()[:32-n], xBuf...)
	}

	if n := len(yBuf); n < 32 {
		yBuf = append(zeroByteSlice()[:32-n], yBuf...)
	}

	z = append(z, ENTLa...)
	z = append(z, id...)
	z = append(z, sm2.SM2PARAM_A.Bytes()...)
	z = append(z, sm2.P256Sm2().Params().B.Bytes()...)
	z = append(z, sm2.P256Sm2().Params().Gx.Bytes()...)
	z = append(z, sm2.P256Sm2().Params().Gy.Bytes()...)
	z = append(z, xBuf...)
	z = append(z, yBuf...)
	return sm3.SumSM3(z)
}

//Za = sm3(ENTL||IDa||a||b||Gx||Gy||Xa||Xy)
func getZ(pub *sm2.PublicKey) []byte {
	return getZById(pub, []byte("1234567812345678"))
}

func SignData(configFile, data []byte) ([]byte, error) {

	log.Println("data:"+hex.EncodeToString(data))

	if data == nil {
		return nil, errors.New("data cannot be nil")
	}

	if len(data) == 0 {
		return nil, errors.New("data cannot be empty")
	}

	certBytes, err := GetServerCert(configFile)
	if err != nil {
		return nil, errors.New("get server cert err")
	}
	px, py, err := GetPubKeyFromX509CertPEM(certBytes)
	if err != nil {
		return nil, errors.New("get pubkey key from cert err")
	}

	pubkey := &sm2.PublicKey{
		Curve: sm2.P256Sm2(),
		X:     new(big.Int).SetBytes(px),
		Y:     new(big.Int).SetBytes(py),
	}

	var m = make([]byte, 32+len(data))
	copy(m, getZ(pubkey))
	copy(m[32:], data)

	e := new(big.Int).SetBytes(sm3.SumSM3(m))

	sig, err := SignHashedData(configFile, e.Bytes())
	if err != nil {
		return nil, err
	}
	return sig, nil

}

