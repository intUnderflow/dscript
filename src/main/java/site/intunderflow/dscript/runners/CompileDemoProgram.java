package site.intunderflow.dscript.runners;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.dapp_ddl.DAppCreate;
import site.intunderflow.dscript.application.executor.ddl.v1.executor.InstructionExecutor;
import site.intunderflow.dscript.application.executor.ddl.v1.instruction.*;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.application.executor.ddl.v1.program.InstructionSet;
import site.intunderflow.dscript.utility.BitString;

import site.intunderflow.dscript.utility.ByteBuffer;
import site.intunderflow.dscript.utility.Hex;

import java.io.File;

public class CompileDemoProgram {

    public static void main(String args[]) throws Exception {
        InstructionSet program = InstructionSet.newBuilder()
                .addInstruction(
                        new PermanentMemoryGet(),
                        ByteBuffer.allocate(64).putInt(0).array()
                )
                .addInstruction(
                        new IsZero(),
                        new byte[0]
                )
                .addInstruction(
                        new IfJump(),
                        ByteBuffer.allocate(4).putInt(12).array()
                )
                .addInstruction(
                        new PermanentMemoryGet(),
                        ByteBuffer.allocate(64).putInt(0).array()
                )
                .addInstruction(
                        new GetInput(),
                        ArrayUtils.addAll(
                                ByteBuffer.allocate(32).putInt(0).array(),
                                ByteBuffer.allocate(1).putInt(1).array()
                        )
                )
                .addInstruction(
                        new SHA512(),
                        ByteBuffer.allocate(2).putInt(1).array()
                )
                .addInstruction(
                        new Eq(),
                        new byte[0]
                )
                .addInstruction(
                        new IfJump(),
                        ByteBuffer.allocate(4).putInt(9).array()
                )
                .addInstruction(
                        new FatalStop(),
                        new byte[0]
                )
                .addInstruction(
                        new GetInput(),
                        ArrayUtils.addAll(
                                ByteBuffer.allocate(32).putInt(64).array(),
                                ByteBuffer.allocate(1).putInt(1).array()
                        )
                )
                .addInstruction(
                        new PermanentMemorySet(),
                        ByteBuffer.allocate(64).putInt(1).array()
                )
                .addInstruction(
                        new Stop(),
                        new byte[0]
                )
                .addInstruction(
                        new GetInput(),
                        ArrayUtils.addAll(
                                ByteBuffer.allocate(32).putInt(0).array(),
                                ByteBuffer.allocate(1).putInt(1).array()
                        )
                )
                .addInstruction(
                        new PermanentMemorySet(),
                        ByteBuffer.allocate(64).putInt(0).array()
                )
                .build();
        String password = "hello";
        String asHex = Hex.encodeString(password);
        while (asHex.length() < 128){
            asHex = asHex + "0";
        }
        byte[] passwordBytes = Hex.decode(asHex);
        String toSubmit = Hex.encode(site.intunderflow.dscript.utility.hashing.SHA512.hash(passwordBytes));
        System.out.println(toSubmit);
        ProgramState programState = new ProgramState(
                Hex.decode(
                        toSubmit
                ),
                program
        );
        InstructionExecutor executor = new InstructionExecutor(programState);
        executor.executeProgram();
        ProgramState nextState = new ProgramState(
                Hex.decode(
                        asHex
                        + "68692074686572652100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                ),
                programState
        );
        InstructionExecutor nextExecutor = new InstructionExecutor(nextState);
        nextExecutor.executeProgram();
        System.out.println("THE PASSWORD IS " + password);
        System.out.println("THE HASH IS " + toSubmit);
        System.out.println("THE KEY IS " + asHex);
        String indexContents = FileUtils.readFileToString(
                new File(
                        "C:/Users/IntegerUnderflow/Documents/DScript Websites/helloworld/index.html"
                )
        );
        System.out.println(indexContents);
        JSONObject filesJson = new JSONObject();
        filesJson.put("index.html", indexContents);
        System.out.println("FILES PAYLOAD");
        System.out.println(Hex.encodeString(filesJson.toString()));
        InstructionSet basicProgram = InstructionSet.newBuilder()
                .addInstruction(
                        new FatalStop(),
                        new byte[0]
                )
                .build();
        System.out.println(Hex.encode(basicProgram.toBytes()));
    }

}
