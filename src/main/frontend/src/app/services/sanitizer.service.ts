import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SanitizerService {

  sanitizeInput(value: any): any {
    if (typeof value === 'string') {
      return value
        .replace(/<script.*?>.*?<\/script>/gi, '')   // remove <script> tags
        .replace(/on\w+=".*?"/gi, '')                // remove inline event handlers
        .replace(/javascript:/gi, '');               // block javascript: URLs
    } else if (typeof value === 'object' && value !== null) {
      // recursively sanitize objects
      for (const key in value) {
        if (Object.prototype.hasOwnProperty.call(value, key)) {
          value[key] = this.sanitizeInput(value[key]);
        }
      }
    }
    return value;
  }
}
