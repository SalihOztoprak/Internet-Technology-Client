import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileTransfer {
    private String fileName;
    private String ontvanger;

    public FileTransfer(String fileName, String ontvanger) {
        this.fileName = fileName;
        this.ontvanger = ontvanger;
    }

    public void findFile(){

    }

        public void sendFile2() {
        File file = new File(System.getProperty("user.home") + "/Downloads/" + fileName);
        FileInputStream fileInputStream;
        DataOutputStream dataOutputStream;

        try {
            dataOutputStream = new DataOutputStream(handler.getSocket().getOutputStream());
            fileInputStream = new FileInputStream(file);

            int count;
            byte[] buffer = new byte[1024];

            while ((count = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, count);
            }


            fileInputStream.close();
            sendMessage(handler.getSocket(), "File sent");
        } catch (IOException IOE) {
            IOE.getCause();
        }
    }

    protected void transferFile() {
        sendMessage(handler.getSocket(), "To Who do you want to send the file?<br>Enter the name: ");

        String recepient = scanner.nextLine();
        System.out.println();

        boolean fileExists = false;
        File folder = new File(System.getProperty("user.home") + "/Downloads");
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    System.out.println("File " + file.getName());
                }
            }

            System.out.println("Enter the name of the file you want to send");
                String fileName = scanner.nextLine();
                long fileSize = 0;

                //Check if the file already existed
                for (File file : listOfFiles) {
                    if (file.getName().equals(fileName)) {
                        fileExists = true;
                        fileSize = file.length();
                    }
            }

            if (fileExists) {
                sendFileName = fileName;
                String extension = "";

                int i = fileName.lastIndexOf('.');
                if (i > 0) {
                    extension = fileName.substring(i);
                }

                for (int j = 0; j < clientHandlers.size(); j++) {
                    if (clientHandlers.get(i).getUsername().equalsIgnoreCase(recepient)) {
                        sendFile(clientHandlers.get(i));
                        break;
                    }
                }
            } else {
                System.out.println("File with that name not found in downloads folder.");
            }
        } else {
            System.out.println("Place files you want to transfer in your downloads folder.");
        }
    }
}
