package com.rigobertocanseco.example.jce;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class EncryptionTool {
    private final static int ITERATIONS = 65536 ;
    private final static int KEY_SIZE = 256;

    /**
     * Class EncryptionToolException
     */
    static class EncryptionToolException extends Exception {
        public EncryptionToolException(String message, Throwable cause){
            super(message, cause);
        }
    }

    /**
     * Class Message
     */
    static class Message {
        private byte[] message;

        public Message(byte[] message){
            this.message = Base64.encodeBase64(message);
        }

        public Message(String message)  {
            try {
                this.message = message.getBytes("UTF-8");
            } catch (Exception ex) {
                this.message = null;
            }
        }

        public byte[] getMessage() {
            return message;
        }

        public void setMessage(byte[] message) {
            this.message = message;
        }

        public char[] toCharArray() {
            return new String(this.message).toCharArray();
        }

        public String encode64() throws EncryptionToolException {
            try {
                return new String(Base64.encodeBase64(this.message));
            }catch (Exception ex){
                throw new EncryptionToolException("Bytes to Base64 failed:" + ex.getMessage(), ex);
            }
        }

        public String decode64() throws EncryptionToolException {
            try {
                return new String(Base64.decodeBase64(this.message));
            }catch (Exception ex){
                throw new EncryptionToolException("Bytes to Base64 failed:" + ex.getMessage(), ex);
            }
        }

        @Override
        public String toString() {
            return "Message{" + "message=" + new String(this.message) + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Message)) return false;
            Message message1 = (Message) o;
            return Arrays.equals(getMessage(), message1.getMessage());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(getMessage());
        }
    }

    /**
     * Class AES
     */
    static final class AES {

        /**
         * Genera una llave
         * @return
         * @throws EncryptionTool.EncryptionToolException Key generator failed
         */
        public static byte[] getKey() throws EncryptionTool.EncryptionToolException {
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(KEY_SIZE, new SecureRandom());
                int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
                System.out.println("MAX LENGTH KEY AES: " + maxKeyLen);

                return keyGenerator.generateKey().getEncoded();
            }catch (Exception ex){
                throw new EncryptionTool.EncryptionToolException("Key generator failed:" + ex.getMessage(), ex);
            }
        }

        /**
         * Genera un salt aleatorio
         * @return byte[]
         */
        public static byte[] getSalt() {
            SecureRandom sr = new SecureRandom();// SHA1PRNG
            byte[] salt = new byte[20];
            sr.nextBytes(salt);
            return salt;
        }

        /**
         * Genera un IV aleatorio
         * @return byte[]
         */
        public static byte[] getIV() {
            SecureRandom secureRandom = new SecureRandom();
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);

            return iv;
        }

        /**
         * Cifra un string con algoritmo AES
         * @param secret String
         * @param iv String
         * @param salt String
         * @param string String
         * @return Retorna un Message encode64
         * @throws EncryptionToolException AES encrypt failed
         */
        public static String encrypt(String secret, String iv, String salt, String string) throws EncryptionToolException {
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec spec = new PBEKeySpec(secret.toCharArray(), Base64.decodeBase64(salt.getBytes()), ITERATIONS, KEY_SIZE);
                SecretKey tmp = factory.generateSecret(spec);
                SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(Base64.decodeBase64(iv.getBytes())));

                return new String(Base64.encodeBase64(cipher.doFinal(string.getBytes("UTF-8"))));
            } catch (Exception ex) {
                throw new EncryptionToolException("AES encrypt failed:" + ex.getMessage(), ex);
            }
        }

        /**
         * Descifra un string con algoritmo AES
         * @param secret String
         * @param iv String
         * @param salt String
         * @param string String
         * @return String
         * @throws EncryptionToolException AES decrypt failed
         */
        public static String decrypt(String secret, String iv, String salt, String string) throws EncryptionToolException {
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec spec = new PBEKeySpec(secret.toCharArray(), Base64.decodeBase64(salt.getBytes()), ITERATIONS, KEY_SIZE);
                SecretKey tmp = factory.generateSecret(spec);
                SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(Base64.decodeBase64(iv.getBytes())));

                return new String(cipher.doFinal(Base64.decodeBase64(string.getBytes())));
            } catch (Exception ex) {
                throw new EncryptionToolException("AES decrypt failed:" + ex.getMessage(), ex);
            }
        }

    }

    /**
     * Agregar un proveedor criptografico
     * @param provider Provider
     */
    public static void addProvider(Provider provider){
        Security.addProvider(provider);
    }

    /**
     * Genera una llave AES 256
     * @return SecretKey
     * @throws EncryptionToolException, Key Generator failed
     */
    public static SecretKey keyGenerator() throws EncryptionToolException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(KEY_SIZE, new SecureRandom());
            int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
            System.out.println("MAX LENGTH KEY AES: " + maxKeyLen);

            return keyGenerator.generateKey();
        }catch (Exception ex){
            throw new EncryptionToolException("Key generator failed:" + ex.getMessage(), ex);
        }
    }

    /**
     * Genera un par de llaves DSA
     * @return KeyPair
     * @throws EncryptionToolException Key pair generator failed
     */
    public static KeyPair keyPairGenerator() throws EncryptionToolException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");

            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new EncryptionToolException("Key pair generator failed:" + ex.getMessage(), ex);
        }
    }

    /**
     * Encode Base64
     * @param bytes Bytes
     * @return String
     * @throws EncryptionToolException Bytes to Base64 failed
     */
    public static String encode64(byte[] bytes) throws EncryptionToolException {
        try {
            return new String(Base64.encodeBase64(bytes));
        } catch (Exception ex){
            throw new EncryptionToolException("Bytes to encode64 failed:" + ex.getMessage(), ex);
        }
    }

    /**
     * Decode Base64
     * @param bytes Bytes
     * @return String
     * @throws EncryptionToolException Bytes to Base64 failed
     */
    public static String decode64(byte[] bytes) throws EncryptionToolException {
        try {
            return new String(Base64.decodeBase64(bytes));
        }catch (Exception ex){
            throw new EncryptionToolException("Bytes to decode64 failed:" + ex.getMessage(), ex);
        }
    }

    /**
     * Genera MD5
     * @param message Message
     * @return Message
     * @throws EncryptionToolException Bytes to MD5 failed
     */
    public static Message messageToMD5(Message message) throws EncryptionToolException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            return new Message(messageDigest.digest(message.getMessage()));
        }catch (Exception ex){
            throw new EncryptionToolException("Bytes to MD5 failed:" + ex.getMessage(), ex);
        }
    }

    /**
     * Genera SHA-256
     * @param message Message
     * @return Message
     * @throws EncryptionToolException Bytes to SHA-256 failed
     */
    public static Message messageToSHA256(Message message) throws EncryptionToolException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            return new Message(messageDigest.digest(message.getMessage()));
        }catch (Exception ex){
            throw new EncryptionToolException("Bytes to SHA-256 failed:" + ex.getMessage(), ex);
        }
    }

    /**
     * Genera SHA-512
     * @param message Message
     * @return Message
     * @throws EncryptionToolException Bytes to ShA-512 failed
     */
    public static Message messageToSHA512(Message message) throws EncryptionToolException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            byte[] digest = messageDigest.digest(message.getMessage());

            return new Message(digest);
        }catch (Exception ex){
            throw new EncryptionToolException("Bytes to SHA-512 failed:" + ex.getMessage(), ex);
        }
    }

    /**
     * Genera HMAC-SHA256
     * @param key Key
     * @param message Message
     * @return Message
     * @throws EncryptionToolException HMAC-SHA256 generator failed
     */
    public static Message generateHmacSHA256(Key key, Message message) throws EncryptionToolException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            byte[] keyBytes   = new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
            String algorithm  = "RawBytes";
            SecretKeySpec key2 = new SecretKeySpec(keyBytes, algorithm);
            mac.init(key2);

            return new Message(mac.doFinal(message.getMessage()));
        }catch (Exception ex){
            throw new EncryptionToolException("HMAC-256 generator failed:" + ex.getMessage(), ex);
        }
    }


    public static void main(String[] args) throws Exception {
        String originalString = "hola mundo";
        byte[] key = EncryptionTool.AES.getKey();
        byte[] iv = EncryptionTool.AES.getIV();
        byte[] salt = EncryptionTool.AES.getSalt();

        System.out.println(EncryptionTool.encode64(key));
        System.out.println(EncryptionTool.encode64(iv));
        System.out.println(EncryptionTool.encode64(salt));


        String encryptedString = EncryptionTool.AES.encrypt(EncryptionTool.encode64(key), EncryptionTool.encode64(iv),
                EncryptionTool.encode64(salt), originalString);
        System.out.println(encryptedString);

        String decryptedString = EncryptionTool.AES.decrypt(EncryptionTool.encode64(key), EncryptionTool.encode64(iv),
                EncryptionTool.encode64(salt), encryptedString);

        System.out.println(decryptedString);
    }
}
