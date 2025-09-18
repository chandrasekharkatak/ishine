import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { first } from 'rxjs/operators';
import { Log } from '../models/log';
import { environment } from 'src/environments/environment';
import { EncryptionService } from './EncryptionService';

@Injectable({
  providedIn: 'root'
})
export class LogService {
  private baseUrl:any = environment.baseUrl;
  private logSubject: BehaviorSubject<Log>;
  public log: Observable<Log>;
  logString: string | null;
  
  constructor(private http: HttpClient, private encryptionService: EncryptionService) {
    const encryptedLogString = sessionStorage.getItem('logInfo');
    if (encryptedLogString) {
      const decryptedString = this.encryptionService.decrypt(encryptedLogString);
      if (decryptedString) {
        try {
          this.logString = decryptedString;
        } catch (error) {
          console.error('Failed to parse decrypted log info:', decryptedString, error);
          this.logString = null;
        }
      } else {
        console.warn('Decryption returned empty string for log info.');
        this.logString = null;
      }
    } else {
      console.warn('No logInfo found in sessionStorage');
      this.logString = null;
    }
    this.logSubject = new BehaviorSubject<Log>(JSON.parse(this.logString));
    this.log = this.logSubject.asObservable();
  }

  public get getSessionInfo(): Log {
    return this.logSubject.value;
  }

  updateLogInfo(log:Log) {
    console.log("Updating log info: ", log);
    this.logSubject.next(log);
    this.setSessionInfo(log).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const encryptedLog = this.encryptionService.encrypt(JSON.stringify(log));
        sessionStorage.setItem('logInfo', encryptedLog);
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
