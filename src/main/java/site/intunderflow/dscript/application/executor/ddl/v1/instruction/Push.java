package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.FinishStateCodes;

public class Push implements Instruction {

    @Override
    public byte getID(){
        return 0x2;
    }

    @Override
    public void apply(ProgramState programState, byte[] input){
        applyWithByteValue64(programState, new ByteValue64(input));
    }

    public int getInputLength(){
        return 64;
    }

    protected void applyWithByteValue64(ProgramState programState, ByteValue64 byteValue64){
        if (programState.getStack().size() >= 2048){
            programState.setFinishStateCode(FinishStateCodes.STACK_TOO_LARGE);
            programState.finishExecution();
        }
        else {
            programState.getStack().push(byteValue64);
        }
    }

}
