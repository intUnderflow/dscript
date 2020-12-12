package site.intunderflow.dscript.application.blocklattice.blockchain;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.NameRegistration;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink.Create;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink.Receive;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink.Transfer;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.dapp_ddl.DAppCreate;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.dapp_ddl.StateChanged;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class BlockFromBlockStringFactory {

    private final String blockString;

    public BlockFromBlockStringFactory(String blockString){
        this.blockString = Preconditions.checkNotNull(blockString);
    }

    public Block getBlock() {
        JSONObject jsonObject = new JSONObject(blockString);
        String account;
        if (jsonObject.has("account")){
            account = jsonObject.getString("account");
        }
        else{
            account = null;
        }
        String type = jsonObject.getString("type");
        if (account == null){
            switch(type){
                case "name_registration":
                    return NameRegistration.fromString(blockString);
            }
        }
        else if (account.equals("basic_tink")){
            try {
                switch (type) {
                    case "create":
                        return Create.fromString(blockString);
                    case "transfer":
                        return Transfer.fromString(blockString);
                    case "receive":
                        return Receive.fromString(blockString);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else if (account.equals("dapp_ddl")){
            try {
                switch(type) {
                    case "dapp_create":
                        return DAppCreate.fromString(blockString);
                    case "dapp_state_changed":
                        return StateChanged.fromString(blockString);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        // Unsupported account or type.
        return null;
    }

}
