package site.intunderflow.dscript.utility;

import com.google.common.base.Preconditions;

/**
 * Holds an immutable {@link String} which has a maximum amount of characters.
 * This class will throw a {@link IllegalArgumentException} if you attempt to provide
 * a string that is over the maximum length.
 */
public class FixedMaxLengthString {

    /**
     * Holds the value of the string AFTER being checked for validity.
     */
    private final String value;

    public FixedMaxLengthString(
            int maximumLength, String value
    ){
        Preconditions.checkNotNull(value);
        Preconditions.checkArgument(
                value.length() <= maximumLength,
                "String specified is over maximum length."
        );
        this.value = value;
    }

    public FixedMaxLengthString(String value, int maximumLength){
        this(maximumLength, value);
    }

    public String getValue(){
        return value;
    }

    @Override
    public String toString(){
        return getValue();
    }

}
