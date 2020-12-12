package site.intunderflow.dscript.application.bcra.executor;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockFromBlockStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockHashFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutorIdentity {

    private static final Logger logger = Logger.getLogger(ExecutorIdentity.class.getName());

    private final BaseAddress baseAddress;

    private CreateBlock createBlock;

    public ExecutorIdentity(
            BaseAddress baseAddress
    ){
        this(baseAddress, null);
    }

    public ExecutorIdentity(
            BaseAddress baseAddress,
            CreateBlock createBlock
    ){
        this.baseAddress = Preconditions.checkNotNull(baseAddress);
        if (createBlock != null) {
            validateCreateBlock(createBlock);
        }
        this.createBlock = createBlock;
    }

    public BaseAddress getBaseAddress() {
        return baseAddress;
    }

    public CreateBlock getCreateBlock() {
        return createBlock;
    }

    public void setCreateBlock(CreateBlock createBlock) {
        validateCreateBlock(createBlock);
        this.createBlock = createBlock;
    }

    public void validateCreateBlock(CreateBlock createBlock){
        Preconditions.checkArgument(baseAddress.equals(
                new BaseAddress(
                        new BlockHashFactory(
                                createBlock
                        ).hash()
                )
        ), "Invalid create block!");
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", baseAddress.toString());
        if (createBlock != null) {
            jsonObject.put("createBlock", createBlock.toString());
        }
        return jsonObject.toString();
    }

    @Override
    public boolean equals(Object o){
        if (o == null){
            return false;
        }
        else if (!o.getClass().equals(getClass())){
            return false;
        }
        else {
            return o.toString().equals(toString());
        }
    }

    @Override
    public int hashCode(){
        return toString().hashCode();
    }

    public static ExecutorIdentity fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        ExecutorIdentity executorIdentity = new ExecutorIdentity(
                new BaseAddress(
                        jsonObject.getString("address")
                )
        );
        if (jsonObject.has("createBlock")){
            try {
                executorIdentity.setCreateBlock(
                        (CreateBlock) new BlockFromBlockStringFactory(
                                jsonObject.getString("createBlock")
                        ).getBlock()
                );
            }
            catch(Exception e){
                logger.log(Level.INFO, e.getMessage());
            }
        }
        return executorIdentity;
    }
}
