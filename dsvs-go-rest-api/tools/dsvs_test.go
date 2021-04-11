package tools

import (
	"bytes"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"github.com/cetcxinlian/cryptogm/sm2"
	"github.com/cetcxinlian/cryptogm/sm3"
	"github.com/cetcxinlian/cryptogm/x509"
	"io/ioutil"
	"log"
	"math/big"
	"net/http"
	"os"
	"testing"
	"time"
)

func TestSignHashedData(t *testing.T) {

	bigE := big.Int{}
	bigE.SetBytes([]byte{0x0a, 0x0b, 0x0c, 0xff})
	fmt.Println(bigE.Text(16))
	fmt.Println(string(bigE.Bytes()))
	fmt.Printf("%02x\n", bigE.Bytes())
	fmt.Println(hex.EncodeToString(bigE.Bytes()))

	fmt.Println("aaa")
	b, _ := hex.DecodeString("0a0b0cff")
	fmt.Println(string(b))
	fmt.Printf("%02x\n", b)
	fmt.Println(hex.EncodeToString(b))

	configFile := []byte("/home/hj/go/fabric-samples/bjca-sm2-dsvs-2.0.0-raft/dsvs/admin1.org1/BJCA_SVS_Config.ini")
	digest := sm3.SumSM3([]byte("hello world"))
	signData, err := SignHashedData(configFile, digest)
	if err != nil {
		fmt.Println("sign err")
	}
	fmt.Println(hex.EncodeToString(signData))
}

func TestDsvs(t *testing.T) {

	os.Setenv("DSVS_CONFIG_FILE", "/home/hj/dsvs/BJCA_SVS_Config.ini")
	os.Setenv("DSVS_LIB_FILE", "/home/hj/dsvs/libsvscc.so")

	configFile := []byte(os.Getenv("DSVS_CONFIG_FILE"))

	serverCert, err := GetServerCert(configFile)
	if err != nil {
		fmt.Println("get server cert error")
		return
	}
	fmt.Printf("%s\n", serverCert)
	fmt.Println(len(serverCert))

	msg := []byte("hello world")
	digest := sm3.SumSM3(msg)
	signData, err := SignHashedData(configFile, digest)
	if err != nil {
		fmt.Println("sign failed")
		return
	}
	fmt.Printf("sig:%2x\n", signData)
	fmt.Println(len(signData))
	r, s, _ := UnmarshalSM2Signature(signData)
	//fmt.Printf("r:%x\n", r)
	//fmt.Printf("s:%x\n", s)
	R, S, _ := UnmarshalSM2Signature(signData)
	fmt.Printf("R:%2x\n", R.Bytes())
	fmt.Printf("S:%2x\n", S.Bytes())

	digest = sm3.SumSM3(msg)
	isOk, err := VerifyByHashedData(configFile, serverCert, digest, signData)
	if err != nil {
		fmt.Println("verify failed")
	}
	fmt.Println(isOk)

	x, y, _ := GetPubKeyFromX509CertPEM(serverCert)
	fmt.Printf("%2x\n", x)
	fmt.Printf("%2x\n", y)

	Px := big.Int{}
	Py := big.Int{}
	Px.SetBytes(x)
	Py.SetBytes(y)
	sm2PK := sm2.PublicKey{Curve: sm2.P256Sm2(), X: &Px, Y: &Py}
	//ok := sm2.VerifyById(&sm2PK, msg, []byte("1234567812345678"), r, s)
	ok := sm2.VerifyWithDigest(&sm2PK, digest, r, s)
	fmt.Println(ok)

}

