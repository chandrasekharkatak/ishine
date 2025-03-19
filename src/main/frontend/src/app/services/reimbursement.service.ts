import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { MyReimbursementComponent } from '../reimbursement/my-reimbursement/my-reimbursement.component';

@Injectable({
  providedIn: 'root'
})
export class ReimbursementService { 

  private baseUrl:any = environment.baseUrl;
  
  constructor(private http: HttpClient) { }

  fetchReimbursementData(reimbursementObj: MyReimbursementComponent) {
    return this.http.post(`${this.baseUrl}` + `api/fetchReimbursementData`, reimbursementObj);
  }

  saveReimbursementData(reimbursementObj: MyReimbursementComponent) {
    return this.http.post(`${this.baseUrl}` + `api/saveReimbursementData`, reimbursementObj);
  }

  updateReimbursementData(reimbursementObj: MyReimbursementComponent) {
    return this.http.post(`${this.baseUrl}` + `api/updateReimbursementData`, reimbursementObj);
  }

  revokeReimbursement(reimbursementObj: MyReimbursementComponent) {
    return this.http.post(`${this.baseUrl}` + `api/revokeReimbursement`, reimbursementObj);
  }

  approveOrRejectReimbursement(reimbursementObj: MyReimbursementComponent) {
    return this.http.post(`${this.baseUrl}` + `api/approveOrRejectReimbursement`, reimbursementObj);
  }
}
