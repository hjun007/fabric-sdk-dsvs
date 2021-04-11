# 运行dsvs rest api服务
```
export DSVS_LIB_FILE=/path/to/libsvscc.so
export DSVS_REST_API_PORT=10086

cd dsvs-go-rest-api
go build main.go
./main
```

# 启动fabric容器
```
cd gm-docker-with-bjca-1
./fabric.sh down
./fabric.sh up
```

# 测试sdk
运行src/test/java/org/hyperledger/fabric/sdkintegration/End2endLifecycleIT.java

# 测试query和invoke
运行src/test/java/org/hyperledger/fabric/sdkintegration/End2endQueryAndInvoke.java
