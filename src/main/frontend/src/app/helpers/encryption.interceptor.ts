import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as CryptoJS from 'crypto-js';

@Injectable()
export class EncryptionInterceptor implements HttpInterceptor {

  private readonly KEY1 = CryptoJS.enc.Utf8.parse('msoe837%)ks&!6eb');
  private readonly KEY2 = CryptoJS.enc.Utf8.parse('p10Cu&@m3idh9so5');

  // List of endpoints to encrypt/decrypt
  private readonly SECURE_ENDPOINTS: string[] = [
    '/api/authenticateUser',
    '/api/authenticateUserWithOTP',
    '/api/checkUserSession',
    '/api/logoutUser',
    '/api/checkEmailWhenForgotPassword',
    '/api/checkOTPWhenForgotPassword',
    '/api/resendOTP'
  ];

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let encryptedReq = req;

    // 🔒 Encrypt request body if it's a secure endpoint
    if (this.isSecureEndpoint(req.url) && req.body) {
      const salt = Date.now(); // current time in ms
      const encryptedData = this.encrypt(JSON.stringify(req.body), salt);

      encryptedReq = req.clone({
        body: { encryptedData, salt }
      });
    }

    return next.handle(encryptedReq).pipe(
      map(event => {
        if (event instanceof HttpResponse && event.body && this.isSecureEndpoint(req.url)) {
          try {
            const body = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;

            const encrypted = body.encryptedData;
            const salt = body.salt;

            if (!encrypted || !salt) {
              console.warn('Missing encryptedData or salt, skipping decryption');
              return event;
            }

            // ⏱️ Validate salt: must be <= current time and within 10s
            const currentTs = Date.now();
            if (salt > currentTs) {
              throw new Error(`Invalid salt: future timestamp detected`);
            }
            const diff = currentTs - salt;
            if (diff > 10000) {
              throw new Error(`Invalid salt: expired (${diff}ms old)`);
            }

            const decrypted = this.decrypt(encrypted);
            console.log('Decrypted raw string:', decrypted);

            // Remove appended salt from decrypted string
            const lastPipeIndex = decrypted.lastIndexOf('|');
            let cleanDecrypted = decrypted;
            if (lastPipeIndex >= 0) {
              cleanDecrypted = decrypted.substring(0, lastPipeIndex);
            }

            // Try parsing decrypted string as JSON, fallback to string
            let parsedBody: any;
            try {
              parsedBody = JSON.parse(cleanDecrypted);
            } catch (e) {
              parsedBody = cleanDecrypted;
            }

            return event.clone({ body: parsedBody });
          } catch (e) {
            console.error('Response decryption failed:', e);
            return event;
          }
        }
        return event;
      })
    );
  }

  private isSecureEndpoint(url: string): boolean {
    const cleanedUrl = new URL(url, window.location.origin).pathname;
    return this.SECURE_ENDPOINTS.includes(cleanedUrl);
  }

  private encrypt(plainText: string, salt: number): string {
    const saltedPlainText = plainText + '|' + salt;

    const encrypted = CryptoJS.AES.encrypt(
      CryptoJS.enc.Utf8.parse(saltedPlainText),
      this.KEY1,
      {
        keySize: 128 / 8,
        iv: this.KEY2,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
      }
    );

    return encrypted.toString();
  }

  private decrypt(cipherText: string): string {
    const decrypted = CryptoJS.AES.decrypt(cipherText, this.KEY1, {
      keySize: 128 / 8,
      iv: this.KEY2,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    });
    return decrypted.toString(CryptoJS.enc.Utf8);
  }
}
