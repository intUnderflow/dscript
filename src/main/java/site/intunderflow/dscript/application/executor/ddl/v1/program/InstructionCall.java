package site.intunderflow.dscript.application.executor.ddl.v1.program;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.executor.ddl.v1.instruction.Instruction;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.InstructionLookup;
import site.intunderflow.dscript.utility.ByteArrayReader;
import site.intunderflow.dscript.utility.ByteArrayWriter;

import site.intunderflow.dscript.utility.ByteBuffer;

public class InstructionCall {

    private final byte id;

    private final byte[] input;

    public InstructionCall(
            byte id,
            byte[] input
    ){
        this.id = id;
        this.input = Preconditions.checkNotNull(input);
    }

    public byte getId() {
        return id;
    }

    public byte[] getInput() {
        return input;
    }

    public byte[] toBytes(){
        ByteArrayWriter writer = new ByteArrayWriter();
        writer.writeBytes(
                ByteBuffer.allocate(2).put(id).array()
        );
        Instruction instruction = InstructionLookup.get(id);
        writer.writeBytes(
                ByteBuffer.allocate(instruction.getInputLength()).put(input).array()
        );
        return writer.getBytes();
    }

    public static InstructionCall fromBytes(byte[] bytes){
        ByteArrayReader reader = new ByteArrayReader(bytes);
        byte id = ByteBuffer.wrap(reader.nextBytes(2)).get();
        Instruction instruction = InstructionLookup.get(id);
        byte[] input = new byte[instruction.getInputLength()];
        ByteBuffer.wrap(reader.nextBytes(instruction.getInputLength())).get(input);
        return new InstructionCall(id, input);
    }

}
