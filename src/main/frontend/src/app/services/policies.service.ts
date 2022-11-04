import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { UploadPolicy } from '../models/UploadPolicy';


@Injectable({
  providedIn: 'root'
})
export class PoliciesService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  getAllDocument() {
    return this.http.get(`${this.baseUrl}` + `api/getAllDocument`);
  }
  downloadDocument(policyID: string) {
    return this.http.get(`${this.baseUrl}` + `api/downloadDocument/${policyID}`, {
      responseType: 'blob'
    });
  }
  onReadPolicy(fileObj: UploadPolicy){
    return this.http.post(`${this.baseUrl}` + `api/setPolicyReadResponseByEmpId`, fileObj);

  }
  getReadPoliciesByEmpId(fileObj:UploadPolicy){
    return this.http.post(`${this.baseUrl}` + `api/getReadPoliciesByEmpId`, fileObj);
}
}