func TestSignature(t *testing.T) {

	//os.Setenv("DSVS_CONFIG_FILE", "/home/hj/go/fabric-samples/bjca-sm2-dsvs-2.0.0/dsvs/peer0.org1/BJCA_SVS_Config.ini")
	//os.Setenv("DSVS_LIB_FILE", "/home/hj/go/fabric-samples/bjca-sm2-dsvs-2.0.0/dsvs/libsvscc.so")
	os.Setenv("DSVS_LIB_FILE", "/home/hj/dsvs/libsvscc.so")

	sig := big.Int{}
	digest := big.Int{}
	configFile := "/home/hj/workspace/go/rest-api/config/admin1.org1/BJCA_SVS_Config.ini"

	sig.SetString("304502201dcf381feb4a526f9dfed807b560ec4e8aefef5bea8be10998d295d22e2ee6d8022100b0ce76ad4b135ad788ec1f9a52a4d6b909d76cb8f3e67de4aa49797fe50c11c3", 16)
	digest.SetString("f447d1c28c85a7391ab5bd6c38f5cc2d8231a28feb8853ab573f47d6a3502778", 16)
	cert, _ := GetServerCert([]byte(configFile))
	fmt.Println(string(cert))

	ok, _ := VerifyByHashedData([]byte(configFile), cert, digest.Bytes(), sig.Bytes())
	fmt.Println(ok)

	r, s, _ := UnmarshalSM2Signature(sig.Bytes())

	x, y, _ := GetPubKeyFromX509CertPEM(cert)
	Px := big.Int{}
	Py := big.Int{}
	Px.SetBytes(x)
	Py.SetBytes(y)
	sm2PK := sm2.PublicKey{Curve: sm2.P256Sm2(), X: &Px, Y: &Py}

	fmt.Println("px:" + hex.EncodeToString(sm2PK.X.Bytes()))
	fmt.Println("py:" + hex.EncodeToString(sm2PK.Y.Bytes()))
	fmt.Println("digest:" + hex.EncodeToString(digest.Bytes()))
	fmt.Println("r:" + hex.EncodeToString(r.Bytes()))
	fmt.Println("s:" + hex.EncodeToString(s.Bytes()))
	ok = sm2.VerifyWithDigest(&sm2PK, digest.Bytes(), r, s)
	fmt.Println(ok)

}

