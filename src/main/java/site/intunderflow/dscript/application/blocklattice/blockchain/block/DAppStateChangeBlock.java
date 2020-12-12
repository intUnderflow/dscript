package site.intunderflow.dscript.application.blocklattice.blockchain.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import site.intunderflow.dscript.application.consensus.dpos.Signature;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;

public abstract class DAppStateChangeBlock extends Block {

    public abstract ImmutableList<Signature> getAuthoritySignatures();

    public abstract ImmutableMap<ByteValue64, ByteValue64> getPermanentMemory();

    public abstract byte[] getContentToSign();

}
