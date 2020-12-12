package site.intunderflow.dscript.application.lddb.location;

import site.intunderflow.dscript.application.lddb.resource.Identifier;
import site.intunderflow.dscript.application.lddb.resource.Location;

public interface LocalLocationProvider {

    Location getLocation(Identifier identifier, byte[] content);

}