func Test(t *testing.T) {

	admin1CertPEM := `-----BEGIN CERTIFICATE-----
MIIEdDCCBBqgAwIBAgIKGhAAAAAAAAsS1DAKBggqgRzPVQGDdTBEMQswCQYDVQQG
EwJDTjENMAsGA1UECgwEQkpDQTENMAsGA1UECwwEQkpDQTEXMBUGA1UEAwwOQmVp
amluZyBTTTIgQ0EwHhcNMjEwNDA1MTYwMDAwWhcNMjIwNDA2MTU1OTU5WjCBoDES
MBAGA1UEKQwJMjAyMTA0MDEwMR0wGwYDVQQDDBRhZG1pbjEub3JnMS5iamNhLmNv
bTEOMAwGA1UECwwFYWRtaW4xJjAkBgNVBAoMHUJFSUpJTkcgQ0VSVElGSUNBVEUg
QVVUSE9SSVRZMRIwEAYDVQQHDAnlj4zpuK3lsbExEjAQBgNVBAgMCem7kem+meax
nzELMAkGA1UEBgwCQ04wWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAAQ32DMGWwyC
wYoUt9b16qEij0JcTN9SZU7aWYJvBCyb8d+LrCO3VE5+2X0NrCvYWWrPuY+o0QyN
dsPn0wbHkixLo4IClTCCApEwHwYDVR0jBBgwFoAUH+bP1I/FIiqXSimKFecWyZI0
xLYwHQYDVR0OBBYEFH7Cus5sYBcMToTvRRxbmlFJBFaYMAsGA1UdDwQEAwIGwDCB
ogYDVR0fBIGaMIGXMGCgXqBcpFowWDELMAkGA1UEBhMCQ04xDTALBgNVBAoMBEJK
Q0ExDTALBgNVBAsMBEJKQ0ExFzAVBgNVBAMMDkJlaWppbmcgU00yIENBMRIwEAYD
VQQDEwljYTIxY3JsMjkwM6AxoC+GLWh0dHA6Ly8xMTEuMjA3LjE3Ny4xODk6ODAw
My9jcmwvY2EyMWNybDI5LmNybDAbBgoqgRyG7zICAQEBBA0MC0pKMjAyMTA0MDEw
MGAGCCsGAQUFBwEBBFQwUjAjBggrBgEFBQcwAYYXT0NTUDovL29jc3AuYmpjYS5v
cmcuY24wKwYIKwYBBQUHMAKGH2h0dHA6Ly9jcmwuYmpjYS5vcmcuY24vY2Fpc3N1
ZXIwQAYDVR0gBDkwNzA1BgkqgRyG7zICAgEwKDAmBggrBgEFBQcCARYaaHR0cDov
L3d3dy5iamNhLm9yZy5jbi9jcHMwEQYJYIZIAYb4QgEBBAQDAgD/MBkGCiqBHIbv
MgIBAQgECwwJMjAyMTA0MDEwMBsGCiqBHIbvMgIBAgIEDQwLSkoyMDIxMDQwMTAw
HwYKKoEchu8yAgEBDgQRDA85OTgwMDAxMDAzNTc2NzkwGwYKKoEchu8yAgEBBAQN
DAtKSjIwMjEwNDAxMDAkBgoqgRyG7zICAQEXBBYMFDFAMjE1MDA5SkowMjAyMTA0
MDEwMBcGCCqBHNAUBAEEBAsMCTIwMjEwNDAxMDAUBgoqgRyG7zICAQEeBAYMBDI5
NTUwCgYIKoEcz1UBg3UDSAAwRQIgOoZ4dJL11d45vVoyUzGzUIPsW2/Fauzld1Ur
AjwA3uACIQC+M5OgSzunnjz/8tqr1t+DNevFsJvL/VBA2c4SLdoyfA==
-----END CERTIFICATE-----`

	x, y, _ := GetPubKeyFromX509CertPEM([]byte(admin1CertPEM))
	Px := big.Int{}
	Py := big.Int{}
	Px.SetBytes(x)
	Py.SetBytes(y)
	P := sm2.PublicKey{Curve: sm2.P256Sm2(), X: &Px, Y: &Py}

	b, _ := hex.DecodeString("8e235fc38793a6e080b6d971d29778658ce8a5d3af5fe7e82698ec0dac891e34")
	fmt.Println(hex.EncodeToString(sm3.SumSM3(b)))

	pk, _ := hex.DecodeString("3059301306072a8648ce3d020106082a811ccf5501822d0342000437d833065b0c82c18a14b7d6f5eaa1228f425c4cdf52654eda59826f042c9bf1df8bac23b7544e7ed97d0dac2bd8596acfb98fa8d10c8d76c3e7d306c7922c4b")
	key, _ := x509.ParsePKIXPublicKey(pk)
	sm2Pk, ok := key.(*sm2.PublicKey)
	if !ok {
		fmt.Println("parse pk err")
		return
	}

	hash, _ := hex.DecodeString("8e235fc38793a6e080b6d971d29778658ce8a5d3af5fe7e82698ec0dac891e34")
	sig, _ := hex.DecodeString("304502201dcf381feb4a526f9dfed807b560ec4e8aefef5bea8be10998d295d22e2ee6d8022100b0ce76ad4b135ad788ec1f9a52a4d6b909d76cb8f3e67de4aa49797fe50c11c3")
	r, s, err := UnmarshalSM2Signature(sig)
	if err != nil {
		fmt.Println("parse sig err")
		return
	}

	fmt.Println("px:" + hex.EncodeToString(sm2Pk.X.Bytes()))
	fmt.Println("py:" + hex.EncodeToString(sm2Pk.Y.Bytes()))
	fmt.Println("digest:" + hex.EncodeToString(hash))
	fmt.Println("r:" + hex.EncodeToString(r.Bytes()))
	fmt.Println("s:" + hex.EncodeToString(s.Bytes()))
	fmt.Println(sm2.Verify(sm2Pk, hash, r, s))
	fmt.Println(sm2.Verify(&P, hash, r, s))

	/*

	f447d1c28c85a7391ab5bd6c38f5cc2d8231a28feb8853ab573f47d6a3502778
	px:37d833065b0c82c18a14b7d6f5eaa1228f425c4cdf52654eda59826f042c9bf1
	py:df8bac23b7544e7ed97d0dac2bd8596acfb98fa8d10c8d76c3e7d306c7922c4b
	digest:8e235fc38793a6e080b6d971d29778658ce8a5d3af5fe7e82698ec0dac891e34
	r:1dcf381feb4a526f9dfed807b560ec4e8aefef5bea8be10998d295d22e2ee6d8
	s:b0ce76ad4b135ad788ec1f9a52a4d6b909d76cb8f3e67de4aa49797fe50c11c3

	px:37d833065b0c82c18a14b7d6f5eaa1228f425c4cdf52654eda59826f042c9bf1
	py:df8bac23b7544e7ed97d0dac2bd8596acfb98fa8d10c8d76c3e7d306c7922c4b
	digest:f447d1c28c85a7391ab5bd6c38f5cc2d8231a28feb8853ab573f47d6a3502778
	r:1dcf381feb4a526f9dfed807b560ec4e8aefef5bea8be10998d295d22e2ee6d8
	s:b0ce76ad4b135ad788ec1f9a52a4d6b909d76cb8f3e67de4aa49797fe50c11c3

	 */

}

