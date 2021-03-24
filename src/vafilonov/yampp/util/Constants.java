package vafilonov.yampp.util;

public abstract class Constants {

    // TODO ЗАПИЛИ СЕПАРАТОРЫ
    public static final String TOKEN_SEPARATOR = "\0";

    public static final String LOGIN_TYPE = "LOG";      //  LOG/0username
    public static final String SIGN_UP_TYPE = "SUP";    //  SUP/0username
    public static final String MESSAGE_TYPE = "MSG";    //  MSG/0sender/0dest/0message
    public static final String ERROR_TYPE = "ERR";      //  ERR/0errcode/0err_msg/0time
    public static final String ECHO_TYPE = "ECH";       //  ECH/0{init_message}/0time
    public static final String SIGNAL_TYPE = "SIG";     //  SIG/0sigcode
    public static final String ALIVE_TYPE = "ALV";      //  ALV
    public static final String INTERNAL_TYPE = "INT";   //

    public enum MessageType {
        LOGIN,
        SIGN_UP,
        MESSAGE,
        ERROR,
        ECHO,
        SIGNAL,
        ALIVE,
        INTERNAL
    }

    public enum ClientState {
        INITIAL,
        LOGIN_TRANSIT,
        LOGGED_IN,
        DIALOG_TRANSIT,
        DIALOG
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
            case ALIVE_TYPE:
                return MessageType.ALIVE;
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }

}
