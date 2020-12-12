package site.intunderflow.dscript.network.cluster.address.network;

import com.google.common.base.Preconditions;

import java.net.InetAddress;

public class IPv6 extends NetworkAddress {

    private final InetAddress address;

    public IPv6(InetAddress address){
        this.address = Preconditions.checkNotNull(address);
    }

    @Override
    public String toString() {
        return address.getHostAddress();
    }

}
