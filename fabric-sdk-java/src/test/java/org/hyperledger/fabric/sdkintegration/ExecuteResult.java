package org.hyperledger.fabric.sdkintegration;

import org.hyperledger.fabric.sdk.ProposalResponse;

import java.io.Serializable;
import java.util.Collection;

public class ExecuteResult implements Serializable {

    private String result;
    private Collection<ProposalResponse> propResp;

    public String getResult() {
        return result;
    }

    public Collection<ProposalResponse> getPropResp() {
        return propResp;
    }

    public ExecuteResult(String payload, Collection<ProposalResponse> propResp) {
        this.result = payload;
        this.propResp = propResp;
    }
}
