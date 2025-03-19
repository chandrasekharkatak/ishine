import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { MyTravelrequestComponent } from '../travel-allowance/my-travelrequest/my-travelrequest.component';

@Injectable({
  providedIn: 'root'
})
export class TravelDeskService {

  private baseUrl:any = environment.baseUrl;
  
  constructor(private http: HttpClient) { }

  

  fetchTravelData(travelDeskObj: MyTravelrequestComponent) {
    return this.http.post(`${this.baseUrl}` + `api/fetchTravelData`, travelDeskObj);
  }

  saveTravelData(travelDeskObj: MyTravelrequestComponent) {
    return this.http.post(`${this.baseUrl}` + `api/saveTravelData`, travelDeskObj);
  }

  updateTravelData(travelDeskObj: MyTravelrequestComponent) {
    return this.http.post(`${this.baseUrl}` + `api/updateTravelData`, travelDeskObj);
  }

  revokeTravel(travelDeskObj: MyTravelrequestComponent) {
    return this.http.post(`${this.baseUrl}` + `api/revokeTravel`, travelDeskObj);
  }

  approveOrRejectTraveldesk(travelDeskObj: MyTravelrequestComponent) {
    return this.http.post(`${this.baseUrl}` + `api/approveOrRejectTraveldesk`, travelDeskObj);
  }
}
