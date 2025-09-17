import { Injectable, NgZone, OnDestroy, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, Subscription, timer } from 'rxjs';
import { User } from '../models/user';
import { environment } from 'src/environments/environment';
import { EncryptionService } from './EncryptionService';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService implements OnDestroy,OnInit {

  private baseUrl: string = environment.baseUrl;
  private currentUserSubject: BehaviorSubject<User>;
  public currentUser: Observable<User>;
  
  sessionItem: string | null;
  timerId: any;
  sessionString: string;
  sessionTimeout:number;
  // private sessionString: string;
  private sessionSubscription?: Subscription;
  private idleTimer?: any;
  private idleTimeLimit = 5 * 60 * 1000; // 5 minutes (adjust as needed)

  constructor(private http: HttpClient, private router: Router, private ngZone: NgZone, private encryptionService: EncryptionService) {
    const encryptedUser = sessionStorage.getItem('currentUser');
    if (encryptedUser) {
      const decryptedString = this.encryptionService.decrypt(encryptedUser);
      if (decryptedString) {
        try {
          this.sessionItem = JSON.parse(decryptedString);
        } catch (error) {
          console.error('Failed to parse decrypted session user:', decryptedString, error);
          this.sessionItem = null;
        }
      } else {
        console.warn('Decryption returned empty string.');
        this.sessionItem = null;
      }
    } else {
      console.warn('No currentUser found in sessionStorage');
      this.sessionItem = null;
    }    // this.sessionItem = sessionStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<User>(JSON.parse(this.sessionItem));
    this.currentUser = this.currentUserSubject.asObservable();

    // Start user activity listeners
    this.startUserActivityTracking();

  }
  ngOnInit(): void {
    this.resetIdleTimer();
    if (sessionStorage.getItem('sessioncheck')) {
      this.startUserSessionCheck();
    }
  }
  ngOnDestroy(): void {
    this.stopUserSessionCheck();
    if (this.idleTimer) clearTimeout(this.idleTimer);
    this.removeUserActivityListeners();
  }

  public get currentUserValue(): User {
    return this.currentUserSubject.value;
  }

  setcurrentUserSubject(user: User) {
    if (user) {
    const encrypted = this.encryptionService.encrypt(JSON.stringify(user));
        sessionStorage.setItem('currentUser', encrypted);
        }
    else {
      sessionStorage.removeItem('currentUser');
    }
    this.currentUserSubject.next(user);
  }

  authenticateUser(user: User) {
    return this.http.post(`${this.baseUrl}api/authenticateUser`, user);
  }

  authenticateUserWithOTP(user: User) {
    return this.http.post(`${this.baseUrl}api/authenticateUserWithOTP`, user);
  }

  checkUserSession(user: User) {
    return this.http.post(`${this.baseUrl}api/checkUserSession`, user);
  }

  logoutUser(user: User) {
    return this.http.post(`${this.baseUrl}api/logoutUser`, user);
  }

  checkEmailWhenForgotPassword(user: User) {
    return this.http.post(`${this.baseUrl}api/checkEmailWhenForgotPassword`, user);
  }

  checkOTPWhenForgotPassword(user: User) {
    return this.http.post(`${this.baseUrl}api/checkOTPWhenForgotPassword`, user);
  }

  resendOTP(user: User) {
    return this.http.post(`${this.baseUrl}api/resendOTP`, user);
  }

  /** Session Check Logic */
  checkSession() {
    if (!this.currentUserValue) return;

    let user = new User();
    user.empId = this.currentUserValue.empId;
    user.sessionString = this.sessionString;

    this.checkUserSession(user).subscribe((response: any) => {
      if (response.serviceStatus !== "Success") {
        console.error(response.serviceResponse);
        this.stopUserSessionCheck();
        this.userLogout();
      }
    });
  }

  startUserSessionCheck() {
    sessionStorage.setItem('sessioncheck', 'true');
    console.log('▶️ Starting user session check');
    this.sessionSubscription = timer(0, 30000).subscribe(() => this.checkSession());
  }

  stopUserSessionCheck() {
    console.log('⏸️ Stopping user session check');
    if (this.sessionSubscription) {
      this.sessionSubscription.unsubscribe();
    }
    sessionStorage.removeItem('sessioncheck');
  }

  /** 🖱️ Idle/Active Tracking */
  private startUserActivityTracking() {
    this.ngZone.runOutsideAngular(() => {
      document.addEventListener('mousemove', this.resetIdleTimer);
      document.addEventListener('keydown', this.resetIdleTimer);
      document.addEventListener('click', this.resetIdleTimer);
    });

    this.resetIdleTimer(); // initialize on load
  }

  private removeUserActivityListeners() {
    document.removeEventListener('mousemove', this.resetIdleTimer);
    document.removeEventListener('keydown', this.resetIdleTimer);
    document.removeEventListener('click', this.resetIdleTimer);
  }

  private resetIdleTimer = () => {
    if (this.idleTimer) clearTimeout(this.idleTimer);

    // Resume session check if not running
    if (!this.sessionSubscription || this.sessionSubscription.closed) {
      this.ngZone.run(() => this.startUserSessionCheck());
    }

    // Stop session check after being idle for X minutes
    this.idleTimer = setTimeout(() => {
      this.ngZone.run(() => this.stopUserSessionCheck());
    }, this.idleTimeLimit);
  };

  userLogout() {
    this.stopUserSessionCheck();
    sessionStorage.clear();
    this.deleteCookies();
    this.setcurrentUserSubject(null);
    this.router.navigate(['/login']);
    setTimeout(() => location.reload());
  }

  getCookie(name: string) {
    let ca: Array<string> = document.cookie.split(';');
    let cookieName = `${name}=`;
    for (let c of ca) {
      c = c.trim();
      if (c.indexOf(cookieName) === 0) {
        return c.substring(cookieName.length, c.length);
      }
    }
    return '';
  }

  deleteCookies() {
    document.cookie.split(";").forEach(c => {
      document.cookie = c.replace(/^ +/, "")
        .replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
    });
  }

  setCookie(params: any) {
    let d: Date = new Date();
    d.setTime(d.getTime() + (params.expireDays ?? 1) * 24 * 60 * 60 * 1000);
    document.cookie = `${params.name ?? ''}=${params.value ?? ''};` +
      `${params.session ? '' : 'expires=' + d.toUTCString() + ';'}path=${params.path ?? '/'};` +
      `${location.protocol === 'https:' && params.secure ? 'secure' : ''}`;
  }

}
