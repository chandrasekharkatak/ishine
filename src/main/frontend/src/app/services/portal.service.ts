import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Portal } from 'src/app/models/portal';
import { enableAppreciation } from '../models/enableAppreciation';
import { Timesheet } from '../models/timesheet';

@Injectable({
  providedIn: 'root'
})
export class PortalService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  getPortalConfig() {
    return this.http.get(`${this.baseUrl}` + `api/getPortalConfig`);
  }

  updatePortalConfig(portalObj: Portal) {
    return this.http.post(`${this.baseUrl}` + `api/updatePortalConfig`, portalObj);
  }

  generatePerviousMonthDSR() {
    return this.http.get(`${this.baseUrl}` + `api/generatePerviousMonthDSR`);
  }

  getAllEmployees() {
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployees`);
  }

  enableAppreciation(data: any) {
    return this.http.post(`${this.baseUrl}` + `api/enableAppreciation`, data);
  }

  getAllEvent() {
    return this.http.get(`${this.baseUrl}` + `api/getAllAppreciationEvent`);
  }

  viewAppreciations(appreciationObj: enableAppreciation) {
    return this.http.post(`${this.baseUrl}` + `api/viewAppreciation`, appreciationObj);
  }

  OnCheckEventName(appreciationObj: enableAppreciation) {
    return this.http.post(`${this.baseUrl}` + `api/OnCheckEventName`, appreciationObj);
  }

  updateAppreciationEvent(appreciationObj: enableAppreciation) {
    return this.http.post(`${this.baseUrl}` + `api/updateAppreciationEvent`, appreciationObj);
  }

  deleteAppreciationEvent(appreciationObj: enableAppreciation) {
    return this.http.post(`${this.baseUrl}` + `api/deleteAppreciationEvent`, appreciationObj);
  }

  generateAllEmployeeDSR(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/generateAllEmployeeDSR`, timesheetObj);
  }
}
