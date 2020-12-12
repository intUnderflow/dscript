package site.intunderflow.dscript.application.executor.ddl.v1.executor;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.executor.ddl.v1.instruction.*;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.application.executor.ddl.v1.program.InstructionCall;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.InstructionLookup;

import java.util.HashMap;
import java.util.Map;

public class InstructionExecutor {

    private final ProgramState programState;

    public InstructionExecutor(
            ProgramState programState
    ){
        this.programState = Preconditions.checkNotNull(programState);
    }

    public void execute(InstructionCall instructionCall){
        Instruction toRun = InstructionLookup.get(instructionCall.getId());
        Preconditions.checkNotNull(toRun);
        toRun.apply(programState, instructionCall.getInput());
    }

    public void executeProgram(){
        programState.setNextInstruction(0);
        int amountOfInstructions = programState.getInstructionSet().getAmountOfInstructions();
        while (
                !programState.isExecutionFinished()
                        && programState.getNextInstruction() < amountOfInstructions){
            int currentInstruction = programState.getNextInstruction();
            System.out.println(currentInstruction);
            execute(
                    programState.getInstructionSet().getInstruction(
                            currentInstruction
                    )
            );
            if (programState.getNextInstruction() == currentInstruction){
                programState.setNextInstruction(++currentInstruction);
            }
        }
        programState.finishExecution();
        System.out.println("Finish " + programState.getFinishStateCode());
    }

}
