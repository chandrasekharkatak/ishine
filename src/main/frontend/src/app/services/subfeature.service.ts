import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Feature } from '../models/feature';
import { JobRole } from '../models/jobRole';
import { SubFeature } from '../models/subFeature';
import { environment } from 'src/environments/environment';
import { FeatureUsageLog } from '../models/featureUsageLog';

@Injectable({
  providedIn: 'root'
})
export class SubfeatureService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  getSubfeaturesByJobRoleId(jobRoleObj: JobRole) {
    return this.http.post(`${this.baseUrl}` + `api/getSubfeaturesByJobRoleId`, jobRoleObj);
  }

  getAllSubFeatures() {
    return this.http.get(`${this.baseUrl}` + `api/getAllSubFeatures`);
  }

  updateRoleFeatureMapping(updateFeatureObj: Feature) {
    return this.http.post(`${this.baseUrl}` + `api/updateRoleFeatureMapping`, updateFeatureObj);
  }

  saveFeatureUsageLog(featureUsageLog: FeatureUsageLog) {
    return this.http.post(`${this.baseUrl}` + `api/saveFeatureUsageLog`, featureUsageLog);
  }
}
