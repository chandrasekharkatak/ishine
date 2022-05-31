import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CrudMapping } from '../models/crudMapping';
import { UserType } from '../models/userType';

@Injectable({
  providedIn: 'root'
})
export class UsertypeService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  /* ----- User Type ----- */
  createUserType(userTypeObj:UserType) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/createUserType`, userTypeObj);
  }

  getAllUserTypes() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllUserTypes`);
  }

  
  /* ----- CRUD Mappping ----- */
  updateCrudMappingsByMapId(crudMappingObj:CrudMapping) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateCrudMappingsByMapId`, crudMappingObj);
  }

  getUserTypeCrudMappingsByUserTypeId(crudMappingObj:CrudMapping) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getUserTypeCrudMappingsByUserTypeId`, crudMappingObj);
  }

  getAllUserTypeCrudMappings() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllUserTypeCrudMappings`);
  }
}
