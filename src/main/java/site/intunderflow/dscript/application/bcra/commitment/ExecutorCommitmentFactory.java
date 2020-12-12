package site.intunderflow.dscript.application.bcra.commitment;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.bcra.executor.ExecutorIdentity;
import site.intunderflow.dscript.application.consensus.dpos.SignatureFactory;
import site.intunderflow.dscript.work.BasicSHA512Work;

public class ExecutorCommitmentFactory {

    private final SignatureFactory signatureFactory;

    private final ExecutorIdentity executorIdentity;

    public ExecutorCommitmentFactory(
            SignatureFactory signatureFactory,
            ExecutorIdentity executorIdentity
    ){
        this.signatureFactory = Preconditions.checkNotNull(signatureFactory);
        this.executorIdentity = Preconditions.checkNotNull(executorIdentity);
    }

     public ExecutorCommitment toExecutorCommitment(long round, int difficulty){
         BasicSHA512Work basicSHA512Work = new BasicSHA512Work(
                 ExecutorCommitment.getComponentForWork(round, executorIdentity)
         );
         byte[] work = basicSHA512Work.performWork(difficulty);
         return new ExecutorCommitment(
                 round,
                 executorIdentity,
                 work
         );
     }

     public SignatureFactory getSignatureFactory() {
        return signatureFactory;
     }
}
