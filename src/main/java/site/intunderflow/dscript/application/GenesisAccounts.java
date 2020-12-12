package site.intunderflow.dscript.application;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.utility.Hex;

import java.util.Map;

public class GenesisAccounts {

    private static final String GENESIS_MAIN_ADDR =
            "194DE01752A806C44CDA8A4435925D130C8554AFD41D3C1EFBE1D207FA24B273A872B57D0C39C4CEE2841053E34B539086D0BDB8A9FEA4245EC27DAF9DCDF8AC";

    //10,000,000,000
    private static final long GENESIS_MAIN_AMOUNT = Long.valueOf("10000000000");

    private static final ImmutableMap<String, Long> GENESIS =
            ImmutableMap.<String, Long>builder()
                .put(GENESIS_MAIN_ADDR, GENESIS_MAIN_AMOUNT)
                .build();

    public static long getGenesisAmount(byte[] hash){
        return GENESIS.getOrDefault(Hex.encode(hash), (long)0);
    }

    @VisibleForTesting
    public static byte[] getGenesisMainAddr(){
        return Hex.decode(GENESIS_MAIN_ADDR);
    }

    @VisibleForTesting
    public static long getGenesisMainAmount(){ return GENESIS_MAIN_AMOUNT; }

}
