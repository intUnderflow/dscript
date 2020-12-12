package site.intunderflow.dscript.application.bcra.commitment;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.bcra.executor.ExecutorIdentity;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockBytesFromStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;
import site.intunderflow.dscript.authorization.AuthorizationCheck;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.work.BasicSHA512Work;

import java.security.GeneralSecurityException;

public class ExecutorCommitment {

    /** Round is the number of intervals since epoch 0 to the round this commitment is for. */
    private final long round;

    /** The executor to join the list. */
    private final ExecutorIdentity executor;

    /** The work to prevent clogging the list. */
    private final byte[] work;

    private String authorizationType;

    private byte[] authorization;

    public ExecutorCommitment(
            long round,
            ExecutorIdentity executor,
            byte[] work
    ){
        Preconditions.checkArgument(round > 0);
        this.round = round;
        this.executor = Preconditions.checkNotNull(executor);
        this.work = Preconditions.checkNotNull(work);
    }

    public static String getComponentForWork(long round, ExecutorIdentity executor){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("round", round);
        jsonObject.put("executor", executor.toString());
        return jsonObject.toString();
    }

    public String getComponentForWork(){
        return getComponentForWork(round, executor);
    }

    public int getDifficulty(){
        return new BasicSHA512Work(getComponentForWork()).getDifficulty(work);
    }

    public JSONObject getComponentToAuthorize(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("round", round);
        jsonObject.put("executor", executor.toString());
        jsonObject.put("work", Hex.encode(work));
        return jsonObject;
    }

    public byte[] getAuthorizationSubject(){
        return new BlockBytesFromStringFactory(getComponentToAuthorize().toString()).getBytes();
    }

    public void setAuthorization(String authorizationType, byte[] authorization){
        this.authorizationType = Preconditions.checkNotNull(authorizationType);
        this.authorization = Preconditions.checkNotNull(authorization);
    }

    public void checkAuthorized(NetworkState networkState) throws GeneralSecurityException {
        if (authorizationType == null || authorization == null){
            throw new GeneralSecurityException("No authorization supplied.");
        }
        CreateBlock createBlock;
        if (executor.getCreateBlock() == null && networkState == null){
            throw new IllegalStateException("No networkState supplied and createBlock is empty!");
        }
        else if (executor.getCreateBlock() == null){
            createBlock = (CreateBlock) networkState.getGenesis(executor.getBaseAddress());
        }
        else {
            createBlock  = executor.getCreateBlock();
        }
        new AuthorizationCheck(
            createBlock.getInitializationParams()
        ).checkAuthorization(
            authorizationType,
            authorization,
            getAuthorizationSubject()
        );
    }

    @Override
    public String toString(){
        JSONObject jsonObject = getComponentToAuthorize();
        if (authorizationType != null && authorization != null){
            jsonObject.put("authorizationType", authorizationType);
            jsonObject.put("authorization", Hex.encode(authorization));
        }
        return jsonObject.toString();
    }

    public ExecutorIdentity getExecutor() {
        return executor;
    }

    public static ExecutorCommitment fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        ExecutorCommitment executorCommitment = new ExecutorCommitment(
                jsonObject.getLong("round"),
                ExecutorIdentity.fromString(
                        jsonObject.getString("executor")
                ),
                Hex.decode(jsonObject.getString("work"))
        );
        if (jsonObject.has("authorizationType") && jsonObject.has("authorization")){
            executorCommitment.setAuthorization(
                    jsonObject.getString("authorizationType"),
                    Hex.decode(jsonObject.getString("authorization"))
            );
        }
        return executorCommitment;
    }

    @Override
    public boolean equals(Object o){
        if (o == null) {
            return false;
        }
        else if (!o.getClass().equals(getClass())){
            return false;
        }
        else{
            return o.toString().equals(toString());
        }
    }

    @Override
    public int hashCode(){
        return toString().hashCode();
    }

}
