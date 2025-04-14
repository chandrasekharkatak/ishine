import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { MyTravelrequestComponent } from '../travel-allowance/my-travelrequest/my-travelrequest.component';
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
}
