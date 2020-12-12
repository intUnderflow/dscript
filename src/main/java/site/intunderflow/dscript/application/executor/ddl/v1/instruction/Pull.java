package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.FinishStateCodes;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.InputExpectations;

public class Pull implements Instruction {

    public byte getID(){
        return 0x3;
    }

    public void apply(ProgramState programState, byte[] input){
        InputExpectations.expectNoInput(input);
        applyAndReturn(programState);
    }

    public int getInputLength(){
        return 0;
    }

    @CanIgnoreReturnValue
    protected ByteValue64 applyAndReturn(ProgramState programState){
        if (programState.getStack().size() == 0){
            programState.setFinishStateCode(FinishStateCodes.STACK_EMPTY);
            programState.finishExecution();
            return null;
        }
        else {
            return programState.getStack().pop();
        }
    }

}
