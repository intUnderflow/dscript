package site.intunderflow.dscript.application.lddb.resource;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import site.intunderflow.dscript.application.lddb.fetcher.Fetcher;
import site.intunderflow.dscript.application.lddb.fetcher.Hex;
import site.intunderflow.dscript.application.lddb.fetcher.Url;

/** Represents the location of a resource for fetching. */
public class Location {

    private static final ImmutableList<Fetcher> fetchers =
            ImmutableList.of(
                    new Hex(),
                    new Url()
            );

    private final String type;

    private final String location;

    public Location(
            String type,
            String location
    ){
        this.type = Preconditions.checkNotNull(type);
        this.location = Preconditions.checkNotNull(location);
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }


    public byte[] fetch(){
        for (Fetcher fetcher : fetchers){
            if (fetcher.getType().equals(getType())){
                return fetcher.fetch(this);
            }
        }
        return new byte[0];
    }

    public boolean canWeFulfill(){
        for (Fetcher fetcher : fetchers){
            if (fetcher.isRemote()){
                if (fetcher.getType().equals(getType())){
                    return fetcher.canWeFulfill(this);
                }
            }
        }
        return false;
    }

    public void attemptToFulfill(byte[] data){
        for (Fetcher fetcher : fetchers){
            if (fetcher.isRemote()){
                if (fetcher.getType().equals(getType())){
                    fetcher.attemptToFulfill(this, data);
                }
            }
        }
    }

    @Override
    public String toString(){
        return type + "=" + location;
    }

    public static Location fromString(String from){
        int split = from.indexOf("=");
        String type = from.substring(0, split);
        String location = from.substring(split + 1);
        return new Location(type, location);
    }

}
