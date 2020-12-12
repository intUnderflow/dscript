package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.FinishStateCodes;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.InputExpectations;

public class FatalStop implements Instruction {

    @Override
    public byte getID(){
        return 0x1;
    }

    @Override
    public void apply(ProgramState programState, byte[] input) {
        InputExpectations.expectNoInput(input);
        programState.setFinishStateCode(FinishStateCodes.PROGRAM_THROWN_EXCEPTION);
        programState.finishExecution();
    }

    public int getInputLength(){
        return 0;
    }

}
