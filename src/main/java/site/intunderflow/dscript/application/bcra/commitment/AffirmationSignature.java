package site.intunderflow.dscript.application.bcra.commitment;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.consensus.dpos.Signature;
import site.intunderflow.dscript.application.consensus.dpos.SignatureFactory;
import site.intunderflow.dscript.application.lddb.resource.Identifier;
import site.intunderflow.dscript.utility.Hex;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class AffirmationSignature {

    private final Signature signature;

    private final Identifier subject;

    public AffirmationSignature(SignatureFactory signatureFactory, CommitmentList list)
    throws GeneralSecurityException {
        this.subject = Identifier.forData(list.toString().getBytes(StandardCharsets.UTF_8));
        this.signature = signatureFactory.sign(subject.getHash());
    }

    private AffirmationSignature(Signature signature, Identifier subject){
        this.signature = Preconditions.checkNotNull(signature);
        this.subject = Preconditions.checkNotNull(subject);
    }

    public Signature getSignature(){
        return signature;
    }

    public Identifier getSubject(){
        return subject;
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("signature", signature.toString());
        jsonObject.put("subject", subject.toString());
        return jsonObject.toString();
    }

    public static AffirmationSignature fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        return new AffirmationSignature(
                Signature.fromString(jsonObject.getString("signature")),
                Identifier.fromString(jsonObject.getString("subject"))
        );
    }

}
