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


  saveFoodType(reason: any) {
    return this.http.post(`${this.baseUrl}`+`api/saveFoodType`, reason);
  }

  onGetFoodType() {
    return this.http.get(`${this.baseUrl}` + `api/onGetFoodType`);
  }

  deleteExpenditureType(id: number) {
    return this.http.post(`${this.baseUrl}api/deleteExpenditureType`, { id });
  }

  deleteReimbursementTravelMode(travelModeId: number) {
    return this.http.post(`${this.baseUrl}api/deleteReimbursementTravelMode`, { travelModeId });
  }

  deleteVehicleType(vehicleTypeId: number) {
    return this.http.post(`${this.baseUrl}api/deleteVehicleType`, { vehicleTypeId });
  }

  deleteFoodType(foodTypeId: number) {
    return this.http.post(`${this.baseUrl}api/deleteFoodType`, { foodTypeId });
  }

  saveReimbursementTicket(body: any) {
    return this.http.post(`${this.baseUrl}api/saveReimbursementTicket`, body);
  }

  fetchReimbursementClaimProjectOptions(body: { empId: any }) {
    return this.http.post(`${this.baseUrl}api/fetchReimbursementClaimProjectOptions`, body);
  }

  /** Client list from {@code clients} table (for BD Others dropdown). */
  fetchReimbursementClientsFromMaster() {
    return this.http.post(`${this.baseUrl}api/fetchReimbursementClientsFromMaster`, {});
  }

  fetchMyReimbursementTickets(body: { empId: any }) {
    return this.http.post(`${this.baseUrl}api/fetchMyReimbursementTickets`, body);
  }

  fetchReimbursementTicketsForApproval(body: { empId: any; email: string }) {
    return this.http.post(`${this.baseUrl}api/fetchReimbursementTicketsForApproval`, body);
  }

  /** Every ticket assigned to this approver (any status), not only pending-at-stage queues. */
  fetchReimbursementTicketsAssignedAll(body: { empId: any; email: string }) {
    return this.http.post(`${this.baseUrl}api/fetchReimbursementTicketsAssignedAll`, body);
  }

  processReimbursementTicketHod(body: any) {
    return this.http.post(`${this.baseUrl}api/processReimbursementTicketHod`, body);
  }

  processReimbursementTicketHr(body: any) {
    return this.http.post(`${this.baseUrl}api/processReimbursementTicketHr`, body);
  }

  processReimbursementTicketFinance(body: any) {
    return this.http.post(`${this.baseUrl}api/processReimbursementTicketFinance`, body);
  }

  fetchReimbursementTicketAuditByTicketId(ticketId: number) {
    return this.http.post(`${this.baseUrl}api/fetchReimbursementTicketAuditByTicketId`, { ticketId });
  }

  fetchReimbursementDashboard(filter?: any) {
    return this.http.post(`${this.baseUrl}api/fetchReimbursementDashboard`, filter || {});
  }

  getAllReimbursementApprovalMatrices() {
    return this.http.get(`${this.baseUrl}api/getAllReimbursementApprovalMatrices`);
  }

  saveAllReimbursementApprovalMatrices(body: { createdBy: number; matrices: any[] }) {
    return this.http.post(`${this.baseUrl}api/saveAllReimbursementApprovalMatrices`, body);
  }

  saveReimbursementApprovalMatrix(body: { createdBy: number; matrices: any[] }) {
    return this.http.post(`${this.baseUrl}api/saveReimbursementApprovalMatrix`, body);
  }

  deleteReimbursementApprovalMatrix(matrixId: number) {
    return this.http.post(`${this.baseUrl}api/deleteReimbursementApprovalMatrix`, { matrixId });
  }

  resolveReimbursementApprovalMatrixForEmployee(empId: number) {
    return this.http.get(`${this.baseUrl}api/resolveReimbursementApprovalMatrixForEmployee`, {
      params: { empId: String(empId) }
    });
  }

  getReimbursementSubmissionSettings() {
    return this.http.get(`${this.baseUrl}api/getReimbursementSubmissionSettings`);
  }

  saveReimbursementSubmissionSettings(body: { monthlyDeadlineDay: number; enabled: boolean; updatedBy: number }) {
    return this.http.post(`${this.baseUrl}api/saveReimbursementSubmissionSettings`, body);
  }

  getReimbursementSubmissionWindowStatus() {
    return this.http.get(`${this.baseUrl}api/getReimbursementSubmissionWindowStatus`);
  }

}
