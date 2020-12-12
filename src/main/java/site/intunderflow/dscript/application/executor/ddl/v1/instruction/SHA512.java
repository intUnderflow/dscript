package site.intunderflow.dscript.application.executor.ddl.v1.instruction;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.utility.ByteArrayConvert;

public class SHA512 implements Instruction {

    public byte getID(){
        return 0x18;
    }

    @Override
    public void apply(ProgramState programState, byte[] input) {
        Preconditions.checkArgument(input.length == 2, "Must supply 2 bytes to indicate how much"
                + " to read from the stack.");
        int amountToRead = ByteArrayConvert.toInt(input);
        Preconditions.checkArgument(amountToRead >= 1 && amountToRead <= 2048,
                "Amount to read must be between 1 and 2048.");
        byte[] toHash = new byte[64 * amountToRead];
        int currentIndex = 0;
        for (int i = 0; i < amountToRead; i++){
            ByteValue64 toRead = new Pull().applyAndReturn(programState);
            if (toRead == null){
                return;
            }
            for (byte current : toRead.getValue().getArray()){
                toHash[currentIndex] = current;
                currentIndex++;
            }
        }
        byte[] hash = site.intunderflow.dscript.utility.hashing.SHA512.hash(toHash);
        new Push().applyWithByteValue64(programState, new ByteValue64(hash));
    }

    public int getInputLength(){
        return 2;
    }

}
