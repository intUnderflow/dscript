package site.intunderflow.dscript.application.blocklattice;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;


public class AccountCreationHelper {

    private final CreateBlock createBlock;
    private final ListeningNode node;

    public AccountCreationHelper(CreateBlock createBlock, ListeningNode node){
        this.createBlock = Preconditions.checkNotNull(createBlock);
        this.node = Preconditions.checkNotNull(node);
    }

    public void doCreationTasks(){
        node.getLddb().broadcastNewData(createBlock.toBytes());
    }

}
