import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Employee } from '../models/employee';
import { EventPhoto } from '../models/EventPhoto';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private baseUrl:any = environment.baseUrl;
  
  constructor(private http: HttpClient) { }

  uploadMultipleImages(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/uploadMultipleImages`,formData);
  }

  getAllEventPhotos() {
    return this.http.get(`${this.baseUrl}` + `api/getAllEventPhotos`);
  }

  // getAllEventPhotosForHome
  getAllEventPhotosForHome() {
    return this.http.get(`${this.baseUrl}` + `api/getAllEventPhotosForHome`);
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
