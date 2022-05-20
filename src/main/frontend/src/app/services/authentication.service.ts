import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";
  private currentUserSubject: BehaviorSubject<User>;
  public currentUser: Observable<User>;
  sessionItem: string | null;

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

setcurrentUserSubject(user: User){
    this.currentUserSubject.next(user);
}

}
