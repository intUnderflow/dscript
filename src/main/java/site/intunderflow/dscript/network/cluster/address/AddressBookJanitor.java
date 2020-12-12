package site.intunderflow.dscript.network.cluster.address;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.network.cluster.NodeInterface;

public class AddressBookJanitor {

    private final AddressBook addressBook;

    public AddressBookJanitor(AddressBook addressBook){
        this.addressBook = Preconditions.checkNotNull(addressBook);
    }

    private void check(Address address){
        NodeInterface nodeInterface = new NodeInterface(address);
        nodeInterface.setAddressBook(addressBook);
        nodeInterface.attemptPing();
    }

    public void checkAll(){
        addressBook.cleanUp();
        for (Address address : addressBook.getAllAddresses()){
            check(address);
        }
    }

}
