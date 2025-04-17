import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { MyReimbursementComponent } from '../reimbursement/my-reimbursement/my-reimbursement.component';
import { MyReimbursement } from '../models/reimbursement';

@Injectable({
  providedIn: 'root'
})
export class ReimbursementService { 

  private baseUrl:any = environment.baseUrl;
  
  constructor(private http: HttpClient) { }

  fetchReimbursementData(reimbursementObj: MyReimbursement) {
    return this.http.post(`${this.baseUrl}` + `api/fetchReimbursementData`, reimbursementObj);
  }

  saveReimbursementData(reimbursementObj: MyReimbursement) {
    return this.http.post(`${this.baseUrl}` + `api/saveReimbursementData`, reimbursementObj);
  }

  updateReimbursementData(reimbursementObj: MyReimbursement) {
    return this.http.post(`${this.baseUrl}` + `api/updateReimbursementData`, reimbursementObj);
  }

  revokeReimbursement(reimbursementObj: MyReimbursement) {
    return this.http.post(`${this.baseUrl}` + `api/revokeReimbursement`, reimbursementObj);
  }

  approveOrRejectReimbursement(reimbursementObj: MyReimbursement) {
    return this.http.post(`${this.baseUrl}` + `api/approveOrRejectReimbursement`, reimbursementObj);
  }

  uploadFileReimbursement(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/uploadFileReimbursement`,formData);
  }
}
