package site.intunderflow.dscript.utility.tests;

import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.network.cluster.address.Address;

import java.util.ArrayList;
import java.util.List;

public class ClusterSetup {

    private static final int NODE_COUNT = 8;

    private static final int START_PORT = 8500;

    private static int currentPort = START_PORT;

    private static int getNextPort(){
        currentPort++;
        return getPort() - 1;
    }

    private static int getPort(){
        return currentPort;
    }

    public static ListeningNode[] getClusterForTesting(){
        return getClusterForTesting(NODE_COUNT);
    }

    public static ListeningNode[] getClusterForTesting(int amount){
        List<Address> bootstrapNodeList = new ArrayList<>();
        bootstrapNodeList.add(
                Address.fromString(
                        "127.0.0.1:" + START_PORT
                )
        );
        ListeningNode[] listeningNodes = new ListeningNode[amount];
        ListeningNode bootstrapNode = new ListeningNode(getNextPort(), new ArrayList<>());
        listeningNodes[0] = bootstrapNode;
        for (int i = 0; i < amount - 1; i++){
            ListeningNode nextNode;
            if (i == 0) {
                nextNode = new ListeningNode(getNextPort(), new ArrayList<>());
            } else {
                nextNode = new ListeningNode(getNextPort(), bootstrapNodeList);
            }
            listeningNodes[i + 1] = nextNode;
        }
        return listeningNodes;
    }

}
