import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;

public class Encryption {
    public static void main(String[] args) {
        new Encryption("Hallo");
    }
    private String encryptedMessage;
    private String decryptedMessage;

    public Encryption(String message) {
        try {
            byte[] rawData = message.getBytes();
            KeyPair keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, keys.getPublic());
            byte[] encrypted = cipher.doFinal(rawData);
            encryptedMessage = Arrays.toString(encrypted);

            cipher.init(Cipher.DECRYPT_MODE, keys.getPrivate());
            byte[] decrypted = cipher.doFinal(encryptedMessage.getBytes());
            decryptedMessage = Arrays.toString(decrypted);

            System.out.println(encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getEncryptedMessage() {
        return encryptedMessage;
    }

    public String getDecryptedMessage() {
        return decryptedMessage;
    }
}
