import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Employee } from '../models/employee';
import { EventPhoto } from '../models/EventPhoto';

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";
  
  constructor(private http: HttpClient) { }

  uploadMultipleImages(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/uploadMultipleImages`,formData);
  }

  getAllEventPhotos() {
    return this.http.get(`${this.baseUrl}` + `api/getAllEventPhotos`);
  }

  deleteEventPhoto(imageObj: EventPhoto) {
    return this.http.post(`${this.baseUrl}` + `api/deleteEventPhoto`, imageObj);
  }

  /* Document Upload */
  uploadEmployeeDocument(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/uploadEmployeeDocument`,formData);
  }

  saveEmployeeDocuments(employeeObj: Employee){
    return this.http.post(`${this.baseUrl}` + `api/saveEmployeeDocuments`, employeeObj);
  }

  getEmployeeDocuments(employeeObj: Employee){
    return this.http.post(`${this.baseUrl}` + `api/getEmployeeDocuments`, employeeObj);
  }
}
