package site.intunderflow.dscript.utility;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.network.cluster.address.Address;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

public class AddressFromListUniqueRandomProvider implements Provider<Address>{

    private final List<Address> list;

    private final Random random;

    private final Flag exhaustedFlag;

    private void checkSize(){
        if (list.size() == 0) {
            exhaustedFlag.raise();
        }
    }

    public AddressFromListUniqueRandomProvider(List<Address> list){
        this.list = Preconditions.checkNotNull(list);
        this.random = new SecureRandom();
        this.exhaustedFlag = new Flag();
        checkSize();
    }

    @Override
    public Address provide(){
        Preconditions.checkArgument(!exhausted());
        int nextIndex = random.nextInt(list.size());
        Address address = list.get(nextIndex);
        list.remove(nextIndex);
        checkSize();
        return address;
    }

    @Override
    public boolean exhausted(){
        return exhaustedFlag.isRaised();
    }

}