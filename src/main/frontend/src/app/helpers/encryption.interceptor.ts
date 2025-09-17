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

    // Encrypt request body
    if (this.isSecureEndpoint(req.url) && req.body) {
      const salt = Date.now();
      const encryptedData = this.encrypt(JSON.stringify(req.body), salt);

      encryptedReq = req.clone({
        body: { encryptedData } // salt is inside the encrypted data now
      });
    }

    return next.handle(encryptedReq).pipe(
      map(event => {
        if (event instanceof HttpResponse && event.body && this.isSecureEndpoint(req.url)) {
          try {
            const body = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;
            const encrypted = body.encryptedData;
            if (!encrypted) {
              console.warn('Missing encryptedData in response');
              return event;
            }

            const decrypted = this.decrypt(encrypted);
            console.log('Decrypted raw string:', decrypted);

            // Extract plaintext + salt
            const lastPipeIndex = decrypted.lastIndexOf('|');
            if (lastPipeIndex < 0) throw new Error('Invalid decrypted format');

            const cleanDecrypted = decrypted.substring(0, lastPipeIndex);
            const salt = parseInt(decrypted.substring(lastPipeIndex + 1), 10);
            const currentTs = Date.now();

            // Validate salt
            if (salt > currentTs) throw new Error(`Future timestamp detected`);
            if (currentTs - salt > 10000) throw new Error(`Response expired (${currentTs - salt}ms old)`);

            let parsedBody: any;
            try {
              parsedBody = JSON.parse(cleanDecrypted);
            } catch {
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
