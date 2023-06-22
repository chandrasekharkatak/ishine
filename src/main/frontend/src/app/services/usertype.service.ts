import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CrudMapping } from '../models/crudMapping';
import { UserType } from '../models/userType';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UsertypeService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  /* ----- User Type ----- */
  createUserType(userTypeObj:UserType) {
    return this.http.post(`${this.baseUrl}` + `api/createUserType`, userTypeObj);
  }

  getAllUserTypes() {
    return this.http.get(`${this.baseUrl}` + `api/getAllUserTypes`);
  }

  
  /* ----- CRUD Mappping ----- */
  updateCrudMappingsByMapId(crudMappingObj:CrudMapping) {
    return this.http.post(`${this.baseUrl}` + `api/updateCrudMappingsByMapId`, crudMappingObj);
  }

  getUserTypeCrudMappingsByUserTypeId(crudMappingObj:CrudMapping) {
    return this.http.post(`${this.baseUrl}` + `api/getUserTypeCrudMappingsByUserTypeId`, crudMappingObj);
  }

  getAllUserTypeCrudMappings() {
    return this.http.get(`${this.baseUrl}` + `api/getAllUserTypeCrudMappings`);
  }
}
