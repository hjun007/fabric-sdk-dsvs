package com.bjca.fabricsdkdsvsrest.service;

import com.bjca.fabricsdkdsvsrest.pojo.TxArgsPojo;
import com.bjca.fabricsdkdsvsrest.tools.ExecuteResult;
import com.bjca.fabricsdkdsvsrest.tools.FabricConnection;
import com.bjca.fabricsdkdsvsrest.tools.TestUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.springframework.stereotype.Service;
import org.whu.gmssl.jsse.provider.GMJsseProvider;

import java.io.File;
import java.security.Security;

@Service
public class FabricService {

    static FabricConnection con;

    static {

        Security.insertProviderAt(new GMJsseProvider(), 1);
        Security.insertProviderAt(new BouncyCastleProvider(), 2);

        try {
            con = getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        ExecuteResult er = con.query(TestUtil.myCC, "query", "a");
        System.out.println(er.getResult());
    }

    static FabricConnection getConnection() throws Exception {

        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        HFClient hfclient = HFClient.createNewInstance();
        hfclient.setCryptoSuite(cryptoSuite);
        NetworkConfig networkConfig = NetworkConfig.fromYamlFile(new File(TestUtil.netWorkConfig));
        hfclient.setUserContext(TestUtil.getUser());
        hfclient.loadChannelFromConfig(TestUtil.myChannel, networkConfig);
        Channel myChannel = hfclient.getChannel(TestUtil.myChannel);
        myChannel.initialize();

        FabricConnection con = new FabricConnection(hfclient, myChannel, TestUtil.getUser());
        return con;
    }

    public ExecuteResult query(TxArgsPojo txArgsPojo) throws Exception {
        return con.query(txArgsPojo.getChaincodeName(), txArgsPojo.getFunction(), txArgsPojo.getArgs());
    }

    public ExecuteResult invoke(TxArgsPojo txArgsPojo) throws Exception {
        return con.invoke(txArgsPojo.getChaincodeName(), txArgsPojo.getFunction(), txArgsPojo.getArgs());
    }


}
