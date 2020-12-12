package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;

public class PermanentMemorySet implements Instruction {

    public byte getID(){
        return 0x30;
    }

    @Override
    public void apply(ProgramState programState, byte[] input) {
        Preconditions.checkArgument(input.length == 64, "Must supply a 64 byte index.");
        ByteValue64 index = new ByteValue64(input);
        ByteValue64 value = new Pull().applyAndReturn(programState);
        programState.setPermanentMemory(index, value);
    }

    public int getInputLength(){
        return 64;
    }

}
