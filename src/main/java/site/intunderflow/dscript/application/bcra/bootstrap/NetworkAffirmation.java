package site.intunderflow.dscript.application.bcra.bootstrap;

import site.intunderflow.dscript.application.bcra.commitment.AffirmationSignature;

import java.util.ArrayList;
import java.util.List;

public class NetworkAffirmation {

    private final List<AffirmationSignature> affirmations;

    public NetworkAffirmation(){
        this.affirmations = new ArrayList<>();
    }

    public void add(AffirmationSignature signature){
        if (!affirmations.contains(signature)){
            affirmations.add(signature);
        }
    }



}
