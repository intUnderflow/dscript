package site.intunderflow.dscript.work;

import site.intunderflow.dscript.utility.ByteArrayBitIterable;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.hashing.SHA512;

import java.security.SecureRandom;
import java.util.Random;

public class BasicSHA512Work implements Work {

    private final byte[] sha512Hash;

    private final Random random;

    public BasicSHA512Work(
        String contentToHash
    ){
        this.sha512Hash = SHA512.hash(contentToHash);
        random = new SecureRandom();
    }

    public int getDifficulty(byte[] work){
        byte[] hashAndWork = new byte[sha512Hash.length + work.length];
        System.arraycopy(sha512Hash, 0, hashAndWork, 0, sha512Hash.length);
        System.arraycopy(work, 0, hashAndWork, sha512Hash.length, work.length);
        byte[] difficultyHash = SHA512.hash(hashAndWork);
        // Count number of zero bits that the difficulty hash begins with.
        int difficulty = 0;
        for (boolean bit : new ByteArrayBitIterable(difficultyHash)){
            if (bit){
                break;
            }
            else{
                difficulty++;
            }
        }
        return difficulty;
    }

    private byte[] randomWork(){
        byte[] bytes = new byte[128];
        random.nextBytes(bytes);
        return bytes;
    }

    public byte[] performWork(int forDifficulty){
        boolean workDone = false;
        byte[] work = new byte[0];
        while (!workDone){
            work = randomWork();
            workDone = getDifficulty(work) >= forDifficulty;
        }
        return work;
    }

}
