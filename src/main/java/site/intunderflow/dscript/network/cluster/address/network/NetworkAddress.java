package site.intunderflow.dscript.network.cluster.address.network;

import org.apache.commons.validator.routines.InetAddressValidator;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

/**
 * Used to represent an IPv4, IPv6 or Fully Qualified Domain Name address.
 */

public abstract class NetworkAddress {

    abstract public String toString();

    private static String getAddressType(String address){
        InetAddressValidator validator = InetAddressValidator.getInstance();
        if (validator.isValidInet4Address(address)){
            return "ipv4";
        }
        else if (validator.isValidInet6Address(address)){
            return "ipv6";
        }
        else{
            return "domain";
        }
    }

    public static NetworkAddress fromString(String address){
        String addressType = getAddressType(address);
        try {
            switch (addressType) {
                case "ipv4":
                    return new IPv4(
                            Inet4Address.getByName(
                                    address
                            )
                    );
                case "ipv6":
                    return new IPv6(
                            Inet6Address.getByName(
                                    address
                            )
                    );
                case "domain":
                    return new Domain(
                            address
                    );
            }
        }
        catch(UnknownHostException e){
            throw new Error(e);
        }
        return null;
    }

}