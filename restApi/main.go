package main

import (
	"github.com/Unknwon/goconfig"
	"github.com/gin-gonic/gin"
	"log"
	"os"
	"restApi/router"
)

func main() {

	log.Println(os.Getenv("PWD"))

	var err error
	router.Config, err = goconfig.LoadConfigFile("./config.ini")
	if err != nil {
		panic(err.Error())
	}
	port, err := router.Config.GetValue("self", "port")
	if err != nil {
		panic(err.Error())
	}
	dsvsLibFile, err := router.Config.GetValue("dsvs", "dsvs_lib_file")
	if err != nil {
		panic(err.Error())
	}
	os.Setenv("DSVS_LIB_FILE", dsvsLibFile)

	log.Println(port)
	log.Println(os.Getenv("DSVS_LIB_FILE"))

	r := gin.Default()
	r.POST("/sign_sm2", router.SignSm2)
	r.Run(":" + port)
}

/*

curl -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"data": "xxx","app_name": "Admin1Org1"}'  http://localhost:10086/sign_sm2

 */
