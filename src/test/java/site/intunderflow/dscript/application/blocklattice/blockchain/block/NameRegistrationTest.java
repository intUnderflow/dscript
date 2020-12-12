package site.intunderflow.dscript.application.blocklattice.blockchain.block;

import com.google.common.base.Preconditions;
import org.junit.Test;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;

public class NameRegistrationTest {

    @Test
    public void testBlockEquality(){
        String a = "";
        for (int i = 0; i < 128; i++){
            a = a + "A";
        }
        NameRegistration name1 = new NameRegistration("a",
                new BaseAddress(a));
        NameRegistration name2 = new NameRegistration("a",
                new BaseAddress(a));
        Preconditions.checkArgument(name1.equals(name2), "Block that should be equal is not!");
    }
}
