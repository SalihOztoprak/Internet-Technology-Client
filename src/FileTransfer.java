import java.io.*;
import java.util.Base64;

public class FileTransfer {
    private final static String filePath = "/Downloads/";
    private String fileName;
    private File file;
    private String recipient;
    private String base64String;

    public FileTransfer(String fileName, String recipient) {
        this.fileName = fileName;
        this.file = new File(System.getProperty("user.home") + filePath + fileName);
        this.recipient = recipient;
        this.base64String = null;
    }

    public FileTransfer(String base64String, String fileName, boolean isReceived){
        this.base64String = base64String;
        this.fileName = fileName;
    }

    public String sendFile(){
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            base64String = new String(Base64.getEncoder().encode(bytes));
        } catch (Exception e) {
            System.out.println("File isn't found in /Downloads (please include extension)");
        }

        if (file.exists()) {
            return recipient + " " + fileName + " " + base64String;
        }
        return null;
    }

    public void saveFile(){
        //Decode the file first to a byte array
        byte[] data = Base64.getDecoder().decode(base64String);
        try {
            File file = new File(System.getProperty("user.home") + filePath + fileName);
            if (file.exists()){
                fileName = "$" + fileName;
            }
            OutputStream stream = new FileOutputStream(System.getProperty("user.home") + filePath + fileName);
            stream.write(data);
        } catch (Exception e) {
            System.out.println("File couldn't be downloaded to /Downloads");
        }
    }
}
