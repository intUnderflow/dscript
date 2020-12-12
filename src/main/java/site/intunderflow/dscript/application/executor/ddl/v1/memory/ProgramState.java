package site.intunderflow.dscript.application.executor.ddl.v1.memory;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.executor.ddl.v1.instruction.Instruction;
import site.intunderflow.dscript.application.executor.ddl.v1.program.InstructionSet;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.FinishStateCodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ProgramState {

    private final byte[] input;

    private final InstructionSet instructionSet;

    private final Map<ByteValue64, ByteValue64> permanentMemory;

    private final Stack<ByteValue64> stack;

    private int nextInstruction;

    private ByteValue64 temporaryValue;

    boolean executionFinished;

    byte finishStateCode = FinishStateCodes.SUCCESS;

    public ProgramState(
            byte[] input,
            InstructionSet instructionSet){
        this.input = Preconditions.checkNotNull(input);
        this.instructionSet = Preconditions.checkNotNull(instructionSet);
        this.permanentMemory = new HashMap<>();
        this.stack = new Stack<>();
        this.nextInstruction = 0;
        this.temporaryValue = ByteValue64.minimum();
        this.executionFinished = false;
    }

    public ProgramState(
            byte[] input,
            ProgramState programState
    ){
        this.input = Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(programState);
        this.instructionSet = programState.instructionSet;
        this.permanentMemory = programState.permanentMemory;
        this.stack = new Stack<>();
        this.nextInstruction = 0;
        this.temporaryValue = ByteValue64.minimum();
        this.executionFinished = false;
    }

    public InstructionSet getInstructionSet() {
        return instructionSet;
    }

    public void setFinishStateCode(byte finishStateCode){
        Preconditions.checkState(this.finishStateCode == FinishStateCodes.SUCCESS);
        this.finishStateCode = finishStateCode;
    }

    public byte[] getInput() {
        return input;
    }

    public Stack<ByteValue64> getStack() {
        return stack;
    }

    public void finishExecution(){
        executionFinished = true;
    }

    public boolean isExecutionFinished() {
        return executionFinished;
    }

    public void addToPermanentMemory(ByteValue64 index, ByteValue64 value){
        permanentMemory.put(index, value);
    }

    public ByteValue64 getFromPermanentMemory(ByteValue64 index){
        return permanentMemory.getOrDefault(index, ByteValue64.minimum());
    }

    public void setPermanentMemory(ByteValue64 index, ByteValue64 value){
        permanentMemory.put(index, value);
    }

    public int getNextInstruction() {
        return nextInstruction;
    }

    public void setNextInstruction(int nextInstruction) {
        this.nextInstruction = nextInstruction;
    }

    public Map<ByteValue64, ByteValue64> getPermanentMemory() {
        return new HashMap<>(permanentMemory);
    }

    public byte getFinishStateCode(){
        return finishStateCode;
    }

}
