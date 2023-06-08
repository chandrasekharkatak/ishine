import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Domain } from '../models/domain';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DomainService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  createDomain(domainObj:Domain) {
    return this.http.post(`${this.baseUrl}` + `api/createDomain`, domainObj);
  }

  getAllDomain() {
    return this.http.get(`${this.baseUrl}` + `api/getAllDomain`);
  }

  getDomainSpecializationByDomainId(domainObj:Domain) {
    return this.http.post(`${this.baseUrl}` + `api/getDomainSpecializationByDomainId`, domainObj);
  }

  updateDomain(domainObj:Domain) {
    return this.http.post(`${this.baseUrl}` + `api/updateDomain`, domainObj);
  }

  deleteDomain(domainObj:Domain) {
    return this.http.post(`${this.baseUrl}` + `api/deleteDomain`, domainObj);
  }

  checkDomainName(domainObj:Domain) {
    return this.http.post(`${this.baseUrl}` + `api/checkDomainName`, domainObj);
  }

  getDomainSpecialization(domainObj:Domain) {
    return this.http.post(`${this.baseUrl}` + `api/getDomainSpecialization`, domainObj);
  }

  getDomainSpecializationByEmpId(domainObj:Domain) {
    return this.http.post(`${this.baseUrl}` + `api/getDomainSpecializationByEmpId`, domainObj);
  }
}
