import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, Subscription, interval, observable, timer } from 'rxjs';
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

  sessionSubscription:Subscription;

  constructor(private http: HttpClient, private router: Router) {
    // this.sessionItem = sessionStorage.getItem('currentUser');
    // this.currentUserSubject = new BehaviorSubject<User>( this.sessionItem !== null ? JSON.parse(this.sessionItem): {});
    this.sessionItem = sessionStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<User>(JSON.parse(this.sessionItem));
    this.currentUser = this.currentUserSubject.asObservable();
    this.sessionString = sessionStorage.getItem('token');
    let sessionCheck = sessionStorage.getItem('sessioncheck');

    if(sessionCheck){
      this.startUserSessionCheck();
    }
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

  checkSession(){
    let user = new User();
    user.empId = this.currentUserValue.empId;
    user.sessionString = this.sessionString;

    //console.log("checking session ..", new Date().toTimeString());
    
      this.checkUserSession(user).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          //do nothing
        } else {
        console.error(response.serviceResponse);
        this.stopUserSessionCheck();
        // this.userLogout();   
        }
      });
  }

  startUserSessionCheck() {
    sessionStorage.setItem('sessioncheck', 'true');
    this.sessionSubscription = timer(0,30000).subscribe(() =>  {
      this.checkSession();
    });
  }

  stopUserSessionCheck() {
    if(this.sessionSubscription)
      this.sessionSubscription.unsubscribe();
  }

  userLogout(){
    this.stopUserSessionCheck();
    sessionStorage.removeItem('currentUser');
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('logInfo');
    sessionStorage.removeItem('maxFileSize');
    sessionStorage.removeItem('maxRequestSize');
    sessionStorage.removeItem('sessioncheck');
    sessionStorage.removeItem('breadcrumb');
    // delete method call for cookies
    this.deleteCookies();
    this.setcurrentUserSubject(null);
    this.router.navigate(['/login']);
    setTimeout(() => {location.reload();});   
  }

 

  // implemnting cookie in code by anurag 

    getCookie(name:string){
    let ca:Array<string> =document.cookie.split(';');
    //console.log(document.cookie);
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
