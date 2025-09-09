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

  // must match your backend keys
  private readonly KEY1 = CryptoJS.enc.Utf8.parse("msoe837%)ks&!6eb");
  private readonly KEY2 = CryptoJS.enc.Utf8.parse("p10Cu&@m3idh9so5");

  // Add the list of URLs that need encryption
  private readonly SECURE_ENDPOINTS: string[] = [
    '/api/authenticateUser',
  ];

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let encryptedReq = req;

    // 🔒 Check if request URL matches secure endpoints
    if (this.isSecureEndpoint(req.url) && req.body) {
      const encryptedData = this.encrypt(JSON.stringify(req.body));
      encryptedReq = req.clone({
        body: { encryptedData }
      });
    }

    return next.handle(encryptedReq).pipe(
  map(event => {
    if (event instanceof HttpResponse && event.body && this.isSecureEndpoint(req.url)) {
      try {
        // backend always sends { encryptedData: "..." }
        const encrypted = JSON.parse(event.body).encryptedData;
        console.log('Encrypted response data:', encrypted);

        const decrypted = this.decrypt(encrypted); // returns a string
        console.log('Decrypted response data (raw):', decrypted);

        // try parsing if it's valid JSON, else return as string
        let parsedBody: any;
        try {
          parsedBody = JSON.parse(decrypted);
        } catch (e) {
          console.warn('Decrypted data is not JSON, returning as string');
          parsedBody = decrypted;
        }

        return event.clone({ body: parsedBody });
      } catch (e) {
        console.error('Response decryption failed:', e);
        return event; // fallback for plain responses
      }
    }
    return event;
  })
);
  }

  // helper: check if request URL matches secure endpoints
  private isSecureEndpoint(url: string): boolean {
  const cleanedUrl = new URL(url, window.location.origin).pathname;
  return this.SECURE_ENDPOINTS.includes(cleanedUrl);
}

  private encrypt(plainText: string): string {
    const encrypted = CryptoJS.AES.encrypt(
      CryptoJS.enc.Utf8.parse(plainText),
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
    console.log('Cipher text to decrypt:', cipherText);
    const decrypted = CryptoJS.AES.decrypt(cipherText, this.KEY1, {
      keySize: 128 / 8,
      iv: this.KEY2,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    });
    return decrypted.toString(CryptoJS.enc.Utf8);
  }
}
