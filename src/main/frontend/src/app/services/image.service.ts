import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { EventPhoto } from '../models/EventPhoto';

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";
  
  constructor(private http: HttpClient) { }

  uploadMultipleImages(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`employeeportal/api/uploadMultipleImages`,formData);
  }

  getAllEventPhotos() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllEventPhotos`);
  }

  deleteEventPhoto(imageObj: EventPhoto) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/deleteEventPhoto`, imageObj);
  }

  /* Document Upload */
  uploadEmployeeDocument(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`employeeportal/api/uploadEmployeeDocument`,formData);
  }
}
