package site.intunderflow.dscript.network.cluster.address;

import com.google.common.base.Preconditions;
import org.junit.Test;

import java.util.List;

public class AddressBookTest {

    @Test
    public void testAddingToBook(){
        AddressBook addressBook = new AddressBook();
        String addressString = "127.0.0.1:8081";
        Address address = Address.fromString(addressString);
        addressBook.addAddress(address);
        List<Address> gotBack = addressBook.getAllAddresses();
        Preconditions.checkArgument(gotBack.size() == 1, "Address list doesnt contain 1 address.");
        Address backAddress = gotBack.get(0);
        Preconditions.checkArgument(backAddress != null, "Address null");
        Preconditions.checkArgument(backAddress.toString().equals(addressString), "Not same address.");
        System.out.println("Got the correct address from the book: " + backAddress.toString());
    }

}
