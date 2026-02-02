import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TrainingService {

  private baseUrl: any = environment.baseUrl;

  constructor(private http: HttpClient) { }

 

  // Create training with content in single request (FormData for file support)
  createTrainingWithContent(formData: FormData) {
    return this.http.post(`${this.baseUrl}api/training/createTrainingWithContent`, formData);
  }

  // Update training with content in single request (FormData for file support)
  updateTrainingWithContent(formData: FormData) {
    return this.http.post(`${this.baseUrl}api/training/updateTrainingWithContent`, formData);
  }
  getAllTrainings(activeStatus?: string, mandatoryFlag?: string) {
    let params = new HttpParams();
    if (activeStatus) {
      params = params.set('activeStatus', activeStatus);
    }
    if (mandatoryFlag) {
      params = params.set('mandatoryFlag', mandatoryFlag);
    }
    return this.http.get(`${this.baseUrl}api/training/getAllTrainings`, { params });
  }

  addTrainingContent(formData: FormData) {
    return this.http.post(`${this.baseUrl}api/training/addTrainingContent`, formData);
  }


  getTrainingContent(trainingId: number) {
    return this.http.get(`${this.baseUrl}api/training/getTrainingContent/${trainingId}`);
  }

  deactivateTraining(trainingId: number, updatedBy: number) {
    return this.http.post(`${this.baseUrl}api/training/deactivateTraining`, {
      trainingId: trainingId,
      updatedBy: updatedBy
    });
  }

  // Employee Training APIs
  getPendingTraining(empId: number) {
    return this.http.post(`${this.baseUrl}api/training/getPendingTraining`, { empId: empId });
  }

  getUserTrainings(empId: number) {
    return this.http.post(`${this.baseUrl}api/training/getUserTrainings`, { empId: empId });
  }

  submitConsent(consentData: any) {
    return this.http.post(`${this.baseUrl}api/training/submitConsent`, consentData);
  }

  skipTraining(skipData: any) {
    return this.http.post(`${this.baseUrl}api/training/skipTraining`, skipData);
  }

  getLockStatus(empId: number) {
    return this.http.post(`${this.baseUrl}api/training/getLockStatus`, { empId: empId });
  }


  downloadContent(contentId: number) {
    return this.http.get(`${this.baseUrl}api/training/downloadContent/${contentId}`, {
      responseType: 'blob',
      observe: 'response' // Get full response with headers
    });
  }



  checkTrainingFrequency(empId: number, trainingId: number) {
    return this.http.post(`${this.baseUrl}api/training/checkTrainingFrequency`, {
      empId: empId,
      trainingId: trainingId
    });
  }

  // Reporting APIs
  getEmployeeTrainingHistory(empId: number, trainingId?: number) {
    return this.http.post(`${this.baseUrl}api/training/getEmployeeTrainingHistory`, {
      empId: empId,
      trainingId: trainingId
    });
  }

  getComplianceReport(trainingId: number, departmentId?: number, status?: string) {
    return this.http.post(`${this.baseUrl}api/training/getComplianceReport`, {
      trainingId: trainingId,
      departmentId: departmentId,
      status: status
    });
  }
}
