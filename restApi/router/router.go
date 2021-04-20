package router

import (
	"encoding/hex"
	"fmt"
	"github.com/Unknwon/goconfig"
	"github.com/gin-gonic/gin"
	"log"
	"math/big"
	"net/http"
	"restApi/tools"
)

var Config *goconfig.ConfigFile

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

	dsvsConfigPath, err := Config.GetValue("dsvs", "dsvs_config_path")
	fmt.Println(dsvsConfigPath)
	if err != nil {
		panic("config.ini property dsvs.dsvs_config_path not found"+ err.Error())
	}
	var configFile []byte
	if reqJson.AppName == "Admin1Org1" || reqJson.AppName == "Admin1Org2"{
		configFile = []byte(dsvsConfigPath + "/" + reqJson.AppName + "/BJCA_SVS_Config.ini")
	} else {
		c.JSON(http.StatusBadRequest, &Response{
			Code: http.StatusBadRequest,
			Msg: "app name err",
			Req: *reqJson,
			Sig: "",
		})
		return
	}

	fmt.Println(reqJson.Data)

	bigData, ok := new(big.Int).SetString(reqJson.Data, 16)
	if !ok {
		fmt.Println("reqJson.Data set to big.Int err")
	}
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
