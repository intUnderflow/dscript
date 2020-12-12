package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.FinishStateCodes;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.InputExpectations;

public class Add implements Instruction {

    public byte getID(){
        return 0x5;
    }

    public void apply(ProgramState programState, byte[] input){
        InputExpectations.expectNoInput(input);
        Preconditions.checkState(programState.getStack().size() >= 2,
                "Stack does not have 2 values to add.");
        ByteValue64 firstValue = programState.getStack().pop();
        ByteValue64 secondValue = programState.getStack().pop();
        ByteValue64 result = firstValue.add(secondValue);
        if (result.overflowed()){
            programState.setFinishStateCode(FinishStateCodes.INTEGER_OVERFLOW);
            programState.finishExecution();
        }
        else{
            programState.getStack().push(result);
        }
    }

    public int getInputLength(){
        return 0;
    }

}
