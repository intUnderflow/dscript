package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.FinishStateCodes;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.InputExpectations;

public class Eq implements Instruction {

    public byte getID(){
        return 0x15;
    }

    @Override
    public void apply(ProgramState programState, byte[] input) {
        InputExpectations.expectNoInput(input);
        if (programState.getStack().size() < 2){
            programState.setFinishStateCode(FinishStateCodes.STACK_TOO_SMALL);
            programState.finishExecution();
            return;
        }
        ByteValue64 firstValue = programState.getStack().pop();
        ByteValue64 secondValue = programState.getStack().pop();
        if (firstValue.equals(secondValue)){
            programState.getStack().push(
                    ByteValue64.fromInteger(1)
            );
        }
        else{
            programState.getStack().push(
                    ByteValue64.fromInteger(0)
            );
        }
    }

    public int getInputLength(){
        return 0;
    }

}
