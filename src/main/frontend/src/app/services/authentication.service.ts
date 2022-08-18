import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private baseUrl: any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";
  private currentUserSubject: BehaviorSubject<User>;
  public currentUser: Observable<User>;
  sessionItem: string | null;
  timerId: any;
  sessionString: string;

  constructor(private http: HttpClient, private router: Router) {
    // this.sessionItem = sessionStorage.getItem('currentUser');
    // this.currentUserSubject = new BehaviorSubject<User>( this.sessionItem !== null ? JSON.parse(this.sessionItem): {});
    this.sessionItem = sessionStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<User>(JSON.parse(this.sessionItem));
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User {
    return this.currentUserSubject.value;
  }

  setcurrentUserSubject(user: User) {
    this.currentUserSubject.next(user);
  }

  authenticateUser(user: User) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/authenticateUser`, user);
  }

  authenticateUserWithOTP(user: User) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/authenticateUserWithOTP`, user);
  }

  checkUserSession(user: User) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/checkUserSession`, user);
  }

  logoutUser(user: User) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/logoutUser`, user);
  }

  checkEmailWhenForgotPassword(user: User) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/checkEmailWhenForgotPassword`, user);
  }

  checkOTPWhenForgotPassword(user: User) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/checkOTPWhenForgotPassword`, user);
  }

  /* 
  *  Cron to check if user session exists.
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
    location.reload();
    this.router.navigate(['/login']);    
  }
}
