import java.io.*;
import java.util.Base64;

/**
 * This class provides the methods for sending and receiving files
 * It can be used by calling the constructor and either sendFile() or readFile()
 */
public class FileTransfer {
    private final static String filePath = "/Downloads/";
    private String fileName;
    private File file;
    private String recipient;
    private String base64String;

    /**
     * This constructor can be used when you want to send a file to someone else
     *
     * @param fileName  The name of the file you want to send
     * @param recipient The person you want to send the file to
     */
    public FileTransfer(String fileName, String recipient) {
        this.fileName = fileName;
        this.file = new File(System.getProperty("user.home") + filePath + fileName);
        this.recipient = recipient;
        this.base64String = null;
    }

    /**
     * This constructor can be used when you want to read a file from someone else
     *
     * @param base64String The Base64 encoded string of the file you received
     * @param fileName     The filename of the file that has been send to you
     * @param isReceived   This boolean is only used so we can seperate this constructor from the other one
     */
    public FileTransfer(String base64String, String fileName, boolean isReceived) {
        this.base64String = base64String;
        this.fileName = fileName;
    }

    /**
     * This method will find the selected file and decode it with Base64
     *
     * @return The proper formatting for sending a file
     */
    public String sendFile() {
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            base64String = new String(Base64.getEncoder().encode(bytes));
        } catch (Exception e) {
            System.out.println("File isn't found in <User>/Downloads (please include extension)");
        }

        if (file.exists()) {
            return recipient + " " + fileName + " " + base64String;
        }
        return null;
    }

    /**
     * This method will read a file and save it to the filePath
     */
    public void readFile() {
        //Decode the file first to a byte array
        byte[] data = Base64.getDecoder().decode(base64String);
        try {
            //We have to check if the file already exists or not
            boolean placeFile = true;
            while (placeFile) {
                File file = new File(System.getProperty("user.home") + filePath + fileName);
                placeFile = false;
                if (file.exists()) {
                    fileName = "$" + fileName;
                    placeFile = true;
                }
            }
            //If it doesn't exists, we can write it to the filePath
            OutputStream stream = new FileOutputStream(System.getProperty("user.home") + filePath + fileName);
            stream.write(data);
        } catch (Exception e) {
            System.out.println("File couldn't be downloaded to <User>/Downloads");
        }
    }
}
