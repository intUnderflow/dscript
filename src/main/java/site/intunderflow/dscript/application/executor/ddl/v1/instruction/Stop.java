package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.FinishStateCodes;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.InputExpectations;

public class Stop implements Instruction {

    @Override
    public byte getID(){
        return 0x0;
    }

    @Override
    public void apply(ProgramState programState, byte[] input) {
        InputExpectations.expectNoInput(input);
        programState.setFinishStateCode(FinishStateCodes.SUCCESS);
        programState.finishExecution();
    }

    public int getInputLength(){
        return 0;
    }

}
