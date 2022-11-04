import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Log } from '../models/log';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { LogService } from '../services/log.service';

@Injectable()
export class EmployeePortalInterceptor implements HttpInterceptor {

    currentUser:User;
    log:Log;

    constructor(
        private authenticationService: AuthenticationService,
        private logService:LogService
    ) { 
        this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
        this.logService.log.subscribe(x => this.log = x);
    }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // if(this.currentUser){
        //     let body = {};
        //     const regex = /\/api\/[a-zA-Z]+/gm
        //     const match = request.url.match(regex);
        //     console.log("API URL : ", match);
        //     console.log("Log INFO ", this.log);
        //     if (match) this.log.apiUrl = match[0];
        //     if (body) request.body;
        //     body['log'] = this.log;

        //     console.log("Updated Request Body : ", body);

        //     request = request.clone({
        //         body: body
        //     });
        // }

        return next.handle(request);
    }
    
}