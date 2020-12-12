package site.intunderflow.dscript.onstart;

import com.google.common.collect.ImmutableList;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OnStart {

    private static final ImmutableList<OnStartAction> ACTIONS =
            ImmutableList.of(
                    new GoogleTink()
            );

    private static final Logger logger = Logger.getLogger(OnStart.class.getName());

    public static void onStart(){
        for (OnStartAction action : ACTIONS){
            try{
                action.onStart();
            }
            catch(Exception e){
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }

}
