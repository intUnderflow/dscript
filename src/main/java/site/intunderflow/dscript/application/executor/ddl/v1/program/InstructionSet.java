package site.intunderflow.dscript.application.executor.ddl.v1.program;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.executor.ddl.v1.instruction.Instruction;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.InstructionLookup;
import site.intunderflow.dscript.utility.BitString;
import site.intunderflow.dscript.utility.ByteArrayReader;
import site.intunderflow.dscript.utility.ByteArrayWriter;
import site.intunderflow.dscript.utility.Hex;

import java.io.ByteArrayOutputStream;
import site.intunderflow.dscript.utility.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class InstructionSet {

    private final int version;

    private final InstructionCall[] instructions;

    private final int amount;

    public InstructionSet(
            int version,
            InstructionCall[] instructionCalls
    ){
        this.version = version;
        this.instructions = Preconditions.checkNotNull(instructionCalls);
        this.amount = instructions.length;
    }

    public InstructionCall getInstruction(int position){
        return instructions[position];
    }

    public int getAmountOfInstructions() {
        return amount;
    }

    public byte[] toBytes(){
        ByteArrayWriter writer = new ByteArrayWriter();
        writer.writeBytes(ByteBuffer.allocate(2).put((byte) version).array());
        writer.writeBytes(ByteBuffer.allocate(4).putInt(instructions.length).array());
        for (InstructionCall instruction : instructions){
            writer.writeBytes(instruction.toBytes());
        }
        return writer.getBytes();
    }

    public String toString(){
        return Hex.encode(toBytes());
    }

    public static InstructionSet fromBytes(byte[] bytes){
        ByteArrayReader reader = new ByteArrayReader(bytes);
        int version = ByteBuffer.wrap(
                reader.nextBytes(2)
        ).get();
        Preconditions.checkArgument(version == 1, "This is an unsupported version of the"
        + " Deterministic Distributed Language (DDL), this application supports version 1, this program is "
        + " version " + version);
        byte[] instructionsAmountBytes = reader.nextBytes(4);
        int instructionsAmount = ByteBuffer.wrap(instructionsAmountBytes).getInt();
        InstructionCall[] instructions = new InstructionCall[instructionsAmount];
        for (int i = 0 ; i < instructionsAmount; i++){
            byte id = ByteBuffer.wrap(
                    reader.peekBytes(2)
            ).get();
            Instruction instructionSpec = InstructionLookup.get(id);
            int inputLength = instructionSpec.getInputLength();
            InstructionCall call = InstructionCall.fromBytes(
                    reader.nextBytes(2 + inputLength)
            );
            instructions[i] = call;
        }
        return new InstructionSet(version, instructions);
    }

    private String toHumanReadableInternal(boolean detail){
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        int instructionNumber = 0;
        for (InstructionCall call : instructions){
            if (first){
                first = false;
            }
            else{
                stringBuilder.append("\n");
            }
            stringBuilder.append(instructionNumber++);
            stringBuilder.append(" ");
            Instruction instruction = InstructionLookup.get(call.getId());
            stringBuilder.append(instruction.getClass().getSimpleName().toUpperCase());
            if (detail) {
                stringBuilder.append(" ");
                stringBuilder.append(BitString.toStringWithSpaces(call.getInput()));
            }
        }
        return stringBuilder.toString();
    }

    public String toHumanReadableBasic(){
        return toHumanReadableInternal(false);
    }

    public String toHumanReadable(){
        return toHumanReadableInternal(true);
    }

    public static InstructionSet fromString(String from){
        return fromBytes(Hex.decode(from));
    }

    public static Builder newBuilder(){
        return new Builder();
    }

    public int getVersion() {
        return version;
    }

    public static class Builder{

        private final List<InstructionCall> instructions;

        public Builder(){
            instructions = new ArrayList<>();
        }

        public Builder addInstruction(InstructionCall call){
            instructions.add(call);
            return this;
        }

        public Builder addInstruction(Instruction instruction, byte[] input){
            return addInstruction(new InstructionCall(instruction.getID(), input));
        }

        public InstructionSet build(){
            return new InstructionSet(1, instructions.toArray(new InstructionCall[0]));
        }

    }

}
