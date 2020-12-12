package site.intunderflow.dscript.application.bcra.bootstrap;

import java.util.HashMap;
import java.util.Map;

public class AffirmationStorage {

    private final Map<Long, NetworkAffirmation> affirmations;

    public AffirmationStorage(){
        this.affirmations = new HashMap<>();
    }

}
