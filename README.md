# 运行dsvs rest api服务
Go语言封装dsvs接口，做成REST API
```
cd restApi
go build main.go
./main
```

# 启动fabric容器
这是一个测试网络，镜像使用fabric-2.0-gm，peer和orderer使用北京ca国密证书，节点的私钥按照msp文件夹放置，两个组织的两个管理员使用dsvs签名
```
cd gm-docker-with-bjca-1
./fabric.sh down
./fabric.sh up
```

# 测试sdk
设置环境变量DSVS_REST_API_URL=http://localhost:10086/sign_sm2，ip根据go rest api服务的ip调整
运行fabric-sdk-java/src/test/java/org/hyperledger/fabric/sdkintegration/End2endLifecycleIT.java

# 测试query和invoke
设置环境变量DSVS_REST_API_URL=http://localhost:10086/sign_sm2
运行fabric-sdk-java/src/test/java/org/hyperledger/fabric/sdkintegration/End2endQueryAndInvoke.java

# 测试query和invoke的性能
设置环境变量DSVS_REST_API_URL=http://localhost:10086/sign_sm2
运行fabric-sdk-java/src/test/java/org/hyperledger/fabric/sdkintegration/End2endQueryAndInvokePerformance.java

# 启动fabric rest api
设置环境变量DSVS_REST_API_URL=http://localhost:10086/sign_sm2
运行fabric-sdk-dsvs-rest/src/main/java/com/bjca/fabricsdkdsvsrest/FabricSdkDsvsRestApplication.java
