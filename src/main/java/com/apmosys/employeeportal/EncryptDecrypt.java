package com.apmosys.employeeportal;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

@Component
public class EncryptDecrypt {
	
	public final static String key = "PkdtRsJidheGitvS";

	public static String encrypt(String encrypted) throws Exception{
		if(encrypted.equals("") || encrypted==null) {
			return "";
		}
		
		IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
		SecretKeySpec skey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skey, iv);
		byte[] pbites = cipher.doFinal(encrypted.getBytes());
		String encryptedPassword = new String(Base64.getEncoder().encode(pbites), "UTF-8");
		
		return encryptedPassword;
	}
	
	public static String decrypt(String encrypted) throws Exception{
		
		IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
		SecretKeySpec skey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skey, iv);
		byte[] password = cipher.doFinal(Base64.getDecoder().decode(encrypted));
		String OriginalPassword = new String(password);
		
		return OriginalPassword;
	}
	public static void main(String[] args) throws Exception {
		String data = EncryptDecrypt.decrypt("mNwMTvXs8PA2Z1Mvg2BMCw==");
		System.out.println(data+"   :Decrypted data");
	}
}
