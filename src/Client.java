import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

/**
 * This class is used to define a client and handles all the methods for sending and receiving messages
 */
public class Client {
    private boolean isConnected = false;
    private ClientConfiguration conf;
    private MessageReader readerThread;
    private MessageWriter writerThread;
    private NonblockingBufferedReader nonblockReader;
    private Stack<ClientMessage> clientMessages;
    private Stack<ServerMessage> serverMessages;
    private HashMap<String, EncryptionSession> encryptionSessionHashMap;
    private HashMap<String, String> savedMessage;

    /**
     * This constructor is used to define a new client and handles all the methods for sending and receiving messages
     *
     * @param conf The configuration of the client
     */
    public Client(ClientConfiguration conf) {
        clientMessages = new Stack<>();
        serverMessages = new Stack<>();
        this.conf = conf;
        encryptionSessionHashMap = new HashMap<>();
        savedMessage = new HashMap<>();
    }

    /**
     * This method starts a connection from the client to the server
     */
    public void start() {
        try {
            Socket socket = new Socket(conf.getServerIp(), conf.getServerPort());

            java.io.InputStream is = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(is));

            readerThread = new MessageReader(reader);
            new Thread(readerThread).start();

            java.io.OutputStream os = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(os);
            writerThread = new MessageWriter(writer);
            new Thread(writerThread).start();

            while (serverMessages.empty()) {
                //Do nothing
            }

            ServerMessage serverMessage = serverMessages.pop();
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


                isConnected = validateServerMessage(heloMessage, serverMessages.pop());
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
                            } else if (line.startsWith("/file") && line.split(" ").length == 3) {
                                //Split the message first
                                String[] splitLine = line.split(" ");
                                String senderUsername = splitLine[1].toLowerCase();

                                if (!senderUsername.equalsIgnoreCase(username)) {
                                    String fileBase64String = new FileTransfer(splitLine[2], senderUsername).sendFile();
                                    clientMessage = new ClientMessage(ClientMessage.MessageType.FILE, fileBase64String);
                                    System.out.println("You have send " + splitLine[2] + " to " + senderUsername);
                                } else {
                                    System.out.println("You cannot send a file to yourself");
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
                            ServerMessage received = serverMessages.pop();


                            if (received.getMessageType().equals(ServerMessage.MessageType.BCST)) {
                                System.out.println(received.getPayload());
                            }

                            //Check if we received an encrypted message
                            else if (received.getMessageType().equals(ServerMessage.MessageType.ENCR)) {
                                //Split the message first
                                String[] splitMessage = received.getPayload().split(" ");
                                String sender = splitMessage[0].toLowerCase();
                                String encryptedMessage = splitMessage[1];

                                //If we have a session with this user, we can decrypt the message
                                if (encryptionSessionHashMap.containsKey(sender)) {
                                    System.out.println("(" + sender + " -> You): " + encryptionSessionHashMap.get(sender).decryptMessage(encryptedMessage));
                                }
                            }

                            //If this is the first time, we have to share keys
                            else if (received.getMessageType().equals(ServerMessage.MessageType.KEYS)) {
                                //Split the message first
                                String[] splitMessage = received.getPayload().split(" ");
                                String sender = splitMessage[0].toLowerCase();
                                String encryptedKey = splitMessage[1];

                                //Check if we send the first message or not
                                if (encryptionSessionHashMap.containsKey(sender)) {
                                    encryptionSessionHashMap.get(sender).decryptKey(encryptedKey);

                                    //After saving the aes key, we have to resend our first message
                                    String msg = savedMessage.get(sender);
                                    if (msg != null) {
                                        String encryptedLine = encryptionSessionHashMap.get(sender).encryptMessage(msg);
                                        ClientMessage responseMessage = new ClientMessage(ClientMessage.MessageType.ENCR, sender + " " + encryptedLine);
                                        clientMessages.push(responseMessage);
                                        System.out.println("(You -> " + sender + "): " + msg);
                                        savedMessage.remove(sender);
                                    }
                                }

                                //We send our aes key when we didn't send the message first
                                else {
                                    EncryptionSession encryptionSession = new EncryptionSession();
                                    encryptionSessionHashMap.put(sender, encryptionSession);
                                    String aesKey = encryptionSession.encryptKey(encryptedKey);

                                    ClientMessage responseMessage = new ClientMessage(ClientMessage.MessageType.KEYS, sender + " " + aesKey);
                                    clientMessages.push(responseMessage);
                                }
                            }

                            //Check if the message is a file
                            else if (received.getMessageType().equals(ServerMessage.MessageType.FILE)) {
                                String[] splitMessage = received.getPayload().split(" ");
                                String sender = splitMessage[0].toLowerCase();
                                String base64String = splitMessage[2];

                                new FileTransfer(base64String, splitMessage[0], true).readFile();
                                System.out.println("You have received " + splitMessage[1] + " from " + sender);
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

    /**
     * This method will validate the +OK message received from the server when entering an username
     *
     * @param clientMessage The message from the client
     * @param serverMessage The message from the server
     * @return True when message is validated, false when it is not validated
     */
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

    /**
     * This method will disconnect you from the server
     */
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

    /**
     * This method is used to read messages send from the server
     */
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
                        String colorCode = "\033[31m";
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

    /**
     * This method is used to write messages send to the server
     */
    public class MessageWriter implements Runnable {
        private volatile boolean isRunning = true;
        PrintWriter writer;

        public MessageWriter(PrintWriter writer) {
            this.writer = writer;
        }

        public void run() {
            while (isRunning) {
                if (!clientMessages.empty()) {
                    writeToServer(clientMessages.pop(), writer);
                }
            }
        }

        private void writeToServer(ClientMessage message, PrintWriter writer) {
            String line = message.toString();
            if (conf.isShowLogging()) {
                if (conf.isShowColors()) {
                    String colorCode = "\033[32m";
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
