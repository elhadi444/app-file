package com.example.myapplication;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class EncryptionUtil {

    private static final String BASE64_KEY = "WmXyT0+LlFc5h5NCE3B0Mg==";


    public static SecretKey getHardCodedKey() {
        byte[] decodedKey = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            decodedKey = Base64.getDecoder().decode(BASE64_KEY);
        }
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }


//    public static SecretKey generateKey() throws NoSuchAlgorithmException {
//        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
//        keyGenerator.init(128);
//        return keyGenerator.generateKey();
//    }



    public static byte[] encryptBytes(byte[] fileBytes, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(byteArrayOutputStream, cipher);
        cipherOutputStream.write(fileBytes);
        cipherOutputStream.flush();
        cipherOutputStream.close();

        byte[] encryptedData = byteArrayOutputStream.toByteArray();
        byte[] ivAndEncryptedData = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, ivAndEncryptedData, 0, iv.length);
        System.arraycopy(encryptedData, 0, ivAndEncryptedData, iv.length, encryptedData.length);

        return ivAndEncryptedData;
    }


    public static void decryptFile(byte[] encryptedData, SecretKey key, File outputFile) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        byte[] iv = Arrays.copyOfRange(encryptedData, 0, 16);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(encryptedData, 16, encryptedData.length - 16);
             CipherInputStream cipherInputStream = new CipherInputStream(byteArrayInputStream, cipher);
             FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}

