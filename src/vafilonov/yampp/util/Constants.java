package vafilonov.yampp.util;

public abstract class Constants {

    // TODO ЗАПИЛИ СЕПАРАТОРЫ
    public static final String TOKEN_SEPARATOR = "\0";
    public static final String LOGIN_TYPE = "LOG";
    public static final String SIGN_UP_TYPE = "SUP";
    public static final String MESSAGE_TYPE = "MSG";
    public static final String ERROR_TYPE = "ERR";
    public static final String ECHO_TYPE = "ECH";
    public static final String SIGNAL_TYPE = "SIG";

    public enum MessageType {
        LOGIN,
        SIGN_UP,
        MESSAGE,
        ERROR,
        ECHO,
        SIGNAL
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
            case SIGNAL_TYPE:
                return MessageType.SIGNAL;
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }

}
