import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
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

  fetchReimbursementDataforApproval(reimbursementObj: MyReimbursement) {
    return this.http.post(`${this.baseUrl}` + `api/fetchReimbursementDataforApproval`, reimbursementObj);
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

  uploadFileReimbursement(formData:any){
    return this.http.post(`${this.baseUrl}`+`api/uploadFileReimbursement`,formData);
  }

  fetchTotalReimbursementData(reimbursementObj: MyReimbursement) {
    return this.http.post(`${this.baseUrl}` + `api/fetchTotalReimbursementData`, reimbursementObj);
  }

  fetchAllInvoice(){
    return this.http.get(`${this.baseUrl}` + `api/getAllInvoices`);
  }

  getAllDocumentsReimbursmentThroughRequestId(requestId: any) {
    return this.http.get(`${this.baseUrl}api/getAllDocumentsReimbursmentThroughRequestId?requestId=${requestId}`);
  }

  previewDocumentReimbursment(details:any){
    return this.http.post(`${this.baseUrl}`+`api/previewDocumentReimbursment`,details);
  }

  updateInvoicesDetailsByAccountsTeam(details:any){
    return this.http.post(`${this.baseUrl}`+`api/updateInvoicesDetailsByAccountsTeam`,details);
  }
  
  updateReimbursementDetailsByAccountsTeam(details:any){
    return this.http.post(`${this.baseUrl}`+`api/updateReimbursementDetailsByAccountsTeam`,details);
  }

  markAsPaid(details:any){
    return this.http.post(`${this.baseUrl}`+`api/markAsPaid`,details);
  }
  //new aded

  saveExpenditureType(reason: any) {
    return this.http.post(`${this.baseUrl}`+`api/saveExpenditureType`, reason);
  }

  onGetExpenditureType() {
    return this.http.get(`${this.baseUrl}` + `api/onGetExpenditureType`);
  }

  saveTravelMode(travelModeObj: any) {
    return this.http.post(`${this.baseUrl}`+`api/saveReimbursementTravelMode`, travelModeObj);
  }

  getTravelMode() {
    return this.http.get(`${this.baseUrl}` + `api/getReimbursementTravelMode`);
  }

  saveVehicleType(reason: any) {
    return this.http.post(`${this.baseUrl}`+`api/saveVehicleType`, reason);
  }

  onGetVehicleType() {
    return this.http.get(`${this.baseUrl}` + `api/onGetVehicleType`);
  }
  
  
}
