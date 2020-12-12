package site.intunderflow.dscript.utility;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Time {

    public static Long getUTCTimestamp(){
        return TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance(
                TimeZone.getTimeZone("UTC")
        ).getTimeInMillis());
    }

    public static Long getUTCTimestampInSeconds(){
        return getUTCTimestamp();
    }

}
