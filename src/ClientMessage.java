/**
 * This class contains all the messagetypes for the client.
 */
public class ClientMessage {
    private MessageType type;
    private String line;

    /**
     * This enum contains all the messagetypes that the client supports
     */
    public enum MessageType {
        HELO,
        BCST,
        PONG,
        QUIT,
        ENCR,
        KEYS,
        FILE;

        MessageType() {
        }
    }

    /**
     * The default constructor for this class
     *
     * @param type The messagetype
     * @param line The message
     */
    public ClientMessage(MessageType type, String line) {
        this.type = type;
        this.line = line;
    }

    /**
     * Method to return the messagetype to a string
     *
     * @return The proper format of this class
     */
    public String toString() {
        return type + " " + line;
    }
}
