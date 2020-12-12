package site.intunderflow.dscript.network.cluster.address;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.network.cluster.address.network.NetworkAddress;
import site.intunderflow.dscript.utility.FixedMaxLengthString;

/**
 * A cluster layer address following the NetworkAddress scheme in
 * https://docs.google.com/document/d/1Fzb32T_Bn5XNLGWLbL5koCKj3gD2YgAJHRsrXKvAjQU/edit#
 */

public class Address {

    private static final int MAXIMUM_PATH_LENGTH = 50;

    private final NetworkAddress networkAddress;

    private final int port;

    private final FixedMaxLengthString path;

    public Address(
            NetworkAddress networkAddress,
            int port
    ){
        this(networkAddress, port, "");
    }

    public Address(
            NetworkAddress networkAddress,
            int port,
            String path
    ){
        this.networkAddress = Preconditions.checkNotNull(networkAddress);
        this.port = port;
        this.path = new FixedMaxLengthString(
                MAXIMUM_PATH_LENGTH,
                Preconditions.checkNotNull(path)
        );
    }

    @Override
    public String toString(){
        String slash = "";
        if (path.getValue().length() > 0){
            if (path.getValue().charAt(0) != '/'){
                slash = "/";
            }
        }
        return networkAddress.toString() + ":" + port + slash + path;
    }

    @Override
    public boolean equals(Object o){
        if (o == null){
            return false;
        }
        else if (getClass() != o.getClass()){
            return false;
        }
        else{
            Address compare = (Address) o;
            return compare.toString().equals(toString());
        }
    }

    public static Address fromString(String address){
        int positionOfPort = address.indexOf(":");
        String networkAddressString = address.substring(0, positionOfPort);
        NetworkAddress networkAddress = NetworkAddress.fromString(
                networkAddressString
        );
        String portAndPath = address.substring(positionOfPort + 1);
        int positionOfPathStart = portAndPath.indexOf("/");
        int port;
        String path;
        if (positionOfPathStart == -1){
            port = Integer.valueOf(portAndPath);
            path = "";
        }
        else{
            port = Integer.valueOf(portAndPath.substring(0, positionOfPathStart));
            path = portAndPath.substring(positionOfPathStart);
        }
        return new Address(networkAddress, port, path);
    }

}