type ReqData struct {
	Data	string	`json:"data"`
	AppName	string	`json:"app_name"`
}
type Response struct {
	Code	int		`json:"code"`
	Msg 	string	`json:"Msg"`
	Req		ReqData	`json:"req"`
	Sig		string	`json:"sig"`
}

func TestSignData(t *testing.T) {
	os.Setenv("DSVS_CONFIG_FILE", "/home/hj/dsvs/BJCA_SVS_Config.ini")
	os.Setenv("DSVS_LIB_FILE", "/home/hj/dsvs/libsvscc.so")

	configFile := []byte(os.Getenv("DSVS_CONFIG_FILE"))
	data, _ := hex.DecodeString("8e235fc38793a6e080b6d971d29778658ce8a5d3af5fe7e82698ec0dac891e34")

	sig, err := SignData(configFile, data)
	if err != nil {
		log.Fatal("sign data err")
	}


	pk, _ := hex.DecodeString("3059301306072a8648ce3d020106082a811ccf5501822d0342000437d833065b0c82c18a14b7d6f5eaa1228f425c4cdf52654eda59826f042c9bf1df8bac23b7544e7ed97d0dac2bd8596acfb98fa8d10c8d76c3e7d306c7922c4b")
	key, _ := x509.ParsePKIXPublicKey(pk)
	sm2Pk, ok := key.(*sm2.PublicKey)
	if !ok {
		fmt.Println("parse pk err")
		return
	}
	r, s, err := UnmarshalSM2Signature(sig)
	if err != nil {
		fmt.Println("parse sig err")
		return
	}

	fmt.Println(sm2.Verify(sm2Pk, data, r, s))


	client := &http.Client{Timeout: 5 * time.Second}
	jsonStr := []byte("{\"data\": \"8e235fc38793a6e080b6d971d29778658ce8a5d3af5fe7e82698ec0dac891e34\",\"app_name\": \"Admin1Org1\"}")
	url := "http://localhost:10086/sign_sm2"
	contentType := "application/json"
	resp, err := client.Post(url, contentType, bytes.NewBuffer(jsonStr))
	if err != nil {
		panic(err)
	}
	defer resp.Body.Close()

	result, _ := ioutil.ReadAll(resp.Body)

	res := &Response{}
	err = json.Unmarshal(result, res)
	if err != nil {
		panic("unmarshal result err")
	}

	bigSig, _ := new(big.Int).SetString(res.Sig, 16)
	r, s, err = UnmarshalSM2Signature(bigSig.Bytes())
	if err != nil {
		fmt.Println("parse sig err")
		return
	}
	fmt.Println(sm2.Verify(sm2Pk, data, r, s))

}

