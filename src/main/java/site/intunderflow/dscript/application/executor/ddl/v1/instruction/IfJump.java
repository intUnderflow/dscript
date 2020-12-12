package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;

public class IfJump implements Instruction {

    public byte getID(){
        return 0x27;
    }

    @Override
    public void apply(ProgramState programState, byte[] input) {
        if (programState.getStack().size() > 0){
            if (programState.getStack().peek().equals(ByteValue64.fromInteger(1))){
                new Jump().apply(programState, input);
            }
        }
    }

    public int getInputLength(){
        return 4;
    }

}
