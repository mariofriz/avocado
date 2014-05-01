
package avocado.models;

/**
 *
 * @author Sébastien
 */
public class Error {
    public static final short ERROR_NOT_DEFINED = 0;
    public static final short ERROR_FILE_NOT_FOUND = 1;
    public static final short ERROR_ACCESS_VIOLATION = 2;
    public static final short ERROR_DISK_FULL = 3;
    public static final short ERROR_ILLEGAL_TFTP_OPERATION = 4;
    public static final short ERROR_UNKNOWN_TRANSFER_ID = 5;
    public static final short ERROR_FILE_ALREADY_EXISTS = 6;
    public static final short ERROR_NO_SUCH_USER = 7;
    public static final short ERROR_NO_ERROR = 8;
    public static final String[] ERROR_MESSAGES = {"Timeout error occurred",
        "File not found",
        "Access violation",
        "Disk is full",
        "Illegal tftp operation",
        "Unknown transfer ID",
        "File already exists",
        "No such user",
        "No error message"};
}
