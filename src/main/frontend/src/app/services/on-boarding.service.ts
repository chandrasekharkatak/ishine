import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Asset } from '../models/asset'
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OnBoardingService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  getEmployeeOnBoardingDetailByEmployeementId(assetObj: Asset) {
    return this.http.post(`${this.baseUrl}` + `api/getEmployeeOnBoardingDetailByEmployeementId`, assetObj);
  }

  updateOnBoardingCheckList(assetObj: Asset) {
    return this.http.post(`${this.baseUrl}` + `api/updateOnBoardingCheckList`, assetObj);
  }

  getAssetDataFromSnipitPortal(assetObj: Asset) {
    return this.http.post(`${this.baseUrl}` + `api/getAssetDataFromSnipitPortal`, assetObj);
  }
}
