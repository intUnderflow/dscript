package site.intunderflow.dscript.application.multipartypinseeding;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.GenesisAccounts;
import site.intunderflow.dscript.application.blocklattice.BlockchainInterface;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.consensus.dpos.SignatureFactory;
import site.intunderflow.dscript.utility.ThreadSafeSummation;
import site.intunderflow.dscript.utility.crypto.keys.PublicKey;
import site.intunderflow.dscript.utility.hashing.SHA512;

import site.intunderflow.dscript.utility.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.function.Consumer;

public class MultipartyPinSeedingRound {

    private final byte[] initializationVector;

    private final LinkedList<MPPSSignature> signatures;

    private MultipartyPinSeedingRound(byte[] initializationVector){
        this(initializationVector, new LinkedList<>());
    }

    private MultipartyPinSeedingRound(
            byte[] initializationVector,
            LinkedList<MPPSSignature> signatures){
        this.initializationVector = Preconditions.checkNotNull(initializationVector);
        this.signatures = Preconditions.checkNotNull(signatures);
    }

    public void addSignature(MPPSSignature signature){
        signatures.add(Preconditions.checkNotNull(signature));
    }

    public void sign(BaseAddress withAddress, SignatureFactory signatureFactory)
    throws GeneralSecurityException {
        addSignature(
                new MPPSSignature(
                        signatureFactory.sign(getSubjectToSignNext())
                )
        );
    }

    public byte[] getSubjectToSignNext(){
        return getSubjectAt(signatures.size());
    }

    /**
     * Verifies the integrity of the entire round
     * @param networkState - A network state source of truth for deriving keys for signatures.
     * @throws GeneralSecurityException - Thrown if the round is invalid.
     */
    public void verifyIntegrity(NetworkState networkState) throws GeneralSecurityException {
        for (int i = 0; i < signatures.size(); i++){
            byte[] subject = getSubjectAt(i);
            MPPSSignature signature = signatures.get(i);
            // Get the PublicKey for the signature.
            PublicKey publicKey = signature.getSignature().getPublicKey(networkState);
            // Verify the signature is valid.
            publicKey.verifySignatureViaExceptionThrow(
                    signature.getSignature().getSignature(),
                    subject
            );
        }
    }

    public long getStakeInPin(NetworkState networkState) {
        ThreadSafeSummation totalStake = new ThreadSafeSummation();
        forEachSignature((signature) -> {
            BaseAddress address = signature.getSignature().getAddress();
            long stake = new BlockchainInterface(
                    networkState.getBlockchain(address),
                    networkState
            ).getConfirmedAccountValue();
            totalStake.add(stake);
        });
        return totalStake.sum();
    }

    public long getStakeInProportionToNetworkStake(NetworkState networkState) {
        return getStakeInPin(networkState) / GenesisAccounts.getGenesisMainAmount();
    }

    public boolean isStakeAtThreshold(NetworkState networkState){
        return getStakeInProportionToNetworkStake(networkState) >= 0.6;
    }

    private void forEachSignature(Consumer<MPPSSignature> signatureConsumer){
        for (int i = 0; i < signatures.size(); i++){
            signatureConsumer.accept(signatures.get(i));
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

    public static MultipartyPinSeedingRound initialize(byte[] vector){
        return new MultipartyPinSeedingRound(vector);
    }

    public static MultipartyPinSeedingRound initialize(long vector){
        return new MultipartyPinSeedingRound(longToBytes(vector));
    }

    private static byte[] longToBytes(long l){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(l);
        return buffer.array();
    }

    public static Builder newBuilder(){
        return new Builder();
    }

    private static class Builder {

        private byte[] initializationVector;

        private final LinkedList<MPPSSignature> signatures;

        private Builder(){
            signatures = new LinkedList<>();
        }

        void addSignature(MPPSSignature signature){
            signatures.add(signature);
        }

        void setInitializationVector(byte[] vector){
            initializationVector = vector;
        }

        void setInitializationVector(long vector){
            setInitializationVector(longToBytes(vector));
        }

    }

}
