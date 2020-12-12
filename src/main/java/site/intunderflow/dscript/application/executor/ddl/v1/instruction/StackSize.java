package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.InputExpectations;

public class StackSize implements Instruction {

    public byte getID(){
        return 0x4;
    }

    public void apply(ProgramState state, byte[] input){
        InputExpectations.expectNoInput(input);
        new Push().apply(state, ByteValue64.fromInteger(
                state.getStack().size() + 1
        ).getValue().getArray());
    }

    public int getInputLength(){
        return 0;
    }

}
