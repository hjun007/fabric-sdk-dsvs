package org.hyperledger.fabric.sdkintegration;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.whu.gmssl.jsse.provider.GMJsseProvider;

import java.io.File;
import java.security.Security;

public class End2endQueryAndInvoke {

    static {
        Security.insertProviderAt(new GMJsseProvider(), 1);
        Security.insertProviderAt(new BouncyCastleProvider(), 2);
    }

    public static void main(String[] args) throws Exception {

        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        HFClient hfclient = HFClient.createNewInstance();
        hfclient.setCryptoSuite(cryptoSuite);
        NetworkConfig networkConfig = NetworkConfig.fromYamlFile(new File(TestUtil.netWorkConfig));
        hfclient.setUserContext(TestUtil.getUser());
        hfclient.loadChannelFromConfig(TestUtil.myChannel, networkConfig);
        Channel myChannel = hfclient.getChannel(TestUtil.myChannel);
        myChannel.initialize();

        FabricConnection con = new FabricConnection(hfclient, myChannel, TestUtil.getUser());

        ExecuteResult er;
        er = con.query("lc_example_cc_go", "query", "a");
        System.out.println(er.getResult());

        er = con.query("lc_example_cc_go", "query", "b");
        System.out.println(er.getResult());

        er = con.invoke("lc_example_cc_go", "move", "a", "b", "1");
        System.out.println(er.getResult());
        Thread.sleep(2000L);

        er = con.query("lc_example_cc_go", "query", "a");
        System.out.println(er.getResult());

        er = con.query("lc_example_cc_go", "query", "b");
        System.out.println(er.getResult());

        er = con.invoke("lc_example_cc_go", "set", "x", "1");
        System.out.println(er.getResult());
        Thread.sleep(2000L);

        er = con.query("lc_example_cc_go", "query", "x");
        System.out.println(er.getResult());

    }

}
