package site.intunderflow.dscript.application.consensus.dpos;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.crypto.keys.PublicKey;


/**
 * A signature is a binding of a public key and a signature on some data.
 * No representation is made as to the validity of the signature, this can be checked
 * with the content via
 * {@link PublicKey#verifySignature(byte[], byte[])}.
 * This code is used in MPPS at {@link site.intunderflow.dscript.application.multipartypinseeding.MPPSSignature}.
 */
public class Signature {

    private final byte[] signature;

    private final BaseAddress address;

    public Signature(
            BaseAddress address,
            byte[] signature
    ){
        this.address = Preconditions.checkNotNull(address);
        this.signature = Preconditions.checkNotNull(signature);
    }

    public BaseAddress getAddress(){
        return address;
    }

    public PublicKey getPublicKey(NetworkState networkState) {
        CreateBlock block = (CreateBlock) networkState.get(address);
        return (PublicKey) block.getInitializationParams().get("publicKey");
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", address.getAddress());
        jsonObject.put("signature", Hex.encode(signature));
        return jsonObject.toString();
    }

    public static Signature fromString(String from) {
        JSONObject jsonObject = new JSONObject(from);
        return new Signature(
                new BaseAddress(
                        jsonObject.getString("address")
                ),
                Hex.decode(jsonObject.getString("signature"))
        );
    }
}
