package com.ct.ti;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class encode {

	private static final String sKey = "00b09e37363e596e1f25b23c78e49939238b";

	public static Cipher initAESCipher(int cipherMode) {

		KeyGenerator keyGenerator = null;
		Cipher cipher = null;
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128, new SecureRandom(sKey.getBytes()));
			SecretKey secretKey = keyGenerator.generateKey();
			byte[] codeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(codeFormat, "AES");
			cipher = Cipher.getInstance("AES");
			cipher.init(cipherMode, key);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return cipher;
	}

	public static File encryptFile(File sourceFile, File encrypfile) {
 
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = new FileInputStream(sourceFile);
 
			outputStream = new FileOutputStream(encrypfile);
			
			Cipher cipher = initAESCipher(Cipher.ENCRYPT_MODE);
			CipherInputStream cipherInputStream = new CipherInputStream(
					inputStream, cipher);
			byte[] cache = new byte[1024];
			int nRead = 0;
			while ((nRead = cipherInputStream.read(cache)) != -1) {
				outputStream.write(cache, 0, nRead);
				outputStream.flush();
			}
			cipherInputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return encrypfile;
	}
	public static void main(String[] args) throws Exception {

		File oldfile = new File("microlog.txt");
		//���ܺ���ļ�·��
		File encrypfile = new File("encode.txt");
		//���ܺ���ļ�·��
		encryptFile(oldfile,encrypfile);
	}
 
}
