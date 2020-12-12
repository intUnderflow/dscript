package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.FinishStateCodes;
import site.intunderflow.dscript.utility.ByteArrayConvert;
import site.intunderflow.dscript.utility.UnsignedBytes;

public class GetInput implements Instruction {

    public byte getID(){
        return 0x25;
    }

    public int getInputLength(){
        return 32 + 1;
    }

    @Override
    public void apply(ProgramState programState, byte[] in) {
        Preconditions.checkArgument(in.length == 32 + 1, "Input is malformed length. Should be\n"
        + "32 BYTES position to read\n"
        + "1 BYTE reading mode");
        byte[] input = programState.getInput();
        byte[] positionToReadArray = new byte[32];
        for (int i = 0; i < 32; i++){
            positionToReadArray[i] = in[i];
        }
        int positionToRead = ByteArrayConvert.toInt(positionToReadArray);
        double readingMode = UnsignedBytes.getValue(in[32]);
        Preconditions.checkArgument(readingMode == 0x0 || readingMode == 0x1, "Reading mode MUST" +
                " be 0 or 1.");
        if (!canReadFromHere(input.length - 1, positionToRead, 1)){
            programState.setFinishStateCode(FinishStateCodes.POINTER_OVERFLOW);
            programState.finishExecution();
            return;
        }
        if (readingMode == 0x1 && !canReadFromHere(input.length - 1, positionToRead, 64)){
            programState.setFinishStateCode(FinishStateCodes.POINTER_OVERFLOW);
            programState.finishExecution();
            return;
        }
        if (readingMode == 0x0){
            byte toAddToStack = input[positionToRead];
            ByteValue64 toAdd = ByteValue64.fromByte(toAddToStack);
            new Push().applyWithByteValue64(programState, toAdd);
        }
        else{
            byte[] toAddToStack = new byte[64];
            System.arraycopy(
                    input,
                    positionToRead,
                    toAddToStack,
                    0,
                    64
            );
            ByteValue64 toAdd = new ByteValue64(toAddToStack);
            new Push().applyWithByteValue64(programState, toAdd);
        }
    }

    private boolean canReadFromHere(int maximumHeight, int proposedHeight, int amountToRead){
        return (proposedHeight + (amountToRead - 1) <= maximumHeight);
    }
}
