import { HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable()
export class EmployeePortalInterceptor implements HttpInterceptor {

    constructor() { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const token: string = sessionStorage.getItem('token');
        
        if (token) {
            request = request.clone({
                setHeaders: { 
                    Authorization : `Bearer ${token}`
                }
            });
        }

        return next.handle(request);
    }
    
}