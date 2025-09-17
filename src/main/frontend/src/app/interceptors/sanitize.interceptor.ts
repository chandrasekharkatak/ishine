import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { SanitizerService } from '../services/sanitizer.service';

@Injectable()
export class SanitizeInterceptor implements HttpInterceptor {
  constructor(private sanitizer: SanitizerService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // ✅ Sanitize request body before sending
    let sanitizedBody = this.sanitizer.sanitizeInput(req.body);

    const clonedReq = req.clone({
      body: sanitizedBody
    });

    return next.handle(clonedReq).pipe(
      map(event => {
        if (event instanceof HttpResponse) {
          // ✅ Sanitize incoming response body before rendering
          const sanitizedResponse = this.sanitizer.sanitizeInput(event.body);
          return event.clone({ body: sanitizedResponse });
        }
        return event;
      })
    );
  }
}
