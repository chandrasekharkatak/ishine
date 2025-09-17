import * as CryptoJS from 'crypto-js';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class EncryptionService {
  
  private readonly KEY1 = CryptoJS.enc.Utf8.parse("msoe837%)ks&!6ebabcdefg");
  private readonly KEY2 = CryptoJS.enc.Utf8.parse("p10Cu&@m3idh9so5ksgteghdhs");

  encrypt(plainText: string): string {
  const key = CryptoJS.enc.Utf8.parse(this.KEY1);
  const iv = CryptoJS.enc.Utf8.parse(this.KEY2);

  const encrypted = CryptoJS.AES.encrypt(
    CryptoJS.enc.Utf8.parse(plainText),
    key,
    {
      keySize: 128 / 8,
      iv: iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    }
  );

  return encrypted.toString();
}


  decrypt(cipherText: string): string | null {
  if (!cipherText || cipherText.trim() === '') return null;

  const key = CryptoJS.enc.Utf8.parse(this.KEY1);
  const iv = CryptoJS.enc.Utf8.parse(this.KEY2);

  try {
    const decrypted = CryptoJS.AES.decrypt(cipherText, key, {
      keySize: 128 / 8,
      iv: iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    });

    const plaintext = decrypted.toString(CryptoJS.enc.Utf8);
    if (!plaintext) throw new Error('Decryption failed (empty string)');

    return plaintext;
  } catch (err) {
    console.error('Decryption failed:', err);
    return null;
  }
}

}
