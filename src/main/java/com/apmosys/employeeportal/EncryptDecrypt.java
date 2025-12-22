package com.apmosys.employeeportal;

import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

@Component
public class EncryptDecrypt {
	
	public final static String key = "PkdtRsJidheGitvS";
	private static final String ALGORITHM = "AES";
    private static final String SECRET = "my-secret-key";

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
		String data = EncryptDecrypt.decrypt("YYMPVO/ZoyuhVi+4S/aQ7w==");
		System.out.println(data+"   :Decrypted data");
	}
	
	
	 private static SecretKeySpec getKey() {
	        return new SecretKeySpec(SECRET.getBytes(), ALGORITHM);
	    }

	    public static String encryptOtp(String otp) {
	        try {
	            MessageDigest digest = MessageDigest.getInstance("SHA-256"); // secure one-way hash
	            byte[] hash = digest.digest(otp.getBytes());
	            return Base64.getEncoder().encodeToString(hash);
	        } catch (Exception e) {
	            throw new RuntimeException("Error encrypting OTP", e);
	        }
	    }

	    public static String decryptOtp(String encryptedOtp) {
	        try {
	            Cipher cipher = Cipher.getInstance(ALGORITHM);
	            cipher.init(Cipher.DECRYPT_MODE, getKey());
	            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedOtp)));
	        } catch (Exception e) {
	            throw new RuntimeException("Error decrypting OTP", e);
	        }
	    }
}
