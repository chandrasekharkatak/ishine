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


  // getTravelModeByReason(travelModeObj: any) {
  //   console.log(travelModeObj);
  //   return this.http.post(`${this.baseUrl}`+`api/getTravelModeByReason`, travelModeObj);
  // }

  getTravelModeByReason(travelReason: string) {
    console.log(travelReason);
    return this.http.post(`${this.baseUrl}api/getTravelModeByReason`, travelReason, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  saveHotelCategory(reason: any) {
    return this.http.post(`${this.baseUrl}`+`api/saveHotelCategory`, reason);
  }

  getHotelCategory() {
    return this.http.get(`${this.baseUrl}` + `api/getHotelCategory`);
  }
  

  saveHotelSubCategory(reason: any) {
    return this.http.post(`${this.baseUrl}`+`api/saveHotelSubCategory`, reason);
  }

  getHotelSubCategory() {
    return this.http.get(`${this.baseUrl}` + `api/getHotelSubCategory`);
  }

  saveCity(reason: any) {
    return this.http.post(`${this.baseUrl}`+`api/saveCity`, reason);
  }

  getTravelClassByMode(travelReason: string) {
    console.log(travelReason);
    return this.http.post(`${this.baseUrl}api/getTravelClassByMode`, travelReason, {
      headers: { 'Content-Type': 'application/json' }
    });
  }
  


  getAllInvoicesByEmpId(travelModeObj: any) {
    return this.http.post(`${this.baseUrl}`+`api/getAllInvoicesByEmpId`, travelModeObj);
  }
  
  updateReimbursmentBasedOnTravelRequest(travelDetails:any){
    return this.http.post(`${this.baseUrl}` + `api/updateReimbursmentBasedOnTravelRequest`, travelDetails);
  }
  
  updateUploadedFile(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/updateUploadedFile`,formData);
  }

  getCityBySubCategory(travelReason: string) {
    console.log(travelReason);
    return this.http.post(`${this.baseUrl}api/getCityBySubCategory`, travelReason, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  getCity() {
    return this.http.get(`${this.baseUrl}` + `api/getCity`);
  }
  
  
  onGetTravelCass() {
    return this.http.get(`${this.baseUrl}` + `api/onGetTravelCass`);
  }
  
  uploadKycDocument(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/uploadKycDocument`,formData);
  }
  
}
