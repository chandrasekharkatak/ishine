import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { first } from 'rxjs/operators';
import { Log } from '../models/log';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LogService {
  private baseUrl:any = environment.baseUrl;
  private logSubject: BehaviorSubject<Log>;
  public log: Observable<Log>;
  logString: string | null;
  
  constructor(private http: HttpClient) {
    this.logString = sessionStorage.getItem('logInfo');
    this.logSubject = new BehaviorSubject<Log>(JSON.parse(this.logString));
    this.log = this.logSubject.asObservable();
  }

  public get getSessionInfo(): Log {
    return this.logSubject.value;
  }

  updateLogInfo(log:Log) {
    this.logSubject.next(log);
    this.setSessionInfo(log).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        sessionStorage.setItem('logInfo', JSON.stringify(log));
        //console.log("logInfo : ", response.serviceResponse);
        
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  setSessionInfo(log: Log) {
    return this.http.post(`${this.baseUrl}` + `api/setSessionInfo`, log);
  }

}
