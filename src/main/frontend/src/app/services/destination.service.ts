import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Designation } from '../models/designation';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DestinationService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  createDesignation(designationObj: Designation) {
    return this.http.post(`${this.baseUrl}` + `api/createDesignation`, designationObj);
  }

  getAllDesignation() {
    return this.http.get(`${this.baseUrl}` + `api/getAllDesignation`);
  }

  checkDesignationName(designationObj: Designation) {
    return this.http.post(`${this.baseUrl}` + `api/checkDesignationName`, designationObj);
  }

  getDesignationById(designationObj: Designation) {
    return this.http.post(`${this.baseUrl}` + `api/getDesignationById`, designationObj);
  }

  updateDesignation(designationObj: Designation) {
    return this.http.post(`${this.baseUrl}` + `api/updateDesignation`, designationObj);
  }

  getDesignationByDeptId(designationObj: Designation) {
    return this.http.post(`${this.baseUrl}` + `api/getDesignationByDeptId`, designationObj);
  }

  deleteDesignation(designationObj: Designation) {
    return this.http.post(`${this.baseUrl}` + `api/deleteDesignation`, designationObj);
  }

  changeEmployeeDesignationMapping(designationObj: Designation) {
    return this.http.post(`${this.baseUrl}` + `api/changeEmployeeDesignationMapping`, designationObj);
  }
}
