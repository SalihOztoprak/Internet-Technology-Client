import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

public class EncryptionSession {
    private SecretKey aesKey;
    private Cipher aesCipher;
    private KeyPair rsaKeys;
    private Cipher rsaCipher;

    public EncryptionSession() {
        try {
            KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
            aesKey = aesKeyGen.generateKey();
            aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            KeyPairGenerator rsaKeyGen = KeyPairGenerator.getInstance("RSA");
            rsaKeys = rsaKeyGen.generateKeyPair();
            rsaCipher = Cipher.getInstance("RSA");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String encryptKey(PublicKey publicKey) {
        try {
            //Select the encryption and key
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            //Encrypt the bytes and put it in a new array
            byte[] encryptedBytes = rsaCipher.doFinal(aesKey.getEncoded());

            //Return the bytes array as a string
            return bytesToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decryptKey(String cypher) {
        try {
            //Change the string to bytes array
            byte[] rawCipher = stringToBytes(cypher);

            //Select the encryption and key
            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeys.getPrivate());

            //Decrypt the bytes and put it in a new array
            byte[] decryptedBytes = rsaCipher.doFinal(rawCipher);

            //Return the bytes array as a string
            return bytesToString(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String encryptMessage(String message) {
        try {
            //Change the string to bytes array
            byte[] rawMessage = message.getBytes();

            //Select the encryption and key
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);

            //Encrypt the bytes and put it in a new array
            byte[] encryptedBytes;
            encryptedBytes = aesCipher.doFinal(rawMessage);

            //Return the bytes array as a string
            return bytesToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decryptMessage(String cypherText) {
        try {
            //Change the string to bytes array
            byte[] rawCipher = stringToBytes(cypherText);

            //Select the encryption and key
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey);

            //Decrypt the bytes and put it in a new array
            byte[] decryptedBytes = aesCipher.doFinal(rawCipher);

            //Return the bytes array as a string
            return bytesToString(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] stringToBytes(String crypt) {
        byte[] bytes = new byte[crypt.length()];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) crypt.charAt(i);
        }
        return bytes;
    }

    private String bytesToString(byte[] bytes){
        String message = "";
        for (int i = 0; i < bytes.length; i++) {
            message += Character.toString((char) Byte.toUnsignedInt(bytes[i]));
        }
        return message;
    }

    public PublicKey getPublicKey() {
        return rsaKeys.getPublic();
    }
}
