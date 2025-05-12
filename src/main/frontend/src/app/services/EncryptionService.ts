import * as CryptoJS from 'crypto-js';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class EncryptionService {
  private secretKey = CryptoJS.enc.Utf8.parse('1234567890123456'); // Use a proper key

  constructor() {}

  // Encrypt method (URL-safe)
  encrypt(value: string): string {
    const encrypted = CryptoJS.AES.encrypt(value, this.secretKey, {
      mode: CryptoJS.mode.ECB,
      padding: CryptoJS.pad.Pkcs7
    }).toString();

    return this.base64ToUrlSafe(encrypted); // Convert to URL-safe format
  }

  // Decrypt method (URL-safe)
  decrypt(encryptedText: string): string {
    try {
      const urlSafeBase64 = this.urlSafeToBase64(encryptedText); // Convert back to normal Base64
      const bytes = CryptoJS.AES.decrypt(urlSafeBase64, this.secretKey, {
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7
      });

      return bytes.toString(CryptoJS.enc.Utf8);
    } catch (error) {
      console.error('Decryption error:', error);
      return '';
    }
  }

  // Convert Base64 to URL-Safe Base64
  private base64ToUrlSafe(base64: string): string {
    return base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  }

  // Convert URL-Safe Base64 back to normal Base64
  private urlSafeToBase64(urlSafe: string): string {
    let base64 = urlSafe.replace(/-/g, '+').replace(/_/g, '/');
    while (base64.length % 4) {
      base64 += '='; // Pad with '=' if necessary
    }
    return base64;
  }
}
