import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

/**
 * This method is used to create an encryptionsession
 * When you send a private message to another user, an instance of this class will be made
 */
public class EncryptionSession {
    private SecretKey aesKey;
    private Cipher aesCipher;
    private KeyPair rsaKeys;
    private Cipher rsaCipher;
    private IvParameterSpec iv;
    private SecretKeySpec skeySpec;

    /**
     * This is the default constructor for this class
     * The constructor will generate the keys needed for the encryption
     */
    public EncryptionSession() {
        try {
            KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
            aesKey = aesKeyGen.generateKey();
            aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            KeyPairGenerator rsaKeyGen = KeyPairGenerator.getInstance("RSA");
            rsaKeys = rsaKeyGen.generateKeyPair();
            rsaCipher = Cipher.getInstance("RSA");

            iv = new IvParameterSpec(aesKey.getEncoded());
            skeySpec = new SecretKeySpec(aesKey.getEncoded(), "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will encrypt your AES key with the given public key
     *
     * @param publicKey The public key from the sender
     * @return A string that contains an encrypted AES key
     */
    public String encryptKey(String publicKey) {
        try {
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(base64ToBytes(publicKey));
            PublicKey publicKey1 = KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);

            //Select the encryption and key
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey1);

            //Encrypt the bytes and put it in a new array
            byte[] encryptedBytes = rsaCipher.doFinal(aesKey.getEncoded());

            //Return the bytes array as a string
            return bytesToBase64(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method will decrypt the given encrypted AES key and set it as your new AES key
     *
     * @param cypher The encrypted AES key
     */
    public void decryptKey(String cypher) {
        try {
            //Change the string to bytes array
            byte[] rawCipher = base64ToBytes(cypher);

            //Select the encryption and key
            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeys.getPrivate());

            //Decrypt the bytes and put it in a new array
            byte[] decryptedBytes = rsaCipher.doFinal(rawCipher);

            //Return the bytes array as a string
            aesKey = new SecretKeySpec(decryptedBytes, 0, decryptedBytes.length, "AES");
            iv = new IvParameterSpec(aesKey.getEncoded());
            skeySpec = new SecretKeySpec(aesKey.getEncoded(), "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will encrypt your given message using your AES key
     *
     * @param message The message you want to encrypt
     * @return The encrypted message
     */
    public String encryptMessage(String message) {
        try {
            //Change the string to bytes array
            byte[] rawMessage = message.getBytes();

            //Select the encryption and key
            aesCipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            //Encrypt the bytes and put it in a new array
            byte[] encryptedBytes = aesCipher.doFinal(rawMessage);

            //Return the bytes array as a string
            return bytesToBase64(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method will decrypt your encrypted message using your AES key
     *
     * @param cypherText The encrypted message
     * @return The decrypted message
     */
    public String decryptMessage(String cypherText) {
        try {
            //Change the string to bytes array
            byte[] rawCipher = base64ToBytes(cypherText);

            //Select the encryption and key
            aesCipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            //Decrypt the bytes and put it in a new array
            byte[] decryptedBytes = aesCipher.doFinal(rawCipher);

            //Return the bytes array as a string
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method returns a byte array from a Base64 string
     *
     * @param base64String The string you want to decode
     * @return The decoded Base64 byte array
     */
    private byte[] base64ToBytes(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }

    /**
     * This method returns a Base64 string from a byte array
     *
     * @param bytes The byte array you want to encode
     * @return The encoded Base64 string
     */
    private String bytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * This method returns your public key as a Base64 encoded string
     * You can use this for sending your public key to other users
     *
     * @return The Base64 encoded string of your public key
     */
    public String getPublicKey() {
        return bytesToBase64(rsaKeys.getPublic().getEncoded());
    }
}
