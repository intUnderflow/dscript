package site.intunderflow.dscript.application.executor.ddl.v1.utility;

import com.google.common.base.Preconditions;

public class InputExpectations {

    public static void expectNoInput(byte[] input){
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length == 0,
                "This instruction does not expect an input.");
    }

}
