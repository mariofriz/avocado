package avocado.models;

/**
 *
 * @author Mario
 */
public class Log {

    private LogType type;
    private String message;

    public Log(String message, LogType type) {
        this.type = type;
        this.message = message;
    }

    public LogType getType() {
        return type;
    }

    public void setType(LogType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
