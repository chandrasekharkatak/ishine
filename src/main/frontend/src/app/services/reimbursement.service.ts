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

  // createEmployee(reimbursementObj: MyReimbursementComponent) {
  //   return this.http.post(`${this.baseUrl}` + `api/createReimbursement`, reimbursementObj);
  // }
}
