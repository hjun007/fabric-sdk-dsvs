package org.hyperledger.fabric.sdkintegration;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.whu.gmssl.jsse.provider.GMJsseProvider;

import java.io.File;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class End2endQueryAndInvokePerformance {

    static FabricConnection con;
    final static int txCount = 300;

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
        testMultiThread();
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

    public static List<String> testMultiThread() throws Exception {
        List<CompletableFuture<String>> list = new ArrayList<>();
        long a = System.currentTimeMillis();
        String time = "" + System.currentTimeMillis();
        for (int i = 0; i < txCount; i++){
            final int fi = i;
            String finalTime = time;
            CompletableFuture<String> completableFuture =  CompletableFuture.supplyAsync(() ->{
                ExecuteResult executeResult = null;
                try {
                    executeResult = con.invoke("lc_example_cc_go", "set", "key_" + finalTime + "_" + fi, "val_" + finalTime + "_" + fi);
                    //executeResult = con.query("lc_example_cc_go", "query", "a");
                    if(executeResult.getResult().equals("") || executeResult.getResult() == null) {
                        System.out.println("[" + fi + "]:" + "failed");
                    } else {
                        System.out.println("[" + fi + "]:" + executeResult.getResult());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(executeResult == null || executeResult.getResult().equals("") || executeResult.getResult() == null) {
                    return "failed";
                } else {
                    return executeResult.getResult();
                }
            });
            list.add(completableFuture);
        }
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
        try {
            combinedFuture.get();
            long b = System.currentTimeMillis();
            System.out.println("time: " + ((b - a) / 1000.0));
            System.out.println("TPS:  " + ((float)txCount / ((b - a) / 1000.0)));

        }catch (Exception e){
            e.printStackTrace();
        }

        List<String> ret = new ArrayList<>();
        for (CompletableFuture<String> completableFuture : list){
            ret.add(completableFuture.get());
        }
        return ret;

    }


}
