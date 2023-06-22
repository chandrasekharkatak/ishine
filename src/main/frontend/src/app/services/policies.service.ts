import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { UploadPolicy } from '../models/UploadPolicy';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})
export class PoliciesService {

  private baseUrl:any = environment.baseUrl;

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
  
  isAllPolicyRead(policyObj : UploadPolicy) {
    return this.http.post(`${this.baseUrl}` + `api/isAllPolicyRead`, policyObj);
  }


}

