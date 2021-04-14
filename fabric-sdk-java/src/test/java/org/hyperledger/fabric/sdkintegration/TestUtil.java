package org.hyperledger.fabric.sdkintegration;

import java.io.File;
import org.hyperledger.fabric.sdk.*;

public class TestUtil {

    public static String configHome = "/home/hj/workspace/java/fabric-sdk-java-dsvs/gm-docker-with-bjca-1/crypto-config-20210329";
    public static String userName = "Admin@org1.bjca.com";
    private static String userCert = configHome + "/admin.org1/msp/signcerts/admin1.org1.id.crt.pem";
    public static String org = "org1";
    public static String mspId = "Org1MSP";
    public static String myChannel = "v2channel";
    public static String myCC = "lc_example_cc_go";
    public static String netWorkConfig = configHome + "/network-config.yaml";

    private TestUtil() {
    }

    public static User getUser() {
        User appuser = null;
        File sampleStoreFile = new File(System.getProperty("user.home") + File.separator + "test.properties");
        if (sampleStoreFile.exists()) { //For testing start fresh
            sampleStoreFile.delete();
        }
        final SampleStore sampleStore = new SampleStore(sampleStoreFile);
        try {
            appuser = sampleStore.getMember(userName, org, mspId,
                    null,
                    new File(userCert));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appuser;
    }
}