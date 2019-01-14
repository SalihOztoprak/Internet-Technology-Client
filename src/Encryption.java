import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;

public class Encryption {
    private String encryptedMessage;

    public Encryption(String message) {
        try {
            byte[] rawData = message.getBytes();
            KeyPair keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, keys.getPublic());
            byte[] encrypted = cipher.doFinal(rawData);
            encryptedMessage = Arrays.toString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getEncryptedMessage() {
        return encryptedMessage;
    }
}
