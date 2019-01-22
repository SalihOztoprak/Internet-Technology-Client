import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class Client {
    private ClientConfiguration conf;
    private Socket socket;
    private MessageReader readerThread;
    private MessageWriter writerThread;
    boolean isConnected = false;
    private NonblockingBufferedReader nonblockReader;
    private Stack<ClientMessage> clientMessages;
    private Stack<ServerMessage> serverMessages;
    private HashMap<String, EncryptionSession> encryptionSessionHashMap;
    private HashMap<String, String> savedMessage;

    public Client(ClientConfiguration conf) {
        clientMessages = new Stack();
        serverMessages = new Stack();
        this.conf = conf;
        encryptionSessionHashMap = new HashMap<>();
        savedMessage = new HashMap<>();
    }


    public void start() {
        try {
            socket = new Socket(conf.getServerIp(), conf.getServerPort());


            java.io.InputStream is = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(is));


            readerThread = new MessageReader(reader);
            new Thread(readerThread).start();


            java.io.OutputStream os = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(os);
            writerThread = new MessageWriter(writer);
            new Thread(writerThread).start();

            while (serverMessages.empty()) {
            }

            ServerMessage serverMessage = (ServerMessage) serverMessages.pop();
            if (!serverMessage.getMessageType().equals(ServerMessage.MessageType.HELO)) {
                System.out.println("Expecting a HELO message but received: " + serverMessage.toString());
            } else {
                System.out.println("Please fill in your username: ");
                Scanner scanner = new Scanner(System.in);
                String username = scanner.nextLine();


                ClientMessage heloMessage = new ClientMessage(ClientMessage.MessageType.HELO, username);
                clientMessages.push(heloMessage);

                while (serverMessages.empty()) {
                }


                isConnected = validateServerMessage(heloMessage, (ServerMessage) serverMessages.pop());
                if (!isConnected) {
                    System.out.println("Error logging into server");
                } else {
                    System.out.println("Successfully connected to server.");
                    System.out.println("(Type '/quit' to close connection and stop application.)");
                    System.out.println("Type a broadcast message: ");
                    nonblockReader = new NonblockingBufferedReader(new BufferedReader(new java.io.InputStreamReader(System.in)));


                    while (isConnected) {
                        String line = nonblockReader.readLine();
                        if (line != null) {
                            ClientMessage clientMessage;
                            if (line.equals("/quit")) {
                                clientMessage = new ClientMessage(ClientMessage.MessageType.QUIT, username + " left the server");
                                isConnected = false;

                                Thread.sleep(500L);
                            } else if (line.startsWith("/msg") && line.split(" ").length > 1) {
                                //Split the message first
                                String[] splittedline = line.split(" ");
                                String senderUsername = splittedline[1].toLowerCase();

                                if (!senderUsername.equalsIgnoreCase(username)) {
                                    line = line.replace("/msg " + splittedline[1] + " ", "");

                                    //Check if we have an encryptionsession with the user or not
                                    if (encryptionSessionHashMap.containsKey(senderUsername)) {
                                        //We have an encryptionsession, so we can just send the message
                                        String encryptedLine = encryptionSessionHashMap.get(senderUsername).encryptMessage(line);
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.ENCR, senderUsername + " " + encryptedLine);
                                        System.out.println("(You -> " + senderUsername + "): " + line);
                                    } else {
                                        //We don't have an encryptionsession yet, so we create one
                                        EncryptionSession encryptionSession = new EncryptionSession();
                                        encryptionSessionHashMap.put(senderUsername, encryptionSession);
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.KEYS, senderUsername + " " + encryptionSession.getPublicKey());
                                        savedMessage.put(senderUsername, line);
                                    }
                                } else {
                                    System.out.println("You cannot send a message to yourself");
                                    clientMessage = null;
                                }
                            } else {
                                clientMessage = new ClientMessage(ClientMessage.MessageType.BCST, line);
                                System.out.println(line);
                            }

                            if (clientMessage != null) {
                                clientMessages.push(clientMessage);
                            }
                        }
                        if (!serverMessages.empty()) {
                            ServerMessage received = (ServerMessage) serverMessages.pop();
                            if (received.getMessageType().equals(ServerMessage.MessageType.BCST)) {
                                System.out.println(received.getPayload());
                            }

                            //Check if we received an encrypted message
                            if (received.getMessageType().equals(ServerMessage.MessageType.ENCR)) {
                                String[] splitMessage = received.getPayload().split(" ");
                                String cryptedUser = splitMessage[0].toLowerCase();
                                String cryptedMessage = splitMessage[1];
                                //If we have a session with this user, we can decrypt the message
                                if (encryptionSessionHashMap.containsKey(cryptedUser)) {
                                    System.out.println("(" + cryptedUser + " -> You): " + encryptionSessionHashMap.get(cryptedUser).decryptMessage(cryptedMessage));
                                }
                            } else if (received.getMessageType().equals(ServerMessage.MessageType.KEYS)) {
                                //If this is the first time, we have to share keys
                                String[] splitMessage = received.getPayload().split(" ");
                                String cryptedUser = splitMessage[0].toLowerCase();
                                String cryptedMessage = splitMessage[1];

                                //Check if we send the first message or not
                                if (encryptionSessionHashMap.containsKey(cryptedUser)) {
                                    encryptionSessionHashMap.get(cryptedUser).decryptKey(cryptedMessage);

                                    //After saving the aes key, we have to resend our first message
                                    String msg = savedMessage.get(cryptedUser);
                                    if (msg != null) {
                                        String encryptedLine = encryptionSessionHashMap.get(cryptedUser).encryptMessage(msg);
                                        ClientMessage responseMessage = new ClientMessage(ClientMessage.MessageType.ENCR, cryptedUser + " " + encryptedLine);
                                        clientMessages.push(responseMessage);
                                        System.out.println("(You -> " + cryptedUser + "): " + msg);
                                        savedMessage.remove(cryptedUser);
                                    }
                                } else {
                                    //We send our aes key when we didn't send the message first
                                    EncryptionSession encryptionSession = new EncryptionSession();
                                    encryptionSessionHashMap.put(cryptedUser, encryptionSession);
                                    String aesKey = encryptionSession.encryptKey(cryptedMessage);

                                    ClientMessage responseMessage = new ClientMessage(ClientMessage.MessageType.KEYS, cryptedUser + " " + aesKey);
                                    clientMessages.push(responseMessage);
                                }
                            }
                        }
                    }
                }

                disconnect();
                System.out.println("Client disconnected!");
            }
        } catch (IOException e) {
            System.out.println("Ouch! Could not connect to server!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String commandToMessage(String[] command, int start) {
        StringBuilder message;
        message = new StringBuilder();
        for (int i = start; i < command.length; i++) {
            message.append(command[i]);
            if (i != command.length - 1) {
                message.append(" ");
            }
        }
        return message.toString();
    }

    private boolean validateServerMessage(ClientMessage clientMessage, ServerMessage serverMessage) {
        boolean isValid = false;
        try {
            byte[] hash = java.security.MessageDigest.getInstance("MD5").digest(clientMessage.toString().getBytes());
            String encodedHash = new String(java.util.Base64.getEncoder().encode(hash));
            if ((serverMessage.getMessageType().equals(ServerMessage.MessageType.OK)) &&
                    (encodedHash.equals(serverMessage.getPayload()))) {
                isValid = true;
            }
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return isValid;
    }

    private void disconnect() {
        if (readerThread != null)
            readerThread.kill();
        if (writerThread != null)
            writerThread.kill();
        if (nonblockReader != null) {
            nonblockReader.close();
        }
        isConnected = false;
    }

    public class MessageReader implements Runnable {
        private volatile boolean isRunning = true;
        private BufferedReader reader;

        public MessageReader(BufferedReader reader) {
            this.reader = reader;
        }

        public void run() {
            int receiveNull = 0;
            while (isRunning) {
                String line = readFromServer(reader);
                if (line == null) {
                    receiveNull++;
                    if (receiveNull >= 3) {
                        Client.this.disconnect();
                    }
                } else {
                    ServerMessage message = new ServerMessage(line);
                    if (message.getMessageType().equals(ServerMessage.MessageType.PING)) {
                        ClientMessage pongMessage = new ClientMessage(ClientMessage.MessageType.PONG, "");
                        clientMessages.push(pongMessage);
                    }
                    if (message.getMessageType().equals(ServerMessage.MessageType.DSCN)) {
                        System.out.println("Client disconnected by server.");
                        Client.this.disconnect();
                    }

                    serverMessages.push(message);
                    receiveNull = 0;
                }
            }
        }

        private String readFromServer(BufferedReader reader) {
            String line = null;
            try {
                line = reader.readLine();
                if ((line != null) && (conf.isShowLogging())) {
                    if (conf.isShowColors()) {
                        conf.getClass();
                        String colorCode = "\033[31m";
                        conf.getClass();
                        System.out.println(colorCode + ">> " + line + "\033[0m");
                    } else {
                        System.out.println("<< " + line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading buffer: " + e.getMessage());
            }
            return line;
        }

        public void kill() {
            isRunning = false;
        }
    }

    public class MessageWriter implements Runnable {
        private volatile boolean isRunning = true;
        PrintWriter writer;

        public MessageWriter(PrintWriter writer) {
            this.writer = writer;
        }

        public void run() {
            while (isRunning) {
                if (!clientMessages.empty()) {
                    writeToServer((ClientMessage) clientMessages.pop(), writer);
                }
            }
        }

        private void writeToServer(ClientMessage message, PrintWriter writer) {
            String line = message.toString();
            if (conf.isShowLogging()) {
                if (conf.isShowColors()) {
                    conf.getClass();
                    String colorCode = "\033[32m";
                    conf.getClass();
                    System.out.println(colorCode + "<< " + line + "\033[0m");
                } else {
                    System.out.println("<< " + line);
                }
            }
            writer.println(line);
            writer.flush();
        }

        public void kill() {
            isRunning = false;
        }

    }
}
