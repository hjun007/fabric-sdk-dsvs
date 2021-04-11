package org.hyperledger.fabric.sdkintegration;

import java.io.File;
import java.nio.file.Paths;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.User;

import static java.lang.String.format;

public class TestUtil {

    public static String configHome = "/home/hj/workspace/java/gm-docker-with-bjca-1/crypto-config-20210329";

    public static String userName = "Admin@org1.bjca.com";
    private static String userSk = configHome + "/msp/keystore";
    private static String userCert = configHome + "/admin.org1/msp/signcerts/admin1.org1.id.crt.pem";

    public static String peerName = "peer0";
    public static String org = "org1";
    public static String mspId = "Org1MSP";

    public static String myChannel = "v2channel";
    public static String myCC = "lc_example_cc_go";
    public static String myCCVersion = "2";
    public static ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(myCC).setVersion(myCCVersion).build();

    public static String netWorkConfig = configHome + "/network-config.yaml";

    private TestUtil() {
    }

    public static File findFileSk(File directory) {
        File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));
        if (null == matches) {
            throw new RuntimeException(format("Matches returned null does %s directory exist?", directory.getAbsoluteFile().getName()));
        }
        if (matches.length != 1) {
            throw new RuntimeException(format("Expected in %s only 1 sk file but found %d", directory.getAbsoluteFile().getName(), matches.length));
        }
        return matches[0];
    }

    public static User getUser() {
        User appuser = null;
        File sampleStoreFile = new File(System.getProperty("user.home") + File.separator + "test.properties");
        if (sampleStoreFile.exists()) { //For testing start fresh
            sampleStoreFile.delete();
        }
        final SampleStore sampleStore = new SampleStore(sampleStoreFile);
        try {
            appuser = sampleStore.getMember(peerName, org, mspId,
                    null,
                    new File(userCert));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appuser;
    }


}
