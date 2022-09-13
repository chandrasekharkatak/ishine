import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';

@Injectable()
export class EmployeePortalInterceptor implements HttpInterceptor {

    currentUser:User;

    constructor(private authenticationService: AuthenticationService) { 
        this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

        if(this.currentUser){
            request = request.clone({
                setHeaders: { 
                    currentUser: `id: ${this.currentUser.employeementId}, name : ${this.currentUser.name}`
                }
            });
        }

        return next.handle(request);
    }
    
}