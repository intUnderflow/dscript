package site.intunderflow.dscript.application.lddb.location;

import site.intunderflow.dscript.application.lddb.resource.Identifier;
import site.intunderflow.dscript.application.lddb.resource.Location;
import site.intunderflow.dscript.utility.Hex;

public class HexProvider implements LocalLocationProvider {

    public HexProvider(){}

    @Override
    public Location getLocation(Identifier identifier, byte[] content){
        return new Location(
                "hex",
                Hex.encode(content)
        );
    }

}
