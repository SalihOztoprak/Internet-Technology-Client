/**
 * This class contains all the messagetypes for the server.
 */
public class ServerMessage {
    private String line;

    /**
     * This enum contains all the messagetypes that the server supports
     */
    public enum MessageType {
        HELO,
        BCST,
        PING,
        DSCN,
        OK,
        ERR,
        FILE,
        ENCR,
        KEYS,
        UNKOWN;

        MessageType() {
        }

    }

    /**
     * The default constructor for this class
     *
     * @param line The message
     */
    public ServerMessage(String line) {
        this.line = line.replace("<br>", "\n");
    }

    /**
     * This method will read the used messagetype and return it
     *
     * @return The messagetype from the server
     */
    public MessageType getMessageType() {
        MessageType result = MessageType.UNKOWN;
        try {
            if ((line != null) && (line.length() > 0)) {
                String[] splits = line.split("\\s+");
                String lineTypePart = splits[0];

                if ((lineTypePart.startsWith("-")) || (lineTypePart.startsWith("+"))) {
                    lineTypePart = lineTypePart.substring(1);
                }
                result = MessageType.valueOf(lineTypePart);
            }
        } catch (IllegalArgumentException iaex) {
            System.out.println("[ERROR] Unknown command");
        }
        return result;
    }

    /**
     * This method returns the message send by the server
     *
     * @return The message from the server
     */
    public String getPayload() {
        if (getMessageType().equals(MessageType.UNKOWN)) {
            return line;
        }

        if ((line == null) || (line.length() < getMessageType().name().length() + 1)) {
            return "";
        }

        int offset = 0;
        if ((getMessageType().equals(MessageType.OK)) || (getMessageType().equals(MessageType.ERR))) {
            offset = 1;
        }
        return line.substring(getMessageType().name().length() + 1 + offset);
    }

    /**
     * Method to return the message to a string
     *
     * @return The message
     */
    public String toString() {
        return line;
    }
}
