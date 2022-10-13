
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Portal } from 'src/app/models/portal';
import { UploadPolicy } from '../models/UploadPolicy';
import { Observable} from 'rxjs';









@Injectable({
  providedIn: 'root'
})
export class UploadPoliciesService {
  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";


  constructor(private http: HttpClient) { }

  uploadMultipleFiles(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`employeeportal/api/uploadPolicies`,formData);
  }
  getAllDocument() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllDocument`);
  }
  deleteDocument(fileObj: UploadPolicy) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/deletePolicyDocument`, fileObj);
  }
  downloadDocument(policyID: string) {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/downloadDocument/${policyID}`, {
      responseType: 'blob'
    });
  }



}