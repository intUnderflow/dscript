package site.intunderflow.dscript;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import org.junit.BeforeClass;
import org.junit.Test;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.GenesisAccounts;
import site.intunderflow.dscript.application.blocklattice.BlockValueEvaluator;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockBytesFromStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;
import site.intunderflow.dscript.utility.tests.ClusterSetup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Random;

public class SystemGenesisBlockValidityAndValueCheckTest {

    private static final Random random = new SecureRandom();

    private static ListeningNode[] listeningNodes;

    private static ListeningNode nodeHoldingContent;

    private static ListeningNode getRandomListeningNode(){
        return listeningNodes[random.nextInt(listeningNodes.length)];
    }

    @BeforeClass
    public static void createGenesisBlock() throws IOException {
        listeningNodes = ClusterSetup.getClusterForTesting();
        String genesisContent = Resources.toString(SystemGenesisBlockValidityAndValueCheckTest.class.getResource(
            "genesis.json"
        ), StandardCharsets.UTF_8);
        nodeHoldingContent = getRandomListeningNode();
        nodeHoldingContent.getLddb().getStorage().put(
                new BlockBytesFromStringFactory(genesisContent).getBytes()
        );
    }

    @Test
    public void testFetchingGenesisBlockOverLDDB(){
        System.out.println("The node that knows is " + nodeHoldingContent.getPort());
        ListeningNode subjectNode = null;
        while (subjectNode == null || subjectNode == nodeHoldingContent){
            subjectNode = getRandomListeningNode();
        }
        Block block = subjectNode.getNetworkState().get(
                GenesisAccounts.getGenesisMainAddr()
        );
        Preconditions.checkNotNull(block);
        Preconditions.checkArgument(block.isGenesis());
        Preconditions.checkArgument(
                block.getType().equals("create")
        );
        CreateBlock createBlock = (CreateBlock) block;
        Preconditions.checkArgument(
                createBlock.getAccountType().equals("basic_tink")
        );
        Preconditions.checkArgument(
                BlockValueEvaluator.getValue(createBlock) == GenesisAccounts.getGenesisMainAmount()
        );
    }

}
