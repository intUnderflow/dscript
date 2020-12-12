package site.intunderflow.dscript.runners;

import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.GenesisAccounts;
import site.intunderflow.dscript.application.bcra.commitment.ExecutorCommitmentFactory;
import site.intunderflow.dscript.application.bcra.executor.ExecutorIdentity;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink.Create;
import site.intunderflow.dscript.application.blocklattice.commander.basic_tink;
import site.intunderflow.dscript.application.consensus.dpos.SignatureFactory;
import site.intunderflow.dscript.onstart.OnStart;
import site.intunderflow.dscript.utility.FileStorageLocation;
import site.intunderflow.dscript.utility.crypto.keys.KeyDeserializer;
import site.intunderflow.dscript.utility.crypto.keys.KeyPair;
import site.intunderflow.dscript.utility.tests.ClusterSetup;
import site.intunderflow.dscript.webpanel.Webpanel;

import java.io.File;

public class StandardNodeInDummyNetwork {

    private static ListeningNode[] nodes;

    private static void setupDummyCluster(){
        nodes = ClusterSetup.getClusterForTesting(5);
    }

    public static void main(String[] args) throws Exception{
        OnStart.onStart();
        setupDummyCluster();
        ListeningNode ourNode = nodes[nodes.length - 1];
        ourNode.getRouter().printBroadcasts();
        ourNode.getNetworkState().printAccepts();
        Webpanel webpanel = new Webpanel(ourNode);
        ourNode.getEndpoint().setWebPanel(webpanel);
        boolean openedInWebPanel = webpanel.attemptOpenInWebBrowser();
        if (!openedInWebPanel){
            System.out.println("We couldn't automatically open your web browser");
            System.out.println("Please navigate to " + webpanel.getUrlForAttemptToOpen());
        }
        try {
            KeyPair keyPair = KeyDeserializer.forFile(new File(
                    FileStorageLocation.getFileStorageLocationWithFolder("accounts") + "account.json"
            )).toKeyPair();

            SignatureFactory signatureFactory = new SignatureFactory(
                    keyPair,
                    new BaseAddress(GenesisAccounts.getGenesisMainAddr())
            );
            ourNode.setRealtimeMonitorSignatureFactory(
                    signatureFactory
            );
            CreateBlock ourCreateBlock = Create.forKeyWithTimestamp(
                    keyPair.getPublicKey(), 1555629L
            );
            BaseAddress ourAddress = BaseAddress.forCreateBlock(
                    ourCreateBlock
            );
            webpanel.setCommander(
                    new basic_tink(
                            signatureFactory,
                            ourAddress,
                            ourNode.getNetworkState()
                    )
            );
            ourNode.makeMeAnExecutor(
                    new ExecutorCommitmentFactory(
                            signatureFactory,
                            new ExecutorIdentity(ourAddress, ourCreateBlock)
                    )
            );
            System.out.println("Account attached.");
        }
        catch(Exception e){
            System.out.println("Couldn't attach your account because: " + e.getClass().getName() + "-" + e.getMessage());
        }
    }

}
