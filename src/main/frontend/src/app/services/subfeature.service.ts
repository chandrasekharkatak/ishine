import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Feature } from '../models/feature';
import { JobRole } from '../models/jobRole';
import { SubFeature } from '../models/subFeature';

@Injectable({
  providedIn: 'root'
})
export class SubfeatureService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  getSubfeaturesByJobRoleId(jobRoleObj: JobRole) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getSubfeaturesByJobRoleId`, jobRoleObj);
  }

  getAllSubFeatures() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllSubFeatures`);
  }

  updateRoleFeatureMapping(updateFeatureObj: Feature) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateRoleFeatureMapping`, updateFeatureObj);
  }
}
