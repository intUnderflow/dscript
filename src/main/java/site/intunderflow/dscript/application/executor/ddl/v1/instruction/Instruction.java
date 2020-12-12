package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;

public interface Instruction {

    byte getID();

    int getInputLength();

    void apply(ProgramState programState, byte[] input);

}
