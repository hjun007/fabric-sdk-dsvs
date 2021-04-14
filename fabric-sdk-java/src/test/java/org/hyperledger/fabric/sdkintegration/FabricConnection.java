package org.hyperledger.fabric.sdkintegration;

import org.hyperledger.fabric.sdk.*;

import java.util.Collection;

public class FabricConnection {

    private HFClient hfclient;
    private Channel channel;
    private User user;

    public FabricConnection(HFClient hfclient, Channel channel, User user) {
        this.hfclient = hfclient;
        this.channel = channel;
        this.user = user;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getUser() {
        return user;
    }

    public ExecuteResult query(String chaincodeName, String fcn, String... arguments) throws Exception {
        QueryByChaincodeRequest queryByChaincodeRequest = hfclient.newQueryProposalRequest();
        queryByChaincodeRequest.setArgs(arguments);
        queryByChaincodeRequest.setFcn(fcn);
        queryByChaincodeRequest.setChaincodeName(chaincodeName);
        queryByChaincodeRequest.setUserContext(user);

        Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest, channel.getPeers());
        return processProposalResponses(queryProposals);
    }

    public ExecuteResult invoke(String chaincodeName, String fcn, String... arguments) throws Exception {
        TransactionProposalRequest transactionProposalRequest = hfclient.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeName(chaincodeName);
        transactionProposalRequest.setFcn(fcn);
        transactionProposalRequest.setArgs(arguments);
        transactionProposalRequest.setUserContext(user);

        Collection<ProposalResponse> invokePropResp = channel.sendTransactionProposal(transactionProposalRequest);
        ExecuteResult eR = processProposalResponses(invokePropResp);
        channel.sendTransaction(invokePropResp); //CompletableFuture<BlockEvent.TransactionEvent> events
        return eR;
    }

    private ExecuteResult processProposalResponses(Collection<ProposalResponse> propResp) throws Exception {
        String payload = "";
        int i = 0;
        for (ProposalResponse response : propResp) {
            if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                if (i == 0) {
                    payload = response.getProposalResponse().getResponse().getPayload().toStringUtf8();
                }
                String currentPayload = response.getProposalResponse().getResponse().getPayload().toStringUtf8();
                if (null != payload && null != currentPayload && !payload.equals(currentPayload)) {
                    throw new Exception(response.getStatus() + " different proposal responses");
                }
            } else {
                throw new Exception(response.getStatus() + " proposal responses status FAIL, expect SUCCESS");
            }
            i++;
        }
        return new ExecuteResult(payload, propResp);
    }

}
