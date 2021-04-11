#!/usr/bin/env bash
#
# Copyright IBM Corp. All Rights Reserved.
#
# SPDX-License-Identifier: Apache-2.0
#
# simple batch script making it easier to cleanup and start a relatively fresh fabric env.

cd "$(dirname "${BASH_SOURCE[0]}")"

ORG_HYPERLEDGER_FABRIC_SDKTEST_VERSION=${ORG_HYPERLEDGER_FABRIC_SDKTEST_VERSION:-}

function clean(){

  rm -rf /var/hyperledger/*

  if [ -e "/tmp/HFCSampletest.properties" ];then
    rm -f "/tmp/HFCSampletest.properties"
  fi

  lines=`docker ps -a | grep 'hyperledger\|bjca\|example.com' | wc -l`

  if [ "$lines" -gt 0 ]; then
    docker ps -a | grep 'hyperledger\|bjca\|example.com\|dev-' | awk '{print $1}' | xargs docker rm -f
    docker volume rm $(docker volume ls -q)
    docker rmi $(docker images dev-* -q)
  fi


}

function up(){

  # if [ "$ORG_HYPERLEDGER_FABRIC_SDKTEST_VERSION" == "1.0.0" ]; then
  #   docker-compose up --force-recreate ca0 ca1 peer1.org1.example.com peer1.org2.example.com
  # else
    docker-compose -f docker-compose-cli.yaml -f docker-compose-etcdraft.yaml up --force-recreate
# fi

}

function down(){
  docker-compose -f docker-compose-cli.yaml -f docker-compose-etcdraft.yaml down -v;
  docker rmi $(docker images dev-* -q) -f
  docker stop $(docker ps -aq)
  docker rm $(docker ps -aq)
}

function stop (){
  docker-compose -f docker-compose-cli.yaml -f docker-compose-etcdraft.yaml stop;
}

function start (){
  docker-compose -f docker-compose-cli.yaml -f docker-compose-etcdraft.yaml start;
}


for opt in "$@"
do

    case "$opt" in
        up)
            up
            ;;
        down)
            down
            ;;
        stop)
            stop
            ;;
        start)
            start
            ;;
        clean)
            clean
            ;;
        restart)
            down
            clean
            up
            ;;

        *)
            echo $"Usage: $0 {up|down|start|stop|clean|restart}"
            exit 1

esac
done
