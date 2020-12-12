package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;

public class PermanentMemoryGet implements Instruction {

    public byte getID(){
        return 0x29;
    }

    @Override
    public void apply(ProgramState programState, byte[] input) {
        Preconditions.checkArgument(input.length == 64, "Must supply a 64 byte index.");
        ByteValue64 byteValue64 = new ByteValue64(input);
        ByteValue64 value = programState.getFromPermanentMemory(byteValue64);
        new Push().applyWithByteValue64(programState, value);
    }

    public int getInputLength(){
        return 64;
    }

}
