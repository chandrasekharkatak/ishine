import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { User } from '../models/user';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private baseUrl: any = environment.baseUrl;
  private currentUserSubject: BehaviorSubject<User>;
  public currentUser: Observable<User>;
  sessionItem: string | null;
  timerId: any;
  sessionString: string;
  sessionTimeout:number;

  constructor(private http: HttpClient, private router: Router) {
    // this.sessionItem = sessionStorage.getItem('currentUser');
    // this.currentUserSubject = new BehaviorSubject<User>( this.sessionItem !== null ? JSON.parse(this.sessionItem): {});
    this.sessionItem = sessionStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<User>(JSON.parse(this.sessionItem));
    this.currentUser = this.currentUserSubject.asObservable();
    this.sessionString = sessionStorage.getItem('token');
  }

  public get currentUserValue(): User {
    return this.currentUserSubject.value;
  }

  setcurrentUserSubject(user: User) {
    sessionStorage.setItem('currentUser', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  authenticateUser(user: User) {
    return this.http.post(`${this.baseUrl}` + `api/authenticateUser`, user);
  }

  authenticateUserWithOTP(user: User) {
    return this.http.post(`${this.baseUrl}` + `api/authenticateUserWithOTP`, user);
  }

  checkUserSession(user: User) {
    return this.http.post(`${this.baseUrl}` + `api/checkUserSession`, user);
  }

  logoutUser(user: User) {
    return this.http.post(`${this.baseUrl}` + `api/logoutUser`, user);
  }

  checkEmailWhenForgotPassword(user: User) {
    return this.http.post(`${this.baseUrl}` + `api/checkEmailWhenForgotPassword`, user);
  }

  checkOTPWhenForgotPassword(user: User) {
    return this.http.post(`${this.baseUrl}` + `api/checkOTPWhenForgotPassword`, user);
  }

  resendOTP(user: User) {
    return this.http.post(`${this.baseUrl}` + `api/resendOTP`, user);
  }

  /* 
  *  Cron to check if user session exists. every 20 seconds
  *  Added by suraj 12/08/2022
  */
  startUserSessionCheck() {
    this.timerId = setInterval(() => {
      let user = new User();
      user.empId = this.currentUserValue.empId;
      user.sessionString = this.sessionString;
      this.checkUserSession(user).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          //do nothing
        } else {
        console.error(response.serviceResponse);
        this.stopUserSessionCheck();
        this.userLogout();   
        }
      });
    }, 20000);
  }

  stopUserSessionCheck() {
    clearInterval(this.timerId);
  }

  userLogout(){
    sessionStorage.removeItem('currentUser');
    sessionStorage.removeItem('token');
    location.reload();
    this.router.navigate(['/login']);    
  }

 

  // implemnting cookie in code by anurag 

    getCookie(name:string){
    let ca:Array<string> =document.cookie.split(';');
    console.log(document.cookie);
    let caLen:number= ca.length;
    let cookieName=`${name}=`;
    let c :string;
    for (let i: number = 0; i < caLen; i += 1) {
      c = ca[i].replace(/^\s+/g, '');
      if (c.indexOf(cookieName) == 0) {
        return c.substring(cookieName.length, c.length);
      }
    }
    return '';
    
  }
        deleteCookies(){
          document.cookie.split(";").forEach(function (c) {
            document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
          });
        }

    setCookie(params: any) {
    let d: Date = new Date();
    d.setTime(
      d.getTime() +
        (params.expireDays ? params.expireDays : 1) * 24 * 60 * 60 * 1000
    );
    document.cookie =
      (params.name ? params.name : '') +
      '=' +
      (params.value ? params.value : '') +
      ';' +
      (params.session && params.session == true
        ? ''
        : 'expires=' + d.toUTCString() + ';') +
      'path=' +
      (params.path && params.path.length > 0 ? params.path : '/') +
      ';' +
      (location.protocol === 'https:' && params.secure && params.secure == true
        ? 'secure'
        : '');
  }

}
