package org.hyperledger.fabric.sdkintegration;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.File;

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




    }


}
