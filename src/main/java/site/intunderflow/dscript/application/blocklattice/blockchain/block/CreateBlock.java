package site.intunderflow.dscript.application.blocklattice.blockchain.block;

import com.google.common.collect.ImmutableMap;

public abstract class CreateBlock extends Block {

    public abstract String getAccountType();

    public abstract ImmutableMap<String, Object> getInitializationParams();

}
