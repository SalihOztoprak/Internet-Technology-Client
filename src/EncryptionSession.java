import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class EncryptionSession {
    private SecretKey aesKey;
    private Cipher aesCipher;
    private KeyPair rsaKeys;
    private Cipher rsaCipher;
    private IvParameterSpec iv;
    private SecretKeySpec skeySpec;

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

    private byte[] base64ToBytes(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }

    private String bytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public String getPublicKey() {
        return bytesToBase64(rsaKeys.getPublic().getEncoded());
    }
}
