package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.utility.ByteArrayConvert;

public class Jump implements Instruction {

    @Override
    public byte getID() {
        return 0x26;
    }

    @Override
    public void apply(ProgramState programState, byte[] input) {
        Preconditions.checkArgument(input.length == 4,
                "Position to jump to must be 4 bytes long.");
        applyWithPosition(programState, ByteArrayConvert.toInt(input));
    }

    protected void applyWithPosition(ProgramState programState, int position){
        Preconditions.checkArgument(position < programState.getInstructionSet().getAmountOfInstructions(),
                "Position must be a valid instruction.");
        programState.setNextInstruction(position);
    }

    public int getInputLength(){
        return 4;
    }

}
