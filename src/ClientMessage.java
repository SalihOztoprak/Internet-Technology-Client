public class ClientMessage {
    private MessageType type;

    public static enum MessageType {
        HELO,
        BCST,
        PONG,
        QUIT,
        ENCR,
        KEYS;

        private MessageType() {
        }
    }

    private String line;

    public ClientMessage(MessageType type, String line) {
        this.type = type;
        this.line = line;
    }

    public String toString() {
        return type + " " + line;
    }
}
