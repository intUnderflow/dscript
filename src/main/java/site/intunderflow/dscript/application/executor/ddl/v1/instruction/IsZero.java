package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.FinishStateCodes;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.InputExpectations;

public class IsZero implements Instruction {

    public byte getID(){
        return 0x17;
    }

    public int getInputLength(){
        return 0;
    }

    public void apply(ProgramState programState, byte[] input){
        InputExpectations.expectNoInput(input);
        if (programState.getStack().size() == 0){
            programState.setFinishStateCode(FinishStateCodes.STACK_TOO_SMALL);
            programState.finishExecution();
            return;
        }
        if (programState.getStack().peek().equals(ByteValue64.fromInteger(0))){
            programState.getStack().push(ByteValue64.fromInteger(1));
        }
        else{
            programState.getStack().push(ByteValue64.fromInteger(0));
        }
    }

}
