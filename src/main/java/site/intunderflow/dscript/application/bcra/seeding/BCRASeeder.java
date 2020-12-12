package site.intunderflow.dscript.application.bcra.seeding;

import org.apache.commons.lang3.ArrayUtils;
import site.intunderflow.dscript.application.bcra.commitment.CommitmentList;
import site.intunderflow.dscript.application.bcra.commitment.ExecutorCommitment;
import site.intunderflow.dscript.utility.hashing.SHA512;

import java.nio.charset.StandardCharsets;

/**
 * Take advantage of the BCRA list to create a seed.
 */
public class BCRASeeder {

    private final byte[] seed;

    public BCRASeeder(CommitmentList list){
        byte[] current = new byte[0];
        for (ExecutorCommitment commitment : list.getCommitments()){
            current = SHA512.hash(ArrayUtils.addAll(
                    current,
                    commitment.toString().getBytes(StandardCharsets.UTF_8)
            ));
        }
        seed = current;
    }

    public byte[] getSeed(){
        return seed;
    }

}
