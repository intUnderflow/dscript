package site.intunderflow.dscript.application.executor.ddl.v1.utility;

public class FinishStateCodes {

    public static final byte SUCCESS = 0x0;

    public static final byte PROGRAM_THROWN_EXCEPTION = 0x1;

    public static final byte STACK_TOO_LARGE = 0x2;

    public static final byte STACK_EMPTY = 0x3;

    public static final byte INTEGER_OVERFLOW = 0x4;

    public static final byte INTEGER_UNDERFLOW = 0x5;

    public static final byte POINTER_OVERFLOW = 0x6;

    public static final byte STACK_TOO_SMALL = 0x7;

}
