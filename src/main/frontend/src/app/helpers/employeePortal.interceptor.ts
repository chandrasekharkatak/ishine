import { HttpErrorResponse, HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { catchError, first, tap } from 'rxjs/operators';
import { AuthenticationService } from '../services/authentication.service';
import { Router } from '@angular/router';
import { LoaderService } from '../services/loader.service';
import { User } from '../models/user';

@Injectable()
export class EmployeePortalInterceptor implements HttpInterceptor {
currentUser:User = new User();
    constructor(
        private authenticationService : AuthenticationService,private loaderService: LoaderService,
        private router: Router
    ) { 
        this.authenticationService.currentUser.subscribe(x => {
      this.currentUser = x;
    });
    }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

        if(request.url.startsWith("https")){
            return next.handle(request);
        }

        const token: string = sessionStorage.getItem('token');
        const empId: string = this.authenticationService.currentUserValue ? this.authenticationService.currentUserValue.empId : ''; 
        
        if (token) {
            request = request.clone({
                setHeaders: { 
                    Authorization : `Bearer ${token}|${empId}`
                }
            });
        }

        return next.handle(request).pipe(
            tap(evt => {
                // Do Nothing
            }),
            catchError((err: any) => {
                if (err instanceof HttpErrorResponse) {
                    if (err.status == 401)
                        this.userLogout();
                    else if (err.status === 500) {
                        this.loaderService.resetSpinner();
                    }
                    else if (err.status === 403) {
                        this.loaderService.resetSpinner();
                        this.router.navigate(['/home']); // access denied
                    }
                }
                // Rethrow so subscriber's error callback runs and LoaderInterceptor can stop the loader
                return throwError(() => err);
            })
        );
    }
    
    userLogout(){
        let user = new User();
        user.empId = this.currentUser.empId;
            this.authenticationService.logoutUser(user).pipe(first()).subscribe((response: any) => {
              if (response.serviceStatus == "Success") {
                this.authenticationService.stopUserSessionCheck();
                //console.log(response.serviceResponse);
                  sessionStorage.removeItem('currentUser');
                  sessionStorage.removeItem('token');
                  sessionStorage.removeItem('logInfo');
                  sessionStorage.removeItem('maxFileSize');
                  sessionStorage.removeItem('maxRequestSize');
                  sessionStorage.removeItem('sessioncheck');
                  sessionStorage.removeItem('breadcrumb');
                // delete method call for cookies
                this.authenticationService.deleteCookies();
                this.authenticationService.setcurrentUserSubject(null);
                this.router.navigate(['/login']);
                setTimeout(() => {location.reload();});
              }
        },);

        // this.authenticationService.stopUserSessionCheck();
        //   sessionStorage.removeItem('currentUser');
        //   sessionStorage.removeItem('token');
        //   sessionStorage.removeItem('logInfo');
        //   sessionStorage.removeItem('maxFileSize');
        //   sessionStorage.removeItem('maxRequestSize');
        //   sessionStorage.removeItem('sessioncheck');
        //   sessionStorage.removeItem('breadcrumb');
        //   // delete method call for cookies
        //   this.authenticationService.deleteCookies();
        //   this.authenticationService.setcurrentUserSubject(null);
        //   this.router.navigate(['/login']);
        //   setTimeout(() => {location.reload();});   
      }
}