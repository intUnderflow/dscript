package site.intunderflow.dscript.network.cluster.address.network;

import com.google.common.base.Preconditions;

public class Domain extends NetworkAddress {

    private final String domain;

    public Domain(String domain){
        this.domain = Preconditions.checkNotNull(domain);
    }

    @Override
    public String toString(){
        return domain;
    }

}
