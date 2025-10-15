import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Domain } from '../models/domain';
import { environment } from 'src/environments/environment';
import { CustomQueryDetails } from '../models/customQueryDetails';
import { Observable } from 'rxjs';

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

  billableFile(formData : FormData){
    return this.http.post(`${this.baseUrl}`+`api/upload/billableFile`,formData);
  }


  skillFile(file:any,skillCertConfigObj:any){
     const formDataFile = new FormData();
  formDataFile.append('dto', new Blob([JSON.stringify(skillCertConfigObj)], { type: 'application/json' }));
  formDataFile.append('doc1', file);
    return this.http.post(`${this.baseUrl}` + `api/uploadSkillBulk`, formDataFile);
  }

  certficateFile(file:any,skillCertConfigObj:any){
     const formDataFile = new FormData();
  formDataFile.append('dto', new Blob([JSON.stringify(skillCertConfigObj)], { type: 'application/json' }));
  formDataFile.append('doc1', file);
    return this.http.post(`${this.baseUrl}` + `api/uploadCertificateBulk`, formDataFile);
  }
  saveExcelDataForManagerMapping(formData : FormData){
    return this.http.post(`${this.baseUrl}`+`api/upload/saveExcelDataForManagerMapping`,formData);
  }
  designationBulkUpload(formData : FormData){
    return this.http.post(`${this.baseUrl}`+`api/upload/designationBulkUpload`,formData);
  }
  onConfirmationDateUpload(formData : FormData){
    return this.http.post(`${this.baseUrl}`+`api/upload/confirmationDateBulkUpload`,formData);
  }
  employeeBulkUpload(formData : FormData){
    return this.http.post(`${this.baseUrl}`+`api/employeeBulkUpload`,formData);
  }

  saveCustomQueryDetails(customQueryObj : CustomQueryDetails):Observable<any>{
    return this.http.post(`${this.baseUrl}`+`api/saveCustomQueryDetails`,customQueryObj);
  }

  getCustomQueries(): Observable<any> {
    return this.http.get(`${this.baseUrl}`+`api/getCustomQueries`);
  }
}
