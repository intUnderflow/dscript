package site.intunderflow.dscript.network.message;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.utility.FixedMaxLengthString;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.work.BasicSHA512Work;
import site.intunderflow.dscript.work.Work;

public class Message {

    private static final int MAX_LENGTH = 1048576;

    private final FixedMaxLengthString content;

    private final byte[] work;

    private final int claimedWork;

    private final int workDone;

    public Message(
            String content,
            byte[] work,
            int claimedWork
    ){
        this.content = new FixedMaxLengthString(
                Preconditions.checkNotNull(content),
                MAX_LENGTH
        );
        this.work = work;
        this.claimedWork = claimedWork;
        Work workDoneCalculator = new BasicSHA512Work(
            content
        );
        this.workDone = workDoneCalculator.getDifficulty(work);
    }

    public String getContentString(){
        return content.getValue();
    }

    public MessageContent getContent() {
        return new MessageContentFromStringFactory(getContentString()).getMessageContent();
    }

    public byte[] getWork(){
        return work;
    }

    public int getWorkDone() {
        return workDone;
    }

    public int getClaimedWork() {
        return claimedWork;
    }

    public int getWorkForBroadcast(){
        if (getClaimedWork() > getWorkDone()){
            return 0;
        }
        else{
            return getClaimedWork();
        }
    }

    public Message getForNextBroadcast(){
        int newClaimedWork = claimedWork - 3;
        if (newClaimedWork < 0){
            newClaimedWork = 0;
        }
        return new Message(
                content.getValue(),
                work,
                newClaimedWork
        );
    }

    public static Message fromString(String messageString){
        JSONObject messageJson = new JSONObject(messageString);
        String content = messageJson.getString("content");
        byte[] work = Hex.decode(messageJson.getString("work"));
        int claimedWork = messageJson.getInt("claimedWork");
        return new Message(content, work, claimedWork);
    }

    public static Message buildWithWork(String content, int workNeeded){
        BasicSHA512Work worker = new BasicSHA512Work(content);
        byte[] work = worker.performWork(workNeeded);
        return new Message(
                content,
                work,
                workNeeded
        );
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", content.getValue());
        jsonObject.put("work", Hex.encode(work));
        jsonObject.put("claimedWork", claimedWork);
        jsonObject.put("version", 1);
        return jsonObject.toString();
    }

}
