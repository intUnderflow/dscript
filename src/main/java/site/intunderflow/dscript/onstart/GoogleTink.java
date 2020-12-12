package site.intunderflow.dscript.onstart;

import com.google.crypto.tink.config.TinkConfig;

public class GoogleTink extends OnStartAction {

    void onStart() throws Exception {
        TinkConfig.register();
    }

}
