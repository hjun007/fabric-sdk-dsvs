package router

import (
	"encoding/hex"
	"github.com/gin-gonic/gin"
	"log"
	"math/big"
	"net/http"
	"restApi/tools"
)

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

func SignSm2(c *gin.Context) {
	reqJson := &ReqData{}
	c.BindJSON(reqJson)
	log.Printf("%+v\n", reqJson)

	var configFile []byte
	if reqJson.AppName == "Admin1Org1" {
		configFile = []byte("/home/hj/workspace/go/rest-api/config/admin1.org1/BJCA_SVS_Config.ini")
	} else if reqJson.AppName == "Admin1Org2" {
		configFile = []byte("/home/hj/workspace/go/rest-api/config/admin1.org2/BJCA_SVS_Config.ini")
	} else {
		c.JSON(http.StatusBadRequest, &Response{
			Code: http.StatusBadRequest,
			Msg: "app name err",
			Req: *reqJson,
			Sig: "",
		})
		return
	}

	bigData, _ := new(big.Int).SetString(reqJson.Data, 16)
	sig, err := tools.SignData(configFile, bigData.Bytes())
	if err != nil {
		c.JSON(http.StatusInternalServerError, &Response{
			Code: http.StatusInternalServerError,
			Msg: "sign err: " + err.Error(),
			Req: *reqJson,
			Sig: "",
		})
		return
	}

	c.JSON(http.StatusOK, &Response{
		Code: http.StatusOK,
		Msg: "all good",
		Req: *reqJson,
		Sig: hex.EncodeToString(sig),
	})
	return
}
