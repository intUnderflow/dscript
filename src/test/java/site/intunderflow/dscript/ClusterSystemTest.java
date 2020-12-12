package site.intunderflow.dscript;

import com.google.common.base.Preconditions;
import org.junit.BeforeClass;
import org.junit.Test;
import site.intunderflow.dscript.network.cluster.NodeInterface;
import site.intunderflow.dscript.network.cluster.address.Address;
import site.intunderflow.dscript.network.cluster.address.AddressBook;
import site.intunderflow.dscript.network.cluster.rest.Endpoint;

public class ClusterSystemTest {



    @Test
    public void testClusterDiscovery() throws Exception {
        AddressBook addressBook1 = new AddressBook();
        Address address2 = Address.fromString("127.0.0.1:8082");
        addressBook1.addAddress(address2);
        Endpoint endpoint1 = new Endpoint(addressBook1);
        endpoint1.setup(8080 + 1);
        Address address1 = Address.fromString("127.0.0.1:8081");

        AddressBook addressBook2 = new AddressBook();
        Endpoint endpoint2 = new Endpoint(addressBook2);
        endpoint2.setup(8080 + 2);

        AddressBook addressBook3 = new AddressBook();
        Endpoint endpoint3 = new Endpoint(addressBook3);
        endpoint3.setup(8080 + 3);

        NodeInterface interface1 = new NodeInterface(address1);
        AddressBook addressBookTransmitted1 = interface1.getAllAddresses();
        for (Address address : addressBookTransmitted1.getAllAddresses()){
            System.out.println(address.toString());
            Preconditions.checkArgument(address.toString().equals(address2.toString()),
                    "Unexpected address.");
        }

    }

}
