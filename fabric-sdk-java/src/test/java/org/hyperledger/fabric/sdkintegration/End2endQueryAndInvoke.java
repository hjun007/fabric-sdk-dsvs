package org.hyperledger.fabric.sdkintegration;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;

public class End2endQueryAndInvoke {

    public static void main(String[] args) throws Exception {
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        HFClient hfclient = HFClient.createNewInstance();
        hfclient.setCryptoSuite(cryptoSuite);
        NetworkConfig networkConfig = NetworkConfig.fromYamlFile(new File(TestUtil.netWorkConfig));
        hfclient.setUserContext(TestUtil.getUser());
        hfclient.loadChannelFromConfig(TestUtil.myChannel, networkConfig);
        Channel myChannel = hfclient.getChannel(TestUtil.myChannel);
        myChannel.initialize();

        ExecuteResult er;
        er = query(hfclient, myChannel, TestUtil.getUser(), TestUtil.chaincodeID, "query", "a");
        System.out.println(er.result);

        er = query(hfclient, myChannel, TestUtil.getUser(), TestUtil.chaincodeID, "query", "b");
        System.out.println(er.result);

        er = query(hfclient, myChannel, TestUtil.getUser(), TestUtil.chaincodeID, "move", "a", "b", "10");
        System.out.println(er.result);

        er = query(hfclient, myChannel, TestUtil.getUser(), TestUtil.chaincodeID, "query", "a");
        System.out.println(er.result);

        er = query(hfclient, myChannel, TestUtil.getUser(), TestUtil.chaincodeID, "query", "b");
        System.out.println(er.result);

    }

    public static ExecuteResult query(HFClient hfclient, Channel channel, User userContext, ChaincodeID chainCodeID, String fcn, String... arguments) throws Exception {
        TransactionProposalRequest transactionProposalRequest = hfclient.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chainCodeID);
        transactionProposalRequest.setFcn(fcn);
        transactionProposalRequest.setArgs(arguments);
        transactionProposalRequest.setUserContext(userContext);

        //使用发现服务必须确保至少有一个节点开启了发现服务
        Channel.DiscoveryOptions discoveryOptions = Channel.DiscoveryOptions.createDiscoveryOptions();
        // 随机选取满足背书策略的peer节点组合 ENDORSEMENT_SELECTION_RANDOM
        // 选取满足背书策略的，状态最新、块高最大的peer节点组合 ENDORSEMENT_SELECTION_LEAST_REQUIRED_BLOCKHEIGHT
        discoveryOptions.setEndorsementSelector(ServiceDiscovery.EndorsementSelector.ENDORSEMENT_SELECTION_RANDOM);
        discoveryOptions.setForceDiscovery(false);//false:默认2分钟发现一次，true:每次发送都发现一次
        discoveryOptions.setInspectResults(true);//false:sdk检查背书策略，true:sdk不检查背书策略

        Collection<ProposalResponse> queryPropResp = channel.sendTransactionProposalToEndorsers(transactionProposalRequest, discoveryOptions);
        return processProposalResponses(queryPropResp);
    }

    public static ExecuteResult invoke(HFClient hfclient, Channel channel, User userContext, ChaincodeID chainCodeID, String fcn, String... arguments) throws Exception {
        TransactionProposalRequest transactionProposalRequest = hfclient.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chainCodeID);
        transactionProposalRequest.setFcn(fcn);
        transactionProposalRequest.setArgs(arguments);
        transactionProposalRequest.setUserContext(userContext);

        //使用发现服务必须确保至少有一个节点开启了发现服务
        Channel.DiscoveryOptions discoveryOptions = Channel.DiscoveryOptions.createDiscoveryOptions();
        // 随机选取满足背书策略的peer节点组合 ENDORSEMENT_SELECTION_RANDOM
        // 选取满足背书策略的，状态最新、块高最大的peer节点组合 ENDORSEMENT_SELECTION_LEAST_REQUIRED_BLOCKHEIGHT
        discoveryOptions.setEndorsementSelector(ServiceDiscovery.EndorsementSelector.ENDORSEMENT_SELECTION_RANDOM);
        discoveryOptions.setForceDiscovery(false);//false:默认2分钟发现一次，true:每次发送都发现一次
        discoveryOptions.setInspectResults(true);//false:sdk检查背书策略，true:sdk不检查背书策略

        Collection<ProposalResponse> invokePropResp = channel.sendTransactionProposalToEndorsers(transactionProposalRequest, discoveryOptions);
        ExecuteResult eR = processProposalResponses(invokePropResp);
        channel.sendTransaction(invokePropResp); //CompletableFuture<BlockEvent.TransactionEvent> events
        return eR;
    }

    private static ExecuteResult processProposalResponses(Collection<ProposalResponse> propResp) throws Exception {
        String payload = "";
        int i = 0;
        for (ProposalResponse response : propResp) {
            if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                if (i == 0) {
                    payload = response.getProposalResponse().getResponse().getPayload().toStringUtf8();
                }
                String currentPayload = response.getProposalResponse().getResponse().getPayload().toStringUtf8();
                if (null != payload && null != currentPayload && !payload.equals(currentPayload)) {
                    throw new Exception(response.getStatus() + " different query response");
                }
            } else {
                throw new Exception(response.getStatus() + " err during query");
            }
            i++;
        }
        return new ExecuteResult(payload, propResp);
    }

    public static class ExecuteResult implements Serializable {
        public String getResult() {
            return result;
        }

        public Collection<ProposalResponse> getPropResp() {
            return propResp;
        }

        private String result;
        private static Collection<ProposalResponse> propResp;

        public ExecuteResult(String payload, Collection<ProposalResponse> propResp) {
            this.result = payload;
            this.propResp = propResp;
        }

    }


}
