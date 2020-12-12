package site.intunderflow.dscript.application.executor.ddl.v1.utility;

import site.intunderflow.dscript.application.executor.ddl.v1.instruction.*;

import java.util.HashMap;
import java.util.Map;

public class InstructionLookup {

    private static final Map<Byte, Instruction> instructions = createInstructionMap();

    private static Map<Byte, Instruction> createInstructionMap(){
        Map<Byte, Instruction> map = new HashMap<>();
        addToMap(map, new Add());
        addToMap(map, new Eq());
        addToMap(map, new FatalStop());
        addToMap(map, new GetInput());
        addToMap(map, new IfJump());
        addToMap(map, new Jump());
        addToMap(map, new PermanentMemoryGet());
        addToMap(map, new PermanentMemorySet());
        addToMap(map, new Pull());
        addToMap(map, new Push());
        addToMap(map, new SHA512());
        addToMap(map, new StackSize());
        addToMap(map, new Stop());
        addToMap(map, new IsZero());
        return map;
    }

    private static void addToMap(Map<Byte, Instruction> map, Instruction instruction){
        map.put(instruction.getID(), instruction);
    }

    public static Instruction get(byte id){
        return instructions.get(id);
    }

}
