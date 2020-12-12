package site.intunderflow.dscript.application.lddb.fetcher;

import site.intunderflow.dscript.application.lddb.resource.Location;

public interface Fetcher {

    byte[] fetch(Location location);

    String getType();

    boolean isRemote();

    boolean canWeFulfill(Location location);

    void attemptToFulfill(Location location, byte[] data);

}
