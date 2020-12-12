package site.intunderflow.dscript.application.multipartypinseeding;

import com.google.common.base.Preconditions;
import com.google.common.graph.Network;
import org.apache.commons.lang3.ArrayUtils;
import site.intunderflow.dscript.application.GenesisAccounts;
import site.intunderflow.dscript.application.blocklattice.BlockValueEvaluator;
import site.intunderflow.dscript.application.blocklattice.BlockchainInterface;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.utility.Dual;
import site.intunderflow.dscript.utility.DualBuilder;
import site.intunderflow.dscript.utility.Provider;
import site.intunderflow.dscript.utility.hashing.SHA512;

import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;

/**
 * Finalized pins are pins reduced and ready to use for seeding.
 */
public class FinalizedPin {

    private static final long GOAL_STAKE = GenesisAccounts.getGenesisMainAmount() * (long)0.55;

    private final byte[] initializationVector;

    private final List<MPPSSignature> signatures;

    public FinalizedPin(byte[] initializationVector, List<MPPSSignature> signatures, NetworkState networkState){
        this.initializationVector = Preconditions.checkNotNull(initializationVector);
        // Order pins in order of their size UNTIL the weight of the pins is >= 0.55 of the total stake.
        List<MPPSSignature> prunedList = new LinkedList<>();
        PinByWeightProvider provider = new PinByWeightProvider(signatures, networkState);
        long stake = 0;
        while (stake < GOAL_STAKE && !provider.exhausted()){
            Dual<MPPSSignature, Long> nextValue = provider.provide();
            prunedList.add(nextValue.getA());
            stake = stake + nextValue.getB();
        }
        this.signatures = prunedList;
    }

    public void validatePin(NetworkState networkState) throws GeneralSecurityException {
        for (int i = 0; i < signatures.size(); i++){
            MPPSSignature signature = signatures.get(i);
            byte[] subject = getSubjectAt(i);
            signature.getSignature().getPublicKey(networkState).verifySignatureViaExceptionThrow(
                    signature.getSignature().getSignature(),
                    subject
            );
        }
    }

    private byte[] getSubjectAt(int position){
        if (position == 0){
            return initializationVector;
        }
        else{
            return SHA512.hash(
                    signatures.get(position - 1).toString()
            );
        }
    }

    public byte[] getSeedValueForPRNG(){
        byte[] seedValue = new byte[0];
        for (MPPSSignature signature : signatures){
            byte[] signatureRaw = signature.getSignature().getSignature();
            byte[] toHash = ArrayUtils.addAll(seedValue, signatureRaw);
            seedValue = SHA512.hash(toHash);
        }
        return seedValue;
    }

    // First element of dual is signature, second is stake.
    private class PinByWeightProvider implements Provider<Dual<MPPSSignature, Long>> {

        private final List<MPPSSignature> signatures;

        private final NetworkState currentNetworkState;

        PinByWeightProvider(List<MPPSSignature> signatures, NetworkState networkState){
            this.signatures = Preconditions.checkNotNull(signatures);
            this.currentNetworkState = Preconditions.checkNotNull(networkState);
        }

        private Dual<MPPSSignature, Long> getLargest(NetworkState networkState){
            MPPSSignature largest = null;
            long largestStake = -1;
            for (MPPSSignature signature : signatures){
                long stake = new BlockchainInterface(
                        networkState.getBlockchain(signature.getSignature().getAddress()),
                        networkState
                ).getConfirmedAccountValue();
                if (stake > largestStake){
                    largestStake = stake;
                    largest = signature;
                }
            }
            DualBuilder<MPPSSignature, Long> builder = new DualBuilder<>();
            builder.setA(largest);
            builder.setB(largestStake);
            return builder.build();
        }

        @Override
        public Dual<MPPSSignature, Long> provide(){
            Dual<MPPSSignature, Long> signaturesAndStake = getLargest(currentNetworkState);
            if (signaturesAndStake.getA() == null){
                return null;
            }
            else{
                signatures.remove(signaturesAndStake.getA());
                return signaturesAndStake;
            }
        }

        @Override
        public boolean exhausted(){
            return signatures.size() == 0;
        }

    }

}
