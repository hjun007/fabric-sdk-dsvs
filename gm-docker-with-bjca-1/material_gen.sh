#/bin/bash

export FABRIC_CFG_PATH=$PWD/config
configtxgen -profile TwoOrgsOrdererGenesis -channelID system-channel -outputBlock ./crypto-config-20210329/orderer.block
./configtxgen -profile TwoOrgsChannel -outputCreateChannelTx ./crypto-config-20210329/v2channel.tx -channelID v2channel