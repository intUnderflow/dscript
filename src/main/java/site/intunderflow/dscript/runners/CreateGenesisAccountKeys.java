package site.intunderflow.dscript.runners;

import site.intunderflow.dscript.application.blocklattice.blockchain.BlockHashFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink.Create;
import site.intunderflow.dscript.onstart.OnStart;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.Time;
import site.intunderflow.dscript.utility.crypto.keys.KeyPair;
import site.intunderflow.dscript.utility.crypto.keys.KeySerializer;
import site.intunderflow.dscript.utility.crypto.keys.PublicKey;

import java.io.File;

public class CreateGenesisAccountKeys {

    public static void main(String[] args) throws Exception {
        OnStart.onStart();
        KeyPair keyPair = KeyPair.generateNewExtraStrong();
        KeySerializer serializer = new KeySerializer(keyPair);
        serializer.serializeToFile(new File(
                "C:/dscript/genesis.json"
        ));
        PublicKey publicKey = keyPair.getPublicKey();
        Create genesisStartBlock = Create.forKeyWithTimestamp(publicKey, Time.getUTCTimestamp());
        System.out.println(genesisStartBlock.toString());
        System.out.println(Hex.encode(new BlockHashFactory(genesisStartBlock).hash()));
    }

}
