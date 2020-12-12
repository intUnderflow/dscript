package site.intunderflow.dscript.application.lddb.location;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.lddb.resource.Identifier;
import site.intunderflow.dscript.application.lddb.resource.Location;
import site.intunderflow.dscript.network.cluster.address.Address;
import site.intunderflow.dscript.utility.Hex;

public class EndpointLocationProvider implements LocationProvider {

    private final Address ourAddress;

    public EndpointLocationProvider(
            Address ourAddress
    ){
        this.ourAddress = Preconditions.checkNotNull(ourAddress);
    }

    @Override
    public Location getLocation(Identifier identifier){
        return new Location(
                "url",
                "http://"
                        + ourAddress.toString()
                        + "/lddb?identifier="
                        + Hex.encodeString(identifier.toString())
        );
    }

}
