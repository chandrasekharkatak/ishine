import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { MyTravelDesk } from '../models/travelDesk';

@Injectable({
  providedIn: 'root'
})
export class TravelDeskService {

  private baseUrl:any = environment.baseUrl;
  
  constructor(private http: HttpClient) { }

  

  fetchTravelData(travelDeskObj: MyTravelDesk) {
    return this.http.post(`${this.baseUrl}` + `api/fetchTravelData`, travelDeskObj);
  }

  fetchTravelDataForApproval(travelDeskObj: MyTravelDesk) {
    return this.http.post(`${this.baseUrl}` + `api/fetchTravelDataForApproval`, travelDeskObj);
  }

  saveTravelData(travelDeskObj: MyTravelDesk) {
    return this.http.post(`${this.baseUrl}` + `api/saveTravelData`, travelDeskObj);
  }

  uploadFile(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/uploadFile`,formData);
  }

  updateTravelData(travelDeskObj: MyTravelDesk) {
    return this.http.post(`${this.baseUrl}` + `api/updateTravelData`, travelDeskObj);
  }

  revokeTravel(travelDeskObj: MyTravelDesk) {
    return this.http.post(`${this.baseUrl}` + `api/revokeTravel`, travelDeskObj);
  }

  approveOrRejectTraveldesk(travelDeskObj: MyTravelDesk) {
    return this.http.post(`${this.baseUrl}` + `api/approveOrRejectTravel`, travelDeskObj);
  }
  submitReimbursmentBasedOnTravelRequest(travelDetails:any){
    return this.http.post(`${this.baseUrl}` + `api/submitReimbursmentBasedOnTravelRequest`, travelDetails);
  }

  uploadFileTravelBased(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/uploadFiletravelBased`,formData);
  }

  checkInvoiceNumberPresentorNot(invoiceDetails:any){
    return this.http.post(`${this.baseUrl}`+`api/checkInvoiceNumberPresentorNot`,invoiceDetails);
  }

  previewDocument(details:any){
    return this.http.post(`${this.baseUrl}`+`api/previewDocument`,details);
  }
  
  totalTravelData(details:any){
    return this.http.post(`${this.baseUrl}`+`api/totalTravelData`,details);
  }
  
  getAllDocumentsThroughRequestId(requestId: any) {
  return this.http.get(`${this.baseUrl}api/getAllDocumentsThroughRequestId?requestId=${requestId}`);
}

  

  saveTravelReason(reason: any) {
    return this.http.post(`${this.baseUrl}`+`api/travel-reason/create`, reason);
  }

  getTravelReason() {
    return this.http.get(`${this.baseUrl}` + `api/getTravelReason`);
  }

  saveTravelMode(travelModeObj: any) {
    return this.http.post(`${this.baseUrl}`+`api/saveTravelMode`, travelModeObj);
  }

  getTravelMode() {
    return this.http.get(`${this.baseUrl}` + `api/getTravelMode`);
  }
  uploadTicket(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/uploadTicket`,formData);
  }

  saveTravelClass(travelModeObj: any) {
    return this.http.post(`${this.baseUrl}`+`api/saveTravelClass`, travelModeObj);
  }
  
}
