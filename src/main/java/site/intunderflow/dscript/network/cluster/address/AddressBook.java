package site.intunderflow.dscript.network.cluster.address;

import site.intunderflow.dscript.utility.Filter;
import site.intunderflow.dscript.utility.Time;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddressBook {

    private final ConcurrentHashMap<Address, AddressMetadata> map;

    private Address exclude;

    public AddressBook(){
        map = new ConcurrentHashMap<>();
    }

    public void addAddress(Address address){
        addAddress(address, Time.getUTCTimestamp());
    }

    public void addAddress(Address address, Long lastSeen){
        if (!address.equals(exclude)) {
            map.put(address, new AddressMetadata(lastSeen));
        }
    }

    public void addAddressIfNewer(Address address, Long lastSeen){
        if (map.containsKey(address)){
            if (map.get(address).getLastSeen() > lastSeen){
                return;
            }
        }
        addAddress(address, lastSeen);
    }

    public void removeAddress(Address address){
        map.remove(address);
    }

    private ArrayList<Address> getAddresses(ArrayList<Map.Entry<Address, Long>> list){
        ArrayList<Address> addresses = new ArrayList<>();
        for (Map.Entry<Address, Long> entry : list){
            addresses.add(entry.getKey());
        }
        return addresses;
    }

    private ArrayList<Map.Entry<Address, Long>> getWithFilter(Filter<Map.Entry<Address, AddressMetadata>> filter){
        ArrayList<Map.Entry<Address, Long>> entries = new ArrayList<>();
        for(Map.Entry<Address, AddressMetadata> addressEntry : map.entrySet()){
            Map.Entry<Address, Long> newEntry = new AbstractMap.SimpleEntry<>(
                    addressEntry.getKey(), addressEntry.getValue().getLastSeen()
            );
            if (filter.shouldFilter(addressEntry)) {
                entries.add(newEntry);
            }
        }
        return entries;
    }

    public ArrayList<Map.Entry<Address, Long>> getAll(){
        return getWithFilter((entry) -> true);
    }

    public ArrayList<Address> getAllAddresses(){
        return getAddresses(getAll());
    }

    public ArrayList<Map.Entry<Address, Long>> getAlive(){
        return getWithFilter((entry) -> entry.getValue().isAlive());
    }

    public ArrayList<Address> getAliveAddresses(){
        return getAddresses(getAlive());
    }

    public AddressBook filter(Filter<Address> filter){
        AddressBook newBook = new AddressBook();
        for (Map.Entry<Address, Long> entry : getAll()){
            if (filter.shouldFilter(entry.getKey())){
                newBook.addAddressIfNewer(entry.getKey(), entry.getValue());
            }
        }
        return newBook;
    }

    public AddressBook mergeInto(AddressBook toMergeInto){
        for (Map.Entry<Address, Long> entry : getAll()){
            toMergeInto.addAddress(entry.getKey(), entry.getValue());
        }
        return toMergeInto;
    }

    public AddressBook copy(){
        AddressBook newBook = new AddressBook();
        mergeInto(newBook);
        return newBook;
    }

    public void exclude(Address address){
        this.exclude = address;
        removeAddress(address);
    }

    public void cleanUp(){
        for (Map.Entry<Address, AddressMetadata> entry : map.entrySet()){
            if (entry.getValue().shouldBeCleanedUp()){
                map.remove(entry.getKey());
            }
        }
    }

    class AddressMetadata{

        private final Long lastSeen;

        AddressMetadata(Long lastSeen){
            this.lastSeen = lastSeen;
        }

        Long getLastSeen() {
            return lastSeen;
        }

        boolean isAlive(){
            return getLastSeen() + (60 * 60 * 12) > Time.getUTCTimestamp();
        }

        boolean shouldBeCleanedUp(){
            return getLastSeen() + (60 * 60 * 12) * 2 > Time.getUTCTimestamp();
        }

    }

}
