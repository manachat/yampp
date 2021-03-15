package vafilonov.yampp.util;

public abstract class Constants {

    public static final String LOGIN_TYPE = "LOG";
    public static final String SIGN_UP_TYPE = "SUP";
    public static final String MESSAGE_TYPE = "MSG";
    public static final String ERROR_TYPE = "ERR";
    public static final String ECHO_TYPE = "ECH";

    public enum MessageType {
        LOGIN,
        SIGN_UP,
        MESSAGE,
        ERROR,
        ECHO
    }

    public enum ConnectionState {
        INITIAL,
        LOGGED
    }

    public static MessageType resolveType(String input) {
        switch (input) {
            case LOGIN_TYPE:
                return MessageType.LOGIN;
            case SIGN_UP_TYPE:
                return MessageType.SIGN_UP;
            case MESSAGE_TYPE:
                return MessageType.MESSAGE;
            case ERROR_TYPE:
                return MessageType.ERROR;
            case ECHO_TYPE:
                return MessageType.ECHO;
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }

}
