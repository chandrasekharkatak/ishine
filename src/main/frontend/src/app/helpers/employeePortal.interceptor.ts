import { HttpErrorResponse, HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { AuthenticationService } from '../services/authentication.service';
import { Router } from '@angular/router';

@Injectable()
export class EmployeePortalInterceptor implements HttpInterceptor {

    constructor(
        private authenticationService : AuthenticationService,
        private router: Router
    ) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const token: string = sessionStorage.getItem('token');
        
        if (token) {
            request = request.clone({
                setHeaders: { 
                    Authorization : `Bearer ${token}`
                }
            });
        }

        return next.handle(request).pipe(
            tap(evt => {
                // Do Nothing
            }),
            catchError((err: any) => {
                if(err instanceof HttpErrorResponse) {
                    if(err.status == 401)
                        this.userLogout();
                }
                return of(err);
            }));;
    }
    
    userLogout(){
        this.authenticationService.stopUserSessionCheck();
          sessionStorage.removeItem('currentUser');
          sessionStorage.removeItem('token');
          sessionStorage.removeItem('logInfo');
          sessionStorage.removeItem('maxFileSize');
          sessionStorage.removeItem('maxRequestSize');
          sessionStorage.removeItem('sessioncheck');
          // delete method call for cookies
          this.authenticationService.deleteCookies();
          this.authenticationService.setcurrentUserSubject(null);
          this.router.navigate(['/login']);
          setTimeout(() => {location.reload();});   
      }
}