package vafilonov.yampp.util;

import java.time.ZonedDateTime;

public class TimedMessage {

    private final String message;
    private final ZonedDateTime time;

    public TimedMessage(String msg, ZonedDateTime time) {
        this.message = msg;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public ZonedDateTime getTime() {
        return time;
    }

}
