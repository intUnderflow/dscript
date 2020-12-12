package site.intunderflow.dscript;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.network.cluster.address.Address;
import site.intunderflow.dscript.network.cluster.address.AddressBook;

import java.util.ArrayList;
import java.util.List;

public class SystemWithAddressAnnouncementsTest {

    private static final ImmutableList<String> EXPECTED_ADDRESSES = ImmutableList.of(
            "127.0.0.1:8182",
            "127.0.0.1:8183",
            "127.0.0.1:8184",
            "127.0.0.1:8185"
    );

    private void checkBook(AddressBook book){
        List<String> notFound = new ArrayList<>();
        for (String address : EXPECTED_ADDRESSES) {
            boolean found = false;
            for (Address current : book.getAllAddresses()){
                if (current.toString().equals(address)){
                    found = true;
                    break;
                }
            }
            if (!found){
                notFound.add(address);
            }
        }
        // 1 should be missing (their own address)
        Preconditions.checkState(notFound.size() == 1, "Not found: " +
                String.join(", ", notFound));
    }

    private void printStart(int num){
        System.out.println("Starting node" + num);
    }

    @Test
    public void testSystem() throws Exception{
        ListeningNode bootstrapper = new ListeningNode(8181, new ArrayList<>());
        List<Address> bootstrap = new ArrayList<>();
        bootstrap.add(
                Address.fromString("127.0.0.1:8181")
        );
        printStart(1);
        ListeningNode node1 = new ListeningNode(8182, bootstrap);
        printStart(2);
        ListeningNode node2 = new ListeningNode(8183, bootstrap);
        printStart(3);
        ListeningNode node3 = new ListeningNode(8184, bootstrap);
        List<Address> viaLeaf = new ArrayList<>();
        viaLeaf.add(
                Address.fromString("127.0.0.1:8184")
        );
        printStart(4);
        ListeningNode node4 = new ListeningNode(8185, viaLeaf);
        checkBook(node1.getAddressBook());
        checkBook(node2.getAddressBook());
        checkBook(node3.getAddressBook());
        checkBook(node4.getAddressBook());
    }

}
