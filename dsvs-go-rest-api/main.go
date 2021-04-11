package main

import (
	"github.com/gin-gonic/gin"
	"os"
	"restApi/router"
)



func main() {
	os.Setenv("DSVS_LIB_FILE", "/home/hj/workspace/go/rest-api/lib/libsvscc.so")
	port := os.Getenv("DSVS_REST_API_PORT")
	r := gin.Default()
	r.POST("/sign_sm2", router.SignSm2)
	r.Run(":" + port)
}

/*

curl -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"hashed_data": "xxx","app_name": "xxxx"}'  http://localhost:10086/sign_sm2

 */
