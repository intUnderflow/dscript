package site.intunderflow.dscript.application.blocklattice.commander;

import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.TransferBlock;

import java.security.GeneralSecurityException;

/**
 * Commanders are used to manipulate accounts by those authorized to do so.
 */
public interface Commander {

    BaseAddress getFor();

    TransferBlock createTransfer(BaseAddress to, long amount) throws GeneralSecurityException;

    long getBalance();
}
