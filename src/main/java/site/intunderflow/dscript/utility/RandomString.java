package site.intunderflow.dscript.utility;

import java.security.SecureRandom;
import java.util.Random;

public class RandomString {

    private static final Random RANDOM = new SecureRandom();

    private static final String POSSIBLE_CHARACTERS =
            "01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final int POSSIBLE_CHARACTERS_LENGTH = POSSIBLE_CHARACTERS.length();

    public static String getRandomString(int length){
        String randomString = "";
        for (int i = 0; i < length; i++){
            char nextCharacter = POSSIBLE_CHARACTERS.charAt(RANDOM.nextInt(POSSIBLE_CHARACTERS_LENGTH));
            randomString = randomString + nextCharacter;
        }
        return randomString;
    }

}
