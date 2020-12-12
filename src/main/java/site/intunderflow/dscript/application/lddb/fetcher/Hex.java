package site.intunderflow.dscript.application.lddb.fetcher;

import site.intunderflow.dscript.application.lddb.resource.Location;

public class Hex implements Fetcher {

    @Override
    public String getType(){
        return "hex";
    }

    @Override
    public byte[] fetch(Location resourceLocation){
        return site.intunderflow.dscript.utility.Hex.decode(resourceLocation.getLocation());
    }

    @Override
    public boolean isRemote(){
        return false;
    }

    @Override
    public boolean canWeFulfill(Location location){
        return false;
    }

    @Override
    public void attemptToFulfill(Location location, byte[] data){
    }
}
