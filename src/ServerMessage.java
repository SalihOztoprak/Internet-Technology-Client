public class ServerMessage {
    public static enum MessageType {
        HELO,
        BCST,
        PING,
        DSCN,
        OK,
        ERR,
        UNKOWN;

        private MessageType() {
        }
    }

    public ServerMessage(String line) {
        this.line = line;
    }

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

    private String line;

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

    public String toString() {
        return line;
    }

    private String[] printNewLine(String[] text) {
        for (int i = 0; i < text.length; i++) {
            if (text[i].contains("&")) {
                text[i] = "\n";
            }
        }
        return text;
    }
}
